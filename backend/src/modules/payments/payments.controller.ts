import { Request, Response, NextFunction } from 'express';
import { PaymentsService } from './payments.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const paymentsService = new PaymentsService();

export class PaymentsController {
  async getPaymentsForStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payments = await paymentsService.getPaymentsForStudent(req.params.studentId);
      sendSuccess(res, 'Student payments retrieved', payments);
    } catch (error) {
      next(error);
    }
  }

  async getPaymentsForHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payments = await paymentsService.getPaymentsForHostel(req.params.hostelId);
      sendSuccess(res, 'Hostel payments history retrieved', payments);
    } catch (error) {
      next(error);
    }
  }

  async getPaymentById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payment = await paymentsService.getPaymentById(req.params.id);
      sendSuccess(res, 'Payment receipt retrieved', payment);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async recordPayment(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const studentId = req.body.studentId || req.user?.profileId || req.user?.userId;
      if (!req.body.feeId || !req.body.amountPaid) {
        sendError(res, 'feeId and amountPaid are required', 400);
        return;
      }
      const payment = await paymentsService.recordPayment({
        ...req.body,
        studentId
      });
      sendSuccess(res, 'Payment processed successfully', payment, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }
}
