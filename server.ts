import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import dotenv from 'dotenv';

import { bootstrapAppEngine } from './bootstrap';

dotenv.config();

const app = express();

const PORT = Number(process.env.PORT) || 5000;

// ============================================
// MIDDLEWARE
// ============================================

app.use(helmet());

app.use(
  cors({
    origin: process.env.CORS_ORIGINS
      ? process.env.CORS_ORIGINS.split(',')
      : '*'
  })
);

app.use(compression());

app.use(express.json({ limit: '10mb' }));

app.use(express.urlencoded({
  extended: true,
  limit: '10mb'
}));

// ============================================
// HEALTH CHECK
// ============================================

app.get('/', (_req, res) => {
  res.json({
    success: true,
    service: 'SHEBAODDS Backend',
    status: 'online',
    version: '3.0.0'
  });
});

app.get('/health', (_req, res) => {
  res.status(200).json({
    success: true,
    status: 'healthy',
    database:
      process.env.MONGODB_URI
        ? 'configured'
        : 'not configured'
  });
});

// ============================================
// START SERVER
// ============================================

async function startServer() {
  try {

    await bootstrapAppEngine();

    app.listen(PORT, '0.0.0.0', () => {

      console.log('');
      console.log('============================================');
      console.log('🦁 SHEBAODDS SERVER ONLINE');
      console.log('============================================');
      console.log(`🚀 Port: ${PORT}`);
      console.log(`🌍 Environment: ${process.env.NODE_ENV}`);
      console.log('============================================');

    });

  } catch (error) {

    console.error('');
    console.error('💥 SERVER STARTUP FAILED');
    console.error(error);

    process.exit(1);
  }
}

startServer();

export default app;