import { Request, Response, NextFunction } from 'express';
import { ZodSchema, ZodError } from 'zod';
import { sendError } from '../utils/apiResponse';

export function validate(schema: ZodSchema) {
  return async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const parsed = await schema.parseAsync({
        body: req.body,
        query: req.query,
        params: req.params
      });
      req.body = parsed.body !== undefined ? parsed.body : req.body;
      req.query = parsed.query !== undefined ? parsed.query : req.query;
      req.params = parsed.params !== undefined ? parsed.params : req.params;
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const errors = error.errors.map(err => ({
          path: err.path.join('.'),
          message: err.message
        }));
        sendError(res, 'Validation failed', 400, errors);
      } else {
        sendError(res, 'Invalid request data', 400);
      }
    }
  };
}
