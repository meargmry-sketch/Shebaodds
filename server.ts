import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import dotenv from 'dotenv';

import { bootstrapAppEngine } from './bootstrap';

dotenv.config();

/*
|--------------------------------------------------------------------------
| EXPRESS APPLICATION
|--------------------------------------------------------------------------
*/

const app = express();

/*
|--------------------------------------------------------------------------
| PORT
|--------------------------------------------------------------------------
*/

const PORT = Number(process.env.PORT) || 5000;

/*
|--------------------------------------------------------------------------
| SECURITY MIDDLEWARE
|--------------------------------------------------------------------------
*/

app.use(
  helmet()
);

/*
|--------------------------------------------------------------------------
| CORS
|--------------------------------------------------------------------------
*/

const corsOrigins = process.env.CORS_ORIGINS
  ? process.env.CORS_ORIGINS
      .split(',')
      .map((origin) => origin.trim())
      .filter(Boolean)
  : true;

app.use(
  cors({
    origin: corsOrigins,
    credentials: true
  })
);

/*
|--------------------------------------------------------------------------
| COMPRESSION
|--------------------------------------------------------------------------
*/

app.use(
  compression()
);

/*
|--------------------------------------------------------------------------
| BODY PARSERS
|--------------------------------------------------------------------------
*/

app.use(
  express.json({
    limit: '10mb'
  })
);

app.use(
  express.urlencoded({
    extended: true,
    limit: '10mb'
  })
);

/*
|--------------------------------------------------------------------------
| REQUEST LOGGER
|--------------------------------------------------------------------------
*/

app.use(
  (req, _res, next) => {
    console.log(
      `[HTTP] ${req.method} ${req.originalUrl}`
    );

    next();
  }
);

/*
|--------------------------------------------------------------------------
| ROOT ROUTE
|--------------------------------------------------------------------------
*/

app.get(
  '/',
  (_req, res) => {
    res.status(200).json({
      success: true,
      service: 'SHEBAODDS Backend',
      status: 'online',
      version: '3.0.0',
      environment:
        process.env.NODE_ENV || 'development',
      timestamp: new Date().toISOString()
    });
  }
);

/*
|--------------------------------------------------------------------------
| HEALTH CHECK
|--------------------------------------------------------------------------
*/

app.get(
  '/health',
  (_req, res) => {
    const mongoConnected =
      mongooseConnectionState();

    res.status(
      mongoConnected ? 200 : 503
    ).json({
      success: mongoConnected,
      status:
        mongoConnected
          ? 'healthy'
          : 'unhealthy',
      service: 'SHEBAODDS Backend',
      database:
        mongoConnected
          ? 'connected'
          : 'disconnected',
      timestamp: new Date().toISOString()
    });
  }
);

/*
|--------------------------------------------------------------------------
| API HEALTH CHECK
|--------------------------------------------------------------------------
*/

app.get(
  '/api/health',
  (_req, res) => {
    res.status(200).json({
      success: true,
      message: 'SHEBAODDS API is running',
      timestamp: new Date().toISOString()
    });
  }
);

/*
|--------------------------------------------------------------------------
| 404 HANDLER
|--------------------------------------------------------------------------
*/

app.use(
  (_req, res) => {
    res.status(404).json({
      success: false,
      message: 'Route not found'
    });
  }
);

/*
|--------------------------------------------------------------------------
| ERROR HANDLER
|--------------------------------------------------------------------------
*/

app.use(
  (
    error: any,
    _req: express.Request,
    res: express.Response,
    _next: express.NextFunction
  ) => {
    console.error(
      '❌ Express Error:',
      error
    );

    res.status(
      error?.status || 500
    ).json({
      success: false,
      message:
        error?.message ||
        'Internal server error'
    });
  }
);

/*
|--------------------------------------------------------------------------
| MONGODB STATE
|--------------------------------------------------------------------------
*/

import mongoose from 'mongoose';

function mongooseConnectionState(): boolean {
  return (
    mongoose.connection.readyState === 1
  );
}

/*
|--------------------------------------------------------------------------
| START SERVER
|--------------------------------------------------------------------------
*/

async function startServer(): Promise<void> {
  try {
    console.log('');
    console.log(
      '🦁 SHEBAODDS SERVER INITIALIZATION'
    );
    console.log(
      '============================================'
    );

    /*
     * Bootstrap:
     *
     * 1. Environment validation
     * 2. MongoDB connection
     */

    await bootstrapAppEngine();

    /*
     * Start HTTP server
     */

    app.listen(
      PORT,
      '0.0.0.0',
      () => {
        console.log('');
        console.log(
          '============================================'
        );

        console.log(
          '🦁 SHEBAODDS SERVER ONLINE'
        );

        console.log(
          '============================================'
        );

        console.log(
          `🚀 Port: ${PORT}`
        );

        console.log(
          `🌍 Environment: ${
            process.env.NODE_ENV ||
            'development'
          }`
        );

        console.log(
          `💾 MongoDB: ${
            mongooseConnectionState()
              ? 'CONNECTED'
              : 'DISCONNECTED'
          }`
        );

        console.log(
          '============================================'
        );

        console.log('');
      }
    );
  } catch (error: any) {
    console.error('');
    console.error(
      '============================================'
    );

    console.error(
      '💥 SERVER STARTUP FAILED'
    );

    console.error(
      '============================================'
    );

    console.error(
      error?.message || error
    );

    console.error('');

    process.exit(1);
  }
}

/*
|--------------------------------------------------------------------------
| START
|--------------------------------------------------------------------------
*/

startServer();

/*
|--------------------------------------------------------------------------
| EXPORT
|--------------------------------------------------------------------------
*/

export default app;