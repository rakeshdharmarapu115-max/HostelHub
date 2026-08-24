import { Request, Response, NextFunction } from 'express';
import { LeaveRequestsService } from './leave-requests.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const leaveRequestsService = new LeaveRequestsService();

export class LeaveRequestsController {
  async getLeaveRequestsForStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const leaves = await leaveRequestsService.getLeaveRequestsForStudent(req.params.studentId);
      sendSuccess(res, 'Leave requests retrieved', leaves);
    } catch (error) {
      next(error);
    }
  }

  async getLeaveRequestsForHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const leaves = await leaveRequestsService.getLeaveRequestsForHostel(req.params.hostelId);
      sendSuccess(res, 'Hostel leave requests retrieved', leaves);
    } catch (error) {
      next(error);
    }
  }

  async createLeaveRequest(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const studentId = req.body.studentId || req.user?.profileId || req.user?.userId;
      if (!studentId || !req.body.startDate || !req.body.endDate || !req.body.reason) {
        sendError(res, 'startDate, endDate, and reason are required', 400);
        return;
      }
      const leave = await leaveRequestsService.createLeaveRequest({
        ...req.body,
        studentId
      });
      sendSuccess(res, 'Leave request created successfully', leave, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async updateLeaveStatus(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const updated = await leaveRequestsService.updateLeaveStatus(req.params.id, {
        ...req.body,
        approverId: req.user?.userId
      });
      sendSuccess(res, 'Leave request status updated', updated);
    } catch (error) {
      next(error);
    }
  }
}
