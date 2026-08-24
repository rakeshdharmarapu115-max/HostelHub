import { Request, Response, NextFunction } from 'express';
import { FeesService } from './fees.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';

const feesService = new FeesService();

export class FeesController {
  async getFeesForStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const fees = await feesService.getFeesForStudent(req.params.studentId);
      sendSuccess(res, 'Student fees retrieved', fees);
    } catch (error) {
      next(error);
    }
  }

  async getFeesForHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const fees = await feesService.getFeesForHostel(req.params.hostelId);
      sendSuccess(res, 'Hostel fees retrieved', fees);
    } catch (error) {
      next(error);
    }
  }

  async getAllFees(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const fees = await feesService.getAllFees();
      sendSuccess(res, 'All fees retrieved', fees);
    } catch (error) {
      next(error);
    }
  }

  async createFee(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const created = await feesService.createFee(req.body);
      sendSuccess(res, 'Fee created successfully', created, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async updateFee(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const updated = await feesService.updateFee(req.params.id, req.body);
      sendSuccess(res, 'Fee updated successfully', updated);
    } catch (error) {
      next(error);
    }
  }
}
