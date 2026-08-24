import * as jwt from 'jsonwebtoken';
import { env } from '../config/env';
import { UserRole } from '../types/enums';

export interface TokenPayload {
  userId: string;
  email: string;
  role: UserRole;
  fullName?: string;
  profileId?: string;
  hostelId?: string;
}

export function generateAccessToken(payload: TokenPayload): string {
  return jwt.sign(payload, env.jwtSecret, { expiresIn: env.jwtExpiresIn as any });
}

export function generateRefreshToken(payload: { userId: string }): string {
  return jwt.sign(payload, env.jwtRefreshSecret, { expiresIn: env.jwtRefreshExpiresIn as any });
}

export function verifyAccessToken(token: string): TokenPayload {
  return jwt.verify(token, env.jwtSecret) as TokenPayload;
}

export function verifyRefreshToken(token: string): { userId: string } {
  return jwt.verify(token, env.jwtRefreshSecret) as { userId: string };
}
