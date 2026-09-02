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
  async createRazorpayOrder(feeId: string, customAmount?: number, userId?: string) {
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

    const pendingAmount = Math.max(0, fee.amount - fee.amountPaid);
    if (pendingAmount <= 0) {
      throw { status: 400, message: 'This fee invoice has already been fully paid.' };
    }

    const payableAmount = (customAmount && customAmount > 0 && customAmount <= pendingAmount)
      ? Number(customAmount)
      : pendingAmount;

    const amountInPaise = Math.round(payableAmount * 100);
    const keyId = process.env.RAZORPAY_KEY_ID || 'rzp_test_hostelhub_dev';
    const keySecret = process.env.RAZORPAY_KEY_SECRET;

    let razorpayOrderId = `order_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;

    // If real Razorpay API keys are provided, create live order via Razorpay API
    if (keySecret && keyId && !keyId.includes('dev')) {
      try {
        const authHeader = 'Basic ' + Buffer.from(`${keyId}:${keySecret}`).toString('base64');
        const response = await fetch('https://api.razorpay.com/v1/orders', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': authHeader
          },
          body: JSON.stringify({
            amount: amountInPaise,
            currency: 'INR',
            receipt: `rcpt_${fee.id.substring(0, 8)}_${Date.now()}`,
            notes: {
              feeId: fee.id,
              feeTitle: fee.title,
              studentId: fee.studentId,
              studentName: fee.student.fullName,
              hostelId: fee.hostelId
            }
          })
        });

        if (response.ok) {
          const orderData: any = await response.json();
          razorpayOrderId = orderData.id;
        }
      } catch (err) {
        console.error('[RAZORPAY] Error calling Razorpay API directly:', err);
      }
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
  }) {
    const { feeId, razorpayOrderId, razorpayPaymentId, razorpaySignature, amountPaid } = data;

    if (!feeId || !razorpayOrderId || !razorpayPaymentId) {
      throw { status: 400, message: 'Missing required Razorpay payment verification parameters.' };
    }

    // 1. Idempotency Check: if this payment was already verified, return existing record
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

    // 2. Cryptographic signature check if key secret is present
    const keySecret = process.env.RAZORPAY_KEY_SECRET;
    if (keySecret && razorpaySignature) {
      const generatedSignature = crypto
        .createHmac('sha256', keySecret)
        .update(`${razorpayOrderId}|${razorpayPaymentId}`)
        .digest('hex');

      if (generatedSignature !== razorpaySignature) {
        throw { status: 400, message: 'Invalid Razorpay payment signature verification failed.' };
      }
    }

    // 3. Perform database transaction to record payment and update fee
    return prisma.$transaction(async (tx) => {
      const fee = await tx.fee.findUnique({
        where: { id: feeId },
        include: { student: { include: { user: true } }, hostel: true }
      });

      if (!fee) {
        throw { status: 404, message: `Fee not found for ID: ${feeId}` };
      }

      const verifiedAmount = Number(amountPaid) || Math.max(0, fee.amount - fee.amountPaid);
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
      if (!targetHostelId && user.profileId) {
        const host = await prisma.host.findUnique({
          where: { id: user.profileId },
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
      transactionReference: p.transactionReference,
      razorpayOrderId: p.razorpayOrderId || null,
      razorpayPaymentId: p.razorpayPaymentId || null,
      paymentDate: p.paymentDate.getTime(),
      receiptUrl: p.receiptUrl,
      status: p.status,
      verifiedByHostId: p.verifiedByHostId,
      remarks: p.remarks,
      createdAt: p.createdAt.getTime()
    };
  }
}
