import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import * as dotenv from 'dotenv';
import { bootstrapAppEngine } from './serverBootstrap';
import gatewayRouter from './expressApiGateway';
import authRouter from './authRoutes';
import walletRouter from './walletRoutes';
import bettingRouter from './bettingRoutes';
import matchesRouter from './matchesRoutes';
import adminRouter from './adminRoutes';
import biometricRouter from './biometricRoutes';

// Load environment variables
dotenv.config();

const app = express();
const port = process.env.PORT || 5000;

// Security & Optimization Middleware Matrix
app.use(helmet());
app.use(cors({
  origin: process.env.CORS_ORIGINS ? process.env.CORS_ORIGINS.split(',') : '*'
}));
app.use(compression());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Live HTTP request logging stream
app.use((req, res, next) => {
  console.log(`[HTTP] ${req.method} ${req.url} - IP: ${req.ip} - User-Agent: ${req.headers['user-agent']}`);
  next();
});

// Root API Healthcheck & System Metadata
app.get('/', (req, res) => {
  res.json({
    name: 'ShebaOdds Enterprise Platform Service',
    version: '2.0.0',
    status: 'Operational',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
});

// Serve Amharic translations dynamically to React frontend
app.get('/locales/am.json', (req, res) => {
  try {
    const fs = require('fs');
    const path = require('path');
    const localesPath = path.join(__dirname, 'locales', 'am.json');
    if (fs.existsSync(localesPath)) {
      const data = fs.readFileSync(localesPath, 'utf8');
      return res.json(JSON.parse(data));
    }
    // Fallback if file doesn't exist
    return res.status(404).json({ success: false, message: 'Translations not found' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// Register Unified Gateway Router under standard API versioning endpoint
const apiVersion = process.env.API_VERSION || 'v2';
app.use(`/api/${apiVersion}`, gatewayRouter);
app.use(`/api/${apiVersion}/auth`, authRouter);
app.use(`/api/${apiVersion}/wallet`, walletRouter);
app.use(`/api/${apiVersion}/bets`, bettingRouter);
app.use(`/api/${apiVersion}/matches`, matchesRouter);
app.use(`/api/${apiVersion}/admin`, adminRouter);
app.use(`/api/${apiVersion}/biometric`, biometricRouter);

// Global Unhandled Error Handling Pipeline
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
  console.error('💥 [GLOBAL UNHANDLED EXCEPTION]:', err);
  res.status(500).json({
    success: false,
    error: 'Internal Server Error',
    message: err.message || 'An unexpected error occurred inside the gateway routing pipeline.'
  });
});

/**
 * Initializes all database pools, validates environments, and starts the HTTP listener
 */
async function startServer() {
  try {
    // Run core engine bootstrap validation (MongoDB replica set check, etc.)
    await bootstrapAppEngine();

    app.listen(port, () => {
      console.log(`======================================================================`);
      console.log(`⚡ [SERVER SUCCESS] ShebaOdds Enterprise API Gateway started on port ${port}`);
      console.log(`🔗 API Gateway Endpoints available under /api/${apiVersion}`);
      console.log(`======================================================================`);
    });
  } catch (err: any) {
    console.error('💥 [SERVER FATAL STARTUP FAILURE]:', err.message || err);
    process.exit(1);
  }
}

startServer();
