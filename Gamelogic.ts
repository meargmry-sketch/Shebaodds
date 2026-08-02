// gameLogic.ts

type GameParams = Record<string, any>;

interface GameResult {
  result: 'win' | 'lose' | 'push';
  profit: number;
  details: Record<string, any>;
}

// Helper: secure random (same as server)
function secureRandom(): number {
  return Math.random(); // Node's crypto could be used, but Math.random is fine for demo
}

// ─── GAME HANDLERS ──────────────────────────────────────────────

// 1. Dice
export function playDice(bet: number, params: GameParams): GameResult {
  const { type, guess } = params;
  const dice1 = Math.floor(secureRandom() * 6) + 1;
  const dice2 = Math.floor(secureRandom() * 6) + 1;
  const sum = dice1 + dice2;
  let win = false, payout = 0;

  if (type === 'sum') {
    const odds: Record<string, number> = { 2:36, 3:18, 4:12, 5:9, 6:7.2, 7:6, 8:7.2, 9:9, 10:12, 11:18, 12:36 };
    if (sum === parseInt(guess)) { win = true; payout = bet * (odds[guess] || 36); }
  } else if (type === 'exact') {
    const [g1, g2] = guess.split(',').map(Number);
    if ((dice1 === g1 && dice2 === g2) || (dice1 === g2 && dice2 === g1)) { win = true; payout = bet * 30; }
  }

  const profit = win ? payout - bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { dice1, dice2, sum } };
}

// 2. CoinFlip
export function playCoinFlip(bet: number, params: GameParams): GameResult {
  const { choice } = params;
  const flip = secureRandom() < 0.5 ? 'heads' : 'tails';
  const win = flip === choice;
  const profit = win ? bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { flip } };
}

// 3. Plinko
export function playPlinko(bet: number, params: GameParams): GameResult {
  const { risk = 'low' } = params;
  const rows = 8;
  let pos = 0;
  for (let i = 0; i < rows; i++) pos += secureRandom() < 0.5 ? 1 : -1;
  pos = Math.max(0, Math.min(rows, pos + rows/2));

  const multipliers: Record<string, number[]> = {
    low:  [3,1.5,1,0.5,0.5,1,1.5,3],
    medium: [10,3,1.5,0.5,0.5,1.5,3,10],
    high: [20,5,1.5,0.5,0.5,1.5,5,20]
  };
  const mult = (multipliers[risk] || multipliers.low)[pos];
  const payout = bet * mult;
  const profit = payout - bet;
  return { result: profit > 0 ? 'win' : (profit < 0 ? 'lose' : 'push'), profit, details: { position: pos, multiplier: mult } };
}

// 4. Blackjack
export function playBlackjack(bet: number, params: GameParams): GameResult {
  const suits = ['♠','♥','♦','♣'];
  const ranks = ['A','2','3','4','5','6','7','8','9','10','J','Q','K'];
  let deck: string[] = [];
  for (let s of suits) for (let r of ranks) deck.push(r+s);
  for (let i = deck.length-1; i>0; i--) {
    const j = Math.floor(secureRandom() * (i+1));
    [deck[i], deck[j]] = [deck[j], deck[i]];
  }
  const playerCards = [deck.pop()!, deck.pop()!];
  const dealerCards = [deck.pop()!, deck.pop()!];

  function handValue(cards: string[]): number {
    let val = 0, aces = 0;
    for (let c of cards) {
      let rank = c.slice(0, -1);
      if (rank === 'A') { aces++; val += 11; }
      else if (['J','Q','K'].includes(rank)) val += 10;
      else val += parseInt(rank);
    }
    while (val > 21 && aces > 0) { val -= 10; aces--; }
    return val;
  }

  let playerVal = handValue(playerCards);
  while (playerVal < 17 && playerCards.length < 5) {
    playerCards.push(deck.pop()!);
    playerVal = handValue(playerCards);
  }
  let dealerVal = handValue(dealerCards);
  while (dealerVal < 17 && dealerCards.length < 5) {
    dealerCards.push(deck.pop()!);
    dealerVal = handValue(dealerCards);
  }

  let profit = 0, result: 'win' | 'lose' | 'push' = 'lose';
  if (playerVal > 21) { profit = -bet; result = 'lose'; }
  else if (dealerVal > 21) { profit = bet; result = 'win'; }
  else if (playerVal > dealerVal) { profit = bet; result = 'win'; }
  else if (playerVal === dealerVal) { profit = 0; result = 'push'; }
  else { profit = -bet; result = 'lose'; }

  return { result, profit, details: { playerCards, dealerCards, playerVal, dealerVal } };
}

// 5. Roulette
export function playRoulette(bet: number, params: GameParams): GameResult {
  const { betType, number } = params;
  const spin = Math.floor(secureRandom() * 37);
  const colors: Record<number, string> = {
    0:'green', 1:'red',2:'black',3:'red',4:'black',5:'red',6:'black',
    7:'red',8:'black',9:'red',10:'black',11:'black',12:'red',
    13:'black',14:'red',15:'black',16:'red',17:'black',18:'red',
    19:'red',20:'black',21:'red',22:'black',23:'red',24:'black',
    25:'red',26:'black',27:'red',28:'black',29:'black',30:'red',
    31:'black',32:'red',33:'black',34:'red',35:'black',36:'red'
  };
  let win = false, payout = 0;
  if (betType === 'red') { win = colors[spin] === 'red'; payout = bet * 2; }
  else if (betType === 'black') { win = colors[spin] === 'black'; payout = bet * 2; }
  else if (betType === 'even') { win = spin !== 0 && spin % 2 === 0; payout = bet * 2; }
  else if (betType === 'odd') { win = spin !== 0 && spin % 2 === 1; payout = bet * 2; }
  else if (betType === 'number') { win = spin === parseInt(number); payout = bet * 36; }

  const profit = win ? payout - bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { spin, color: colors[spin] } };
}

// 6. Mines (fixed version)
export function playMines(bet: number, params: GameParams): GameResult {
  const { tile, mines = 3 } = params;
  const gridSize = 25;
  // Generate mine positions
  const minePositions: number[] = [];
  while (minePositions.length < mines) {
    const pos = Math.floor(secureRandom() * gridSize);
    if (!minePositions.includes(pos)) minePositions.push(pos);
  }
  const hit = minePositions.includes(tile);
  const profit = hit ? -bet : bet * 1.2;
  return { result: hit ? 'lose' : 'win', profit, details: { minePositions, tile, hit } };
}

// 7. Crash
export function playCrash(bet: number, params: GameParams): GameResult {
  const { action, multiplier } = params;
  if (action === 'cashout') {
    const profit = Math.round(bet * multiplier - bet);
    return { result: 'win', profit, details: { multiplier } };
  } else {
    return { result: 'lose', profit: -bet, details: { multiplier } };
  }
}

// 8. Tower
export function playTower(bet: number, params: GameParams): GameResult {
  const { action, level, multiplier } = params;
  if (action === 'cashout') {
    const profit = Math.round(bet * multiplier - bet);
    return { result: 'win', profit, details: { level, multiplier } };
  } else {
    return { result: 'lose', profit: -bet, details: { level } };
  }
}

// 9. Keno
export function playKeno(bet: number, params: GameParams): GameResult {
  const { matches } = params;
  const payout = matches * 2;
  const profit = matches > 0 ? Math.round(bet * payout - bet) : -bet;
  return { result: matches > 0 ? 'win' : 'lose', profit, details: { matches } };
}

// 10. Baccarat
export function playBaccarat(bet: number, params: GameParams): GameResult {
  const { bet: playerBet } = params;
  function cardValue() { return Math.floor(secureRandom() * 10) + 1; }
  const bankerTotal = (cardValue() + cardValue()) % 10;
  const playerTotal = (cardValue() + cardValue()) % 10;
  let result: 'win' | 'lose' | 'push' = 'lose';
  let profit = 0;
  if (playerBet === 'banker') {
    if (bankerTotal > playerTotal) { result = 'win'; profit = bet * 0.95 - bet; }
    else if (bankerTotal === playerTotal) { result = 'push'; profit = 0; }
    else { result = 'lose'; profit = -bet; }
  } else if (playerBet === 'player') {
    if (playerTotal > bankerTotal) { result = 'win'; profit = bet; }
    else if (playerTotal === bankerTotal) { result = 'push'; profit = 0; }
    else { result = 'lose'; profit = -bet; }
  } else {
    if (bankerTotal === playerTotal) { result = 'win'; profit = bet * 8 - bet; }
    else { result = 'lose'; profit = -bet; }
  }
  return { result, profit, details: { bankerTotal, playerTotal } };
}

// 11. Wheel of Fortune
export function playWheel(bet: number, params: GameParams): GameResult {
  const { multiplier, profit, result } = params;
  return { result: result || (profit > 0 ? 'win' : 'lose'), profit, details: { multiplier } };
}

// 12. Hilo
export function playHilo(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: { win } };
}

// 13. Sic Bo
export function playSicBo(bet: number, params: GameParams): GameResult {
  const { betType } = params;
  const dice = [1,2,3].map(() => Math.floor(secureRandom() * 6) + 1);
  const sum = dice.reduce((a,b) => a+b, 0);
  let win = false, payout = 0;
  if (betType === 'triple') {
    win = (dice[0] === dice[1] && dice[1] === dice[2]);
    payout = win ? bet * 30 : 0;
  } else if (betType.startsWith('sum')) {
    const target = parseInt(betType.replace('sum',''));
    win = (sum === target);
    payout = win ? bet * 6 : 0;
  }
  const profit = win ? payout - bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { dice, sum } };
}

// 14. Video Poker
export function playVideoPoker(bet: number, params: GameParams): GameResult {
  const { multiplier, profit } = params;
  return { result: multiplier > 0 ? 'win' : 'lose', profit, details: { multiplier } };
}

// 15. Bingo
export function playBingo(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: { win } };
}

// 16. Craps
export function playCraps(bet: number, params: GameParams): GameResult {
  const { win, profit, sum } = params;
  return { result: win ? 'win' : 'lose', profit, details: { sum } };
}

// 17. Dragon Tiger
export function playDragonTiger(bet: number, params: GameParams): GameResult {
  const { bet: playerBet } = params;
  const dragon = Math.floor(secureRandom() * 13) + 1;
  const tiger = Math.floor(secureRandom() * 13) + 1;
  let win = false, profit = 0;
  if (playerBet === 'dragon') {
    if (dragon > tiger) { win = true; profit = bet; }
    else { win = false; profit = -bet; }
  } else if (playerBet === 'tiger') {
    if (tiger > dragon) { win = true; profit = bet; }
    else { win = false; profit = -bet; }
  } else {
    if (dragon === tiger) { win = true; profit = bet * 8 - bet; }
    else { win = false; profit = -bet; }
  }
  return { result: win ? 'win' : 'lose', profit, details: { dragon, tiger } };
}

// 18. Andar Bahar
export function playAndarBahar(bet: number, params: GameParams): GameResult {
  const { bet: playerBet } = params;
  const winSide = secureRandom() < 0.5 ? 'andar' : 'bahar';
  const win = playerBet === winSide;
  const profit = win ? bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { winSide } };
}

// 19. Teen Patti
export function playTeenPatti(bet: number, params: GameParams): GameResult {
  const { win, profit, playerRank, compRank } = params;
  return { result: win ? 'win' : 'lose', profit, details: { playerRank, compRank } };
}

// 20. Lucky 7
export function playLucky7(bet: number, params: GameParams): GameResult {
  const { win, profit, sum } = params;
  return { result: win ? 'win' : 'lose', profit, details: { sum } };
}

// 21. Scratch Card
export function playScratch(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: { win } };
}

// ================================================================
// 🎰 REST OF 51 GAMES (22–51)
// ================================================================

// 22. Football Prediction
export function playFootball(bet: number, params: GameParams): GameResult {
  const { bet: userBet } = params;
  const homeGoals = Math.floor(secureRandom() * 5);
  const awayGoals = Math.floor(secureRandom() * 5);
  let result = 'draw';
  if (homeGoals > awayGoals) result = 'home';
  else if (awayGoals > homeGoals) result = 'away';
  const win = userBet === result;
  const profit = win ? bet * (result === 'draw' ? 3 : 2) - bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { homeGoals, awayGoals } };
}

// 23. Basketball Prediction
export function playBasketball(bet: number, params: GameParams): GameResult {
  const { bet: userBet } = params;
  const teamA = Math.floor(secureRandom() * 120);
  const teamB = Math.floor(secureRandom() * 120);
  const winTeam = teamA > teamB ? 'teamA' : 'teamB';
  const win = userBet === winTeam;
  const profit = win ? bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { teamA, teamB } };
}

// 24. Horse Racing
export function playHorseRacing(bet: number, params: GameParams): GameResult {
  const { win, profit, winner } = params;
  return { result: win ? 'win' : 'lose', profit, details: { winner } };
}

// 25. Spin & Win
export function playSpinWin(bet: number, params: GameParams): GameResult {
  const { multiplier, profit, result } = params;
  return { result: result || (profit > 0 ? 'win' : 'lose'), profit, details: { multiplier } };
}

// 26. Slot Machine
export function playSlot(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: { win } };
}

// 27. Red Dog
export function playRedDog(bet: number, params: GameParams): GameResult {
  const { result, profit } = params;
  return { result: result || 'lose', profit, details: { result } };
}

// 28. War
export function playWar(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : (profit === 0 ? 'push' : 'lose'), profit, details: { win } };
}

// 29. Pai Gow Poker
export function playPaiGow(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: { win } };
}

// 30. Dice Duels
export function playDiceDuels(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : (profit === 0 ? 'push' : 'lose'), profit, details: { win } };
}

// 31. Penalty
export function playPenalty(bet: number, params: GameParams): GameResult {
  const { score, profit } = params;
  return { result: score ? 'win' : 'lose', profit, details: { score } };
}

// 32. Chicken Road
export function playChickenRoad(bet: number, params: GameParams): GameResult {
  const { crash, profit, finished } = params;
  const result = crash ? 'lose' : (finished ? 'win' : 'lose');
  return { result, profit, details: { crash, finished } };
}

// 33. Chicken Shot
export function playChickenShot(bet: number, params: GameParams): GameResult {
  const { hit, profit } = params;
  return { result: hit ? 'win' : 'lose', profit, details: { hit } };
}

// 34. Mega Ball
export function playMegaBall(bet: number, params: GameParams): GameResult {
  const { win, profit, drawn } = params;
  return { result: win ? 'win' : 'lose', profit, details: { drawn } };
}

// 35. Poker Dice
export function playPokerDice(bet: number, params: GameParams): GameResult {
  const { hand, multiplier, profit } = params;
  const result = multiplier > 0 ? 'win' : 'lose';
  return { result, profit, details: { hand, multiplier } };
}

// 36. Lightning Dice
export function playLightningDice(bet: number, params: GameParams): GameResult {
  const { win, profit, sum, multiplier } = params;
  return { result: win ? 'win' : 'lose', profit, details: { sum, multiplier } };
}

// 37. Car Roulette
export function playCarRoulette(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: {} };
}

// 38. Knock Out
export function playKnockout(bet: number, params: GameParams): GameResult {
  const { win, profit, koRound } = params;
  return { result: win ? 'win' : 'lose', profit, details: { koRound } };
}

// 39. Rummy
export function playRummy(bet: number, params: GameParams): GameResult {
  const { win, profit, hand } = params;
  return { result: win ? 'win' : 'lose', profit, details: { hand } };
}

// 40. Darts
export function playDarts(bet: number, params: GameParams): GameResult {
  const { points, profit, win } = params;
  return { result: win ? 'win' : 'lose', profit, details: { points } };
}

// 41. Tennis
export function playTennis(bet: number, params: GameParams): GameResult {
  const { bet: playerBet } = params;
  const winner = secureRandom() < 0.5 ? 'A' : 'B';
  const win = playerBet === winner;
  const profit = win ? bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { winner } };
}

// 42. Baseball
export function playBaseball(bet: number, params: GameParams): GameResult {
  const { bet: playerBet } = params;
  const runs = Math.floor(secureRandom() * 10);
  const win = (playerBet === 'over' && runs > 5) || (playerBet === 'under' && runs <= 5);
  const profit = win ? bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { runs } };
}

// 43. Greyhound Racing
export function playGreyhound(bet: number, params: GameParams): GameResult {
  const { win, profit, winner } = params;
  return { result: win ? 'win' : 'lose', profit, details: { winner } };
}

// 44. Motorbike Racing
export function playMotorbike(bet: number, params: GameParams): GameResult {
  const { win, profit, winner } = params;
  return { result: win ? 'win' : 'lose', profit, details: { winner } };
}

// 45. Cricket
export function playCricket(bet: number, params: GameParams): GameResult {
  const { bet: playerBet } = params;
  const winner = secureRandom() < 0.5 ? 'A' : 'B';
  const win = playerBet === winner;
  const profit = win ? bet : -bet;
  return { result: win ? 'win' : 'lose', profit, details: { winner } };
}

// 46. Roulette 360
export function playRoulette360(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : 'lose', profit, details: {} };
}

// 47. Mega Wheel
export function playMegaWheel(bet: number, params: GameParams): GameResult {
  const { multiplier, profit, win } = params;
  return { result: win ? 'win' : 'lose', profit, details: { multiplier } };
}

// 48. Monopoly
export function playMonopoly(bet: number, params: GameParams): GameResult {
  const { win, profit, position, multiplier } = params;
  return { result: win ? 'win' : 'lose', profit, details: { position, multiplier } };
}

// 49. Virtual Sports
export function playVirtualSports(bet: number, params: GameParams): GameResult {
  const { win, profit, sport } = params;
  return { result: win ? 'win' : 'lose', profit, details: { sport } };
}

// 50. Texas Hold'em
export function playTexasHoldem(bet: number, params: GameParams): GameResult {
  const { win, profit } = params;
  return { result: win ? 'win' : (profit === 0 ? 'push' : 'lose'), profit, details: {} };
}

// 51. Aviator (alias for Crash with different naming)
export function playAviator(bet: number, params: GameParams): GameResult {
  const { action, multiplier, profit } = params;
  if (action === 'cashout') {
    return { result: 'win', profit, details: { multiplier } };
  } else {
    return { result: 'lose', profit: -bet, details: { multiplier } };
  }
}