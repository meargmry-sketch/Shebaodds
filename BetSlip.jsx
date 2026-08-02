import React, { useState, useEffect } from 'react';
import { Trash2, AlertCircle, CheckCircle2, Ticket, ReceiptText, Gamepad2 } from 'lucide-react';

export default function BetSlip() {
  // Mock active wagers inside the user's bet slip matrix (Sportsbook)
  const [selections, setSelections] = useState([
    { id: 101, match: 'Ethiopia vs Egypt', market: '1X2', selection: 'Ethiopia (Home)', odds: 2.45, previousOdds: 2.45, status: 'stable' },
    { id: 102, match: 'Arsenal vs Chelsea', market: 'Both Teams to Score', selection: 'Yes', odds: 1.85, previousOdds: 1.85, status: 'stable' }
  ]);

  // ==========================================================
  // 🎰 NEW: CASINO GAME INTEGRATION
  // ==========================================================
  const [activeMode, setActiveMode] = useState('Sportsbook'); // 'Sportsbook', 'Casino'
  const [selectedCasinoGame, setSelectedCasinoGame] = useState('aviator');
  const [casinoMultiplier, setCasinoMultiplier] = useState(1.0);
  const [casinoResult, setCasinoResult] = useState(null);

  const CASINO_GAMES = [
    { id: 'dice', name: 'Dice' },
    { id: 'aviator', name: 'Aviator' },
    { id: 'coinflip', name: 'CoinFlip' },
    { id: 'plinko', name: 'Plinko' },
    { id: 'blackjack', name: 'Blackjack' },
    { id: 'roulette', name: 'Roulette' },
    { id: 'mines', name: 'Mines' },
    { id: 'crash', name: 'Crash' },
    { id: 'tower', name: 'Tower' },
    { id: 'keno', name: 'Keno' },
    { id: 'baccarat', name: 'Baccarat' },
    { id: 'wheel', name: 'Wheel of Fortune' },
    { id: 'hilo', name: 'Hilo' },
    { id: 'sicbo', name: 'Sic Bo' },
    { id: 'videopoker', name: 'Video Poker' },
    { id: 'bingo', name: 'Bingo' },
    { id: 'craps', name: 'Craps' },
    { id: 'dragontiger', name: 'Dragon Tiger' },
    { id: 'andarbahar', name: 'Andar Bahar' },
    { id: 'teenpatti', name: 'Teen Patti' },
    { id: 'lucky7', name: 'Lucky 7' },
    { id: 'scratch', name: 'Scratch Card' },
    { id: 'football', name: 'Football Prediction' },
    { id: 'basketball', name: 'Basketball Prediction' },
    { id: 'horseracing', name: 'Horse Racing' },
    { id: 'spinwin', name: 'Spin & Win' },
    { id: 'slot', name: 'Slot Machine' },
    { id: 'reddog', name: 'Red Dog' },
    { id: 'war', name: 'War' },
    { id: 'paigow', name: 'Pai Gow Poker' },
    { id: 'diceduels', name: 'Dice Duels' },
    { id: 'penalty', name: 'Penalty' },
    { id: 'chickenroad', name: 'Chicken Road' },
    { id: 'chickenshot', name: 'Chicken Shot' },
    { id: 'megaball', name: 'Mega Ball' },
    { id: 'pokerdice', name: 'Poker Dice' },
    { id: 'lightningdice', name: 'Lightning Dice' },
    { id: 'carroulette', name: 'Car Roulette' },
    { id: 'knockout', name: 'Knock Out' },
    { id: 'rummy', name: 'Rummy' },
    { id: 'darts', name: 'Darts' },
    { id: 'tennis', name: 'Tennis' },
    { id: 'baseball', name: 'Baseball' },
    { id: 'greyhound', name: 'Greyhound Racing' },
    { id: 'motorbike', name: 'Motorbike Racing' },
    { id: 'cricket', name: 'Cricket' },
    { id: 'roulette360', name: 'Roulette 360' },
    { id: 'megawheel', name: 'Mega Wheel' },
    { id: 'monopoly', name: 'Monopoly' },
    { id: 'virtualsports', name: 'Virtual Sports' },
    { id: 'texasholdem', name: 'Texas Hold\'em' }
  ];

  // Simulate real-time data provider webhook changes over WebSockets every 5 seconds (Sportsbook only)
  useEffect(() => {
    const interval = setInterval(() => {
      // Only update if in Sportsbook mode
      if (activeMode !== 'Sportsbook') return;
      
      setSelections(prev => 
        prev.map(item => {
          if (Math.random() > 0.8) {
            const delta = (Math.random() * 0.4 - 0.2);
            const newOdds = Math.max(1.10, parseFloat((item.odds + delta).toFixed(2)));
            return {
              ...item,
              previousOdds: item.odds,
              odds: newOdds,
              status: newOdds > item.odds ? 'up' : 'down'
            };
          }
          return { ...item, status: 'stable' };
        })
      );
    }, 5000);

    return () => clearInterval(interval);
  }, [activeMode]);

  // Remove a particular game selection from the slip
  const removeSelection = (id) => {
    setSelections(selections.filter(item => item.id !== id));
  };

  // Clear entire betting board state arrays
  const clearSlip = () => {
    setSelections([]);
    setStake('');
    setCasinoResult(null);
  };

  // Compute total combined accumulator odds (Sportsbook)
  const totalOdds = selections.reduce((acc, curr) => acc * curr.odds, 1).toFixed(2);

  // Financial breakdown computations matching East African statutory tax rules
  const [stake, setStake] = useState('');
  const [betPlaced, setBetPlaced] = useState(false);
  const [oddsAcceptedMode, setOddsAcceptedMode] = useState('ask');

  const numericalStake = parseFloat(stake) || 0;
  
  // Determine potential payout based on mode
  let grossPayout = 0;
  let finalNetReturn = 0;
  let taxAmount = 0;

  if (activeMode === 'Sportsbook') {
    grossPayout = numericalStake * parseFloat(totalOdds);
    const netWinnings = grossPayout > numericalStake ? grossPayout - numericalStake : 0;
    taxAmount = netWinnings * 0.10; // 10% Withholding Tax
    finalNetReturn = grossPayout - taxAmount;
  } else {
    // Casino mode: win is based on the multiplier
    const isWin = casinoResult === 'win';
    grossPayout = isWin ? numericalStake * casinoMultiplier : 0;
    const netWinnings = isWin ? grossPayout - numericalStake : 0;
    taxAmount = netWinnings * 0.10; // 10% Withholding Tax
    finalNetReturn = grossPayout - taxAmount;
  }

  const handlePlaceWager = (e) => {
    e.preventDefault();
    if (numericalStake <= 0) return;
    
    if (activeMode === 'Sportsbook' && selections.length === 0) {
      alert('Please add at least one selection to the slip.');
      return;
    }

    if (activeMode === 'Casino' && !selectedCasinoGame) {
      alert('Please select a Casino game.');
      return;
    }

    // Simulate Casino Game Result
    if (activeMode === 'Casino') {
      // Generate a random multiplier between 1.0x and 10.0x
      const mult = parseFloat((1.0 + Math.random() * 9.0).toFixed(2));
      setCasinoMultiplier(mult);
      
      // 45% chance of winning
      const win = Math.random() < 0.45;
      setCasinoResult(win ? 'win' : 'lose');
    }

    setBetPlaced(true);
    setTimeout(() => {
      setBetPlaced(false);
      clearSlip();
    }, 4000);
  };

  return (
    <div className="w-full max-w-sm bg-[#111625] border border-slate-800 rounded-xl shadow-2xl text-slate-200 overflow-hidden font-sans">

      {/* Bet Slip Tab Headers */}
      <div className="bg-[#151c2e] px-4 py-3 border-b border-slate-800 flex justify-between items-center">
        <div className="flex items-center gap-2">
          <Ticket className="h-4 w-4 text-sky-400" />
          <h2 className="font-bold tracking-wide uppercase text-xs text-white">Bet Slip</h2>
          <span className="bg-sky-500/10 text-sky-400 text-[10px] font-extrabold px-1.5 py-0.5 rounded-full">
            {activeMode === 'Casino' ? '🎰' : selections.length}
          </span>
        </div>
        {selections.length > 0 && activeMode === 'Sportsbook' && (
          <button 
            onClick={clearSlip} 
            className="text-slate-400 hover:text-rose-400 transition-colors text-[11px] font-medium uppercase tracking-wider"
          >
            Clear All
          </button>
        )}
      </div>

      {/* Slip Classification Mode Selector Switches (Added Casino Tab) */}
      <div className="grid grid-cols-3 bg-[#090d16] p-1 border-b border-slate-800/60 text-center text-xs font-semibold">
        <button 
          onClick={() => setActiveMode('Sportsbook')}
          className={`py-2 rounded-md transition-all ${activeMode === 'Sportsbook' ? 'bg-[#111625] text-white shadow-sm' : 'text-slate-400 hover:text-slate-200'}`}
        >
          Single
        </button>
        <button 
          onClick={() => setActiveMode('Accumulator')}
          className={`py-2 rounded-md transition-all ${activeMode === 'Accumulator' ? 'bg-[#111625] text-white shadow-sm' : 'text-slate-400 hover:text-slate-200'}`}
        >
          Accum
        </button>
        <button 
          onClick={() => setActiveMode('Casino')}
          className={`py-2 rounded-md transition-all ${activeMode === 'Casino' ? 'bg-[#111625] text-white shadow-sm' : 'text-slate-400 hover:text-slate-200'}`}
        >
          🎰 Casino
        </button>
      </div>

      {/* Main Container Body */}
      <div className="p-4 max-h-[280px] overflow-y-auto space-y-3 custom-scrollbar">
        {betPlaced ? (
          <div className="py-10 text-center space-y-3 animate-fade-in">
            <div className="inline-flex p-3 bg-emerald-500/10 rounded-full text-emerald-400 mx-auto">
              <CheckCircle2 className="h-8 w-8 animate-bounce" />
            </div>
            <h3 className="text-sm font-bold text-white">
              {activeMode === 'Casino' ? '🎰 Casino Round Complete!' : 'Wager Successfully Placed!'}
            </h3>
            <p className="text-xs text-slate-400 px-4">
              {activeMode === 'Casino' 
                ? `You ${casinoResult === 'win' ? 'WON!' : 'LOST'} with a ${casinoMultiplier}x multiplier.`
                : 'Ticket transferred asynchronously to RabbitMQ queue cluster processing nodes...'
              }
            </p>
          </div>
        ) : activeMode === 'Casino' ? (
          // 🎰 CASINO GAME SELECTOR
          <div className="py-4 text-center space-y-4">
            <div className="flex items-center justify-center gap-2 text-amber-400">
              <Gamepad2 className="h-6 w-6" />
              <span className="font-bold text-sm">Select Casino Game</span>
            </div>
            <select 
              value={selectedCasinoGame}
              onChange={(e) => setSelectedCasinoGame(e.target.value)}
              className="w-full bg-[#090d16] border border-slate-700 rounded-lg p-2 text-xs font-bold text-white outline-none"
            >
              {CASINO_GAMES.map(game => (
                <option key={game.id} value={game.id}>{game.name}</option>
              ))}
            </select>
            {casinoResult && (
              <div className={`p-2 rounded-lg ${casinoResult === 'win' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-rose-500/20 text-rose-400'}`}>
                {casinoResult === 'win' ? '🎉 You Won!' : '💔 You Lost!'} (Multiplier: {casinoMultiplier}x)
              </div>
            )}
          </div>
        ) : selections.length === 0 ? (
          <div className="py-12 text-center text-slate-500 space-y-2">
            <ReceiptText className="h-8 w-8 mx-auto text-slate-600 opacity-60" />
            <p className="text-xs">Your bet slip is empty.</p>
            <p className="text-[11px] text-slate-600 px-6">Select outcome markets from live matches to configure a ticket.</p>
          </div>
        ) : (
          selections.map((item) => (
            <div 
              key={item.id} 
              className={`p-3 rounded-lg border bg-[#090d16]/60 transition-all duration-300 relative overflow-hidden ${
                item.status === 'up' ? 'border-emerald-500 bg-emerald-950/10' : 
                item.status === 'down' ? 'border-rose-500 bg-rose-950/10' : 'border-slate-800/80'
              }`}
            >
              {/* Dynamic Flash Banner Alerts for Odds Movements */}
              {item.status !== 'stable' && (
                <div className={`absolute top-0 right-0 left-0 text-[9px] font-bold text-center py-0.5 animate-pulse uppercase tracking-widest ${
                  item.status === 'up' ? 'bg-emerald-500 text-white' : 'bg-rose-500 text-white'
                }`}>
                  Odds Line Changed
                </div>
              )}

              <div className={`flex justify-between items-start ${item.status !== 'stable' ? 'mt-2' : ''}`}>
                <div className="space-y-0.5 pr-4">
                  <p className="text-[11px] font-bold text-white leading-tight">{item.match}</p>
                  <p className="text-[10px] text-slate-400 font-medium">{item.market}</p>
                  <p className="text-xs font-semibold text-sky-400 mt-1">Selection: {item.selection}</p>
                </div>

                <div className="flex flex-col items-end gap-2 shrink-0">
                  <button 
                    onClick={() => removeSelection(item.id)}
                    className="text-slate-500 hover:text-rose-400 transition-colors"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                  <span className={`font-mono text-xs font-bold px-1.5 py-0.5 rounded ${
                    item.status === 'up' ? 'text-emerald-400 bg-emerald-500/10 font-extrabold scale-105' :
                    item.status === 'down' ? 'text-rose-400 bg-rose-500/10 font-extrabold scale-105' : 'text-amber-400 bg-amber-500/5'
                  } transition-all duration-300`}>
                    {item.odds.toFixed(2)}
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Financial Accounting and Settlement Section */}
      {(selections.length > 0 || activeMode === 'Casino') && !betPlaced && (
        <div className="bg-[#141b2e] border-t border-slate-800 p-4 space-y-3 font-medium text-xs">

          {/* Slip Totals Summary */}
          <div className="space-y-1.5 border-b border-slate-800/60 pb-3 text-slate-400">
            <div className="flex justify-between">
              <span>{activeMode === 'Casino' ? 'Game Selected:' : 'Total Combined Odds:'}</span>
              <span className="font-mono font-bold text-white text-sm text-amber-400">
                {activeMode === 'Casino' ? selectedCasinoGame.toUpperCase() : totalOdds}
              </span>
            </div>
            <div className="flex justify-between text-[11px]">
              <span className="flex items-center gap-1">
                Withholding Government Tax <span className="font-bold text-rose-400">(10%)</span>:
              </span>
              <span className="font-mono text-rose-400">
                {taxAmount > 0 ? `-${taxAmount.toFixed(2)}` : '0.00'} ETB
              </span>
            </div>
          </div>

          {/* Interactive Stake Management Field */}
          <form onSubmit={handlePlaceWager} className="space-y-3">
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Wager Stake Amount (ETB)
              </label>
              <div className="relative rounded-lg overflow-hidden shadow-sm">
                <input 
                  type="number" 
                  min="1"
                  step="any"
                  value={stake}
                  onChange={(e) => setStake(e.target.value)}
                  placeholder="Enter custom stake..." 
                  className="w-full bg-[#090d16] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-lg py-2 pl-3 pr-12 text-xs font-mono font-bold text-white outline-none transition-all"
                  required
                />
                <span className="absolute right-3 top-2 text-[10px] font-extrabold text-slate-500">ETB</span>
              </div>
            </div>

            {/* Net Estimated Payout Metrics Output */}
            <div className="p-2.5 bg-[#090d16]/80 rounded-lg border border-slate-800 flex justify-between items-center">
              <span className="text-slate-400 font-semibold text-[11px]">Net Estimated Return:</span>
              <span className={`font-mono text-sm font-bold ${finalNetReturn > 0 ? 'text-emerald-400' : 'text-slate-400'}`}>
                {finalNetReturn > 0 ? finalNetReturn.toFixed(2) : '0.00'} ETB
              </span>
            </div>

            {/* Slip Settings Controls Option Blocks */}
            <div className="text-[10px] text-slate-500 flex justify-between items-center py-1">
              <span className="flex items-center gap-1"><AlertCircle className="h-3 w-3" /> {activeMode === 'Casino' ? 'Game Mode:' : 'If odds shift downwards:'}</span>
              <select 
                value={activeMode === 'Casino' ? 'casino' : oddsAcceptedMode}
                onChange={(e) => {
                  if (e.target.value !== 'casino') setOddsAcceptedMode(e.target.value);
                }}
                className="bg-[#090d16] border border-slate-800 rounded px-1.5 py-0.5 text-slate-300 font-bold outline-none cursor-pointer"
                disabled={activeMode === 'Casino'}
              >
                <option value="ask">Ask Me</option>
                <option value="higher">Accept Higher Only</option>
                <option value="any">Accept Any Price</option>
                <option value="casino" disabled>🎰 Casino Mode</option>
              </select>
            </div>

            {/* Final Execution Button Submission Gateway */}
            <button 
              type="submit"
              disabled={numericalStake <= 0 || (activeMode === 'Sportsbook' && selections.length === 0)}
              className={`w-full font-bold py-2.5 rounded-lg text-center tracking-wide uppercase transition-all duration-200 shadow-md ${
                numericalStake > 0 && !(activeMode === 'Sportsbook' && selections.length === 0)
                  ? 'bg-amber-500 text-slate-950 hover:bg-amber-400 cursor-pointer active:scale-[0.99]' 
                  : 'bg-slate-800 text-slate-500 cursor-not-allowed'
              }`}
            >
              {activeMode === 'Casino' ? 'Play Casino Game' : 'Place Bet Ticket'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}