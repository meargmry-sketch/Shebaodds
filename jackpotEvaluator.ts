import mongoose from 'mongoose';
import { JackpotPool, JackpotTicket } from './jackpotSchema';

/**
 * Evaluate a traditional 12‑match sports jackpot.
 * @param poolId - The ID of the jackpot pool
 * @param actualOutcomes - Array of 12 outcomes (e.g., ['1','X','2',...])
 */
export async function evaluateSportsJackpot(poolId: string, actualOutcomes: string[]): Promise<void> {
  if (actualOutcomes.length !== 12) {
    throw new Error('Must provide exactly 12 outcomes');
  }

  const pool = await JackpotPool.findById(poolId);
  if (!pool) throw new Error('Jackpot pool not found');
  if (pool.type !== 'sports') throw new Error('Pool is not a sports jackpot');

  // Get all tickets for this pool
  const tickets = await JackpotTicket.find({ jackpotPoolId: poolId });

  // Calculate correct guesses for each ticket
  for (const ticket of tickets) {
    if (!ticket.predictions || ticket.predictions.length !== 12) continue;
    let correct = 0;
    for (let i = 0; i < 12; i++) {
      if (ticket.predictions[i] === actualOutcomes[i]) correct++;
    }
    ticket.correctGuessesCount = correct;
    ticket.isWinner = (correct === 12); // only full 12/12 wins
    await ticket.save();
  }

  // Find winner(s)
  const winners = await JackpotTicket.find({ jackpotPoolId: poolId, isWinner: true });
  if (winners.length > 0) {
    // Award prize – e.g., split grand prize among winners
    const prizePerWinner = Math.floor(pool.grandPrize / winners.length);
    for (const winner of winners) {
      // TODO: Add money to user's wallet
      console.log(`User ${winner.userId} wins ${prizePerWinner} ETB`);
    }
    pool.winnerUserId = winners.map(w => w.userId).join(',');
  }

  pool.status = 'Settled';
  pool.results = actualOutcomes;
  await pool.save();
}

/**
 * Evaluate a casino jackpot (e.g., highest multiplier for Aviator).
 * @param poolId - The ID of the jackpot pool
 * @param userPerformance - Map of userId -> { multiplier, totalWon } (or just one metric)
 */
export async function evaluateCasinoJackpot(
  poolId: string,
  userPerformance: Map<string, { multiplier: number; totalWon: number }>
): Promise<void> {
  const pool = await JackpotPool.findById(poolId);
  if (!pool) throw new Error('Jackpot pool not found');
  if (pool.type !== 'casino') throw new Error('Pool is not a casino jackpot');

  const tickets = await JackpotTicket.find({ jackpotPoolId: poolId });

  // Update tickets with performance data
  for (const ticket of tickets) {
    const perf = userPerformance.get(ticket.userId);
    if (perf) {
      ticket.multiplier = perf.multiplier;
      ticket.totalWon = perf.totalWon;
      await ticket.save();
    }
  }

  // Determine winner based on criteria
  let winnerId: string | null = null;
  if (pool.criteria === 'highest_multiplier') {
    const sorted = tickets.sort((a, b) => (b.multiplier || 0) - (a.multiplier || 0));
    if (sorted.length > 0) winnerId = sorted[0].userId;
  } else if (pool.criteria === 'highest_total_winnings') {
    const sorted = tickets.sort((a, b) => (b.totalWon || 0) - (a.totalWon || 0));
    if (sorted.length > 0) winnerId = sorted[0].userId;
  } else {
    throw new Error('Unknown criteria for casino jackpot');
  }

  if (winnerId) {
    // Mark winner
    await JackpotTicket.findOneAndUpdate(
      { jackpotPoolId: poolId, userId: winnerId },
      { isWinner: true }
    );
    pool.winnerUserId = winnerId;
    // TODO: Award grand prize
    console.log(`Casino jackpot winner: ${winnerId} gets ${pool.grandPrize} ETB`);
  }

  pool.status = 'Settled';
  await pool.save();
}