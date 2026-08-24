import { Request, Response, NextFunction } from 'express';
import { VisitorsService } from './visitors.service';
import { sendSuccess } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const visitorsService = new VisitorsService();

export class VisitorsController {
  async getVisitorsForHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const visitors = await visitorsService.getVisitorsForHostel(req.params.hostelId);
      sendSuccess(res, 'Hostel visitors retrieved', visitors);
    } catch (error) {
      next(error);
    }
  }

  async getVisitorsForStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const visitors = await visitorsService.getVisitorsForStudent(req.params.studentId);
      sendSuccess(res, 'Student visitors retrieved', visitors);
    } catch (error) {
      next(error);
    }
  }

  async registerVisitor(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const visitor = await visitorsService.registerVisitor({
        ...req.body,
        approvedBy: req.user?.userId
      });
      sendSuccess(res, 'Visitor checked in successfully', visitor, 201);
    } catch (error) {
      next(error);
    }
  }

  async checkoutVisitor(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const updated = await visitorsService.checkoutVisitor(req.params.id);
      sendSuccess(res, 'Visitor checked out successfully', updated);
    } catch (error) {
      next(error);
    }
  }
}
