// ============================================================
// SHEBAODDS - WALLET ROUTES
// Production-ready Mongoose 8 + TypeScript version
// Mock Deposit + Admin Approval System
// ============================================================

import express, {
  Request,
  Response,
  Router,
} from 'express';

import mongoose, {
  Model,
  Types,
} from 'mongoose';

import {
  authenticate,
} from './authRoutes';

import {
  checkResponsibleGambling,
} from './responsibleGamblingMiddleware';

import User from './User';

import {
  Transaction,
  TRANSACTION_TYPES,
  TRANSACTION_STATUS,
} from './Transaction';

// ============================================================
// ROUTER
// ============================================================

const router: Router = express.Router();

// ============================================================
// TYPES
// ============================================================

interface AuthenticatedRequest extends Request {
  user?: any;
}

interface WalletLike {
  balance?: number;
  bonusBalance?: number;
  lockedBalance?: number;

  totalDeposited?: number;
  totalWithdrawn?: number;

  totalWagered?: number;
  totalWon?: number;

  totalTaxPaid?: number;

  currency?: string;
}

interface TransactionDocumentLike {
  _id: Types.ObjectId;

  userId: Types.ObjectId;

  type: string;
  amount: number;

  fee?: number;
  taxAmount?: number;
  netAmount?: number;

  paymentMethod?: string;
  paymentReference?: string;
  paymentGatewayReference?: string;
  paymentDetails?: Record<string, unknown>;

  previousBalance?: number;
  previousBonusBalance?: number;

  newBalance?: number;
  newBonusBalance?: number;

  status: string;

  requiresApproval?: boolean;

  failureReason?: string;

  ipAddress?: string;
  userAgent?: string;

  metadata?: Record<string, unknown>;

  completedAt?: Date;

  createdAt?: Date;
  updatedAt?: Date;

  save(): Promise<TransactionDocumentLike>;
}

// ============================================================
// MONGOOSE MODEL NORMALIZATION
// ============================================================
//
// Mongoose 8 can expose incompatible overload unions when a
// model has been exported/imported in different ways.
//
// These stable model references prevent TS2349 errors such as:
//
// "Each member of the union type ... has signatures, but none
// of those signatures are compatible with each other."
//
// ============================================================

const TransactionModel =
  Transaction as unknown as Model<TransactionDocumentLike>;

const UserModel =
  User as unknown as Model<any>;

// ============================================================
// WALLET HELPERS
// ============================================================

function ensureWallet(user: any): WalletLike {
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

      currency: 'ETB',
    };
  }

  user.wallet.balance =
    Number(user.wallet.balance) || 0;

  user.wallet.bonusBalance =
    Number(user.wallet.bonusBalance) || 0;

  user.wallet.lockedBalance =
    Number(user.wallet.lockedBalance) || 0;

  user.wallet.totalDeposited =
    Number(user.wallet.totalDeposited) || 0;

  user.wallet.totalWithdrawn =
    Number(user.wallet.totalWithdrawn) || 0;

  user.wallet.totalWagered =
    Number(user.wallet.totalWagered) || 0;

  user.wallet.totalWon =
    Number(user.wallet.totalWon) || 0;

  user.wallet.totalTaxPaid =
    Number(user.wallet.totalTaxPaid) || 0;

  user.wallet.currency =
    user.wallet.currency || 'ETB';

  return user.wallet;
}

// ============================================================
// OBJECT ID HELPER
// ============================================================

function toObjectId(
  value: unknown
): Types.ObjectId {
  if (value instanceof Types.ObjectId) {
    return value;
  }

  if (
    typeof value !== 'string' ||
    !mongoose.isValidObjectId(value)
  ) {
    throw new Error('Invalid user ID');
  }

  return new Types.ObjectId(value);
}

// ============================================================
// NUMBER HELPERS
// ============================================================

function positiveNumber(
  value: unknown
): number | null {
  const number = Number(value);

  if (
    !Number.isFinite(number) ||
    number <= 0
  ) {
    return null;
  }

  return number;
}

function envNumber(
  name: string,
  fallback: number
): number {
  const value =
    Number(process.env[name]);

  if (
    !Number.isFinite(value) ||
    value <= 0
  ) {
    return fallback;
  }

  return value;
}

// ============================================================
// DATE HELPERS
// ============================================================

function startOfToday(): Date {
  const date = new Date();

  date.setHours(
    0,
    0,
    0,
    0
  );

  return date;
}

function startOfWeek(): Date {
  const date = new Date();

  date.setDate(
    date.getDate() -
      date.getDay()
  );

  date.setHours(
    0,
    0,
    0,
    0
  );

  return date;
}

function startOfMonth(): Date {
  const date = new Date();

  date.setDate(1);

  date.setHours(
    0,
    0,
    0,
    0
  );

  return date;
}

// ============================================================
// MOCK PAYMENT SERVICE
// ============================================================

export interface ProcessDepositOptions {
  userId: Types.ObjectId;

  amount: number;

  paymentMethod: string;

  paymentDetails: Record<string, unknown>;

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
      )}`,
  };
}

// ============================================================
// MOCK WITHDRAWAL SERVICE
// ============================================================

export interface ProcessWithdrawalOptions {
  userId: Types.ObjectId;

  amount: number;

  paymentMethod: string;

  paymentDetails: Record<string, unknown>;

  reference: string;
}

export async function processWithdrawal(
  options: ProcessWithdrawalOptions
) {
  console.log(
    `[MOCK PAYMENT] Withdrawal initiated: ${options.amount} ETB`
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
      `MOCK_WDR_${Date.now()}_${Math.floor(
        1000 + Math.random() * 9000
      )}`,
  };
}

// ============================================================
// NOTIFICATION SERVICE
// ============================================================

export interface SendNotificationOptions {
  userId: Types.ObjectId;

  title: string;

  message: string;

  type: string;

  data?: Record<string, unknown>;
}

export async function sendNotification(
  options: SendNotificationOptions
) {
  console.log(
    `[Notification] ${options.title}`
  );

  console.log(
    `[Notification] User: ${options.userId.toString()}`
  );

  console.log(
    `[Notification] ${options.message}`
  );

  return {
    success: true,
  };
}

// ============================================================
// GET BALANCE
// ============================================================

router.get(
  '/balance',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const user = req.user;

      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Authentication required',
        });
      }

      const wallet =
        ensureWallet(user);

      return res.json({
        success: true,

        balance:
          wallet.balance || 0,

        bonusBalance:
          wallet.bonusBalance || 0,

        lockedBalance:
          wallet.lockedBalance || 0,

        availableBalance:
          Math.max(
            0,
            (wallet.balance || 0) -
              (wallet.lockedBalance || 0)
          ),

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
          wallet.currency || 'ETB',
      });
    } catch (error) {
      console.error(
        'Balance error:',
        error
      );

      return res.status(500).json({
        success: false,
        message: 'Unable to retrieve wallet balance',
      });
    }
  }
);

// ============================================================
// GET TRANSACTIONS
// ============================================================

router.get(
  '/transactions',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const user = req.user;

      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Authentication required',
        });
      }

      const limitValue =
        Array.isArray(req.query.limit)
          ? req.query.limit[0]
          : req.query.limit;

      const pageValue =
        Array.isArray(req.query.page)
          ? req.query.page[0]
          : req.query.page;

      const typeValue =
        Array.isArray(req.query.type)
          ? req.query.type[0]
          : req.query.type;

      const statusValue =
        Array.isArray(req.query.status)
          ? req.query.status[0]
          : req.query.status;

      const fromValue =
        Array.isArray(req.query.from)
          ? req.query.from[0]
          : req.query.from;

      const toValue =
        Array.isArray(req.query.to)
          ? req.query.to[0]
          : req.query.to;

      const limitNum = Math.min(
        Math.max(
          Number.parseInt(
            String(limitValue || '50'),
            10
          ) || 50,
          1
        ),
        100
      );

      const pageNum = Math.max(
        Number.parseInt(
          String(pageValue || '1'),
          10
        ) || 1,
        1
      );

      const userId =
        toObjectId(user._id);

      const query: Record<string, any> = {
        userId,
      };

      if (
        typeof typeValue === 'string' &&
        typeValue.trim()
      ) {
        query.type =
          typeValue.trim();
      }

      if (
        typeof statusValue === 'string' &&
        statusValue.trim()
      ) {
        query.status =
          statusValue.trim();
      }

      if (
        typeof fromValue === 'string' ||
        typeof toValue === 'string'
      ) {
        const createdAt: Record<
          string,
          Date
        > = {};

        if (
          typeof fromValue === 'string'
        ) {
          const fromDate =
            new Date(fromValue);

          if (
            !Number.isNaN(
              fromDate.getTime()
            )
          ) {
            createdAt.$gte =
              fromDate;
          }
        }

        if (
          typeof toValue === 'string'
        ) {
          const toDate =
            new Date(toValue);

          if (
            !Number.isNaN(
              toDate.getTime()
            )
          ) {
            query.createdAt = createdAt;
          }

          if (
            !Number.isNaN(
              toDate.getTime()
            )
          ) {
            createdAt.$lte =
              toDate;

            query.createdAt =
              createdAt;
          }
        }

        if (
          Object.keys(createdAt).length > 0
        ) {
          query.createdAt =
            createdAt;
        }
      }

      const skip =
        (pageNum - 1) *
        limitNum;

      const [
        transactions,
        total,
      ] = await Promise.all([
        TransactionModel
          .find(query)
          .sort({
            createdAt: -1,
          })
          .skip(skip)
          .limit(limitNum)
          .lean()
          .exec(),

        TransactionModel
          .countDocuments(query)
          .exec(),
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
            ),
        },
      });
    } catch (error) {
      console.error(
        'Transaction history error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Unable to retrieve transaction history',
      });
    }
  }
);

// ============================================================
// DEPOSIT
// ============================================================

router.post(
  '/deposit',
  authenticate,
  checkResponsibleGambling,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const user = req.user;

      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Authentication required',
        });
      }

      const {
        amount,
        paymentMethod,
        paymentDetails,
      } = req.body as {
        amount?: unknown;
        paymentMethod?: unknown;
        paymentDetails?: Record<
          string,
          unknown
        >;
      };

      // --------------------------------------------------------
      // Validate amount
      // --------------------------------------------------------

      const minDeposit =
        envNumber(
          'DEPOSIT_MIN_AMOUNT',
          10
        );

      const maxDeposit =
        envNumber(
          'DEPOSIT_MAX_AMOUNT',
          100000
        );

      const depositAmount =
        positiveNumber(amount);

      if (depositAmount === null) {
        return res.status(400).json({
          success: false,
          message:
            'Please enter a valid deposit amount',
        });
      }

      if (
        depositAmount <
        minDeposit
      ) {
        return res.status(400).json({
          success: false,
          message:
            `Minimum deposit is ${minDeposit} ETB`,
        });
      }

      if (
        depositAmount >
        maxDeposit
      ) {
        return res.status(400).json({
          success: false,
          message:
            `Maximum deposit is ${maxDeposit} ETB`,
        });
      }

      if (
        typeof paymentMethod !==
          'string' ||
        !paymentMethod.trim()
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Payment method is required',
        });
      }

      const normalizedPaymentMethod =
        paymentMethod.trim();

      // --------------------------------------------------------
      // Wallet
      // --------------------------------------------------------

      const wallet =
        ensureWallet(user);

      // --------------------------------------------------------
      // Daily deposit limit
      // --------------------------------------------------------

      const todayStart =
        startOfToday();

      const userId =
        toObjectId(user._id);

      const todayDeposits =
        await TransactionModel
          .aggregate<{
            _id: null;
            total: number;
          }>([
            {
              $match: {
                userId,

                type:
                  TRANSACTION_TYPES.DEPOSIT,

                status:
                  TRANSACTION_STATUS.COMPLETED,

                createdAt: {
                  $gte:
                    todayStart,
                },
              },
            },

            {
              $group: {
                _id: null,

                total: {
                  $sum:
                    '$amount',
                },
              },
            },
          ])
          .exec();

      const depositedToday =
        Number(
          todayDeposits[0]?.total
        ) || 0;

      const dailyLimit =
        typeof user.getDepositLimit ===
        'function'
          ? Number(
              user.getDepositLimit()
            ) || 50000
          : Number(
              user.responsibleGambling
                ?.depositLimit
            ) || 50000;

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
            )} ETB remaining today.`,
        });
      }

      // --------------------------------------------------------
      // Reference
      // --------------------------------------------------------

      const paymentReference =
        `DEP_${Date.now()}_${Math.random()
          .toString(36)
          .substring(2, 10)
          .toUpperCase()}`;

      // --------------------------------------------------------
      // Create pending transaction
      // --------------------------------------------------------

      const transaction =
        new TransactionModel({
          userId,

          type:
            TRANSACTION_TYPES.DEPOSIT,

          amount:
            depositAmount,

          fee: 0,

          taxAmount: 0,

          netAmount:
            depositAmount,

          paymentMethod:
            normalizedPaymentMethod,

          paymentReference,

          paymentDetails:
            paymentDetails || {},

          previousBalance:
            wallet.balance || 0,

          previousBonusBalance:
            wallet.bonusBalance || 0,

          // DO NOT credit wallet here.
          newBalance:
            wallet.balance || 0,

          newBonusBalance:
            wallet.bonusBalance || 0,

          status:
            TRANSACTION_STATUS.PENDING,

          requiresApproval:
            true,

          ipAddress:
            req.ip,

          userAgent:
            req.headers[
              'user-agent'
            ],

          metadata: {
            mockPayment: true,

            awaitingAdminApproval:
              true,
          },
        });

      await transaction.save();

      // --------------------------------------------------------
      // Mock gateway
      // --------------------------------------------------------

      const paymentResult =
        await processDeposit({
          userId,

          amount:
            depositAmount,

          paymentMethod:
            normalizedPaymentMethod,

          paymentDetails:
            paymentDetails || {},

          reference:
            paymentReference,

          callbackUrl:
            `${process.env.BASE_URL || ''}/api/payments/callback`,
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
            'Payment could not be initialized',
        });
      }

      transaction.paymentGatewayReference =
        paymentResult.gatewayReference;

      transaction.requiresApproval =
        true;

      transaction.status =
        TRANSACTION_STATUS.PENDING;

      await transaction.save();

      // --------------------------------------------------------
      // IMPORTANT:
      // Wallet is NOT credited here.
      // Admin approval must credit it.
      // --------------------------------------------------------

      await sendNotification({
        userId,

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
            transaction._id,
        },
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
          wallet.balance || 0,
      });
    } catch (error) {
      console.error(
        'Deposit error:',
        error
      );

      return res.status(500).json({
        success: false,

        message:
          'Deposit failed',
      });
    }
  }
);

// ============================================================
// WITHDRAWAL
// ============================================================

router.post(
  '/withdraw',
  authenticate,
  checkResponsibleGambling,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const user = req.user;

      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Authentication required',
        });
      }

      const {
        amount,
        paymentMethod,
        paymentDetails,
      } = req.body as {
        amount?: unknown;
        paymentMethod?: unknown;
        paymentDetails?: Record<
          string,
          unknown
        >;
      };

      // --------------------------------------------------------
      // Validate amount
      // --------------------------------------------------------

      const minWithdraw =
        envNumber(
          'WITHDRAWAL_MIN_AMOUNT',
          50
        );

      const maxWithdraw =
        envNumber(
          'WITHDRAWAL_MAX_AMOUNT',
          50000
        );

      const withdrawAmount =
        positiveNumber(amount);

      if (withdrawAmount === null) {
        return res.status(400).json({
          success: false,
          message:
            'Please enter a valid withdrawal amount',
        });
      }

      if (
        withdrawAmount <
        minWithdraw
      ) {
        return res.status(400).json({
          success: false,
          message:
            `Minimum withdrawal is ${minWithdraw} ETB`,
        });
      }

      if (
        withdrawAmount >
        maxWithdraw
      ) {
        return res.status(400).json({
          success: false,
          message:
            `Maximum withdrawal per transaction is ${maxWithdraw} ETB`,
        });
      }

      if (
        typeof paymentMethod !==
          'string' ||
        !paymentMethod.trim()
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Payment method is required',
        });
      }

      const normalizedPaymentMethod =
        paymentMethod.trim();

      // --------------------------------------------------------
      // Wallet
      // --------------------------------------------------------

      const wallet =
        ensureWallet(user);

      const previousBalance =
        Number(
          wallet.balance
        ) || 0;

      const previousLockedBalance =
        Number(
          wallet.lockedBalance
        ) || 0;

      const availableBalance =
        Math.max(
          0,
          previousBalance -
            previousLockedBalance
        );

      if (
        availableBalance <
        withdrawAmount
      ) {
        return res.status(400).json({
          success: false,

          message:
            'Insufficient available balance',
        });
      }

      // --------------------------------------------------------
      // Daily withdrawal limit
      // --------------------------------------------------------

      const userId =
        toObjectId(user._id);

      const todayStart =
        startOfToday();

      const todayWithdrawals =
        await TransactionModel
          .aggregate<{
            _id: null;
            total: number;
          }>([
            {
              $match: {
                userId,

                type:
                  TRANSACTION_TYPES.WITHDRAWAL,

                status:
                  TRANSACTION_STATUS.COMPLETED,

                createdAt: {
                  $gte:
                    todayStart,
                },
              },
            },

            {
              $group: {
                _id: null,

                total: {
                  $sum:
                    '$amount',
                },
              },
            },
          ])
          .exec();

      const withdrawnToday =
        Number(
          todayWithdrawals[0]?.total
        ) || 0;

      const dailyLimit =
        envNumber(
          'WITHDRAWAL_DAILY_LIMIT',
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
            `Daily withdrawal limit of ${dailyLimit} ETB reached.`,
        });
      }

      // --------------------------------------------------------
      // Weekly withdrawal limit
      // --------------------------------------------------------

      const weekStart =
        startOfWeek();

      const weeklyWithdrawals =
        await TransactionModel
          .aggregate<{
            _id: null;
            total: number;
          }>([
            {
              $match: {
                userId,

                type:
                  TRANSACTION_TYPES.WITHDRAWAL,

                status:
                  TRANSACTION_STATUS.COMPLETED,

                createdAt: {
                  $gte:
                    weekStart,
                },
              },
            },

            {
              $group: {
                _id: null,

                total: {
                  $sum:
                    '$amount',
                },
              },
            },
          ])
          .exec();

      const withdrawnWeekly =
        Number(
          weeklyWithdrawals[0]?.total
        ) || 0;

      const weeklyLimit =
        envNumber(
          'WITHDRAWAL_WEEKLY_LIMIT',
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
            `Weekly withdrawal limit of ${weeklyLimit} ETB reached.`,
        });
      }

      // --------------------------------------------------------
      // VIP / approval
      // --------------------------------------------------------

      const vipLevel =
        Number(
          user.vip?.level
        ) || 0;

      const isVipLevelHigh =
        vipLevel >= 3;

      const requiresApproval =
        withdrawAmount > 10000 ||
        !isVipLevelHigh;

      // --------------------------------------------------------
      // Lock withdrawal amount
      // --------------------------------------------------------
      //
      // We remove it from available balance and place the
      // amount into lockedBalance.
      //
      // If approved:
      //   lockedBalance decreases.
      //
      // If rejected:
      //   balance is restored and lockedBalance decreases.
      //
      // --------------------------------------------------------

      wallet.lockedBalance =
        previousLockedBalance +
        withdrawAmount;

      wallet.balance =
        previousBalance -
        withdrawAmount;

      // --------------------------------------------------------
      // Reference
      // --------------------------------------------------------

      const paymentReference =
        `WDR_${Date.now()}_${Math.random()
          .toString(36)
          .substring(2, 10)
          .toUpperCase()}`;

      // --------------------------------------------------------
      // Create transaction
      // --------------------------------------------------------

      const transaction =
        new TransactionModel({
          userId,

          type:
            TRANSACTION_TYPES.WITHDRAWAL,

          amount:
            withdrawAmount,

          fee: 0,

          taxAmount: 0,

          netAmount:
            withdrawAmount,

          paymentMethod:
            normalizedPaymentMethod,

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
            ],

          metadata: {
            mockPayment: true,

            awaitingAdminApproval:
              requiresApproval,
          },
        });

      // --------------------------------------------------------
      // Save atomically at application level
      // --------------------------------------------------------

      await Promise.all([
        user.save(),
        transaction.save(),
      ]);

      await sendNotification({
        userId,

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
            paymentReference,

          transactionId:
            transaction._id,
        },
      });

      // --------------------------------------------------------
      // AUTO PROCESS
      // --------------------------------------------------------

      if (!requiresApproval) {
        const result =
          await processWithdrawal({
            userId,

            amount:
              withdrawAmount,

            paymentMethod:
              normalizedPaymentMethod,

            paymentDetails:
              paymentDetails || {},

            reference:
              paymentReference,
          });

        if (result.success) {
          wallet.lockedBalance =
            Math.max(
              0,
              Number(
                wallet.lockedBalance
              ) -
                withdrawAmount
            );

          wallet.totalWithdrawn =
            Number(
              wallet.totalWithdrawn
            ) +
            withdrawAmount;

          transaction.status =
            TRANSACTION_STATUS.COMPLETED;

          transaction.completedAt =
            new Date();

          transaction.paymentGatewayReference =
            result.gatewayReference;

          transaction.newBalance =
            wallet.balance;

          await Promise.all([
            user.save(),
            transaction.save(),
          ]);

          await sendNotification({
            userId,

            title:
              'Withdrawal Completed ✅',

            message:
              `Your withdrawal of ${withdrawAmount.toLocaleString()} ETB has been processed.`,

            type:
              'withdrawal_completed',

            data: {
              amount:
                withdrawAmount,

              transactionId:
                transaction._id,
            },
          });

          return res.json({
            success: true,

            message:
              'Withdrawal processed successfully!',

            transactionId:
              transaction._id,

            newBalance:
              wallet.balance,

            lockedBalance:
              wallet.lockedBalance,
          });
        }

        // ------------------------------------------------------
        // If gateway fails, restore the locked amount.
        // ------------------------------------------------------

        wallet.balance =
          Number(
            wallet.balance
          ) +
          withdrawAmount;

        wallet.lockedBalance =
          Math.max(
            0,
            Number(
              wallet.lockedBalance
            ) -
              withdrawAmount
          );

        transaction.status =
          TRANSACTION_STATUS.FAILED;

        transaction.failureReason =
          'Mock withdrawal gateway failed';

        transaction.newBalance =
          wallet.balance;

        await Promise.all([
          user.save(),
          transaction.save(),
        ]);

        return res.status(400).json({
          success: false,

          message:
            'Withdrawal could not be processed',
        });
      }

      // --------------------------------------------------------
      // Approval required
      // --------------------------------------------------------

      return res.json({
        success: true,

        message:
          'Withdrawal request submitted for review.',

        transactionId:
          transaction._id,

        requiresApproval,

        reference:
          paymentReference,

        amount:
          withdrawAmount,

        status:
          TRANSACTION_STATUS.PENDING,

        newBalance:
          wallet.balance,

        lockedBalance:
          wallet.lockedBalance,
      });
    } catch (error) {
      console.error(
        'Withdrawal error:',
        error
      );

      return res.status(500).json({
        success: false,

        message:
          'Withdrawal failed',
      });
    }
  }
);

// ============================================================
// WALLET SUMMARY
// ============================================================

router.get(
  '/summary',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const user = req.user;

      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Authentication required',
        });
      }

      const userId =
        toObjectId(user._id);

      const todayStart =
        startOfToday();

      const weekStart =
        startOfWeek();

      const monthStart =
        startOfMonth();

      // --------------------------------------------------------
      // Pipeline builder
      // --------------------------------------------------------

      const createStatsPipeline =
        (startDate: Date) => [
          {
            $match: {
              userId,

              status:
                TRANSACTION_STATUS.COMPLETED,

              createdAt: {
                $gte:
                  startDate,
              },
            },
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
                        TRANSACTION_TYPES.DEPOSIT,
                      ],
                    },

                    '$amount',

                    0,
                  ],
                },
              },

              withdrawals: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.WITHDRAWAL,
                      ],
                    },

                    '$amount',

                    0,
                  ],
                },
              },

              wins: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.BET_WIN,
                      ],
                    },

                    '$amount',

                    0,
                  ],
                },
              },

              losses: {
                $sum: {
                  $cond: [
                    {
                      $eq: [
                        '$type',
                        TRANSACTION_TYPES.BET_LOSS,
                      ],
                    },

                    '$amount',

                    0,
                  ],
                },
              },
            },
          },
        ];

      const [
        todayStats,
        weekStats,
        monthStats,
      ] = await Promise.all([
        TransactionModel
          .aggregate(
            createStatsPipeline(
              todayStart
            )
          )
          .exec(),

        TransactionModel
          .aggregate(
            createStatsPipeline(
              weekStart
            )
          )
          .exec(),

        TransactionModel
          .aggregate(
            createStatsPipeline(
              monthStart
            )
          )
          .exec(),
      ]);

      // --------------------------------------------------------
      // Format statistics
      // --------------------------------------------------------

      const formatStats =
        (result: any[]) => {
          const stats =
            result[0] || {};

          const deposits =
            Number(
              stats.deposits
            ) || 0;

          const withdrawals =
            Number(
              stats.withdrawals
            ) || 0;

          const wins =
            Number(
              stats.wins
            ) || 0;

          const losses =
            Number(
              stats.losses
            ) || 0;

          return {
            deposits,

            withdrawals,

            wins,

            losses,

            netProfit:
              wins - losses,
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
          ),
      });
    } catch (error) {
      console.error(
        'Wallet summary error:',
        error
      );

      return res.status(500).json({
        success: false,

        message:
          'Unable to retrieve wallet summary',
      });
    }
  }
);

// ============================================================
// EXPORT
// ============================================================

export default router;