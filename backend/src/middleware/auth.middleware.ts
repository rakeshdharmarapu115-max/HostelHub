import { Request, Response, NextFunction } from 'express';
import { verifyAccessToken, TokenPayload } from '../utils/jwt';
import { sendError } from '../utils/apiResponse';
import { prisma } from '../config/prisma';

export interface AuthenticatedRequest extends Request {
  user?: TokenPayload;
}

export async function authenticate(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    sendError(res, 'Authentication token required', 401);
    return;
  }

  const token = authHeader.split(' ')[1];
  try {
    const payload = verifyAccessToken(token);

    // Verify user is still active in database
    const user = await prisma.user.findFirst({
      where: {
        OR: [
          { id: payload.userId },
          { studentProfile: { id: payload.userId } },
          { hostProfile: { id: payload.userId } },
          { adminProfile: { id: payload.userId } }
        ]
      },
      select: {
        id: true,
        isActive: true,
        role: true,
        studentProfile: {
          select: { status: true }
        }
      }
    });

    if (!user || !user.isActive) {
      sendError(res, 'Your hostel allocation has ended. You have been logged out.', 403, undefined, 'HOSTEL_ALLOCATION_INACTIVE');
      return;
    }

    if (user.role === 'STUDENT' && (user.studentProfile?.status === 'DEALLOCATED' || user.studentProfile?.status !== 'ACTIVE')) {
      sendError(
        res,
        'Your hostel allocation has ended. You have been logged out.',
        403,
        undefined,
        'HOSTEL_ALLOCATION_INACTIVE'
      );
      return;
    }

    req.user = payload;
    next();
  } catch (error) {
    sendError(res, 'Invalid or expired token', 401);
  }
}
