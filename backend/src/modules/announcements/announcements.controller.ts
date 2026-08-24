import { Request, Response, NextFunction } from 'express';
import { AnnouncementsService } from './announcements.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const announcementsService = new AnnouncementsService();

export class AnnouncementsController {
  async getAnnouncements(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = (req.query.hostelId as string) || (req.params.hostelId as string);
      const announcements = await announcementsService.getAnnouncements(hostelId);
      sendSuccess(res, 'Announcements retrieved', announcements);
    } catch (error) {
      next(error);
    }
  }

  async getAnnouncementById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const announcement = await announcementsService.getAnnouncementById(req.params.id);
      sendSuccess(res, 'Announcement details retrieved', announcement);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async createAnnouncement(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const senderId = req.user?.userId || req.body.senderId;
      if (!senderId || !req.body.title || !req.body.message) {
        sendError(res, 'title and message are required', 400);
        return;
      }
      const created = await announcementsService.createAnnouncement({
        ...req.body,
        senderId,
        senderRole: req.user?.role
      });
      sendSuccess(res, 'Announcement created successfully', created, 201);
    } catch (error) {
      next(error);
    }
  }

  async deleteAnnouncement(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await announcementsService.deleteAnnouncement(req.params.id);
      sendSuccess(res, 'Announcement deleted successfully', result);
    } catch (error) {
      next(error);
    }
  }
}
