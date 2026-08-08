// CalendarScreen.jsx
import React, { useState, useEffect } from 'react';
import EthiopianCalendar from "../components/EthiopianCalendar";
import { useTranslation } from '../LanguageContext';
import { toEthiopian } from '../utils/ethiopianCalendar';
import axios from 'axios';

export default function CalendarScreen() {
  const { language } = useTranslation();
  const [matches, setMatches] = useState([]);
  const [selectedDayMatches, setSelectedDayMatches] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch upcoming matches (soon games)
    const fetchMatches = async () => {
      try {
        const res = await axios.get('/api/matches/upcoming');
        const enriched = res.data.map(m => {
          const eth = toEthiopian(new Date(m.startTime));
          return { ...m, ethYear: eth.year, ethMonth: eth.month, ethDay: eth.day };
        });
        setMatches(enriched);
      } catch (error) {
        // Fallback mock
        const now = new Date();
        const ethNow = toEthiopian(now);
        setMatches([
          { id: 1, homeTeam: 'Real Madrid', awayTeam: 'Barcelona', ethYear: ethNow.year, ethMonth: ethNow.month, ethDay: ethNow.day + 1, startTime: new Date(now.getTime() + 86400000).toISOString() },
          { id: 2, homeTeam: 'Arsenal', awayTeam: 'Chelsea', ethYear: ethNow.year, ethMonth: ethNow.month, ethDay: ethNow.day + 3, startTime: new Date(now.getTime() + 3*86400000).toISOString() },
        ]);
      } finally {
        setLoading(false);
      }
    };
    fetchMatches();
  }, []);

  const handleDateSelect = ({ year, month, day }) => {
    const filtered = matches.filter(m => m.ethYear === year && m.ethMonth === month && m.ethDay === day);
    setSelectedDayMatches(filtered);
    setSelectedDate({ year, month, day });
  };

  if (loading) return <div className="loading-spinner">Loading calendar...</div>;

  return (
    <div className="calendar-screen">
      <h1 className="page-title">📅 Ethiopian Calendar</h1>
      <EthiopianCalendar onDateSelect={handleDateSelect} matches={matches} />
      <div className="selected-matches">
        <h2>Matches on {selectedDate ? `${selectedDate.day}/${selectedDate.month}/${selectedDate.year}` : 'selected day'}</h2>
        {selectedDayMatches.length === 0 ? (
          <p className="no-matches">No matches scheduled for this day</p>
        ) : (
          <div className="matches-list">
            {selectedDayMatches.map(m => (
              <div key={m.id} className="match-item">
                <span className="teams">{m.homeTeam} vs {m.awayTeam}</span>
                <span className="time">{new Date(m.startTime).toLocaleTimeString()}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}