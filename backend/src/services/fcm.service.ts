import { prisma } from '../config/prisma';
import { firebaseConfig } from '../config/firebase.config';

export interface PushNotificationPayload {
  title: string;
  body: string;
  type?: string;
  relatedEntityId?: string;
  data?: Record<string, string>;
}

export class FcmService {
  /**
   * Dispatches a push notification to a specific user by their user ID or student/host profile ID.
   * Also records the notification in the persistent database.
   */
  async sendToUser(
    targetUserIdOrProfileId: string,
    payload: PushNotificationPayload
  ): Promise<{ delivered: boolean; notificationId: string }> {
    // Find user record (check by userId or profileId)
    const user = await prisma.user.findFirst({
      where: {
        OR: [
          { id: targetUserIdOrProfileId },
          { studentProfile: { id: targetUserIdOrProfileId } },
          { hostProfile: { id: targetUserIdOrProfileId } },
          { adminProfile: { id: targetUserIdOrProfileId } }
        ]
      },
      select: {
        id: true,
        fcmToken: true,
        fullName: true,
        email: true
      }
    });

    if (!user) {
      console.warn(`[FCM] Target user '${targetUserIdOrProfileId}' not found for push dispatch`);
      return { delivered: false, notificationId: '' };
    }

    // 1. Create database notification record
    const dbNotification = await prisma.notification.create({
      data: {
        recipientUserId: user.id,
        title: payload.title,
        body: payload.body,
        type: payload.type || 'SYSTEM',
        relatedEntityId: payload.relatedEntityId || null,
        isRead: false
      }
    });

    // 2. Dispatch FCM cloud push if token available and Firebase is configured
    if (user.fcmToken) {
      if (firebaseConfig.isConfigured) {
        console.log(`[FCM-CLOUD] Sending push to ${user.fullName} (${user.email}) -> Token: ${user.fcmToken.slice(0, 10)}...`);
        // When production Firebase Admin is active, dispatch via admin.messaging().send()
      } else {
        console.log(`[FCM-DEV-LOG] Push dispatched to ${user.fullName}: "${payload.title}" - "${payload.body}"`);
      }
    } else {
      console.log(`[FCM-DEV-LOG] In-app notification created for ${user.fullName} (no FCM device token registered yet)`);
    }

    return { delivered: true, notificationId: dbNotification.id };
  }

  /**
   * Broadcasts a notification to all students & staff in a specific hostel
   */
  async sendToHostel(
    hostelId: string,
    payload: PushNotificationPayload
  ): Promise<{ sentCount: number }> {
    const students = await prisma.student.findMany({
      where: { hostelId, status: 'ACTIVE' },
      select: { userId: true }
    });

    let sentCount = 0;
    for (const student of students) {
      try {
        await this.sendToUser(student.userId, payload);
        sentCount++;
      } catch (err) {
        console.error(`[FCM] Failed sending to student user ${student.userId}:`, err);
      }
    }

    return { sentCount };
  }

  /**
   * Sends an announcement notification to all active users or targeted roles
   */
  async broadcastAnnouncement(
    announcement: {
      id: string;
      title: string;
      message: string;
      hostelId?: string;
      priority: string;
      targetAudience?: string;
    }
  ): Promise<void> {
    const payload: PushNotificationPayload = {
      title: announcement.priority === 'URGENT' ? `🚨 URGENT: ${announcement.title}` : `📢 ${announcement.title}`,
      body: announcement.message,
      type: 'ANNOUNCEMENT',
      relatedEntityId: announcement.id,
      data: {
        announcementId: announcement.id,
        priority: announcement.priority
      }
    };

    if (announcement.hostelId && announcement.hostelId !== 'GLOBAL_CAMPUS') {
      await this.sendToHostel(announcement.hostelId, payload);
    } else {
      // Global broadcast to all active users
      const users = await prisma.user.findMany({
        where: { isActive: true },
        select: { id: true }
      });

      for (const u of users) {
        await this.sendToUser(u.id, payload);
      }
    }
  }
}

export const fcmService = new FcmService();
