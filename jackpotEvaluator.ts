import mongoose from 'mongoose';
import { JackpotPool, JackpotTicket } from './jackpotSchema';
import User from './User'; // Adjust the import path to your actual User model

// ==============================================================================
// 🏆 SPORTS JACKPOT EVALUATOR (12 matches, 1X2)
// ==============================================================================

/**
 * Evaluates a sports jackpot pool. Assumes the pool is already locked.
 * @param poolId - The ID of the JackpotPool document.
 * @param actualOutcomes - Array of 12 results ("1", "X", "2") in order.
 */
export async function evaluateSportsJackpot(
  poolId: string,
  actualOutcomes: string[]
): Promise<void> {
  // Validate input
  if (!actualOutcomes || actualOutcomes.length !== 12) {
    throw new Error('Must provide exactly 12 outcomes for sports jackpot.');
  }
  if (!actualOutcomes.every((o) => ['1', 'X', '2'].includes(o))) {
    throw new Error('Outcomes must be one of "1", "X", or "2".');
  }

  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    // 1. Get and lock the pool
    const pool = await JackpotPool.findById(poolId).session(session);
    if (!pool) {
      throw new Error('Jackpot pool not found.');
    }
    if (pool.status !== 'Locked') {
      throw new Error(`Pool is not locked (current status: ${pool.status}).`);
    }
    if (pool.type !== 'sports') {
      throw new Error('This pool is not a sports jackpot.');
    }

    // 2. Fetch all tickets for this pool
    const tickets = await JackpotTicket.find({ jackpotPoolId: poolId }).session(session);
    if (tickets.length === 0) {
      // No participants – settle with no winner
      pool.results = actualOutcomes;
      pool.status = 'Settled';
      pool.winnerUserId = '';
      await pool.save({ session });
      await session.commitTransaction();
      console.log(`⚠️ Sports jackpot ${poolId} settled – no tickets found.`);
      return;
    }

    // 3. Calculate correct guesses for each ticket
    const winners: string[] = [];
    for (const ticket of tickets) {
      let correct = 0;
      if (ticket.predictions && ticket.predictions.length === 12) {
        for (let i = 0; i < 12; i++) {
          if (ticket.predictions[i] === actualOutcomes[i]) {
            correct++;
          }
        }
      }
      ticket.correctGuessesCount = correct;
      ticket.isWinner = (correct === 12);
      await ticket.save({ session });
      if (ticket.isWinner) {
        winners.push(ticket.userId);
      }
    }

    // 4. Distribute prize if there are winners
    const taxRate = parseFloat(process.env.JACKPOT_TAX_RATE || '0.10');

    if (winners.length > 0) {
      const prizePerWinner = Math.floor(pool.grandPrize / winners.length);
      const taxDeduction = Math.floor(prizePerWinner * taxRate);
      const netPayout = prizePerWinner - taxDeduction;

      for (const userId of winners) {
        // Credit the user's wallet (embedded in User model)
        const result = await User.updateOne(
          { _id: userId },
          { $inc: { 'wallet.balance': netPayout } }
        ).session(session);

        if (result.modifiedCount === 0) {
          throw new Error(`Wallet not found for user ${userId}.`);
        }

        // Optional: log tax transaction – see note below
        console.log(`💰 User ${userId} won ${netPayout} ETB (tax: ${taxDeduction} ETB)`);
      }

      // Store winner IDs as comma-separated
      pool.winnerUserId = winners.join(',');
    } else {
      pool.winnerUserId = '';
    }

    // 5. Finalize pool
    pool.results = actualOutcomes;
    pool.status = 'Settled';
    await pool.save({ session });

    await session.commitTransaction();
    console.log(`✅ Sports jackpot ${poolId} settled. Winners: ${pool.winnerUserId || 'none'}`);
  } catch (error) {
    await session.abortTransaction();
    console.error('❌ Sports jackpot settlement failed:', error);
    throw error; // Re-throw so the caller can handle it
  } finally {
    session.endSession();
  }
}

// ==============================================================================
// 🎰 CASINO JACKPOT EVALUATOR (based on criteria)
// ==============================================================================

/**
 * Evaluates a casino jackpot pool. Assumes the pool is already locked.
 * @param poolId - The ID of the JackpotPool document.
 * @param playerStats - Optional array of per-player stats.
 *                       If not provided, stats are read from the tickets themselves.
 */
export async function evaluateCasinoJackpot(
  poolId: string,
  playerStats?: Array<{ userId: string; multiplier: number; totalWon: number }>
): Promise<void> {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    // 1. Get and lock the pool
    const pool = await JackpotPool.findById(poolId).session(session);
    if (!pool) {
      throw new Error('Casino jackpot pool not found.');
    }
    if (pool.status !== 'Locked') {
      throw new Error(`Pool is not locked (current status: ${pool.status}).`);
    }
    if (pool.type !== 'casino') {
      throw new Error('This pool is not a casino jackpot.');
    }
    if (!pool.criteria) {
      throw new Error('Casino jackpot must have a criteria (highest_multiplier or highest_total_winnings).');
    }

    // 2. Fetch all tickets
    const tickets = await JackpotTicket.find({ jackpotPoolId: poolId }).session(session);
    if (tickets.length === 0) {
      pool.status = 'Settled';
      pool.winnerUserId = '';
      await pool.save({ session });
      await session.commitTransaction();
      console.log(`⚠️ Casino jackpot ${poolId} settled – no tickets found.`);
      return;
    }

    // 3. Determine stats (either from provided array or from tickets)
    let stats: Array<{ userId: string; multiplier: number; totalWon: number }>;
    if (playerStats) {
      stats = playerStats;
    } else {
      stats = tickets.map((t) => ({
        userId: t.userId,
        multiplier: t.multiplier || 0,
        totalWon: t.totalWon || 0,
      }));
    }

    // 4. Find winner(s) based on criteria
    const criteria = pool.criteria;
    let bestValue = -1;
    const candidateWinners: string[] = [];

    for (const stat of stats) {
      let value = 0;
      if (criteria === 'highest_multiplier') {
        value = stat.multiplier;
      } else if (criteria === 'highest_total_winnings') {
        value = stat.totalWon;
      }

      if (value > bestValue) {
        bestValue = value;
        candidateWinners.length = 0;
        candidateWinners.push(stat.userId);
      } else if (value === bestValue && value > 0) {
        candidateWinners.push(stat.userId);
      }
    }

    if (candidateWinners.length === 0 || bestValue === 0) {
      // No one achieved a positive score – no winner
      pool.status = 'Settled';
      pool.winnerUserId = '';
      await pool.save({ session });
      await session.commitTransaction();
      console.log(`⚠️ Casino jackpot ${poolId} – no eligible winner (best value: ${bestValue}).`);
      return;
    }

    // 5. Distribute prize among winners (split evenly)
    const taxRate = parseFloat(process.env.JACKPOT_TAX_RATE || '0.10');
    const prizePerWinner = Math.floor(pool.grandPrize / candidateWinners.length);
    const taxDeduction = Math.floor(prizePerWinner * taxRate);
    const netPayout = prizePerWinner - taxDeduction;

    for (const userId of candidateWinners) {
      // Credit wallet
      const result = await User.updateOne(
        { _id: userId },
        { $inc: { 'wallet.balance': netPayout } }
      ).session(session);

      if (result.modifiedCount === 0) {
        throw new Error(`Wallet not found for user ${userId}.`);
      }

      // Mark the corresponding ticket as winner
      const ticket = tickets.find((t) => t.userId === userId);
      if (ticket) {
        ticket.isWinner = true;
        await ticket.save({ session });
      }

      console.log(`🎰 User ${userId} won ${netPayout} ETB from casino jackpot (tax: ${taxDeduction} ETB)`);
    }

    // 6. Finalize pool
    pool.winnerUserId = candidateWinners.join(',');
    pool.status = 'Settled';
    await pool.save({ session });

    await session.commitTransaction();
    console.log(`✅ Casino jackpot ${poolId} settled. Winners: ${pool.winnerUserId}`);
  } catch (error) {
    await session.abortTransaction();
    console.error('❌ Casino jackpot settlement failed:', error);
    throw error;
  } finally {
    session.endSession();
  }
}