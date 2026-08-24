import { prisma } from '../../config/prisma';
import { PaymentMethodEnum, PaymentStatusEnum, FeeStatusEnum } from '../../types/enums';
import { fcmService } from '../../services/fcm.service';
import { emailService } from '../../services/email.service';

export class PaymentsService {
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
      // 1. Verify Fee
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

      // 2. Create Payment Record
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

      // 3. Update Fee status & amount paid if payment is SUCCESS
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

        // 4. Notify Student via In-App & Cloud FCM
        await tx.notification.create({
          data: {
            recipientUserId: fee.student.userId,
            title: '💳 Payment Received (₹)',
            body: `Your payment of ₹${payment.amountPaid} for "${fee.title}" has been successfully processed. Ref: ${txnRef}`,
            type: 'PAYMENT_CONFIRMED',
            relatedEntityId: payment.id
          }
        });

        // 5. Cloud Push Notification & Email Alert
        fcmService.sendToUser(fee.student.userId, {
          title: '💳 Payment Verified (₹)',
          body: `Payment of ₹${payment.amountPaid} for "${fee.title}" processed successfully. Ref: ${txnRef}`,
          type: 'PAYMENT_CONFIRMED',
          relatedEntityId: payment.id
        }).catch(err => console.error('[FCM] Payment notification error:', err));

        // 6. Audit Log
        await tx.auditLog.create({
          data: {
            userId: fee.student.userId,
            action: 'RECORD_PAYMENT',
            entityType: 'PAYMENT',
            entityId: payment.id,
            details: `Paid ₹${payment.amountPaid} for fee ${fee.id} (${txnRef})`
          }
        });
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
      paymentDate: p.paymentDate.getTime(),
      receiptUrl: p.receiptUrl,
      status: p.status,
      verifiedByHostId: p.verifiedByHostId,
      remarks: p.remarks,
      createdAt: p.createdAt.getTime()
    };
  }
}
