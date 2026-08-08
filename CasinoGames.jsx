// CasinoGames.jsx – 51+ games with live play, favorites, history
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from './AuthContext.jsx';
import { useTranslation } from './LanguageContext';
import axios from 'axios';

export default function CasinoGames() {
  const { user } = useAuth?.() || {};
  const { language } = useTranslation?.() || { language: 'en' };
  const navigate = window.location?.href ? (path) => window.location.href = path : () => {};

  const [selectedGame, setSelectedGame] = useState(null);
  const [balance, setBalance] = useState(0);
  const [betAmount, setBetAmount] = useState(10);
  const [isBetPanelOpen, setIsBetPanelOpen] = useState(false);
  const [gameState, setGameState] = useState({});
  const [showResultModal, setShowResultModal] = useState(false);
  const [resultData, setResultData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [gameHistory, setGameHistory] = useState([]);
  const [favorites, setFavorites] = useState(() => {
    const saved = localStorage.getItem('shebaodds_favorite_games');
    return saved ? JSON.parse(saved) : [];
  });
  const [liveGameData, setLiveGameData] = useState({});
  const canvasRef = useRef(null);

  // 51+ games definition
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

  // Sound effects (optional – you can create /public/sounds/win.mp3 etc.)
  const sounds = {
    win: new Audio('/sounds/win.mp3'),
    lose: new Audio('/sounds/lose.mp3'),
    spin: new Audio('/sounds/spin.mp3'),
    coinFlip: new Audio('/sounds/coin.mp3'),
    diceRoll: new Audio('/sounds/dice.mp3'),
    slotSpin: new Audio('/sounds/slot.mp3')
  };
  const playSound = (type) => {
    if (sounds[type]) {
      sounds[type].currentTime = 0;
      sounds[type].play().catch(() => {});
    }
  };

  // Fetch balance
  const fetchBalance = useCallback(async () => {
    try {
      const res = await axios.get('/api/wallet/balance');
      setBalance(res.data.balance || 0);
    } catch (error) {
      console.error('Error fetching balance:', error);
    }
  }, []);

  useEffect(() => {
    if (user) fetchBalance();
  }, [user, fetchBalance]);

  // Game logic (fallback client-side)
  const gameLogic = {
    dice: (bet, params) => {
      const playerRoll = Math.floor(Math.random() * 6) + 1;
      const houseRoll = Math.floor(Math.random() * 6) + 1;
      const win = playerRoll > houseRoll;
      const profit = win ? bet * 2 : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { playerRoll, houseRoll } };
    },
    coinflip: (bet, params) => {
      const result = Math.random() < 0.5 ? 'heads' : 'tails';
      const win = params.side === result;
      const profit = win ? bet * 1.9 : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { result, side: params.side } };
    },
    plinko: (bet, params) => {
      const rows = params.rows || 12;
      const multipliers = { 8: [5.6,2.1,1.1,1.0,1.0,1.1,2.1,5.6], 12: [10,5,3,1.5,0.5,0.5,0.5,0.5,1.5,3,5,10], 16: [29,10,5,2,1,0.5,0.3,0.3,0.3,0.3,0.5,1,2,5,10,29] };
      const multiplier = multipliers[rows][Math.floor(Math.random() * rows)] || 1;
      const profit = bet * multiplier - bet;
      return { result: profit > 0 ? 'win' : 'lose', profit, details: { multiplier, rows } };
    },
    roulette: (bet, params) => {
      const number = Math.floor(Math.random() * 37);
      const reds = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];
      const isRed = reds.includes(number);
      const isEven = number > 0 && number % 2 === 0;
      let win = false, multiplier = 0;
      switch(params.bet) {
        case 'red': win = isRed; multiplier = 1.9; break;
        case 'black': win = !isRed && number !== 0; multiplier = 1.9; break;
        case 'even': win = isEven; multiplier = 1.9; break;
        case 'odd': win = !isEven && number !== 0; multiplier = 1.9; break;
        default: win = false; multiplier = 0;
      }
      const profit = win ? bet * multiplier : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { number, isRed, isEven } };
    },
    slot: (bet, params) => {
      const symbols = ['🍒', '🍋', '🍊', '🔔', '💎', '7️⃣'];
      const reels = [symbols[Math.floor(Math.random()*6)], symbols[Math.floor(Math.random()*6)], symbols[Math.floor(Math.random()*6)]];
      let win = false, multiplier = 0;
      if (reels[0] === reels[1] && reels[1] === reels[2]) { win = true; multiplier = 5; }
      else if (reels[0] === reels[1] || reels[1] === reels[2] || reels[0] === reels[2]) { win = true; multiplier = 0.5; }
      const profit = win ? bet * multiplier : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { reels, multiplier } };
    },
    blackjack: (bet, params) => {
      const getCard = () => { const v = Math.floor(Math.random()*13)+1; return { value: Math.min(v,10), display: v }; };
      const pCards = [getCard(), getCard()], dCards = [getCard(), getCard()];
      const score = (cards) => {
        let total = cards.reduce((s,c) => s + c.value, 0);
        const aces = cards.filter(c => c.display === 1).length;
        let adj = total, aUsed = 0;
        while (adj <= 11 && aUsed < aces) { adj += 10; aUsed++; }
        return adj;
      };
      const pScore = score(pCards), dScore = score(dCards);
      let result = 'lose', profit = -bet;
      if (pScore === 21 && pCards.length === 2) { result = 'win'; profit = bet * 2.5; }
      else if (pScore > 21) { result = 'lose'; profit = -bet; }
      else if (dScore > 21) { result = 'win'; profit = bet; }
      else if (pScore > dScore) { result = 'win'; profit = bet; }
      else if (pScore === dScore) { result = 'push'; profit = 0; }
      return { result, profit, details: { pCards: pCards.map(c=>c.display), dCards: dCards.map(c=>c.display), pScore, dScore } };
    },
    aviator: (bet, params) => {
      const crashPoint = 1 + Math.random() * 9;
      const cashOut = params.action === 'cashout' ? Math.min(1 + Math.random() * 5, crashPoint) : 0;
      const win = params.action === 'cashout' && cashOut < crashPoint;
      const multiplier = win ? cashOut : 0;
      const profit = win ? bet * multiplier : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { crashPoint, multiplier } };
    },
    mines: (bet, params) => {
      const gridSize = 25, mineCount = params.mines || 3;
      const mines = []; while (mines.length < mineCount) { const p = Math.floor(Math.random()*gridSize); if(!mines.includes(p)) mines.push(p); }
      const tile = params.tile || Math.floor(Math.random()*gridSize);
      const hit = mines.includes(tile);
      const profit = hit ? -bet : bet * 1.2;
      return { result: hit ? 'lose' : 'win', profit, details: { mines, tile, hit } };
    },
    crash: (bet, params) => {
      const crashPoint = 1 + Math.random() * 9;
      const cashOut = params.action === 'cashout' ? Math.min(1 + Math.random() * 5, crashPoint) : 0;
      const win = params.action === 'cashout' && cashOut < crashPoint;
      const mult = win ? cashOut : 0;
      const profit = win ? bet * mult : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { crashPoint, multiplier: mult } };
    },
    default: (bet, params) => {
      const win = Math.random() < 0.45;
      return { result: win ? 'win' : 'lose', profit: win ? bet * 1.9 : -bet, details: {} };
    }
  };

  // Play Game
  const playGame = async (gameId, params = {}) => {
    if (!user) {
      alert('Please login to play games');
      navigate('/login');
      return;
    }
    const game = GAMES.find(g => g.id === gameId);
    if (!game) return;
    if (betAmount < game.minBet) { alert(`Minimum bet is ${game.minBet} ETB`); return; }
    if (betAmount > game.maxBet) { alert(`Maximum bet is ${game.maxBet} ETB`); return; }
    if (betAmount > balance) { alert(`Insufficient balance. Your balance is ${balance} ETB`); return; }

    setLoading(true);
    try {
      let response;
      try {
        response = await axios.post('/api/casino/play', {
          gameId,
          bet: betAmount,
          params,
          userId: user.id
        });
      } catch (serverError) {
        console.warn('Server unavailable, using client-side fallback');
        const logic = gameLogic[gameId] || gameLogic.default;
        const result = logic(betAmount, params);
        response = { data: { ...result, newBalance: balance + result.profit, gameId } };
      }

      const data = response.data;
      setBalance(data.newBalance);

      setResultData(data);
      setShowResultModal(true);
      setGameHistory(prev => [{ gameId, bet: betAmount, result: data.result, profit: data.profit, details: data.details, timestamp: new Date() }, ...prev].slice(0, 50));

      if (data.result === 'win') playSound('win');
      else if (data.result === 'lose') playSound('lose');

    } catch (error) {
      const msg = error.response?.data?.error || error.response?.data?.message || 'Failed to play game.';
      alert(msg);
    } finally {
      setLoading(false);
    }
  };

  const initGameState = (gameId) => {
    const defaultStates = {
      aviator: { multiplier: 1.0, gameStarted: false, crashed: false, history: [] },
      plinko: { rows: 12, ballPosition: null, multiplier: null },
      blackjack: { playing: false },
      roulette: { spinning: false },
      mines: { revealed: [], mines: [], gameOver: false },
      slot: { reels: ['🍒', '🍒', '🍒'], spinning: false }
    };
    setGameState(prev => ({ ...prev, [gameId]: defaultStates[gameId] || {} }));
  };

  const openGame = (game) => {
    setSelectedGame(game);
    setIsBetPanelOpen(true);
    initGameState(game.id);
  };

  const toggleFavorite = (gameId) => {
    setFavorites(prev => {
      const newFav = prev.includes(gameId) ? prev.filter(id => id !== gameId) : [...prev, gameId];
      localStorage.setItem('shebaodds_favorite_games', JSON.stringify(newFav));
      return newFav;
    });
  };

  const renderGameCard = (game) => (
    <div key={game.id} className={`game-card ${selectedGame?.id === game.id ? 'active' : ''}`} onClick={() => openGame(game)}>
      {game.id === 'aviator' && <span className="badge live">LIVE</span>}
      {['slot','megaball','lucky7'].includes(game.id) && <span className="badge hot">HOT</span>}
      <button className={`favorite-btn ${favorites.includes(game.id) ? 'active' : ''}`} onClick={(e) => { e.stopPropagation(); toggleFavorite(game.id); }}>
        {favorites.includes(game.id) ? '★' : '☆'}
      </button>
      <span className="game-icon">{game.icon}</span>
      <span className="game-name">{language === 'am' ? game.nameAm : game.name}</span>
      <span className="game-min-bet">{game.minBet} ETB</span>
    </div>
  );

  const renderGames = () => {
    const categories = ['crash','classic','table','slots','sports','special'];
    const categoryLabels = { crash: '💥 Crash', classic: '🃏 Classic', table: '🪑 Table', slots: '🎰 Slots', sports: '🏅 Sports', special: '✨ Special' };
    const favGames = GAMES.filter(g => favorites.includes(g.id));
    const otherGames = GAMES.filter(g => !favorites.includes(g.id));

    return (
      <>
        {favGames.length > 0 && (
          <div className="game-category">
            <h3 className="category-title">⭐ Favorites</h3>
            <div className="game-grid">{favGames.map(renderGameCard)}</div>
          </div>
        )}
        {categories.map(cat => {
          const gamesInCat = otherGames.filter(g => g.cat === cat);
          if (gamesInCat.length === 0) return null;
          return (
            <div key={cat} className="game-category">
              <h3 className="category-title">{categoryLabels[cat]} <small>{gamesInCat.length} games</small></h3>
              <div className="game-grid">{gamesInCat.map(renderGameCard)}</div>
            </div>
          );
        })}
      </>
    );
  };

  const renderGameSpecificUI = (gameId) => {
    switch (gameId) {
      case 'aviator':
        return (
          <div className="aviator-game">
            <canvas ref={canvasRef} className="aviator-canvas" width="400" height="200" />
            <div className="aviator-multiplier">{(liveGameData.aviator?.multiplier || 1.0).toFixed(2)}x</div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('aviator', { action: 'bet' })} disabled={loading || liveGameData.aviator?.gameStarted}>
                {loading ? '⏳' : '✈️ Place Bet'}
              </button>
              <button className="btn-cashout" onClick={() => playGame('aviator', { action: 'cashout' })} disabled={loading || !liveGameData.aviator?.gameStarted}>
                💰 Cash Out
              </button>
            </div>
          </div>
        );
      case 'dice':
        return (
          <div className="dice-game">
            <div className="dice-display"><span className="dice">🎲</span><span className="vs-text">VS</span><span className="dice">🎲</span></div>
            <div className="dice-info">Roll higher than the house to win!</div>
            <button className="btn-play" onClick={() => playGame('dice')} disabled={loading}>{loading ? '🎲 Rolling...' : '🎲 Roll Dice'}</button>
          </div>
        );
      case 'coinflip':
        return (
          <div className="coinflip-game">
            <div className="coin-display">🪙</div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('coinflip', { side: 'heads' })} disabled={loading}>Heads</button>
              <button className="btn-bet" onClick={() => playGame('coinflip', { side: 'tails' })} disabled={loading}>Tails</button>
            </div>
          </div>
        );
      case 'plinko':
        return (
          <div className="plinko-game">
            <div className="plinko-multiplier">{(gameState.plinko?.multiplier || 1.0)}x</div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('plinko', { rows: 8 })}>8 Rows</button>
              <button className="btn-bet" onClick={() => playGame('plinko', { rows: 12 })}>12 Rows</button>
              <button className="btn-bet" onClick={() => playGame('plinko', { rows: 16 })}>16 Rows</button>
            </div>
            <button className="btn-play" onClick={() => playGame('plinko', { rows: gameState.plinko?.rows || 12 })} disabled={loading}>
              {loading ? '🔽 Dropping...' : '🔴 Drop Ball'}
            </button>
          </div>
        );
      case 'blackjack':
        return (
          <div className="blackjack-game">
            <div className="bj-display">
              <div className="bj-hand"><span className="hand-label">Dealer</span><span>🃏 <span className="hand-score">0</span></span></div>
              <div className="bj-hand"><span className="hand-label">You</span><span>🃏 <span className="hand-score">0</span></span></div>
            </div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('blackjack', { action: 'hit' })} disabled={loading || !gameState.blackjack?.playing}>Hit</button>
              <button className="btn-bet" onClick={() => playGame('blackjack', { action: 'stand' })} disabled={loading || !gameState.blackjack?.playing}>Stand</button>
              <button className="btn-play" onClick={() => { setGameState(prev => ({...prev, blackjack:{playing:true}})); playGame('blackjack', { action: 'deal' }); }} disabled={loading}>New Hand</button>
            </div>
          </div>
        );
      case 'slot':
        return (
          <div className="slot-game">
            <div className="slot-reels"><span>🍒</span><span>🍒</span><span>🍒</span></div>
            <button className="btn-play spin" onClick={() => { playSound('slotSpin'); playGame('slot'); }} disabled={loading}>
              {loading ? '🔄 Spinning...' : '🎰 Spin'}
            </button>
          </div>
        );
      default:
        return (
          <div className="default-game">
            <div className="game-icon-large">{GAMES.find(g=>g.id===gameId)?.icon || '🎮'}</div>
            <div className="game-info-text">Place your bet and try your luck!</div>
            <button className="btn-play" onClick={() => playGame(gameId)} disabled={loading}>
              {loading ? '⏳ Playing...' : '▶️ Play Now'}
            </button>
          </div>
        );
    }
  };

  return (
    <div className="casino-games-page">
      <div className="casino-header">
        <div className="header-left">
          <h1>🎰 {language === 'am' ? 'ካሲኖ ጨዋታዎች' : 'Casino Games'}</h1>
          <span className="game-count">{GAMES.length} {language === 'am' ? 'ጨዋታዎች' : 'Games'}</span>
        </div>
        <div className="balance-box">
          <span>💰 {language === 'am' ? 'ቀሪ ሂሳብ' : 'Balance'}</span>
          <span className="balance-amount">{balance.toLocaleString()} ETB</span>
          <button className="refresh-balance" onClick={fetchBalance}>🔄</button>
        </div>
      </div>

      <div className="games-container">{renderGames()}</div>

      {selectedGame && (
        <div className="game-view">
          <div className="game-view-header">
            <h2 className="game-title">{selectedGame.icon} {language === 'am' ? selectedGame.nameAm : selectedGame.name}</h2>
            <div className="game-stats">
              <span>💰 {balance.toLocaleString()} ETB</span>
              <span>📂 {selectedGame.cat.toUpperCase()}</span>
            </div>
          </div>
          <div className="game-area">{renderGameSpecificUI(selectedGame.id)}</div>
          <div className="game-tutorial">
            <button className="tutorial-btn" onClick={() => alert(`How to play ${selectedGame.name}: Place your bet and try your luck!`)}>❓ How to play</button>
          </div>
        </div>
      )}

      {gameHistory.length > 0 && (
        <div className="game-history">
          <h4>📜 Recent Games</h4>
          <div className="history-list">
            {gameHistory.slice(0, 10).map((g, i) => (
              <div key={i} className={`history-item ${g.result}`}>
                <span className="history-game">{g.gameId}</span>
                <span className="history-bet">{g.bet} ETB</span>
                <span className="history-result">{g.result === 'win' ? '✅' : '❌'}</span>
                <span className={`history-profit ${g.profit >= 0 ? 'positive' : 'negative'}`}>
                  {g.profit >= 0 ? '+' : ''}{g.profit} ETB
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {isBetPanelOpen && (
        <div className="bet-panel">
          <div className="game-info">
            <span>{selectedGame?.icon} {language === 'am' ? selectedGame?.nameAm : selectedGame?.name}</span>
            <small>Min: {selectedGame?.minBet} ETB</small>
          </div>
          <div className="quick-bets">
            {[{label:'10%',value:Math.round(balance*0.1)},{label:'25%',value:Math.round(balance*0.25)},{label:'50%',value:Math.round(balance*0.5)},{label:'100%',value:Math.round(balance)}].map(qb => (
              <button key={qb.label} className="quick-bet-btn" onClick={() => setBetAmount(Math.max(qb.value, 1))} disabled={qb.value < 1}>{qb.label}</button>
            ))}
          </div>
          <div className="bet-amounts">
            {[1,2,5,10,20,50,100,500,1000].map(amt => (
              <button key={amt} className={`amt-btn ${betAmount === amt ? 'active' : ''}`} onClick={() => setBetAmount(amt)}>{amt}</button>
            ))}
            <button className="amt-btn max" onClick={() => setBetAmount(Math.min(balance, selectedGame?.maxBet || 10000))}>MAX</button>
          </div>
          <div className="manual-input">
            <input type="number" value={betAmount} onChange={(e) => setBetAmount(Math.max(1, Number(e.target.value)))} min="1" /><span>ETB</span>
          </div>
          <div className="bet-actions">
            <button className="play-btn" onClick={() => playGame(selectedGame?.id)} disabled={loading || betAmount > balance}>{loading ? '⏳' : '▶️ Play'}</button>
            <button className="close-btn" onClick={() => setIsBetPanelOpen(false)}>✕</button>
          </div>
          <div className="balance-display">{language === 'am' ? 'ቀሪ ሂሳብ' : 'Balance'}: <strong>{balance.toLocaleString()} ETB</strong></div>
          {betAmount > balance && <div className="balance-warning">⚠️ Insufficient balance</div>}
        </div>
      )}

      {showResultModal && resultData && (
        <div className="result-modal-overlay" onClick={() => setShowResultModal(false)}>
          <div className="result-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-icon">{resultData.result === 'win' ? '🎉' : '😔'}</div>
            <h2 className="modal-title">{language === 'am' ? selectedGame?.nameAm : selectedGame?.name}</h2>
            <div className={`result-value ${resultData.result}`}>
              {resultData.result === 'win' ? '✅ WIN' : resultData.result === 'lose' ? '❌ LOSE' : '⚖️ PUSH'}
            </div>
            <div className="result-details">
              <div>{language === 'am' ? 'ውርርድ' : 'Bet'}: <span>{betAmount} ETB</span></div>
              <div>{language === 'am' ? 'ትርፍ' : 'Profit'}: <span className={resultData.result === 'win' ? 'green' : 'red'}>{resultData.profit} ETB</span></div>
              <div>{language === 'am' ? 'አዲስ ቀሪ ሂሳብ' : 'New Balance'}: <span className="gold">{resultData.newBalance} ETB</span></div>
            </div>
            {resultData.details && Object.keys(resultData.details).length > 0 && (
              <div className="result-extra">
                {Object.entries(resultData.details).map(([key, value]) => (
                  <div key={key}>{key}: <span>{typeof value === 'object' ? JSON.stringify(value) : value}</span></div>
                ))}
              </div>
            )}
            <button className="modal-btn" onClick={() => setShowResultModal(false)}>
              {language === 'am' ? 'ቀጥል' : 'Continue'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}