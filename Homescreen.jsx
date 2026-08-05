import React from "react";

export default function HomeScreen() {
  return (
    <div className="home-screen">

      <div className="balance-card">
        <h2>12,456.50 ETB</h2>
        <p>Total Balance</p>

        <div className="wallet-buttons">
          <button className="deposit-btn">Deposit</button>
          <button className="withdraw-btn">Withdraw</button>
        </div>
      </div>

      <div className="welcome-card">
        <h3>🎁 Welcome Bonus</h3>
        <p>100% bonus up to 10,000 ETB</p>
        <button>Join Now</button>
      </div>

      <h3>Top Sports</h3>

      <div className="sports-row">
        <button>⚽ Football</button>
        <button>🏀 Basketball</button>
        <button>🎾 Tennis</button>
        <button>🏐 Volleyball</button>
      </div>

      <h3>Featured Matches</h3>

      <div className="match-card">
        <h4>Ethiopia Bunna vs St. George</h4>

        <div className="odds">
          <button>2.10</button>
          <button>3.20</button>
          <button>3.45</button>
        </div>
      </div>

    </div>
  );
}