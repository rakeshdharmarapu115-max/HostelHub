import { Request, Response, NextFunction } from 'express';
import { ComplaintsService } from './complaints.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const complaintsService = new ComplaintsService();

export class ComplaintsController {
  async getComplaintsForStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const complaints = await complaintsService.getComplaintsForStudent(req.params.studentId);
      sendSuccess(res, 'Student complaints retrieved', complaints);
    } catch (error) {
      next(error);
    }
  }

  async getComplaintsForHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const complaints = await complaintsService.getComplaintsForHostel(req.params.hostelId);
      sendSuccess(res, 'Hostel complaints retrieved', complaints);
    } catch (error) {
      next(error);
    }
  }

  async getAllComplaints(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const complaints = await complaintsService.getAllComplaints();
      sendSuccess(res, 'All complaints retrieved', complaints);
    } catch (error) {
      next(error);
    }
  }

  async getComplaintById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const complaint = await complaintsService.getComplaintById(req.params.id);
      sendSuccess(res, 'Complaint details retrieved', complaint);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async submitComplaint(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const studentId = req.body.studentId || req.user?.profileId || req.user?.userId;
      if (!studentId || !req.body.title || !req.body.description) {
        sendError(res, 'title and description are required', 400);
        return;
      }
      const complaint = await complaintsService.submitComplaint({
        ...req.body,
        studentId
      });
      sendSuccess(res, 'Complaint submitted successfully', complaint, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async updateComplaintStatus(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { status, notes, assignedStaffName, resolutionSummary } = req.body;
      const updated = await complaintsService.updateComplaintStatus(req.params.id, {
        status,
        notes,
        assignedStaffName,
        resolutionSummary
      });
      sendSuccess(res, 'Complaint status updated successfully', updated);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async deleteComplaint(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await complaintsService.deleteComplaint(req.params.id);
      sendSuccess(res, 'Complaint deleted successfully', result);
    } catch (error) {
      next(error);
    }
  }
}
