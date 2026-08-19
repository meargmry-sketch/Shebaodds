// ============================================
// SHEBAODDS - WALLET ROUTES
// Deposit / Withdrawal / Transactions
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

import User, {
  createDefaultWallet,
  IUser
} from './User';

import {
  Transaction,
  TRANSACTION_TYPES,
  TRANSACTION_STATUS
} from './Transaction';

const router: Router =
  express.Router();

// ============================================
// HELPERS
// ============================================

function ensureWallet(user: IUser) {
  if (!user.wallet) {
    user.wallet =
      createDefaultWallet();
  }

  return user.wallet;
}

function parsePositiveAmount(
  value: unknown
): number | null {
  const amount =
    typeof value === 'number'
      ? value
      : Number(value);

  if (
    !Number.isFinite(amount) ||
    amount <= 0
  ) {
    return null;
  }

  return amount;
}

function getObjectId(
  value: mongoose.Types.ObjectId | string
): mongoose.Types.ObjectId {
  if (
    value instanceof mongoose.Types.ObjectId
  ) {
    return value;
  }

  return new mongoose.Types.ObjectId(
    String(value)
  );
}

// ============================================
// PAYMENT SERVICE BOUNDARY
// ============================================

export interface ProcessDepositOptions {
  userId: mongoose.Types.ObjectId;
  amount: number;
  paymentMethod: string;
  paymentDetails: any;
  reference: string;
  callbackUrl: string;
}

export async function processDeposit(
  options: ProcessDepositOptions
) {
  /*
   * DEVELOPMENT / MOCK ONLY.
   *
   * Replace this with a licensed payment
   * provider and verified webhook before
   * production.
   */

  console.log(
    `[PaymentService] Deposit ${options.amount} ETB via ${options.paymentMethod}`
  );

  return {
    success: true,
    instant: true,
    gatewayReference:
      `GW_DEP_${Date.now()}_${Math.floor(
        1000 + Math.random() * 9000
      )}`
  };
}

// ============================================
// WITHDRAWAL SERVICE
// ============================================

export interface ProcessWithdrawalOptions {
  userId: mongoose.Types.ObjectId;
  amount: number;
  paymentMethod: string;
  paymentDetails: any;
  reference: string;
}

export async function processWithdrawal(
  options: ProcessWithdrawalOptions
) {
  /*
   * DEVELOPMENT / MOCK ONLY.
   *
   * Replace this with actual disbursement
   * and verified provider response.
   */

  console.log(
    `[PaymentService] Withdrawal ${options.amount} ETB via ${options.paymentMethod}`
  );

  return {
    success: true,
    gatewayReference:
      `GW_WDR_${Date.now()}_${Math.floor(
        1000 + Math.random() * 9000
      )}`
  };
}

// ============================================
// NOTIFICATION SERVICE
// ============================================

export interface SendNotificationOptions {
  userId: mongoose.Types.ObjectId;
  title: string;
  message: string;
  type: string;
  data?: any;
}

export async function sendNotification(
  options: SendNotificationOptions
) {
  console.log(
    `[NotificationService] ${options.title}: ${options.message}`
  );

  return {
    success: true
  };
}

// ============================================
// BALANCE
// ============================================

router.get(
  '/balance',
  authenticate,
  async (
    req: any,
    res: Response
  ) => {
    try {
      const user =
        req.user as IUser;

      const wallet =
        ensureWallet(user);

      return res.json({
        success: true,

        balance: wallet.balance,
        bonusBalance:
          wallet.bonusBalance,

        lockedBalance:
          wallet.lockedBalance,

        totalDeposited:
          wallet.totalDeposited,

        totalWithdrawn:
          wallet.totalWithdrawn,

        totalWagered:
          wallet.totalWagered,

        totalWon:
          wallet.totalWon,

        totalLost:
          wallet.totalLost,

        totalTaxPaid:
          wallet.totalTaxPaid,

        totalBonusReceived:
          wallet.totalBonusReceived,

        totalCashbackReceived:
          wallet.totalCashbackReceived,

        currency:
          wallet.currency
      });
    } catch (error: any) {
      console.error(
        'Balance error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Unable to retrieve wallet balance'
      });
    }
  }
);

// ============================================
// TRANSACTIONS
// ============================================

router.get(
  '/transactions',
  authenticate,
  async (
    req: any,
    res: Response
  ) => {
    try {
      const user =
        req.user as IUser;

      const {
        limit = '50',
        page = '1',
        type,
        status,
        from,
        to
      } = req.query as any;

      const limitNum = Math.min(
        Math.max(
          parseInt(limit, 10) || 50,
          1
        ),
        100
      );

      const pageNum = Math.max(
        parseInt(page, 10) || 1,
        1
      );

      const query: any = {
        userId: user._id
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

          if (
            !Number.isNaN(
              fromDate.getTime()
            )
          ) {
            query.createdAt.$gte =
              fromDate;
          }
        }

        if (to) {
          const toDate =
            new Date(to);

          if (
            !Number.isNaN(
              toDate.getTime()
            )
          ) {
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
          .exec(),

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
          pages: Math.ceil(
            total / limitNum
          )
        }
      });
    } catch (error: any) {
      console.error(
        'Transactions error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Unable to retrieve transactions'
      });
    }
  }
);

// ============================================
// DEPOSIT
// ============================================

router.post(
  '/deposit',
  authenticate,
  checkResponsibleGambling,
  async (
    req: any,
    res: Response
  ) => {
    try {
      const user =
        req.user as IUser;

      const {
        amount,
        paymentMethod,
        paymentDetails
      } = req.body || {};

      const depositAmount =
        parsePositiveAmount(
          amount
        );

      if (
        depositAmount === null
      ) {
        return res.status(400).json({
          success: false,
          message:
            'A valid deposit amount is required'
        });
      }

      if (!paymentMethod) {
        return res.status(400).json({
          success: false,
          message:
            'Payment method is required'
        });
      }

      const minDeposit =
        Number(
          process.env
            .DEPOSIT_MIN_AMOUNT ||
            10
        );

      const maxDeposit =
        Number(
          process.env
            .DEPOSIT_MAX_AMOUNT ||
            100000
        );

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

      const wallet =
        ensureWallet(user);

      // ========================================
      // DAILY DEPOSIT LIMIT
      // ========================================

      const todayStart =
        new Date();

      todayStart.setHours(
        0,
        0,
        0,
        0
      );

      const userId =
        getObjectId(user._id);

      const todayDeposits =
        await Transaction.aggregate([
          {
            $match: {
              userId,
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
        todayDeposits[0]?.total ||
        0;

      const dailyLimit =
        typeof user.getDepositLimit ===
        'function'
          ? user.getDepositLimit()
          : (
              user.responsibleGambling
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
              0,
              dailyLimit -
                depositedToday
            )} ETB remaining today.`
        });
      }

      // ========================================
      // CREATE TRANSACTION
      // ========================================

      const paymentReference =
        `DEP_${Date.now()}_${cryptoRandomId()}`;

      const previousBalance =
        wallet.balance || 0;

      const previousBonusBalance =
        wallet.bonusBalance || 0;

      const newBalance =
        previousBalance +
        depositAmount;

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

          previousBalance,

          previousBonusBalance,

          newBalance,

          newBonusBalance:
            previousBonusBalance,

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval: false,

          ipAddress: req.ip,

          userAgent:
            req.headers[
              'user-agent'
            ]
        });

      await transaction.save();

      // ========================================
      // PROCESS PAYMENT
      // ========================================

      const paymentResult =
        await processDeposit({
          userId,
          amount:
            depositAmount,

          paymentMethod,
          paymentDetails:
            paymentDetails || {},

          reference:
            paymentReference,

          callbackUrl:
            `${
              process.env.BASE_URL ||
              'http://localhost:3000'
            }/api/payments/callback`
        });

      if (
        !paymentResult.success
      ) {
        await transaction.fail(
          'Payment gateway rejected the deposit'
        );

        return res.status(400).json({
          success: false,
          message:
            'Payment gateway rejected the deposit',
          transactionId:
            transaction._id
        });
      }

      // ========================================
      // INSTANT PAYMENT
      // ========================================

      if (
        paymentResult.instant
      ) {
        wallet.balance =
          newBalance;

        wallet.totalDeposited =
          (wallet.totalDeposited ||
            0) +
          depositAmount;

        transaction.status =
          TRANSACTION_STATUS.COMPLETED;

        transaction.completedAt =
          new Date();

        transaction.paymentGatewayReference =
          paymentResult.gatewayReference;

        transaction.newBalance =
          wallet.balance;

        await Promise.all([
          user.save(),
          transaction.save()
        ]);

        await sendNotification({
          userId,

          title:
            'Deposit Successful',

          message:
            `${depositAmount.toLocaleString()} ETB has been added to your wallet.`,

          type: 'deposit',

          data: {
            amount:
              depositAmount,

            newBalance:
              wallet.balance
          }
        });

        // ======================================
        // FIRST DEPOSIT BONUS
        // ======================================

        const depositCount =
          await Transaction.countDocuments({
            userId,

            type:
              TRANSACTION_TYPES.DEPOSIT,

            status:
              TRANSACTION_STATUS.COMPLETED
          });

        if (
          depositCount === 1
        ) {
          const bonusPercentage =
            Number(
              process.env
                .DEPOSIT_BONUS_PERCENTAGE ||
                50
            );

          const maxBonus =
            Number(
              process.env
                .DEPOSIT_BONUS_MAX ||
                500
            );

          const bonusAmount =
            Math.min(
              (
                depositAmount *
                bonusPercentage
              ) / 100,
              maxBonus
            );

          if (
            bonusAmount > 0
          ) {
            wallet.bonusBalance =
              (
                wallet.bonusBalance ||
                0
              ) +
              bonusAmount;

            wallet.totalBonusReceived =
              (
                wallet.totalBonusReceived ||
                0
              ) +
              bonusAmount;

            await user.save();

            await sendNotification({
              userId,

              title:
                'First Deposit Bonus',

              message:
                `You received ${bonusAmount.toLocaleString()} ETB bonus.`,

              type: 'bonus',

              data: {
                bonusAmount
              }
            });
          }
        }

        return res.json({
          success: true,

          message:
            'Deposit successful!',

          transactionId:
            transaction._id,

          newBalance:
            wallet.balance,

          bonusBalance:
            wallet.bonusBalance
        });
      }

      return res.json({
        success: true,

        message:
          'Deposit initiated. Please complete the payment.',

        transactionId:
          transaction._id,

        reference:
          paymentReference
      });

    } catch (error: any) {
      console.error(
        'Deposit error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Unable to process deposit'
      });
    }
  }
);

// ============================================
// WITHDRAWAL
// ============================================

router.post(
  '/withdraw',
  authenticate,
  checkResponsibleGambling,
  async (
    req: any,
    res: Response
  ) => {
    try {
      const user =
        req.user as IUser;

      const {
        amount,
        paymentMethod,
        paymentDetails
      } = req.body || {};

      const withdrawAmount =
        parsePositiveAmount(
          amount
        );

      if (
        withdrawAmount === null
      ) {
        return res.status(400).json({
          success: false,
          message:
            'A valid withdrawal amount is required'
        });
      }

      if (!paymentMethod) {
        return res.status(400).json({
          success: false,
          message:
            'Payment method is required'
        });
      }

      const minWithdraw =
        Number(
          process.env
            .WITHDRAWAL_MIN_AMOUNT ||
            50
        );

      const maxWithdraw =
        Number(
          process.env
            .WITHDRAWAL_MAX_AMOUNT ||
            50000
        );

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

      const currentBalance =
        wallet.balance || 0;

      if (
        currentBalance <
        withdrawAmount
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Insufficient balance'
        });
      }

      // ========================================
      // DAILY LIMIT
      // ========================================

      const todayStart =
        new Date();

      todayStart.setHours(
        0,
        0,
        0,
        0
      );

      const userId =
        getObjectId(user._id);

      const todayWithdrawals =
        await Transaction.aggregate([
          {
            $match: {
              userId,
              type:
                TRANSACTION_TYPES.WITHDRAWAL,
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

      const withdrawnToday =
        todayWithdrawals[0]?.total ||
        0;

      const dailyLimit =
        Number(
          process.env
            .WITHDRAWAL_DAILY_LIMIT ||
            100000
        );

      if (
        withdrawnToday +
          withdrawAmount >
        dailyLimit
      ) {
        return res.status(400).json({
          success: false,

          message:
            `Daily withdrawal limit of ${dailyLimit} ETB reached. ` +
            `You have ${Math.max(
              0,
              dailyLimit -
                withdrawnToday
            )} ETB remaining today.`
        });
      }

      // ========================================
      // WEEKLY LIMIT
      // ========================================

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
              userId,
              type:
                TRANSACTION_TYPES.WITHDRAWAL,
              status:
                TRANSACTION_STATUS.COMPLETED,
              createdAt: {
                $gte: weekStart
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
        weeklyWithdrawals[0]?.total ||
        0;

      const weeklyLimit =
        Number(
          process.env
            .WITHDRAWAL_WEEKLY_LIMIT ||
            500000
        );

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

      // ========================================
      // LOCK BALANCE
      // ========================================

      const previousBalance =
        currentBalance;

      const previousBonusBalance =
        wallet.bonusBalance || 0;

      wallet.balance =
        previousBalance -
        withdrawAmount;

      wallet.lockedBalance =
        (wallet.lockedBalance ||
          0) +
        withdrawAmount;

      const newBalance =
        wallet.balance;

      const paymentReference =
        `WDR_${Date.now()}_${cryptoRandomId()}`;

      const isVipLevelHigh =
        Boolean(
          user.vip &&
          user.vip.level >= 3
        );

      const requiresApproval =
        withdrawAmount > 10000 ||
        !isVipLevelHigh;

      // ========================================
      // TRANSACTION
      // ========================================

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

          previousBalance,

          previousBonusBalance,

          newBalance,

          newBonusBalance:
            previousBonusBalance,

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval,

          ipAddress: req.ip,

          userAgent:
            req.headers[
              'user-agent'
            ]
        });

      try {
        await Promise.all([
          user.save(),
          transaction.save()
        ]);
      } catch (saveError) {
        /*
         * Do not leave the amount locked if
         * transaction/user persistence fails.
         */
        wallet.balance =
          previousBalance;

        wallet.lockedBalance =
          Math.max(
            0,
            (
              wallet.lockedBalance ||
              0
            ) -
            withdrawAmount
          );

        throw saveError;
      }

      // ========================================
      // NOTIFICATION
      // ========================================

      await sendNotification({
        userId,

        title:
          'Withdrawal Request Submitted',

        message:
          `Your withdrawal request of ${withdrawAmount.toLocaleString()} ETB has been submitted.`,

        type: 'withdrawal',

        data: {
          amount:
            withdrawAmount,

          reference:
            paymentReference
        }
      });

      // ========================================
      // AUTO PROCESS
      // ========================================

      if (!requiresApproval) {
        const result =
          await processWithdrawal({
            userId,

            amount:
              withdrawAmount,

            paymentMethod,

            paymentDetails:
              paymentDetails || {},

            reference:
              paymentReference
          });

        if (
          result.success
        ) {
          wallet.lockedBalance =
            Math.max(
              0,
              (
                wallet.lockedBalance ||
                0
              ) -
              withdrawAmount
            );

          wallet.totalWithdrawn =
            (
              wallet.totalWithdrawn ||
              0
            ) +
            withdrawAmount;

          transaction.status =
            TRANSACTION_STATUS.COMPLETED;

          transaction.completedAt =
            new Date();

          transaction.processedAt =
            new Date();

          transaction.paymentGatewayReference =
            result.gatewayReference;

          await Promise.all([
            user.save(),
            transaction.save()
          ]);

          await sendNotification({
            userId,

            title:
              'Withdrawal Completed',

            message:
              `Your withdrawal of ${withdrawAmount.toLocaleString()} ETB has been processed.`,

            type: 'withdrawal',

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
              wallet.balance
          });
        }

        // ======================================
        // WITHDRAWAL FAILED
        // ======================================

        wallet.balance =
          (
            wallet.balance ||
            0
          ) +
          withdrawAmount;

        wallet.lockedBalance =
          Math.max(
            0,
            (
              wallet.lockedBalance ||
              0
            ) -
            withdrawAmount
          );

        transaction.status =
          TRANSACTION_STATUS.FAILED;

        transaction.failureReason =
          'Withdrawal processing failed';

        transaction.processedAt =
          new Date();

        await Promise.all([
          user.save(),
          transaction.save()
        ]);

        return res.status(400).json({
          success: false,

          message:
            'Withdrawal could not be processed',

          transactionId:
            transaction._id
        });
      }

      // ========================================
      // MANUAL APPROVAL
      // ========================================

      return res.json({
        success: true,

        message:
          'Withdrawal request submitted for review.',

        transactionId:
          transaction._id,

        requiresApproval: true,

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
          'Unable to process withdrawal'
      });
    }
  }
);

// ============================================
// WALLET SUMMARY
// ============================================

router.get(
  '/summary',
  authenticate,
  async (
    req: any,
    res: Response
  ) => {
    try {
      const user =
        req.user as IUser;

      const userId =
        getObjectId(user._id);

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

      const buildSummary =
        async (
          startDate: Date
        ) => {
          const result =
            await Transaction.aggregate([
              {
                $match: {
                  userId,

                  status:
                    TRANSACTION_STATUS.COMPLETED,

                  createdAt: {
                    $gte: startDate
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

          const row =
            result[0] || {};

          return {
            deposits:
              row.deposits || 0,

            withdrawals:
              row.withdrawals || 0,

            wins:
              row.wins || 0,

            losses:
              row.losses || 0,

            netProfit:
              (row.wins || 0) -
              (row.losses || 0)
          };
        };

      const [
        today,
        weekly,
        monthly
      ] = await Promise.all([
        buildSummary(
          todayStart
        ),

        buildSummary(
          weekStart
        ),

        buildSummary(
          monthStart
        )
      ]);

      return res.json({
        success: true,
        today,
        weekly,
        monthly
      });

    } catch (error: any) {
      console.error(
        'Wallet summary error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Unable to retrieve wallet summary'
      });
    }
  }
);

// ============================================
// RANDOM REFERENCE ID
// ============================================

function cryptoRandomId(): string {
  return Math.random()
    .toString(36)
    .substring(2, 10)
    .toUpperCase();
}

// ============================================
// EXPORT
// ============================================

export default router;