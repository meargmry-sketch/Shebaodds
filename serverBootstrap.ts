import mongoose from 'mongoose';
import * as dotenv from 'dotenv';

// Load environment variables
dotenv.config();

/**
 * List of absolutely required environment variables for the ShebaOdds Enterprise Server
 */
const REQUIRED_ENV_VARIABLES = [
  'MONGODB_URI',
  'TAX_RATE',
  'WELCOME_BONUS',
  'TELE_BIRR_API_KEY',
  'TELE_BIRR_SECRET',
  'CBE_API_KEY',
  'CHAPA_API_KEY',
  'SPORTS_DATA_API_KEY',
  'BIOMETRIC_ENCRYPTION_KEY',
  'REDIS_HOST'
];

/**
 * Optional environment variables for casino games
 */
const CASINO_ENV_VARIABLES = [
  'CASINO_GAMES_ENABLED',
  'CASINO_SECRET_KEY',
  'CASINO_AGGREGATOR_URL'
];

/**
 * Step 1: Check Injected Key Environment Mappings & Fail Fast Guard
 */
function verifyEnvironmentMappings(): void {
  console.log('🔄 [BOOTSTRAP] Step 1: Checking injected key environment mappings...');

  // Dynamic fallbacks for enterprise environment compatibility
  if (!process.env.WELCOME_BONUS && process.env.WELCOME_BONUS_AMOUNT) {
    process.env.WELCOME_BONUS = process.env.WELCOME_BONUS_AMOUNT;
  }
  if (!process.env.BIOMETRIC_ENCRYPTION_KEY && process.env.ENCRYPTION_KEY) {
    process.env.BIOMETRIC_ENCRYPTION_KEY = process.env.ENCRYPTION_KEY;
  }
  if (!process.env.REDIS_HOST) {
    process.env.REDIS_HOST = '127.0.0.1';
  }

  const missingVars: string[] = [];

  for (const variable of REQUIRED_ENV_VARIABLES) {
    if (!process.env[variable] || process.env[variable]?.includes('change_this') || process.env[variable]?.includes('your_')) {
      missingVars.push(variable);
    }
  }

  if (missingVars.length > 0) {
    console.error('❌ [BOOTSTRAP CRITICAL ERROR] Missing or unconfigured required environment variables:');
    missingVars.forEach(v => console.error(`   - ${v}`));
    if (process.env.NODE_ENV === 'production') {
      console.error('💥 Fail Fast Guard Triggered: Terminating server initialization process.');
      process.exit(1);
    } else {
      console.warn('⚠️ [BOOTSTRAP WARNING] Running in non-production mode. Bypassing Fail Fast Guard...');
    }
  }

  // Check casino environment variables (optional – only warn if missing)
  for (const variable of CASINO_ENV_VARIABLES) {
    if (!process.env[variable]) {
      console.warn(`⚠️ [BOOTSTRAP WARNING] Optional casino environment variable "${variable}" is not set. Casino features may be limited.`);
    }
  }

  console.log('✅ [BOOTSTRAP] Environment configuration mappings validated.');
}

/**
 * Step 2: Mongoose Connection and ACID Replica Set Check
 */
async function connectAndValidateReplicaSet(): Promise<void> {
  const mongoUri = process.env.MONGODB_URI as string;
  console.log('🔄 [BOOTSTRAP] Step 2: Initiating Mongoose cluster connection check...');

  try {
    // Attempt database connection
    await mongoose.connect(mongoUri);
    console.log('✅ [BOOTSTRAP] MongoDB connection established successfully.');

    // Validate if the connected database is running as an ACID-compliant Replica Set
    const adminDb = mongoose.connection.db.admin();
    const status = await adminDb.command({ replSetGetStatus: 1 }).catch(() => null);

    if (!status || !status.ok) {
      // In MongoDB, transactions require a replica set. Sharded clusters or standalone instances without replSet will fail ACID transactions.
      console.warn('⚠️ [BOOTSTRAP WARN] "replSetGetStatus" command was not recognized or returned not OK.');

      // Secondary check: examine the connection string for replicaSet parameters
      const hasReplicaSetParam = mongoUri.includes('replicaSet=') || mongoUri.includes('replicaSet');

      if (!hasReplicaSetParam) {
        console.error('❌ [BOOTSTRAP CRITICAL ERROR] MongoDB is not running as an ACID-compliant Replica Set!');
        console.error('👉 Transactions (required for multi-wallet ledger and real-time casino turns) will fail on standalone MongoDB deployments.');

        if (process.env.NODE_ENV === 'production') {
          console.error('💥 Standalone Guard Triggered: Terminating server initialization process.');
          // Terminate database connection
          await mongoose.disconnect();
          process.exit(1);
        } else {
          console.warn('⚠️ [BOOTSTRAP WARNING] Running in non-production mode. Bypassing Standalone Guard...');
        }
      } else {
        console.log('ℹ️ [BOOTSTRAP] Replica Set connection parameter detected in MONGODB_URI.');
      }
    } else {
      console.log(`✅ [BOOTSTRAP] Verified MongoDB ACID Replica Set: "${status.set}" with ${status.members.length} member(s).`);
    }

  } catch (error: any) {
    console.error('❌ [BOOTSTRAP CRITICAL ERROR] Database connection check failed:', error.message || error);
    if (process.env.NODE_ENV === 'production') {
      process.exit(1);
    } else {
      console.warn('⚠️ [BOOTSTRAP WARNING] Connection failed. Bypassing Database Connection Guard for development mock server stability...');
    }
  }
}

/**
 * Main App Engine Server Bootstrapper
 */
export async function bootstrapAppEngine(): Promise<void> {
  console.log('🚀 [BOOTSTRAP] Booting ShebaOdds App Engine Server Initialization...');
  console.log('----------------------------------------------------------------------');

  // Step 1: Environment Guard Check
  verifyEnvironmentMappings();

  // Step 2: Database Connection & Transaction capabilities check
  await connectAndValidateReplicaSet();

  // Casino Games status
  const casinoEnabled = process.env.CASINO_GAMES_ENABLED === 'true';
  console.log(`🎰 [BOOTSTRAP] Casino Games: ${casinoEnabled ? 'ENABLED' : 'DISABLED (set CASINO_GAMES_ENABLED=true to enable)'}`);

  console.log('----------------------------------------------------------------------');
  console.log('⚡ [BOOTSTRAP SUCCESS] Server validated. Igniting Live Sportsbook and Casino Services...');
}

// Auto-run if executed directly as a script
if (require.main === module) {
  bootstrapAppEngine().catch(err => {
    console.error('💥 [BOOTSTRAP PANIC] Unexpected server engine crash:', err);
    process.exit(1);
  });
}