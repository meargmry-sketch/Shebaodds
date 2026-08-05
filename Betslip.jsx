// BetSlip.jsx – Floating bet slip with slide-out panel
import React, { useState, useEffect } from 'react';
import { ShoppingBag, X } from 'lucide-react';

export default function BetSlip() {
  const [isOpen, setIsOpen] = useState(false);
  const [selections, setSelections] = useState([]);

  // Listen for "add to bet slip" events (from match cards)
  useEffect(() => {
    const handleAdd = (e) => {
      setSelections(prev => [...prev, e.detail]);
      setIsOpen(true);
    };
    window.addEventListener('addToBetSlip', handleAdd);
    return () => window.removeEventListener('addToBetSlip', handleAdd);
  }, []);

  const removeSelection = (index) => {
    setSelections(prev => prev.filter((_, i) => i !== index));
  };

  const clearAll = () => setSelections([]);

  const totalOdds = selections.reduce((acc, s) => acc * s.odds, 1);
  const stake = 10; // placeholder – you can add an input later
  const potentialWin = stake * totalOdds;

  return (
    <>
      {/* Floating button (mobile) – hidden when drawer is open */}
      {!isOpen && selections.length > 0 && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 z-30 lg:hidden flex items-center gap-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-4 py-3 rounded-full shadow-2xl transition-transform active:scale-95 duration-150 relative"
        >
          <ShoppingBag className="h-5 w-5" />
          <span className="text-xs uppercase tracking-wider">Bet Slip</span>
          <span className="absolute -top-1.5 -right-1.5 h-5 w-5 bg-rose-600 text-white rounded-full flex items-center justify-center text-[10px] font-black border-2 border-[#090d16] animate-pulse">
            {selections.length}
          </span>
        </button>
      )}

      {/* Desktop sidebar – always visible on large screens */}
      <div className="hidden lg:block sticky top-24">
        <div className="bg-[#111625] border border-slate-800 rounded-xl p-4">
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-3">Bet Slip</h3>
          {selections.length === 0 ? (
            <p className="text-xs text-slate-500">No selections yet</p>
          ) : (
            <>
              <div className="space-y-2 max-h-60 overflow-y-auto">
                {selections.map((sel, i) => (
                  <div key={i} className="flex justify-between items-center bg-[#090d16] p-2 rounded text-xs">
                    <div>
                      <span className="text-white">{sel.homeTeam} vs {sel.awayTeam}</span>
                      <span className="block text-amber-400">{sel.betType} @ {sel.odds.toFixed(2)}</span>
                    </div>
                    <button onClick={() => removeSelection(i)} className="text-rose-500 hover:text-rose-400">
                      <X className="h-3 w-3" />
                    </button>
                  </div>
                ))}
              </div>
              <div className="mt-3 pt-3 border-t border-slate-800 text-xs">
                <div className="flex justify-between"><span>Total Odds</span><span className="font-bold">{totalOdds.toFixed(2)}</span></div>
                <div className="flex justify-between"><span>Stake</span><span>{stake} ETB</span></div>
                <div className="flex justify-between text-amber-400"><span>Potential Win</span><span>{potentialWin.toFixed(2)} ETB</span></div>
                <button className="w-full mt-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold py-2 rounded-lg transition">
                  Place Bet
                </button>
                <button onClick={clearAll} className="w-full mt-1 text-rose-500 hover:text-rose-400 text-xs">
                  Clear All
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Mobile slide-out drawer */}
      <div
        className={`fixed inset-0 z-50 transform lg:hidden transition-all duration-300 ease-in-out ${
          isOpen ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        }`}
      >
        <div onClick={() => setIsOpen(false)} className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm" />
        <div
          className={`absolute right-0 top-0 bottom-0 w-full max-w-sm bg-[#111625] border-l border-slate-800 shadow-2xl flex flex-col transform transition-transform duration-300 ease-in-out ${
            isOpen ? 'translate-x-0' : 'translate-x-full'
          }`}
        >
          <div className="p-4 bg-[#151c2e] border-b border-slate-800 flex items-center justify-between">
            <span className="text-xs font-extrabold uppercase tracking-widest text-slate-400">Bet Slip</span>
            <button onClick={() => setIsOpen(false)} className="p-1.5 rounded-md text-slate-400 hover:text-white bg-[#090d16] border border-slate-800">
              <X className="h-4 w-4" />
            </button>
          </div>
          <div className="flex-1 overflow-y-auto p-4">
            {selections.length === 0 ? (
              <p className="text-sm text-slate-500">No selections yet</p>
            ) : (
              <>
                {selections.map((sel, i) => (
                  <div key={i} className="flex justify-between items-center bg-[#090d16] p-3 rounded mb-2">
                    <div>
                      <span className="text-sm text-white">{sel.homeTeam} vs {sel.awayTeam}</span>
                      <span className="block text-amber-400 text-xs">{sel.betType} @ {sel.odds.toFixed(2)}</span>
                    </div>
                    <button onClick={() => removeSelection(i)} className="text-rose-500">
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                ))}
                <div className="mt-4 pt-3 border-t border-slate-800 text-sm">
                  <div className="flex justify-between"><span>Total Odds</span><span className="font-bold">{totalOdds.toFixed(2)}</span></div>
                  <div className="flex justify-between"><span>Stake</span><span>{stake} ETB</span></div>
                  <div className="flex justify-between text-amber-400"><span>Potential Win</span><span>{potentialWin.toFixed(2)} ETB</span></div>
                  <button className="w-full mt-3 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold py-3 rounded-lg transition">
                    Place Bet
                  </button>
                  <button onClick={clearAll} className="w-full mt-2 text-rose-500 hover:text-rose-400 text-xs">
                    Clear All
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}