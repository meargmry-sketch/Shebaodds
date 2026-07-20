import React, { useState, useEffect, useMemo } from 'react';
import { 
  LayoutDashboard, Users, Trophy, Landmark, Receipt, FileBarChart2, 
  Gift, Settings, ShieldAlert, FileText, Mail, Search, Bell, 
  ChevronDown, Plus, Lock, Unlock, Trash2, Edit2, Check, X, 
  ArrowUpRight, ArrowDownRight, Wallet, Filter, CheckCircle2, 
  AlertTriangle, Play, Eye, DollarSign, Activity, Globe, Gamepad2, 
  Star, RefreshCcw, Zap, Coins, Swords, Car, Bike, Dice6, Atom, Sparkles
} from 'lucide-react';

export default function LiveUpcomingMatches({ wsUrl = 'ws://127.0.0.1:9090' }) {
  // --- STATE SYSTEM ---
  const [activeTab, setActiveTab] = useState('dashboard');
  const [searchQuery, setSearchQuery] = useState('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const [isFinanceExpanded, setIsFinanceExpanded] = useState(true);

  // System Configuration States (Editable in Settings Modal)
  const [systemSettings, setSystemSettings] = useState({
    taxRate: 10,
    welcomeBonus: 100,
    minBet: 10,
    maxBet: 50000,
    maxDailyLoss: 20000,
    oddsRefreshRate: 5
  });

  // Clock state
  const [currentTime, setCurrentTime] = useState(new Date());

  // Main Datasets with Live Update Support
  const [matches, setMatches] = useState([ /* ... existing matches ... */ ]);
  const [transactions, setTransactions] = useState([ /* ... existing transactions ... */ ]);
  const [liveBets, setLiveBets] = useState([ /* ... existing live bets ... */ ]);
  const [users, setUsers] = useState([ /* ... existing users ... */ ]);
  
  // ==========================================================
  // 🎰 NEW: CASINO STATE (51+ Games)
  // ==========================================================
  const [casinoBroadcasts, setCasinoBroadcasts] = useState([]);
  const [selectedGame, setSelectedGame] = useState(null);
  const [casinoBalance, setCasinoBalance] = useState(25000);
  const [casinoBetAmount, setCasinoBetAmount] = useState(10);
  const [casinoIsBetPanelOpen, setCasinoIsBetPanelOpen] = useState(false);
  const [casinoGameState, setCasinoGameState] = useState({});
  const [casinoShowResultModal, setCasinoShowResultModal] = useState(false);
  const [casinoResultData, setCasinoResultData] = useState(null);
  const [casinoLoading, setCasinoLoading] = useState(false);
  const [casinoHistory, setCasinoHistory] = useState([]);
  const [casinoFavorites, setCasinoFavorites] = useState(() => {
    const saved = localStorage.getItem('shebaodds_admin_favorite_games');
    return saved ? JSON.parse(saved) : [];
  });

  // 51 GAMES DATA
  const GAMES = [
    { id: 'dice', name: 'Dice', nameAm: 'ዳይስ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'aviator', name: 'Aviator', nameAm: 'አቪዬተር', icon: '✈️', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'coinflip', name: 'CoinFlip', nameAm: 'ሳንቲም', icon: '🪙', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'plinko', name: 'Plinko', nameAm: 'ፕሊንኮ', icon: '📉', cat: 'crash', minBet: 1, maxBet: 10000 },
    { id: 'blackjack', name: 'Blackjack', nameAm: 'ብላክጃክ', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'roulette', name: 'Roulette', nameAm: 'ሩሌት', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'mines', name: 'Mines', nameAm: 'ማይንስ', icon: '💣', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'crash', name: 'Crash', nameAm: 'ክራሽ', icon: '📈', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'tower', name: 'Tower', nameAm: 'ግንብ', icon: '🏗️', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'keno', name: 'Keno', nameAm: 'ኬኖ', icon: '🔢', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'baccarat', name: 'Baccarat', nameAm: 'ባካራት', icon: '♣️', cat: 'table', minBet: 5, maxBet: 10000 },
    { id: 'wheel', name: 'Wheel of Fortune', nameAm: 'የዕድል መንኮራኩር', icon: '🎰', cat: 'table', minBet: 1, maxBet: 5000 },
    { id: 'hilo', name: 'Hilo', nameAm: 'ሂሎ', icon: '⬆️⬇️', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'sicbo', name: 'Sic Bo', nameAm: 'ሲክቦ', icon: '🎲🎲🎲', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'videopoker', name: 'Video Poker', nameAm: 'ቪዲዮ ፖከር', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'bingo', name: 'Bingo', nameAm: 'ቢንጎ', icon: '🎯', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'craps', name: 'Craps', nameAm: 'ክራፕስ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'dragontiger', name: 'Dragon Tiger', nameAm: 'ድራጎን ታይገር', icon: '🐉🐯', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'andarbahar', name: 'Andar Bahar', nameAm: 'አንዳር ባሃር', icon: '🃏', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'teenpatti', name: 'Teen Patti', nameAm: 'ቲን ፓቲ', icon: '♠️', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'lucky7', name: 'Lucky 7', nameAm: 'ላኪ 7', icon: '🍀7️⃣', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'scratch', name: 'Scratch Card', nameAm: 'ስክራች ካርድ', icon: '🎫', cat: 'slots', minBet: 1, maxBet: 10000 },
    { id: 'football', name: 'Football Prediction', nameAm: 'እግር ኳስ ትንበያ', icon: '⚽', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'basketball', name: 'Basketball Prediction', nameAm: 'ቅርጫት ኳስ ትንበያ', icon: '🏀', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'horseracing', name: 'Horse Racing', nameAm: 'ፈረስ እሽቅድምድም', icon: '🐎', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'spinwin', name: 'Spin & Win', nameAm: 'ደብል አሸንፍ', icon: '🌀', cat: 'special', minBet: 1, maxBet: 5000 },
    { id: 'slot', name: 'Slot Machine', nameAm: 'ስሎት ማሽን', icon: '🎰', cat: 'slots', minBet: 1, maxBet: 10000 },
    { id: 'reddog', name: 'Red Dog', nameAm: 'ቀይ ውሻ', icon: '🐕', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'war', name: 'War', nameAm: 'ጦርነት', icon: '⚔️', cat: 'table', minBet: 1, maxBet: 5000 },
    { id: 'paigow', name: 'Pai Gow Poker', nameAm: 'ፓይ ጋው ፖከር', icon: '🀄️', cat: 'table', minBet: 5, maxBet: 10000 },
    { id: 'diceduels', name: 'Dice Duels', nameAm: 'ዳይስ ዱኤልስ', icon: '⚔️🎲', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'penalty', name: 'Penalty', nameAm: 'ፍፃጎት ምት', icon: '⚽', cat: 'sports', minBet: 1, maxBet: 5000 },
    { id: 'chickenroad', name: 'Chicken Road', nameAm: 'ዶሮ መንገድ', icon: '🐔', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'chickenshot', name: 'Chicken Shot', nameAm: 'ዶሮ ምት', icon: '🔫🐔', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'megaball', name: 'Mega Ball', nameAm: 'ሜጋ ቦል', icon: '⚾', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'pokerdice', name: 'Poker Dice', nameAm: 'ፖከር ዳይስ', icon: '🎲', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'lightningdice', name: 'Lightning Dice', nameAm: 'መብረቅ ዳይስ', icon: '⚡🎲', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'carroulette', name: 'Car Roulette', nameAm: 'መኪና ሩሌት', icon: '🚗', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'knockout', name: 'Knock Out', nameAm: 'ናክ አውት', icon: '🥊', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'rummy', name: 'Rummy', nameAm: 'ራሚ', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'darts', name: 'Darts', nameAm: 'ዳርትስ', icon: '🎯', cat: 'special', minBet: 1, maxBet: 5000 },
    { id: 'tennis', name: 'Tennis', nameAm: 'ቴኒስ', icon: '🎾', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'baseball', name: 'Baseball', nameAm: 'ቤዝቦል', icon: '⚾', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'greyhound', name: 'Greyhound Racing', nameAm: 'ግሬይሀውንድ እሽቅድምድም', icon: '🐕‍🦺', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'motorbike', name: 'Motorbike Racing', nameAm: 'ሞተር እሽቅድምድም', icon: '🏍️', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'cricket', name: 'Cricket', nameAm: 'ክሪኬት', icon: '🏏', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'roulette360', name: 'Roulette 360', nameAm: 'ሩሌት 360', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'megawheel', name: 'Mega Wheel', nameAm: 'ሜጋ መንኮራኩር', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'monopoly', name: 'Monopoly', nameAm: 'ሞኖፖሊ', icon: '🎩', cat: 'table', minBet: 1, maxBet: 5000 },
    { id: 'virtualsports', name: 'Virtual Sports', nameAm: 'ቨርቹዋል ስፖርት', icon: '🎮', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'texasholdem', name: 'Texas Hold\'em', nameAm: 'ቴክሳስ ሆልደም', icon: '♠️', cat: 'classic', minBet: 5, maxBet: 10000 }
  ];

  // CASINO GAME LOGIC (Client-side fallback)
  const gameLogic = {
    dice: (bet, params) => {
      const p = Math.floor(Math.random()*6)+1, h = Math.floor(Math.random()*6)+1, w = p > h;
      return { result: w ? 'win':'lose', profit: w ? bet*2 : -bet, details: { playerRoll: p, houseRoll: h } };
    },
    coinflip: (bet, params) => {
      const r = Math.random()<0.5?'heads':'tails', w = params.side === r;
      return { result: w ? 'win':'lose', profit: w ? bet*1.9 : -bet, details: { result: r, side: params.side } };
    },
    roulette: (bet, params) => {
      const n = Math.floor(Math.random()*37), r = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];
      const isR = r.includes(n), isE = n>0&&n%2===0; let w=false,m=0;
      if(params.bet==='red'&&isR){w=true;m=1.9;}else if(params.bet==='black'&&!isR&&n!==0){w=true;m=1.9;}
      else if(params.bet==='even'&&isE){w=true;m=1.9;}else if(params.bet==='odd'&&!isE&&n!==0){w=true;m=1.9;}
      const p = w ? bet*m : -bet; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details: {number:n,isRed:isR,isEven:isE}};
    },
    slot: (bet, params) => {
      const sym = ['🍒','🍋','🍊','🔔','💎','7️⃣']; const reels=[sym[Math.floor(Math.random()*6)],sym[Math.floor(Math.random()*6)],sym[Math.floor(Math.random()*6)]];
      let w=false,m=0; if(reels[0]===reels[1]&&reels[1]===reels[2]){w=true;m=5;}else if(reels[0]===reels[1]||reels[1]===reels[2]||reels[0]===reels[2]){w=true;m=0.5;}
      const p = w ? bet*m : -bet; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details: { reels, multiplier: m } };
    },
    blackjack: (bet, params) => {
      const g = () => { const v=Math.floor(Math.random()*13)+1; return {value:Math.min(v,10),display:v}; };
      const pC=[g(),g()], dC=[g(),g()]; const s=(c)=>{let t=c.reduce((s,c)=>s+c.value,0), a=c.filter(c=>c.display===1).length, adj=t,aU=0; while(adj<=11&&aU<a){adj+=10;aU++;}return adj;};
      const pS=s(pC), dS=s(dC); let res='lose', p=-bet; if(pS===21&&pC.length===2){res='win';p=bet*2.5;}else if(pS>21){res='lose';p=-bet;}else if(dS>21){res='win';p=bet;}else if(pS>dS){res='win';p=bet;}else if(pS===dS){res='push';p=0;}
      return { result: res, profit: Math.round(p*100)/100, details: {pCards:pC.map(c=>c.display),dCards:dC.map(c=>c.display),pScore:pS,dScore:dS}};
    },
    aviator: (bet, params) => {
      const cp=1+Math.random()*9, co=params.action==='cashout'?Math.min(1+Math.random()*5,cp):0, w=params.action==='cashout'&&co<cp;
      const ml=w?co:0, p=w?bet*ml:-bet; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details: {crashPoint:cp, multiplier:ml}};
    },
    mines: (bet, params) => {
      const gs=25,mc=params.mines||3,m=[]; while(m.length<mc){const p=Math.floor(Math.random()*gs);if(!m.includes(p))m.push(p);}
      const t=params.tile||Math.floor(Math.random()*gs), h=m.includes(t), p=h?-bet:bet*1.2; return { result: h?'lose':'win', profit: Math.round(p*100)/100, details:{mines:m,tile:t,hit:h}};
    },
    crash: (bet, params) => {
      const cp=1+Math.random()*9, co=params.action==='cashout'?Math.min(1+Math.random()*5,cp):0, w=params.action==='cashout'&&co<cp;
      const ml=w?co:0, p=w?bet*ml:-bet; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details:{crashPoint:cp, multiplier:ml}};
    },
    default: (bet) => {
      const w = Math.random()<0.45, p=w?bet*1.9:-bet; return { result: w?'win':'lose', profit: Math.round(p*100)/100, details: {} };
    }
  };

  // CASINO PLAY HANDLER
  const playCasinoGame = (gameId, params = {}) => {
    const game = GAMES.find(g => g.id === gameId);
    if (!game) return;
    if (casinoBetAmount < game.minBet) { alert(`Minimum bet is ${game.minBet} ETB`); return; }
    if (casinoBetAmount > game.maxBet) { alert(`Maximum bet is ${game.maxBet} ETB`); return; }
    if (casinoBetAmount > casinoBalance) { alert(`Insufficient balance.`); return; }

    setCasinoLoading(true);
    try {
      const logic = gameLogic[game.id] || gameLogic.default;
      const result = logic(casinoBetAmount, params);
      
      // Update Balance
      const newBalance = casinoBalance + result.profit;
      setCasinoBalance(newBalance);

      // Set Result Modal
      setCasinoResultData(result);
      setCasinoShowResultModal(true);

      // Add to history
      setCasinoHistory(prev => [{ gameId, bet: casinoBetAmount, result: result.result, profit: result.profit, details: result.details, timestamp: new Date() }, ...prev].slice(0, 50));

      // Add to Admin Broadcast
      setCasinoBroadcasts(prev => [{
        id: `CAS-${Date.now()}`,
        user: 'SuperAdmin',
        game: game.name,
        stake: casinoBetAmount,
        profit: result.profit,
        outcome: result.result,
        time: currentTime.toLocaleTimeString()
      }, ...prev].slice(0, 20));

    } finally {
      setCasinoLoading(false);
    }
  };

  const toggleCasinoFavorite = (gameId) => {
    setCasinoFavorites(prev => {
      const newFav = prev.includes(gameId) ? prev.filter(id => id !== gameId) : [...prev, gameId];
      localStorage.setItem('shebaodds_admin_favorite_games', JSON.stringify(newFav));
      return newFav;
    });
  };

  const renderCasinoGameCard = (game) => (
    <div key={game.id} className={`game-card ${selectedGame?.id === game.id ? 'active' : ''}`} onClick={() => { setSelectedGame(game); setCasinoIsBetPanelOpen(true); }}>
      {['slot','megaball','lucky7'].includes(game.id) && <span className="badge hot">HOT</span>}
      <button className={`favorite-btn ${casinoFavorites.includes(game.id) ? 'active' : ''}`} onClick={(e) => { e.stopPropagation(); toggleCasinoFavorite(game.id); }}>
        {casinoFavorites.includes(game.id) ? '★' : '☆'}
      </button>
      <span className="game-icon">{game.icon}</span>
      <span className="game-name">{game.name}</span>
      <span className="game-min-bet">{game.minBet} ETB</span>
    </div>
  );

  const renderCasinoGamesGrid = () => {
    const categories = ['crash','classic','table','slots','sports','special'];
    const categoryLabels = { crash: '💥 Crash', classic: '🃏 Classic', table: '🪑 Table', slots: '🎰 Slots', sports: '🏅 Sports', special: '✨ Special' };
    const favGames = GAMES.filter(g => casinoFavorites.includes(g.id));
    const otherGames = GAMES.filter(g => !casinoFavorites.includes(g.id));

    return (
      <>
        {favGames.length > 0 && (
          <div className="game-category">
            <h3 className="category-title">⭐ Favorites</h3>
            <div className="game-grid">{favGames.map(renderCasinoGameCard)}</div>
          </div>
        )}
        {categories.map(cat => {
          const gamesInCat = otherGames.filter(g => g.cat === cat);
          if (gamesInCat.length === 0) return null;
          return (
            <div key={cat} className="game-category">
              <h3 className="category-title">{categoryLabels[cat]} <small>{gamesInCat.length} games</small></h3>
              <div className="game-grid">{gamesInCat.map(renderCasinoGameCard)}</div>
            </div>
          );
        })}
      </>
    );
  };

  const renderCasinoGameSpecificUI = (gameId) => {
    switch (gameId) {
      case 'dice':
        return (
          <div className="dice-game flex flex-col items-center gap-4 py-4">
            <div className="dice-display text-6xl flex gap-8 items-center">
              <span>🎲</span><span className="text-slate-500 text-2xl font-bold">VS</span><span>🎲</span>
            </div>
            <button className="btn-play" onClick={() => playCasinoGame('dice')} disabled={casinoLoading}>
              {casinoLoading ? '🎲 Rolling...' : '🎲 Roll Dice'}
            </button>
          </div>
        );
      case 'coinflip':
        return (
          <div className="coinflip-game flex flex-col items-center gap-4 py-4">
            <div className="coin-display text-8xl">🪙</div>
            <div className="game-controls flex gap-4">
              <button className="btn-bet" onClick={() => playCasinoGame('coinflip', { side: 'heads' })} disabled={casinoLoading}>Heads</button>
              <button className="btn-bet" onClick={() => playCasinoGame('coinflip', { side: 'tails' })} disabled={casinoLoading}>Tails</button>
            </div>
          </div>
        );
      case 'slot':
        return (
          <div className="slot-game flex flex-col items-center gap-4 py-4">
            <div className="slot-reels text-6xl flex gap-4"><span>🍒</span><span>🍒</span><span>🍒</span></div>
            <button className="btn-play spin" onClick={() => playCasinoGame('slot')} disabled={casinoLoading}>
              {casinoLoading ? '🔄 Spinning...' : '🎰 Spin'}
            </button>
          </div>
        );
      case 'aviator':
        return (
          <div className="aviator-game flex flex-col items-center gap-4 py-4">
            <div className="aviator-multiplier text-5xl font-black text-amber-400">1.00x</div>
            <div className="game-controls flex gap-4">
              <button className="btn-bet" onClick={() => playCasinoGame('aviator', { action: 'bet' })} disabled={casinoLoading}>
                {casinoLoading ? '⏳' : '✈️ Place Bet'}
              </button>
              <button className="btn-cashout" onClick={() => playCasinoGame('aviator', { action: 'cashout' })} disabled={casinoLoading}>
                💰 Cash Out
              </button>
            </div>
          </div>
        );
      default:
        return (
          <div className="default-game flex flex-col items-center gap-4 py-4">
            <div className="text-7xl">{GAMES.find(g=>g.id===gameId)?.icon || '🎮'}</div>
            <button className="btn-play" onClick={() => playCasinoGame(gameId)} disabled={casinoLoading}>
              {casinoLoading ? '⏳ Playing...' : '▶️ Play Now'}
            </button>
          </div>
        );
    }
  };

  // Financial aggregates
  const stats = useMemo(() => {
    const totalUsersCount = 12458 + users.length - 5;
    const totalBalanceAmount = 1257850 + users.reduce((acc, u) => acc + u.balance, 0) - 22101.45;
    const totalBetsTodayCount = 8564 + liveBets.length - 5;
    const totalDepositsAmount = 523600 + transactions.filter(t => t.type === 'Deposit' && t.status === 'Approved').reduce((acc, t) => acc + t.amount, 0) - 8000;
    const totalWithdrawalsAmount = 186250 + transactions.filter(t => t.type === 'Withdrawal' && t.status === 'Approved').reduce((acc, t) => acc + t.amount, 0);
    const profitToday = 125750 + liveBets.reduce((acc, b) => acc + b.stake, 0) * 0.15;
    const totalProfitAmount = 3245680 + profitToday - 125750;

    return { users: totalUsersCount, balance: totalBalanceAmount, betsToday: totalBetsTodayCount, deposits: totalDepositsAmount, withdrawals: totalWithdrawalsAmount, profitToday: profitToday, totalProfit: totalProfitAmount };
  }, [users, transactions, liveBets]);

  // Modals controller states
  const [modals, setModals] = useState({ user: false, match: false, deposit: false, withdrawal: false, settings: false, betSlip: false });
  const [newUserForm, setNewUserForm] = useState({ id: '', email: '', role: 'Player', balance: 100 });
  const [newMatchForm, setNewMatchForm] = useState({ home: '', away: '', sport: 'Football', date: 'Today, 22:00', odds1: 1.9, oddsX: 3.2, odds2: 3.1 });
  const [newDepositForm, setNewDepositForm] = useState({ user: 'User1234', amount: 500, method: 'TeleBirr' });
  const [newWithdrawForm, setNewWithdrawForm] = useState({ user: 'User1234', amount: 500, method: 'TeleBirr' });

  // Web socket simulator & real listener
  useEffect(() => {
    let ws = null;
    function connect() {
      ws = new WebSocket(wsUrl);
      ws.onmessage = (event) => {
        try {
          const rawPayload = JSON.parse(event.data);
          if (rawPayload && rawPayload.eventId) {
            setMatches((prevMatches) => prevMatches.map((match) => {
              if (match.id === rawPayload.eventId) {
                const updatedOdds = {};
                const nextOddsUp = { ...match.oddsUp };
                const nextOddsDown = { ...match.oddsDown };
                const activeMarket = rawPayload.markets?.find(m => m.marketId === '1X2');
                if (activeMarket && activeMarket.odds) {
                  activeMarket.odds.forEach((item) => {
                    const outcome = item.outcome;
                    const newPrice = Number(item.price);
                    const oldPrice = match.odds[outcome];
                    if (oldPrice && newPrice !== oldPrice) {
                      if (newPrice > oldPrice) { nextOddsUp[outcome] = true; nextOddsDown[outcome] = false; } else { nextOddsDown[outcome] = true; nextOddsUp[outcome] = false; }
                      setTimeout(() => { setMatches((current) => current.map((m) => { if (m.id === match.id) { const clearedUp = { ...m.oddsUp }; const clearedDown = { ...m.oddsDown }; delete clearedUp[outcome]; delete clearedDown[outcome]; return { ...m, oddsUp: clearedUp, oddsDown: clearedDown }; } return m; })); }, 1500);
                    }
                    updatedOdds[outcome] = newPrice;
                  });
                }
                return { ...match, score: rawPayload.score ? { home: rawPayload.score.home, away: rawPayload.score.away, elapsed: rawPayload.score.elapsed } : match.score, odds: { ...match.odds, ...updatedOdds }, oddsUp: nextOddsUp, oddsDown: nextOddsDown, status: rawPayload.status || match.status };
              }
              return match;
            }));
          }
        } catch (e) {}
      };
    }
    connect();

    const simInterval = setInterval(() => {
      setMatches((current) => current.map((match) => {
        if (match.status === 'Live' && Math.random() > 0.6) {
          const keys = ['1', 'X', '2']; const keyToChange = keys[Math.floor(Math.random() * keys.length)];
          const oldVal = match.odds[keyToChange]; const change = (Math.random() * 0.3 - 0.15); const newVal = Math.max(1.1, parseFloat((oldVal + change).toFixed(2)));
          const nextOddsUp = { ...match.oddsUp }; const nextOddsDown = { ...match.oddsDown };
          if (newVal > oldVal) nextOddsUp[keyToChange] = true; else nextOddsDown[keyToChange] = true;
          setTimeout(() => { setMatches((mList) => mList.map((m) => { if (m.id === match.id) { const u = { ...m.oddsUp }; const d = { ...m.oddsDown }; delete u[keyToChange]; delete d[keyToChange]; return { ...m, oddsUp: u, oddsDown: d }; } return m; })); }, 1500);
          let nextElapsed = match.score.elapsed; if (nextElapsed.includes("'")) { const currentMin = parseInt(nextElapsed.replace("'", '')); nextElapsed = currentMin < 90 ? `${currentMin + 1}'` : "FT"; }
          return { ...match, score: { ...match.score, elapsed: nextElapsed, home: Math.random() > 0.95 ? match.score.home + 1 : match.score.home, away: Math.random() > 0.97 ? match.score.away + 1 : match.score.away }, odds: { ...match.odds, [keyToChange]: newVal }, oddsUp: nextOddsUp, oddsDown: nextOddsDown };
        }
        return match;
      }));
    }, 4000);

    return () => { if (ws) ws.close(); clearInterval(simInterval); };
  }, [wsUrl]);

  // Clock running effect
  useEffect(() => { const timer = setInterval(() => setCurrentTime(new Date()), 1000); return () => clearInterval(timer); }, []);

  // Filter datasets based on Search
  const filteredMatches = useMemo(() => matches.filter(m => m.homeTeam.toLowerCase().includes(searchQuery.toLowerCase()) || m.awayTeam.toLowerCase().includes(searchQuery.toLowerCase()) || m.sport.toLowerCase().includes(searchQuery.toLowerCase())), [matches, searchQuery]);
  const filteredTransactions = useMemo(() => transactions.filter(t => t.user.toLowerCase().includes(searchQuery.toLowerCase()) || t.method.toLowerCase().includes(searchQuery.toLowerCase()) || t.status.toLowerCase().includes(searchQuery.toLowerCase())), [transactions, searchQuery]);
  const filteredUsers = useMemo(() => users.filter(u => u.id.toLowerCase().includes(searchQuery.toLowerCase()) || u.email.toLowerCase().includes(searchQuery.toLowerCase()) || u.role.toLowerCase().includes(searchQuery.toLowerCase())), [users, searchQuery]);
  const filteredLiveBets = useMemo(() => liveBets.filter(b => b.user.toLowerCase().includes(searchQuery.toLowerCase()) || b.match.toLowerCase().includes(searchQuery.toLowerCase())), [liveBets, searchQuery]);

  // Action handlers (Existing)
  const handleApproveTransaction = (trxId) => { setTransactions(prev => prev.map(t => { if (t.id === trxId) { const targetUser = users.find(u => u.id === t.user); if (targetUser) { setUsers(ul => ul.map(u => u.id === t.user ? { ...u, balance: Math.max(0, u.balance + (t.type === 'Deposit' ? t.amount : -t.amount)) } : u)); } return { ...t, status: 'Approved' }; } return t; })); };
  const handleRejectTransaction = (trxId) => { setTransactions(prev => prev.map(t => t.id === trxId ? { ...t, status: 'Rejected' } : t)); };
  const handleDeleteMatch = (matchId) => { setMatches(prev => prev.filter(m => m.id !== matchId)); };
  const handleSettleBet = (betId, outcome) => { setLiveBets(prev => prev.filter(b => b.id !== betId)); const bet = liveBets.find(b => b.id === betId); if (bet && outcome === 'Won') { const trxId = `#TRX${Math.floor(1000 + Math.random() * 9000)}`; setTransactions(prev => [{ id: trxId, user: bet.user, type: 'Deposit', amount: bet.possibleWin, method: 'Winnings Settle', status: 'Approved', time: currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }, ...prev]); setUsers(ul => ul.map(u => u.id === bet.user ? { ...u, balance: u.balance + bet.possibleWin } : u)); } };
  const handleAddUser = (e) => { e.preventDefault(); if (!newUserForm.id || !newUserForm.email) return; setUsers(prev => [...prev, { id: newUserForm.id, email: newUserForm.email, role: newUserForm.role, balance: parseFloat(newUserForm.balance), joined: new Date().toISOString().split('T')[0] }]); setNewUserForm({ id: '', email: '', role: 'Player', balance: 100 }); setModals(m => ({ ...m, user: false })); };
  const handleAddMatch = (e) => { e.preventDefault(); if (!newMatchForm.home || !newMatchForm.away) return; const matchId = `sr:match:${100 + matches.length + 1}`; setMatches(prev => [...prev, { id: matchId, sport: newMatchForm.sport, homeTeam: newMatchForm.home, awayTeam: newMatchForm.away, status: 'Upcoming', startTime: newMatchForm.date, odds: { '1': parseFloat(newMatchForm.odds1), 'X': parseFloat(newMatchForm.oddsX), '2': parseFloat(newMatchForm.odds2) }, oddsUp: {}, oddsDown: {} }]); setNewMatchForm({ home: '', away: '', sport: 'Football', date: 'Today, 22:00', odds1: 1.9, oddsX: 3.2, odds2: 3.1 }); setModals(m => ({ ...m, match: false })); };
  const handleDepositRequest = (e) => { e.preventDefault(); const trxId = `#TRX${Math.floor(1000 + Math.random() * 9000)}`; setTransactions(prev => [{ id: trxId, user: newDepositForm.user, type: 'Deposit', amount: parseFloat(newDepositForm.amount), method: newDepositForm.method, status: 'Pending', time: currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }, ...prev]); setModals(m => ({ ...m, deposit: false })); };
  const handleWithdrawRequest = (e) => { e.preventDefault(); const trxId = `#TRX${Math.floor(1000 + Math.random() * 9000)}`; setTransactions(prev => [{ id: trxId, user: newWithdrawForm.user, type: 'Withdrawal', amount: parseFloat(newWithdrawForm.amount), method: newWithdrawForm.method, status: 'Pending', time: currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }, ...prev]); setModals(m => ({ ...m, withdrawal: false })); };

  return (
    <div className="min-h-screen bg-[#090D16] text-slate-100 font-sans antialiased flex">
      {/* --- SIDEBAR NAVIGATION --- */}
      <aside className={`fixed top-0 bottom-0 left-0 z-40 bg-[#111625]/95 border-r border-slate-800 w-64 transition-transform duration-300 transform ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0 flex flex-col justify-between`}>
        <div className="flex-1 overflow-y-auto py-5 px-4 scrollbar-thin">
          <div className="flex items-center gap-3 px-3 mb-8 cursor-pointer">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-[#EAB308] to-amber-600 flex items-center justify-center font-black text-slate-950 text-xl shadow-[0_0_15px_rgba(234,179,8,0.3)]">SO</div>
            <div><span className="text-lg font-black tracking-wider bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">SHEBA<span className="text-amber-400">ODDS</span></span><p className="text-[10px] uppercase font-bold text-slate-500 tracking-widest leading-none">Admin Panel</p></div>
          </div>
          <nav className="space-y-1.5">
            <button onClick={() => setActiveTab('dashboard')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'dashboard' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><LayoutDashboard className="h-4.5 w-4.5" /><span>Dashboard</span></button>
            <button onClick={() => setActiveTab('users')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'users' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Users className="h-4.5 w-4.5" /><span>Users</span></button>
            <button onClick={() => setActiveTab('matches')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'matches' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Trophy className="h-4.5 w-4.5" /><span>Matches & Odds</span></button>
            <button onClick={() => setActiveTab('bets')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'bets' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Receipt className="h-4.5 w-4.5" /><span>Bet Management</span></button>
            <button onClick={() => setActiveTab('casino')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'casino' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Gamepad2 className="h-4.5 w-4.5" /><span>🎰 Casino</span></button>
            <div><button onClick={() => setIsFinanceExpanded(!isFinanceExpanded)} className="w-full flex items-center justify-between px-4 py-3 rounded-xl text-sm font-semibold text-slate-400 hover:text-white hover:bg-slate-800/40 transition-all"><div className="flex items-center gap-3.5"><Landmark className="h-4.5 w-4.5" /><span>Finance</span></div><ChevronDown className={`h-3.5 w-3.5 transform transition-transform ${isFinanceExpanded ? 'rotate-180' : ''}`} /></button>
            {isFinanceExpanded && (<div className="pl-11 mt-1 space-y-1"><button onClick={() => setActiveTab('deposits')} className={`w-full text-left py-2 px-3 rounded-lg text-xs font-medium transition-all ${activeTab === 'deposits' ? 'text-amber-400 font-bold bg-amber-500/10' : 'text-slate-400 hover:text-white'}`}>Deposits</button><button onClick={() => setActiveTab('withdrawals')} className={`w-full text-left py-2 px-3 rounded-lg text-xs font-medium transition-all ${activeTab === 'withdrawals' ? 'text-amber-400 font-bold bg-amber-500/10' : 'text-slate-400 hover:text-white'}`}>Withdrawals</button><button onClick={() => setActiveTab('transactions')} className={`w-full text-left py-2 px-3 rounded-lg text-xs font-medium transition-all ${activeTab === 'transactions' ? 'text-amber-400 font-bold bg-amber-500/10' : 'text-slate-400 hover:text-white'}`}>All Transactions</button></div>)}
            <button onClick={() => setActiveTab('reports')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'reports' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><FileBarChart2 className="h-4.5 w-4.5" /><span>Reports</span></button>
            <button onClick={() => setActiveTab('bonuses')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'bonuses' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Gift className="h-4.5 w-4.5" /><span>Bonus Management</span></button>
            <button onClick={() => setActiveTab('settings')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'settings' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Settings className="h-4.5 w-4.5" /><span>System Settings</span></button>
            <button onClick={() => setActiveTab('admins')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'admins' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><ShieldAlert className="h-4.5 w-4.5" /><span>Admin Management</span></button>
            <button onClick={() => setActiveTab('logs')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'logs' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><FileText className="h-4.5 w-4.5" /><span>Logs & Activity</span></button>
            <button onClick={() => setActiveTab('support')} className={`w-full flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all ${activeTab === 'support' ? 'bg-gradient-to-r from-amber-500/20 to-transparent border-l-4 border-amber-400 text-amber-300 font-bold' : 'text-slate-400 hover:text-white hover:bg-slate-800/40'}`}><Mail className="h-4.5 w-4.5" /><span>Support Messages</span></button>
          </nav>
        </div>
        <div className="p-4 border-t border-slate-800 bg-[#0F1321] flex items-center justify-between">
          <div className="flex items-center gap-3"><div className="h-9 w-9 rounded-full bg-amber-500/20 border border-amber-400/30 flex items-center justify-center font-bold text-amber-400">SA</div><div><p className="text-xs font-bold text-white leading-none">Super Admin</p><span className="inline-flex items-center gap-1 text-[9px] text-emerald-400 font-medium mt-0.5"><span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />Online</span></div></div>
          <button onClick={() => alert('Administrative settings profile launched.')} className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all"><ChevronDown className="h-4 w-4" /></button>
        </div>
      </aside>

      {/* --- MAIN PAGE WRAPPER --- */}
      <div className="flex-1 lg:pl-64 flex flex-col min-h-screen">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-slate-800/80 bg-[#111625]/90 px-4 sm:px-6 lg:px-8 backdrop-blur-md">
          <div className="flex items-center gap-4 flex-1">
            <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="lg:hidden p-2 text-slate-400 hover:text-white transition-colors"><Activity className="h-5 w-5" /></button>
            <div className="relative w-full max-w-md hidden sm:block"><span className="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-500 pointer-events-none"><Search className="h-4 w-4" /></span><input type="text" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} placeholder="Search users, matches, transactions..." className="w-full bg-[#090D16] border border-slate-800 rounded-xl py-2 pl-9 pr-4 text-xs font-medium text-slate-300 placeholder-slate-500 focus:outline-none focus:border-amber-500 transition-colors" /></div>
          </div>
          <div className="flex items-center gap-4">
            <div className="hidden md:flex items-center gap-3.5 text-slate-400 text-xs font-bold bg-[#151C2E] px-3.5 py-2 rounded-xl border border-slate-800"><div className="flex items-center gap-1.5"><span className="w-1.5 h-1.5 rounded-full bg-amber-400" /><span>{currentTime.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })}</span></div><span className="text-slate-500">|</span><span className="font-mono text-amber-300 font-extrabold">{currentTime.toLocaleTimeString()}</span></div>
            <div className="relative"><button onClick={() => setIsNotificationsOpen(!isNotificationsOpen)} className="p-2 bg-[#151C2E] hover:bg-slate-800 text-slate-300 rounded-xl border border-slate-800 transition-all relative"><Bell className="h-4.5 w-4.5" /><span className="absolute -top-1 -right-1 h-4 w-4 bg-red-600 text-[9px] font-black text-white rounded-full flex items-center justify-center animate-bounce">8</span></button>
            {isNotificationsOpen && (<div className="absolute right-0 mt-2.5 w-80 bg-[#111625] border border-slate-800 rounded-xl shadow-2xl p-4 space-y-3 z-50"><div className="flex justify-between items-center border-b border-slate-800 pb-2"><span className="text-xs font-extrabold text-white">System Notifications</span><button onClick={() => setIsNotificationsOpen(false)} className="text-xs text-amber-400 hover:underline">Mark all read</button></div><div className="space-y-2.5 text-xs"><div className="p-2 bg-[#1C1F2E] rounded-lg border border-slate-800/50"><p className="font-semibold text-amber-300">New Deposit Request</p><p className="text-[10px] text-slate-400 mt-0.5">User1234 initiated 5,000.00 ETB deposit via TeleBirr.</p></div><div className="p-2 bg-[#1C1F2E] rounded-lg border border-slate-800/50"><p className="font-semibold text-rose-400">Large Withdrawal Alert</p><p className="text-[10px] text-slate-400 mt-0.5">User5678 requested a 12,500.00 ETB payout.</p></div></div></div>)}
            </div>
            <div className="relative"><button onClick={() => setIsProfileOpen(!isProfileOpen)} className="flex items-center gap-2.5 bg-[#151C2E] border border-slate-800 p-1.5 pr-3.5 rounded-xl text-left hover:border-slate-700 transition-colors"><div className="h-7.5 w-7.5 rounded-lg bg-gradient-to-tr from-amber-500 to-amber-600 flex items-center justify-center font-black text-slate-950 text-xs">SA</div><div className="hidden sm:block"><p className="text-[11px] font-bold text-white leading-none">Super Admin</p><p className="text-[9px] text-slate-500 mt-0.5">Administrator</p></div><ChevronDown className="h-3.5 w-3.5 text-slate-500" /></button>
            {isProfileOpen && (<div className="absolute right-0 mt-2.5 w-48 bg-[#111625] border border-slate-800 rounded-xl shadow-2xl p-2.5 z-50 text-xs"><a href="#profile" className="block px-3 py-2 text-slate-300 hover:bg-slate-800 rounded-lg hover:text-white transition-colors">My Profile</a><a href="#logs" className="block px-3 py-2 text-slate-300 hover:bg-slate-800 rounded-lg hover:text-white transition-colors">Activity Logs</a><hr className="border-slate-800 my-1.5" /><button onClick={() => alert('Log out successfully.')} className="w-full text-left px-3 py-2 text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors font-semibold">Sign Out</button></div>)}
            </div>
          </div>
        </header>

        <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6">
          <div className="relative w-full sm:hidden"><span className="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-500 pointer-events-none"><Search className="h-4 w-4" /></span><input type="text" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} placeholder="Search user, match, odds..." className="w-full bg-[#111625] border border-slate-800 rounded-xl py-2 pl-9 pr-4 text-xs font-medium text-slate-300 placeholder-slate-500 focus:outline-none focus:border-amber-500 transition-colors" /></div>

          {/* --- DASHBOARD VIEW --- */}
          {activeTab === 'dashboard' && ( /* ... dashboard layout ... */ )}

          {/* --- VIEW: USERS --- */}
          {activeTab === 'users' && ( /* ... users table ... */ )}

          {/* --- VIEW: MATCHES --- */}
          {activeTab === 'matches' && ( /* ... matches grid ... */ )}

          {/* --- VIEW: DEPOSITS & WITHDRAWALS --- */}
          {(activeTab === 'deposits' || activeTab === 'withdrawals' || activeTab === 'transactions') && ( /* ... transactions table ... */ )}

          {/* --- VIEW: REPORTS --- */}
          {activeTab === 'reports' && ( /* ... reports block ... */ )}

          {/* --- VIEW: BONUSES --- */}
          {activeTab === 'bonuses' && ( /* ... bonuses block ... */ )}

          {/* --- VIEW: SETTINGS --- */}
          {activeTab === 'settings' && ( /* ... settings block ... */ )}

          {/* --- VIEW: ADMINS --- */}
          {activeTab === 'admins' && ( /* ... admins block ... */ )}

          {/* --- VIEW: LOGS --- */}
          {activeTab === 'logs' && ( /* ... logs block ... */ )}

          {/* --- VIEW: SUPPORT --- */}
          {activeTab === 'support' && ( /* ... support block ... */ )}

          {/* ============================================================== */}
          {/* 🎰 NEW: 51+ CASINO GAMES VIEW */}
          {/* ============================================================== */}
          {activeTab === 'casino' && (
            <div className="casino-games-page p-4 bg-[#0b0e1a] rounded-xl text-white">
              <div className="casino-header flex justify-between items-center p-4 bg-[#1a1f33] rounded-xl mb-6 border-b-2 border-amber-400">
                <div className="header-left flex items-center gap-3">
                  <h1 className="text-2xl font-bold text-amber-400">🎰 Casino Games</h1>
                  <span className="text-sm text-slate-400 bg-[#1e2338] px-3 py-1 rounded-full">{GAMES.length} Games</span>
                </div>
                <div className="balance-box flex items-center gap-4 bg-[#1e2338] px-5 py-2 rounded-full border border-amber-400/30">
                  <span className="text-sm text-slate-400">💰 Balance</span>
                  <span className="text-xl font-bold text-amber-400">{casinoBalance.toLocaleString()} ETB</span>
                  <button className="text-slate-400 hover:text-amber-400 transition-colors" onClick={() => setCasinoBalance(25000)}><RefreshCcw className="h-4 w-4" /></button>
                </div>
              </div>

              {/* Casino Game Grid */}
              <div className="games-container">{renderCasinoGamesGrid()}</div>

              {/* Casino Game View (If game selected and panel open) */}
              {selectedGame && casinoIsBetPanelOpen && (
                <div className="game-view bg-[#0f1322] border border-amber-400/40 rounded-2xl p-6 mt-6">
                  <div className="game-view-header flex justify-between items-center mb-4">
                    <h2 className="game-title text-2xl font-bold text-amber-400">{selectedGame.icon} {selectedGame.name}</h2>
                    <button onClick={() => { setSelectedGame(null); setCasinoIsBetPanelOpen(false); }} className="text-slate-400 hover:text-white text-xl">✕</button>
                  </div>
                  <div className="game-area bg-[#1a1f33] rounded-xl p-6 min-h-[200px] flex flex-col items-center justify-center">
                    {renderCasinoGameSpecificUI(selectedGame.id)}
                  </div>
                  <div className="game-tutorial mt-4 text-center">
                    <button className="tutorial-btn bg-transparent border border-slate-700 text-slate-400 px-4 py-1.5 rounded-full hover:border-amber-400 hover:text-amber-400 transition-colors" onClick={() => alert(`How to play ${selectedGame.name}: Place your bet and try your luck!`)}>
                      ❓ How to play
                    </button>
                  </div>
                </div>
              )}

              {/* Casino History Table */}
              <div className="game-history bg-[#1a1f33] rounded-xl p-4 mt-6 border border-slate-800">
                <h4 className="text-sm font-bold text-amber-400 mb-3">📜 Recent Casino Rounds (Admin Feed)</h4>
                <div className="history-list space-y-2 max-h-[200px] overflow-y-auto">
                  {casinoHistory.slice(0, 10).map((g, i) => (
                    <div key={i} className={`history-item flex justify-between p-2 rounded bg-[#0b0e1a] ${g.result === 'win' ? 'border-l-4 border-emerald-500' : 'border-l-4 border-rose-500'}`}>
                      <span className="history-game text-sm text-slate-300">{g.gameId}</span>
                      <span className="history-bet text-sm text-slate-400">{g.bet} ETB</span>
                      <span className="history-result text-sm">{g.result === 'win' ? '✅' : '❌'}</span>
                      <span className={`history-profit text-sm font-bold ${g.profit >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                        {g.profit >= 0 ? '+' : ''}{g.profit} ETB
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* --- CASINO BET PANEL MODAL --- */}
      {selectedGame && casinoIsBetPanelOpen && (
        <div className="fixed bottom-0 left-0 right-0 z-50 bg-[#0b0e1a]/95 backdrop-blur-md border-t border-amber-400/50 p-4 flex flex-wrap items-center justify-center gap-3">
          <div className="game-info text-center min-w-[100px]">
            <span className="text-sm font-bold text-amber-400">{selectedGame.icon} {selectedGame.name}</span>
            <small className="block text-xs text-slate-400">Min: {selectedGame.minBet} ETB</small>
          </div>
          <div className="quick-bets flex gap-1">
            {['10%', '25%', '50%', '100%'].map((label, idx) => {
              const val = Math.round(casinoBalance * (parseInt(label) / 100));
              return <button key={label} className="quick-bet-btn bg-[#1e2338] border border-slate-700 text-slate-300 px-2 py-1 rounded text-xs hover:bg-[#2a3150]" onClick={() => setCasinoBetAmount(Math.max(val, 1))} disabled={val < 1}>{label}</button>;
            })}
          </div>
          <div className="bet-amounts flex flex-wrap gap-1">
            {[1, 2, 5, 10, 20, 50, 100, 500, 1000].map(amt => (
              <button key={amt} className={`amt-btn bg-[#1e2338] border border-slate-700 text-slate-300 px-2 py-1 rounded text-xs font-semibold ${casinoBetAmount === amt ? 'bg-amber-400 text-black border-amber-400' : ''}`} onClick={() => setCasinoBetAmount(amt)}>{amt}</button>
            ))}
            <button className="amt-btn max border border-rose-500 text-rose-500 px-2 py-1 rounded text-xs font-semibold" onClick={() => setCasinoBetAmount(Math.min(casinoBalance, selectedGame?.maxBet || 10000))}>MAX</button>
          </div>
          <div className="manual-input flex items-center bg-[#1e2338] rounded-lg px-3 py-1 border border-slate-700">
            <input type="number" value={casinoBetAmount} onChange={(e) => setCasinoBetAmount(Math.max(1, Number(e.target.value)))} className="bg-transparent border-none text-white w-20 text-sm font-semibold outline-none" min="1" />
            <span className="text-xs text-slate-400">ETB</span>
          </div>
          <div className="bet-actions flex gap-2 items-center">
            <button className="play-btn bg-amber-400 text-black font-bold px-6 py-2 rounded-full hover:bg-amber-300 transition-colors" onClick={() => playCasinoGame(selectedGame.id)} disabled={casinoLoading || casinoBetAmount > casinoBalance}>
              {casinoLoading ? '⏳' : '▶️ Play'}
            </button>
            <button className="close-btn text-slate-400 hover:text-white text-xl px-2" onClick={() => { setSelectedGame(null); setCasinoIsBetPanelOpen(false); }}>✕</button>
          </div>
        </div>
      )}

      {/* --- CASINO RESULT MODAL --- */}
      {casinoShowResultModal && casinoResultData && (
        <div className="fixed inset-0 z-[60] bg-black/85 backdrop-blur-sm flex items-center justify-center p-4" onClick={() => setCasinoShowResultModal(false)}>
          <div className="bg-[#151b2b] rounded-2xl max-w-sm w-full p-6 text-center border border-amber-400/30 shadow-2xl" onClick={(e) => e.stopPropagation()}>
            <div className="text-5xl mb-2">{casinoResultData.result === 'win' ? '🎉' : '😔'}</div>
            <h2 className="text-xl font-bold text-slate-200 mb-2">{selectedGame?.name}</h2>
            <div className={`text-4xl font-black mb-4 ${casinoResultData.result === 'win' ? 'text-emerald-400' : 'text-rose-500'}`}>
              {casinoResultData.result === 'win' ? '✅ WIN' : '❌ LOSE'}
            </div>
            <div className="space-y-2 text-sm mb-4">
              <div className="flex justify-between border-b border-slate-800 pb-2"><span>Bet</span><span>{casinoBetAmount} ETB</span></div>
              <div className="flex justify-between border-b border-slate-800 pb-2"><span>Profit</span><span className={casinoResultData.profit >= 0 ? 'text-emerald-400' : 'text-rose-500'}>{casinoResultData.profit} ETB</span></div>
              <div className="flex justify-between"><span>New Balance</span><span className="text-amber-400">{casinoBalance} ETB</span></div>
            </div>
            <button className="w-full bg-amber-400 text-black font-bold py-3 rounded-full hover:bg-amber-300 transition-colors" onClick={() => setCasinoShowResultModal(false)}>Continue</button>
          </div>
        </div>
      )}

      {/* --- STYLING FOR CASINO --- */}
      <style jsx>{`
        .casino-games-page h1, .casino-games-page h2, .casino-games-page h3 { color: #f0b90b; }
        .game-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 12px; }
        .game-card { background: #151b2b; border-radius: 16px; padding: 16px 8px 12px; text-align: center; cursor: pointer; transition: 0.25s; border: 2px solid transparent; position: relative; }
        .game-card:hover { transform: translateY(-4px); border-color: #f0b90b66; background: #1c2338; }
        .game-card.active { border-color: #f0b90b; background: #1c2338; }
        .game-card .game-icon { font-size: 32px; display: block; margin-bottom: 6px; }
        .game-card .game-name { font-size: 11px; font-weight: 600; color: #ccd6f6; }
        .game-card .game-min-bet { font-size: 9px; color: #8892b0; display: block; margin-top: 4px; }
        .badge { position: absolute; top: 6px; right: 6px; font-size: 8px; padding: 2px 8px; border-radius: 10px; }
        .badge.live { background: #ff4757; color: #fff; animation: pulse 1.5s infinite; }
        .badge.hot { background: #f0b90b; color: #0b0e1a; }
        .favorite-btn { position: absolute; top: 6px; left: 6px; background: transparent; border: none; color: #8892b0; cursor: pointer; font-size: 14px; padding: 2px; transition: 0.2s; z-index: 2; }
        .favorite-btn.active { color: #f0b90b; }
        .favorite-btn:hover { transform: scale(1.2); }
        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
      `}</style>

    </div>
  );
}