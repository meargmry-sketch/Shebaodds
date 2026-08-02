// ============================================
// SHEBAODDS - CASINO ROUTES
// 51+ Casino Games: listing, details, and play
// ============================================

import express, { Request, Response, Router } from 'express';
import { authenticate } from './authMiddleware';
import { checkResponsibleGambling, checkSelfExclusion } from './responsibleGamblingMiddleware';
import User from './User';
import Bet, { BET_STATUS } from './Bet';
import { Transaction, TRANSACTION_TYPES, TRANSACTION_STATUS } from './Transaction';
import { CasinoGame, CASINO_GAMES_DATA } from './Match';
import { processTaxForWinning } from './taxService';
import * as GameLogic from './Gamelogic';

const router: Router = express.Router();

// Maps a casino gameId to its handler in Gamelogic.ts
const GAME_HANDLERS: Record<string, (bet: number, params: Record<string, any>) => { result: 'win' | 'lose' | 'push'; profit: number; details: Record<string, any> }> = {
  dice: GameLogic.playDice,
  aviator: GameLogic.playAviator,
  coinflip: GameLogic.playCoinFlip,
  plinko: GameLogic.playPlinko,
  blackjack: GameLogic.playBlackjack,
  roulette: GameLogic.playRoulette,
  mines: GameLogic.playMines,
  crash: GameLogic.playCrash,
  tower: GameLogic.playTower,
  keno: GameLogic.playKeno,
  baccarat: GameLogic.playBaccarat,
  wheel: GameLogic.playWheel,
  hilo: GameLogic.playHilo,
  sicbo: GameLogic.playSicBo,
  videopoker: GameLogic.playVideoPoker,
  bingo: GameLogic.playBingo,
  craps: GameLogic.playCraps,
  dragontiger: GameLogic.playDragonTiger,
  andarbahar: GameLogic.playAndarBahar,
  teenpatti: GameLogic.playTeenPatti,
  lucky7: GameLogic.playLucky7,
  scratch: GameLogic.playScratch,
  football: GameLogic.playFootball,
  basketball: GameLogic.playBasketball,
  horseracing: GameLogic.playHorseRacing,
  spinwin: GameLogic.playSpinWin,
  slot: GameLogic.playSlot,
  reddog: GameLogic.playRedDog,
  war: GameLogic.playWar,
  paigow: GameLogic.playPaiGow,
  diceduels: GameLogic.playDiceDuels,
  penalty: GameLogic.playPenalty,
  chickenroad: GameLogic.playChickenRoad,
  chickenshot: GameLogic.playChickenShot,
  megaball: GameLogic.playMegaBall,
  pokerdice: GameLogic.playPokerDice,
  lightningdice: GameLogic.playLightningDice,
  carroulette: GameLogic.playCarRoulette,
  knockout: GameLogic.playKnockout,
  rummy: GameLogic.playRummy,
  darts: GameLogic.playDarts,
  tennis: GameLogic.playTennis,
  baseball: GameLogic.playBaseball,
  greyhound: GameLogic.playGreyhound,
  motorbike: GameLogic.playMotorbike,
  cricket: GameLogic.playCricket,
  roulette360: GameLogic.playRoulette360,
  megawheel: GameLogic.playMegaWheel,
  monopoly: GameLogic.playMonopoly,
  virtualsports: GameLogic.playVirtualSports,
  texasholdem: GameLogic.playTexasHoldem
};

// ==================== LIST GAMES ====================
router.get('/games', async (req: Request, res: Response) => {
  try {
    const { category } = req.query as any;
    const query: any = {};
    if (category) query.category = category;

    let games = await CasinoGame.find(query).sort({ category: 1, name: 1 });

    // Fall back to the static catalog if the DB hasn't been seeded yet
    if (games.length === 0) {
      const fallback = category
        ? CASINO_GAMES_DATA.filter((g) => g.category === category)
        : CASINO_GAMES_DATA;
      return res.json({ success: true, games: fallback, seeded: false });
    }

    res.json({ success: true, games, seeded: true });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load casino games', error: error.message });
  }
});

// ==================== GAME DETAILS ====================
router.get('/games/:gameId', async (req: Request, res: Response) => {
  try {
    const { gameId } = req.params;
    const game = await CasinoGame.findOne({ gameId });
    if (!game) {
      const fallback = CASINO_GAMES_DATA.find((g) => g.gameId === gameId);
      if (!fallback) {
        return res.status(404).json({ success: false, message: 'Casino game not found' });
      }
      return res.json({ success: true, game: fallback, seeded: false });
    }
    res.json({ success: true, game, seeded: true });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load casino game', error: error.message });
  }
});

// ==================== PLAY A GAME ====================
router.post('/play/:gameId', authenticate, checkSelfExclusion, checkResponsibleGambling, async (req: any, res: Response) => {
  try {
    const { gameId } = req.params;
    const { stake, params } = req.body as { stake: number; params?: Record<string, any> };

    const handler = GAME_HANDLERS[gameId];
    if (!handler) {
      return res.status(404).json({ success: false, message: `Unsupported or unimplemented game: ${gameId}` });
    }

    const stakeAmount = parseFloat(stake as any);
    if (!stakeAmount || stakeAmount <= 0) {
      return res.status(400).json({ success: false, message: 'Invalid stake amount' });
    }

    const gameMeta = (await CasinoGame.findOne({ gameId })) || CASINO_GAMES_DATA.find((g) => g.gameId === gameId);
    if (gameMeta) {
      if (stakeAmount < gameMeta.minBet) {
        return res.status(400).json({ success: false, message: `Minimum stake is ${gameMeta.minBet} ETB` });
      }
      if (stakeAmount > gameMeta.maxBet) {
        return res.status(400).json({ success: false, message: `Maximum stake is ${gameMeta.maxBet} ETB` });
      }
    }

    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    if (user.wallet.balance < stakeAmount) {
      return res.status(400).json({ success: false, message: 'Insufficient balance' });
    }

    // Run the actual game logic
    const outcome = handler(stakeAmount, params || {});

    const previousBalance = user.wallet.balance;
    user.wallet.balance += outcome.profit;
    user.wallet.totalWagered += stakeAmount;
    user.statistics.totalBets += 1;

    let netWinAfterTax = 0;
    if (outcome.profit > 0) {
      user.wallet.totalWon += outcome.profit;
      user.statistics.totalWins += 1;
    } else if (outcome.profit < 0) {
      user.wallet.totalLost += Math.abs(outcome.profit);
    }

    const bet = new Bet({
      userId: user._id,
      betType: 'casino',
      marketType: 'casino',
      isCasinoBet: true,
      casinoGameId: gameId,
      selection: gameMeta ? (gameMeta as any).name : gameId,
      odds: Math.max(1.01, outcome.details?.multiplier || 1.01),
      stake: stakeAmount,
      potentialWin: outcome.profit > 0 ? stakeAmount + outcome.profit : 0,
      actualWin: outcome.profit > 0 ? outcome.profit : 0,
      status: outcome.result === 'win' ? BET_STATUS.WON : outcome.result === 'push' ? BET_STATUS.VOID : BET_STATUS.LOST,
      ipAddress: req.ip,
      metadata: outcome.details
    });
    await bet.save();

    // Apply statutory withholding tax on net winnings
    if (outcome.profit > 0) {
      const taxResult = await processTaxForWinning(bet._id, user._id, outcome.profit);
      netWinAfterTax = taxResult.netWinning;
      user.wallet.balance -= taxResult.taxAmount;
      user.wallet.totalTaxPaid += taxResult.taxAmount;
    }

    await user.save();

    const transaction = new Transaction({
      userId: user._id,
      betId: bet._id,
      type: outcome.profit > 0 ? TRANSACTION_TYPES.BET_WIN : TRANSACTION_TYPES.BET_LOSS,
      amount: Math.abs(outcome.profit),
      taxAmount: outcome.profit > 0 ? outcome.profit - netWinAfterTax : 0,
      netAmount: outcome.profit > 0 ? netWinAfterTax : Math.abs(outcome.profit),
      previousBalance,
      newBalance: user.wallet.balance,
      status: TRANSACTION_STATUS.COMPLETED,
      completedAt: new Date(),
      metadata: { gameId, result: outcome.result }
    });
    await transaction.save();

    if (gameMeta && (gameMeta as any).save) {
      (gameMeta as any).timesPlayed += 1;
      (gameMeta as any).totalWagered += stakeAmount;
      if (outcome.profit > 0) (gameMeta as any).totalWon += outcome.profit;
      await (gameMeta as any).save();
    }

    req.io?.to(`user_${user._id}`).emit('wallet_update', { balance: user.wallet.balance });

    res.json({
      success: true,
      result: outcome.result,
      profit: outcome.profit,
      details: outcome.details,
      balance: user.wallet.balance,
      betId: bet._id
    });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to play casino game', error: error.message });
  }
});

// ==================== PLAY HISTORY ====================
router.get('/history', authenticate, async (req: any, res: Response) => {
  try {
    const { limit = '50', page = '1' } = req.query as any;
    const limitNum = parseInt(limit, 10) || 50;
    const pageNum = parseInt(page, 10) || 1;
    const skip = (pageNum - 1) * limitNum;

    const [bets, total] = await Promise.all([
      Bet.find({ userId: req.user._id, isCasinoBet: true }).sort({ createdAt: -1 }).skip(skip).limit(limitNum),
      Bet.countDocuments({ userId: req.user._id, isCasinoBet: true })
    ]);

    res.json({ success: true, bets, total, page: pageNum, pages: Math.ceil(total / limitNum) });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load casino history', error: error.message });
  }
});

export default router;
