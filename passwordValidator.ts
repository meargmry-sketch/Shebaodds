// ============================================
// SHEBAODDS - STRONG PASSWORD VALIDATION
// ============================================

import {
  Request,
  Response,
  NextFunction
} from 'express';

import bcrypt from 'bcryptjs';

// ============================================
// STRENGTH
// ============================================

export const PASSWORD_STRENGTH = {
  WEAK: 'weak',
  FAIR: 'fair',
  GOOD: 'good',
  STRONG: 'strong',
  VERY_STRONG: 'very_strong'
} as const;

export type PasswordStrength =
  typeof PASSWORD_STRENGTH[
    keyof typeof PASSWORD_STRENGTH
  ];

// ============================================
// RULES
// ============================================

export const PASSWORD_RULES = {
  minLength: 8,
  maxLength: 128,

  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: true,

  preventCommonPasswords: true,
  preventPersonalInfo: true,

  preventSequentialChars: true,
  preventRepeatedChars: true,

  maxHistory: 5
};

// ============================================
// COMMON PASSWORDS
// ============================================

export const COMMON_PASSWORDS = [
  'password',
  '12345678',
  'qwerty123',
  'admin123',
  'letmein123',
  'welcome123',
  'password123',
  'abc123456',
  'shebaodds',
  'ethiopia123',
  '123456789',
  '11111111',
  '00000000',
  'passw0rd',
  'admin@123'
];

// ============================================
// SPECIAL CHARACTERS
// ============================================

export const SPECIAL_CHARS =
  '!@#$%^&*()_+-=[]{}|;:,.<>?';

// ============================================
// USER INFO
// ============================================

export interface UserInfo {
  username?: string;
  email?: string;
  fullName?: string;
  phone?: string;
}

// ============================================
// RESULT
// ============================================

export interface ValidationResult {
  isValid: boolean;
  strength: PasswordStrength;
  score: number;
  errors: string[];
  warnings: string[];

  hasUppercase: boolean;
  hasLowercase: boolean;
  hasNumbers: boolean;
  hasSpecial: boolean;
  isLongEnough: boolean;
}

// ============================================
// PASSWORD VALIDATION
// ============================================

export function validatePasswordStrength(
  password: string,
  userInfo: UserInfo = {}
): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];

  let score = 0;

  if (typeof password !== 'string') {
    return {
      isValid: false,
      strength: PASSWORD_STRENGTH.WEAK,
      score: 0,
      errors: ['Password must be a string'],
      warnings: [],
      hasUppercase: false,
      hasLowercase: false,
      hasNumbers: false,
      hasSpecial: false,
      isLongEnough: false
    };
  }

  // ==========================================
  // LENGTH
  // ==========================================

  if (
    password.length <
    PASSWORD_RULES.minLength
  ) {
    errors.push(
      `Password must be at least ${PASSWORD_RULES.minLength} characters`
    );
  }

  if (
    password.length >
    PASSWORD_RULES.maxLength
  ) {
    errors.push(
      `Password cannot exceed ${PASSWORD_RULES.maxLength} characters`
    );
  }

  if (password.length >= 12) {
    score += 2;
  } else if (password.length >= 10) {
    score += 1.5;
  } else if (password.length >= 8) {
    score += 1;
  }

  // ==========================================
  // CHARACTER TYPES
  // ==========================================

  const hasUppercase =
    /[A-Z]/.test(password);

  const hasLowercase =
    /[a-z]/.test(password);

  const hasNumbers =
    /[0-9]/.test(password);

  const escapedSpecialChars =
    SPECIAL_CHARS.replace(
      /[-\/\\^$*+?.()|[\]{}]/g,
      '\\$&'
    );

  const hasSpecial =
    new RegExp(
      `[${escapedSpecialChars}]`
    ).test(password);

  if (
    PASSWORD_RULES.requireUppercase &&
    !hasUppercase
  ) {
    errors.push(
      'Password must contain at least one uppercase letter'
    );
  } else if (hasUppercase) {
    score += 1;
  }

  if (
    PASSWORD_RULES.requireLowercase &&
    !hasLowercase
  ) {
    errors.push(
      'Password must contain at least one lowercase letter'
    );
  } else if (hasLowercase) {
    score += 1;
  }

  if (
    PASSWORD_RULES.requireNumbers &&
    !hasNumbers
  ) {
    errors.push(
      'Password must contain at least one number'
    );
  } else if (hasNumbers) {
    score += 1;
  }

  if (
    PASSWORD_RULES.requireSpecialChars &&
    !hasSpecial
  ) {
    errors.push(
      'Password must contain at least one special character'
    );
  } else if (hasSpecial) {
    score += 1.5;
  }

  // ==========================================
  // COMMON PASSWORD
  // ==========================================

  if (
    PASSWORD_RULES.preventCommonPasswords
  ) {
    if (
      COMMON_PASSWORDS.includes(
        password.toLowerCase()
      )
    ) {
      errors.push(
        'This password is too common. Please choose a more secure password'
      );
    }
  }

  // ==========================================
  // SEQUENTIAL CHARACTERS
  // ==========================================

  if (
    PASSWORD_RULES.preventSequentialChars
  ) {
    const patterns = [
      'abcdefghijklmnopqrstuvwxyz',
      'qwertyuiop',
      'asdfghjkl',
      'zxcvbnm',
      '1234567890',
      '0123456789'
    ];

    const lowerPassword =
      password.toLowerCase();

    let foundSequential = false;

    for (const pattern of patterns) {
      for (
        let i = 0;
        i <= lowerPassword.length - 4;
        i++
      ) {
        const chunk =
          lowerPassword.substring(
            i,
            i + 4
          );

        if (pattern.includes(chunk)) {
          foundSequential = true;
          break;
        }
      }

      if (foundSequential) {
        break;
      }
    }

    if (foundSequential) {
      errors.push(
        'Password contains sequential characters'
      );
    }
  }

  // ==========================================
  // REPEATED CHARACTERS
  // ==========================================

  if (
    PASSWORD_RULES.preventRepeatedChars &&
    /(.)\1{3,}/.test(password)
  ) {
    errors.push(
      'Password contains too many repeated characters'
    );
  }

  // ==========================================
  // PERSONAL INFORMATION
  // ==========================================

  if (
    PASSWORD_RULES.preventPersonalInfo
  ) {
    const personalInfo = [
      userInfo.username,
      userInfo.email?.split('@')[0],
      userInfo.fullName,
      userInfo.phone
    ]
      .filter(Boolean)
      .map(value =>
        String(value)
          .trim()
          .toLowerCase()
      )
      .filter(value => value.length >= 3);

    const lowerPassword =
      password.toLowerCase();

    for (const info of personalInfo) {
      if (
        lowerPassword.includes(info)
      ) {
        errors.push(
          'Password should not contain personal information'
        );
        break;
      }
    }
  }

  // ==========================================
  // WARNINGS
  // ==========================================

  if (password.length < 12) {
    warnings.push(
      'A password of 12 or more characters is recommended'
    );
  }

  // ==========================================
  // STRENGTH
  // ==========================================

  let strength: PasswordStrength;

  if (score >= 6) {
    strength =
      PASSWORD_STRENGTH.VERY_STRONG;
  } else if (score >= 4.5) {
    strength =
      PASSWORD_STRENGTH.STRONG;
  } else if (score >= 3) {
    strength =
      PASSWORD_STRENGTH.GOOD;
  } else if (score >= 1.5) {
    strength =
      PASSWORD_STRENGTH.FAIR;
  } else {
    strength =
      PASSWORD_STRENGTH.WEAK;
  }

  return {
    isValid: errors.length === 0,
    strength,
    score,
    errors,
    warnings,

    hasUppercase,
    hasLowercase,
    hasNumbers,
    hasSpecial,

    isLongEnough:
      password.length >=
      PASSWORD_RULES.minLength
  };
}

// ============================================
// PASSWORD HISTORY
// ============================================

export class PasswordHistory {
  userId: string;
  passwordHistory: string[];

  constructor(
    userId: string,
    passwordHistory: string[] = []
  ) {
    this.userId = userId;
    this.passwordHistory =
      passwordHistory;
  }

  async isPasswordReused(
    newPassword: string
  ): Promise<boolean> {
    if (
      !PASSWORD_RULES.maxHistory ||
      this.passwordHistory.length === 0
    ) {
      return false;
    }

    for (
      const oldHash
      of this.passwordHistory
    ) {
      if (
        await bcrypt.compare(
          newPassword,
          oldHash
        )
      ) {
        return true;
      }
    }

    return false;
  }

  async addToHistory(
    newPasswordHash: string
  ): Promise<string[]> {
    this.passwordHistory.unshift(
      newPasswordHash
    );

    if (
      this.passwordHistory.length >
      PASSWORD_RULES.maxHistory
    ) {
      this.passwordHistory =
        this.passwordHistory.slice(
          0,
          PASSWORD_RULES.maxHistory
        );
    }

    return this.passwordHistory;
  }
}

// ============================================
// STRENGTH METER
// ============================================

export function getPasswordStrengthMeter(
  strength: string
) {
  const meters: Record<string, any> = {
    [PASSWORD_STRENGTH.WEAK]: {
      label: 'Weak',
      labelAm: 'ደካማ',
      color: '#F44336',
      percentage: 20,
      suggestions: [
        'Use at least 8 characters',
        'Add uppercase letters',
        'Add numbers',
        'Add special characters'
      ]
    },

    [PASSWORD_STRENGTH.FAIR]: {
      label: 'Fair',
      labelAm: 'መጠነኛ',
      color: '#FF9800',
      percentage: 40,
      suggestions: [
        'Make it longer',
        'Add more character variety'
      ]
    },

    [PASSWORD_STRENGTH.GOOD]: {
      label: 'Good',
      labelAm: 'ጥሩ',
      color: '#2196F3',
      percentage: 60,
      suggestions: [
        'Make it longer',
        'Use a less predictable combination'
      ]
    },

    [PASSWORD_STRENGTH.STRONG]: {
      label: 'Strong',
      labelAm: 'ጠንካራ',
      color: '#4CAF50',
      percentage: 80,
      suggestions: []
    },

    [PASSWORD_STRENGTH.VERY_STRONG]: {
      label: 'Very Strong',
      labelAm: 'በጣም ጠንካራ',
      color: '#00E676',
      percentage: 100,
      suggestions: []
    }
  };

  return (
    meters[strength] ||
    meters[PASSWORD_STRENGTH.WEAK]
  );
}

// ============================================
// EXPRESS MIDDLEWARE
// ============================================

export function validatePassword(
  req: Request,
  res: Response,
  next: NextFunction
) {
  const {
    password,
    username,
    email,
    fullName,
    phone
  } = req.body || {};

  if (
    typeof password !== 'string' ||
    !password
  ) {
    return res.status(400).json({
      success: false,
      message: 'Password is required'
    });
  }

  const validation =
    validatePasswordStrength(
      password,
      {
        username,
        email,
        fullName,
        phone
      }
    );

  if (!validation.isValid) {
    return res.status(400).json({
      success: false,
      message:
        'Password does not meet security requirements',
      errors: validation.errors,
      warnings: validation.warnings,
      strength: validation.strength
    });
  }

  (req as any).passwordWarnings =
    validation.warnings;

  (req as any).passwordStrength =
    validation.strength;

  (req as any).passwordValidation =
    validation;

  return next();
}