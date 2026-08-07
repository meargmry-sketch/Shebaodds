import React from "react";

export default function CasinoGames() {
  const games = [
    {
      id: 1,
      name: "Live Casino",
      icon: "🎰",
    },
    {
      id: 2,
      name: "Slots",
      icon: "🎯",
    },
    {
      id: 3,
      name: "Roulette",
      icon: "🎡",
    },
    {
      id: 4,
      name: "Blackjack",
      icon: "🃏",
    },
  ];

  return (
    <div className="casino-games">
      <div className="casino-games-header">
        <h2>Casino Games</h2>
        <p>Choose a game to start playing.</p>
      </div>

      <div className="casino-games-grid">
        {games.map((game) => (
          <div
            key={game.id}
            className="casino-game-card"
          >
            <div className="casino-game-icon">
              {game.icon}
            </div>

            <h3>{game.name}</h3>

            <button type="button">
              Play
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}