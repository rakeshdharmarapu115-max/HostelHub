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
}
