// ============================================
// SHEBAODDS - AUTH ROUTES
// Complete Authentication API
// ============================================

import express, { Request, Response, NextFunction } from 'express';
import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import { User, UserDocument } from './models/User';

const router = express.Router();

// ============================================
// CONFIGURATION
// ============================================

const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
  console.error('❌ JWT_SECRET is not configured');
}

const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '7d';
const MAX_LOGIN_ATTEMPTS = 5;
const LOCK_TIME = 15 * 60 * 1000;

// ============================================
// TYPES
// ============================================

interface AuthenticatedRequest extends Request {
  user?: UserDocument;
}

// ============================================
// HELPERS
// ============================================

function signToken(user: UserDocument): string {
  if (!JWT_SECRET) {
    throw new Error('JWT_SECRET is not configured');
  }

  return jwt.sign(
    {
      sub: user._id.toString(),
      username: user.username,
      email: user.email,
      isAdmin: user.isAdmin
    },
    JWT_SECRET,
    {
      expiresIn: JWT_EXPIRES_IN as jwt.SignOptions['expiresIn']
    }
  );
}

function sanitizeUser(user: UserDocument) {
  const obj = user.toObject();

  delete obj.password;
  delete obj.passwordHistory;
  delete obj.twoFactorSecret;
  delete obj.twoFactorBackupCodes;
  delete obj.resetPasswordToken;
  delete obj.emailVerificationToken;
  delete obj.phoneVerificationCode;

  return obj;
}

function generateSessionId(): string {
  return crypto.randomBytes(32).toString('hex');
}

function generateVerificationToken(): string {
  return crypto.randomBytes(32).toString('hex');
}

function normalizeUsername(username: string): string {
  return username.trim().toLowerCase();
}

function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

// ============================================
// AUTH MIDDLEWARE
// ============================================

async function authenticate(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
) {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        error: 'Unauthorized',
        message: 'Authentication token is required'
      });
    }

    const token = authHeader.substring(7);

    if (!JWT_SECRET) {
      return res.status(500).json({
        success: false,
        error: 'Server Configuration Error',
        message: 'Authentication service is not configured'
      });
    }

    const decoded = jwt.verify(token, JWT_SECRET) as {
      sub: string;
    };

    const user = await User.findById(decoded.sub).select(
      '+password +twoFactorSecret +twoFactorBackupCodes'
    );

    if (!user) {
      return res.status(401).json({
        success: false,
        error: 'Unauthorized',
        message: 'User no longer exists'
      });
    }

    if (!user.isActive || user.isBlocked) {
      return res.status(403).json({
        success: false,
        error: 'Account Disabled',
        message: 'Your account is disabled or blocked'
      });
    }

    req.user = user;

    next();
  } catch (error: any) {
    if (error?.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        error: 'Token Expired',
        message: 'Your session has expired. Please login again.'
      });
    }

    return res.status(401).json({
      success: false,
      error: 'Invalid Token',
      message: 'Authentication token is invalid'
    });
  }
}

// ============================================
// GET AUTH ROUTE INFORMATION
// ============================================

router.get('/', (_req: Request, res: Response) => {
  res.status(200).json({
    success: true,
    service: 'SHEBAODDS Authentication',
    version: 'v2',
    routes: {
      register: 'POST /register',
      login: 'POST /login',
      me: 'GET /me',
      logout: 'POST /logout',
      verifyEmail: 'POST /verify-email',
      forgotPassword: 'POST /forgot-password',
      resetPassword: 'POST /reset-password',
      changePassword: 'POST /change-password'
    }
  });
});

// ============================================
// REGISTER
// POST /api/v2/auth/register
// ============================================

router.post(
  '/register',
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const {
        username,
        email,
        password,
        phone,
        fullName,
        dateOfBirth,
        country,
        city,
        address,
        postalCode,
        referralCode
      } = req.body;

      // ----------------------------------------
      // VALIDATION
      // ----------------------------------------

      if (!username || !email || !password || !phone) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message:
            'Username, email, password and phone are required'
        });
      }

      const cleanUsername = normalizeUsername(username);
      const cleanEmail = normalizeEmail(email);
      const cleanPhone = String(phone).trim();

      if (cleanUsername.length < 3 || cleanUsername.length > 20) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Username must be between 3 and 20 characters'
        });
      }

      if (password.length < 8) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Password must be at least 8 characters'
        });
      }

      if (!/^[a-zA-Z0-9_]+$/.test(cleanUsername)) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message:
            'Username can only contain letters, numbers and underscore'
        });
      }

      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail)) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Please provide a valid email address'
        });
      }

      if (!/^\+?[0-9]{10,15}$/.test(cleanPhone)) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Please provide a valid phone number'
        });
      }

      // ----------------------------------------
      // DUPLICATE CHECK
      // ----------------------------------------

      const existingUser = await User.findOne({
        $or: [
          { username: cleanUsername },
          { email: cleanEmail },
          { phone: cleanPhone }
        ]
      });

      if (existingUser) {
        let field = 'account information';

        if (existingUser.username === cleanUsername) {
          field = 'username';
        } else if (existingUser.email === cleanEmail) {
          field = 'email';
        } else if (existingUser.phone === cleanPhone) {
          field = 'phone number';
        }

        return res.status(409).json({
          success: false,
          error: 'Already Exists',
          message: `An account with this ${field} already exists`
        });
      }

      // ----------------------------------------
      // REFERRAL
      // ----------------------------------------

      let referredBy = undefined;

      if (referralCode) {
        const referrer = await User.findOne({
          referralCode: String(referralCode).trim().toUpperCase()
        });

        if (!referrer) {
          return res.status(400).json({
            success: false,
            error: 'Invalid Referral',
            message: 'Referral code is invalid'
          });
        }

        referredBy = referrer._id;
      }

      // ----------------------------------------
      // EMAIL VERIFICATION
      // ----------------------------------------

      const verificationToken = generateVerificationToken();

      // ----------------------------------------
      // CREATE USER
      // ----------------------------------------

      const user = new User({
        username: cleanUsername,
        email: cleanEmail,
        password,
        phone: cleanPhone,

        fullName,
        dateOfBirth,
        country: country || 'Ethiopia',
        city,
        address,
        postalCode,

        referredBy,

        emailVerificationToken: verificationToken,
        emailVerificationExpires: new Date(
          Date.now() + 24 * 60 * 60 * 1000
        ),

        emailVerified: false,
        phoneVerified: false,

        isActive: true,
        isAdmin: false,
        isVerified: false,
        isBlocked: false,
        isSuspended: false
      });

      await user.save();

      // ----------------------------------------
      // UPDATE REFERRER
      // ----------------------------------------

      if (referredBy) {
        await User.findByIdAndUpdate(referredBy, {
          $inc: {
            referralCount: 1
          }
        });
      }

      // ----------------------------------------
      // TOKEN
      // ----------------------------------------

      const token = signToken(user);

      // ----------------------------------------
      // RESPONSE
      // ----------------------------------------

      return res.status(201).json({
        success: true,
        message: 'Account created successfully',
        token,
        user: sanitizeUser(user)
      });
    } catch (error: any) {
      console.error('❌ Registration error:', error);

      if (error?.code === 11000) {
        return res.status(409).json({
          success: false,
          error: 'Duplicate Account',
          message: 'Username, email or phone number already exists'
        });
      }

      next(error);
    }
  }
);

// ============================================
// LOGIN
// POST /api/v2/auth/login
// ============================================

router.post(
  '/login',
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { identifier, email, username, password } = req.body;

      const loginIdentifier =
        identifier || email || username;

      if (!loginIdentifier || !password) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Username/email and password are required'
        });
      }

      const normalizedIdentifier =
        String(loginIdentifier).trim().toLowerCase();

      const user = await User.findByEmailOrUsername(
        normalizedIdentifier
      );

      if (!user) {
        return res.status(401).json({
          success: false,
          error: 'Invalid Credentials',
          message: 'Username/email or password is incorrect'
        });
      }

      // ----------------------------------------
      // ACCOUNT STATUS
      // ----------------------------------------

      if (user.isBlocked) {
        return res.status(403).json({
          success: false,
          error: 'Account Blocked',
          message: 'Your account has been blocked'
        });
      }

      if (!user.isActive) {
        return res.status(403).json({
          success: false,
          error: 'Account Disabled',
          message: 'Your account is disabled'
        });
      }

      if (
        user.lockedUntil &&
        user.lockedUntil > new Date()
      ) {
        const remaining = Math.ceil(
          (user.lockedUntil.getTime() - Date.now()) / 60000
        );

        return res.status(423).json({
          success: false,
          error: 'Account Locked',
          message: `Too many failed attempts. Try again in ${remaining} minutes.`
        });
      }

      // ----------------------------------------
      // PASSWORD
      // ----------------------------------------

      const passwordCorrect =
        await user.comparePassword(password);

      if (!passwordCorrect) {
        user.loginAttempts += 1;

        if (user.loginAttempts >= MAX_LOGIN_ATTEMPTS) {
          user.lockedUntil = new Date(
            Date.now() + LOCK_TIME
          );
          user.loginAttempts = 0;
        }

        await user.save();

        return res.status(401).json({
          success: false,
          error: 'Invalid Credentials',
          message: 'Username/email or password is incorrect'
        });
      }

      // ----------------------------------------
      // RESET LOGIN SECURITY
      // ----------------------------------------

      user.loginAttempts = 0;
      user.lockedUntil = undefined;

      user.lastLogin = new Date();
      user.lastActive = new Date();

      // ----------------------------------------
      // SESSION
      // ----------------------------------------

      const sessionId = generateSessionId();

      user.sessions.push({
        sessionId,
        ipAddress:
          req.ip ||
          req.headers['x-forwarded-for']?.toString(),
        userAgent: req.headers['user-agent'],
        loginAt: new Date(),
        lastActivity: new Date(),
        expiresAt: new Date(
          Date.now() + 7 * 24 * 60 * 60 * 1000
        )
      });

      // Keep sessions manageable
      if (user.sessions.length > 10) {
        user.sessions = user.sessions.slice(-10);
      }

      await user.save();

      // ----------------------------------------
      // JWT
      // ----------------------------------------

      const token = signToken(user);

      // ----------------------------------------
      // RESPONSE
      // ----------------------------------------

      return res.status(200).json({
        success: true,
        message: 'Login successful',
        token,
        sessionId,
        user: sanitizeUser(user)
      });
    } catch (error) {
      console.error('❌ Login error:', error);
      next(error);
    }
  }
);

// ============================================
// GET CURRENT USER
// GET /api/v2/auth/me
// ============================================

router.get(
  '/me',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: 'Unauthorized',
          message: 'User not authenticated'
        });
      }

      req.user.lastActive = new Date();

      await req.user.save();

      return res.status(200).json({
        success: true,
        user: sanitizeUser(req.user)
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// LOGOUT
// POST /api/v2/auth/logout
// ============================================

router.post(
  '/logout',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: 'Unauthorized',
          message: 'User not authenticated'
        });
      }

      const { sessionId } = req.body;

      if (sessionId) {
        req.user.sessions =
          req.user.sessions.filter(
            session => session.sessionId !== sessionId
          );
      } else {
        // If no session ID is supplied, remove expired
        // sessions only.
        const now = new Date();

        req.user.sessions =
          req.user.sessions.filter(
            session =>
              !session.expiresAt ||
              session.expiresAt > now
          );
      }

      await req.user.save();

      return res.status(200).json({
        success: true,
        message: 'Logout successful'
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// VERIFY EMAIL
// POST /api/v2/auth/verify-email
// ============================================

router.post(
  '/verify-email',
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { token } = req.body;

      if (!token) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Verification token is required'
        });
      }

      const user = await User.findOne({
        emailVerificationToken: token,
        emailVerificationExpires: {
          $gt: new Date()
        }
      });

      if (!user) {
        return res.status(400).json({
          success: false,
          error: 'Invalid Token',
          message:
            'Verification token is invalid or expired'
        });
      }

      user.emailVerified = true;
      user.isVerified = true;
      user.emailVerificationToken = undefined;
      user.emailVerificationExpires = undefined;

      await user.save();

      return res.status(200).json({
        success: true,
        message: 'Email verified successfully'
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// FORGOT PASSWORD
// POST /api/v2/auth/forgot-password
// ============================================

router.post(
  '/forgot-password',
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { email } = req.body;

      if (!email) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Email is required'
        });
      }

      const user = await User.findOne({
        email: normalizeEmail(email)
      });

      /*
       * Always return the same response whether the
       * account exists or not.
       */
      if (!user) {
        return res.status(200).json({
          success: true,
          message:
            'If an account exists with this email, password reset instructions have been sent.'
        });
      }

      const resetToken = crypto
        .randomBytes(32)
        .toString('hex');

      user.resetPasswordToken = resetToken;
      user.resetPasswordExpires = new Date(
        Date.now() + 60 * 60 * 1000
      );

      await user.save();

      /*
       * IMPORTANT:
       * In production, send resetToken through your
       * email service instead of returning it.
       *
       * It is included here only when development mode
       * is enabled so you can test the API.
       */

      const response: any = {
        success: true,
        message:
          'If an account exists with this email, password reset instructions have been sent.'
      };

      if (process.env.NODE_ENV !== 'production') {
        response.developmentResetToken = resetToken;
      }

      return res.status(200).json(response);
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// RESET PASSWORD
// POST /api/v2/auth/reset-password
// ============================================

router.post(
  '/reset-password',
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const {
        token,
        password
      } = req.body;

      if (!token || !password) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message:
            'Reset token and new password are required'
        });
      }

      if (password.length < 8) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message:
            'Password must be at least 8 characters'
        });
      }

      const user = await User.findOne({
        resetPasswordToken: token,
        resetPasswordExpires: {
          $gt: new Date()
        }
      }).select('+password +passwordHistory');

      if (!user) {
        return res.status(400).json({
          success: false,
          error: 'Invalid Token',
          message:
            'Password reset token is invalid or expired'
        });
      }

      // ----------------------------------------
      // PREVENT REUSE OF RECENT PASSWORDS
      // ----------------------------------------

      if (user.passwordHistory) {
        for (const oldPassword of user.passwordHistory) {
          const reused =
            await require('bcryptjs').compare(
              password,
              oldPassword
            );

          if (reused) {
            return res.status(400).json({
              success: false,
              error: 'Password Reuse',
              message:
                'You cannot reuse one of your recent passwords'
            });
          }
        }
      }

      user.password = password;
      user.resetPasswordToken = undefined;
      user.resetPasswordExpires = undefined;

      user.loginAttempts = 0;
      user.lockedUntil = undefined;

      await user.save();

      return res.status(200).json({
        success: true,
        message:
          'Password reset successfully. Please login again.'
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// CHANGE PASSWORD
// POST /api/v2/auth/change-password
// ============================================

router.post(
  '/change-password',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const {
        currentPassword,
        newPassword
      } = req.body;

      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: 'Unauthorized',
          message: 'User not authenticated'
        });
      }

      if (!currentPassword || !newPassword) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message:
            'Current password and new password are required'
        });
      }

      if (newPassword.length < 8) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message:
            'New password must be at least 8 characters'
        });
      }

      const currentPasswordCorrect =
        await req.user.comparePassword(
          currentPassword
        );

      if (!currentPasswordCorrect) {
        return res.status(401).json({
          success: false,
          error: 'Invalid Password',
          message: 'Current password is incorrect'
        });
      }

      req.user.password = newPassword;

      await req.user.save();

      return res.status(200).json({
        success: true,
        message: 'Password changed successfully'
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// 2FA SETUP
// POST /api/v2/auth/2fa/setup
// ============================================

router.post(
  '/2fa/setup',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: 'Unauthorized',
          message: 'User not authenticated'
        });
      }

      const secret =
        req.user.generateTwoFactorSecret();

      await req.user.save();

      return res.status(200).json({
        success: true,
        message:
          'Two-factor authentication secret generated',
        secret: secret.base32,
        otpauthUrl: secret.otpauth_url
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// 2FA ENABLE
// POST /api/v2/auth/2fa/enable
// ============================================

router.post(
  '/2fa/enable',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const { token } = req.body;

      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: 'Unauthorized',
          message: 'User not authenticated'
        });
      }

      if (!token) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: '2FA token is required'
        });
      }

      const valid =
        req.user.verifyTwoFactorToken(token);

      if (!valid) {
        return res.status(400).json({
          success: false,
          error: 'Invalid Token',
          message: 'Invalid authenticator code'
        });
      }

      req.user.twoFactorEnabled = true;

      const backupCodes =
        req.user.generateBackupCodes();

      await req.user.save();

      return res.status(200).json({
        success: true,
        message:
          'Two-factor authentication enabled',
        backupCodes
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// 2FA DISABLE
// POST /api/v2/auth/2fa/disable
// ============================================

router.post(
  '/2fa/disable',
  authenticate,
  async (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const { password } = req.body;

      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: 'Unauthorized',
          message: 'User not authenticated'
        });
      }

      if (!password) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error',
          message: 'Password is required'
        });
      }

      const valid =
        await req.user.comparePassword(password);

      if (!valid) {
        return res.status(401).json({
          success: false,
          error: 'Invalid Password',
          message: 'Password is incorrect'
        });
      }

      req.user.twoFactorEnabled = false;
      req.user.twoFactorSecret = undefined;
      req.user.twoFactorBackupCodes = [];

      await req.user.save();

      return res.status(200).json({
        success: true,
        message:
          'Two-factor authentication disabled'
      });
    } catch (error) {
      next(error);
    }
  }
);

// ============================================
// ADMIN STATUS
// GET /api/v2/auth/status
// ============================================

router.get(
  '/status',
  (_req: Request, res: Response) => {
    res.status(200).json({
      success: true,
      service: 'SHEBAODDS Auth',
      status: 'operational',
      timestamp: new Date().toISOString()
    });
  }
);

// ============================================
// EXPORT
// ============================================

export default router;