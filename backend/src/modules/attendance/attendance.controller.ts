import { Request, Response, NextFunction } from 'express';
import { AttendanceService } from './attendance.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const attendanceService = new AttendanceService();

export class AttendanceController {
  async getAttendanceForStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { month, year } = req.query;
      const records = await attendanceService.getAttendanceForStudent(
        req.params.studentId,
        month ? Number(month) : undefined,
        year ? Number(year) : undefined
      );
      sendSuccess(res, 'Attendance records retrieved', records);
    } catch (error) {
      next(error);
    }
  }

  async getAttendanceForHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const records = await attendanceService.getAttendanceForHostel(
        req.params.hostelId,
        req.query.date as string
      );
      sendSuccess(res, 'Hostel attendance records retrieved', records);
    } catch (error) {
      next(error);
    }
  }

  async markAttendance(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const markedBy = req.user?.role === 'STUDENT' ? 'STUDENT_SELF' : (req.user?.role || 'STUDENT_SELF');
      const studentId = req.body.studentId || req.user?.profileId || req.user?.userId;
      if (!studentId) {
        sendError(res, 'studentId is required', 400);
        return;
      }
      const record = await attendanceService.markAttendance({
        ...req.body,
        studentId,
        markedBy
      });
      sendSuccess(res, 'Attendance recorded successfully', record, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async markBatchAttendance(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { records } = req.body;
      if (!Array.isArray(records)) {
        sendError(res, 'records must be an array of attendance items', 400);
        return;
      }
      const result = await attendanceService.markBatchAttendance(records);
      sendSuccess(res, 'Batch attendance recorded successfully', result);
    } catch (error) {
      next(error);
    }
  }
}
