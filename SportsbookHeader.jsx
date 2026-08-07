// SportsbookHeader.jsx – Pure top header with user info and logout
import React, { useState } from 'react';
import { useAuth } from "./contexts/AuthContext";
import { useTranslation } from './LanguageContext';
import { Link } from 'react-router-dom';
import { User, Wallet, Globe, ChevronDown, Bell, Menu, X, LogOut } from 'lucide-react';

export default function SportsbookHeader({ onLogout }) {
  const { user, logout: contextLogout, biometricAvailable } = useAuth?.() || {};
  const { t, language, setLanguage } = useTranslation?.() || { t: (key) => key, language: 'en', setLanguage: () => {} };
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    if (onLogout) {
      onLogout();
    } else if (contextLogout) {
      contextLogout();
    }
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-[#111625]/90 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">

        {/* Left: Logo */}
        <Link to="/" className="flex items-center gap-2 cursor-pointer">
          <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-sky-500 to-amber-500 flex items-center justify-center font-black text-slate-950 text-lg">
            X
          </div>
          <span className="text-xl font-black tracking-wider bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent hidden sm:block">
            SHEBA<span className="text-amber-400">ODDS</span>
          </span>
        </Link>

        {/* Center: Search (hidden on mobile) */}
        <div className="hidden md:flex flex-1 max-w-md mx-4">
          <div className="relative w-full">
            <input
              type="text"
              placeholder={t('search_placeholder') || "Search matches, leagues, teams..."}
              className="w-full bg-[#090d16] border border-slate-800 rounded-lg py-1.5 px-4 pr-10 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 transition"
            />
            <span className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500">🔍</span>
          </div>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-3">
          {/* Balance (only if logged in) */}
          {user && (
            <div className="hidden sm:flex items-center gap-2 bg-[#090d16] border border-slate-800 rounded-lg p-1.5 pr-3">
              <div className="p-1.5 bg-sky-500/10 rounded-md text-sky-400">
                <Wallet className="h-4 w-4" />
              </div>
              <div className="text-right">
                <p className="text-[11px] font-mono font-bold text-emerald-400 leading-none">
                  {user.wallet?.balance?.toFixed(2) || '0.00'} <span className="text-[9px] text-slate-400 font-sans font-normal">ETB</span>
                </p>
                <p className="text-[9px] text-slate-500 font-medium mt-0.5 leading-none">
                  Bonus: {user.wallet?.bonusBalance?.toFixed(2) || '0.00'}
                </p>
              </div>
            </div>
          )}

          {/* Language toggle */}
          <button className="hidden sm:flex items-center gap-1 px-2.5 py-1.5 rounded-lg border border-slate-800 bg-[#151c2e] text-xs font-bold hover:border-slate-700 transition-all">
            <Globe className="h-3.5 w-3.5 text-slate-400" />
            <span>{language.toUpperCase()}</span>
            <ChevronDown className="h-3 w-3 text-slate-500" />
          </button>

          {/* Notifications */}
          <button className="relative p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors">
            <Bell className="h-4 w-4" />
            <span className="absolute -top-1 -right-1 h-4 w-4 bg-rose-600 text-white rounded-full flex items-center justify-center text-[9px] font-bold">3</span>
          </button>

          {/* User avatar / profile */}
          {user ? (
            <div className="flex items-center gap-2">
              <Link to="/profile" className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors relative">
                <User className="h-4 w-4" />
                {biometricAvailable && (
                  <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-green-500 rounded-full border border-[#111625]"></span>
                )}
              </Link>
              {/* Logout button */}
              <button
                onClick={handleLogout}
                className="p-2 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 rounded-lg transition-colors"
              >
                <LogOut className="h-4 w-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/login" className="text-sm font-semibold text-slate-300 hover:text-white transition-colors">
                {t('login') || 'Login'}
              </Link>
              <Link to="/register" className="bg-amber-500 text-[#090d16] px-4 py-2 rounded-lg text-sm font-bold hover:bg-amber-400 transition-colors">
                {t('register') || 'Sign Up'}
              </Link>
            </div>
          )}

          {/* Mobile menu toggle */}
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="p-2 text-slate-400 hover:text-white md:hidden transition-colors"
          >
            {isMobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile dropdown menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-b border-slate-800 bg-[#111625] px-4 py-3 space-y-2 font-bold uppercase tracking-wide text-xs text-slate-400">
          <Link to="/" className="block px-3 py-2 rounded-md hover:text-white">Home</Link>
          <Link to="/live" className="block px-3 py-2 rounded-md hover:text-white">Live</Link>
          <Link to="/casino" className="block px-3 py-2 rounded-md text-amber-400">Casino</Link>
          <Link to="/profile" className="block px-3 py-2 rounded-md hover:text-white">Profile</Link>
          <Link to="/my-bets" className="block px-3 py-2 rounded-md hover:text-white">My Bets</Link>
          <Link to="/wallet" className="block px-3 py-2 rounded-md hover:text-white">Wallet</Link>
          {user && (
            <button onClick={handleLogout} className="block w-full text-left px-3 py-2 rounded-md text-rose-400 hover:bg-rose-500/10 transition-colors">
              Logout
            </button>
          )}
          {!user && (
            <>
              <Link to="/login" className="block px-3 py-2 rounded-md text-amber-400">Login</Link>
              <Link to="/register" className="block px-3 py-2 rounded-md text-amber-400">Register</Link>
            </>
          )}
        </div>
      )}
    </header>
  );
}