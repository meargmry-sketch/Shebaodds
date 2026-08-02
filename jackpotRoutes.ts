// ============================================
// SHEBAODDS - JACKPOT ROUTES
// Sports (12-match) & Casino Jackpot Pools
// ============================================

import express, { Request, Response, Router } from 'express';
import { authenticate, isAdmin } from './authMiddleware';
import User from './User';
import { Transaction, TRANSACTION_TYPES, TRANSACTION_STATUS } from './Transaction';
import { JackpotPool, JackpotTicket } from './jackpotSchema';
import { evaluateSportsJackpot, evaluateCasinoJackpot } from './jackpotEvaluator';

const router: Router = express.Router();

// ==================== LIST OPEN POOLS ====================
router.get('/pools', async (req: Request, res: Response) => {
  try {
    const { type, status } = req.query as any;
    const query: any = {};
    if (type) query.type = type;
    query.status = status || 'Open';

    const pools = await JackpotPool.find(query).sort({ createdAt: -1 });
    res.json({ success: true, pools });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load jackpot pools', error: error.message });
  }
});

// ==================== POOL DETAILS ====================
router.get('/pools/:poolId', async (req: Request, res: Response) => {
  try {
    const pool = await JackpotPool.findById(req.params.poolId);
    if (!pool) {
      return res.status(404).json({ success: false, message: 'Jackpot pool not found' });
    }
    const ticketCount = await JackpotTicket.countDocuments({ jackpotPoolId: pool._id });
    res.json({ success: true, pool, ticketCount });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load jackpot pool', error: error.message });
  }
});

// ==================== CREATE POOL (Admin) ====================
router.post('/pools', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const { title, type, matchIds, casinoGameId, criteria, grandPrize, entryFee } = req.body;

    const pool = new JackpotPool({
      title,
      type,
      matchIds,
      casinoGameId,
      criteria,
      grandPrize,
      entryFee,
      status: 'Open'
    });
    await pool.save();

    res.status(201).json({ success: true, pool });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to create jackpot pool', error: error.message });
  }
});

// ==================== LOCK POOL (Admin) ====================
router.post('/pools/:poolId/lock', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const pool = await JackpotPool.findById(req.params.poolId);
    if (!pool) {
      return res.status(404).json({ success: false, message: 'Jackpot pool not found' });
    }
    if (pool.status !== 'Open') {
      return res.status(400).json({ success: false, message: `Pool cannot be locked from status: ${pool.status}` });
    }
    pool.status = 'Locked';
    await pool.save();
    res.json({ success: true, pool });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to lock jackpot pool', error: error.message });
  }
});

// ==================== SETTLE / EVALUATE POOL (Admin) ====================
router.post('/pools/:poolId/settle', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const pool = await JackpotPool.findById(req.params.poolId);
    if (!pool) {
      return res.status(404).json({ success: false, message: 'Jackpot pool not found' });
    }

    if (pool.type === 'sports') {
      const { actualOutcomes } = req.body as { actualOutcomes: string[] };
      await evaluateSportsJackpot(String(pool._id), actualOutcomes);
    } else {
      const { playerStats } = req.body as { playerStats?: Array<{ userId: string; multiplier: number; totalWon: number }> };
      await evaluateCasinoJackpot(String(pool._id), playerStats);
    }

    const settledPool = await JackpotPool.findById(req.params.poolId);
    res.json({ success: true, pool: settledPool });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to settle jackpot pool', error: error.message });
  }
});

// ==================== ENTER POOL (BUY TICKET) ====================
router.post('/pools/:poolId/enter', authenticate, async (req: any, res: Response) => {
  try {
    const pool = await JackpotPool.findById(req.params.poolId);
    if (!pool) {
      return res.status(404).json({ success: false, message: 'Jackpot pool not found' });
    }
    if (pool.status !== 'Open') {
      return res.status(400).json({ success: false, message: 'This jackpot pool is not open for entries' });
    }

    const { predictions } = req.body as { predictions?: string[] };
    if (pool.type === 'sports' && (!predictions || predictions.length !== 12)) {
      return res.status(400).json({ success: false, message: 'Exactly 12 predictions are required for a sports jackpot' });
    }

    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    if (user.wallet.balance < pool.entryFee) {
      return res.status(400).json({ success: false, message: 'Insufficient balance for the entry fee' });
    }

    const previousBalance = user.wallet.balance;
    user.wallet.balance -= pool.entryFee;
    await user.save();

    const ticket = new JackpotTicket({
      jackpotPoolId: pool._id,
      userId: String(user._id),
      predictions
    });
    await ticket.save();

    const transaction = new Transaction({
      userId: user._id,
      type: TRANSACTION_TYPES.BET_PLACE,
      amount: pool.entryFee,
      netAmount: pool.entryFee,
      previousBalance,
      newBalance: user.wallet.balance,
      status: TRANSACTION_STATUS.COMPLETED,
      completedAt: new Date(),
      metadata: { jackpotPoolId: pool._id, ticketId: ticket._id }
    });
    await transaction.save();

    req.io?.to(`user_${user._id}`).emit('wallet_update', { balance: user.wallet.balance });

    res.status(201).json({ success: true, ticket, balance: user.wallet.balance });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to enter jackpot pool', error: error.message });
  }
});

// ==================== MY TICKETS ====================
router.get('/my-tickets', authenticate, async (req: any, res: Response) => {
  try {
    const tickets = await JackpotTicket.find({ userId: String(req.user._id) }).sort({ createdAt: -1 });
    res.json({ success: true, tickets });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load tickets', error: error.message });
  }
});

export default router;
