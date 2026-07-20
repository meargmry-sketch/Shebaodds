// ============================================
// SHEBAODDS - ADMIN ROUTES
// Complete Admin Dashboard API
// SUPPORTS: Sportsbook & 51+ Casino Games
// ============================================

import express, { Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { authenticate, isAdmin, isSuperAdmin } from './authMiddleware';
import User from './User';
import Match from './Match';
import Bet, { BET_STATUS } from './Bet';
import { Transaction, TRANSACTION_TYPES, TRANSACTION_STATUS } from './Transaction';
import { TaxTransaction } from './Tax';
import { generateMonthlyTaxReport, submitTaxReport } from './taxService';
import { sendNotification } from './walletRoutes';

const router = express.Router();

// ==================== DASHBOARD STATISTICS ====================
router.get('/dashboard/stats', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);

    const weekStart = new Date();
    weekStart.setDate(weekStart.getDate() - weekStart.getDay());
    weekStart.setHours(0, 0, 0, 0);

    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);

    // ── User statistics ──
    const [totalUsers, activeUsers, newUsersToday, newUsersThisWeek, newUsersThisMonth] = await Promise.all([
      User.countDocuments(),
      User.countDocuments({ lastActive: { $gte: new Date(Date.now() - 5 * 60 * 1000) } }),
      User.countDocuments({ createdAt: { $gte: todayStart } }),
      User.countDocuments({ createdAt: { $gte: weekStart } }),
      User.countDocuments({ createdAt: { $gte: monthStart } })
    ]);

    // ── Sportsbook Bet statistics ──
    const [totalBets, todayBets, pendingBets, runningBets, totalWagered, todayWagered] = await Promise.all([
      Bet.countDocuments({ isCasinoBet: { $ne: true } }),
      Bet.countDocuments({ isCasinoBet: { $ne: true }, createdAt: { $gte: todayStart } }),
      Bet.countDocuments({ isCasinoBet: { $ne: true }, status: BET_STATUS.PENDING }),
      Bet.countDocuments({ isCasinoBet: { $ne: true }, status: BET_STATUS.RUNNING }),
      Bet.aggregate([{ $match: { isCasinoBet: { $ne: true } } }, { $group: { _id: null, total: { $sum: '$stake' } } }]),
      Bet.aggregate([
        { $match: { isCasinoBet: { $ne: true }, createdAt: { $gte: todayStart } } },
        { $group: { _id: null, total: { $sum: '$stake' } } }
      ])
    ]);

    // ── 🎰 Casino Bet statistics ──
    const [totalCasinoBets, todayCasinoBets, casinoWagered, todayCasinoWagered] = await Promise.all([
      Bet.countDocuments({ isCasinoBet: true }),
      Bet.countDocuments({ isCasinoBet: true, createdAt: { $gte: todayStart } }),
      Bet.aggregate([{ $match: { isCasinoBet: true } }, { $group: { _id: null, total: { $sum: '$stake' } } }]),
      Bet.aggregate([
        { $match: { isCasinoBet: true, createdAt: { $gte: todayStart } } },
        { $group: { _id: null, total: { $sum: '$stake' } } }
      ])
    ]);

    // ── Transaction statistics ──
    const [totalDeposits, todayDeposits, totalWithdrawals, todayWithdrawals, pendingWithdrawals] = await Promise.all([
      Transaction.aggregate([
        { $match: { type: TRANSACTION_TYPES.DEPOSIT, status: TRANSACTION_STATUS.COMPLETED } },
        { $group: { _id: null, total: { $sum: '$amount' } } }
      ]),
      Transaction.aggregate([
        { $match: { type: TRANSACTION_TYPES.DEPOSIT, status: TRANSACTION_STATUS.COMPLETED, createdAt: { $gte: todayStart } } },
        { $group: { _id: null, total: { $sum: '$amount' } } }
      ]),
      Transaction.aggregate([
        { $match: { type: TRANSACTION_TYPES.WITHDRAWAL, status: TRANSACTION_STATUS.COMPLETED } },
        { $group: { _id: null, total: { $sum: '$amount' } } }
      ]),
      Transaction.aggregate([
        { $match: { type: TRANSACTION_TYPES.WITHDRAWAL, status: TRANSACTION_STATUS.COMPLETED, createdAt: { $gte: todayStart } } },
        { $group: { _id: null, total: { $sum: '$amount' } } }
      ]),
      Transaction.countDocuments({ type: TRANSACTION_TYPES.WITHDRAWAL, status: TRANSACTION_STATUS.PENDING })
    ]);

    // ── Tax statistics ──
    const [totalTaxCollected, todayTaxCollected] = await Promise.all([
      TaxTransaction.aggregate([{ $group: { _id: null, total: { $sum: '$taxAmount' } } }]),
      TaxTransaction.aggregate([
        { $match: { deductedAt: { $gte: todayStart } } },
        { $group: { _id: null, total: { $sum: '$taxAmount' } } }
      ])
    ]);

    // ── Platform statistics ──
    const [totalBalance, totalBonusBalance] = await Promise.all([
      User.aggregate([{ $group: { _id: null, total: { $sum: '$wallet.balance' } } }]),
      User.aggregate([{ $group: { _id: null, total: { $sum: '$wallet.bonusBalance' } } }])
    ]);

    // ── Chart data (last 7 days) ──
    const chartData = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      date.setHours(0, 0, 0, 0);
      const nextDate = new Date(date);
      nextDate.setDate(nextDate.getDate() + 1);

      const [bets, deposits, withdrawals, newUsers, tax] = await Promise.all([
        Bet.countDocuments({ createdAt: { $gte: date, $lt: nextDate } }),
        Transaction.aggregate([
          { $match: { type: TRANSACTION_TYPES.DEPOSIT, status: TRANSACTION_STATUS.COMPLETED, createdAt: { $gte: date, $lt: nextDate } } },
          { $group: { _id: null, total: { $sum: '$amount' } } }
        ]),
        Transaction.aggregate([
          { $match: { type: TRANSACTION_TYPES.WITHDRAWAL, status: TRANSACTION_STATUS.COMPLETED, createdAt: { $gte: date, $lt: nextDate } } },
          { $group: { _id: null, total: { $sum: '$amount' } } }
        ]),
        User.countDocuments({ createdAt: { $gte: date, $lt: nextDate } }),
        TaxTransaction.aggregate([
          { $match: { deductedAt: { $gte: date, $lt: nextDate } } },
          { $group: { _id: null, total: { $sum: '$taxAmount' } } }
        ])
      ]);

      chartData.push({
        date: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        bets,
        deposits: deposits[0]?.total || 0,
        withdrawals: withdrawals[0]?.total || 0,
        newUsers,
        tax: tax[0]?.total || 0
      });
    }

    // ── Recent transactions (sports + casino) ──
    const recentTransactions = await Transaction.find()
      .populate('userId', 'username email')
      .sort({ createdAt: -1 })
      .limit(10);

    return res.json({
      success: true,
      users: {
        total: totalUsers,
        active: activeUsers,
        newToday: newUsersToday,
        newThisWeek: newUsersThisWeek,
        newThisMonth: newUsersThisMonth
      },
      sportsbook: {
        total: totalBets,
        today: todayBets,
        pending: pendingBets,
        running: runningBets,
        totalWagered: totalWagered[0]?.total || 0,
        todayWagered: todayWagered[0]?.total || 0
      },
      casino: {
        total: totalCasinoBets,
        today: todayCasinoBets,
        totalWagered: casinoWagered[0]?.total || 0,
        todayWagered: todayCasinoWagered[0]?.total || 0
      },
      finances: {
        totalDeposits: totalDeposits[0]?.total || 0,
        todayDeposits: todayDeposits[0]?.total || 0,
        totalWithdrawals: totalWithdrawals[0]?.total || 0,
        todayWithdrawals: todayWithdrawals[0]?.total || 0,
        pendingWithdrawals,
        totalBalance: totalBalance[0]?.total || 0,
        totalBonusBalance: totalBonusBalance[0]?.total || 0
      },
      tax: {
        totalCollected: totalTaxCollected[0]?.total || 0,
        todayCollected: todayTaxCollected[0]?.total || 0,
        rate: Math.round(parseFloat(process.env.TAX_RATE || '0.15') * 100),
        freeLimit: parseInt(process.env.TAX_FREE_LIMIT || '100', 10)
      },
      chartData,
      recentTransactions
    });

  } catch (error: any) {
    console.error('Dashboard stats error:', error);
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET ALL USERS ====================
router.get('/users', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const { 
      search, 
      status, 
      vipLevel,
      kycStatus,
      limit = '50', 
      page = '1',
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    const query: any = {};

    if (search) {
      query.$or = [
        { username: { $regex: search as string, $options: 'i' } },
        { email: { $regex: search as string, $options: 'i' } },
        { phone: { $regex: search as string, $options: 'i' } },
        { fullName: { $regex: search as string, $options: 'i' } }
      ];
    }

    if (status === 'active') query.isActive = true;
    if (status === 'blocked') query.isBlocked = true;
    if (status === 'suspended') query.isSuspended = true;
    if (vipLevel) query['vip.level'] = parseInt(vipLevel as string, 10);
    if (kycStatus) query.kycStatus = kycStatus;

    const limitNum = parseInt(limit as string, 10) || 50;
    const pageNum = parseInt(page as string, 10) || 1;
    const skip = (pageNum - 1) * limitNum;
    const sort = { [sortBy as string]: sortOrder === 'desc' ? -1 : 1 } as any;

    const [users, total] = await Promise.all([
      User.find(query)
        .select('-password -twoFactorSecret -twoFactorBackupCodes')
        .sort(sort)
        .skip(skip)
        .limit(limitNum),
      User.countDocuments(query)
    ]);

    return res.json({
      success: true,
      users,
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        pages: Math.ceil(total / limitNum)
      }
    });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET USER DETAILS ====================
router.get('/users/:userId', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const { userId } = req.params;

    const user = await User.findById(userId).select('-password -twoFactorSecret');
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    // Get user's recent bets (sports + casino)
    const recentBets = await Bet.find({ userId: user._id })
      .populate('matchId', 'homeTeam awayTeam league')
      .sort({ createdAt: -1 })
      .limit(20);

    // Get user's transactions
    const recentTransactions = await Transaction.find({ userId: user._id })
      .sort({ createdAt: -1 })
      .limit(20);

    // Get user's tax summary
    const taxSummary = await TaxTransaction.aggregate([
      { $match: { userId: user._id } },
      { 
        $group: { 
          _id: null, 
          totalGross: { $sum: '$grossWinning' }, 
          totalTax: { $sum: '$taxAmount' }, 
          totalNet: { $sum: '$netWinning' }, 
          count: { $sum: 1 } 
        } 
      }
    ]);

    // 🎰 Casino game stats for this user
    const casinoStats = await Bet.aggregate([
      { $match: { userId: user._id, isCasinoBet: true } },
      { 
        $group: { 
          _id: null, 
          totalCasinoBets: { $sum: 1 },
          totalCasinoStake: { $sum: '$stake' },
          totalCasinoWon: { $sum: '$actualWin' }
        } 
      }
    ]);

    return res.json({
      success: true,
      user,
      recentBets,
      recentTransactions,
      taxSummary: taxSummary[0] || { totalGross: 0, totalTax: 0, totalNet: 0, count: 0 },
      casinoStats: casinoStats[0] || { totalCasinoBets: 0, totalCasinoStake: 0, totalCasinoWon: 0 }
    });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== UPDATE USER ====================
router.put('/users/:userId', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { userId } = req.params;
    const { 
      balance, 
      bonusBalance, 
      isActive, 
      isBlocked, 
      isVerified, 
      vipLevel,
      kycStatus,
      note 
    } = req.body;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    // Update balance (with transaction)
    if (balance !== undefined && balance !== user.wallet.balance) {
      const difference = balance - user.wallet.balance;
      const previousBalance = user.wallet.balance;
      user.wallet.balance = balance;

      const transaction = new Transaction({
        userId: user._id,
        type: TRANSACTION_TYPES.ADJUSTMENT,
        amount: Math.abs(difference),
        netAmount: Math.abs(difference),
        previousBalance,
        newBalance: balance,
        status: TRANSACTION_STATUS.COMPLETED,
        processedBy: req.user._id,
        processedAt: new Date(),
        notes: `Admin balance adjustment by ${req.user.username}`,
        metadata: { adminId: req.user._id, adminName: req.user.username }
      });
      await transaction.save();
    }

    if (bonusBalance !== undefined) user.wallet.bonusBalance = bonusBalance;
    if (isActive !== undefined) user.isActive = isActive;
    if (isBlocked !== undefined) user.isBlocked = isBlocked;
    if (isVerified !== undefined) user.isVerified = isVerified;
    if (vipLevel !== undefined) {
      user.vip.level = vipLevel;
      if (typeof user.updateVipLevel === 'function') {
        user.updateVipLevel();
      }
    }
    if (kycStatus !== undefined) user.kycStatus = kycStatus;
    if (note) {
      user.notes.push({
        note,
        createdBy: req.user._id,
        createdAt: new Date()
      });
    }

    await user.save();

    // Send notification to user
    await sendNotification({
      userId: user._id,
      title: 'Account Updated',
      message: 'Your account has been updated by an administrator.',
      type: 'system',
      data: { admin: req.user.username }
    });

    return res.json({ success: true, user: user.toJSON() });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== TOGGLE USER BLOCK ====================
router.post('/users/:userId/toggle-block', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { userId } = req.params;
    const { reason } = req.body;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.isBlocked = !user.isBlocked;
    if (user.isBlocked) {
      user.isActive = false;
      user.suspensionReason = reason || 'Blocked by administrator';
      user.suspensionEndDate = null;
    } else {
      user.isActive = true;
      user.suspensionReason = null;
    }

    await user.save();

    await sendNotification({
      userId: user._id,
      title: user.isBlocked ? 'Account Blocked' : 'Account Unblocked',
      message: user.isBlocked 
        ? `Your account has been blocked. Reason: ${reason || 'Violation of terms'}. Please contact support.`
        : 'Your account has been unblocked. You can now login and bet again.',
      type: 'security'
    });

    return res.json({ success: true, isBlocked: user.isBlocked });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET ALL TRANSACTIONS ====================
router.get('/transactions', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const { 
      type, 
      status, 
      paymentMethod,
      userId,
      from,
      to,
      limit = '50', 
      page = '1' 
    } = req.query;

    const query: any = {};

    if (type) query.type = type;
    if (status) query.status = status;
    if (paymentMethod) query.paymentMethod = paymentMethod;
    if (userId) query.userId = new mongoose.Types.ObjectId(userId as string);
    if (from || to) {
      query.createdAt = {};
      if (from) query.createdAt.$gte = new Date(from as string);
      if (to) query.createdAt.$lte = new Date(to as string);
    }

    const limitNum = parseInt(limit as string, 10) || 50;
    const pageNum = parseInt(page as string, 10) || 1;
    const skip = (pageNum - 1) * limitNum;

    const [transactions, total] = await Promise.all([
      Transaction.find(query)
        .populate('userId', 'username email phone fullName')
        .populate('processedBy', 'username')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limitNum),
      Transaction.countDocuments(query)
    ]);

    // Get summary
    const summary = await Transaction.aggregate([
      { $match: query },
      { 
        $group: {
          _id: null,
          totalAmount: { $sum: '$amount' },
          totalDeposits: { $sum: { $cond: [{ $eq: ['$type', TRANSACTION_TYPES.DEPOSIT] }, '$amount', 0] } },
          totalWithdrawals: { $sum: { $cond: [{ $eq: ['$type', TRANSACTION_TYPES.WITHDRAWAL] }, '$amount', 0] } },
          pendingCount: { $sum: { $cond: [{ $eq: ['$status', TRANSACTION_STATUS.PENDING] }, 1, 0] } },
          completedCount: { $sum: { $cond: [{ $eq: ['$status', TRANSACTION_STATUS.COMPLETED] }, 1, 0] } }
        }
      }
    ]);

    return res.json({
      success: true,
      transactions,
      summary: summary[0] || { totalAmount: 0, totalDeposits: 0, totalWithdrawals: 0, pendingCount: 0, completedCount: 0 },
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        pages: Math.ceil(total / limitNum)
      }
    });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== APPROVE TRANSACTION ====================
router.post('/transactions/:id/approve', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { id } = req.params;

    const transaction = await Transaction.findById(id);
    if (!transaction) {
      return res.status(404).json({ success: false, message: 'Transaction not found' });
    }

    if (transaction.status !== TRANSACTION_STATUS.PENDING) {
      return res.status(400).json({ success: false, message: 'Transaction already processed' });
    }

    const user = await User.findById(transaction.userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (transaction.type === TRANSACTION_TYPES.WITHDRAWAL) {
      if (user.wallet.lockedBalance !== undefined) {
        user.wallet.lockedBalance -= transaction.amount;
      }
      user.wallet.totalWithdrawn += transaction.amount;
      transaction.status = TRANSACTION_STATUS.COMPLETED;
      transaction.completedAt = new Date();

      await sendNotification({
        userId: user._id,
        title: 'Withdrawal Approved ✅',
        message: `Your withdrawal of ${transaction.amount.toLocaleString()} ETB has been approved and processed.`,
        type: 'withdrawal',
        data: { amount: transaction.amount, reference: transaction.paymentReference }
      });

    } else if (transaction.type === TRANSACTION_TYPES.DEPOSIT) {
      user.wallet.balance += transaction.amount;
      user.wallet.totalDeposited += transaction.amount;
      transaction.status = TRANSACTION_STATUS.COMPLETED;
      transaction.completedAt = new Date();

      await sendNotification({
        userId: user._id,
        title: 'Deposit Approved ✅',
        message: `Your deposit of ${transaction.amount.toLocaleString()} ETB has been approved and added to your wallet.`,
        type: 'deposit',
        data: { amount: transaction.amount, newBalance: user.wallet.balance }
      });
    }

    transaction.processedBy = req.user._id;
    transaction.processedAt = new Date();

    await Promise.all([user.save(), transaction.save()]);

    req.io?.to(`user_${user._id}`).emit('wallet_update', { balance: user.wallet.balance });

    return res.json({ success: true, message: 'Transaction approved', newBalance: user.wallet.balance });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== REJECT TRANSACTION ====================
router.post('/transactions/:id/reject', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const transaction = await Transaction.findById(id);
    if (!transaction) {
      return res.status(404).json({ success: false, message: 'Transaction not found' });
    }

    if (transaction.status !== TRANSACTION_STATUS.PENDING) {
      return res.status(400).json({ success: false, message: 'Transaction already processed' });
    }

    const user = await User.findById(transaction.userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (transaction.type === TRANSACTION_TYPES.WITHDRAWAL) {
      user.wallet.balance += transaction.amount;
      if (user.wallet.lockedBalance !== undefined) {
        user.wallet.lockedBalance -= transaction.amount;
      }
      await user.save();

      await sendNotification({
        userId: user._id,
        title: 'Withdrawal Rejected ❌',
        message: `Your withdrawal of ${transaction.amount.toLocaleString()} ETB was rejected. Reason: ${reason || 'Please contact support for details'}.`,
        type: 'withdrawal'
      });
    }

    transaction.status = TRANSACTION_STATUS.FAILED;
    transaction.failureReason = reason || 'Rejected by administrator';
    transaction.processedBy = req.user._id;
    transaction.processedAt = new Date();

    await transaction.save();

    req.io?.to(`user_${user._id}`).emit('wallet_update', { balance: user.wallet.balance });

    return res.json({ success: true, message: 'Transaction rejected' });

  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ================================================================
// 🎰 NEW: CASINO STATISTICS ENDPOINT
// ================================================================

router.get('/casino/stats', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);

    // Total casino bets
    const totalCasinoBets = await Bet.countDocuments({ isCasinoBet: true });
    const todayCasinoBets = await Bet.countDocuments({ isCasinoBet: true, createdAt: { $gte: todayStart } });

    // Total casino wagered
    const casinoWageredAgg = await Bet.aggregate([
      { $match: { isCasinoBet: true } },
      { $group: { _id: null, total: { $sum: '$stake' } } }
    ]);
    const todayCasinoWageredAgg = await Bet.aggregate([
      { $match: { isCasinoBet: true, createdAt: { $gte: todayStart } } },
      { $group: { _id: null, total: { $sum: '$stake' } } }
    ]);

    // Top 5 casino games by volume
    const topGames = await Bet.aggregate([
      { $match: { isCasinoBet: true } },
      { 
        $group: { 
          _id: '$casinoGameId', 
          count: { $sum: 1 },
          totalStake: { $sum: '$stake' },
          totalPayout: { $sum: '$actualWin' }
        } 
      },
      { $sort: { count: -1 } },
      { $limit: 5 }
    ]);

    return res.json({
      success: true,
      totalBets: totalCasinoBets,
      todayBets: todayCasinoBets,
      totalWagered: casinoWageredAgg[0]?.total || 0,
      todayWagered: todayCasinoWageredAgg[0]?.total || 0,
      topGames
    });

  } catch (error: any) {
    console.error('Casino stats error:', error);
    return res.status(500).json({ success: false, message: error.message });
  }
});

export default router;