import { Request, Response, NextFunction } from 'express';
import { StudentsService } from './students.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';
import { UserRole } from '../../types/enums';

const studentsService = new StudentsService();

export class StudentsController {
  async getAllStudents(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const students = await studentsService.getAllStudents();
      sendSuccess(res, 'Students retrieved successfully', students);
    } catch (error) {
      next(error);
    }
  }

  async getStudentById(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const targetId = req.params.id;
      // Allow student to access own profile, or host/admin to view
      if (req.user?.role === UserRole.STUDENT && req.user.profileId !== targetId && req.user.userId !== targetId) {
        sendError(res, 'Forbidden: You can only view your own profile', 403);
        return;
      }
      const student = await studentsService.getStudentById(targetId);
      sendSuccess(res, 'Student profile retrieved', student);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async getResidentsByHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const residents = await studentsService.getStudentsByHostel(req.params.hostelId);
      sendSuccess(res, 'Hostel residents retrieved', residents);
    } catch (error) {
      next(error);
    }
  }

  async updateStudentProfile(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const targetId = req.params.id;
      if (req.user?.role === UserRole.STUDENT && req.user.profileId !== targetId && req.user.userId !== targetId) {
        sendError(res, 'Forbidden: You can only update your own profile', 403);
        return;
      }
      const updated = await studentsService.updateStudentProfile(targetId, req.body);
      sendSuccess(res, 'Student profile updated successfully', updated);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async generateStudentId(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const studentId = await studentsService.generateUniqueStudentId();
      sendSuccess(res, 'Student ID generated successfully', { studentId });
    } catch (error) {
      next(error);
    }
  }

  async createStudentByAdmin(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await studentsService.createStudentByAdmin(req.body, req.user);
      sendSuccess(res, result.message, result, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async deallocateStudent(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const targetStudentId = req.params.id || req.body.studentId;
      const remarks = req.body.remarks;
      const requesterHostId = req.user?.profileId;
      const requesterRole = req.user?.role;

      const result = await studentsService.deallocateStudent({
        studentId: targetStudentId,
        requesterHostId,
        requesterRole,
        remarks
      });

      sendSuccess(res, result.message, result);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async deleteStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await studentsService.deleteStudent(req.params.id);
      sendSuccess(res, 'Student deleted successfully', result);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }
}
