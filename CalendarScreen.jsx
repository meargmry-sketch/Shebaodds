import React, { useEffect, useState } from "react";
import EthiopianCalendar from "./EthiopianCalendar.jsx";

export default function CalendarScreen() {
  const [matches, setMatches] = useState([]);
  const [selectedDayMatches, setSelectedDayMatches] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const today = new Date();

    setMatches([
      {
        id: 1,
        homeTeam: "Real Madrid",
        awayTeam: "Barcelona",
        startTime: new Date(
          today.getTime() + 24 * 60 * 60 * 1000
        ).toISOString(),
      },
      {
        id: 2,
        homeTeam: "Arsenal",
        awayTeam: "Chelsea",
        startTime: new Date(
          today.getTime() + 3 * 24 * 60 * 60 * 1000
        ).toISOString(),
      },
    ]);

    setLoading(false);
  }, []);

  const handleDateSelect = (date) => {
    setSelectedDate(date);

    // The calendar can provide the selected date.
    // For now, show all available matches.
    setSelectedDayMatches(matches);
  };

  if (loading) {
    return (
      <div className="loading-spinner">
        Loading calendar...
      </div>
    );
  }

  return (
    <div className="calendar-screen">
      <h1 className="page-title">
        📅 Ethiopian Calendar
      </h1>

      <EthiopianCalendar
        onDateSelect={handleDateSelect}
        matches={matches}
      />

      <div className="selected-matches">
        <h2>
          Matches on{" "}
          {selectedDate
            ? `${selectedDate.day}/${selectedDate.month}/${selectedDate.year}`
            : "selected day"}
        </h2>

        {selectedDayMatches.length === 0 ? (
          <p className="no-matches">
            No matches scheduled for this day
          </p>
        ) : (
          <div className="matches-list">
            {selectedDayMatches.map((match) => (
              <div
                key={match.id}
                className="match-item"
              >
                <span className="teams">
                  {match.homeTeam} vs {match.awayTeam}
                </span>

                <span className="time">
                  {new Date(
                    match.startTime
                  ).toLocaleTimeString()}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}