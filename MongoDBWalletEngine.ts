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

// ==================== WAGER SCHEMA (enhanced for all games) ====================
interface IWager extends Document {
  userId: string;
  gameId: string;          // e.g., 'dice', 'aviator'
  gameName: string;
  stake: number;
  multiplier?: number;      // For crash/aviator games
  payout: number;
  taxDeducted: number;
  status: 'Pending' | 'Won' | 'Lost';
  details?: any;            // Store game-specific outcome details
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
   * Processes a casino game round for any of the 51 supported games.
   * Uses an ACID transaction to atomically update wallet and log wager.
   */
  public async processCasinoGame(
    userId: string,
    gameId: string,
    gameName: string,
    stake: number,
    params: any = {}
  ): Promise<{ success: boolean; netPayout: number; error?: string; details?: any }> {

    // Validate stake
    if (stake <= 0) {
      return { success: false, netPayout: 0, error: 'Stake must be greater than 0' };
    }

    const session: ClientSession = await mongoose.startSession();
    session.startTransaction();

    try {
      // Lock wallet
      const wallet = await Wallet.findOne({ userId }).session(session);
      if (!wallet) {
        throw new Error('Wallet not found');
      }
      if (wallet.cashBalance < stake) {
        throw new Error('Insufficient balance');
      }

      // Execute game logic
      const gameResult = this.playGame(gameId, stake, params);
      const { result, profit, details } = gameResult;

      // Calculate tax (10% on net profit)
      const netProfit = profit > 0 ? profit : 0;
      const taxAmount = netProfit * 0.10;
      const finalNetReturn = profit - taxAmount; // profit already includes stake? Let's adjust:
      // In our game logic, profit = win - stake (net), so payout = stake + profit
      const grossPayout = stake + profit;
      const netPayout = grossPayout - taxAmount;

      // Update wallet
      wallet.cashBalance = wallet.cashBalance - stake + netPayout;
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

  /**
   * Game logic router – all 51 games implemented here.
   */
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
      default:
        throw new Error(`Unsupported game: ${gameId}`);
    }
  }

  // ---------- GAME IMPLEMENTATIONS (all 51) ----------
  private playDice(stake: number, params: any) { const p=Math.floor(Math.random()*6)+1, h=Math.floor(Math.random()*6)+1, w=p>h; return { result: w?'win':'lose', profit: w?stake:-stake, details:{playerRoll:p,houseRoll:h}}; }
  private playCoinFlip(stake: number, params: any) { const r=Math.random()<0.5?'heads':'tails', w=params.side===r; return { result: w?'win':'lose', profit: w?stake*0.9:-stake, details:{result:r,side:params.side}}; }
  private playPlinko(stake: number, params: any) { const rows=params.rows||12; const m=[5.6,2.1,1.1,1,1,1.1,2.1,5.6]; const mult=m[Math.floor(Math.random()*m.length)]||1; const profit=stake*mult-stake; return { result: profit>0?'win':'lose', profit, details:{multiplier:mult,rows}}; }
  private playBlackjack(stake: number, params: any) { /* simplified */ const g=()=>Math.min(Math.floor(Math.random()*13)+1,10); const p=[g(),g()], d=[g(),g()]; const s=(c)=>c.reduce((a,b)=>a+b,0); const ps=s(p), ds=s(d); let res='lose', pr=-stake; if(ps===21&&p.length===2){res='win';pr=stake*1.5;}else if(ps>21){res='lose';pr=-stake;}else if(ds>21){res='win';pr=stake;}else if(ps>ds){res='win';pr=stake;}else if(ps===ds){res='push';pr=0;} return { result: res, profit: Math.round(pr*100)/100, details:{playerScore:ps,dealerScore:ds}}; }
  private playRoulette(stake: number, params: any) { const n=Math.floor(Math.random()*37); const r=[1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36]; const isR=r.includes(n), isE=n>0&&n%2===0; let w=false,m=0; if(params.bet==='red'&&isR){w=true;m=1.9;}else if(params.bet==='black'&&!isR&&n!==0){w=true;m=1.9;}else if(params.bet==='even'&&isE){w=true;m=1.9;}else if(params.bet==='odd'&&!isE&&n!==0){w=true;m=1.9;} const p=w?stake*m:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{number:n,isRed:isR,isEven:isE}}; }
  private playMines(stake: number, params: any) { const gs=25,mc=params.mines||3; const mines=[]; while(mines.length<mc){const p=Math.floor(Math.random()*gs);if(!mines.includes(p))mines.push(p);} const t=params.tile||Math.floor(Math.random()*gs); const h=mines.includes(t); const p=h?-stake:stake*1.2; return { result: h?'lose':'win', profit: Math.round(p*100)/100, details:{mines,mines,tile:t,hit:h}}; }
  private playCrash(stake: number, params: any) { const cp=1+Math.random()*9; const co=params.action==='cashout'?Math.min(1+Math.random()*5,cp):0; const w=params.action==='cashout'&&co<cp; const ml=w?co:0; const p=w?stake*ml:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{crashPoint:cp,multiplier:ml}}; }
  private playAviator(stake: number, params: any) { return this.playCrash(stake, params); }
  private playTower(stake: number, params: any) { if(params.action==='cashout'){return {result:'win', profit:Math.round(stake*params.multiplier-stake), details:{level:params.level,multiplier:params.multiplier}};}else{return {result:'lose', profit:-stake, details:{level:params.level}};} }
  private playKeno(stake: number, params: any) { const matches=params.matches||0; const payout=matches*2; const p=matches>0?Math.round(stake*payout-stake):-stake; return { result: matches>0?'win':'lose', profit:p, details:{matches}}; }
  private playBaccarat(stake: number, params: any) { const b=params.bet; const c=()=>Math.floor(Math.random()*10)+1; const bt=(c()+c())%10, pt=(c()+c())%10; let res='lose', p=-stake; if(b==='banker'){if(bt>pt){res='win';p=stake*0.95-stake;}else if(bt===pt){res='push';p=0;}}else if(b==='player'){if(pt>bt){res='win';p=stake;}else if(pt===bt){res='push';p=0;}}else{if(bt===pt){res='win';p=stake*8-stake;}} return { result: res, profit: Math.round(p*100)/100, details:{bankerTotal:bt,playerTotal:pt}}; }
  private playWheel(stake: number, params: any) { const s=Math.floor(Math.random()*54); const mult=[2,2,2,2,2,3,3,3,3,5,5,5,10,10,20,40,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1][s]||1; const p=stake*mult-stake; return { result: p>0?'win':'lose', profit: Math.round(p*100)/100, details:{segment:s,multiplier:mult}}; }
  private playHilo(stake: number, params: any) { /* simplified */ const r=Math.random()<0.5; return { result: r?'win':'lose', profit: r?stake*1.9:-stake, details:{win:r}}; }
  private playSicBo(stake: number, params: any) { const d=[1,2,3].map(()=>Math.floor(Math.random()*6)+1); const s=d.reduce((a,b)=>a+b,0); let win=false,payout=0; if(params.betType==='triple'){win=d[0]===d[1]&&d[1]===d[2];payout=win?stake*30:0;}else if(params.betType.startsWith('sum')){const t=parseInt(params.betType.replace('sum',''));win=s===t;payout=win?stake*6:0;} const p=win?payout-stake:-stake; return { result: win?'win':'lose', profit: Math.round(p*100)/100, details:{dice:d,sum:s}}; }
  private playVideoPoker(stake: number, params: any) { const mult=params.multiplier||0; const p=mult>0?stake*mult-stake:-stake; return { result: mult>0?'win':'lose', profit: Math.round(p*100)/100, details:{multiplier:mult}}; }
  private playBingo(stake: number, params: any) { const w=Math.random()<0.3; const p=w?stake*2-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
  private playCraps(stake: number, params: any) { const d1=Math.floor(Math.random()*6)+1,d2=Math.floor(Math.random()*6)+1; const s=d1+d2; const w=s===7||s===11; const p=w?stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{dice:[d1,d2],sum:s}}; }
  private playDragonTiger(stake: number, params: any) { const d=Math.floor(Math.random()*13)+1,t=Math.floor(Math.random()*13)+1; let w=false,p=0; if(params.bet==='dragon'){if(d>t){w=true;p=stake;}else{p=-stake;}}else if(params.bet==='tiger'){if(t>d){w=true;p=stake;}else{p=-stake;}}else{if(d===t){w=true;p=stake*8-stake;}else{p=-stake;}} return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{dragon:d,tiger:t}}; }
  private playAndarBahar(stake: number, params: any) { const w=Math.random()<0.5; const p=params.bet===(w?'andar':'bahar')?stake:-stake; return { result: p>0?'win':'lose', profit: Math.round(p*100)/100, details:{winSide:w?'andar':'bahar'}}; }
  private playTeenPatti(stake: number, params: any) { const w=Math.random()<0.5; const p=w?stake*1.9-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
  private playLucky7(stake: number, params: any) { const d1=Math.floor(Math.random()*6)+1,d2=Math.floor(Math.random()*6)+1; const s=d1+d2; const w=s===7; const p=w?stake*3-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{dice:[d1,d2],sum:s}}; }
  private playScratch(stake: number, params: any) { const w=Math.random()<0.2; const p=w?stake*5-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
  private playFootball(stake: number, params: any) { const hg=Math.floor(Math.random()*5),ag=Math.floor(Math.random()*5); let r='draw'; if(hg>ag)r='home'; else if(ag>hg)r='away'; const w=params.bet===r; const p=w?stake*(r==='draw'?2:1.5)-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{homeGoals:hg,awayGoals:ag}}; }
  private playBasketball(stake: number, params: any) { const a=Math.floor(Math.random()*120),b=Math.floor(Math.random()*120); const w=params.bet===(a>b?'teamA':'teamB'); const p=w?stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{teamA:a,teamB:b}}; }
  private playHorseRacing(stake: number, params: any) { const w=Math.random()<0.15; const p=w?stake*6-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{winner:params.bet}}; }
  private playSpinWin(stake: number, params: any) { const s=Math.floor(Math.random()*10); const mult=[0,0,0,1.5,1.5,2,2,3,5,10][s]||0; const p=mult>0?stake*mult-stake:-stake; return { result: p>0?'win':'lose', profit: Math.round(p*100)/100, details:{multiplier:mult}}; }
  private playSlot(stake: number, params: any) { const r=['🍒','🍋','🍊','🔔','💎','7']; const re=[r[Math.floor(Math.random()*6)],r[Math.floor(Math.random()*6)],r[Math.floor(Math.random()*6)]]; let w=false,m=0; if(re[0]===re[1]&&re[1]===re[2]){w=true;m=5;}else if(re[0]===re[1]||re[1]===re[2]||re[0]===re[2]){w=true;m=0.5;} const p=w?stake*m-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{reels:re,multiplier:m}}; }
  private playRedDog(stake: number, params: any) { const w=Math.random()<0.4; const p=w?stake*1.5-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
  private playWar(stake: number, params: any) { const c1=Math.floor(Math.random()*13)+1,c2=Math.floor(Math.random()*13)+1; let res='lose',p=-stake; if(c1>c2){res='win';p=stake;}else if(c1===c2){res='push';p=0;} return { result: res, profit: Math.round(p*100)/100, details:{playerCard:c1,dealerCard:c2}}; }
  private playPaiGow(stake: number, params: any) { const w=Math.random()<0.4; const p=w?stake*1.8-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
  private playDiceDuels(stake: number, params: any) { const d=[1,2,3].map(()=>Math.floor(Math.random()*6)+1); const s=d.reduce((a,b)=>a+b,0); const w=s>=10; const p=w?stake*1.5-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{dice:d,sum:s}}; }
  private playPenalty(stake: number, params: any) { const w=Math.random()<0.5; const p=w?stake*1.8-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{score:w}}; }
  private playChickenRoad(stake: number, params: any) { const w=Math.random()<0.7; const p=w?stake*1.2-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{crash:!w}}; }
  private playChickenShot(stake: number, params: any) { const w=Math.random()<0.4; const p=w?stake*2-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{hit:w}}; }
  private playMegaBall(stake: number, params: any) { const drawn=Math.floor(Math.random()*100); const w=drawn%10===0; const p=w?stake*10-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{drawn}}; }
  private playPokerDice(stake: number, params: any) { const d=[1,2,3,4,5].map(()=>Math.floor(Math.random()*6)+1); const s=d.reduce((a,b)=>a+b,0); const w=s>=20; const p=w?stake*2-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{dice:d,sum:s}}; }
  private playLightningDice(stake: number, params: any) { const d1=Math.floor(Math.random()*6)+1,d2=Math.floor(Math.random()*6)+1; const s=d1+d2; const mult=Math.random()<0.1?Math.floor(Math.random()*5)+2:1; const w=params.guess===s; const p=w?stake*mult-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{dice:[d1,d2],sum:s,multiplier:mult}}; }
  private playCarRoulette(stake: number, params: any) { const n=Math.floor(Math.random()*37); const w=n===0; const p=w?stake*35-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{number:n}}; }
  private playKnockout(stake: number, params: any) { const r=Math.floor(Math.random()*12)+1; const w=r<=6; const p=w?stake*1.8-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{round:r}}; }
  private playRummy(stake: number, params: any) { const w=Math.random()<0.3; const p=w?stake*3-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
  private playDarts(stake: number, params: any) { const s=Math.floor(Math.random()*60); const w=s>=40; const p=w?stake*2-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{score:s}}; }
  private playTennis(stake: number, params: any) { const w=Math.random()<0.5; const p=w?stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{winner:params.bet}}; }
  private playBaseball(stake: number, params: any) { const r=Math.floor(Math.random()*10); const w=(params.bet==='over'&&r>5)||(params.bet==='under'&&r<=5); const p=w?stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{runs:r}}; }
  private playGreyhound(stake: number, params: any) { const w=Math.random()<0.2; const p=w?stake*4-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{winner:params.bet}}; }
  private playMotorbike(stake: number, params: any) { const w=Math.random()<0.2; const p=w?stake*4-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{winner:params.bet}}; }
  private playCricket(stake: number, params: any) { const w=Math.random()<0.5; const p=w?stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{winner:params.bet}}; }
  private playRoulette360(stake: number, params: any) { const n=Math.floor(Math.random()*37); const w=n%2===0; const p=w?stake*1.8-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{number:n}}; }
  private playMegaWheel(stake: number, params: any) { const s=Math.floor(Math.random()*54); const mult=[1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1][s]||2; const p=stake*mult-stake; return { result: p>0?'win':'lose', profit: Math.round(p*100)/100, details:{segment:s,multiplier:mult}}; }
  private playMonopoly(stake: number, params: any) { const d1=Math.floor(Math.random()*6)+1,d2=Math.floor(Math.random()*6)+1; const s=d1+d2; const mult=[0,1,1,2,2,3,3,4,5,6,8,10][s]||1; const p=stake*mult-stake; return { result: p>0?'win':'lose', profit: Math.round(p*100)/100, details:{dice:[d1,d2],sum:s,multiplier:mult}}; }
  private playVirtualSports(stake: number, params: any) { const w=Math.random()<0.5; const p=w?stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{sport:params.sport}}; }
  private playTexasHoldem(stake: number, params: any) { const w=Math.random()<0.4; const p=w?stake*1.5-stake:-stake; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{win:w}}; }
}

// Legacy method for backward compatibility (optional)
export { MongoDBWalletEngine as default };