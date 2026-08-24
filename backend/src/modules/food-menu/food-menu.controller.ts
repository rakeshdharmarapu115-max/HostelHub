import { Request, Response, NextFunction } from 'express';
import { FoodMenuService } from './food-menu.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';

const foodMenuService = new FoodMenuService();

export class FoodMenuController {
  async getWeeklyMenu(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = (req.query.hostelId as string) || req.params.hostelId;
      const weekStartDate = req.query.weekStartDate as string;
      const menu = await foodMenuService.getWeeklyMenu(hostelId, weekStartDate);
      sendSuccess(res, 'Food menu retrieved', menu);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async createOrUpdateMenu(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const menu = await foodMenuService.createOrUpdateMenu(req.body);
      sendSuccess(res, 'Food menu saved successfully', menu, 200);
    } catch (error) {
      next(error);
    }
  }

  async deleteMenu(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await foodMenuService.deleteMenu(req.params.id);
      sendSuccess(res, 'Food menu deleted successfully', result);
    } catch (error) {
      next(error);
    }
  }
}
