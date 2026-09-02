import { Request, Response, NextFunction } from 'express';
import { UsersService } from './users.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';

const usersService = new UsersService();

export class UsersController {
  async getAllUsers(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const users = await usersService.getAllUsers();
      sendSuccess(res, 'Users retrieved successfully', users);
    } catch (error) {
      next(error);
    }
  }

  async getUserById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const user = await usersService.getUserById(req.params.id);
      sendSuccess(res, 'User retrieved successfully', user);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async updateUser(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const user = await usersService.updateUser(req.params.id, req.body);
      sendSuccess(res, 'User updated successfully', user);
    } catch (error) {
      next(error);
    }
  }

  async toggleStatus(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { isActive } = req.body;
      const user = await usersService.toggleUserStatus(req.params.id, Boolean(isActive));
      sendSuccess(res, 'User status updated successfully', user);
    } catch (error) {
      next(error);
    }
  }

  async getMySettings(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = (req as any).user?.id || (req as any).user?.userId;
      if (!userId) {
        sendError(res, 'Unauthorized', 401);
        return;
      }
      const settings = await usersService.getUserSettings(userId);
      sendSuccess(res, 'Settings retrieved successfully', settings);
    } catch (error) {
      next(error);
    }
  }

  async updateMySettings(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = (req as any).user?.id || (req as any).user?.userId;
      if (!userId) {
        sendError(res, 'Unauthorized', 401);
        return;
      }
      const settings = await usersService.updateUserSettings(userId, req.body);
      sendSuccess(res, 'Settings updated successfully', settings);
    } catch (error) {
      next(error);
    }
  }
}
