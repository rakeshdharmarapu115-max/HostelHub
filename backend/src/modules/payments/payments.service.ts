import crypto from 'crypto';
import { prisma } from '../../config/prisma';
import { PaymentMethodEnum, PaymentStatusEnum, FeeStatusEnum, UserRole } from '../../types/enums';
import { fcmService } from '../../services/fcm.service';
import { emailService } from '../../services/email.service';

export class PaymentsService {
  /**
   * Create a real Razorpay Order for a student fee invoice.
   * Key secret stays securely on the backend.
   */
  async createRazorpayOrder(feeId: string, customAmount?: number, userId?: string, role?: string) {
    const keyId = process.env.RAZORPAY_KEY_ID;
    const keySecret = process.env.RAZORPAY_KEY_SECRET;

    if (!keyId || !keySecret) {
      throw {
        status: 503,
        message: 'Razorpay payment gateway credentials are not configured on server. Please configure RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.'
      };
    }

    const fee = await prisma.fee.findUnique({
      where: { id: feeId },
      include: {
        student: { include: { user: true } },
        hostel: true
      }
    });

    if (!fee) {
      throw { status: 404, message: `Fee invoice not found for ID: ${feeId}` };
    }

    // Authorization check: student can only pay their own fee
    if (userId && role === UserRole.STUDENT && fee.student.userId !== userId) {
      throw { status: 403, message: 'Unauthorized: You can only create payment orders for your own fee invoice.' };
    }

    const pendingAmount = Math.max(0, fee.amount - fee.amountPaid);
    if (pendingAmount <= 0 || fee.status === FeeStatusEnum.PAID) {
      throw { status: 400, message: 'This fee invoice has already been fully paid.' };
    }

    // Backend determines payable amount from actual fee record in database
    const payableAmount = (customAmount && typeof customAmount === 'number' && customAmount > 0 && customAmount <= pendingAmount)
      ? Number(customAmount)
      : pendingAmount;

    const amountInPaise = Math.round(payableAmount * 100);

    // Call real Razorpay Orders API
    const authHeader = 'Basic ' + Buffer.from(`${keyId}:${keySecret}`).toString('base64');
    const receiptId = `rcpt_${fee.id.substring(0, 8)}_${Date.now()}`;

    let razorpayOrderId: string;
    try {
      const response = await fetch('https://api.razorpay.com/v1/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': authHeader
        },
        body: JSON.stringify({
          amount: amountInPaise,
          currency: 'INR',
          receipt: receiptId,
          notes: {
            feeId: fee.id,
            feeTitle: fee.title,
            studentId: fee.studentId,
            studentName: fee.student.fullName,
            hostelId: fee.hostelId
          }
        })
      });

      const orderData: any = await response.json();

      if (!response.ok) {
        const errorDesc = orderData?.error?.description || response.statusText;
        console.error('[RAZORPAY] Order creation error response:', orderData);
        throw { status: 400, message: `Razorpay order creation failed: ${errorDesc}` };
      }

      razorpayOrderId = orderData.id;
    } catch (err: any) {
      if (err.status) throw err;
      console.error('[RAZORPAY] Network or communication error connecting to Razorpay API:', err);
      throw { status: 502, message: 'Unable to connect to Razorpay payment gateway. Please try again.' };
    }

    return {
      orderId: razorpayOrderId,
      amount: payableAmount,
      amountInPaise,
      currency: 'INR',
      keyId,
      feeId: fee.id,
      feeTitle: fee.title,
      studentName: fee.student.fullName,
      studentEmail: fee.student.user?.email || '',
      studentPhone: fee.student.emergencyContactPhone || fee.student.user?.phoneNumber || '',
      hostelName: fee.hostel?.name || 'Campus Hostel'
    };
  }

  /**
   * Securely verify Razorpay payment signatures on backend and idempotently record success.
   */
  async verifyRazorpayPayment(data: {
    feeId: string;
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature?: string;
    amountPaid?: number;
    userId?: string;
    role?: string;
  }) {
    const { feeId, razorpayOrderId, razorpayPaymentId, razorpaySignature, amountPaid, userId, role } = data;

    if (!feeId || !razorpayOrderId || !razorpayPaymentId) {
      throw { status: 400, message: 'Missing required Razorpay payment verification parameters.' };
    }

    if (!razorpaySignature) {
      throw { status: 400, message: 'Missing Razorpay cryptographic signature for verification.' };
    }

    const keySecret = process.env.RAZORPAY_KEY_SECRET;
    if (!keySecret) {
      throw { status: 503, message: 'Razorpay payment gateway secret is not configured on server.' };
    }

    // 1. Cryptographic HMAC SHA-256 signature verification
    const generatedSignature = crypto
      .createHmac('sha256', keySecret)
      .update(`${razorpayOrderId}|${razorpayPaymentId}`)
      .digest('hex');

    if (generatedSignature !== razorpaySignature) {
      throw { status: 400, message: 'Invalid Razorpay payment signature. Verification failed.' };
    }

    // 2. Idempotency Check: if this payment was already verified, return existing record
    const existingPayment = await prisma.payment.findFirst({
      where: {
        OR: [
          { razorpayPaymentId },
          { transactionReference: razorpayPaymentId }
        ]
      },
      include: { fee: true, student: true }
    });

    if (existingPayment && existingPayment.status === PaymentStatusEnum.SUCCESS) {
      return this.mapPayment(existingPayment, existingPayment.student?.fullName);
    }

    // 3. Perform database transaction to record payment and update fee atomically
    return prisma.$transaction(async (tx) => {
      const fee = await tx.fee.findUnique({
        where: { id: feeId },
        include: { student: { include: { user: true } }, hostel: true }
      });

      if (!fee) {
        throw { status: 404, message: `Fee not found for ID: ${feeId}` };
      }

      // Authorization check
      if (userId && role === UserRole.STUDENT && fee.student.userId !== userId) {
        throw { status: 403, message: 'Unauthorized: You can only verify payments for your own fee invoice.' };
      }

      const pendingBalance = Math.max(0, fee.amount - fee.amountPaid);
      const verifiedAmount = (amountPaid && typeof amountPaid === 'number' && amountPaid > 0 && amountPaid <= pendingBalance)
        ? Number(amountPaid)
        : (pendingBalance > 0 ? pendingBalance : Number(amountPaid) || fee.amount);

      const receiptUrl = `https://receipts.hostelhub.com/${razorpayPaymentId}.pdf`;

      // Create Payment record
      const payment = await tx.payment.create({
        data: {
          feeId: fee.id,
          studentId: fee.studentId,
          hostelId: fee.hostelId,
          amountPaid: verifiedAmount,
          paymentMethod: PaymentMethodEnum.UPI,
          transactionReference: razorpayPaymentId,
          razorpayOrderId,
          razorpayPaymentId,
          razorpaySignature: razorpaySignature || null,
          paymentDate: new Date(),
          receiptUrl,
          status: PaymentStatusEnum.SUCCESS,
          remarks: `Verified Razorpay payment (Order: ${razorpayOrderId})`
        },
        include: { student: true, fee: true }
      });

      // Update Fee status & amount paid
      const newAmountPaid = fee.amountPaid + verifiedAmount;
      const newStatus = newAmountPaid >= fee.amount ? FeeStatusEnum.PAID : FeeStatusEnum.PARTIALLY_PAID;

      await tx.fee.update({
        where: { id: fee.id },
        data: {
          amountPaid: newAmountPaid,
          status: newStatus
        }
      });

      // Notify Student via In-App Notification
      await tx.notification.create({
        data: {
          recipientUserId: fee.student.userId,
          title: '💳 Fee Payment Successful (₹)',
          body: `Payment of ₹${verifiedAmount} for "${fee.title}" has been confirmed. Ref: ${razorpayPaymentId}`,
          type: 'PAYMENT_CONFIRMED',
          relatedEntityId: payment.id
        }
      });

      // FCM and Email Alert
      fcmService.sendToUser(fee.student.userId, {
        title: '💳 Payment Received (₹)',
        body: `Payment of ₹${verifiedAmount} for "${fee.title}" recorded successfully. Ref: ${razorpayPaymentId}`,
        type: 'PAYMENT_CONFIRMED',
        relatedEntityId: payment.id
      }).catch(err => console.error('[FCM] Payment notification error:', err));

      if (fee.student.user?.email) {
        emailService.sendNotificationEmail(
          fee.student.user.email,
          'Payment Confirmation - HostelHub',
          `Dear ${fee.student.fullName},\n\nWe have received your payment of ₹${verifiedAmount} for "${fee.title}".\n\nPayment ID: ${razorpayPaymentId}\nDate: ${new Date().toLocaleString()}\nStatus: SUCCESS`
        ).catch(err => console.error('[EMAIL] Payment email error:', err));
      }

      // Record Audit Log
      await tx.auditLog.create({
        data: {
          userId: fee.student.userId,
          action: 'RAZORPAY_PAYMENT_SUCCESS',
          entityType: 'PAYMENT',
          entityId: payment.id,
          details: `Razorpay paid ₹${verifiedAmount} for fee ${fee.id} (${razorpayPaymentId})`
        }
      });

      return this.mapPayment(payment, fee.student.fullName);
    });
  }

  /**
   * Record payment failure or cancellation without modifying fee balances.
   */
  async recordPaymentFailure(data: {
    feeId: string;
    razorpayOrderId?: string;
    razorpayPaymentId?: string;
    errorMessage?: string;
    amount?: number;
  }) {
    const fee = await prisma.fee.findUnique({
      where: { id: data.feeId },
      include: { student: true }
    });

    if (!fee) return null;

    const ref = data.razorpayPaymentId || `FAIL-${Date.now()}`;
    const payment = await prisma.payment.create({
      data: {
        feeId: fee.id,
        studentId: fee.studentId,
        hostelId: fee.hostelId,
        amountPaid: Number(data.amount) || (fee.amount - fee.amountPaid),
        paymentMethod: PaymentMethodEnum.UPI,
        transactionReference: ref,
        razorpayOrderId: data.razorpayOrderId || null,
        razorpayPaymentId: data.razorpayPaymentId || null,
        paymentDate: new Date(),
        status: PaymentStatusEnum.FAILED,
        remarks: data.errorMessage || 'Payment cancelled by resident'
      },
      include: { student: true, fee: true }
    });

    return this.mapPayment(payment, fee.student.fullName);
  }

  /**
   * Get complete transaction & payment history with RBAC enforcement.
   */
  async getTransactionHistory(user: {
    userId: string;
    role: string;
    profileId?: string;
    hostelId?: string;
  }) {
    if (user.role === UserRole.STUDENT) {
      const student = await prisma.student.findFirst({
        where: {
          OR: [
            { userId: user.userId },
            ...(user.profileId ? [{ id: user.profileId }] : [])
          ]
        }
      });

      if (!student) return [];

      const payments = await prisma.payment.findMany({
        where: { studentId: student.id },
        include: { fee: true, student: true },
        orderBy: { paymentDate: 'desc' }
      });

      return payments.map(p => this.mapPayment(p, student.fullName));
    } else if (user.role === UserRole.HOST) {
      let targetHostelId = user.hostelId;
      if (!targetHostelId && (user.profileId || user.userId)) {
        const host = await prisma.host.findFirst({
          where: {
            OR: [
              ...(user.profileId ? [{ id: user.profileId }] : []),
              ...(user.userId ? [{ userId: user.userId }, { id: user.userId }] : [])
            ]
          },
          include: { hostels: { select: { id: true } } }
        });
        targetHostelId = host?.hostels[0]?.id;
      }

      const payments = await prisma.payment.findMany({
        where: targetHostelId ? { hostelId: targetHostelId } : {},
        include: { fee: true, student: true },
        orderBy: { paymentDate: 'desc' }
      });

      return payments.map(p => this.mapPayment(p, p.student?.fullName));
    } else {
      // Admin / Association Head sees all campus transactions
      const payments = await prisma.payment.findMany({
        include: { fee: true, student: true },
        orderBy: { paymentDate: 'desc' }
      });

      return payments.map(p => this.mapPayment(p, p.student?.fullName));
    }
  }

  async getPaymentsForStudent(studentIdOrUserId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [
          { id: studentIdOrUserId },
          { userId: studentIdOrUserId },
          { rollNumber: studentIdOrUserId }
        ]
      }
    });

    if (!student) {
      return [];
    }

    const payments = await prisma.payment.findMany({
      where: { studentId: student.id },
      include: { fee: true },
      orderBy: { paymentDate: 'desc' }
    });

    return payments.map(p => this.mapPayment(p, student.fullName));
  }

  async getPaymentsForHostel(hostelId: string) {
    let targetHostelId = hostelId;
    if (!targetHostelId || targetHostelId.trim() === '') {
      const firstHostel = await prisma.hostel.findFirst();
      targetHostelId = firstHostel?.id || 'hostel_001';
    }

    const payments = await prisma.payment.findMany({
      where: { hostelId: targetHostelId },
      include: { fee: true, student: true },
      orderBy: { paymentDate: 'desc' }
    });

    return payments.map(p => this.mapPayment(p, p.student?.fullName));
  }

  async getPaymentById(id: string) {
    const payment = await prisma.payment.findUnique({
      where: { id },
      include: {
        fee: true,
        student: true,
        hostel: true
      }
    });

    if (!payment) {
      throw { status: 404, message: `Payment receipt not found for ID: ${id}` };
    }

    return this.mapPayment(payment, payment.student?.fullName);
  }

  async recordPayment(data: {
    feeId: string;
    studentId?: string;
    hostelId?: string;
    amountPaid: number;
    paymentMethod?: PaymentMethodEnum;
    transactionReference?: string;
    receiptUrl?: string;
    status?: PaymentStatusEnum;
    verifiedByHostId?: string;
    remarks?: string;
  }) {
    return prisma.$transaction(async (tx) => {
      const fee = await tx.fee.findUnique({
        where: { id: data.feeId },
        include: { student: true }
      });

      if (!fee) {
        throw { status: 404, message: `Fee not found for ID: ${data.feeId}` };
      }

      const studentId = fee.studentId;
      const hostelId = data.hostelId || fee.hostelId;
      const txnRef = data.transactionReference || `TXN-${Date.now()}-${Math.floor(Math.random() * 1000)}`;

      const payment = await tx.payment.create({
        data: {
          feeId: fee.id,
          studentId,
          hostelId,
          amountPaid: Number(data.amountPaid) || fee.amount,
          paymentMethod: data.paymentMethod || PaymentMethodEnum.UPI,
          transactionReference: txnRef,
          paymentDate: new Date(),
          receiptUrl: data.receiptUrl || `https://receipts.campus.edu/${txnRef}.pdf`,
          status: data.status || PaymentStatusEnum.SUCCESS,
          verifiedByHostId: data.verifiedByHostId,
          remarks: data.remarks || 'Recorded payment'
        },
        include: { student: true, fee: true }
      });

      if ((data.status || PaymentStatusEnum.SUCCESS) === PaymentStatusEnum.SUCCESS) {
        const newAmountPaid = fee.amountPaid + payment.amountPaid;
        const newStatus = newAmountPaid >= fee.amount ? FeeStatusEnum.PAID : FeeStatusEnum.PARTIALLY_PAID;

        await tx.fee.update({
          where: { id: fee.id },
          data: {
            amountPaid: newAmountPaid,
            status: newStatus
          }
        });

        await tx.notification.create({
          data: {
            recipientUserId: fee.student.userId,
            title: '💳 Payment Received (₹)',
            body: `Your payment of ₹${payment.amountPaid} for "${fee.title}" has been successfully processed. Ref: ${txnRef}`,
            type: 'PAYMENT_CONFIRMED',
            relatedEntityId: payment.id
          }
        });

        fcmService.sendToUser(fee.student.userId, {
          title: '💳 Payment Verified (₹)',
          body: `Payment of ₹${payment.amountPaid} for "${fee.title}" processed successfully. Ref: ${txnRef}`,
          type: 'PAYMENT_CONFIRMED',
          relatedEntityId: payment.id
        }).catch(err => console.error('[FCM] Payment notification error:', err));
      }

      return this.mapPayment(payment, fee.student.fullName);
    });
  }

  /**
   * Automatically resolve the authenticated student's allocated hostel owner QR payment configuration.
   * Strict security: Student cannot supply arbitrary hostel ID.
   */
  async getStudentHostelPaymentConfig(userId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [
          { userId },
          { id: userId }
        ]
      },
      include: {
        hostel: {
          include: {
            host: true
          }
        },
        room: true,
        fees: {
          where: {
            status: {
              not: FeeStatusEnum.PAID
            }
          }
        }
      }
    });

    if (!student) {
      throw { status: 404, message: 'Student profile not found for authenticated user.' };
    }

    if (!student.hostel) {
      throw { status: 404, message: 'Student is not currently allocated to any hostel.' };
    }

    const hostel = student.hostel;
    const totalPendingDues = student.fees.reduce((sum, f) => sum + Math.max(0, f.amount - f.amountPaid), 0);

    return {
      hostelId: hostel.id,
      hostelName: hostel.name,
      hostId: hostel.hostId,
      hostName: hostel.host?.fullName || 'Hostel Owner',
      hostContactPhone: hostel.contactPhone || hostel.host?.contactPhone || '',
      hostContactEmail: hostel.contactEmail || hostel.host?.contactEmail || '',
      paymentAccountId: hostel.paymentAccountId || null,
      paymentAccountStatus: hostel.paymentAccountStatus || 'ACTIVE',
      paymentQrUrl: hostel.paymentQrUrl || null,
      qrPaymentEnabled: hostel.qrPaymentEnabled !== false,
      upiId: hostel.upiId || null,
      merchantName: hostel.merchantName || hostel.name,
      studentId: student.id,
      studentName: student.fullName,
      studentRollNumber: student.rollNumber,
      roomNumber: student.room?.roomNumber || student.roomNumber || 'A-204',
      totalPendingDues
    };
  }

  /**
   * Get hostel payment configuration for hostel owner or admin.
   */
  async getHostelPaymentConfig(hostelId: string, requesterUserId?: string, requesterRole?: string) {
    let hostel = await prisma.hostel.findUnique({
      where: { id: hostelId },
      include: {
        host: true
      }
    });

    if (!hostel) {
      if (requesterUserId) {
        hostel = await prisma.hostel.findFirst({
          where: {
            OR: [
              { hostId: requesterUserId },
              { host: { userId: requesterUserId } },
              { host: { id: requesterUserId } }
            ]
          },
          include: { host: true }
        });
      }

      if (!hostel && hostelId) {
        hostel = await prisma.hostel.findFirst({
          where: {
            OR: [
              { hostId: hostelId },
              { host: { userId: hostelId } }
            ]
          },
          include: { host: true }
        });
      }

      if (!hostel) {
        hostel = await prisma.hostel.findFirst({ include: { host: true } });
      }
    }

    if (!hostel) {
      throw { status: 404, message: `Hostel not found for ID: ${hostelId}` };
    }

    if (requesterRole === UserRole.HOST && requesterUserId) {
      const hostProfile = await prisma.host.findFirst({
        where: {
          OR: [
            { userId: requesterUserId },
            { id: requesterUserId }
          ]
        }
      });
      if (hostProfile && hostel.hostId !== hostProfile.id && hostel.hostId !== requesterUserId && hostel.host?.userId !== requesterUserId) {
        throw { status: 403, message: 'Unauthorized: You are not the owner of this hostel.' };
      }
    }

    return {
      hostelId: hostel.id,
      hostelName: hostel.name,
      hostId: hostel.hostId,
      hostName: hostel.host?.fullName || 'Hostel Owner',
      hostContactPhone: hostel.contactPhone || hostel.host?.contactPhone || '',
      hostContactEmail: hostel.contactEmail || hostel.host?.contactEmail || '',
      paymentAccountId: hostel.paymentAccountId || null,
      paymentAccountStatus: hostel.paymentAccountStatus || 'ACTIVE',
      paymentQrUrl: hostel.paymentQrUrl || null,
      qrPaymentEnabled: hostel.qrPaymentEnabled !== false,
      upiId: hostel.upiId || null,
      merchantName: hostel.merchantName || hostel.name
    };
  }

  /**
   * Update hostel owner official payment QR and UPI settings.
   */
  async updateHostelPaymentConfig(
    hostelId: string,
    data: {
      paymentAccountId?: string;
      paymentAccountStatus?: string;
      paymentQrUrl?: string;
      qrPaymentEnabled?: boolean;
      upiId?: string;
      merchantName?: string;
    },
    requesterUserId?: string,
    requesterRole?: string
  ) {
    let hostel = await prisma.hostel.findUnique({
      where: { id: hostelId },
      include: { host: true }
    });

    if (!hostel) {
      if (requesterUserId) {
        hostel = await prisma.hostel.findFirst({
          where: {
            OR: [
              { hostId: requesterUserId },
              { host: { userId: requesterUserId } },
              { host: { id: requesterUserId } }
            ]
          },
          include: { host: true }
        });
      }

      if (!hostel && hostelId) {
        hostel = await prisma.hostel.findFirst({
          where: {
            OR: [
              { hostId: hostelId },
              { host: { userId: hostelId } }
            ]
          },
          include: { host: true }
        });
      }

      if (!hostel) {
        hostel = await prisma.hostel.findFirst({ include: { host: true } });
      }
    }

    if (!hostel) {
      throw { status: 404, message: `Hostel not found for ID: ${hostelId}` };
    }

    if (requesterRole === UserRole.HOST && requesterUserId) {
      const hostProfile = await prisma.host.findFirst({
        where: {
          OR: [
            { userId: requesterUserId },
            { id: requesterUserId }
          ]
        }
      });
      if (hostProfile && hostel.hostId !== hostProfile.id && hostel.hostId !== requesterUserId && hostel.host?.userId !== requesterUserId) {
        throw { status: 403, message: 'Unauthorized: You can only update payment settings for your own hostel.' };
      }
    }

    // Format & validate UPI ID if provided
    let sanitizedUpiId = hostel.upiId;
    if (data.upiId !== undefined) {
      if (data.upiId === null || data.upiId.trim() === '') {
        sanitizedUpiId = null;
      } else {
        const cleanUpi = data.upiId.trim();
        const upiRegex = /^[a-zA-Z0-9.\-_]{2,256}@[a-zA-Z]{2,64}$/;
        if (!upiRegex.test(cleanUpi)) {
          throw {
            status: 400,
            message: 'Invalid UPI ID format. Expected format: username@bank or mobile@upi (e.g. hostel.owner@hdfcbank, 9876543210@upi)'
          };
        }
        sanitizedUpiId = cleanUpi;
      }
    }

    const updated = await prisma.hostel.update({
      where: { id: hostel.id },
      data: {
        paymentAccountId: data.paymentAccountId !== undefined ? (data.paymentAccountId?.trim() || null) : hostel.paymentAccountId,
        paymentAccountStatus: data.paymentAccountStatus !== undefined ? data.paymentAccountStatus : hostel.paymentAccountStatus,
        paymentQrUrl: data.paymentQrUrl !== undefined ? (data.paymentQrUrl?.trim() || null) : hostel.paymentQrUrl,
        qrPaymentEnabled: data.qrPaymentEnabled !== undefined ? data.qrPaymentEnabled : hostel.qrPaymentEnabled,
        upiId: sanitizedUpiId,
        merchantName: data.merchantName !== undefined ? (data.merchantName?.trim() || hostel.name) : hostel.merchantName
      },
      include: { host: true }
    });

    return {
      hostelId: updated.id,
      hostelName: updated.name,
      hostId: updated.hostId,
      hostName: updated.host?.fullName || 'Hostel Owner',
      paymentAccountId: updated.paymentAccountId,
      paymentAccountStatus: updated.paymentAccountStatus,
      paymentQrUrl: updated.paymentQrUrl,
      qrPaymentEnabled: updated.qrPaymentEnabled,
      upiId: updated.upiId,
      merchantName: updated.merchantName
    };
  }

  /**
   * Submit a manual static QR payment (status: PENDING_VERIFICATION).
   * Does NOT mark the fee as PAID automatically. Queued for host review.
   */
  async submitManualQrPayment(data: {
    feeId: string;
    amountPaid: number;
    transactionReference: string;
    remarks?: string;
    receiptUrl?: string;
    userId?: string;
  }) {
    const { feeId, amountPaid, transactionReference, remarks, receiptUrl, userId } = data;

    if (!feeId || !transactionReference || !amountPaid || amountPaid <= 0) {
      throw { status: 400, message: 'feeId, amountPaid, and transactionReference (UTR/Ref ID) are required.' };
    }

    // 1. Check for duplicate transaction reference
    const existingPayment = await prisma.payment.findUnique({
      where: { transactionReference }
    });

    if (existingPayment) {
      throw { status: 400, message: 'A payment with this transaction reference has already been submitted.' };
    }

    // 2. Find Fee & Student
    const fee = await prisma.fee.findUnique({
      where: { id: feeId },
      include: {
        student: { include: { user: true } },
        hostel: { include: { host: { include: { user: true } } } }
      }
    });

    if (!fee) {
      throw { status: 404, message: `Fee invoice not found for ID: ${feeId}` };
    }

    // Verify student ownership if userId provided
    if (userId && fee.student.userId !== userId && fee.studentId !== userId) {
      throw { status: 403, message: 'You cannot submit payment for another student fee invoice.' };
    }

    // 3. Create Payment record in PENDING_VERIFICATION status
    const payment = await prisma.payment.create({
      data: {
        feeId: fee.id,
        studentId: fee.studentId,
        hostelId: fee.hostelId,
        amountPaid: Number(amountPaid),
        paymentMethod: PaymentMethodEnum.UPI,
        paymentGateway: 'MANUAL_QR',
        transactionReference,
        paymentDate: new Date(),
        receiptUrl: receiptUrl || null,
        status: PaymentStatusEnum.PENDING_VERIFICATION,
        remarks: remarks || `Manual static QR payment submitted (Ref: ${transactionReference}). Awaiting host verification.`
      },
      include: { student: true, fee: true }
    });

    // 4. Notify Hostel Owner (Host) about the pending submission
    const hostUserId = fee.hostel.host?.userId;
    if (hostUserId) {
      await prisma.notification.create({
        data: {
          recipientUserId: hostUserId,
          title: '🔔 New UPI QR Payment Submitted (₹)',
          body: `${fee.student.fullName} submitted ₹${amountPaid} for "${fee.title}" via QR. Ref: ${transactionReference}. Please verify.`,
          type: 'PAYMENT_DUE',
          relatedEntityId: payment.id
        }
      });

      fcmService.sendToUser(hostUserId, {
        title: '🔔 New UPI Payment for Review',
        body: `${fee.student.fullName} submitted ₹${amountPaid} (Ref: ${transactionReference}). Verify in Payment Settings.`,
        type: 'PAYMENT_DUE',
        relatedEntityId: payment.id
      }).catch(err => console.error('[FCM] Error notifying host:', err));
    }

    // 5. In-app confirmation for student
    await prisma.notification.create({
      data: {
        recipientUserId: fee.student.userId,
        title: '⏳ Payment Submitted for Verification',
        body: `Your payment of ₹${amountPaid} for "${fee.title}" (Ref: ${transactionReference}) is pending verification by your hostel owner.`,
        type: 'PAYMENT_DUE',
        relatedEntityId: payment.id
      }
    });

    return this.mapPayment(payment, fee.student.fullName);
  }

  /**
   * Verify/Approve or Reject a student's manual QR payment reference.
   */
  async verifyManualPayment(data: {
    paymentId: string;
    approved: boolean;
    remarks?: string;
    hostUserId: string;
  }) {
    const { paymentId, approved, remarks, hostUserId } = data;

    return prisma.$transaction(async (tx) => {
      const payment = await tx.payment.findUnique({
        where: { id: paymentId },
        include: {
          fee: true,
          student: { include: { user: true } },
          hostel: { include: { host: true } }
        }
      });

      if (!payment) {
        throw { status: 404, message: `Payment record not found for ID: ${paymentId}` };
      }

      if (payment.status !== PaymentStatusEnum.PENDING_VERIFICATION && payment.status !== PaymentStatusEnum.PENDING) {
        throw { status: 400, message: `This payment is already in '${payment.status}' status and cannot be modified.` };
      }

      if (approved) {
        // 1. Mark Payment SUCCESS
        const updatedPayment = await tx.payment.update({
          where: { id: payment.id },
          data: {
            status: PaymentStatusEnum.SUCCESS,
            verifiedByHostId: hostUserId,
            remarks: remarks || `Verified and approved by hostel owner (${new Date().toLocaleDateString()})`
          },
          include: { student: true, fee: true }
        });

        // 2. Update Fee balance and status
        const fee = payment.fee;
        const newAmountPaid = fee.amountPaid + payment.amountPaid;
        const newStatus = newAmountPaid >= fee.amount ? FeeStatusEnum.PAID : FeeStatusEnum.PARTIALLY_PAID;

        await tx.fee.update({
          where: { id: fee.id },
          data: {
            amountPaid: newAmountPaid,
            status: newStatus
          }
        });

        // 3. Notify Student
        if (payment.student?.userId) {
          try {
            await tx.notification.create({
              data: {
                recipientUserId: payment.student.userId,
                title: '💳 Payment Verified & Approved (₹)',
                body: `Your payment of ₹${payment.amountPaid} for "${fee.title}" has been verified and confirmed by your hostel owner. Ref: ${payment.transactionReference}`,
                type: 'PAYMENT_CONFIRMED',
                relatedEntityId: payment.id
              }
            });

            fcmService.sendToUser(payment.student.userId, {
              title: '💳 Payment Approved (₹)',
              body: `Payment of ₹${payment.amountPaid} for "${fee.title}" confirmed. Status: PAID`,
              type: 'PAYMENT_CONFIRMED',
              relatedEntityId: payment.id
            }).catch(err => console.error('[FCM] Error notifying student:', err));
          } catch (e) {
            console.warn('[NOTIF] Could not send student notification on verify:', e);
          }
        }

        return this.mapPayment(updatedPayment, payment.student?.fullName);
      } else {
        // Rejected
        const updatedPayment = await tx.payment.update({
          where: { id: payment.id },
          data: {
            status: PaymentStatusEnum.FAILED,
            verifiedByHostId: hostUserId,
            remarks: remarks || `Payment reference rejected by hostel owner: Invalid reference or amount not received`
          },
          include: { student: true, fee: true }
        });

        // Notify Student
        if (payment.student?.userId) {
          try {
            await tx.notification.create({
              data: {
                recipientUserId: payment.student.userId,
                title: '❌ Payment Verification Failed',
                body: `Your manual payment submission for "${payment.fee.title}" was not verified. Reason: ${remarks || 'Invalid transaction reference'}. Please contact hostel management.`,
                type: 'PAYMENT_DUE',
                relatedEntityId: payment.id
              }
            });
          } catch (e) {
            console.warn('[NOTIF] Could not send student notification on reject:', e);
          }
        }

        return this.mapPayment(updatedPayment, payment.student.fullName);
      }
    });
  }

  /**
   * Get Admin Overview of all hostels' payment configurations and collections.
   * Sensitive credentials are completely redacted.
   */
  async getAdminPaymentOverview() {
    const hostels = await prisma.hostel.findMany({
      include: {
        host: true,
        payments: {
          select: {
            id: true,
            amountPaid: true,
            status: true,
            paymentGateway: true
          }
        }
      },
      orderBy: { name: 'asc' }
    });

    return hostels.map(h => {
      const successfulPayments = h.payments.filter(p => p.status === PaymentStatusEnum.SUCCESS);
      const pendingVerificationPayments = h.payments.filter(p => p.status === PaymentStatusEnum.PENDING_VERIFICATION);
      const totalCollections = successfulPayments.reduce((sum, p) => sum + p.amountPaid, 0);

      return {
        hostelId: h.id,
        hostelName: h.name,
        city: h.city,
        ownerId: h.hostId,
        ownerName: h.host?.fullName || 'Hostel Owner',
        ownerContact: h.contactPhone || h.host?.contactPhone || '',
        ownerEmail: h.contactEmail || h.host?.contactEmail || '',
        paymentAccountId: h.paymentAccountId || null,
        paymentAccountStatus: h.paymentAccountStatus || 'ACTIVE',
        qrConfigured: Boolean(h.paymentQrUrl || h.upiId),
        qrPaymentEnabled: h.qrPaymentEnabled !== false,
        paymentQrUrl: h.paymentQrUrl || null,
        upiId: h.upiId || null,
        merchantName: h.merchantName || h.name,
        totalCollections,
        successfulPaymentCount: successfulPayments.length,
        pendingVerificationCount: pendingVerificationPayments.length
      };
    });
  }

  private mapPayment(p: any, studentName?: string) {
    return {
      paymentId: p.id,
      feeId: p.feeId,
      feeTitle: p.fee?.title || 'Hostel Fee',
      studentId: p.studentId,
      studentName: studentName || p.student?.fullName || 'Resident Student',
      hostelId: p.hostelId,
      amountPaid: p.amountPaid,
      paymentMethod: p.paymentMethod,
      paymentGateway: p.paymentGateway || 'RAZORPAY',
      orderId: p.orderId || p.razorpayOrderId || null,
      transactionId: p.transactionId || p.razorpayPaymentId || null,
      transactionReference: p.transactionReference,
      razorpayOrderId: p.razorpayOrderId || null,
      razorpayPaymentId: p.razorpayPaymentId || null,
      paymentDate: p.paymentDate.getTime ? p.paymentDate.getTime() : new Date(p.paymentDate).getTime(),
      receiptUrl: p.receiptUrl,
      status: p.status,
      verifiedByHostId: p.verifiedByHostId,
      remarks: p.remarks,
      createdAt: p.createdAt.getTime ? p.createdAt.getTime() : new Date(p.createdAt).getTime()
    };
  }
}
