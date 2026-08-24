import { prisma } from '../../config/prisma';
import { VisitorStatusEnum } from '../../types/enums';

export class VisitorsService {
  async getVisitorsForHostel(hostelId: string) {
    const visitors = await prisma.visitor.findMany({
      where: { hostelId },
      include: { student: { select: { fullName: true, roomNumber: true } } },
      orderBy: { checkInTime: 'desc' }
    });

    return visitors.map(v => this.mapVisitor(v));
  }

  async getVisitorsForStudent(studentIdOrUserId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: studentIdOrUserId }, { userId: studentIdOrUserId }]
      }
    });

    if (!student) return [];

    const visitors = await prisma.visitor.findMany({
      where: { studentId: student.id },
      orderBy: { checkInTime: 'desc' }
    });

    return visitors.map(v => this.mapVisitor(v));
  }

  async registerVisitor(data: {
    hostelId: string;
    studentId: string;
    visitorName: string;
    relationship: string;
    phone: string;
    idProofType?: string;
    idProofNumber?: string;
    purpose: string;
    approvedBy?: string;
  }) {
    const created = await prisma.visitor.create({
      data: {
        hostelId: data.hostelId,
        studentId: data.studentId,
        visitorName: data.visitorName,
        relationship: data.relationship,
        phone: data.phone,
        idProofType: data.idProofType,
        idProofNumber: data.idProofNumber,
        purpose: data.purpose,
        checkInTime: new Date(),
        approvedBy: data.approvedBy,
        status: VisitorStatusEnum.INSIDE
      }
    });

    return this.mapVisitor(created);
  }

  async checkoutVisitor(id: string) {
    const updated = await prisma.visitor.update({
      where: { id },
      data: {
        status: VisitorStatusEnum.CHECKED_OUT,
        checkOutTime: new Date()
      }
    });

    return this.mapVisitor(updated);
  }

  private mapVisitor(v: any) {
    return {
      visitorId: v.id,
      hostelId: v.hostelId,
      studentId: v.studentId,
      studentName: v.student?.fullName,
      roomNumber: v.student?.roomNumber,
      visitorName: v.visitorName,
      relationship: v.relationship,
      phone: v.phone,
      idProofType: v.idProofType,
      idProofNumber: v.idProofNumber,
      purpose: v.purpose,
      checkInTime: v.checkInTime.getTime(),
      checkOutTime: v.checkOutTime ? v.checkOutTime.getTime() : null,
      status: v.status,
      remarks: v.remarks
    };
  }
}
