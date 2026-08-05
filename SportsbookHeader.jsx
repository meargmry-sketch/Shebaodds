// SportsbookHeader.jsx – Pure top header (no sidebar or page layout)
import React, { useState } from 'react';
import { User, Wallet, Globe, ChevronDown, Sun, Moon, Bell, Menu, X } from 'lucide-react';
import { useAuth, useTranslation } from './LanguageContext'; // adjust imports as needed

export default function SportsbookHeader() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [language, setLanguage] = useState('EN');
  const { theme, toggleTheme } = useAuth?.() || { theme: 'dark', toggleTheme: () => {} };
  const { t } = useTranslation?.() || { t: (key) => key };

  // Simulated balance – replace with real data from context
  const balance = { cash: 2450.75, bonus: 500.00 };
  const notifications = 3; // unread count

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-[#111625]/90 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">

        {/* Left: Logo */}
        <div className="flex items-center gap-2 cursor-pointer">
          <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-sky-500 to-amber-500 flex items-center justify-center font-black text-slate-950 text-lg">
            X
          </div>
          <span className="text-xl font-black tracking-wider bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent hidden sm:block">
            SHEBA<span className="text-amber-400">ODDS</span>
          </span>
        </div>

        {/* Center: Search Bar (hidden on mobile, shown on larger screens) */}
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

          {/* Balance capsule */}
          <div className="hidden sm:flex items-center gap-2 bg-[#090d16] border border-slate-800 rounded-lg p-1.5 pr-3">
            <div className="p-1.5 bg-sky-500/10 rounded-md text-sky-400">
              <Wallet className="h-4 w-4" />
            </div>
            <div className="text-right">
              <p className="text-[11px] font-mono font-bold text-emerald-400 leading-none">
                {balance.cash.toFixed(2)} <span className="text-[9px] text-slate-400 font-sans font-normal">ETB</span>
              </p>
              <p className="text-[9px] text-slate-500 font-medium mt-0.5 leading-none">
                Bonus: {balance.bonus.toFixed(2)}
              </p>
            </div>
          </div>

          {/* Language toggle */}
          <button className="hidden sm:flex items-center gap-1 px-2.5 py-1.5 rounded-lg border border-slate-800 bg-[#151c2e] text-xs font-bold hover:border-slate-700 transition-all">
            <Globe className="h-3.5 w-3.5 text-slate-400" />
            <span>{language}</span>
            <ChevronDown className="h-3 w-3 text-slate-500" />
          </button>

          {/* Notifications */}
          <button className="relative p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors">
            <Bell className="h-4 w-4" />
            {notifications > 0 && (
              <span className="absolute -top-1 -right-1 h-4 w-4 bg-rose-600 text-white rounded-full flex items-center justify-center text-[9px] font-bold">
                {notifications}
              </span>
            )}
          </button>

          {/* Theme toggle */}
          <button
            onClick={toggleTheme}
            className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors"
          >
            {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          </button>

          {/* Profile */}
          <button className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors">
            <User className="h-4 w-4" />
          </button>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="p-2 text-slate-400 hover:text-white md:hidden transition-colors"
          >
            {isMobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile dropdown menu (simple navigation) – can be expanded later */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-b border-slate-800 bg-[#111625] px-4 py-3 space-y-2 font-bold uppercase tracking-wide text-xs text-slate-400">
          <a href="/" className="block px-3 py-2 rounded-md hover:text-white">Home</a>
          <a href="/live" className="block px-3 py-2 rounded-md hover:text-white">Live</a>
          <a href="/casino" className="block px-3 py-2 rounded-md text-amber-400">Casino</a>
          <a href="/profile" className="block px-3 py-2 rounded-md hover:text-white">Profile</a>
        </div>
      )}
    </header>
  );
}