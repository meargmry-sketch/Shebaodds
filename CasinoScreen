// CasinoScreen.jsx – Casino games hub
import React from 'react';
import CasinoGames from './CasinoGames'; // your existing 51+ games component
import { useTranslation } from './LanguageContext';

export default function CasinoScreen() {
  const { t } = useTranslation?.() || { t: (key) => key };

  return (
    <div className="casino-screen">
      {/* The full casino games interface – already styled in global.css */}
      <CasinoGames />
    </div>
  );
}