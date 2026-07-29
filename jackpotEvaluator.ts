import mongoose from 'mongoose';
import { JackpotPool, JackpotTicket } from './jackpotSchema';
import User from './User'; // adjust path to your User model

// ==============================================================================
// 🏆 SPORTS JACKPOT
// ==============================================================================
export async function evaluateSportsJackpot(
  poolId: string,
  actualOutcomes: string[]
): Promise<void> {
  if (!actualOutcomes || actualOutcomes.length !== 12) {
    throw new Error('Must provide exactly 12 outcomes.');
  }
  if (!actualOutcomes.every(o => ['1', 'X', '2'].includes(o))) {
    throw new Error('Outcomes must be "1", "X", or "2".');
  }

  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const pool = await JackpotPool.findById(poolId).session(session);
    if (!pool || pool.status !== 'Locked' || pool.type !== 'sports') {
      throw new Error('Pool not found or not in Locked state.');
    }

    const tickets = await JackpotTicket.find({ jackpotPoolId: poolId }).session(session);
    if (tickets.length === 0) {
      pool.results = actualOutcomes;
      pool.status = 'Settled';
      pool.winnerUserId = '';
      await pool.save({ session });
      await session.commitTransaction();
      console.log(`⚠️ Sports jackpot ${poolId} – no tickets.`);
      return;
    }

    const winners: string[] = [];
    for (const ticket of tickets) {
      let correct = 0;
      if (ticket.predictions && ticket.predictions.length === 12) {
        for (let i = 0; i < 12; i++) {
          if (ticket.predictions[i] === actualOutcomes[i]) correct++;
        }
      }
      ticket.correctGuessesCount = correct;
      ticket.isWinner = (correct === 12);
      await ticket.save({ session });
      if (ticket.isWinner) winners.push(ticket.userId);
    }

    const taxRate = parseFloat(process.env.JACKPOT_TAX_RATE || '0.10');

    if (winners.length > 0) {
      const prizePerWinner = Math.floor(pool.grandPrize / winners.length);
      const taxDeduction = Math.floor(prizePerWinner * taxRate);
      const netPayout = prizePerWinner - taxDeduction;

      for (const userId of winners) {
        // CORRECTED: use { _id: userId } and $inc on 'wallet.balance'
        const result = await User.updateOne(
          { _id: userId },
          { $inc: { 'wallet.balance': netPayout } }
        ).session(session);

        if (result.modifiedCount === 0) {
          throw new Error(`Wallet not found for user ${userId}.`);
        }
        console.log(`💰 User ${userId} won ${netPayout} ETB (tax: ${taxDeduction} ETB)`);
      }
      pool.winnerUserId = winners.join(',');
    } else {
      pool.winnerUserId = '';
    }

    pool.results = actualOutcomes;
    pool.status = 'Settled';
    await pool.save({ session });
    await session.commitTransaction();
    console.log(`✅ Sports jackpot ${poolId} settled. Winners: ${pool.winnerUserId || 'none'}`);
  } catch (err) {
    await session.abortTransaction();
    console.error('❌ Sports jackpot failed:', err);
    throw err;
  } finally {
    session.endSession();
  }
}

// ==============================================================================
// 🎰 CASINO JACKPOT
// ==============================================================================
export async function evaluateCasinoJackpot(
  poolId: string,
  playerStats?: Array<{ userId: string; multiplier: number; totalWon: number }>
): Promise<void> {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const pool = await JackpotPool.findById(poolId).session(session);
    if (!pool || pool.status !== 'Locked' || pool.type !== 'casino' || !pool.criteria) {
      throw new Error('Pool not found, not locked, not casino, or missing criteria.');
    }

    const tickets = await JackpotTicket.find({ jackpotPoolId: poolId }).session(session);
    if (tickets.length === 0) {
      pool.status = 'Settled';
      pool.winnerUserId = '';
      await pool.save({ session });
      await session.commitTransaction();
      console.log(`⚠️ Casino jackpot ${poolId} – no tickets.`);
      return;
    }

    let stats = playerStats;
    if (!stats) {
      stats = tickets.map(t => ({
        userId: t.userId,
        multiplier: t.multiplier || 0,
        totalWon: t.totalWon || 0,
      }));
    }

    const criteria = pool.criteria;
    let bestValue = -1;
    const candidates: string[] = [];

    for (const stat of stats) {
      const value = criteria === 'highest_multiplier' ? stat.multiplier : stat.totalWon;
      if (value > bestValue) {
        bestValue = value;
        candidates.length = 0;
        candidates.push(stat.userId);
      } else if (value === bestValue && value > 0) {
        candidates.push(stat.userId);
      }
    }

    if (candidates.length === 0 || bestValue === 0) {
      pool.status = 'Settled';
      pool.winnerUserId = '';
      await pool.save({ session });
      await session.commitTransaction();
      console.log(`⚠️ Casino jackpot ${poolId} – no positive score.`);
      return;
    }

    const taxRate = parseFloat(process.env.JACKPOT_TAX_RATE || '0.10');
    const prizePerWinner = Math.floor(pool.grandPrize / candidates.length);
    const taxDeduction = Math.floor(prizePerWinner * taxRate);
    const netPayout = prizePerWinner - taxDeduction;

    for (const userId of candidates) {
      const result = await User.updateOne(
        { _id: userId },
        { $inc: { 'wallet.balance': netPayout } }
      ).session(session);

      if (result.modifiedCount === 0) {
        throw new Error(`Wallet not found for user ${userId}.`);
      }

      const ticket = tickets.find(t => t.userId === userId);
      if (ticket) {
        ticket.isWinner = true;
        await ticket.save({ session });
      }
      console.log(`🎰 User ${userId} won ${netPayout} ETB (tax: ${taxDeduction} ETB)`);
    }

    pool.winnerUserId = candidates.join(',');
    pool.status = 'Settled';
    await pool.save({ session });
    await session.commitTransaction();
    console.log(`✅ Casino jackpot ${poolId} settled. Winners: ${pool.winnerUserId}`);
  } catch (err) {
    await session.abortTransaction();
    console.error('❌ Casino jackpot failed:', err);
    throw err;
  } finally {
    session.endSession();
  }
}