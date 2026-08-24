import { Request, Response, NextFunction } from 'express';
import { RoomsService } from './rooms.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';

const roomsService = new RoomsService();

export class RoomsController {
  async getRoomsByHostel(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const rooms = await roomsService.getRoomsByHostel(req.params.hostelId);
      sendSuccess(res, 'Rooms retrieved successfully', rooms);
    } catch (error) {
      next(error);
    }
  }

  async getRoomById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const room = await roomsService.getRoomById(req.params.id);
      sendSuccess(res, 'Room details retrieved', room);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async addRoom(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = req.params.hostelId || req.body.hostelId;
      const room = await roomsService.addRoom({ ...req.body, hostelId });
      sendSuccess(res, 'Room added successfully', room, 201);
    } catch (error) {
      next(error);
    }
  }

  async updateRoom(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const room = await roomsService.updateRoom(req.params.id, req.body);
      sendSuccess(res, 'Room updated successfully', room);
    } catch (error) {
      next(error);
    }
  }

  async deleteRoom(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await roomsService.deleteRoom(req.params.id);
      sendSuccess(res, 'Room deleted successfully', result);
    } catch (error) {
      next(error);
    }
  }

  // Beds
  async getBedsByRoom(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const beds = await roomsService.getBedsByRoom(req.params.roomId);
      sendSuccess(res, 'Beds retrieved successfully', beds);
    } catch (error) {
      next(error);
    }
  }

  async addBed(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const bed = await roomsService.addBed(req.params.roomId, req.body.bedNumber);
      sendSuccess(res, 'Bed added successfully', bed, 201);
    } catch (error) {
      next(error);
    }
  }

  async deleteBed(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await roomsService.deleteBed(req.params.id);
      sendSuccess(res, 'Bed deleted successfully', result);
    } catch (error) {
      next(error);
    }
  }
}
