// ============================================
// SHEBAODDS - WALLET ROUTES
// Mock Deposit + Admin Approval System
// ============================================

import express, {
  Response,
  Router
} from 'express';

import mongoose from 'mongoose';

import {
  authenticate
} from './authRoutes';

import {
  checkResponsibleGambling
} from './responsibleGamblingMiddleware';

import User from './User';

import {
  Transaction,
  TRANSACTION_TYPES,
  TRANSACTION_STATUS
} from './Transaction';

const router: Router =
  express.Router();

// ==================== WALLET DEFAULTS ====================

function ensureWallet(user: any) {

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

// ==================== MOCK PAYMENT SERVICE ====================
//
// IMPORTANT:
//
// This is ONLY a mock gateway.
//
// It creates a gateway reference.
// It DOES NOT credit the user's wallet.
//
// The wallet is credited only after admin approval.
//

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
    `[MOCK PAYMENT] Deposit initiated: ${options.amount} ETB`
  );

  console.log(
    `[MOCK PAYMENT] Method: ${options.paymentMethod}`
  );

  console.log(
    `[MOCK PAYMENT] Reference: ${options.reference}`
  );

  return {

    success: true,

    instant: false,

    requiresAdminApproval: true,

    gatewayReference:
      `MOCK_DEP_${Date.now()}_${Math.floor(
        1000 + Math.random() * 9000
      )}`
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
    `[MOCK PAYMENT] Withdrawal initiated: ${options.amount} ETB`
  );

  return {

    success: true,

    gatewayReference:
      `MOCK_WDR_${Date.now()}_${Math.floor(
        1000 + Math.random() * 9000
      )}`
  };
}

// ==================== NOTIFICATION SERVICE ====================

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
    `[Notification] ${options.title}`
  );

  console.log(
    `[Notification] User: ${options.userId}`
  );

  console.log(
    `[Notification] ${options.message}`
  );

  return {
    success: true
  };
}

// ==================== GET BALANCE ====================

router.get(
  '/balance',
  authenticate,
  async (
    req: any,
    res: Response
  ) => {

    try {

      const user = req.user;

      const wallet =
        ensureWallet(user);

      res.json({

        success: true,

        balance:
          wallet.balance || 0,

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

      console.error(
        'Balance error:',
        error
      );

      return res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

// ==================== GET TRANSACTIONS ====================

router.get(
  '/transactions',
  authenticate,
  async (
    req: any,
    res: Response
  ) => {

    try {

      const {
        limit = '50',
        page = '1',
        type,
        status,
        from,
        to
      } = req.query;

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

          const fromDate =
            new Date(from);

          if (!isNaN(fromDate.getTime())) {
            query.createdAt.$gte =
              fromDate;
          }
        }

        if (to) {

          const toDate =
            new Date(to);

          if (!isNaN(toDate.getTime())) {
            query.createdAt.$lte =
              toDate;
          }
        }
      }

      const skip =
        (pageNum - 1) *
        limitNum;

      const [
        transactions,
        total
      ] = await Promise.all([

        Transaction.find(query)
          .sort({
            createdAt: -1
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
        'Transaction history error:',
        error
      );

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
  async (
    req: any,
    res: Response
  ) => {

    try {

      const {
        amount,
        paymentMethod,
        paymentDetails
      } = req.body;

      const user = req.user;

      // -----------------------------
      // Validate amount
      // -----------------------------

      const minDeposit =
        parseInt(
          process.env.DEPOSIT_MIN_AMOUNT ||
          '10',
          10
        ) || 10;

      const maxDeposit =
        parseInt(
          process.env.DEPOSIT_MAX_AMOUNT ||
          '100000',
          10
        ) || 100000;

      const depositAmount =
        Number(amount);

      if (
        !Number.isFinite(
          depositAmount
        ) ||
        depositAmount <= 0
      ) {

        return res.status(400).json({
          success: false,
          message:
            'Please enter a valid deposit amount'
        });
      }

      if (
        depositAmount <
        minDeposit
      ) {

        return res.status(400).json({
          success: false,
          message:
            `Minimum deposit is ${minDeposit} ETB`
        });
      }

      if (
        depositAmount >
        maxDeposit
      ) {

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

      // -----------------------------
      // Wallet
      // -----------------------------

      const wallet =
        ensureWallet(user);

      // -----------------------------
      // Daily deposit limit
      // -----------------------------

      const todayStart =
        new Date();

      todayStart.setHours(
        0,
        0,
        0,
        0
      );

      const todayDeposits =
        await Transaction.aggregate([

          {
            $match: {

              userId:
                new mongoose.Types.ObjectId(
                  user._id
                ),

              type:
                TRANSACTION_TYPES.DEPOSIT,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte:
                  todayStart
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
        typeof user.getDepositLimit ===
        'function'

          ? user.getDepositLimit()

          : (
              user
                .responsibleGambling
                ?.depositLimit ||
              50000
            );

      if (
        depositedToday +
        depositAmount >
        dailyLimit
      ) {

        return res.status(400).json({

          success: false,

          message:
            `Daily deposit limit of ${dailyLimit} ETB reached. ` +
            `You have ${Math.max(
              dailyLimit -
              depositedToday,
              0
            )} ETB remaining today.`
        });
      }

      // -----------------------------
      // Generate reference
      // -----------------------------

      const paymentReference =
        `DEP_${Date.now()}_${Math.random()
          .toString(36)
          .substring(2, 10)
          .toUpperCase()}`;

      // -----------------------------
      // Create PENDING transaction
      // -----------------------------

      const transaction =
        new Transaction({

          userId:
            user._id,

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
            wallet.balance || 0,

          previousBonusBalance:
            wallet.bonusBalance || 0,

          // IMPORTANT:
          // DO NOT add money yet.
          newBalance:
            wallet.balance || 0,

          newBonusBalance:
            wallet.bonusBalance || 0,

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval: true,

          ipAddress:
            req.ip,

          userAgent:
            req.headers[
              'user-agent'
            ],

          metadata: {

            mockPayment: true,

            awaitingAdminApproval: true
          }
        });

      await transaction.save();

      // -----------------------------
      // Mock gateway
      // -----------------------------

      const paymentResult =
        await processDeposit({

          userId:
            user._id,

          amount:
            depositAmount,

          paymentMethod,

          paymentDetails:
            paymentDetails || {},

          reference:
            paymentReference,

          callbackUrl:
            `${process.env.BASE_URL || ''}/api/payments/callback`
        });

      if (!paymentResult.success) {

        transaction.status =
          TRANSACTION_STATUS.FAILED;

        transaction.failureReason =
          'Mock payment initialization failed';

        await transaction.save();

        return res.status(400).json({

          success: false,

          message:
            'Payment could not be initialized'
        });
      }

      transaction.paymentGatewayReference =
        paymentResult.gatewayReference;

      transaction.requiresApproval =
        true;

      transaction.status =
        TRANSACTION_STATUS.PENDING;

      await transaction.save();

      // -----------------------------
      // IMPORTANT:
      // Wallet is NOT credited here.
      // -----------------------------

      await sendNotification({

        userId:
          user._id,

        title:
          'Deposit Submitted 💰',

        message:
          `${depositAmount.toLocaleString()} ETB deposit submitted. ` +
          `It is waiting for admin approval.`,

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

      return res.status(201).json({

        success: true,

        message:
          'Deposit submitted successfully. Awaiting admin approval.',

        transactionId:
          transaction._id,

        reference:
          paymentReference,

        gatewayReference:
          paymentResult.gatewayReference,

        amount:
          depositAmount,

        status:
          TRANSACTION_STATUS.PENDING,

        requiresApproval:
          true,

        currentBalance:
          wallet.balance || 0
      });

    } catch (error: any) {

      console.error(
        'Deposit error:',
        error
      );

      return res.status(500).json({

        success: false,

        message:
          error.message ||
          'Deposit failed'
      });
    }
  }
);

// ==================== WITHDRAWAL ====================

router.post(
  '/withdraw',
  authenticate,
  checkResponsibleGambling,
  async (
    req: any,
    res: Response
  ) => {

    try {

      const {
        amount,
        paymentMethod,
        paymentDetails
      } = req.body;

      const user = req.user;

      const minWithdraw =
        parseInt(
          process.env.WITHDRAWAL_MIN_AMOUNT ||
          '50',
          10
        ) || 50;

      const maxWithdraw =
        parseInt(
          process.env.WITHDRAWAL_MAX_AMOUNT ||
          '50000',
          10
        ) || 50000;

      const withdrawAmount =
        Number(amount);

      if (
        !Number.isFinite(
          withdrawAmount
        ) ||
        withdrawAmount <= 0
      ) {

        return res.status(400).json({
          success: false,
          message:
            'Please enter a valid withdrawal amount'
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
            `Maximum withdrawal per transaction is ${maxWithdraw} ETB`
        });
      }

      const wallet =
        ensureWallet(user);

      const availableBalance =
        (wallet.balance || 0) -
        (wallet.lockedBalance || 0);

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

      // -----------------------------
      // Daily withdrawal limit
      // -----------------------------

      const todayStart =
        new Date();

      todayStart.setHours(
        0,
        0,
        0,
        0
      );

      const todayWithdrawals =
        await Transaction.aggregate([

          {
            $match: {

              userId:
                new mongoose.Types.ObjectId(
                  user._id
                ),

              type:
                TRANSACTION_TYPES.WITHDRAWAL,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte:
                  todayStart
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

      const withdrawnToday =
        todayWithdrawals[0]?.total || 0;

      const dailyLimit =
        parseInt(
          process.env.WITHDRAWAL_DAILY_LIMIT ||
          '100000',
          10
        ) || 100000;

      if (
        withdrawnToday +
        withdrawAmount >
        dailyLimit
      ) {

        return res.status(400).json({

          success: false,

          message:
            `Daily withdrawal limit of ${dailyLimit} ETB reached.`
        });
      }

      // -----------------------------
      // Weekly limit
      // -----------------------------

      const weekStart =
        new Date();

      weekStart.setDate(
        weekStart.getDate() -
        weekStart.getDay()
      );

      weekStart.setHours(
        0,
        0,
        0,
        0
      );

      const weeklyWithdrawals =
        await Transaction.aggregate([

          {
            $match: {

              userId:
                new mongoose.Types.ObjectId(
                  user._id
                ),

              type:
                TRANSACTION_TYPES.WITHDRAWAL,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte:
                  weekStart
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

      const withdrawnWeekly =
        weeklyWithdrawals[0]?.total || 0;

      const weeklyLimit =
        parseInt(
          process.env.WITHDRAWAL_WEEKLY_LIMIT ||
          '500000',
          10
        ) || 500000;

      if (
        withdrawnWeekly +
        withdrawAmount >
        weeklyLimit
      ) {

        return res.status(400).json({

          success: false,

          message:
            `Weekly withdrawal limit of ${weeklyLimit} ETB reached.`
        });
      }

      // -----------------------------
      // Lock balance
      // -----------------------------

      const previousBalance =
        wallet.balance || 0;

      const previousLockedBalance =
        wallet.lockedBalance || 0;

      wallet.lockedBalance =
        previousLockedBalance +
        withdrawAmount;

      wallet.balance =
        previousBalance -
        withdrawAmount;

      // -----------------------------
      // Reference
      // -----------------------------

      const paymentReference =
        `WDR_${Date.now()}_${Math.random()
          .toString(36)
          .substring(2, 10)
          .toUpperCase()}`;

      const isVipLevelHigh =
        Boolean(
          user.vip &&
          user.vip.level >= 3
        );

      const requiresApproval =
        withdrawAmount > 10000 ||
        !isVipLevelHigh;

      // -----------------------------
      // Transaction
      // -----------------------------

      const transaction =
        new Transaction({

          userId:
            user._id,

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

          previousBalance,

          previousBonusBalance:
            wallet.bonusBalance || 0,

          newBalance:
            wallet.balance,

          newBonusBalance:
            wallet.bonusBalance || 0,

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval,

          ipAddress:
            req.ip,

          userAgent:
            req.headers[
              'user-agent'
            ]
        });

      await Promise.all([

        user.save(),

        transaction.save()

      ]);

      await sendNotification({

        userId:
          user._id,

        title:
          'Withdrawal Request Submitted 💸',

        message:
          `Your withdrawal request of ${withdrawAmount.toLocaleString()} ETB has been submitted.`,

        type:
          'withdrawal',

        data: {

          amount:
            withdrawAmount,

          reference:
            paymentReference
        }
      });

      // -----------------------------
      // Auto process
      // -----------------------------

      if (!requiresApproval) {

        const result =
          await processWithdrawal({

            userId:
              user._id,

            amount:
              withdrawAmount,

            paymentMethod,

            paymentDetails:
              paymentDetails || {},

            reference:
              paymentReference
          });

        if (result.success) {

          user.wallet.lockedBalance =
            Math.max(
              0,
              (user.wallet.lockedBalance || 0) -
              withdrawAmount
            );

          user.wallet.totalWithdrawn =
            (user.wallet.totalWithdrawn || 0) +
            withdrawAmount;

          transaction.status =
            TRANSACTION_STATUS.COMPLETED;

          transaction.completedAt =
            new Date();

          transaction.paymentGatewayReference =
            result.gatewayReference;

          await Promise.all([

            user.save(),

            transaction.save()

          ]);

          await sendNotification({

            userId:
              user._id,

            title:
              'Withdrawal Completed ✅',

            message:
              `Your withdrawal of ${withdrawAmount.toLocaleString()} ETB has been processed.`,

            type:
              'withdrawal_completed',

            data: {
              amount:
                withdrawAmount
            }
          });

          return res.json({

            success: true,

            message:
              'Withdrawal processed successfully!',

            transactionId:
              transaction._id,

            newBalance:
              user.wallet.balance
          });
        }
      }

      return res.json({

        success: true,

        message:
          'Withdrawal request submitted for review.',

        transactionId:
          transaction._id,

        requiresApproval,

        reference:
          paymentReference
      });

    } catch (error: any) {

      console.error(
        'Withdrawal error:',
        error
      );

      return res.status(500).json({

        success: false,

        message:
          error.message ||
          'Withdrawal failed'
      });
    }
  }
);

// ==================== WALLET SUMMARY ====================

router.get(
  '/summary',
  authenticate,
  async (
    req: any,
    res: Response
  ) => {

    try {

      const userId =
        new mongoose.Types.ObjectId(
          req.user._id
        );

      const todayStart =
        new Date();

      todayStart.setHours(
        0,
        0,
        0,
        0
      );

      const weekStart =
        new Date();

      weekStart.setDate(
        weekStart.getDate() -
        weekStart.getDay()
      );

      weekStart.setHours(
        0,
        0,
        0,
        0
      );

      const monthStart =
        new Date();

      monthStart.setDate(1);

      monthStart.setHours(
        0,
        0,
        0,
        0
      );

      const createStatsPipeline =
        (startDate: Date) => [

          {
            $match: {

              userId,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte:
                  startDate
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
        ];

      const [
        todayStats,
        weekStats,
        monthStats
      ] = await Promise.all([

        Transaction.aggregate(
          createStatsPipeline(
            todayStart
          )
        ),

        Transaction.aggregate(
          createStatsPipeline(
            weekStart
          )
        ),

        Transaction.aggregate(
          createStatsPipeline(
            monthStart
          )
        )

      ]);

      const formatStats =
        (result: any[]) => {

          const stats =
            result[0] || {};

          const deposits =
            stats.deposits || 0;

          const withdrawals =
            stats.withdrawals || 0;

          const wins =
            stats.wins || 0;

          const losses =
            stats.losses || 0;

          return {

            deposits,

            withdrawals,

            wins,

            losses,

            netProfit:
              wins - losses
          };
        };

      return res.json({

        success: true,

        today:
          formatStats(
            todayStats
          ),

        weekly:
          formatStats(
            weekStats
          ),

        monthly:
          formatStats(
            monthStats
          )
      });

    } catch (error: any) {

      console.error(
        'Wallet summary error:',
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

export default router;