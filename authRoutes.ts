// ============================================
// SHEBAODDS - AUTHENTICATION ROUTES
// TypeScript / Express / Mongoose
// ============================================

import express, {
  Request,
  Response,
  NextFunction,
  Router,
} from 'express';

import jwt from 'jsonwebtoken';
import crypto from 'crypto';

import User from './User';
import {
  validatePasswordStrength,
  PasswordHistory,
} from './passwordValidator';

// Third-party packages without reliable TS declarations
const speakeasy: any = require('speakeasy');
const QRCode: any = require('qrcode');

const router: Router = express.Router();

// ============================================================
// ENVIRONMENT
// ============================================================

const JWT_SECRET =
  process.env.JWT_SECRET ||
  'sheba_odds_jwt_secret_high_entropy_fallback_token_99812';

const JWT_REFRESH_SECRET =
  process.env.JWT_REFRESH_SECRET ||
  'sheba_odds_jwt_refresh_secret_99812';

// ============================================================
// TYPES
// ============================================================

interface AuthenticatedRequest extends Request {
  user?: any;
  _validationErrors?: Array<{
    msg: string;
    path: string;
    value: any;
  }>;
}

interface BodyValidator {
  (req: Request, res: Response, next: NextFunction): void;

  isLength(options: {
    min?: number;
    max?: number;
  }): BodyValidator;

  matches(regex: RegExp): BodyValidator;

  isEmail(): BodyValidator;

  normalizeEmail(): BodyValidator;

  optional(): BodyValidator;

  isString(): BodyValidator;

  isDate(): BodyValidator;

  notEmpty(): BodyValidator;
}

// ============================================================
// CUSTOM BODY VALIDATOR
// ============================================================

export function body(field: string): BodyValidator {
  const checks: Array<(value: any) => string | null> = [];

  let optional = false;

  const validator = ((req: Request, res: Response, next: NextFunction) => {
    const value = req.body?.[field];

    const request = req as AuthenticatedRequest;

    if (!request._validationErrors) {
      request._validationErrors = [];
    }

    for (const check of checks) {
      const error = check(value);

      if (error) {
        request._validationErrors.push({
          msg: error,
          path: field,
          value,
        });

        break;
      }
    }

    next();
  }) as BodyValidator;

  // ----------------------------------------------------------
  // isLength
  // ----------------------------------------------------------

  validator.isLength = ({
    min,
    max,
  }: {
    min?: number;
    max?: number;
  }) => {
    checks.push((value: any) => {
      if (
        optional &&
        (value === undefined || value === null)
      ) {
        return null;
      }

      const stringValue = String(value ?? '');

      if (
        min !== undefined &&
        stringValue.length < min
      ) {
        return `${field} must be at least ${min} characters`;
      }

      if (
        max !== undefined &&
        stringValue.length > max
      ) {
        return `${field} cannot exceed ${max} characters`;
      }

      return null;
    });

    return validator;
  };

  // ----------------------------------------------------------
  // matches
  // ----------------------------------------------------------

  validator.matches = (regex: RegExp) => {
    checks.push((value: any) => {
      if (
        optional &&
        (value === undefined || value === null)
      ) {
        return null;
      }

      const stringValue = String(value ?? '');

      // Reset global regex state if necessary
      regex.lastIndex = 0;

      if (!regex.test(stringValue)) {
        return `${field} is invalid`;
      }

      return null;
    });

    return validator;
  };

  // ----------------------------------------------------------
  // isEmail
  // ----------------------------------------------------------

  validator.isEmail = () => {
    checks.push((value: any) => {
      if (
        optional &&
        (value === undefined || value === null)
      ) {
        return null;
      }

      const stringValue = String(value ?? '').trim();

      const emailRegex =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (!emailRegex.test(stringValue)) {
        return `${field} must be a valid email`;
      }

      return null;
    });

    return validator;
  };

  // ----------------------------------------------------------
  // normalizeEmail
  // ----------------------------------------------------------

  validator.normalizeEmail = () => {
    checks.push((value: any) => {
      if (
        value !== undefined &&
        value !== null
      ) {
        req.body[field] = String(value)
          .trim()
          .toLowerCase();
      }

      return null;
    });

    return validator;
  };

  // ----------------------------------------------------------
  // optional
  // ----------------------------------------------------------

  validator.optional = () => {
    optional = true;
    return validator;
  };

  // ----------------------------------------------------------
  // isString
  // ----------------------------------------------------------

  validator.isString = () => {
    checks.push((value: any) => {
      if (
        optional &&
        (value === undefined || value === null)
      ) {
        return null;
      }

      if (typeof value !== 'string') {
        return `${field} must be a string`;
      }

      return null;
    });

    return validator;
  };

  // ----------------------------------------------------------
  // isDate
  // ----------------------------------------------------------

  validator.isDate = () => {
    checks.push((value: any) => {
      if (
        optional &&
        (value === undefined || value === null)
      ) {
        return null;
      }

      if (
        typeof value !== 'string' &&
        !(value instanceof Date)
      ) {
        return `${field} must be a valid date`;
      }

      const timestamp = Date.parse(
        String(value)
      );

      if (Number.isNaN(timestamp)) {
        return `${field} must be a valid date`;
      }

      return null;
    });

    return validator;
  };

  // ----------------------------------------------------------
  // notEmpty
  // ----------------------------------------------------------

  validator.notEmpty = () => {
    checks.push((value: any) => {
      if (
        value === undefined ||
        value === null ||
        String(value).trim() === ''
      ) {
        return `${field} is required`;
      }

      return null;
    });

    return validator;
  };

  return validator;
}

// ============================================================
// VALIDATION RESULT
// ============================================================

export function validationResult(
  req: Request
) {
  const request = req as AuthenticatedRequest;

  return {
    isEmpty: () =>
      !request._validationErrors ||
      request._validationErrors.length === 0,

    array: () =>
      request._validationErrors || [],
  };
}

// ============================================================
// NOTIFICATION HELPERS
// ============================================================

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
  data: any;
  attachments?: any[];
}) {
  console.log(
    `[NotificationService] Email -> ${to}`,
    {
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
}) {
  console.log(
    `[NotificationService] SMS -> ${to}`,
    message
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
  userId: any;
  eventType: string;
  ipAddress?: string;
  userAgent?: string;
  metadata?: any;
}) {
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

// ============================================================
// RATE LIMITER
// ============================================================

export function rateLimiter(
  req: Request,
  res: Response,
  next: NextFunction
) {
  next();
}

// ============================================================
// AUTHENTICATION MIDDLEWARE
// ============================================================

export async function authenticate(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
) {
  try {
    const authHeader =
      req.headers.authorization;

    if (!authHeader) {
      return res.status(401).json({
        success: false,
        error: 'Access Token Missing',
        message:
          'Authorization header is required.',
      });
    }

    if (
      !authHeader.startsWith('Bearer ')
    ) {
      return res.status(401).json({
        success: false,
        error: 'Invalid Authorization Header',
        message:
          'Use Bearer <token> format.',
      });
    }

    const token =
      authHeader.substring(7).trim();

    if (!token) {
      return res.status(401).json({
        success: false,
        error: 'Access Token Missing',
        message:
          'Authentication bearer token is required.',
      });
    }

    const decoded = jwt.verify(
      token,
      JWT_SECRET
    ) as {
      userId: string;
      email?: string;
      role?: string;
    };

    if (!decoded?.userId) {
      return res.status(403).json({
        success: false,
        error: 'Invalid Access Token',
        message: 'Token payload is invalid.',
      });
    }

    const user =
      await User.findById(decoded.userId);

    if (!user) {
      return res.status(401).json({
        success: false,
        message:
          'User associated with this token was not found.',
      });
    }

    if (
      user.isBlocked ||
      user.isSuspended
    ) {
      return res.status(403).json({
        success: false,
        message:
          'This account cannot access the platform.',
      });
    }

    req.user = user;

    next();
  } catch (error: any) {
    if (
      error?.name === 'TokenExpiredError'
    ) {
      return res.status(403).json({
        success: false,
        error: 'Token Expired',
        message:
          'Your access token has expired. Please refresh your session.',
      });
    }

    if (
      error?.name === 'JsonWebTokenError'
    ) {
      return res.status(403).json({
        success: false,
        error: 'Invalid Access Token',
        message:
          'The token provided is invalid.',
      });
    }

    next(error);
  }
}

// ============================================================
// TOKEN GENERATION
// ============================================================

export function generateToken(
  user: any
): string {
  return jwt.sign(
    {
      userId: String(user._id),
      email: user.email,
      role: user.isAdmin
        ? 'SuperAdmin'
        : 'Player',
    },
    JWT_SECRET,
    {
      expiresIn: '24h',
    }
  );
}

export function generateRefreshToken(
  user: any
): string {
  return jwt.sign(
    {
      userId: String(user._id),
    },
    JWT_REFRESH_SECRET,
    {
      expiresIn: '7d',
    }
  );
}

// ============================================================
// VALIDATION RULES
// ============================================================

const registerValidation = [
  body('username')
    .isString()
    .isLength({
      min: 3,
      max: 20,
    })
    .matches(/^[a-zA-Z0-9_]+$/),

  body('email')
    .isString()
    .isEmail()
    .normalizeEmail(),

  body('password')
    .isString()
    .isLength({
      min: 8,
    }),

  body('phone')
    .isString()
    .matches(/^\+?[0-9]{10,15}$/),

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
    .isString(),
];

const loginValidation = [
  body('email')
    .isString()
    .isEmail()
    .normalizeEmail(),

  body('password')
    .isString()
    .notEmpty(),
];

// ============================================================
// REGISTER
// ============================================================

router.post(
  '/register',
  registerValidation,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const errors =
        validationResult(req);

      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          errors: errors.array(),
        });
      }

      const {
        username,
        email,
        password,
        phone,
        fullName,
        dateOfBirth,
        referralCode,
      } = req.body;

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

      if (!passwordValidation.isValid) {
        return res.status(400).json({
          success: false,
          message:
            'Password does not meet security requirements',
          errors:
            passwordValidation.errors,
          strength:
            passwordValidation.strength,
        });
      }

      const existingUser =
        await User.findOne({
          $or: [
            {
              email:
                String(email).toLowerCase(),
            },
            {
              username:
                String(username).toLowerCase(),
            },
            {
              phone,
            },
          ],
        });

      if (existingUser) {
        return res.status(400).json({
          success: false,
          message:
            'User already exists with this email, username, or phone number',
        });
      }

      // --------------------------------------------------------
      // AGE CHECK
      // --------------------------------------------------------

      if (dateOfBirth) {
        const birthDate =
          new Date(dateOfBirth);

        const age = Math.floor(
          (
            Date.now() -
            birthDate.getTime()
          ) /
            (
              365.25 *
              24 *
              60 *
              60 *
              1000
            )
        );

        if (age < 18) {
          return res.status(400).json({
            success: false,
            message:
              'You must be at least 18 years old',
          });
        }
      }

      // --------------------------------------------------------
      // REFERRAL
      // --------------------------------------------------------

      let referredByUser: any = null;

      if (referralCode) {
        referredByUser =
          await User.findOne({
            referralCode:
              String(
                referralCode
              ).toUpperCase(),
          });
      }

      // --------------------------------------------------------
      // WELCOME BONUS
      // --------------------------------------------------------

      const welcomeBonus =
        Number(
          process.env.WELCOME_BONUS_AMOUNT
        ) || 100;

      // --------------------------------------------------------
      // CREATE USER
      // --------------------------------------------------------

      const user = new User({
        username:
          String(username).toLowerCase(),

        email:
          String(email).toLowerCase(),

        password,

        phone,

        fullName,

        dateOfBirth,

        referredBy:
          referredByUser?._id,

        wallet: {
          balance: welcomeBonus,
        },
      });

      await user.save();

      // --------------------------------------------------------
      // WELCOME EMAIL
      // --------------------------------------------------------

      await sendEmail({
        to: user.email,
        subject:
          'Welcome to SHEBAODDS! 🦁',
        template: 'welcome',
        data: {
          username: user.username,
          bonusAmount: welcomeBonus,
          tagline:
            'Smart Bets. Real Wins.',
        },
      });

      // --------------------------------------------------------
      // WELCOME SMS
      // --------------------------------------------------------

      if (user.phone) {
        await sendSMS({
          to: user.phone,
          message:
            `Welcome to SHEBAODDS! 🦁 You've received ${welcomeBonus} ETB bonus. Smart Bets. Real Wins.`,
        });
      }

      // --------------------------------------------------------
      // REFERRAL BONUS
      // --------------------------------------------------------

      if (referredByUser) {
        const referralBonus =
          Number(
            process.env
              .REFERRAL_BONUS_AMOUNT
          ) || 50;

        if (!referredByUser.wallet) {
          referredByUser.wallet = {
            balance: 0,
          };
        }

        referredByUser.wallet.balance =
          Number(
            referredByUser.wallet.balance
          ) + referralBonus;

        referredByUser.referralCount =
          Number(
            referredByUser.referralCount || 0
          ) + 1;

        referredByUser.referralEarnings =
          Number(
            referredByUser.referralEarnings ||
              0
          ) + referralBonus;

        await referredByUser.save();

        await sendEmail({
          to: referredByUser.email,
          subject:
            'You earned a referral bonus! 🎉',
          template: 'referral_bonus',
          data: {
            username:
              referredByUser.username,
            amount: referralBonus,
            referredUser:
              user.username,
          },
        });
      }

      const token =
        generateToken(user);

      const refreshToken =
        generateRefreshToken(user);

      await logSecurityEvent({
        userId: user._id,
        eventType:
          'user_registered',
        ipAddress: req.ip,
        userAgent:
          req.headers['user-agent'],
      });

      return res.status(201).json({
        success: true,
        message:
          'Registration successful! Welcome to SHEBAODDS.',
        token,
        refreshToken,
        user: user.toJSON(),
      });
    } catch (error: any) {
      console.error(
        'Registration error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Registration failed',
        error:
          process.env.NODE_ENV ===
          'production'
            ? undefined
            : error?.message,
      });
    }
  }
);

// ============================================================
// LOGIN
// ============================================================

router.post(
  '/login',
  loginValidation,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const errors =
        validationResult(req);

      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          errors: errors.array(),
        });
      }

      const {
        email,
        password,
        twoFactorCode,
        deviceId,
        deviceName,
      } = req.body;

      const user =
        await User.findOne({
          email:
            String(email).toLowerCase(),
        }).select(
          '+password +twoFactorSecret +twoFactorBackupCodes'
        );

      if (!user) {
        return res.status(401).json({
          success: false,
          message:
            'Invalid email or password',
        });
      }

      // --------------------------------------------------------
      // ACCOUNT LOCK
      // --------------------------------------------------------

      if (
        user.lockedUntil &&
        user.lockedUntil > new Date()
      ) {
        const remainingMinutes =
          Math.ceil(
            (
              user.lockedUntil.getTime() -
              Date.now()
            ) / 60000
          );

        return res.status(401).json({
          success: false,
          message:
            `Account locked. Please try again in ${remainingMinutes} minutes.`,
        });
      }

      // --------------------------------------------------------
      // ACCOUNT STATUS
      // --------------------------------------------------------

      if (
        user.isBlocked ||
        user.isSuspended
      ) {
        return res.status(401).json({
          success: false,
          message:
            user.isSuspended
              ? `Account suspended until ${
                  user.suspensionEndDate
                    ? new Date(
                        user.suspensionEndDate
                      ).toLocaleDateString()
                    : 'further notice'
                }`
              : 'Account has been blocked. Please contact support.',
        });
      }

      // --------------------------------------------------------
      // SELF EXCLUSION
      // --------------------------------------------------------

      const selfExcluded =
        user.responsibleGambling
          ?.selfExcluded;

      const exclusionEnd =
        user.responsibleGambling
          ?.selfExclusionEndDate;

      if (
        selfExcluded &&
        exclusionEnd &&
        new Date(exclusionEnd) >
          new Date()
      ) {
        return res.status(403).json({
          success: false,
          message:
            `Self-exclusion active until ${new Date(
              exclusionEnd
            ).toLocaleDateString()}. Please contact support.`,
        });
      }

      // --------------------------------------------------------
      // PASSWORD
      // --------------------------------------------------------

      const passwordValid =
        await user.comparePassword(
          password
        );

      if (!passwordValid) {
        user.loginAttempts =
          Number(
            user.loginAttempts || 0
          ) + 1;

        if (user.loginAttempts >= 5) {
          user.lockedUntil =
            new Date(
              Date.now() +
                30 * 60 * 1000
            );

          await user.save();

          return res.status(401).json({
            success: false,
            message:
              'Too many failed attempts. Account locked for 30 minutes.',
          });
        }

        await user.save();

        return res.status(401).json({
          success: false,
          message:
            'Invalid email or password',
        });
      }

      // --------------------------------------------------------
      // RESET LOGIN ATTEMPTS
      // --------------------------------------------------------

      user.loginAttempts = 0;
      user.lockedUntil = undefined;

      // --------------------------------------------------------
      // TWO FACTOR AUTHENTICATION
      // --------------------------------------------------------

      if (user.twoFactorEnabled) {
        if (!twoFactorCode) {
          return res.status(401).json({
            success: false,
            requiresTwoFactor: true,
            message:
              '2FA code required',
          });
        }

        let valid2FA = false;

        try {
          valid2FA =
            Boolean(
              user.verifyTwoFactorToken(
                twoFactorCode
              )
            );
        } catch {
          valid2FA = false;
        }

        if (!valid2FA) {
          let validBackup = false;

          try {
            validBackup =
              Boolean(
                await user.verifyBackupCode(
                  twoFactorCode
                )
              );
          } catch {
            validBackup = false;
          }

          if (!validBackup) {
            return res.status(401).json({
              success: false,
              message:
                'Invalid 2FA code',
            });
          }
        }
      }

      // --------------------------------------------------------
      // LOGIN INFORMATION
      // --------------------------------------------------------

      user.lastLogin =
        new Date();

      user.lastActive =
        new Date();

      user.lastLoginIP =
        req.ip;

      // --------------------------------------------------------
      // DEVICE
      // --------------------------------------------------------

      if (deviceId) {
        const devices =
          user.devices || [];

        const existingDevice =
          devices.find(
            (device: any) =>
              device.deviceId ===
              deviceId
          );

        if (existingDevice) {
          existingDevice.lastUsed =
            new Date();

          if (deviceName) {
            existingDevice.deviceName =
              deviceName;
          }
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
              ).includes('Mobile')
                ? 'mobile'
                : 'web',

            ipAddress:
              req.ip,

            lastUsed:
              new Date(),

            biometricEnabled:
              false,

            isActive:
              true,
          });
        }

        user.devices = devices;
      }

      // --------------------------------------------------------
      // SESSION
      // --------------------------------------------------------

      const sessionId =
        crypto
          .randomBytes(32)
          .toString('hex');

      const sessions =
        user.sessions || [];

      sessions.push({
        sessionId,

        ipAddress:
          req.ip,

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
          new Date(
            Date.now() +
              7 *
                24 *
                60 *
                60 *
                1000
          ),
      });

      // Keep only the latest 10 sessions
      user.sessions =
        sessions.slice(-10);

      await user.save();

      // --------------------------------------------------------
      // TOKENS
      // --------------------------------------------------------

      const token =
        generateToken(user);

      const refreshToken =
        generateRefreshToken(user);

      await logSecurityEvent({
        userId: user._id,
        eventType:
          'user_login',
        ipAddress: req.ip,
        userAgent:
          req.headers[
            'user-agent'
          ],
        metadata: {
          deviceId,
          deviceName,
        },
      });

      return res.json({
        success: true,
        message:
          `Welcome back to SHEBAODDS, ${user.username}! 🦁`,
        token,
        refreshToken,
        sessionId,
        user: user.toJSON(),
      });
    } catch (error: any) {
      console.error(
        'Login error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Login failed',
        error:
          process.env.NODE_ENV ===
          'production'
            ? undefined
            : error?.message,
      });
    }
  }
);

// ============================================================
// REFRESH TOKEN
// ============================================================

router.post(
  '/refresh-token',
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        refreshToken,
      } = req.body;

      if (!refreshToken) {
        return res.status(401).json({
          success: false,
          message:
            'Refresh token required',
        });
      }

      const decoded =
        jwt.verify(
          refreshToken,
          JWT_REFRESH_SECRET
        ) as {
          userId: string;
        };

      if (!decoded?.userId) {
        return res.status(401).json({
          success: false,
          message:
            'Invalid refresh token',
        });
      }

      const user =
        await User.findById(
          decoded.userId
        );

      if (!user) {
        return res.status(401).json({
          success: false,
          message:
            'Invalid refresh token',
        });
      }

      if (!user.isActive) {
        return res.status(401).json({
          success: false,
          message:
            'User account is inactive',
        });
      }

      const newToken =
        generateToken(user);

      const newRefreshToken =
        generateRefreshToken(user);

      return res.json({
        success: true,
        token: newToken,
        refreshToken:
          newRefreshToken,
      });
    } catch {
      return res.status(401).json({
        success: false,
        message:
          'Invalid or expired refresh token',
      });
    }
  }
);

// ============================================================
// LOGOUT
// ============================================================

router.post(
  '/logout',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      const {
        sessionId,
      } = req.body;

      if (
        req.user &&
        sessionId
      ) {
        await User.findByIdAndUpdate(
          req.user._id,
          {
            $pull: {
              sessions: {
                sessionId,
              },
            },
          }
        );
      }

      if (req.user) {
        await logSecurityEvent({
          userId:
            req.user._id,
          eventType:
            'user_logout',
          ipAddress:
            req.ip,
          userAgent:
            req.headers[
              'user-agent'
            ],
        });
      }

      return res.json({
        success: true,
        message:
          'Logged out successfully',
      });
    } catch {
      return res.status(500).json({
        success: false,
        message:
          'Logout failed',
      });
    }
  }
);

// ============================================================
// GET CURRENT USER
// ============================================================

router.get(
  '/me',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const user =
        await User.findById(
          req.user._id
        );

      if (!user) {
        return res.status(404).json({
          success: false,
          message:
            'User not found',
        });
      }

      return res.json({
        success: true,
        user: user.toJSON(),
      });
    } catch {
      return res.status(500).json({
        success: false,
        message:
          'Failed to fetch user',
      });
    }
  }
);

// ============================================================
// UPDATE PROFILE
// ============================================================

router.put(
  '/profile',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const {
        fullName,
        phone,
        address,
        city,
        country,
        language,
        timezone,
        currency,
      } = req.body;

      const user =
        req.user;

      if (
        fullName !== undefined
      ) {
        user.fullName =
          fullName;
      }

      if (
        phone !== undefined
      ) {
        const existingUser =
          await User.findOne({
            phone,
            _id: {
              $ne: user._id,
            },
          });

        if (existingUser) {
          return res.status(400).json({
            success: false,
            message:
              'Phone number already in use',
          });
        }

        user.phone =
          phone;
      }

      if (
        address !== undefined
      ) {
        user.address =
          address;
      }

      if (
        city !== undefined
      ) {
        user.city =
          city;
      }

      if (
        country !== undefined
      ) {
        user.country =
          country;
      }

      if (
        language !== undefined
      ) {
        user.language =
          language;
      }

      if (
        timezone !== undefined
      ) {
        user.timezone =
          timezone;
      }

      if (
        currency !== undefined
      ) {
        user.currency =
          currency;
      }

      await user.save();

      return res.json({
        success: true,
        message:
          'Profile updated successfully',
        user:
          user.toJSON(),
      });
    } catch (error: any) {
      console.error(
        'Profile update error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to update profile',
      });
    }
  }
);

// ============================================================
// CHANGE PASSWORD
// ============================================================

router.put(
  '/change-password',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const {
        currentPassword,
        newPassword,
      } = req.body;

      if (
        !currentPassword ||
        !newPassword
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Current password and new password are required',
        });
      }

      const user =
        await User.findById(
          req.user._id
        ).select(
          '+password +passwordHistory'
        );

      if (!user) {
        return res.status(404).json({
          success: false,
          message:
            'User not found',
        });
      }

      const isMatch =
        await user.comparePassword(
          currentPassword
        );

      if (!isMatch) {
        return res.status(401).json({
          success: false,
          message:
            'Current password is incorrect',
        });
      }

      const passwordValidation =
        validatePasswordStrength(
          newPassword,
          user
        );

      if (
        !passwordValidation.isValid
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Password does not meet security requirements',
          errors:
            passwordValidation.errors,
          strength:
            passwordValidation.strength,
        });
      }

      const historyHelper =
        new PasswordHistory(
          user._id.toString(),
          user.passwordHistory ||
            []
        );

      if (
        await historyHelper.isPasswordReused(
          newPassword
        )
      ) {
        return res.status(400).json({
          success: false,
          message:
            'You cannot reuse any of your last 5 passwords',
        });
      }

      user.password =
        newPassword;

      await user.save();

      await sendEmail({
        to: user.email,
        subject:
          'Password Changed',
        template:
          'password_changed',
        data: {
          username:
            user.username,
        },
      });

      return res.json({
        success: true,
        message:
          'Password changed successfully',
      });
    } catch (error: any) {
      console.error(
        'Change password error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to change password',
      });
    }
  }
);

// ============================================================
// FORGOT PASSWORD
// ============================================================

router.post(
  '/forgot-password',
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        email,
      } = req.body;

      if (
        typeof email !==
        'string'
      ) {
        return res.json({
          success: true,
          message:
            'If your email is registered, you will receive a reset link',
        });
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

      // Always return the same message
      // to prevent account enumeration.
      if (!user) {
        return res.json({
          success: true,
          message:
            'If your email is registered, you will receive a reset link',
        });
      }

      const resetToken =
        crypto
          .randomBytes(32)
          .toString('hex');

      user.resetPasswordToken =
        resetToken;

      user.resetPasswordExpires =
        new Date(
          Date.now() +
            60 * 60 * 1000
        );

      await user.save();

      const baseUrl =
        process.env.BASE_URL ||
        'http://localhost:3000';

      const resetUrl =
        `${baseUrl}/reset-password?token=${resetToken}`;

      await sendEmail({
        to: user.email,
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

      return res.json({
        success: true,
        message:
          'If your email is registered, you will receive a reset link',
      });
    } catch (error: any) {
      console.error(
        'Forgot password error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to process password reset request',
      });
    }
  }
);

// ============================================================
// RESET PASSWORD
// ============================================================

router.post(
  '/reset-password',
  async (
    req: Request,
    res: Response
  ) => {
    try {
      const {
        token,
        newPassword,
      } = req.body;

      if (
        typeof token !==
        'string' ||
        typeof newPassword !==
        'string'
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Token and new password are required',
        });
      }

      const user =
        await User.findOne({
          resetPasswordToken:
            token,

          resetPasswordExpires: {
            $gt: new Date(),
          },
        }).select(
          '+passwordHistory'
        );

      if (!user) {
        return res.status(400).json({
          success: false,
          message:
            'Invalid or expired reset token',
        });
      }

      const passwordValidation =
        validatePasswordStrength(
          newPassword,
          user
        );

      if (
        !passwordValidation.isValid
      ) {
        return res.status(400).json({
          success: false,
          message:
            'Password does not meet security requirements',
          errors:
            passwordValidation.errors,
          strength:
            passwordValidation.strength,
        });
      }

      const historyHelper =
        new PasswordHistory(
          user._id.toString(),
          user.passwordHistory ||
            []
        );

      if (
        await historyHelper.isPasswordReused(
          newPassword
        )
      ) {
        return res.status(400).json({
          success: false,
          message:
            'You cannot reuse any of your last 5 passwords',
        });
      }

      user.password =
        newPassword;

      user.resetPasswordToken =
        undefined;

      user.resetPasswordExpires =
        undefined;

      await user.save();

      return res.json({
        success: true,
        message:
          'Password reset successfully',
      });
    } catch (error: any) {
      console.error(
        'Reset password error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to reset password',
      });
    }
  }
);

// ============================================================
// 2FA SETUP
// ============================================================

router.post(
  '/2fa/setup',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const user =
        req.user;

      const secret =
        user.generateTwoFactorSecret();

      await user.save();

      const otpauthUrl =
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

      const backupCodes =
        user.generateBackupCodes();

      await user.save();

      return res.json({
        success: true,
        secret:
          secret.base32,
        qrCode,
        backupCodes,
      });
    } catch (error: any) {
      console.error(
        '2FA setup error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to setup 2FA',
      });
    }
  }
);

// ============================================================
// 2FA VERIFY
// ============================================================

router.post(
  '/2fa/verify',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const {
        token,
      } = req.body;

      if (
        typeof token !==
        'string' ||
        !/^\d{6}$/.test(token)
      ) {
        return res.status(400).json({
          success: false,
          message:
            'A valid 6-digit 2FA code is required',
        });
      }

      const user =
        req.user;

      const isValid =
        Boolean(
          user.verifyTwoFactorToken(
            token
          )
        );

      if (!isValid) {
        return res.status(400).json({
          success: false,
          message:
            'Invalid 2FA code',
        });
      }

      user.twoFactorEnabled =
        true;

      await user.save();

      return res.json({
        success: true,
        message:
          '2FA enabled successfully',
      });
    } catch (error: any) {
      console.error(
        '2FA verification error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to verify 2FA',
      });
    }
  }
);

// ============================================================
// 2FA DISABLE
// ============================================================

router.post(
  '/2fa/disable',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const {
        token,
      } = req.body;

      if (
        typeof token !==
        'string'
      ) {
        return res.status(400).json({
          success: false,
          message:
            '2FA code is required',
        });
      }

      const user =
        req.user;

      const isValid =
        Boolean(
          user.verifyTwoFactorToken(
            token
          )
        );

      if (!isValid) {
        return res.status(400).json({
          success: false,
          message:
            'Invalid 2FA code',
        });
      }

      user.twoFactorEnabled =
        false;

      user.twoFactorSecret =
        undefined;

      user.twoFactorBackupCodes =
        undefined;

      await user.save();

      return res.json({
        success: true,
        message:
          '2FA disabled successfully',
      });
    } catch (error: any) {
      console.error(
        '2FA disable error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to disable 2FA',
      });
    }
  }
);

// ============================================================
// VERIFY EMAIL
// ============================================================

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

      const user =
        await User.findOne({
          emailVerificationToken:
            token,

          emailVerificationExpires: {
            $gt: new Date(),
          },
        });

      if (!user) {
        return res.status(400).json({
          success: false,
          message:
            'Invalid or expired verification token',
        });
      }

      if (user.emailVerified) {
        return res.json({
          success: true,
          message:
            'Email is already verified',
        });
      }

      user.emailVerified =
        true;

      user.emailVerificationToken =
        undefined;

      user.emailVerificationExpires =
        undefined;

      // --------------------------------------------------------
      // Verification bonus
      // --------------------------------------------------------

      const verificationBonus =
        50;

      if (!user.wallet) {
        user.wallet = {
          balance: 0,
        };
      }

      user.wallet.balance =
        Number(
          user.wallet.balance || 0
        ) + verificationBonus;

      await user.save();

      return res.json({
        success: true,
        message:
          `Email verified successfully! You received ${verificationBonus} ETB bonus.`,
      });
    } catch (error: any) {
      console.error(
        'Email verification error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to verify email',
      });
    }
  }
);

// ============================================================
// RESEND EMAIL VERIFICATION
// ============================================================

router.post(
  '/resend-verification',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message:
            'Authentication required',
        });
      }

      const user =
        req.user;

      if (user.emailVerified) {
        return res.status(400).json({
          success: false,
          message:
            'Email already verified',
        });
      }

      const token =
        user.generateEmailVerificationToken();

      await user.save();

      const baseUrl =
        process.env.BASE_URL ||
        'http://localhost:3000';

      const verificationUrl =
        `${baseUrl}/verify-email/${token}`;

      await sendEmail({
        to: user.email,
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

      return res.json({
        success: true,
        message:
          'Verification email sent',
      });
    } catch (error: any) {
      console.error(
        'Resend verification error:',
        error
      );

      return res.status(500).json({
        success: false,
        message:
          'Failed to send verification email',
      });
    }
  }
);

// ============================================================
// EXPORT
// ============================================================

export default router;