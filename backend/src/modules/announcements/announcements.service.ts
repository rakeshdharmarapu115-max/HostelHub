import { prisma } from '../../config/prisma';
import { UserRole, AnnouncementPriorityEnum } from '../../types/enums';
import { fcmService } from '../../services/fcm.service';

export class AnnouncementsService {
  async getAnnouncements(hostelId?: string) {
    const where: any = {};

    if (hostelId) {
      where.OR = [
        { hostelId },
        { hostelId: 'GLOBAL_CAMPUS' },
        { senderRole: UserRole.ADMIN }
      ];
    }

    const announcements = await prisma.announcement.findMany({
      where,
      orderBy: { createdAt: 'desc' }
    });

    return announcements.map(a => this.mapAnnouncement(a));
  }

  async getAnnouncementById(id: string) {
    const announcement = await prisma.announcement.findUnique({
      where: { id }
    });

    if (!announcement) {
      throw { status: 404, message: `Announcement not found for ID: ${id}` };
    }

    return this.mapAnnouncement(announcement);
  }

  async createAnnouncement(data: {
    hostelId?: string;
    senderId: string;
    senderRole?: UserRole;
    senderName?: string;
    title: string;
    message: string;
    priority?: AnnouncementPriorityEnum;
    targetAudience?: string;
    attachmentUrls?: string[];
    expiresAt?: number | Date;
  }) {
    const sender = await prisma.user.findUnique({
      where: { id: data.senderId }
    });

    const senderRole = data.senderRole || sender?.role || UserRole.HOST;
    const senderName = data.senderName || sender?.fullName || 'Hostel Administration';
    const hostelId = data.hostelId || (senderRole === UserRole.ADMIN ? 'GLOBAL_CAMPUS' : 'hostel_001');

    const created = await prisma.announcement.create({
      data: {
        hostelId,
        senderId: data.senderId,
        senderRole,
        senderName,
        title: data.title,
        message: data.message,
        priority: data.priority || AnnouncementPriorityEnum.NORMAL,
        targetAudience: data.targetAudience || 'ALL',
        attachmentUrls: Array.isArray(data.attachmentUrls) ? JSON.stringify(data.attachmentUrls) : (data.attachmentUrls || '[]'),
        expiresAt: data.expiresAt ? new Date(data.expiresAt) : null
      }
    });

    const mapped = this.mapAnnouncement(created);

    // Broadcast Cloud Push Notification asynchronously
    fcmService.broadcastAnnouncement({
      id: created.id,
      title: created.title,
      message: created.message,
      hostelId: created.hostelId,
      priority: created.priority,
      targetAudience: created.targetAudience
    }).catch(err => console.error('[FCM] Announcement broadcast error:', err));

    return mapped;
  }

  async deleteAnnouncement(id: string) {
    await prisma.announcement.delete({
      where: { id }
    });

    return { success: true };
  }

  private mapAnnouncement(a: any) {
    let attachments: string[] = [];
    try {
      attachments = typeof a.attachmentUrls === 'string' ? JSON.parse(a.attachmentUrls) : (a.attachmentUrls || []);
    } catch {
      attachments = [];
    }
    return {
      announcementId: a.id,
      hostelId: a.hostelId,
      senderId: a.senderId,
      senderRole: a.senderRole,
      senderName: a.senderName,
      title: a.title,
      message: a.message,
      priority: a.priority,
      targetAudience: a.targetAudience,
      attachmentUrls: attachments,
      createdAt: a.createdAt.getTime(),
      expiresAt: a.expiresAt ? a.expiresAt.getTime() : null
    };
  }
}
