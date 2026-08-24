import { Request, Response, NextFunction } from 'express';
import { AllocationsService } from './allocations.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const allocationsService = new AllocationsService();

export class AllocationsController {
  async allocateBed(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const bedId = req.body.bedId || req.body.id;
      const roomId = req.body.roomId || req.body.room_id;
      const studentId = req.body.studentId || req.body.student_id || req.body.rollNumber;
      const studentName = req.body.studentName || req.body.fullName;
      const remarks = req.body.remarks;

      if (!bedId || !roomId || !studentId) {
        sendError(res, 'bedId, roomId, and studentId are required', 400);
        return;
      }

      const result = await allocationsService.allocateBed({
        bedId,
        roomId,
        studentId,
        studentName,
        allocatedBy: req.user?.userId,
        remarks
      });
      sendSuccess(res, 'Bed allocated successfully', result, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async vacateBed(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const bedId = req.body.bedId || req.body.id || (req.query.bedId as string);
      const roomId = req.body.roomId || req.body.room_id || (req.query.roomId as string);
      const allocationId = req.params.id || req.body.allocationId;
      const remarks = req.body.remarks;

      const result = await allocationsService.vacateBed({
        bedId,
        roomId,
        allocationId,
        vacatedBy: req.user?.userId,
        remarks
      });
      sendSuccess(res, 'Bed vacated successfully', result);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async getAllocationsByStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const allocations = await allocationsService.getAllocationsByStudent(req.params.studentId);
      sendSuccess(res, 'Student allocation history retrieved', allocations);
    } catch (error) {
      next(error);
    }
  }
}
