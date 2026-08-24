import { prisma } from '../../config/prisma';
import { ComplaintCategoryEnum, ComplaintUrgencyEnum, ComplaintStatusEnum } from '../../types/enums';
import { fcmService } from '../../services/fcm.service';

export class ComplaintsService {
  async getComplaintsForStudent(studentIdOrUserId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: studentIdOrUserId }, { userId: studentIdOrUserId }]
      }
    });

    if (!student) {
      return [];
    }

    const complaints = await prisma.complaint.findMany({
      where: { studentId: student.id },
      orderBy: { createdAt: 'desc' }
    });

    return complaints.map(c => this.mapComplaint(c));
  }

  async getComplaintsForHostel(hostelId: string) {
    const complaints = await prisma.complaint.findMany({
      where: { hostelId },
      orderBy: { createdAt: 'desc' }
    });

    return complaints.map(c => this.mapComplaint(c));
  }

  async getAllComplaints() {
    const complaints = await prisma.complaint.findMany({
      orderBy: { createdAt: 'desc' }
    });

    return complaints.map(c => this.mapComplaint(c));
  }

  async getComplaintById(id: string) {
    const complaint = await prisma.complaint.findUnique({
      where: { id }
    });

    if (!complaint) {
      throw { status: 404, message: `Complaint not found for ID: ${id}` };
    }

    return this.mapComplaint(complaint);
  }

  async submitComplaint(data: {
    hostelId?: string;
    studentId: string;
    studentName?: string;
    roomNumber?: string;
    category?: ComplaintCategoryEnum;
    title: string;
    description: string;
    attachments?: string[];
    urgency?: ComplaintUrgencyEnum;
  }) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: data.studentId }, { userId: data.studentId }]
      }
    });

    if (!student) {
      throw { status: 404, message: `Student not found for ID: ${data.studentId}` };
    }

    const hostelId = data.hostelId || student.hostelId || 'hostel_001';
    const studentName = data.studentName || student.fullName;
    const roomNumber = data.roomNumber || student.roomNumber || 'A-204';

    const created = await prisma.complaint.create({
      data: {
        hostelId,
        studentId: student.id,
        studentName,
        roomNumber,
        category: data.category || ComplaintCategoryEnum.OTHER,
        title: data.title,
        description: data.description,
        attachments: Array.isArray(data.attachments) ? JSON.stringify(data.attachments) : (data.attachments || '[]'),
        urgency: data.urgency || ComplaintUrgencyEnum.MEDIUM,
        status: ComplaintStatusEnum.OPEN
      }
    });

    // Notify hostel owners/wardens
    const hostel = await prisma.hostel.findUnique({
      where: { id: hostelId },
      include: { host: true }
    });

    if (hostel?.host?.userId) {
      fcmService.sendToUser(hostel.host.userId, {
        title: '⚠️ New Complaint Submitted',
        body: `Student ${studentName} (Room ${roomNumber}) logged: "${data.title}"`,
        type: 'COMPLAINT_UPDATE',
        relatedEntityId: created.id
      }).catch(err => console.error('[FCM] Complaint submit push error:', err));
    }

    return this.mapComplaint(created);
  }

  async updateComplaintStatus(id: string, data: {
    status: ComplaintStatusEnum;
    notes?: string;
    assignedStaffName?: string;
    resolutionSummary?: string;
  }) {
    const existing = await prisma.complaint.findUnique({
      where: { id },
      include: { student: true }
    });

    if (!existing) {
      throw { status: 404, message: 'Complaint not found' };
    }

    const isResolved = data.status === ComplaintStatusEnum.RESOLVED || data.status === ComplaintStatusEnum.REJECTED;

    const updated = await prisma.complaint.update({
      where: { id },
      data: {
        status: data.status,
        hostNotes: data.notes !== undefined ? data.notes : existing.hostNotes,
        assignedStaffName: data.assignedStaffName !== undefined ? data.assignedStaffName : existing.assignedStaffName,
        resolutionSummary: data.resolutionSummary !== undefined ? data.resolutionSummary : existing.resolutionSummary,
        resolvedAt: isResolved ? new Date() : null
      }
    });

    // Notify student of update
    fcmService.sendToUser(existing.student.userId, {
      title: `🔧 Complaint ${data.status}`,
      body: `Your complaint "${existing.title}" is now marked as ${data.status}.${data.resolutionSummary ? ` Summary: ${data.resolutionSummary}` : ''}`,
      type: 'COMPLAINT_UPDATE',
      relatedEntityId: updated.id
    }).catch(err => console.error('[FCM] Complaint update push error:', err));

    return this.mapComplaint(updated);
  }

  async deleteComplaint(id: string) {
    await prisma.complaint.delete({
      where: { id }
    });

    return { success: true };
  }

  private mapComplaint(c: any) {
    let parsedAttachments: string[] = [];
    try {
      parsedAttachments = typeof c.attachments === 'string' ? JSON.parse(c.attachments) : (c.attachments || []);
    } catch {
      parsedAttachments = [];
    }
    return {
      complaintId: c.id,
      hostelId: c.hostelId,
      studentId: c.studentId,
      studentName: c.studentName,
      roomNumber: c.roomNumber,
      category: c.category,
      title: c.title,
      description: c.description,
      attachments: parsedAttachments,
      urgency: c.urgency,
      status: c.status,
      assignedStaffName: c.assignedStaffName,
      hostNotes: c.hostNotes,
      resolutionSummary: c.resolutionSummary,
      createdAt: c.createdAt.getTime(),
      resolvedAt: c.resolvedAt ? c.resolvedAt.getTime() : null
    };
  }
}
