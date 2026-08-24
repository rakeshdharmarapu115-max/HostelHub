import { Request, Response, NextFunction } from 'express';
import { NotificationsService } from './notifications.service';
import { sendSuccess } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const notificationsService = new NotificationsService();

export class NotificationsController {
  async getNotifications(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.userId || (req.query.userId as string);
      const notifications = await notificationsService.getNotificationsForUser(userId);
      sendSuccess(res, 'Notifications retrieved', notifications);
    } catch (error) {
      next(error);
    }
  }

  async markAsRead(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await notificationsService.markAsRead(req.params.id);
      sendSuccess(res, 'Notification marked as read', result);
    } catch (error) {
      next(error);
    }
  }

  async markAllAsRead(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.userId;
      if (userId) {
        await notificationsService.markAllAsRead(userId);
      }
      sendSuccess(res, 'All notifications marked as read', { success: true });
    } catch (error) {
      next(error);
    }
  }
}
