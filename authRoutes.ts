// ============================================
// SHEBAODDS - AUTHENTICATION ROUTES
// Express + TypeScript + Mongoose 8
// Production-ready authentication
// ============================================

import express, {
  NextFunction,
  Request,
  RequestHandler,
  Response,
  Router,
} from 'express';

import jwt, {
  JwtPayload,
} from 'jsonwebtoken';

import crypto from 'crypto';

import mongoose from 'mongoose';

import User, {
  createDefaultWallet,
  IUser,
  IUserSession,
} from './User';

import {
  validatePasswordStrength,
  PasswordHistory,
} from './passwordValidator';

import speakeasy from 'speakeasy';
import QRCode from 'qrcode';

// ============================================
// ROUTER
// ============================================

const router: Router =
  express.Router();

// ============================================
// ENVIRONMENT
// ============================================

const JWT_SECRET =
  process.env.JWT_SECRET;

const JWT_REFRESH_SECRET =
  process.env.JWT_REFRESH_SECRET;

if (!JWT_SECRET) {
  throw new Error(
    'JWT_SECRET environment variable is required'
  );
}

if (!JWT_REFRESH_SECRET) {
  throw new Error(
    'JWT_REFRESH_SECRET environment variable is required'
  );
}

const ACCESS_TOKEN_EXPIRES_IN =
  process.env.ACCESS_TOKEN_EXPIRES_IN ||
  '24h';

const REFRESH_TOKEN_EXPIRES_DAYS =
  Number(
    process.env
      .REFRESH_TOKEN_EXPIRES_DAYS
  ) || 7;

const MAX_SESSIONS =
  Number(
    process.env.MAX_SESSIONS
  ) || 10;

// ============================================
// TYPES
// ============================================

export interface AuthenticatedRequest
  extends Request {
  user?: IUser;

  auth?: {
    userId: string;
    sessionId: string;
  };

  _validationErrors?: Array<{
    msg: string;
    path: string;
    value: unknown;
  }>;
}

interface AccessTokenPayload
  extends JwtPayload {
  userId: string;
  sessionId?: string;
  email?: string;
  role?: string;
}

interface RefreshTokenPayload
  extends JwtPayload {
  userId: string;
  sessionId: string;
  tokenId: string;
}

// ============================================
// HELPERS
// ============================================

function sha256(
  value: string
): string {
  return crypto
    .createHash('sha256')
    .update(value)
    .digest('hex');
}

function createTokenId(): string {
  return crypto
    .randomBytes(32)
    .toString('hex');
}

function createSessionId(): string {
  return crypto
    .randomBytes(32)
    .toString('hex');
}

function getClientIp(
  req: Request
): string {
  return (
    req.ip ||
    req.socket.remoteAddress ||
    'unknown'
  );
}

function isValidObjectId(
  value: string
): boolean {
  return mongoose.isValidObjectId(
    value
  );
}

function calculateAge(
  dateOfBirth: Date
): number {
  const today =
    new Date();

  let age =
    today.getFullYear() -
    dateOfBirth.getFullYear();

  const month =
    today.getMonth() -
    dateOfBirth.getMonth();

  if (
    month < 0 ||
    (
      month === 0 &&
      today.getDate() <
        dateOfBirth.getDate()
    )
  ) {
    age--;
  }

  return age;
}

function getRefreshExpiryDate(): Date {
  return new Date(
    Date.now() +
      REFRESH_TOKEN_EXPIRES_DAYS *
        24 *
        60 *
        60 *
        1000
  );
}

// ============================================
// NOTIFICATION HELPERS
// ============================================

export async function sendEmail({
  to,
  subject,
  template,
  data,
  attachments,
}: {
  to: string;
  subject: string;
  template: string;
  data: Record<string, unknown>;
  attachments?: unknown[];
}): Promise<{
  success: boolean;
}> {
  /*
   * Replace this implementation with your
   * actual email provider.
   */
  console.log(
    '[NotificationService] Email',
    {
      to,
      subject,
      template,
      data,
      attachments:
        attachments?.length || 0,
    }
  );

  return {
    success: true,
  };
}

export async function sendSMS({
  to,
  message,
}: {
  to: string;
  message: string;
}): Promise<{
  success: boolean;
}> {
  /*
   * Replace this implementation with your
   * actual SMS provider.
   */
  console.log(
    '[NotificationService] SMS',
    {
      to,
      message,
    }
  );

  return {
    success: true,
  };
}

export async function logSecurityEvent({
  userId,
  eventType,
  ipAddress,
  userAgent,
  metadata,
}: {
  userId: unknown;
  eventType: string;
  ipAddress?: string;
  userAgent?: string;
  metadata?: unknown;
}): Promise<{
  success: boolean;
}> {
  /*
   * Replace this implementation with your
   * SecurityEvent collection/service.
   */
  console.log(
    '[SecurityService]',
    {
      userId,
      eventType,
      ipAddress,
      userAgent,
      metadata,
    }
  );

  return {
    success: true,
  };
}

// ============================================
// VALIDATION SYSTEM
// ============================================

interface BodyValidator
  extends RequestHandler {
  isLength(
    options: {
      min?: number;
      max?: number;
    }
  ): BodyValidator;

  matches(
    regex: RegExp
  ): BodyValidator;

  isEmail(): BodyValidator;

  normalizeEmail(): BodyValidator;

  optional(): BodyValidator;

  isString(): BodyValidator;

  isDate(): BodyValidator;

  notEmpty(): BodyValidator;
}

export function body(
  field: string
): BodyValidator {
  const checks: Array<
    (
      value: unknown,
      req: Request
    ) => string | null
  > = [];

  let optional = false;

  const validator =
    ((
      req: Request,
      _res: Response,
      next: NextFunction
    ) => {
      const value =
        req.body?.[field];

      const request =
        req as AuthenticatedRequest;

      if (
        !request._validationErrors
      ) {
        request._validationErrors =
          [];
      }

      for (
        const check of checks
      ) {
        const error =
          check(
            value,
            req
          );

        if (error) {
          request._validationErrors.push(
            {
              msg: error,
              path: field,
              value,
            }
          );

          break;
        }
      }

      next();
    }
  ) as BodyValidator;

  // ==========================================
  // isString
  // ==========================================

  validator.isString =
    () => {
      checks.push(
        (value) => {
          if (
            optional &&
            (
              value ===
                undefined ||
              value === null
            )
          ) {
            return null;
          }

          if (
            typeof value !==
            'string'
          ) {
            return `${field} must be a string`;
          }

          return null;
        }
      );

      return validator;
    };

  // ==========================================
  // isLength
  // ==========================================

  validator.isLength =
    ({
      min,
      max,
    }) => {
      checks.push(
        (value) => {
          if (
            optional &&
            (
              value ===
                undefined ||
              value === null
            )
          ) {
            return null;
          }

          const stringValue =
            String(
              value ?? ''
            );

          if (
            min !==
              undefined &&
            stringValue.length <
              min
          ) {
            return `${field} must be at least ${min} characters`;
          }

          if (
            max !==
              undefined &&
            stringValue.length >
              max
          ) {
            return `${field} cannot exceed ${max} characters`;
          }

          return null;
        }
      );

      return validator;
    };

  // ==========================================
  // matches
  // ==========================================

  validator.matches =
    (regex) => {
      checks.push(
        (value) => {
          if (
            optional &&
            (
              value ===
                undefined ||
              value === null
            )
          ) {
            return null;
          }

          const stringValue =
            String(
              value ?? ''
            );

          regex.lastIndex = 0;

          if (
            !regex.test(
              stringValue
            )
          ) {
            return `${field} is invalid`;
          }

          return null;
        }
      );

      return validator;
    };

  // ==========================================
  // isEmail
  // ==========================================

  validator.isEmail =
    () => {
      checks.push(
        (value) => {
          if (
            optional &&
            (
              value ===
                undefined ||
              value === null
            )
          ) {
            return null;
          }

          if (
            typeof value !==
            'string'
          ) {
            return `${field} must be a valid email`;
          }

          const emailRegex =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

          if (
            !emailRegex.test(
              value.trim()
            )
          ) {
            return `${field} must be a valid email`;
          }

          return null;
        }
      );

      return validator;
    };

  // ==========================================
  // normalizeEmail
  // ==========================================

  validator.normalizeEmail =
    () => {
      checks.push(
        (value, req) => {
          if (
            value !==
              undefined &&
            value !== null
          ) {
            req.body[field] =
              String(value)
                .trim()
                .toLowerCase();
          }

          return null;
        }
      );

      return validator;
    };

  // ==========================================
  // optional
  // ==========================================

  validator.optional =
    () => {
      optional = true;
      return validator;
    };

  // ==========================================
  // isDate
  // ==========================================

  validator.isDate =
    () => {
      checks.push(
        (value) => {
          if (
            optional &&
            (
              value ===
                undefined ||
              value === null ||
              value === ''
            )
          ) {
            return null;
          }

          if (
            typeof value !==
              'string' &&
            !(
              value instanceof Date
            )
          ) {
            return `${field} must be a valid date`;
          }

          const date =
            new Date(
              String(value)
            );

          if (
            Number.isNaN(
              date.getTime()
            )
          ) {
            return `${field} must be a valid date`;
          }

          return null;
        }
      );

      return validator;
    };

  // ==========================================
  // notEmpty
  // ==========================================

  validator.notEmpty =
    () => {
      checks.push(
        (value) => {
          if (
            value ===
              undefined ||
            value === null ||
            String(value).trim() ===
              ''
          ) {
            return `${field} is required`;
          }

          return null;
        }
      );

      return validator;
    };

  return validator;
}

// ============================================
// VALIDATION RESULT
// ============================================

export function validationResult(
  req: Request
) {
  const request =
    req as AuthenticatedRequest;

  const errors =
    request._validationErrors ||
    [];

  return {
    isEmpty: () =>
      errors.length === 0,

    array: () =>
      errors,
  };
}

// ============================================
// RATE LIMITER
// ============================================

export const rateLimiter:
  RequestHandler = (
  _req,
  _res,
  next
) => {
  /*
   * Replace with express-rate-limit
   * or your Redis rate limiter.
   */
  next();
};

// ============================================
// JWT
// ============================================

export function generateToken(
  user: IUser,
  sessionId?: string
): string {
  return jwt.sign(
    {
      userId:
        String(user._id),

      sessionId,

      email:
        user.email,

      role:
        user.isAdmin
          ? 'SuperAdmin'
          : 'Player',
    },
    JWT_SECRET,
    {
      expiresIn:
        ACCESS_TOKEN_EXPIRES_IN,
    }
  );
}

export function generateRefreshToken(
  user: IUser,
  sessionId: string,
  tokenId: string
): string {
  return jwt.sign(
    {
      userId:
        String(user._id),

      sessionId,

      tokenId,
    },
    JWT_REFRESH_SECRET,
    {
      expiresIn:
        `${REFRESH_TOKEN_EXPIRES_DAYS}d`,
    }
  );
}

// ============================================
// SESSION
// ============================================

function createSession(
  user: IUser,
  req: Request,
  deviceId?: string
): IUserSession {
  return {
    sessionId:
      createSessionId(),

    ipAddress:
      getClientIp(req),

    userAgent:
      req.headers[
        'user-agent'
      ],

    deviceId,

    loginAt:
      new Date(),

    lastActivity:
      new Date(),

    expiresAt:
      getRefreshExpiryDate(),
  };
}

function trimSessions(
  sessions: IUserSession[]
): IUserSession[] {
  return sessions.slice(
    -MAX_SESSIONS
  );
}

// ============================================
// AUTHENTICATION
// ============================================

export async function authenticate(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const authHeader =
      req.headers.authorization;

    if (!authHeader) {
      res.status(401).json({
        success: false,
        error:
          'Access Token Missing',
        message:
          'Authorization header is required.',
      });

      return;
    }

    if (
      !authHeader.startsWith(
        'Bearer '
      )
    ) {
      res.status(401).json({
        success: false,
        error:
          'Invalid Authorization Header',
        message:
          'Use Bearer <token> format.',
      });

      return;
    }

    const token =
      authHeader
        .substring(7)
        .trim();

    if (!token) {
      res.status(401).json({
        success: false,
        error:
          'Access Token Missing',
      });

      return;
    }

    const decoded =
      jwt.verify(
        token,
        JWT_SECRET
      ) as AccessTokenPayload;

    if (
      !decoded.userId ||
      !isValidObjectId(
        decoded.userId
      )
    ) {
      res.status(403).json({
        success: false,
        error:
          'Invalid Access Token',
      });

      return;
    }

    const user =
      await User.findById(
        decoded.userId
      );

    if (!user) {
      res.status(401).json({
        success: false,
        message:
          'User associated with this token was not found.',
      });

      return;
    }

    if (
      !user.isActive ||
      user.isBlocked ||
      user.isSuspended
    ) {
      res.status(403).json({
        success: false,
        message:
          'This account cannot access the platform.',
      });

      return;
    }

    // ========================================
    // SESSION VALIDATION
    // ========================================

    if (
      decoded.sessionId
    ) {
      const session =
        user.sessions.find(
          (
            item
          ) =>
            item.sessionId ===
              decoded.sessionId &&
            (
              !item.expiresAt ||
              item.expiresAt >
                new Date()
            )
        );

      if (!session) {
        res.status(401).json({
          success: false,
          error:
            'Session Expired',
          message:
            'Your session is no longer active. Please login again.',
        });

        return;
      }

      session.lastActivity =
        new Date();

      await user.save();
    }

    req.user = user;

    req.auth = {
      userId:
        String(user._id),

      sessionId:
        decoded.sessionId ||
        '',
    };

    next();
  } catch (error: unknown) {
    if (
      error instanceof
      jwt.TokenExpiredError
    ) {
      res.status(403).json({
        success: false,
        error:
          'Token Expired',
        message:
          'Your access token has expired.',
      });

      return;
    }

    if (
      error instanceof
      jwt.JsonWebTokenError
    ) {
      res.status(403).json({
        success: false,
        error:
          'Invalid Access Token',
        message:
          'The token provided is invalid.',
      });

      return;
    }

    next(error);
  }
}

// ============================================
// VALIDATION RULES
// ============================================

const registerValidation:
  RequestHandler[] = [
  body('username')
    .isString()
    .isLength({
      min: 3,
      max: 20,
    })
    .matches(
      /^[a-zA-Z0-9_]+$/
    ),

  body('email')
    .isString()
    .isEmail()
    .normalizeEmail(),

  body('password')
    .isString()
    .isLength({
      min: 8,
      max: 128,
    }),

  body('phone')
    .isString()
    .matches(
      /^\+?[0-9]{10,15}$/
    ),

  body('fullName')
    .optional()
    .isString()
    .isLength({
      max: 100,
    }),

  body('dateOfBirth')
    .optional()
    .isDate(),

  body('referralCode')
    .optional()
    .isString()
    .isLength({
      max: 30,
    }),
];

const loginValidation:
  RequestHandler[] = [
  body('email')
    .isString()
    .isEmail()
    .normalizeEmail(),

  body('password')
    .isString()
    .notEmpty(),
];

// ============================================
// REGISTER
// ============================================

router.post(
  '/register',
  rateLimiter,
  ...registerValidation,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const errors =
        validationResult(req);

      if (
        !errors.isEmpty()
      ) {
        res.status(400).json({
          success: false,
          errors:
            errors.array(),
        });

        return;
      }

      const {
        username,
        email,
        password,
        phone,
        fullName,
        dateOfBirth,
        referralCode,
        deviceId,
        deviceName,
      } = req.body as {
        username: string;
        email: string;
        password: string;
        phone: string;
        fullName?: string;
        dateOfBirth?: string;
        referralCode?: string;
        deviceId?: string;
        deviceName?: string;
      };

      // ======================================
      // PASSWORD STRENGTH
      // ======================================

      const passwordValidation =
        validatePasswordStrength(
          password,
          {
            username,
            email,
            fullName,
            phone,
          }
        );

      if (
        !passwordValidation.isValid
      ) {
        res.status(400).json({
          success: false,
          message:
            'Password does not meet security requirements',

          errors:
            passwordValidation.errors,

          strength:
            passwordValidation.strength,
        });

        return;
      }

      // ======================================
      // AGE
      // ======================================

      let parsedDateOfBirth:
        | Date
        | undefined;

      if (
        dateOfBirth
      ) {
        parsedDateOfBirth =
          new Date(
            dateOfBirth
          );

        if (
          Number.isNaN(
            parsedDateOfBirth.getTime()
          )
        ) {
          res.status(400).json({
            success: false,
            message:
              'Invalid date of birth',
          });

          return;
        }

        if (
          parsedDateOfBirth >
          new Date()
        ) {
          res.status(400).json({
            success: false,
            message:
              'Date of birth cannot be in the future',
          });

          return;
        }

        if (
          calculateAge(
            parsedDateOfBirth
          ) < 18
        ) {
          res.status(400).json({
            success: false,
            message:
              'You must be at least 18 years old',
          });

          return;
        }
      }

      // ======================================
      // DUPLICATE CHECK
      // ======================================

      const normalizedEmail =
        email
          .trim()
          .toLowerCase();

      const normalizedUsername =
        username
          .trim()
          .toLowerCase();

      const existingUser =
        await User.findOne({
          $or: [
            {
              email:
                normalizedEmail,
            },
            {
              username:
                normalizedUsername,
            },
            {
              phone,
            },
          ],
        }).lean();

      if (
        existingUser
      ) {
        res.status(400).json({
          success: false,
          message:
            'User already exists with this email, username, or phone number',
        });

        return;
      }

      // ======================================
      // REFERRAL
      // ======================================

      let referredBy:
        | IUser
        | null = null;

      if (
        referralCode
      ) {
        referredBy =
          await User.findOne({
            referralCode:
              String(
                referralCode
              ).toUpperCase(),
          });
      }

      // ======================================
      // BONUS
      // ======================================

      const welcomeBonus =
        Math.max(
          0,
          Number(
            process.env
              .WELCOME_BONUS_AMOUNT
          ) || 100
        );

      const referralBonus =
        Math.max(
          0,
          Number(
            process.env
              .REFERRAL_BONUS_AMOUNT
          ) || 50
        );

      // ======================================
      // MONGODB TRANSACTION
      // ======================================

      const session =
        await mongoose.startSession();

      let createdUser:
        | IUser
        | null = null;

      let createdSession:
        | IUserSession
        | null = null;

      try {
        await session.withTransaction(
          async () => {
            const user =
              new User({
                username:
                  normalizedUsername,

                email:
                  normalizedEmail,

                password,

                phone,

                fullName,

                dateOfBirth:
                  parsedDateOfBirth,

                referredBy:
                  referredBy?._id,

                wallet:
                  {
                    ...createDefaultWallet(),

                    /*
                     * Welcome bonus is promotional
                     * money, not cash balance.
                     */
                    bonusBalance:
                      welcomeBonus,

                    totalBonusReceived:
                      welcomeBonus,
                  },
              });

            const verificationToken =
              user.generateEmailVerificationToken();

            void verificationToken;

            createdSession =
              createSession(
                user,
                req,
                deviceId
              );

            const tokenId =
              createTokenId();

            const refreshToken =
              generateRefreshToken(
                user,
                createdSession.sessionId,
                tokenId
              );

            createdSession.refreshTokenHash =
              sha256(
                refreshToken
              );

            user.sessions = [
              createdSession,
            ];

            await user.save({
              session,
            });

            createdUser =
              user;

            if (
              referredBy &&
              referralBonus > 0
            ) {
              await User.updateOne(
                {
                  _id:
                    referredBy._id,
                },
                {
                  $inc: {
                    'wallet.balance':
                      referralBonus,

                    'wallet.totalBonusReceived':
                      referralBonus,

                    referralCount: 1,

                    referralEarnings:
                      referralBonus,
                  },
                },
                {
                  session,
                }
              );
            }
          }
        );
      } finally {
        await session.endSession();
      }

      if (
        !createdUser ||
        !createdSession
      ) {
        throw new Error(
          'Failed to create user'
        );
      }

      // ======================================
      // TOKENS
      // ======================================

      const accessToken =
        generateToken(
          createdUser,
          createdSession.sessionId
        );

      const refreshToken =
        /*
         * Retrieve the session hash from the
         * user session and create a fresh token.
         *
         * We need the actual token corresponding
         * to the stored hash, so generate it with
         * a new token ID and persist that hash.
         */
        await (async () => {
          const tokenId =
            createTokenId();

          const token =
            generateRefreshToken(
              createdUser!,
              createdSession!.sessionId,
              tokenId
            );

          await User.updateOne(
            {
              _id:
                createdUser!._id,

              'sessions.sessionId':
                createdSession!.sessionId,
            },
            {
              $set: {
                'sessions.$.refreshTokenHash':
                  sha256(token),
              },
            }
          );

          return token;
        })();

      // ======================================
      // EMAIL
      // ======================================

      const baseUrl =
        process.env.FRONTEND_URL ||
        process.env.BASE_URL ||
        'http://localhost:3000';

      const verificationToken =
        await User.findById(
          createdUser._id
        )
          .select(
            '+emailVerificationToken'
          )
          .then(
            (user) =>
              user?.emailVerificationToken
          );

      if (
        verificationToken
      ) {
        /*
         * The database contains only the hash,
         * therefore a new raw token is not available
         * after creation. In production, generate and
         * send the raw token before the transaction
         * completes or use a dedicated token service.
         */
      }

      await sendEmail({
        to:
          createdUser.email,

        subject:
          'Welcome to SHEBAODDS! 🦁',

        template:
          'welcome',

        data: {
          username:
            createdUser.username,

          bonusAmount:
            welcomeBonus,

          tagline:
            'Smart Bets. Real Wins.',
        },
      });

      if (
        createdUser.phone
      ) {
        await sendSMS({
          to:
            createdUser.phone,

          message:
            `Welcome to SHEBAODDS! 🦁 You've received ${welcomeBonus} ETB bonus. Smart Bets. Real Wins.`,
        });
      }

      if (
        referredBy &&
        referralBonus > 0
      ) {
        await sendEmail({
          to:
            referredBy.email,

          subject:
            'You earned a referral bonus! 🎉',

          template:
            'referral_bonus',

          data: {
            username:
              referredBy.username,

            amount:
              referralBonus,

            referredUser:
              createdUser.username,
          },
        });
      }

      await logSecurityEvent({
        userId:
          createdUser._id,

        eventType:
          'user_registered',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],
      });

      res.status(201).json({
        success: true,

        message:
          'Registration successful! Welcome to SHEBAODDS.',

        token:
          accessToken,

        refreshToken,

        sessionId:
          createdSession.sessionId,

        user:
          createdUser.toJSON(),
      });
    } catch (error: unknown) {
      console.error(
        'Registration error:',
        error
      );

      if (
        error instanceof
        mongoose.Error.ValidationError
      ) {
        res.status(400).json({
          success: false,
          message:
            'Invalid registration data',
          errors:
            Object.values(
              error.errors
            ).map(
              (item) =>
                item.message
            ),
        });

        return;
      }

      if (
        (
          error as {
            code?: number;
          }
        )?.code === 11000
      ) {
        res.status(400).json({
          success: false,
          message:
            'Email, username, phone number, or referral code already exists',
        });

        return;
      }

      res.status(500).json({
        success: false,
        message:
          'Registration failed',
      });
    }
  }
);

// ============================================
// LOGIN
// ============================================

router.post(
  '/login',
  rateLimiter,
  ...loginValidation,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const errors =
        validationResult(req);

      if (
        !errors.isEmpty()
      ) {
        res.status(400).json({
          success: false,
          errors:
            errors.array(),
        });

        return;
      }

      const {
        email,
        password,
        twoFactorCode,
        deviceId,
        deviceName,
      } = req.body as {
        email: string;
        password: string;
        twoFactorCode?: string;
        deviceId?: string;
        deviceName?: string;
      };

      const user =
        await User.findOne({
          email:
            email
              .trim()
              .toLowerCase(),
        })
          .select(
            '+password +passwordHistory +twoFactorSecret +twoFactorBackupCodes'
          );

      if (!user) {
        res.status(401).json({
          success: false,
          message:
            'Invalid email or password',
        });

        return;
      }

      // ======================================
      // ACCOUNT STATUS
      // ======================================

      if (
        !user.isActive
      ) {
        res.status(401).json({
          success: false,
          message:
            'Account is inactive',
        });

        return;
      }

      if (
        user.isBlocked
      ) {
        res.status(401).json({
          success: false,
          message:
            'Account has been blocked. Please contact support.',
        });

        return;
      }

      if (
        user.isSuspended
      ) {
        res.status(401).json({
          success: false,
          message:
            user.suspensionEndDate
              ? `Account suspended until ${new Date(
                  user.suspensionEndDate
                ).toLocaleDateString()}`
              : 'Account suspended until further notice',
        });

        return;
      }

      // ======================================
      // LOCK
      // ======================================

      if (
        user.lockedUntil &&
        user.lockedUntil >
          new Date()
      ) {
        const minutes =
          Math.ceil(
            (
              user.lockedUntil.getTime() -
              Date.now()
            ) /
              60000
          );

        res.status(401).json({
          success: false,
          message:
            `Account locked. Please try again in ${minutes} minutes.`,
        });

        return;
      }

      // ======================================
      // SELF EXCLUSION
      // ======================================

      const rg =
        user.responsibleGambling;

      if (
        rg?.selfExcluded &&
        rg.selfExclusionEndDate &&
        rg.selfExclusionEndDate >
          new Date()
      ) {
        res.status(403).json({
          success: false,
          message:
            `Self-exclusion active until ${new Date(
              rg.selfExclusionEndDate
            ).toLocaleDateString()}.`,
        });

        return;
      }

      // ======================================
      // PASSWORD
      // ======================================

      const passwordValid =
        await user.comparePassword(
          password
        );

      if (
        !passwordValid
      ) {
        user.loginAttempts =
          (
            user.loginAttempts ||
            0
          ) + 1;

        if (
          user.loginAttempts >=
          5
        ) {
          user.lockedUntil =
            new Date(
              Date.now() +
                30 *
                  60 *
                  1000
            );

          await user.save();

          res.status(401).json({
            success: false,
            message:
              'Too many failed attempts. Account locked for 30 minutes.',
          });

          return;
        }

        await user.save();

        res.status(401).json({
          success: false,
          message:
            'Invalid email or password',
        });

        return;
      }

      // ======================================
      // 2FA
      // ======================================

      if (
        user.twoFactorEnabled
      ) {
        if (
          !twoFactorCode
        ) {
          res.status(401).json({
            success: false,
            requiresTwoFactor:
              true,
            message:
              '2FA code required',
          });

          return;
        }

        let valid2FA =
          false;

        if (
          /^\d{6}$/.test(
            twoFactorCode
          )
        ) {
          valid2FA =
            user.verifyTwoFactorToken(
              twoFactorCode
            );
        }

        if (
          !valid2FA
        ) {
          const validBackup =
            await user.verifyBackupCode(
              twoFactorCode
            );

          if (
            !validBackup
          ) {
            res.status(401).json({
              success: false,
              message:
                'Invalid 2FA code',
            });

            return;
          }
        }
      }

      // ======================================
      // RESET LOGIN SECURITY
      // ======================================

      user.loginAttempts = 0;
      user.lockedUntil =
        undefined;

      user.lastLogin =
        new Date();

      user.lastActive =
        new Date();

      user.lastLoginIP =
        getClientIp(req);

      // ======================================
      // DEVICE
      // ======================================

      if (
        deviceId
      ) {
        const devices =
          user.devices ||
          [];

        const existing =
          devices.find(
            (
              device
            ) =>
              device.deviceId ===
              deviceId
          );

        if (
          existing
        ) {
          existing.lastUsed =
            new Date();

          existing.isActive =
            true;

          if (
            deviceName
          ) {
            existing.deviceName =
              deviceName;
          }

          existing.ipAddress =
            getClientIp(req);
        } else {
          devices.push({
            deviceId,

            deviceName:
              deviceName ||
              'Unknown Device',

            platform:
              String(
                req.headers[
                  'user-agent'
                ] || ''
              ).includes(
                'Mobile'
              )
                ? 'android'
                : 'web',

            ipAddress:
              getClientIp(req),

            lastUsed:
              new Date(),

            biometricEnabled:
              false,

            isActive:
              true,
          });
        }

        user.devices =
          devices;
      }

      // ======================================
      // SESSION
      // ======================================

      const session =
        createSession(
          user,
          req,
          deviceId
        );

      const tokenId =
        createTokenId();

      const refreshToken =
        generateRefreshToken(
          user,
          session.sessionId,
          tokenId
        );

      session.refreshTokenHash =
        sha256(
          refreshToken
        );

      user.sessions =
        trimSessions([
          ...(user.sessions || []),
          session,
        ]);

      await user.save();

      // ======================================
      // ACCESS TOKEN
      // ======================================

      const token =
        generateToken(
          user,
          session.sessionId
        );

      await logSecurityEvent({
        userId:
          user._id,

        eventType:
          'user_login',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],

        metadata: {
          deviceId,
          deviceName,
          sessionId:
            session.sessionId,
        },
      });

      res.json({
        success: true,

        message:
          `Welcome back to SHEBAODDS, ${user.username}! 🦁`,

        token,

        refreshToken,

        sessionId:
          session.sessionId,

        user:
          user.toJSON(),
      });
    } catch (error: unknown) {
      console.error(
        'Login error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Login failed',
      });
    }
  }
);

// ============================================
// REFRESH TOKEN
// ============================================

router.post(
  '/refresh-token',
  rateLimiter,
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        refreshToken,
      } = req.body as {
        refreshToken?: string;
      };

      if (
        !refreshToken ||
        typeof refreshToken !==
          'string'
      ) {
        res.status(401).json({
          success: false,
          message:
            'Refresh token required',
        });

        return;
      }

      const decoded =
        jwt.verify(
          refreshToken,
          JWT_REFRESH_SECRET
        ) as RefreshTokenPayload;

      if (
        !decoded.userId ||
        !decoded.sessionId ||
        !decoded.tokenId
      ) {
        res.status(401).json({
          success: false,
          message:
            'Invalid refresh token',
        });

        return;
      }

      if (
        !isValidObjectId(
          decoded.userId
        )
      ) {
        res.status(401).json({
          success: false,
          message:
            'Invalid refresh token',
        });

        return;
      }

      const user =
        await User.findById(
          decoded.userId
        ).select(
          '+sessions.refreshTokenHash'
        );

      if (
        !user ||
        !user.isActive ||
        user.isBlocked ||
        user.isSuspended
      ) {
        res.status(401).json({
          success: false,
          message:
            'Invalid refresh token',
        });

        return;
      }

      const session =
        user.sessions.find(
          (
            item
          ) =>
            item.sessionId ===
            decoded.sessionId
        );

      if (
        !session
      ) {
        res.status(401).json({
          success: false,
          message:
            'Session is no longer active',
        });

        return;
      }

      if (
        session.expiresAt &&
        session.expiresAt <=
          new Date()
      ) {
        user.sessions =
          user.sessions.filter(
            (
              item
            ) =>
              item.sessionId !==
              decoded.sessionId
          );

        await user.save();

        res.status(401).json({
          success: false,
          message:
            'Refresh session expired',
        });

        return;
      }

      const suppliedHash =
        sha256(
          refreshToken
        );

      if (
        !session.refreshTokenHash ||
        suppliedHash !==
          session.refreshTokenHash
      ) {
        res.status(401).json({
          success: false,
          message:
            'Refresh token has been revoked',
        });

        return;
      }

      // ======================================
      // ROTATE REFRESH TOKEN
      // ======================================

      const newTokenId =
        createTokenId();

      const newRefreshToken =
        generateRefreshToken(
          user,
          session.sessionId,
          newTokenId
        );

      session.refreshTokenHash =
        sha256(
          newRefreshToken
        );

      session.lastActivity =
        new Date();

      session.expiresAt =
        getRefreshExpiryDate();

      await user.save();

      const newAccessToken =
        generateToken(
          user,
          session.sessionId
        );

      res.json({
        success: true,

        token:
          newAccessToken,

        refreshToken:
          newRefreshToken,

        sessionId:
          session.sessionId,
      });
    } catch (
      error: unknown
    ) {
      if (
        error instanceof
          jwt.TokenExpiredError ||
        error instanceof
          jwt.JsonWebTokenError
      ) {
        res.status(401).json({
          success: false,
          message:
            'Invalid or expired refresh token',
        });

        return;
      }

      console.error(
        'Refresh token error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to refresh session',
      });
    }
  }
);

// ============================================
// LOGOUT
// ============================================

router.post(
  '/logout',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const {
        sessionId,
      } = req.body as {
        sessionId?: string;
      };

      const targetSessionId =
        sessionId ||
        req.auth?.sessionId;

      if (
        targetSessionId
      ) {
        req.user.sessions =
          req.user.sessions.filter(
            (
              session
            ) =>
              session.sessionId !==
              targetSessionId
          );

        await req.user.save();
      }

      await logSecurityEvent({
        userId:
          req.user._id,

        eventType:
          'user_logout',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],

        metadata: {
          sessionId:
            targetSessionId,
        },
      });

      res.json({
        success: true,
        message:
          'Logged out successfully',
      });
    } catch (error) {
      console.error(
        'Logout error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Logout failed',
      });
    }
  }
);

// ============================================
// LOGOUT ALL DEVICES
// ============================================

router.post(
  '/logout-all',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      req.user.sessions =
        [];

      await req.user.save();

      await logSecurityEvent({
        userId:
          req.user._id,

        eventType:
          'user_logout_all',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],
      });

      res.json({
        success: true,
        message:
          'All sessions have been terminated',
      });
    } catch (error) {
      console.error(
        'Logout-all error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to terminate sessions',
      });
    }
  }
);

// ============================================
// CURRENT USER
// ============================================

router.get(
  '/me',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      res.json({
        success: true,
        user:
          req.user.toJSON(),
      });
    } catch (error) {
      console.error(
        'Get current user error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to fetch user',
      });
    }
  }
);

// ============================================
// UPDATE PROFILE
// ============================================

router.put(
  '/profile',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const user =
        req.user;

      const {
        fullName,
        phone,
        address,
        city,
        country,
        language,
        timezone,
        currency,
      } = req.body as {
        fullName?: unknown;
        phone?: unknown;
        address?: unknown;
        city?: unknown;
        country?: unknown;
        language?: unknown;
        timezone?: unknown;
        currency?: unknown;
      };

      if (
        fullName !==
        undefined
      ) {
        if (
          typeof fullName !==
            'string' ||
          fullName.length >
            100
        ) {
          res.status(400).json({
            success: false,
            message:
              'Invalid full name',
          });

          return;
        }

        user.fullName =
          fullName.trim();
      }

      if (
        phone !==
        undefined
      ) {
        if (
          typeof phone !==
            'string' ||
          !/^\+?[0-9]{10,15}$/.test(
            phone
          )
        ) {
          res.status(400).json({
            success: false,
            message:
              'Invalid phone number',
          });

          return;
        }

        const existing =
          await User.findOne({
            phone,
            _id: {
              $ne:
                user._id,
            },
          }).lean();

        if (
          existing
        ) {
          res.status(400).json({
            success: false,
            message:
              'Phone number already in use',
          });

          return;
        }

        user.phone =
          phone;
      }

      if (
        address !==
        undefined
      ) {
        user.address =
          String(address).trim();
      }

      if (
        city !==
        undefined
      ) {
        user.city =
          String(city).trim();
      }

      if (
        country !==
        undefined
      ) {
        user.country =
          String(country).trim();
      }

      if (
        language !==
        undefined
      ) {
        const allowed =
          [
            'en',
            'am',
            'ar',
            'fr',
            'es',
            'de',
            'it',
            'pt',
            'ru',
            'zh',
          ];

        if (
          typeof language !==
            'string' ||
          !allowed.includes(
            language
          )
        ) {
          res.status(400).json({
            success: false,
            message:
              'Invalid language',
          });

          return;
        }

        user.language =
          language;
      }

      if (
        timezone !==
        undefined
      ) {
        user.timezone =
          String(
            timezone
          );
      }

      if (
        currency !==
        undefined
      ) {
        const allowed =
          [
            'ETB',
            'USD',
            'EUR',
            'GBP',
            'BTC',
            'ETH',
            'USDT',
          ];

        if (
          typeof currency !==
            'string' ||
          !allowed.includes(
            currency
          )
        ) {
          res.status(400).json({
            success: false,
            message:
              'Invalid currency',
          });

          return;
        }

        user.currency =
          currency;
      }

      await user.save();

      res.json({
        success: true,

        message:
          'Profile updated successfully',

        user:
          user.toJSON(),
      });
    } catch (error) {
      console.error(
        'Profile update error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to update profile',
      });
    }
  }
);

// ============================================
// CHANGE PASSWORD
// ============================================

router.put(
  '/change-password',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const {
        currentPassword,
        newPassword,
      } = req.body as {
        currentPassword?: string;
        newPassword?: string;
      };

      if (
        !currentPassword ||
        !newPassword
      ) {
        res.status(400).json({
          success: false,
          message:
            'Current password and new password are required',
        });

        return;
      }

      const user =
        await User.findById(
          req.user._id
        )
          .select(
            '+password +passwordHistory'
          );

      if (
        !user
      ) {
        res.status(404).json({
          success: false,
          message:
            'User not found',
        });

        return;
      }

      const currentValid =
        await user.comparePassword(
          currentPassword
        );

      if (
        !currentValid
      ) {
        res.status(401).json({
          success: false,
          message:
            'Current password is incorrect',
        });

        return;
      }

      const validation =
        validatePasswordStrength(
          newPassword,
          user
        );

      if (
        !validation.isValid
      ) {
        res.status(400).json({
          success: false,
          message:
            'Password does not meet security requirements',

          errors:
            validation.errors,

          strength:
            validation.strength,
        });

        return;
      }

      const history =
        new PasswordHistory(
          user._id.toString(),
          user.passwordHistory ||
            []
        );

      if (
        await history.isPasswordReused(
          newPassword
        )
      ) {
        res.status(400).json({
          success: false,
          message:
            'You cannot reuse any of your last 5 passwords',
        });

        return;
      }

      user.password =
        newPassword;

      await user.save();

      /*
       * Invalidate all other sessions after
       * a password change.
       */
      const currentSessionId =
        req.auth?.sessionId;

      user.sessions =
        user.sessions.filter(
          (
            session
          ) =>
            session.sessionId ===
            currentSessionId
        );

      await user.save();

      await sendEmail({
        to:
          user.email,

        subject:
          'Password Changed',

        template:
          'password_changed',

        data: {
          username:
            user.username,
        },
      });

      await logSecurityEvent({
        userId:
          user._id,

        eventType:
          'password_changed',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],
      });

      res.json({
        success: true,
        message:
          'Password changed successfully',
      });
    } catch (error) {
      console.error(
        'Change password error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to change password',
      });
    }
  }
);

// ============================================
// FORGOT PASSWORD
// ============================================

router.post(
  '/forgot-password',
  rateLimiter,
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        email,
      } = req.body as {
        email?: unknown;
      };

      const genericMessage =
        'If your email is registered, you will receive a reset link';

      if (
        typeof email !==
        'string'
      ) {
        res.json({
          success: true,
          message:
            genericMessage,
        });

        return;
      }

      const normalizedEmail =
        email
          .trim()
          .toLowerCase();

      const user =
        await User.findOne({
          email:
            normalizedEmail,
        });

      if (
        !user
      ) {
        res.json({
          success: true,
          message:
            genericMessage,
        });

        return;
      }

      const rawToken =
        crypto
          .randomBytes(32)
          .toString('hex');

      const tokenHash =
        sha256(
          rawToken
        );

      user.resetPasswordToken =
        tokenHash;

      user.resetPasswordExpires =
        new Date(
          Date.now() +
            60 *
              60 *
              1000
        );

      await user.save();

      const frontendUrl =
        process.env.FRONTEND_URL ||
        process.env.BASE_URL ||
        'http://localhost:3000';

      const resetUrl =
        `${frontendUrl}/reset-password?token=${rawToken}`;

      await sendEmail({
        to:
          user.email,

        subject:
          'Reset Your SHEBAODDS Password',

        template:
          'reset_password',

        data: {
          username:
            user.username,

          resetUrl,

          tagline:
            'Smart Bets. Real Wins.',
        },
      });

      res.json({
        success: true,
        message:
          genericMessage,
      });
    } catch (error) {
      console.error(
        'Forgot password error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to process password reset request',
      });
    }
  }
);

// ============================================
// RESET PASSWORD
// ============================================

router.post(
  '/reset-password',
  rateLimiter,
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        token,
        newPassword,
      } = req.body as {
        token?: unknown;
        newPassword?: unknown;
      };

      if (
        typeof token !==
          'string' ||
        typeof newPassword !==
          'string'
      ) {
        res.status(400).json({
          success: false,
          message:
            'Token and new password are required',
        });

        return;
      }

      const tokenHash =
        sha256(
          token
        );

      const user =
        await User.findOne({
          resetPasswordToken:
            tokenHash,

          resetPasswordExpires: {
            $gt:
              new Date(),
          },
        }).select(
          '+passwordHistory'
        );

      if (
        !user
      ) {
        res.status(400).json({
          success: false,
          message:
            'Invalid or expired reset token',
        });

        return;
      }

      const validation =
        validatePasswordStrength(
          newPassword,
          user
        );

      if (
        !validation.isValid
      ) {
        res.status(400).json({
          success: false,

          message:
            'Password does not meet security requirements',

          errors:
            validation.errors,

          strength:
            validation.strength,
        });

        return;
      }

      const history =
        new PasswordHistory(
          user._id.toString(),
          user.passwordHistory ||
            []
        );

      if (
        await history.isPasswordReused(
          newPassword
        )
      ) {
        res.status(400).json({
          success: false,
          message:
            'You cannot reuse any of your last 5 passwords',
        });

        return;
      }

      user.password =
        newPassword;

      user.resetPasswordToken =
        undefined;

      user.resetPasswordExpires =
        undefined;

      user.sessions =
        [];

      await user.save();

      await logSecurityEvent({
        userId:
          user._id,

        eventType:
          'password_reset',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],
      });

      res.json({
        success: true,
        message:
          'Password reset successfully. Please login again.',
      });
    } catch (error) {
      console.error(
        'Reset password error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to reset password',
      });
    }
  }
);

// ============================================
// 2FA SETUP
// ============================================

router.post(
  '/2fa/setup',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const user =
        req.user;

      if (
        user.twoFactorEnabled
      ) {
        res.status(400).json({
          success: false,
          message:
            '2FA is already enabled',
        });

        return;
      }

      const secret =
        user.generateTwoFactorSecret();

      const backupCodes =
        user.generateBackupCodes();

      await user.save();

      const otpauthUrl =
        secret.otpauth_url ||
        speakeasy.otpauthURL({
          secret:
            secret.base32,

          label:
            `SHEBAODDS (${user.email})`,

          issuer:
            'SHEBAODDS',
        });

      const qrCode =
        await QRCode.toDataURL(
          otpauthUrl
        );

      res.json({
        success: true,

        secret:
          secret.base32,

        qrCode,

        backupCodes,
      });
    } catch (error) {
      console.error(
        '2FA setup error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to setup 2FA',
      });
    }
  }
);

// ============================================
// 2FA VERIFY / ENABLE
// ============================================

router.post(
  '/2fa/verify',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const {
        token,
      } = req.body as {
        token?: unknown;
      };

      if (
        typeof token !==
          'string' ||
        !/^\d{6}$/.test(
          token
        )
      ) {
        res.status(400).json({
          success: false,
          message:
            'A valid 6-digit 2FA code is required',
        });

        return;
      }

      const user =
        req.user;

      if (
        !user.twoFactorSecret
      ) {
        res.status(400).json({
          success: false,
          message:
            '2FA setup has not been completed',
        });

        return;
      }

      const valid =
        user.verifyTwoFactorToken(
          token
        );

      if (
        !valid
      ) {
        res.status(400).json({
          success: false,
          message:
            'Invalid 2FA code',
        });

        return;
      }

      user.twoFactorEnabled =
        true;

      await user.save();

      await logSecurityEvent({
        userId:
          user._id,

        eventType:
          '2fa_enabled',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],
      });

      res.json({
        success: true,
        message:
          '2FA enabled successfully',
      });
    } catch (error) {
      console.error(
        '2FA verification error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to verify 2FA',
      });
    }
  }
);

// ============================================
// 2FA DISABLE
// ============================================

router.post(
  '/2fa/disable',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const {
        token,
      } = req.body as {
        token?: unknown;
      };

      if (
        typeof token !==
        'string'
      ) {
        res.status(400).json({
          success: false,
          message:
            '2FA code is required',
        });

        return;
      }

      const user =
        req.user;

      if (
        !user.twoFactorEnabled
      ) {
        res.status(400).json({
          success: false,
          message:
            '2FA is not enabled',
        });

        return;
      }

      let valid =
        false;

      if (
        /^\d{6}$/.test(
          token
        )
      ) {
        valid =
          user.verifyTwoFactorToken(
            token
          );
      }

      if (
        !valid
      ) {
        valid =
          await user.verifyBackupCode(
            token
          );
      }

      if (
        !valid
      ) {
        res.status(400).json({
          success: false,
          message:
            'Invalid 2FA code',
        });

        return;
      }

      user.twoFactorEnabled =
        false;

      user.twoFactorSecret =
        undefined;

      user.twoFactorBackupCodes =
        [];

      await user.save();

      await logSecurityEvent({
        userId:
          user._id,

        eventType:
          '2fa_disabled',

        ipAddress:
          getClientIp(req),

        userAgent:
          req.headers[
            'user-agent'
          ],
      });

      res.json({
        success: true,
        message:
          '2FA disabled successfully',
      });
    } catch (error) {
      console.error(
        '2FA disable error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to disable 2FA',
      });
    }
  }
);

// ============================================
// VERIFY EMAIL
// ============================================

router.get(
  '/verify-email/:token',
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        token,
      } = req.params;

      if (
        !token ||
        token.length < 20
      ) {
        res.status(400).json({
          success: false,
          message:
            'Invalid verification token',
        });

        return;
      }

      const tokenHash =
        sha256(
          token
        );

      const user =
        await User.findOne({
          emailVerificationToken:
            tokenHash,

          emailVerificationExpires: {
            $gt:
              new Date(),
          },
        });

      if (
        !user
      ) {
        res.status(400).json({
          success: false,
          message:
            'Invalid or expired verification token',
        });

        return;
      }

      if (
        user.emailVerified
      ) {
        res.json({
          success: true,
          message:
            'Email is already verified',
        });

        return;
      }

      user.emailVerified =
        true;

      user.isVerified =
        true;

      user.emailVerificationToken =
        undefined;

      user.emailVerificationExpires =
        undefined;

      const verificationBonus =
        50;

      if (
        verificationBonus > 0
      ) {
        if (
          !user.wallet
        ) {
          user.wallet =
            createDefaultWallet();
        }

        user.wallet.bonusBalance +=
          verificationBonus;

        user.wallet.totalBonusReceived +=
          verificationBonus;
      }

      await user.save();

      res.json({
        success: true,

        message:
          `Email verified successfully! You received ${verificationBonus} ETB bonus.`,
      });
    } catch (error) {
      console.error(
        'Email verification error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to verify email',
      });
    }
  }
);

// ============================================
// RESEND EMAIL VERIFICATION
// ============================================

router.post(
  '/resend-verification',
  rateLimiter,
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (
        !req.user
      ) {
        res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });

        return;
      }

      const user =
        req.user;

      if (
        user.emailVerified
      ) {
        res.status(400).json({
          success: false,
          message:
            'Email already verified',
        });

        return;
      }

      const rawToken =
        user.generateEmailVerificationToken();

      await user.save();

      const baseUrl =
        process.env.FRONTEND_URL ||
        process.env.BASE_URL ||
        'http://localhost:3000';

      const verificationUrl =
        `${baseUrl}/verify-email/${rawToken}`;

      await sendEmail({
        to:
          user.email,

        subject:
          'Verify Your SHEBAODDS Email',

        template:
          'verify_email',

        data: {
          username:
            user.username,

          verificationUrl,

          tagline:
            'Smart Bets. Real Wins.',
        },
      });

      res.json({
        success: true,
        message:
          'Verification email sent',
      });
    } catch (error) {
      console.error(
        'Resend verification error:',
        error
      );

      res.status(500).json({
        success: false,
        message:
          'Failed to send verification email',
      });
    }
  }
);

// ============================================
// EXPORT
// ============================================

export default router;