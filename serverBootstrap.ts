// ============================================
// SHEBAODDS - APPLICATION BOOTSTRAP
// ============================================

import mongoose from 'mongoose';
import * as dotenv from 'dotenv';

dotenv.config();

// ============================================
// REQUIRED ENVIRONMENT VARIABLES
// ============================================

const REQUIRED_ENV_VARIABLES = [
  'MONGODB_URI',
  'JWT_SECRET',
  'JWT_REFRESH_SECRET',
  'BIOMETRIC_ENCRYPTION_KEY'
];

// ============================================
// OPTIONAL ENVIRONMENT VARIABLES
// ============================================

const OPTIONAL_ENV_VARIABLES = [
  'TAX_RATE',
  'WELCOME_BONUS',
  'TELE_BIRR_API_KEY',
  'TELE_BIRR_SECRET',
  'CBE_API_KEY',
  'CHAPA_API_KEY',
  'SPORTS_DATA_API_KEY',
  'REDIS_HOST',
  'REDIS_PORT'
];

// ============================================
// CHECK ENVIRONMENT
// ============================================

function verifyEnvironment(): void {
  console.log('🔄 [BOOTSTRAP] Checking environment configuration...');

  const missing: string[] = [];

  for (const variable of REQUIRED_ENV_VARIABLES) {
    const value = process.env[variable];

    if (
      !value ||
      value.trim() === '' ||
      value.includes('change_this') ||
      value.includes('your_')
    ) {
      missing.push(variable);
    }
  }

  if (missing.length > 0) {
    console.error('');
    console.error(
      '❌ [BOOTSTRAP CRITICAL ERROR] Required environment variables are missing:'
    );

    missing.forEach(variable => {
      console.error(`   - ${variable}`);
    });

    console.error('');
    console.error(
      '💥 Server startup cancelled because required configuration is missing.'
    );

    throw new Error(
      `Missing required environment variables: ${missing.join(', ')}`
    );
  }

  console.log('✅ Required environment variables configured.');

  // ============================================
  // OPTIONAL VARIABLES
  // ============================================

  const missingOptional: string[] = [];

  for (const variable of OPTIONAL_ENV_VARIABLES) {
    if (!process.env[variable]) {
      missingOptional.push(variable);
    }
  }

  if (missingOptional.length > 0) {
    console.warn('');
    console.warn(
      '⚠️ [BOOTSTRAP] Optional environment variables not configured:'
    );

    missingOptional.forEach(variable => {
      console.warn(`   - ${variable}`);
    });

    console.warn(
      'Some payment, Redis, sports, or bonus features may be unavailable.'
    );
  }

  // ============================================
  // DEFAULT VALUES
  // ============================================

  if (!process.env.TAX_RATE) {
    process.env.TAX_RATE = '0.15';
  }

  if (!process.env.WELCOME_BONUS) {
    process.env.WELCOME_BONUS = '100';
  }

  console.log('✅ Environment configuration validated.');
}

// ============================================
// MONGODB CONNECTION
// ============================================

async function connectDatabase(): Promise<void> {
  const mongoUri = process.env.MONGODB_URI;

  if (!mongoUri) {
    throw new Error('MONGODB_URI is not configured');
  }

  console.log('🔄 [BOOTSTRAP] Connecting to MongoDB...');

  try {
    await mongoose.connect(mongoUri, {
      serverSelectionTimeoutMS: 10000
    });

    console.log('✅ [BOOTSTRAP] MongoDB connected successfully.');

    if (mongoose.connection.db) {
      console.log(
        `📦 [BOOTSTRAP] Database: ${mongoose.connection.db.databaseName}`
      );
    }

  } catch (error: any) {
    console.error(
      '❌ [BOOTSTRAP] MongoDB connection failed:',
      error.message || error
    );

    throw error;
  }
}

// ============================================
// MAIN BOOTSTRAP
// ============================================

export async function bootstrapAppEngine(): Promise<void> {

  console.log('');
  console.log('============================================');
  console.log('🚀 SHEBAODDS APP ENGINE');
  console.log('============================================');

  // Step 1
  verifyEnvironment();

  // Step 2
  await connectDatabase();

  console.log('');
  console.log('============================================');
  console.log('✅ SHEBAODDS BOOTSTRAP COMPLETE');
  console.log('============================================');
  console.log('');
}

// ============================================
// DIRECT EXECUTION
// ============================================

if (require.main === module) {

  bootstrapAppEngine()
    .then(() => {
      console.log('🚀 Server initialization completed.');
    })
    .catch((error) => {

      console.error('');
      console.error('💥 [BOOTSTRAP PANIC]');
      console.error(error);

      process.exit(1);
    });
}