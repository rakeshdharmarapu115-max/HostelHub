import { Request, Response, NextFunction } from 'express';
import { PaymentsService } from './payments.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const paymentsService = new PaymentsService();

export class PaymentsController {
  async createRazorpayOrder(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { feeId, amount } = req.body;
      if (!feeId) {
        sendError(res, 'feeId is required', 400);
        return;
      }
      const order = await paymentsService.createRazorpayOrder(feeId, amount, req.user?.userId, req.user?.role);
      sendSuccess(res, 'Razorpay order created successfully', order, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async verifyRazorpayPayment(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { feeId, razorpayOrderId, razorpayPaymentId, razorpaySignature, amountPaid } = req.body;
      if (!feeId || !razorpayOrderId || !razorpayPaymentId) {
        sendError(res, 'feeId, razorpayOrderId, and razorpayPaymentId are required', 400);
        return;
      }
      const result = await paymentsService.verifyRazorpayPayment({
        feeId,
        razorpayOrderId,
        razorpayPaymentId,
        razorpaySignature,
        amountPaid,
        userId: req.user?.userId,
        role: req.user?.role
      });
      sendSuccess(res, 'Payment verified and recorded successfully', result, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async recordPaymentFailure(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await paymentsService.recordPaymentFailure(req.body);
      sendSuccess(res, 'Payment failure recorded', result, 200);
    } catch (error: any) {
      next(error);
    }
  }

  async getTransactionHistory(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user) {
        sendError(res, 'Unauthorized', 401);
        return;
      }
      const history = await paymentsService.getTransactionHistory({
        userId: req.user.userId,
        role: req.user.role,
        profileId: req.user.profileId,
        hostelId: req.user.hostelId
      });
      sendSuccess(res, 'Payment transaction history retrieved', history, 200);
    } catch (error: any) {
      next(error);
    }
  }

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

  async getStudentHostelPaymentConfig(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user?.userId) {
        sendError(res, 'Authentication required', 401);
        return;
      }
      const config = await paymentsService.getStudentHostelPaymentConfig(req.user.userId);
      sendSuccess(res, 'Student hostel payment configuration retrieved successfully', config, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async getHostelPaymentConfig(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = req.params.hostelId;
      if (!hostelId) {
        sendError(res, 'hostelId parameter is required', 400);
        return;
      }
      const config = await paymentsService.getHostelPaymentConfig(
        hostelId,
        req.user?.userId,
        req.user?.role
      );
      sendSuccess(res, 'Hostel payment configuration retrieved', config, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async updateHostelPaymentConfig(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = req.params.hostelId;
      if (!hostelId) {
        sendError(res, 'hostelId parameter is required', 400);
        return;
      }
      const updated = await paymentsService.updateHostelPaymentConfig(
        hostelId,
        req.body,
        req.user?.userId,
        req.user?.role
      );
      sendSuccess(res, 'Hostel payment settings updated successfully', updated, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async submitManualQrPayment(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { feeId, amountPaid, transactionReference, remarks, receiptUrl } = req.body;
      if (!feeId || !transactionReference || !amountPaid) {
        sendError(res, 'feeId, amountPaid, and transactionReference (UTR/Ref) are required', 400);
        return;
      }
      const payment = await paymentsService.submitManualQrPayment({
        feeId,
        amountPaid: Number(amountPaid),
        transactionReference,
        remarks,
        receiptUrl,
        userId: req.user?.userId
      });
      sendSuccess(res, 'Manual QR payment submitted for host verification', payment, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async verifyManualPayment(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const paymentId = req.params.id;
      const { approved, remarks } = req.body;
      if (!paymentId || typeof approved !== 'boolean') {
        sendError(res, 'paymentId and approved (boolean) are required', 400);
        return;
      }
      const payment = await paymentsService.verifyManualPayment({
        paymentId,
        approved,
        remarks,
        hostUserId: req.user?.userId || 'host_default'
      });
      sendSuccess(res, approved ? 'Payment verified and marked PAID' : 'Payment rejected', payment, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async getAdminPaymentOverview(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const overview = await paymentsService.getAdminPaymentOverview();
      sendSuccess(res, 'Admin payment and collection overview retrieved', overview, 200);
    } catch (error: any) {
      next(error);
    }
  }
}
