// ============================================
// SHEBAODDS - ADMIN TRANSACTION ROUTES
// Mock Deposit Approval / Rejection
// ============================================

import express, {
  Request,
  Response,
  Router
} from 'express';

import mongoose from 'mongoose';

import {
  authenticate
} from './authRoutes';

import User from './User';

import {
  Transaction,
  TRANSACTION_TYPES,
  TRANSACTION_STATUS
} from './Transaction';

const router: Router =
  express.Router();

// ==================== ADMIN AUTH ====================

function requireAdmin(
  req: Request,
  res: Response,
  next: Function
) {

  const user =
    (req as any).user;

  if (!user) {

    return res.status(401).json({

      success: false,

      message:
        'Authentication required'
    });
  }

  const role =
    String(
      user.role || ''
    ).toLowerCase();

  const isAdmin =
    role === 'admin' ||
    role === 'superadmin' ||
    role === 'super_admin';

  if (!isAdmin) {

    return res.status(403).json({

      success: false,

      message:
        'Administrator access required'
    });
  }

  next();
}

// ==================== WALLET INITIALIZER ====================

function ensureWallet(
  user: any
) {

  if (!user.wallet) {

    user.wallet = {

      balance: 0,

      bonusBalance: 0,

      lockedBalance: 0,

      totalDeposited: 0,

      totalWithdrawn: 0,

      totalWagered: 0,

      totalWon: 0,

      totalTaxPaid: 0,

      currency: 'ETB'
    };
  }

  return user.wallet;
}

// ==================== NOTIFICATION ====================

async function sendNotification(
  userId: any,
  title: string,
  message: string,
  type: string,
  data?: any
) {

  console.log(
    `[Admin Notification] ${userId}`
  );

  console.log(
    `[${title}] ${message}`
  );

  return {
    success: true
  };
}

// ==================== GET PENDING TRANSACTIONS ====================

router.get(
  '/transactions/pending',
  authenticate,
  requireAdmin,
  async (
    req: Request,
    res: Response
  ) => {

    try {

      const {
        type = TRANSACTION_TYPES.DEPOSIT,
        limit = '50',
        page = '1'
      } = req.query as any;

      const limitNum =
        Math.min(
          Math.max(
            parseInt(limit, 10) || 50,
            1
          ),
          100
        );

      const pageNum =
        Math.max(
          parseInt(page, 10) || 1,
          1
        );

      const query: any = {

        status:
          TRANSACTION_STATUS.PENDING,

        requiresApproval:
          true
      };

      if (type) {
        query.type = type;
      }

      const skip =
        (pageNum - 1) *
        limitNum;

      const [
        transactions,
        total
      ] = await Promise.all([

        Transaction.find(query)

          .populate(
            'userId',
            'username email fullName phone'
          )

          .sort({
            createdAt: 1
          })

          .skip(skip)

          .limit(limitNum)

          .lean(),

        Transaction.countDocuments(
          query
        )

      ]);

      return res.json({

        success: true,

        transactions,

        pagination: {

          total,

          page: pageNum,

          limit: limitNum,

          pages:
            Math.ceil(
              total / limitNum
            )
        }
      });

    } catch (error: any) {

      console.error(
        'Pending transaction error:',
        error
      );

      return res.status(500).json({

        success: false,

        message:
          error.message
      });
    }
  }
);

// ==================== GET SINGLE TRANSACTION ====================

router.get(
  '/transactions/:id',
  authenticate,
  requireAdmin,
  async (
    req: Request,
    res: Response
  ) => {

    try {

      const {
        id
      } = req.params;

      if (
        !mongoose.Types.ObjectId.isValid(
          id
        )
      ) {

        return res.status(400).json({

          success: false,

          message:
            'Invalid transaction ID'
        });
      }

      const transaction =
        await Transaction.findById(id)
          .populate(
            'userId',
            'username email fullName phone wallet'
          )
          .populate(
            'approvedBy',
            'username email fullName'
          );

      if (!transaction) {

        return res.status(404).json({

          success: false,

          message:
            'Transaction not found'
        });
      }

      return res.json({

        success: true,

        transaction
      });

    } catch (error: any) {

      console.error(
        'Transaction lookup error:',
        error
      );

      return res.status(500).json({

        success: false,

        message:
          error.message
      });
    }
  }
);

// ==================== APPROVE DEPOSIT ====================

router.post(
  '/transactions/:id/approve',
  authenticate,
  requireAdmin,
  async (
    req: Request,
    res: Response
  ) => {

    try {

      const admin =
        (req as any).user;

      const {
        id
      } = req.params;

      if (
        !mongoose.Types.ObjectId.isValid(
          id
        )
      ) {

        return res.status(400).json({

          success: false,

          message:
            'Invalid transaction ID'
        });
      }

      // -----------------------------
      // Find transaction
      // -----------------------------

      const transaction =
        await Transaction.findById(
          id
        );

      if (!transaction) {

        return res.status(404).json({

          success: false,

          message:
            'Transaction not found'
        });
      }

      // -----------------------------
      // Only deposits can use this
      // -----------------------------

      if (
        transaction.type !==
        TRANSACTION_TYPES.DEPOSIT
      ) {

        return res.status(400).json({

          success: false,

          message:
            'Only deposit transactions can be approved by this endpoint'
        });
      }

      // -----------------------------
      // Prevent double approval
      // -----------------------------

      if (
        transaction.status ===
        TRANSACTION_STATUS.COMPLETED
      ) {

        return res.status(409).json({

          success: false,

          message:
            'This deposit has already been approved',

          transactionId:
            transaction._id
        });
      }

      if (
        transaction.status !==
        TRANSACTION_STATUS.PENDING
      ) {

        return res.status(400).json({

          success: false,

          message:
            `Transaction cannot be approved because its status is ${transaction.status}`
        });
      }

      // -----------------------------
      // Find user
      // -----------------------------

      const user =
        await User.findById(
          transaction.userId
        );

      if (!user) {

        return res.status(404).json({

          success: false,

          message:
            'User associated with transaction was not found'
        });
      }

      // -----------------------------
      // Wallet
      // -----------------------------

      const wallet =
        ensureWallet(user);

      const depositAmount =
        transaction.amount;

      const previousBalance =
        wallet.balance || 0;

      const previousBonusBalance =
        wallet.bonusBalance || 0;

      // -----------------------------
      // First deposit check
      // -----------------------------

      const previousCompletedDeposits =
        await Transaction.countDocuments({

          userId:
            transaction.userId,

          type:
            TRANSACTION_TYPES.DEPOSIT,

          status:
            TRANSACTION_STATUS.COMPLETED,

          _id: {
            $ne:
              transaction._id
          }
        });

      const isFirstDeposit =
        previousCompletedDeposits === 0;

      // -----------------------------
      // Deposit bonus
      // -----------------------------

      let bonusAmount = 0;

      if (isFirstDeposit) {

        const bonusPercentage =
          parseInt(
            process.env.DEPOSIT_BONUS_PERCENTAGE ||
            '50',
            10
          ) || 50;

        const maxBonus =
          parseInt(
            process.env.DEPOSIT_BONUS_MAX ||
            '500',
            10
          ) || 500;

        bonusAmount =
          Math.min(
            (
              depositAmount *
              bonusPercentage
            ) / 100,
            maxBonus
          );
      }

      // -----------------------------
      // Calculate balances
      // -----------------------------

      const newBalance =
        previousBalance +
        depositAmount;

      const newBonusBalance =
        previousBonusBalance +
        bonusAmount;

      // -----------------------------
      // Update user wallet
      // -----------------------------

      wallet.balance =
        newBalance;

      wallet.bonusBalance =
        newBonusBalance;

      wallet.totalDeposited =
        (wallet.totalDeposited || 0) +
        depositAmount;

      // -----------------------------
      // Update transaction
      // -----------------------------

      transaction.previousBalance =
        previousBalance;

      transaction.previousBonusBalance =
        previousBonusBalance;

      transaction.newBalance =
        newBalance;

      transaction.newBonusBalance =
        newBonusBalance;

      transaction.status =
        TRANSACTION_STATUS.COMPLETED;

      transaction.requiresApproval =
        false;

      transaction.approvedBy =
        admin._id;

      transaction.approvedAt =
        new Date();

      transaction.processedBy =
        admin._id;

      transaction.processedAt =
        new Date();

      transaction.completedAt =
        new Date();

      transaction.updatedAt =
        new Date();

      transaction.metadata = {

        ...(transaction.metadata || {}),

        mockPayment: true,

        adminApproved: true,

        approvedAt:
          new Date(),

        bonusAmount
      };

      // -----------------------------
      // Save both
      // -----------------------------

      await user.save();

      await transaction.save();

      // -----------------------------
      // Notify user
      // -----------------------------

      await sendNotification(

        user._id,

        'Deposit Approved 💰',

        `${depositAmount.toLocaleString()} ETB has been added to your wallet.`,

        'deposit_approved',

        {

          transactionId:
            transaction._id,

          amount:
            depositAmount,

          bonusAmount,

          newBalance
        }
      );

      // -----------------------------
      // Bonus notification
      // -----------------------------

      if (bonusAmount > 0) {

        await sendNotification(

          user._id,

          'First Deposit Bonus 🎁',

          `You received ${bonusAmount.toLocaleString()} ETB bonus.`,

          'bonus',

          {

            bonusAmount,

            transactionId:
              transaction._id
          }
        );
      }

      return res.json({

        success: true,

        message:
          'Deposit approved successfully',

        transactionId:
          transaction._id,

        amount:
          depositAmount,

        bonusAmount,

        previousBalance,

        newBalance,

        previousBonusBalance,

        newBonusBalance,

        status:
          transaction.status,

        approvedBy:
          admin._id,

        approvedAt:
          transaction.approvedAt
      });

    } catch (error: any) {

      console.error(
        'Approve deposit error:',
        error
      );

      return res.status(500).json({

        success: false,

        message:
          error.message ||
          'Failed to approve deposit'
      });
    }
  }
);

// ==================== REJECT DEPOSIT ====================

router.post(
  '/transactions/:id/reject',
  authenticate,
  requireAdmin,
  async (
    req: Request,
    res: Response
  ) => {

    try {

      const admin =
        (req as any).user;

      const {
        id
      } = req.params;

      const {
        reason
      } = req.body;

      if (
        !mongoose.Types.ObjectId.isValid(
          id
        )
      ) {

        return res.status(400).json({

          success: false,

          message:
            'Invalid transaction ID'
        });
      }

      const transaction =
        await Transaction.findById(
          id
        );

      if (!transaction) {

        return res.status(404).json({

          success: false,

          message:
            'Transaction not found'
        });
      }

      if (
        transaction.type !==
        TRANSACTION_TYPES.DEPOSIT
      ) {

        return res.status(400).json({

          success: false,

          message:
            'Only deposits can be rejected by this endpoint'
        });
      }

      if (
        transaction.status ===
        TRANSACTION_STATUS.COMPLETED
      ) {

        return res.status(409).json({

          success: false,

          message:
            'A completed deposit cannot be rejected'
        });
      }

      if (
        transaction.status !==
        TRANSACTION_STATUS.PENDING
      ) {

        return res.status(400).json({

          success: false,

          message:
            `Transaction cannot be rejected because its status is ${transaction.status}`
        });
      }

      transaction.status =
        TRANSACTION_STATUS.CANCELLED;

      transaction.requiresApproval =
        false;

      transaction.approvedBy =
        admin._id;

      transaction.approvedAt =
        new Date();

      transaction.processedBy =
        admin._id;

      transaction.processedAt =
        new Date();

      transaction.failureReason =
        reason ||
        'Deposit rejected by administrator';

      transaction.notes =
        reason ||
        'Deposit rejected by administrator';

      transaction.updatedAt =
        new Date();

      transaction.metadata = {

        ...(transaction.metadata || {}),

        mockPayment: true,

        adminRejected: true,

        rejectedAt:
          new Date(),

        rejectedBy:
          admin._id
      };

      await transaction.save();

      await sendNotification(

        transaction.userId,

        'Deposit Rejected',

        `Your deposit of ${transaction.amount.toLocaleString()} ETB was rejected.`,

        'deposit_rejected',

        {

          transactionId:
            transaction._id,

          amount:
            transaction.amount,

          reason:
            transaction.failureReason
        }
      );

      return res.json({

        success: true,

        message:
          'Deposit rejected successfully',

        transactionId:
          transaction._id,

        status:
          transaction.status,

        reason:
          transaction.failureReason
      });

    } catch (error: any) {

      console.error(
        'Reject deposit error:',
        error
      );

      return res.status(500).json({

        success: false,

        message:
          error.message ||
          'Failed to reject deposit'
      });
    }
  }
);

export default router;