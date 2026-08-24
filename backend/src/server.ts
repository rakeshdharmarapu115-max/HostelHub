import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import swaggerUi from 'swagger-ui-express';
import { env } from './config/env';
import { swaggerDocument } from './config/swagger';
import apiRoutes from './routes';
import { errorHandler } from './middleware/error.middleware';

export const app = express();

// Trust proxy for cloud deployments (Render, Railway, Fly.io, Heroku) and tunnels (Cloudflare, Localtunnel, Ngrok)
app.set('trust proxy', 1);

// Security and utility middleware
app.use(helmet({
  contentSecurityPolicy: false // Allows Swagger UI to render properly
}));
app.use(cors({
  origin: env.corsOrigin === '*' ? true : env.corsOrigin.split(','),
  credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

if (env.nodeEnv !== 'test') {
  app.use(morgan('dev'));
}

// Health Check (with PostgreSQL database connectivity diagnostic and timeout protection)
app.get('/health', async (req, res) => {
  let dbStatus = 'disconnected';
  let dbLatencyMs: number | null = null;

  try {
    const { prisma } = await import('./config/prisma');
    const start = Date.now();
    const pingPromise = prisma.$queryRaw`SELECT 1`;
    const timeoutPromise = new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), 1500));
    await Promise.race([pingPromise, timeoutPromise]);
    dbLatencyMs = Date.now() - start;
    dbStatus = 'connected';
  } catch (err: any) {
    dbStatus = err.message === 'timeout' ? 'timeout (database cold-starting or connecting)' : `error: ${err.message || 'unreachable'}`;
  }

  res.status(200).json({
    status: 'ok',
    service: 'HostelHub Backend',
    database: {
      status: dbStatus,
      latencyMs: dbLatencyMs
    },
    environment: env.nodeEnv,
    timestamp: new Date().toISOString()
  });
});

// Swagger API Documentation
app.use('/api/docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

// Mount REST API Routes
app.use('/api', apiRoutes);

// Centralized Error Handling
app.use(errorHandler);

import { autoSeedIfEmpty } from './utils/autoSeed';

// Start server if not in test mode
if (env.nodeEnv !== 'test') {
  app.listen(env.port, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`☁️ HostelHub Cloud Server active on port ${env.port}`);
    console.log(`🌐 API Base URL: http://0.0.0.0:${env.port}/api`);
    console.log(`📖 Swagger API Docs: /api/docs`);
    console.log(`🩺 Health Check: /health`);
    console.log(`=======================================================`);

    // Auto-seed database if fresh deployment
    autoSeedIfEmpty().catch(err => console.error('Seed error:', err));
  });
}
