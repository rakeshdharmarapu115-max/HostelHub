import { Response } from 'express';

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errors?: any[];
}

export function sendSuccess<T>(res: Response, message: string = 'Success', data?: T, statusCode: number = 200) {
  return res.status(statusCode).json({
    success: true,
    message,
    data
  });
}

export function sendError(res: Response, message: string = 'An error occurred', statusCode: number = 500, errors?: any[], code?: string) {
  return res.status(statusCode).json({
    success: false,
    message,
    code,
    errors: errors || []
  });
}
