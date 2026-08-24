import { prisma } from '../../config/prisma';

export class NotificationsService {
  async getNotificationsForUser(userId: string) {
    const notifications = await prisma.notification.findMany({
      where: { recipientUserId: userId },
      orderBy: { createdAt: 'desc' }
    });

    return notifications.map(n => ({
      notificationId: n.id,
      recipientUserId: n.recipientUserId,
      title: n.title,
      body: n.body,
      type: n.type,
      relatedEntityId: n.relatedEntityId,
      isRead: n.isRead,
      createdAt: n.createdAt.getTime()
    }));
  }

  async markAsRead(id: string) {
    await prisma.notification.update({
      where: { id },
      data: { isRead: true }
    });

    return { success: true };
  }

  async markAllAsRead(userId: string) {
    await prisma.notification.updateMany({
      where: { recipientUserId: userId },
      data: { isRead: true }
    });

    return { success: true };
  }
}
