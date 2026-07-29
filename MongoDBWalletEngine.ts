import mongoose, { Schema, Document, ClientSession } from 'mongoose';

// ==================== WALLET SCHEMA ====================
interface IWallet extends Document {
  userId: string;
  cashBalance: number;
  bonusBalance: number;
}

const WalletSchema = new Schema<IWallet>({
  userId: { type: String, required: true, unique: true, index: true },
  cashBalance: { type: Number, required: true, min: 0 },
  bonusBalance: { type: Number, required: true, min: 0 }
});
export const Wallet = mongoose.model<IWallet>('Wallet', WalletSchema);

// ==================== WAGER SCHEMA ====================
interface IWager extends Document {
  userId: string;
  gameId: string;
  gameName: string;
  stake: number;
  multiplier?: number;
  payout: number;
  taxDeducted: number;
  status: 'Pending' | 'Won' | 'Lost';
  details?: any;
}

const WagerSchema = new Schema<IWager>({
  userId: { type: String, required: true, index: true },
  gameId: { type: String, required: true },
  gameName: { type: String, required: true },
  stake: { type: Number, required: true },
  multiplier: { type: Number },
  payout: { type: Number, required: true },
  taxDeducted: { type: Number, required: true },
  status: { type: String, enum: ['Pending', 'Won', 'Lost'], required: true },
  details: { type: Schema.Types.Mixed }
}, { timestamps: true });
export const Wager = mongoose.model<IWager>('Wager', WagerSchema);

// ==================== CASINO GAME ENGINE ====================
export class MongoDBWalletEngine {

  /**
   * Processes any casino game round.
   * Uses a MongoDB transaction to atomically update wallet and log wager.
   */
  public async processCasinoGame(
    userId: string,
    gameId: string,
    gameName: string,
    stake: number,
    params: any = {}
  ): Promise<{ success: boolean; netPayout: number; error?: string; details?: any }> {

    if (stake <= 0) {
      return { success: false, netPayout: 0, error: 'Stake must be greater than 0' };
    }

    const session: ClientSession = await mongoose.startSession();
    session.startTransaction();

    try {
      // Lock wallet
      const wallet = await Wallet.findOne({ userId }).session(session);
      if (!wallet) throw new Error('Wallet not found');
      if (wallet.cashBalance < stake) throw new Error('Insufficient balance');

      // Execute game logic
      const gameResult = this.playGame(gameId, stake, params);
      const { result, profit, details } = gameResult;

      // Calculate tax (only on net profit, using environment variable)
      const taxRate = parseFloat(process.env.TAX_RATE || '0.10');
      const netProfit = profit > 0 ? profit : 0;
      const taxAmount = Math.round(netProfit * taxRate * 100) / 100;
      const finalProfit = profit - taxAmount;   // net after tax (can be negative)
      const netPayout = stake + finalProfit;    // total returned to wallet

      // Update wallet: subtract stake, add net payout
      wallet.cashBalance = Math.round((wallet.cashBalance - stake + netPayout) * 100) / 100;
      await wallet.save({ session });

      // Record wager
      await Wager.create([{
        userId,
        gameId,
        gameName,
        stake,
        multiplier: details?.multiplier || null,
        payout: netPayout,
        taxDeducted: taxAmount,
        status: result === 'win' ? 'Won' : result === 'push' ? 'Pending' : 'Lost',
        details
      }], { session });

      await session.commitTransaction();
      return { success: true, netPayout, details };

    } catch (error: any) {
      await session.abortTransaction();
      return { success: false, netPayout: 0, error: error.message };
    } finally {
      await session.endSession();
    }
  }

  // ---------- Game Router ----------
  private playGame(gameId: string, stake: number, params: any): { result: 'win' | 'lose' | 'push'; profit: number; details: any } {
    switch (gameId) {
      case 'dice': return this.playDice(stake, params);
      case 'coinflip': return this.playCoinFlip(stake, params);
      case 'plinko': return this.playPlinko(stake, params);
      case 'blackjack': return this.playBlackjack(stake, params);
      case 'roulette': return this.playRoulette(stake, params);
      case 'mines': return this.playMines(stake, params);
      case 'crash': return this.playCrash(stake, params);
      case 'aviator': return this.playAviator(stake, params);
      case 'tower': return this.playTower(stake, params);
      case 'keno': return this.playKeno(stake, params);
      case 'baccarat': return this.playBaccarat(stake, params);
      case 'wheel': return this.playWheel(stake, params);
      case 'hilo': return this.playHilo(stake, params);
      case 'sicbo': return this.playSicBo(stake, params);
      case 'videopoker': return this.playVideoPoker(stake, params);
      case 'bingo': return this.playBingo(stake, params);
      case 'craps': return this.playCraps(stake, params);
      case 'dragontiger': return this.playDragonTiger(stake, params);
      case 'andarbahar': return this.playAndarBahar(stake, params);
      case 'teenpatti': return this.playTeenPatti(stake, params);
      case 'lucky7': return this.playLucky7(stake, params);
      case 'scratch': return this.playScratch(stake, params);
      case 'football': return this.playFootball(stake, params);
      case 'basketball': return this.playBasketball(stake, params);
      case 'horseracing': return this.playHorseRacing(stake, params);
      case 'spinwin': return this.playSpinWin(stake, params);
      case 'slot': return this.playSlot(stake, params);
      case 'reddog': return this.playRedDog(stake, params);
      case 'war': return this.playWar(stake, params);
      case 'paigow': return this.playPaiGow(stake, params);
      case 'diceduels': return this.playDiceDuels(stake, params);
      case 'penalty': return this.playPenalty(stake, params);
      case 'chickenroad': return this.playChickenRoad(stake, params);
      case 'chickenshot': return this.playChickenShot(stake, params);
      case 'megaball': return this.playMegaBall(stake, params);
      case 'pokerdice': return this.playPokerDice(stake, params);
      case 'lightningdice': return this.playLightningDice(stake, params);
      case 'carroulette': return this.playCarRoulette(stake, params);
      case 'knockout': return this.playKnockout(stake, params);
      case 'rummy': return this.playRummy(stake, params);
      case 'darts': return this.playDarts(stake, params);
      case 'tennis': return this.playTennis(stake, params);
      case 'baseball': return this.playBaseball(stake, params);
      case 'greyhound': return this.playGreyhound(stake, params);
      case 'motorbike': return this.playMotorbike(stake, params);
      case 'cricket': return this.playCricket(stake, params);
      case 'roulette360': return this.playRoulette360(stake, params);
      case 'megawheel': return this.playMegaWheel(stake, params);
      case 'monopoly': return this.playMonopoly(stake, params);
      case 'virtualsports': return this.playVirtualSports(stake, params);
      case 'texasholdem': return this.playTexasHoldem(stake, params);
      default: throw new Error(`Unsupported game: ${gameId}`);
    }
  }

  // ---------- All 51 Game Implementations ----------
  private playDice(stake: number, params: any) {
    const player = Math.floor(Math.random() * 6) + 1;
    const house = Math.floor(Math.random() * 6) + 1;
    const win = player > house;
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit, details: { playerRoll: player, houseRoll: house } };
  }

  private playCoinFlip(stake: number, params: any) {
    const result = Math.random() < 0.5 ? 'heads' : 'tails';
    const win = params.side === result;
    const profit = win ? stake * 0.9 : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { result, side: params.side } };
  }

  private playPlinko(stake: number, params: any) {
    const mult = [5.6, 2.1, 1.1, 1, 1, 1.1, 2.1, 5.6][Math.floor(Math.random() * 8)] || 1;
    const profit = stake * mult - stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { multiplier: mult } };
  }

  private playBlackjack(stake: number, params: any) {
    const draw = () => Math.min(Math.floor(Math.random() * 13) + 1, 10);
    const playerCards = [draw(), draw()];
    const dealerCards = [draw(), draw()];
    const sum = (c: number[]) => c.reduce((a, b) => a + b, 0);
    const pScore = sum(playerCards), dScore = sum(dealerCards);
    let result = 'lose', profit = -stake;
    if (pScore === 21 && playerCards.length === 2) { result = 'win'; profit = stake * 1.5; }
    else if (pScore > 21) { result = 'lose'; profit = -stake; }
    else if (dScore > 21) { result = 'win'; profit = stake; }
    else if (pScore > dScore) { result = 'win'; profit = stake; }
    else if (pScore === dScore) { result = 'push'; profit = 0; }
    return { result, profit: Math.round(profit * 100) / 100, details: { playerScore: pScore, dealerScore: dScore } };
  }

  private playRoulette(stake: number, params: any) {
    const number = Math.floor(Math.random() * 37);
    const reds = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36];
    const isRed = reds.includes(number), isEven = number > 0 && number % 2 === 0;
    let win = false, multiplier = 0;
    if (params.bet === 'red' && isRed) { win = true; multiplier = 1.9; }
    else if (params.bet === 'black' && !isRed && number !== 0) { win = true; multiplier = 1.9; }
    else if (params.bet === 'even' && isEven) { win = true; multiplier = 1.9; }
    else if (params.bet === 'odd' && !isEven && number !== 0) { win = true; multiplier = 1.9; }
    const profit = win ? stake * multiplier : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { number, isRed, isEven } };
  }

  private playMines(stake: number, params: any) {
    const total = 25, minesCount = params.mines || 3;
    const mines = new Set<number>();
    while (mines.size < minesCount) mines.add(Math.floor(Math.random() * total));
    const tile = params.tile || Math.floor(Math.random() * total);
    const hit = mines.has(tile);
    const profit = hit ? -stake : stake * 1.2;
    return { result: hit ? 'lose' : 'win', profit: Math.round(profit * 100) / 100, details: { mines: [...mines], tile, hit } };
  }

  private playCrash(stake: number, params: any) {
    const crashPoint = 1 + Math.random() * 9;
    const cashOut = params.action === 'cashout' ? Math.min(1 + Math.random() * 5, crashPoint) : 0;
    const win = params.action === 'cashout' && cashOut < crashPoint;
    const multiplier = win ? cashOut : 0;
    const profit = win ? stake * multiplier - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { crashPoint, multiplier } };
  }

  private playAviator(stake: number, params: any) { return this.playCrash(stake, params); }

  private playTower(stake: number, params: any) {
    if (params.action === 'cashout') {
      const profit = stake * (params.multiplier || 1) - stake;
      return { result: 'win', profit: Math.round(profit * 100) / 100, details: { level: params.level, multiplier: params.multiplier } };
    } else {
      return { result: 'lose', profit: -stake, details: { level: params.level } };
    }
  }

  private playKeno(stake: number, params: any) {
    const matches = params.matches || 0;
    const profit = matches > 0 ? stake * matches * 2 - stake : -stake;
    return { result: matches > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { matches } };
  }

  private playBaccarat(stake: number, params: any) {
    const card = () => Math.floor(Math.random() * 10) + 1;
    const bankerTotal = (card() + card()) % 10;
    const playerTotal = (card() + card()) % 10;
    let result = 'lose', profit = -stake;
    if (params.bet === 'banker') {
      if (bankerTotal > playerTotal) { result = 'win'; profit = stake * 0.95; }
      else if (bankerTotal === playerTotal) { result = 'push'; profit = 0; }
    } else if (params.bet === 'player') {
      if (playerTotal > bankerTotal) { result = 'win'; profit = stake; }
      else if (playerTotal === bankerTotal) { result = 'push'; profit = 0; }
    } else { // tie
      if (bankerTotal === playerTotal) { result = 'win'; profit = stake * 8; }
    }
    return { result, profit: Math.round(profit * 100) / 100, details: { bankerTotal, playerTotal } };
  }

  private playWheel(stake: number, params: any) {
    const multipliers = [2, 2, 2, 2, 2, 3, 3, 3, 3, 5, 5, 5, 10, 10, 20, 40, ...Array(38).fill(1)];
    const segment = Math.floor(Math.random() * multipliers.length);
    const mult = multipliers[segment] || 1;
    const profit = stake * mult - stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { segment, multiplier: mult } };
  }

  private playHilo(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const profit = win ? stake * 1.9 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playSicBo(stake: number, params: any) {
    const dice = [1, 2, 3].map(() => Math.floor(Math.random() * 6) + 1);
    const sum = dice.reduce((a, b) => a + b, 0);
    let win = false, payout = 0;
    if (params.betType === 'triple' && dice[0] === dice[1] && dice[1] === dice[2]) { win = true; payout = 30; }
    else if (params.betType.startsWith('sum')) {
      const target = parseInt(params.betType.replace('sum', ''));
      if (sum === target) { win = true; payout = 6; }
    }
    const profit = win ? stake * payout - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice, sum } };
  }

  private playVideoPoker(stake: number, params: any) {
    const mult = params.multiplier || 0;
    const profit = mult > 0 ? stake * mult - stake : -stake;
    return { result: mult > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { multiplier: mult } };
  }

  private playBingo(stake: number, params: any) {
    const win = Math.random() < 0.3;
    const profit = win ? stake * 2 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playCraps(stake: number, params: any) {
    const d1 = Math.floor(Math.random() * 6) + 1, d2 = Math.floor(Math.random() * 6) + 1;
    const sum = d1 + d2;
    const win = sum === 7 || sum === 11;
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice: [d1, d2], sum } };
  }

  private playDragonTiger(stake: number, params: any) {
    const dragon = Math.floor(Math.random() * 13) + 1;
    const tiger = Math.floor(Math.random() * 13) + 1;
    let win = false, profit = -stake;
    if (params.bet === 'dragon' && dragon > tiger) { win = true; profit = stake; }
    else if (params.bet === 'tiger' && tiger > dragon) { win = true; profit = stake; }
    else if (params.bet === 'tie' && dragon === tiger) { win = true; profit = stake * 8 - stake; }
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dragon, tiger } };
  }

  private playAndarBahar(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const side = win ? 'andar' : 'bahar';
    const profit = params.bet === side ? stake : -stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { side } };
  }

  private playTeenPatti(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const profit = win ? stake * 1.9 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playLucky7(stake: number, params: any) {
    const d1 = Math.floor(Math.random() * 6) + 1, d2 = Math.floor(Math.random() * 6) + 1;
    const sum = d1 + d2;
    const win = sum === 7;
    const profit = win ? stake * 3 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice: [d1, d2], sum } };
  }

  private playScratch(stake: number, params: any) {
    const win = Math.random() < 0.2;
    const profit = win ? stake * 5 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playFootball(stake: number, params: any) {
    const home = Math.floor(Math.random() * 5), away = Math.floor(Math.random() * 5);
    let result = 'draw';
    if (home > away) result = 'home';
    else if (away > home) result = 'away';
    const win = params.bet === result;
    const mult = result === 'draw' ? 2 : 1.5;
    const profit = win ? stake * mult - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { homeGoals: home, awayGoals: away } };
  }

  private playBasketball(stake: number, params: any) {
    const teamA = Math.floor(Math.random() * 120), teamB = Math.floor(Math.random() * 120);
    const win = params.bet === (teamA > teamB ? 'teamA' : 'teamB');
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { teamA, teamB } };
  }

  private playHorseRacing(stake: number, params: any) {
    const win = Math.random() < 0.15;
    const profit = win ? stake * 6 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { winner: params.bet } };
  }

  private playSpinWin(stake: number, params: any) {
    const multipliers = [0, 0, 0, 1.5, 1.5, 2, 2, 3, 5, 10];
    const mult = multipliers[Math.floor(Math.random() * multipliers.length)] || 0;
    const profit = mult > 0 ? stake * mult - stake : -stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { multiplier: mult } };
  }

  private playSlot(stake: number, params: any) {
    const symbols = ['🍒', '🍋', '🍊', '🔔', '💎', '7'];
    const reels = [symbols[Math.floor(Math.random() * 6)], symbols[Math.floor(Math.random() * 6)], symbols[Math.floor(Math.random() * 6)]];
    let mult = 0;
    if (reels[0] === reels[1] && reels[1] === reels[2]) mult = 5;
    else if (reels[0] === reels[1] || reels[1] === reels[2] || reels[0] === reels[2]) mult = 0.5;
    const profit = mult > 0 ? stake * mult - stake : -stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { reels, multiplier: mult } };
  }

  private playRedDog(stake: number, params: any) {
    const win = Math.random() < 0.4;
    const profit = win ? stake * 1.5 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playWar(stake: number, params: any) {
    const player = Math.floor(Math.random() * 13) + 1;
    const dealer = Math.floor(Math.random() * 13) + 1;
    let result = 'lose', profit = -stake;
    if (player > dealer) { result = 'win'; profit = stake; }
    else if (player === dealer) { result = 'push'; profit = 0; }
    return { result, profit: Math.round(profit * 100) / 100, details: { playerCard: player, dealerCard: dealer } };
  }

  private playPaiGow(stake: number, params: any) {
    const win = Math.random() < 0.4;
    const profit = win ? stake * 1.8 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playDiceDuels(stake: number, params: any) {
    const dice = [1, 2, 3].map(() => Math.floor(Math.random() * 6) + 1);
    const sum = dice.reduce((a, b) => a + b, 0);
    const win = sum >= 10;
    const profit = win ? stake * 1.5 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice, sum } };
  }

  private playPenalty(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const profit = win ? stake * 1.8 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { score: win } };
  }

  private playChickenRoad(stake: number, params: any) {
    const win = Math.random() < 0.7;
    const profit = win ? stake * 1.2 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { crash: !win } };
  }

  private playChickenShot(stake: number, params: any) {
    const win = Math.random() < 0.4;
    const profit = win ? stake * 2 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { hit: win } };
  }

  private playMegaBall(stake: number, params: any) {
    const drawn = Math.floor(Math.random() * 100);
    const win = drawn % 10 === 0;
    const profit = win ? stake * 10 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { drawn } };
  }

  private playPokerDice(stake: number, params: any) {
    const dice = [1, 2, 3, 4, 5].map(() => Math.floor(Math.random() * 6) + 1);
    const sum = dice.reduce((a, b) => a + b, 0);
    const win = sum >= 20;
    const profit = win ? stake * 2 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice, sum } };
  }

  private playLightningDice(stake: number, params: any) {
    const d1 = Math.floor(Math.random() * 6) + 1, d2 = Math.floor(Math.random() * 6) + 1;
    const sum = d1 + d2;
    const mult = Math.random() < 0.1 ? Math.floor(Math.random() * 5) + 2 : 1;
    const win = params.guess === sum;
    const profit = win ? stake * mult - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice: [d1, d2], sum, multiplier: mult } };
  }

  private playCarRoulette(stake: number, params: any) {
    const number = Math.floor(Math.random() * 37);
    const win = number === 0;
    const profit = win ? stake * 35 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { number } };
  }

  private playKnockout(stake: number, params: any) {
    const round = Math.floor(Math.random() * 12) + 1;
    const win = round <= 6;
    const profit = win ? stake * 1.8 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { round } };
  }

  private playRummy(stake: number, params: any) {
    const win = Math.random() < 0.3;
    const profit = win ? stake * 3 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }

  private playDarts(stake: number, params: any) {
    const score = Math.floor(Math.random() * 60);
    const win = score >= 40;
    const profit = win ? stake * 2 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { score } };
  }

  private playTennis(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { winner: params.bet } };
  }

  private playBaseball(stake: number, params: any) {
    const runs = Math.floor(Math.random() * 10);
    const win = (params.bet === 'over' && runs > 5) || (params.bet === 'under' && runs <= 5);
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { runs } };
  }

  private playGreyhound(stake: number, params: any) {
    const win = Math.random() < 0.2;
    const profit = win ? stake * 4 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { winner: params.bet } };
  }

  private playMotorbike(stake: number, params: any) {
    const win = Math.random() < 0.2;
    const profit = win ? stake * 4 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { winner: params.bet } };
  }

  private playCricket(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { winner: params.bet } };
  }

  private playRoulette360(stake: number, params: any) {
    const number = Math.floor(Math.random() * 37);
    const win = number % 2 === 0;
    const profit = win ? stake * 1.8 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { number } };
  }

  private playMegaWheel(stake: number, params: any) {
    const multipliers = Array(54).fill(1);
    // fill some with higher multipliers
    [0,1,2,3,4].forEach(i => multipliers[i] = 2);
    [5,6,7,8].forEach(i => multipliers[i] = 3);
    [9,10,11].forEach(i => multipliers[i] = 5);
    [12,13].forEach(i => multipliers[i] = 10);
    [14].forEach(i => multipliers[i] = 20);
    const segment = Math.floor(Math.random() * multipliers.length);
    const mult = multipliers[segment] || 1;
    const profit = stake * mult - stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { segment, multiplier: mult } };
  }

  private playMonopoly(stake: number, params: any) {
    const d1 = Math.floor(Math.random() * 6) + 1, d2 = Math.floor(Math.random() * 6) + 1;
    const sum = d1 + d2;
    const multipliers = [0, 1, 1, 2, 2, 3, 3, 4, 5, 6, 8, 10];
    const mult = multipliers[Math.min(sum, 11)] || 1;
    const profit = stake * mult - stake;
    return { result: profit > 0 ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { dice: [d1, d2], sum, multiplier: mult } };
  }

  private playVirtualSports(stake: number, params: any) {
    const win = Math.random() < 0.5;
    const profit = win ? stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { sport: params.sport } };
  }

  private playTexasHoldem(stake: number, params: any) {
    const win = Math.random() < 0.4;
    const profit = win ? stake * 1.5 - stake : -stake;
    return { result: win ? 'win' : 'lose', profit: Math.round(profit * 100) / 100, details: { win } };
  }
}

// Export a default instance for convenience
export default new MongoDBWalletEngine();