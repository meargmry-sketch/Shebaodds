import mongoose from 'mongoose';
import * as dotenv from 'dotenv';
import { UserProfileModel, UserRole } from '../expressApiGateway';
import { JackpotPool } from '../jackpotSchema';
import { CasinoGame, CASINO_GAMES_DATA } from '../Match'; // new casino model

dotenv.config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/shebaodds';

async function seedDatabase() {
  console.log('🔄 [SEED] Commencing ShebaOdds database seeding sequence...');

  try {
    await mongoose.connect(MONGODB_URI);
    console.log('✅ [SEED] Database connected successfully.');

    // 1. Clear existing datasets
    console.log('🔄 [SEED] Cleansing existing tables & indexes...');
    await UserProfileModel.deleteMany({});
    await JackpotPool.deleteMany({});
    await CasinoGame.deleteMany({});

    // 2. Seed User Profiles (Players & Admins)
    console.log('🔄 [SEED] Seeding enterprise user records...');
    const userProfiles = [
      {
        userId: 'USR-849201',
        email: 'player@shebaodds.com',
        role: UserRole.PLAYER,
        preferredLanguage: 'am',
        isFlagged: false,
        nationalIdNumber: 'ET-99410A'
      },
      {
        userId: 'USR-201940',
        email: 'agent@shebaodds.com',
        role: UserRole.AGENT,
        preferredLanguage: 'en',
        isFlagged: false
      },
      {
        userId: 'USR-109403',
        email: 'admin@shebaodds.com',
        role: UserRole.MASTER_ADMIN,
        preferredLanguage: 'am',
        isFlagged: false
      }
    ];
    await UserProfileModel.insertMany(userProfiles);
    console.log('✅ [SEED] Registered 3 core user profiles (Player, Agent, SuperAdmin).');

    // 3. Seed Default 12-Match Grand Jackpot
    console.log('🔄 [SEED] Seeding active 12-match Grand Weekend Jackpot Pool...');
    const activeJackpot = {
      title: 'Grand Weekend 12 Jackpot Event',
      matchIds: [
        1001, 1002, 1003, 1004, 1005, 1006,
        1007, 1008, 1009, 1010, 1011, 1012
      ],
      grandPrize: 100000.00,
      entryFee: 50.00,
      status: 'Open',
      results: []
    };
    await JackpotPool.create(activeJackpot);
    console.log('✅ [SEED] Registered Grand Weekend 12 Jackpot Pool with 100,000 ETB target.');

    // 4. Seed 51+ Casino Games
    console.log('🔄 [SEED] Seeding 51+ casino games...');
    await CasinoGame.insertMany(CASINO_GAMES_DATA);
    console.log(`✅ [SEED] Seeded ${CASINO_GAMES_DATA.length} casino games successfully.`);

    console.log('======================================================================');
    console.log('🎉 [SEED SUCCESS] ShebaOdds Enterprise Database seeded successfully.');
    console.log('======================================================================');

  } catch (err: any) {
    console.error('💥 [SEED ERROR] Failed to seed database:', err.message || err);
    process.exit(1);
  } finally {
    await mongoose.disconnect();
    console.log('🔌 [SEED] Database connection pool closed safely.');
  }
}

seedDatabase();