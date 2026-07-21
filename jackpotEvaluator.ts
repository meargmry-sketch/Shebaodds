import mongoose from 'mongoose';
import { JackpotPool, JackpotTicket } from './jackpotSchema';

/**
 * Evaluate a traditional 12-match football jackpot pool.
 * @param poolId - The ID of the JackpotPool document.
 * @param actualOutcomes - Array of 12 results ("1", "X", "2").
 */
export async function evaluateJackpotPool(poolId: string, actualOutcomes: string[]) {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    // 1. Lock the pool document
    const pool = await JackpotPool.findById(poolId).session(session);
    if (!pool || pool.status !== 'Locked') {
      throw new Error('Jackpot pool not eligible for settlement');
    }

    pool.results = actualOutcomes;
    pool.status = 'Settled';
    await pool.save({ session });

    // 2. Fetch all tickets bought for this specific jackpot
    const tickets = await JackpotTicket.find({ jackpotPoolId: poolId }).session(session);

    for (const ticket of tickets) {
      let correctCount = 0;

      // Calculate matches guessed correctly
      for (let i = 0; i < 12; i++) {
        if (ticket.predictions[i] === actualOutcomes[i]) {
          correctCount++;
        }
      }

      ticket.correctGuessesCount = correctCount;

      // Check if the user guessed all 12 correctly
      if (correctCount === 12) {
        ticket.isWinner = true;

        // Distribute the 100,000 ETB Prize
        const prizeMoney = pool.grandPrize;
        const netProfit = prizeMoney - pool.entryFee;
        const taxDeduction = netProfit * 0.10; // 10% Withholding Tax
        const userNetPayout = prizeMoney - taxDeduction;

        // Credit User Wallet inside the secure session pipeline
        await mongoose.model('Wallet').updateOne(
          { userId: ticket.userId },
          { $inc: { cashBalance: userNetPayout } }
        ).session(session);
      }

      await ticket.save({ session });
    }

    await session.commitTransaction();
    console.log(`🏆 [JACKPOT SETTLED] Pool ${poolId} processed successfully.`);
  } catch (err) {
    await session.abortTransaction();
    console.error('❌ [JACKPOT ERROR] Settlement rolled back:', err);
  } finally {
    session.endSession();
  }
}

// ==============================================================================
// 🎰 NEW: Casino Jackpot Evaluator
// ==============================================================================

/**
 * Evaluates a casino jackpot pool (e.g., slot tournament, Aviator high‑score challenge).
 * The pool should have a `casinoGameId` and a `criteria` field defining how winners are determined.
 * This example supports two criteria: 'highest_multiplier' or 'highest_total_winnings'.
 *
 * @param poolId - The ID of the JackpotPool document (must contain casinoGameId and criteria).
 * @param playerStats - Optional array of per‑player stats if not stored in tickets.
 */
export async function evaluateCasinoJackpot(
  poolId: string,
  playerStats?: Array<{ userId: string; multiplier: number; totalWon: number }>
) {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const pool = await JackpotPool.findById(poolId).session(session);
    if (!pool || pool.status !== 'Locked') {
      throw new Error('Casino jackpot pool not eligible for settlement');
    }

    // Determine evaluation criteria from the pool document (default to 'highest_multiplier')
    const criteria = pool.criteria || 'highest_multiplier';

    // Fetch all tickets for this pool
    const tickets = await JackpotTicket.find({ jackpotPoolId: poolId }).session(session);

    // If playerStats are provided, use them; otherwise derive from tickets (if tickets contain stats)
    let stats: Array<{ userId: string; multiplier: number; totalWon: number }>;
    if (playerStats) {
      stats = playerStats;
    } else {
      // Assume each ticket stores the player's performance (e.g., highest multiplier)
      stats = tickets.map(t => ({
        userId: t.userId,
        multiplier: t.multiplier || 0,
        totalWon: t.totalWon || 0,
      }));
    }

    // Determine winner based on criteria
    let winnerUserId: string | null = null;
    let winningValue = 0;

    for (const stat of stats) {
      let value = 0;
      if (criteria === 'highest_multiplier') {
        value = stat.multiplier;
      } else if (criteria === 'highest_total_winnings') {
        value = stat.totalWon;
      }

      if (value > winningValue) {
        winningValue = value;
        winnerUserId = stat.userId;
      }
    }

    if (!winnerUserId) {
      throw new Error('No eligible winner found for casino jackpot.');
    }

    // Award prize
    const prizeMoney = pool.grandPrize;
    const netProfit = prizeMoney - pool.entryFee;
    const taxDeduction = netProfit * 0.10; // 10% Withholding Tax
    const userNetPayout = prizeMoney - taxDeduction;

    // Credit the winner's wallet
    await mongoose.model('Wallet').updateOne(
      { userId: winnerUserId },
      { $inc: { cashBalance: userNetPayout } }
    ).session(session);

    // Mark the winning ticket
    const winnerTicket = tickets.find(t => t.userId === winnerUserId);
    if (winnerTicket) {
      winnerTicket.isWinner = true;
      await winnerTicket.save({ session });
    }

    // Update pool status
    pool.status = 'Settled';
    pool.winnerUserId = winnerUserId;
    await pool.save({ session });

    await session.commitTransaction();
    console.log(`🎰 [CASINO JACKPOT] Pool ${poolId} settled. Winner: ${winnerUserId}, Prize: ${userNetPayout} ETB (Tax: ${taxDeduction} ETB)`);
  } catch (err) {
    await session.abortTransaction();
    console.error('❌ [CASINO JACKPOT ERROR] Settlement rolled back:', err);
  } finally {
    session.endSession();
  }
}