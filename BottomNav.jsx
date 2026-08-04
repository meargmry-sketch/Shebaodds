import { NavLink } from "react-router-dom";

const menuItems = [
  { name: "Home", path: "/", icon: "🏠" },
  { name: "Live", path: "/live", icon: "📡" },
  { name: "Bet Slip", path: "/betslip", icon: "🎫" },
  { name: "Casino", path: "/casino", icon: "🎰" },
  { name: "Profile", path: "/profile", icon: "👤" },
  { name: "More", path: "/more", icon: "☰" },
];

export default function BottomNav() {
  return (
    <nav style={styles.nav}>
      {menuItems.map((item) => (
        <NavLink
          key={item.name}
          to={item.path}
          style={({ isActive }) => ({
            ...styles.link,
            color: isActive ? "#FACC15" : "#9CA3AF",
          })}
        >
          <span style={styles.icon}>{item.icon}</span>
          <span style={styles.label}>{item.name}</span>
        </NavLink>
      ))}
    </nav>
  );
}

const styles = {
  nav: {
    position: "fixed",
    left: 0,
    right: 0,
    bottom: 0,
    height: "70px",
    backgroundColor: "#111827",
    borderTop: "1px solid #1F2937",
    display: "flex",
    justifyContent: "space-around",
    alignItems: "center",
    zIndex: 1000,
  },

  link: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    textDecoration: "none",
    fontSize: "11px",
    fontWeight: "600",
    flex: 1,
  },

  icon: {
    fontSize: "22px",
    marginBottom: "4px",
  },

  label: {
    fontSize: "11px",
  },
};