function Home() {
  return (
    <div style={styles.container}>
      <h2 style={styles.logo}>ShebaOdds</h2>

      {/* Wallet */}
      <div style={styles.card}>
        <p style={styles.label}>Total Balance</p>
        <h1 style={styles.balance}>12,456.50 ETB</h1>

        <div style={styles.buttonRow}>
          <button style={styles.deposit}>Deposit</button>
          <button style={styles.withdraw}>Withdraw</button>
        </div>
      </div>

      {/* Welcome Bonus */}
      <div style={styles.banner}>
        <h3>🎁 Welcome Bonus</h3>
        <p>100% Bonus up to 10,000 ETB</p>
        <button style={styles.join}>Join Now</button>
      </div>

      {/* Sports */}
      <h3 style={styles.title}>Top Sports</h3>

      <div style={styles.sports}>
        <div style={styles.sport}>⚽ Football</div>
        <div style={styles.sport}>🏀 Basketball</div>
        <div style={styles.sport}>🎾 Tennis</div>
        <div style={styles.sport}>🏐 Volleyball</div>
        <div style={styles.sport}>🏏 Cricket</div>
      </div>

      {/* Featured Match */}
      <h3 style={styles.title}>Featured Match</h3>

      <div style={styles.match}>
        <h4>Ethiopian Premier League</h4>

        <p>Bunna vs St. George FC</p>

        <div style={styles.odds}>
          <button style={styles.odd}>1<br />2.10</button>
          <button style={styles.odd}>X<br />3.20</button>
          <button style={styles.odd}>2<br />3.45</button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    padding: 20,
    color: "white",
    background: "#0B1020",
    minHeight: "100vh",
  },

  logo: {
    color: "#FACC15",
    textAlign: "center",
    marginBottom: 20,
  },

  card: {
    background: "#161B2D",
    borderRadius: 15,
    padding: 20,
    marginBottom: 20,
  },

  label: {
    color: "#A1A1AA",
    margin: 0,
  },

  balance: {
    color: "#22C55E",
    margin: "10px 0",
  },

  buttonRow: {
    display: "flex",
    gap: 10,
    marginTop: 15,
  },

  deposit: {
    flex: 1,
    background: "#2563EB",
    color: "white",
    border: "none",
    borderRadius: 10,
    padding: 12,
    fontWeight: "bold",
  },

  withdraw: {
    flex: 1,
    background: "#374151",
    color: "white",
    border: "none",
    borderRadius: 10,
    padding: 12,
    fontWeight: "bold",
  },

  banner: {
    background: "#1E3A8A",
    borderRadius: 15,
    padding: 20,
    marginBottom: 20,
  },

  join: {
    marginTop: 10,
    background: "#FACC15",
    border: "none",
    borderRadius: 10,
    padding: "10px 20px",
    fontWeight: "bold",
  },

  title: {
    marginTop: 20,
    marginBottom: 10,
  },

  sports: {
    display: "flex",
    overflowX: "auto",
    gap: 10,
    marginBottom: 20,
  },

  sport: {
    background: "#161B2D",
    padding: 15,
    borderRadius: 10,
    whiteSpace: "nowrap",
  },

  match: {
    background: "#161B2D",
    padding: 20,
    borderRadius: 15,
  },

  odds: {
    display: "flex",
    gap: 10,
    marginTop: 15,
  },

  odd: {
    flex: 1,
    background: "#2563EB",
    color: "white",
    border: "none",
    borderRadius: 10,
    padding: 15,
    fontWeight: "bold",
  },
};

export default Home;