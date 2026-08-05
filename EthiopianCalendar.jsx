// components/EthiopianCalendar.jsx
import React, { useState, useEffect } from 'react';
import { toEthiopian, getEthiopianMonthName, getDaysInEthiopianMonth } from '../utils/ethiopianCalendar';
import { useTranslation } from '../LanguageContext';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function EthiopianCalendar({ onDateSelect, matches = [] }) {
  const { language } = useTranslation();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [ethYear, setEthYear] = useState(0);
  const [ethMonth, setEthMonth] = useState(0);
  const [selectedDay, setSelectedDay] = useState(null);

  useEffect(() => {
    const eth = toEthiopian(currentDate);
    setEthYear(eth.year);
    setEthMonth(eth.month);
    if (!selectedDay) setSelectedDay(eth.day);
  }, [currentDate]);

  const goToPrevMonth = () => {
    if (ethMonth > 1) {
      setEthMonth(ethMonth - 1);
    } else {
      setEthMonth(13);
      setEthYear(ethYear - 1);
    }
  };

  const goToNextMonth = () => {
    if (ethMonth < 13) {
      setEthMonth(ethMonth + 1);
    } else {
      setEthMonth(1);
      setEthYear(ethYear + 1);
    }
  };

  const daysInMonth = getDaysInEthiopianMonth(ethYear, ethMonth);
  const monthName = getEthiopianMonthName(ethMonth, language);

  // Build day grid (we'll assume each month starts on a fixed weekday for simplicity)
  // For a real app, compute day of week, but we'll just display days.
  const days = [];
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(i);
  }

  // Helper: check if a day has matches
  const hasMatchOnDay = (day) => {
    return matches.some(m => m.ethYear === ethYear && m.ethMonth === ethMonth && m.ethDay === day);
  };

  const handleDayClick = (day) => {
    setSelectedDay(day);
    if (onDateSelect) {
      onDateSelect({ year: ethYear, month: ethMonth, day });
    }
  };

  return (
    <div className="ethiopian-calendar">
      <div className="calendar-header">
        <button onClick={goToPrevMonth} className="nav-btn"><ChevronLeft size={20} /></button>
        <span className="month-title">{monthName} {ethYear}</span>
        <button onClick={goToNextMonth} className="nav-btn"><ChevronRight size={20} /></button>
      </div>
      <div className="calendar-grid">
        {days.map((day) => {
          const isSelected = selectedDay === day;
          const hasMatch = hasMatchOnDay(day);
          return (
            <div
              key={day}
              className={`calendar-day ${isSelected ? 'selected' : ''} ${hasMatch ? 'has-match' : ''}`}
              onClick={() => handleDayClick(day)}
            >
              <span className="day-number">{day}</span>
              {hasMatch && <span className="match-indicator">●</span>}
            </div>
          );
        })}
      </div>
    </div>
  );
}