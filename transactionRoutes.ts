// ============================================
// SHEBAODDS - TRANSACTION ROUTES
// Lookup, summary, and admin listing.
// Day-to-day deposit/withdrawal flows live in walletRoutes.ts —
// this file covers cross-cutting transaction queries.
// ============================================

import express, { Request, Response, Router } from 'express';
import { authenticate, isAdmin } from './authMiddleware';
import { Transaction, TRANSACTION_STATUS } from './Transaction';

const router: Router = express.Router();

// ==================== MY SUMMARY (by type, current period) ====================
router.get('/summary/me', authenticate, async (req: any, res: Response) => {
  try {
    const summary = await Transaction.aggregate([
      { $match: { userId: req.user._id, status: TRANSACTION_STATUS.COMPLETED } },
      { $group: { _id: '$type', total: { $sum: '$netAmount' }, count: { $sum: 1 } } }
    ]);
    res.json({ success: true, summary });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load transaction summary', error: error.message });
  }
});

// ==================== SINGLE TRANSACTION (own) ====================
router.get('/:id', authenticate, async (req: any, res: Response) => {
  try {
    const transaction = await Transaction.findOne({ _id: req.params.id, userId: req.user._id });
    if (!transaction) {
      return res.status(404).json({ success: false, message: 'Transaction not found' });
    }
    res.json({ success: true, transaction });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load transaction', error: error.message });
  }
});

// ==================== ADMIN: SEARCH ACROSS ALL USERS ====================
router.get('/', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const { userId, type, status, limit = '50', page = '1' } = req.query as any;
    const query: any = {};
    if (userId) query.userId = userId;
    if (type) query.type = type;
    if (status) query.status = status;

    const limitNum = parseInt(limit, 10) || 50;
    const pageNum = parseInt(page, 10) || 1;
    const skip = (pageNum - 1) * limitNum;

    const [transactions, total] = await Promise.all([
      Transaction.find(query).sort({ createdAt: -1 }).skip(skip).limit(limitNum),
      Transaction.countDocuments(query)
    ]);

    res.json({ success: true, transactions, total, page: pageNum, pages: Math.ceil(total / limitNum) });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to search transactions', error: error.message });
  }
});

export default router;
