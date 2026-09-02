import { Request, Response, NextFunction } from 'express';
import { sendError } from '../utils/apiResponse';

export function errorHandler(err: any, req: Request, res: Response, next: NextFunction): void {
  console.error('[SERVER ERROR]', err);

  // 1. Prisma Client Initialization / Database Connection Failures
  if (err?.name === 'PrismaClientInitializationError' || err?.code === 'P1001' || err?.code === 'P1000') {
    const isLocal = req.ip === '127.0.0.1' || req.ip === '::1';
    console.error('❌ Database Connection Error: Unable to reach PostgreSQL database. Check DATABASE_URL configuration.');
    sendError(
      res,
      'Database connection failure. The backend could not connect to PostgreSQL. Please check DATABASE_URL and database availability.',
      503,
      [{ path: 'database', message: err.message || 'PostgreSQL server unreachable' }]
    );
    return;
  }

  // 2. Prisma Unique Constraint Violation (P2002)
  if (err?.code === 'P2002') {
    const targetField = Array.isArray(err.meta?.target) ? err.meta.target.join(', ') : 'field';
    sendError(res, `A record with this ${targetField} already exists.`, 409, [
      { path: targetField, message: `Duplicate value for ${targetField}` }
    ]);
    return;
  }

  // 3. Prisma Record Not Found (P2025)
  if (err?.code === 'P2025') {
    sendError(res, err.meta?.cause || 'Requested record not found in database.', 404);
    return;
  }

  // 4. Prisma Foreign Key Constraint Violation (P2003)
  if (err?.code === 'P2003') {
    const field = err.meta?.field_name || 'referenced entity';
    sendError(res, `Invalid reference: ${field} does not exist.`, 400, [
      { path: field, message: `Foreign key constraint violated on ${field}` }
    ]);
    return;
  }

  // 4. JWT Authentication / Token Errors
  if (err?.name === 'JsonWebTokenError') {
    sendError(res, 'Invalid authentication token. Please log in again.', 401);
    return;
  }
  if (err?.name === 'TokenExpiredError') {
    sendError(res, 'Authentication token has expired. Please refresh your session.', 401);
    return;
  }

  // 5. Zod Validation Error
  if (err?.name === 'ZodError') {
    const formattedErrors = err.issues?.map((issue: any) => ({
      path: issue.path.join('.'),
      message: issue.message
    })) || [];
    sendError(res, 'Validation error', 400, formattedErrors);
    return;
  }

  // 6. Custom or standard errors
  const statusCode = err.status || err.statusCode || 500;
  const message = err.message || 'Internal server error';

  sendError(res, message, statusCode, err.errors, err.code);
}

