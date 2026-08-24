import { Request, Response, NextFunction } from 'express';
import { AuthService } from './auth.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';

const authService = new AuthService();

export class AuthController {
  async registerStudent(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await authService.registerStudent(req.body);
      sendSuccess(res, 'Student registered successfully', result, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async registerHost(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await authService.registerHost(req.body);
      sendSuccess(res, 'Host registered successfully', result, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async registerAdmin(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await authService.registerAdmin(req.body);
      sendSuccess(res, 'Association Head registered successfully', result, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async login(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { email, password } = req.body;
      const result = await authService.login(email, password);
      sendSuccess(res, 'Login successful', result, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async refresh(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { refreshToken } = req.body;
      const result = await authService.refreshToken(refreshToken);
      sendSuccess(res, 'Token refreshed successfully', result, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async logout(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { refreshToken } = req.body;
      await authService.logout(refreshToken);
      sendSuccess(res, 'Logged out successfully', null, 200);
    } catch (error: any) {
      next(error);
    }
  }

  async getMe(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user) {
        sendError(res, 'Unauthorized', 401);
        return;
      }
      const user = await authService.getMe(req.user.userId);
      sendSuccess(res, 'Current user profile retrieved', user, 200);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }
}
