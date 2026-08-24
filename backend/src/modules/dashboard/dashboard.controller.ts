import { Request, Response, NextFunction } from 'express';
import { DashboardService } from './dashboard.service';
import { sendSuccess } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const dashboardService = new DashboardService();

export class DashboardController {
  async getStudentDashboard(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const studentId = (req.query.studentId as string) || req.user?.profileId || req.user?.userId || '';
      const stats = await dashboardService.getStudentDashboardStats(studentId);
      sendSuccess(res, 'Student dashboard stats retrieved', stats);
    } catch (error) {
      next(error);
    }
  }

  async getHostDashboard(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = (req.query.hostelId as string) || req.user?.hostelId;
      const stats = await dashboardService.getHostDashboardStats(hostelId, req.user?.userId);
      sendSuccess(res, 'Host dashboard stats retrieved', stats);
    } catch (error) {
      next(error);
    }
  }

  async getAdminDashboard(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const stats = await dashboardService.getAdminDashboardStats();
      sendSuccess(res, 'Admin dashboard stats retrieved', stats);
    } catch (error) {
      next(error);
    }
  }
}
