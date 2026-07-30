// ============================================
// SHEBAODDS - ENTERPRISE SERVER
// Smart Bets. Real Wins.
// ============================================

import express, { Application, Request, Response, NextFunction } from 'express';
import { createServer } from 'http';
import { Server as SocketServer } from 'socket.io';
import mongoose from 'mongoose';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import rateLimit from 'express-rate-limit';
import mongoSanitize from 'express-mongo-sanitize';
import hpp from 'hpp';
import xss from 'xss';
import { config } from 'dotenv';
import path from 'path';

// ---------- Import Routes ----------
import adminRoutes from './routes/adminRoutes';
import matchesRoutes from './routes/matchesRoutes';
import authRoutes from './routes/authRoutes';
import userRoutes from './routes/userRoutes';
import betRoutes from './routes/betRoutes';
import transactionRoutes from './routes/transactionRoutes';
import taxRoutes from './routes/taxRoutes';
import walletRoutes from './routes/walletRoutes';
import casinoRoutes from './routes/casinoRoutes';
import jackpotRoutes from './routes/jackpotRoutes';

// ---------- Import Middleware ----------
import { authenticate, isAdmin } from './middleware/authMiddleware';
import { validatePassword } from './middleware/passwordValidator';
import { errorHandler, notFoundHandler } from './middleware/errorHandler';
import { requestLogger } from './middleware/logger';

// ---------- Load Environment ----------
config();
const PORT = process.env.PORT || 3000;
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/shebaodds';

// ---------- Express App ----------
const app: Application = express();
const httpServer = createServer(app);

// ---------- Socket.IO ----------
const io = new SocketServer(httpServer, {
  cors: {
    origin: process.env.CLIENT_URL || '*',
    methods: ['GET', 'POST'],
    credentials: true
  }
});

// Make io accessible to routes via `req.io`
app.use((req: Request, res: Response, next: NextFunction) => {
  (req as any).io = io;
  next();
});

// ---------- Security Middleware ----------
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", "'unsafe-inline'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
}));

app.use(cors({
  origin: process.env.CLIENT_URL || '*',
  credentials: true
}));

// Rate Limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
});
app.use('/api', limiter);

// Stricter limiter for auth endpoints
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: 'Too many authentication attempts, please try again later.'
});
app.use('/api/auth', authLimiter);

// ---------- Standard Middleware ----------
app.use(compression());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));
app.use(mongoSanitize()); // Prevent NoSQL injection
app.use(hpp()); // Prevent HTTP parameter pollution
app.use(requestLogger); // Custom logging middleware

// ---------- Static Files (if any) ----------
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// ---------- Health Check ----------
app.get('/health', (req: Request, res: Response) => {
  res.status(200).json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    mongodb: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected'
  });
});

// ---------- API Routes ----------
app.use('/api/admin', adminRoutes);
app.use('/api/matches', matchesRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/bets', betRoutes);
app.use('/api/transactions', transactionRoutes);
app.use('/api/tax', taxRoutes);
app.use('/api/wallet', walletRoutes);
app.use('/api/casino', casinoRoutes);
app.use('/api/jackpot', jackpotRoutes);

// ---------- 404 Handler ----------
app.use(notFoundHandler);

// ---------- Global Error Handler ----------
app.use(errorHandler);

// ---------- Socket.IO Connection ----------
io.on('connection', (socket) => {
  console.log(`🔌 Client connected: ${socket.id}`);

  socket.on('join-room', (room) => {
    socket.join(room);
    console.log(`📦 Socket ${socket.id} joined room ${room}`);
  });

  socket.on('leave-room', (room) => {
    socket.leave(room);
    console.log(`📤 Socket ${socket.id} left room ${room}`);
  });

  socket.on('disconnect', () => {
    console.log(`🔌 Client disconnected: ${socket.id}`);
  });
});

// ---------- Database Connection ----------
async function connectDB() {
  try {
    await mongoose.connect(MONGODB_URI);
    console.log('✅ MongoDB connected successfully.');
  } catch (error) {
    console.error('❌ MongoDB connection error:', error);
    process.exit(1);
  }
}

// ---------- Start Server ----------
async function startServer() {
  await connectDB();

  httpServer.listen(PORT, () => {
    console.log(`🚀 ShebaOdds server running on port ${PORT}`);
    console.log(`📡 WebSocket server ready`);
    console.log(`🌐 Environment: ${process.env.NODE_ENV || 'development'}`);
  });
}

// ---------- Graceful Shutdown ----------
process.on('SIGTERM', () => {
  console.log('🛑 SIGTERM received, shutting down gracefully...');
  httpServer.close(() => {
    console.log('HTTP server closed.');
    mongoose.connection.close(false, () => {
      console.log('MongoDB connection closed.');
      process.exit(0);
    });
  });
});

process.on('SIGINT', () => {
  console.log('🛑 SIGINT received, shutting down gracefully...');
  httpServer.close(() => {
    console.log('HTTP server closed.');
    mongoose.connection.close(false, () => {
      console.log('MongoDB connection closed.');
      process.exit(0);
    });
  });
});

// ---------- Unhandled Rejection & Exception ----------
process.on('unhandledRejection', (err: Error) => {
  console.error('💥 Unhandled Rejection:', err.stack);
  // In production, you might want to restart the process
});

process.on('uncaughtException', (err: Error) => {
  console.error('💥 Uncaught Exception:', err.stack);
  // Graceful shutdown
  httpServer.close(() => {
    mongoose.connection.close(false, () => {
      process.exit(1);
    });
  });
});

// ---------- Start the server ----------
startServer();

export { app, io, httpServer };