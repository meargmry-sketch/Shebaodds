import React, { useState } from "react";

const months = [
  "Meskerem",
  "Tikimt",
  "Hidar",
  "Tahsas",
  "Tir",
  "Yekatit",
  "Megabit",
  "Miazia",
  "Ginbot",
  "Sene",
  "Hamle",
  "Nehase",
  "Pagumen",
];

export default function EthiopianCalendar({
  onDateSelect,
  matches = [],
}) {
  const today = new Date();

  // Approximate Ethiopian year for display.
  const gregorianYear = today.getFullYear();
  const currentMonth = today.getMonth();

  const [selectedDay, setSelectedDay] = useState(
    today.getDate()
  );

  const daysInMonth =
    currentMonth === 1 ? 30 : 30;

  const handleDayClick = (day) => {
    setSelectedDay(day);

    if (onDateSelect) {
      onDateSelect({
        year: gregorianYear - 8,
        month: (currentMonth % 13) + 1,
        day,
      });
    }
  };

  return (
    <div className="ethiopian-calendar">
      <div className="calendar-header">
        <h2>
          {months[currentMonth % 13]}
        </h2>

        <p>
          Ethiopian Year {gregorianYear - 8}
        </p>
      </div>

      <div className="calendar-grid">
        {Array.from(
          { length: daysInMonth },
          (_, index) => {
            const day = index + 1;

            const hasMatch = matches.some(
              (match) => match.ethDay === day
            );

            return (
              <button
                key={day}
                type="button"
                className={
                  selectedDay === day
                    ? "calendar-day selected"
                    : "calendar-day"
                }
                onClick={() =>
                  handleDayClick(day)
                }
              >
                <span>{day}</span>

                {hasMatch && (
                  <small>⚽</small>
                )}
              </button>
            );
          }
        )}
      </div>
    </div>
  );
}