// ============================================
// SHEBAODDS - WALLET ROUTES
// MOCK PAYMENT + ADMIN APPROVAL
// ============================================

import express, {
  Request,
  Response,
  Router
} from 'express';

import mongoose from 'mongoose';

import { authenticate } from './authRoutes';

import {
  checkResponsibleGambling
} from './responsibleGamblingMiddleware';

import User from './User';

import {
  Transaction,
  TRANSACTION_TYPES,
  PAYMENT_METHODS,
  TRANSACTION_STATUS
} from './Transaction';

const router: Router =
  express.Router();

// ==================== WALLET DEFAULT ====================

function createDefaultWallet() {
  return {
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

// ==================== MOCK PAYMENT SERVICE ====================

export interface ProcessDepositOptions {
  userId: any;
  amount: number;
  paymentMethod: string;
  paymentDetails: any;
  reference: string;
  callbackUrl: string;
}

export async function processDeposit(
  options: ProcessDepositOptions
) {

  console.log(
    `[MOCK PAYMENT] Deposit created: ${options.amount} ETB`
  );

  console.log(
    `[MOCK PAYMENT] Method: ${options.paymentMethod}`
  );

  console.log(
    `[MOCK PAYMENT] Reference: ${options.reference}`
  );

  // IMPORTANT:
  // This does NOT credit the wallet.
  // Admin approval is required.

  return {
    success: true,

    instant: false,

    requiresAdminApproval: true,

    gatewayReference:
      `MOCK_DEP_${Date.now()}`
  };
}

// ==================== MOCK WITHDRAWAL ====================

export interface ProcessWithdrawalOptions {
  userId: any;
  amount: number;
  paymentMethod: string;
  paymentDetails: any;
  reference: string;
}

export async function processWithdrawal(
  options: ProcessWithdrawalOptions
) {

  console.log(
    `[MOCK PAYMENT] Withdrawal approved: ${options.amount} ETB`
  );

  console.log(
    `[MOCK PAYMENT] Method: ${options.paymentMethod}`
  );

  console.log(
    `[MOCK PAYMENT] Reference: ${options.reference}`
  );

  return {
    success: true,

    gatewayReference:
      `MOCK_WDR_${Date.now()}`
  };
}

// ==================== NOTIFICATION ====================

export interface SendNotificationOptions {
  userId: any;
  title: string;
  message: string;
  type: string;
  data?: any;
}

export async function sendNotification(
  options: SendNotificationOptions
) {

  console.log(
    `[Notification] ${options.title}: ${options.message}`
  );

  return {
    success: true
  };
}

// ==================== BALANCE ====================

router.get(
  '/balance',
  authenticate,
  async (req: any, res: Response) => {

    try {

      const user = req.user;

      const wallet =
        user.wallet ||
        createDefaultWallet();

      return res.json({
        success: true,

        balance: wallet.balance || 0,

        bonusBalance:
          wallet.bonusBalance || 0,

        lockedBalance:
          wallet.lockedBalance || 0,

        totalDeposited:
          wallet.totalDeposited || 0,

        totalWithdrawn:
          wallet.totalWithdrawn || 0,

        totalWagered:
          wallet.totalWagered || 0,

        totalWon:
          wallet.totalWon || 0,

        totalTaxPaid:
          wallet.totalTaxPaid || 0,

        currency:
          wallet.currency || 'ETB'
      });

    } catch (error: any) {

      return res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

// ==================== TRANSACTIONS ====================

router.get(
  '/transactions',
  authenticate,
  async (req: any, res: Response) => {

    try {

      const {
        limit = '50',
        page = '1',
        type,
        status,
        from,
        to
      } = req.query;

      const query: any = {
        userId: req.user._id
      };

      if (type) {
        query.type = type;
      }

      if (status) {
        query.status = status;
      }

      if (from || to) {

        query.createdAt = {};

        if (from) {
          query.createdAt.$gte =
            new Date(from as string);
        }

        if (to) {
          query.createdAt.$lte =
            new Date(to as string);
        }
      }

      const limitNum =
        Math.min(
          parseInt(limit as string, 10) || 50,
          100
        );

      const pageNum =
        Math.max(
          parseInt(page as string, 10) || 1,
          1
        );

      const skip =
        (pageNum - 1) * limitNum;

      const [
        transactions,
        total
      ] = await Promise.all([

        Transaction.find(query)
          .sort({ createdAt: -1 })
          .skip(skip)
          .limit(limitNum),

        Transaction.countDocuments(query)
      ]);

      return res.json({
        success: true,

        transactions,

        pagination: {
          total,
          page: pageNum,
          limit: limitNum,
          pages:
            Math.ceil(total / limitNum)
        }
      });

    } catch (error: any) {

      return res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

// ==================== DEPOSIT ====================

router.post(
  '/deposit',
  authenticate,
  checkResponsibleGambling,
  async (req: any, res: Response) => {

    try {

      const {
        amount,
        paymentMethod,
        paymentDetails
      } = req.body;

      const user = req.user;

      const depositAmount =
        Number(amount);

      const minDeposit =
        Number(
          process.env.DEPOSIT_MIN_AMOUNT || 10
        );

      const maxDeposit =
        Number(
          process.env.DEPOSIT_MAX_AMOUNT ||
          100000
        );

      // ==================== VALIDATION ====================

      if (
        !Number.isFinite(depositAmount) ||
        depositAmount <= 0
      ) {

        return res.status(400).json({
          success: false,
          message: 'Invalid deposit amount'
        });
      }

      if (depositAmount < minDeposit) {

        return res.status(400).json({
          success: false,
          message:
            `Minimum deposit is ${minDeposit} ETB`
        });
      }

      if (depositAmount > maxDeposit) {

        return res.status(400).json({
          success: false,
          message:
            `Maximum deposit is ${maxDeposit} ETB`
        });
      }

      if (!paymentMethod) {

        return res.status(400).json({
          success: false,
          message:
            'Payment method is required'
        });
      }

      // ==================== WALLET ====================

      user.wallet =
        user.wallet ||
        createDefaultWallet();

      // ==================== DAILY LIMIT ====================

      const todayStart = new Date();

      todayStart.setHours(
        0,
        0,
        0,
        0
      );

      const userObjectId =
        new mongoose.Types.ObjectId(
          user._id.toString()
        );

      const todayDeposits =
        await Transaction.aggregate([

          {
            $match: {

              userId: userObjectId,

              type:
                TRANSACTION_TYPES.DEPOSIT,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte: todayStart
              }
            }
          },

          {
            $group: {
              _id: null,

              total: {
                $sum: '$amount'
              }
            }
          }
        ]);

      const depositedToday =
        todayDeposits[0]?.total || 0;

      const dailyLimit =
        typeof user.getDepositLimit === 'function'
          ? user.getDepositLimit()
          : (
              user.responsibleGambling
                ?.depositLimit || 50000
            );

      if (
        depositedToday +
        depositAmount >
        dailyLimit
      ) {

        return res.status(400).json({
          success: false,

          message:
            `Daily deposit limit of ${dailyLimit} ETB reached.`
        });
      }

      // ==================== REFERENCE ====================

      const paymentReference =
        `DEP_${Date.now()}_${Math.random()
          .toString(36)
          .substring(2, 10)
          .toUpperCase()}`;

      // ==================== CREATE PENDING TRANSACTION ====================

      const currentBalance =
        Number(user.wallet.balance || 0);

      const transaction =
        new Transaction({

          userId: user._id,

          type:
            TRANSACTION_TYPES.DEPOSIT,

          amount:
            depositAmount,

          fee: 0,

          taxAmount: 0,

          netAmount:
            depositAmount,

          paymentMethod,

          paymentReference,

          paymentDetails:
            paymentDetails || {},

          previousBalance:
            currentBalance,

          previousBonusBalance:
            Number(
              user.wallet.bonusBalance || 0
            ),

          // IMPORTANT:
          // Balance does not change yet.

          newBalance:
            currentBalance,

          newBonusBalance:
            Number(
              user.wallet.bonusBalance || 0
            ),

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval: true,

          ipAddress:
            req.ip,

          userAgent:
            req.headers['user-agent']
        });

      await transaction.save();

      // ==================== MOCK PAYMENT ====================

      const paymentResult =
        await processDeposit({

          userId: user._id,

          amount:
            depositAmount,

          paymentMethod,

          paymentDetails,

          reference:
            paymentReference,

          callbackUrl:
            `${process.env.BASE_URL || 'http://localhost:5000'}/api/payments/callback`
        });

      if (!paymentResult.success) {

        transaction.status =
          TRANSACTION_STATUS.FAILED;

        transaction.failureReason =
          'Mock payment failed';

        await transaction.save();

        return res.status(400).json({
          success: false,
          message:
            'Payment could not be initiated'
        });
      }

      transaction.paymentGatewayReference =
        paymentResult.gatewayReference;

      transaction.requiresApproval =
        true;

      transaction.status =
        TRANSACTION_STATUS.PENDING;

      await transaction.save();

      await sendNotification({

        userId: user._id,

        title:
          'Deposit Submitted 💰',

        message:
          `${depositAmount.toLocaleString()} ETB deposit is waiting for admin approval.`,

        type:
          'deposit_pending',

        data: {
          amount:
            depositAmount,

          reference:
            paymentReference,

          transactionId:
            transaction._id
        }
      });

      // ==================== IMPORTANT ====================

      return res.status(201).json({

        success: true,

        message:
          'Deposit submitted successfully. Waiting for admin approval.',

        transactionId:
          transaction._id,

        reference:
          paymentReference,

        amount:
          depositAmount,

        status:
          TRANSACTION_STATUS.PENDING,

        requiresApproval: true,

        balance:
          currentBalance
      });

    } catch (error: any) {

      console.error(
        'Deposit error:',
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

// ==================== WITHDRAWAL ====================

router.post(
  '/withdraw',
  authenticate,
  checkResponsibleGambling,
  async (req: any, res: Response) => {

    try {

      const {
        amount,
        paymentMethod,
        paymentDetails
      } = req.body;

      const user = req.user;

      const withdrawAmount =
        Number(amount);

      const minWithdraw =
        Number(
          process.env.WITHDRAWAL_MIN_AMOUNT || 50
        );

      const maxWithdraw =
        Number(
          process.env.WITHDRAWAL_MAX_AMOUNT ||
          50000
        );

      if (
        !Number.isFinite(withdrawAmount) ||
        withdrawAmount <= 0
      ) {

        return res.status(400).json({
          success: false,
          message:
            'Invalid withdrawal amount'
        });
      }

      if (
        withdrawAmount <
        minWithdraw
      ) {

        return res.status(400).json({
          success: false,
          message:
            `Minimum withdrawal is ${minWithdraw} ETB`
        });
      }

      if (
        withdrawAmount >
        maxWithdraw
      ) {

        return res.status(400).json({
          success: false,
          message:
            `Maximum withdrawal is ${maxWithdraw} ETB`
        });
      }

      if (!paymentMethod) {

        return res.status(400).json({
          success: false,
          message:
            'Payment method is required'
        });
      }

      user.wallet =
        user.wallet ||
        createDefaultWallet();

      const balance =
        Number(user.wallet.balance || 0);

      const lockedBalance =
        Number(
          user.wallet.lockedBalance || 0
        );

      const availableBalance =
        balance - lockedBalance;

      if (
        availableBalance <
        withdrawAmount
      ) {

        return res.status(400).json({
          success: false,
          message:
            'Insufficient available balance'
        });
      }

      // ==================== LOCK FUNDS ====================

      user.wallet.lockedBalance =
        lockedBalance +
        withdrawAmount;

      const paymentReference =
        `WDR_${Date.now()}_${Math.random()
          .toString(36)
          .substring(2, 10)
          .toUpperCase()}`;

      const transaction =
        new Transaction({

          userId: user._id,

          type:
            TRANSACTION_TYPES.WITHDRAWAL,

          amount:
            withdrawAmount,

          fee: 0,

          taxAmount: 0,

          netAmount:
            withdrawAmount,

          paymentMethod,

          paymentReference,

          paymentDetails:
            paymentDetails || {},

          previousBalance:
            balance,

          previousBonusBalance:
            Number(
              user.wallet.bonusBalance || 0
            ),

          newBalance:
            balance -

            withdrawAmount,

          newBonusBalance:
            Number(
              user.wallet.bonusBalance || 0
            ),

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval: true,

          ipAddress:
            req.ip,

          userAgent:
            req.headers['user-agent']
        });

      await Promise.all([
        user.save(),
        transaction.save()
      ]);

      await sendNotification({

        userId: user._id,

        title:
          'Withdrawal Submitted 💸',

        message:
          `${withdrawAmount.toLocaleString()} ETB withdrawal is waiting for admin approval.`,

        type:
          'withdrawal_pending',

        data: {
          amount:
            withdrawAmount,

          reference:
            paymentReference
        }
      });

      return res.status(201).json({

        success: true,

        message:
          'Withdrawal submitted. Waiting for admin approval.',

        transactionId:
          transaction._id,

        reference:
          paymentReference,

        amount:
          withdrawAmount,

        status:
          TRANSACTION_STATUS.PENDING,

        requiresApproval: true,

        balance
      });

    } catch (error: any) {

      console.error(
        'Withdrawal error:',
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

// ==================== SUMMARY ====================

router.get(
  '/summary',
  authenticate,
  async (req: any, res: Response) => {

    try {

      const userId =
        new mongoose.Types.ObjectId(
          req.user._id.toString()
        );

      const today =
        new Date();

      today.setHours(
        0,
        0,
        0,
        0
      );

      const result =
        await Transaction.aggregate([

          {
            $match: {
              userId,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte: today
              }
            }
          },

          {
            $group: {

              _id: null,

              deposits: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.DEPOSIT
                      ]
                    },
                    '$amount',
                    0
                  ]
                }
              },

              withdrawals: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.WITHDRAWAL
                      ]
                    },
                    '$amount',
                    0
                  ]
                }
              },

              wins: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.BET_WIN
                      ]
                    },
                    '$amount',
                    0
                  ]
                }
              },

              losses: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.BET_LOSS
                      ]
                    },
                    '$amount',
                    0
                  ]
                }
              }
            }
          }
        ]);

      const stats =
        result[0] || {
          deposits: 0,
          withdrawals: 0,
          wins: 0,
          losses: 0
        };

      return res.json({

        success: true,

        today: {

          deposits:
            stats.deposits || 0,

          withdrawals:
            stats.withdrawals || 0,

          wins:
            stats.wins || 0,

          losses:
            stats.losses || 0,

          netProfit:
            (stats.wins || 0) -
            (stats.losses || 0)
        }
      });

    } catch (error: any) {

      return res.status(500).json({
        success: false,
        message:
          error.message
      });
    }
  }
);

export default router;