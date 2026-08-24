import { prisma } from '../../config/prisma';
import { LeaveStatusEnum } from '../../types/enums';
import { fcmService } from '../../services/fcm.service';

export class LeaveRequestsService {
  async getLeaveRequestsForStudent(studentIdOrUserId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: studentIdOrUserId }, { userId: studentIdOrUserId }]
      }
    });

    if (!student) return [];

    const leaves = await prisma.leaveRequest.findMany({
      where: { studentId: student.id },
      orderBy: { createdAt: 'desc' }
    });

    return leaves.map(l => this.mapLeave(l));
  }

  async getLeaveRequestsForHostel(hostelId: string) {
    const leaves = await prisma.leaveRequest.findMany({
      where: { hostelId },
      include: { student: true },
      orderBy: { createdAt: 'desc' }
    });

    return leaves.map(l => this.mapLeave(l));
  }

  async createLeaveRequest(data: {
    studentId: string;
    hostelId?: string;
    startDate: string;
    endDate: string;
    reason: string;
    emergencyContactPhone?: string;
  }) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: data.studentId }, { userId: data.studentId }]
      }
    });

    if (!student) {
      throw { status: 404, message: 'Student not found' };
    }

    const hostelId = data.hostelId || student.hostelId || 'hostel_001';

    const created = await prisma.leaveRequest.create({
      data: {
        studentId: student.id,
        hostelId,
        startDate: data.startDate,
        endDate: data.endDate,
        reason: data.reason,
        emergencyContactPhone: data.emergencyContactPhone || student.emergencyContactPhone,
        status: LeaveStatusEnum.PENDING
      }
    });

    return this.mapLeave(created);
  }

  async updateLeaveStatus(id: string, data: { status: LeaveStatusEnum; approverId?: string; remarks?: string; rejectionReason?: string }) {
    const updated = await prisma.leaveRequest.update({
      where: { id },
      data: {
        status: data.status,
        approvedBy: data.approverId,
        remarks: data.remarks,
        rejectionReason: data.rejectionReason
      },
      include: { student: true }
    });

    if (data.status === LeaveStatusEnum.APPROVED) {
      fcmService.sendToUser(updated.student.userId, {
        title: '✅ Leave Request Approved',
        body: `Your leave request for ${updated.startDate} to ${updated.endDate} has been approved.`,
        type: 'LEAVE_APPROVED',
        relatedEntityId: updated.id
      }).catch(err => console.error('[FCM] Leave approval push error:', err));
    } else if (data.status === LeaveStatusEnum.REJECTED) {
      fcmService.sendToUser(updated.student.userId, {
        title: '❌ Leave Request Rejected',
        body: `Your leave request for ${updated.startDate} to ${updated.endDate} was rejected.${data.rejectionReason ? ` Reason: ${data.rejectionReason}` : ''}`,
        type: 'LEAVE_REJECTED',
        relatedEntityId: updated.id
      }).catch(err => console.error('[FCM] Leave rejection push error:', err));
    }

    return this.mapLeave(updated);
  }

  private mapLeave(l: any) {
    return {
      leaveId: l.id,
      studentId: l.studentId,
      studentName: l.student?.fullName,
      hostelId: l.hostelId,
      startDate: l.startDate,
      endDate: l.endDate,
      reason: l.reason,
      emergencyContactPhone: l.emergencyContactPhone,
      status: l.status,
      approvedBy: l.approvedBy,
      rejectionReason: l.rejectionReason,
      remarks: l.remarks,
      createdAt: l.createdAt.getTime()
    };
  }
}
