import { prisma } from '../../config/prisma';
import { FeeTypeEnum, FeeStatusEnum } from '../../types/enums';

export class FeesService {
  async getFeesForStudent(studentIdOrUserId: string) {
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

    const fees = await prisma.fee.findMany({
      where: { studentId: student.id },
      orderBy: { dueDate: 'desc' }
    });

    return fees.map(f => this.mapFee(f, student.fullName));
  }

  async getFeesForHostel(hostelId: string) {
    let targetHostelId = hostelId;
    if (!targetHostelId || targetHostelId.trim() === '') {
      const firstHostel = await prisma.hostel.findFirst();
      targetHostelId = firstHostel?.id || 'hostel_001';
    }

    const fees = await prisma.fee.findMany({
      where: { hostelId: targetHostelId },
      include: { student: true },
      orderBy: { createdAt: 'desc' }
    });

    return fees.map(f => this.mapFee(f, f.student?.fullName));
  }

  async getAllFees() {
    const fees = await prisma.fee.findMany({
      include: { student: true },
      orderBy: { createdAt: 'desc' }
    });

    return fees.map(f => this.mapFee(f, f.student?.fullName));
  }

  async createFee(data: {
    hostelId?: string;
    studentId: string;
    studentName?: string;
    roomId?: string;
    title: string;
    feeType?: FeeTypeEnum;
    amount: number;
    dueDate?: number | Date | string;
    billingMonth?: number;
    billingYear?: number;
  }) {
    let student = await prisma.student.findFirst({
      where: {
        OR: [
          { id: data.studentId },
          { userId: data.studentId },
          { rollNumber: data.studentId }
        ]
      }
    });

    if (!student) {
      // Find any student or create one if needed
      student = await prisma.student.findFirst({
        where: { fullName: data.studentName || data.studentId }
      });
    }

    if (!student) {
      throw { status: 404, message: `Student not found for ID/Roll: ${data.studentId}` };
    }

    const now = new Date();
    const billingMonth = data.billingMonth || (now.getMonth() + 1);
    const billingYear = data.billingYear || now.getFullYear();
    const dueDate = data.dueDate ? new Date(data.dueDate) : new Date(Date.now() + 15 * 24 * 60 * 60 * 1000);
    const hostelId = data.hostelId || student.hostelId || 'hostel_001';

    const created = await prisma.fee.create({
      data: {
        hostelId,
        studentId: student.id,
        roomId: data.roomId || student.roomId,
        title: data.title || 'Monthly Hostel & Mess Fee',
        feeType: data.feeType || FeeTypeEnum.RENT,
        amount: Number(data.amount) || 5000,
        amountPaid: 0.0,
        dueDate,
        billingMonth,
        billingYear,
        status: FeeStatusEnum.PENDING
      }
    });

    // Notify student
    await prisma.notification.create({
      data: {
        recipientUserId: student.userId,
        title: 'New Fee Invoice Issued',
        body: `A new fee invoice of ₹${created.amount} for "${created.title}" has been issued by hostel warden.`,
        type: 'PAYMENT_DUE',
        relatedEntityId: created.id
      }
    });

    return this.mapFee(created, student.fullName);
  }

  async updateFee(id: string, data: Partial<any>) {
    const updated = await prisma.fee.update({
      where: { id },
      include: { student: true },
      data
    });

    return this.mapFee(updated, updated.student?.fullName);
  }

  private mapFee(f: any, studentName?: string) {
    return {
      feeId: f.id,
      hostelId: f.hostelId,
      studentId: f.studentId,
      studentName: studentName || f.student?.fullName || 'Resident Student',
      roomId: f.roomId || '',
      title: f.title,
      feeType: f.feeType,
      amount: f.amount,
      amountPaid: f.amountPaid,
      dueDate: f.dueDate.getTime(),
      billingMonth: f.billingMonth,
      billingYear: f.billingYear,
      status: f.status,
      createdAt: f.createdAt.getTime()
    };
  }
}
