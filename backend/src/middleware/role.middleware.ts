import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from './auth.middleware';
import { UserRole } from '../types/enums';
import { sendError } from '../utils/apiResponse';

export function authorize(...allowedRoles: UserRole[]) {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction): void => {
    if (!req.user) {
      sendError(res, 'Unauthorized: user not authenticated', 401);
      return;
    }

    if (!allowedRoles.includes(req.user.role)) {
      sendError(res, `Forbidden: role '${req.user.role}' lacks required permissions`, 403);
      return;
    }

    next();
  };
}
