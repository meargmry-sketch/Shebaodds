import React, { useEffect, useMemo, useState } from "react";
import {
  LayoutDashboard,
  Users,
  Trophy,
  Landmark,
  Receipt,
  FileBarChart2,
  Gift,
  Settings,
  ShieldAlert,
  FileText,
  Mail,
  Search,
  Bell,
  ChevronDown,
  RefreshCcw,
  Gamepad2,
  Activity,
} from "lucide-react";

export default function LiveUpcomingMatches({
  wsUrl = "ws://127.0.0.1:9090",
}) {
  // =========================================================
  // STATE
  // =========================================================

  const [activeTab, setActiveTab] = useState("dashboard");
  const [searchQuery, setSearchQuery] = useState("");

  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const [isFinanceExpanded, setIsFinanceExpanded] = useState(true);

  const [currentTime, setCurrentTime] = useState(new Date());

  const [systemSettings, setSystemSettings] = useState({
    taxRate: 10,
    welcomeBonus: 100,
    minBet: 10,
    maxBet: 50000,
    maxDailyLoss: 20000,
    oddsRefreshRate: 5,
  });

  // =========================================================
  // DATA
  // =========================================================

  const [matches, setMatches] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [liveBets, setLiveBets] = useState([]);
  const [users, setUsers] = useState([]);

  // =========================================================
  // CASINO STATE
  // =========================================================

  const [casinoBroadcasts, setCasinoBroadcasts] = useState([]);
  const [selectedGame, setSelectedGame] = useState(null);
  const [casinoBalance, setCasinoBalance] = useState(25000);
  const [casinoBetAmount, setCasinoBetAmount] = useState(10);
  const [casinoIsBetPanelOpen, setCasinoIsBetPanelOpen] = useState(false);
  const [casinoShowResultModal, setCasinoShowResultModal] = useState(false);
  const [casinoResultData, setCasinoResultData] = useState(null);
  const [casinoLoading, setCasinoLoading] = useState(false);
  const [casinoHistory, setCasinoHistory] = useState([]);

  const [casinoFavorites, setCasinoFavorites] = useState(() => {
    try {
      const saved = localStorage.getItem(
        "shebaodds_admin_favorite_games"
      );

      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // =========================================================
  // GAMES
  // =========================================================

  const GAMES = [
    {
      id: "dice",
      name: "Dice",
      nameAm: "ዳይስ",
      icon: "🎲",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "aviator",
      name: "Aviator",
      nameAm: "አቪዬተር",
      icon: "✈️",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "coinflip",
      name: "CoinFlip",
      nameAm: "ሳንቲም",
      icon: "🪙",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "plinko",
      name: "Plinko",
      nameAm: "ፕሊንኮ",
      icon: "📉",
      cat: "crash",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "blackjack",
      name: "Blackjack",
      nameAm: "ብላክጃክ",
      icon: "🃏",
      cat: "classic",
      minBet: 5,
      maxBet: 10000,
    },
    {
      id: "roulette",
      name: "Roulette",
      nameAm: "ሩሌት",
      icon: "🎡",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "mines",
      name: "Mines",
      nameAm: "ማይንስ",
      icon: "💣",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "crash",
      name: "Crash",
      nameAm: "ክራሽ",
      icon: "📈",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "tower",
      name: "Tower",
      nameAm: "ግንብ",
      icon: "🏗️",
      cat: "classic",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "keno",
      name: "Keno",
      nameAm: "ኬኖ",
      icon: "🔢",
      cat: "slots",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "baccarat",
      name: "Baccarat",
      nameAm: "ባካራት",
      icon: "♣️",
      cat: "table",
      minBet: 5,
      maxBet: 10000,
    },
    {
      id: "wheel",
      name: "Wheel of Fortune",
      nameAm: "የዕድል መንኮራኩር",
      icon: "🎰",
      cat: "table",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "hilo",
      name: "Hilo",
      nameAm: "ሂሎ",
      icon: "⬆️⬇️",
      cat: "classic",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "sicbo",
      name: "Sic Bo",
      nameAm: "ሲክቦ",
      icon: "🎲🎲🎲",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "videopoker",
      name: "Video Poker",
      nameAm: "ቪዲዮ ፖከር",
      icon: "🃏",
      cat: "classic",
      minBet: 5,
      maxBet: 10000,
    },
    {
      id: "bingo",
      name: "Bingo",
      nameAm: "ቢንጎ",
      icon: "🎯",
      cat: "slots",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "craps",
      name: "Craps",
      nameAm: "ክራፕስ",
      icon: "🎲",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "dragontiger",
      name: "Dragon Tiger",
      nameAm: "ድራጎን ታይገር",
      icon: "🐉🐯",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "andarbahar",
      name: "Andar Bahar",
      nameAm: "አንዳር ባሃር",
      icon: "🃏",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "teenpatti",
      name: "Teen Patti",
      nameAm: "ቲን ፓቲ",
      icon: "♠️",
      cat: "classic",
      minBet: 5,
      maxBet: 10000,
    },
    {
      id: "lucky7",
      name: "Lucky 7",
      nameAm: "ላኪ 7",
      icon: "🍀7️⃣",
      cat: "slots",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "scratch",
      name: "Scratch Card",
      nameAm: "ስክራች ካርድ",
      icon: "🎫",
      cat: "slots",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "football",
      name: "Football Prediction",
      nameAm: "እግር ኳስ ትንበያ",
      icon: "⚽",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "basketball",
      name: "Basketball Prediction",
      nameAm: "ቅርጫት ኳስ ትንበያ",
      icon: "🏀",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "horseracing",
      name: "Horse Racing",
      nameAm: "ፈረስ እሽቅድምድም",
      icon: "🐎",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "spinwin",
      name: "Spin & Win",
      nameAm: "ደብል አሸንፍ",
      icon: "🌀",
      cat: "special",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "slot",
      name: "Slot Machine",
      nameAm: "ስሎት ማሽን",
      icon: "🎰",
      cat: "slots",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "reddog",
      name: "Red Dog",
      nameAm: "ቀይ ውሻ",
      icon: "🐕",
      cat: "classic",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "war",
      name: "War",
      nameAm: "ጦርነት",
      icon: "⚔️",
      cat: "table",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "paigow",
      name: "Pai Gow Poker",
      nameAm: "ፓይ ጋው ፖከር",
      icon: "🀄",
      cat: "table",
      minBet: 5,
      maxBet: 10000,
    },
    {
      id: "diceduels",
      name: "Dice Duels",
      nameAm: "ዳይስ ዱኤልስ",
      icon: "⚔️🎲",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "penalty",
      name: "Penalty",
      nameAm: "ፍፃጎት ምት",
      icon: "⚽",
      cat: "sports",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "chickenroad",
      name: "Chicken Road",
      nameAm: "ዶሮ መንገድ",
      icon: "🐔",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "chickenshot",
      name: "Chicken Shot",
      nameAm: "ዶሮ ምት",
      icon: "🐔",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "megaball",
      name: "Mega Ball",
      nameAm: "ሜጋ ቦል",
      icon: "⚾",
      cat: "slots",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "pokerdice",
      name: "Poker Dice",
      nameAm: "ፖከር ዳይስ",
      icon: "🎲",
      cat: "classic",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "lightningdice",
      name: "Lightning Dice",
      nameAm: "መብረቅ ዳይስ",
      icon: "⚡🎲",
      cat: "crash",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "carroulette",
      name: "Car Roulette",
      nameAm: "መኪና ሩሌት",
      icon: "🚗",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "knockout",
      name: "Knock Out",
      nameAm: "ናክ አውት",
      icon: "🥊",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "rummy",
      name: "Rummy",
      nameAm: "ራሚ",
      icon: "🃏",
      cat: "classic",
      minBet: 5,
      maxBet: 10000,
    },
    {
      id: "darts",
      name: "Darts",
      nameAm: "ዳርትስ",
      icon: "🎯",
      cat: "special",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "tennis",
      name: "Tennis",
      nameAm: "ቴኒስ",
      icon: "🎾",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "baseball",
      name: "Baseball",
      nameAm: "ቤዝቦል",
      icon: "⚾",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "greyhound",
      name: "Greyhound Racing",
      nameAm: "ግሬይሀውንድ",
      icon: "🐕‍🦺",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "motorbike",
      name: "Motorbike Racing",
      nameAm: "ሞተር እሽቅድምድም",
      icon: "🏍️",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "cricket",
      name: "Cricket",
      nameAm: "ክሪኬት",
      icon: "🏏",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "roulette360",
      name: "Roulette 360",
      nameAm: "ሩሌት 360",
      icon: "🎡",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "megawheel",
      name: "Mega Wheel",
      nameAm: "ሜጋ መንኮራኩር",
      icon: "🎡",
      cat: "table",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "monopoly",
      name: "Monopoly",
      nameAm: "ሞኖፖሊ",
      icon: "🎩",
      cat: "table",
      minBet: 1,
      maxBet: 5000,
    },
    {
      id: "virtualsports",
      name: "Virtual Sports",
      nameAm: "ቨርቹዋል ስፖርት",
      icon: "🎮",
      cat: "sports",
      minBet: 1,
      maxBet: 10000,
    },
    {
      id: "texasholdem",
      name: "Texas Hold'em",
      nameAm: "ቴክሳስ ሆልደም",
      icon: "♠️",
      cat: "classic",
      minBet: 5,
      maxBet: 10000,
    },
  ];

  // =========================================================
  // GAME LOGIC
  // =========================================================

  const gameLogic = {
    dice: (bet) => {
      const player = Math.floor(Math.random() * 6) + 1;
      const house = Math.floor(Math.random() * 6) + 1;
      const win = player > house;

      return {
        result: win ? "win" : "lose",
        profit: win ? bet : -bet,
        details: {
          playerRoll: player,
          houseRoll: house,
        },
      };
    },

    coinflip: (bet, params) => {
      const result = Math.random() < 0.5 ? "heads" : "tails";
      const win = params.side === result;

      return {
        result: win ? "win" : "lose",
        profit: win ? bet * 1.9 : -bet,
        details: {
          result,
          side: params.side,
        },
      };
    },

    roulette: (bet, params) => {
      const number = Math.floor(Math.random() * 37);

      const redNumbers = [
        1, 3, 5, 7, 9, 12, 14, 16, 18,
        19, 21, 23, 25, 27, 30, 32, 34, 36,
      ];

      const isRed = redNumbers.includes(number);
      const isEven = number > 0 && number % 2 === 0;

      let win = false;
      let multiplier = 0;

      if (params.bet === "red" && isRed) {
        win = true;
        multiplier = 1.9;
      } else if (
        params.bet === "black" &&
        !isRed &&
        number !== 0
      ) {
        win = true;
        multiplier = 1.9;
      } else if (params.bet === "even" && isEven) {
        win = true;
        multiplier = 1.9;
      } else if (
        params.bet === "odd" &&
        !isEven &&
        number !== 0
      ) {
        win = true;
        multiplier = 1.9;
      }

      const profit = win ? bet * multiplier : -bet;

      return {
        result: win ? "win" : "lose",
        profit: Math.round(profit * 100) / 100,
        details: {
          number,
          isRed,
          isEven,
        },
      };
    },

    slot: (bet) => {
      const symbols = ["🍒", "🍋", "🍊", "🔔", "💎", "7️⃣"];

      const reels = [
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)],
      ];

      let win = false;
      let multiplier = 0;

      if (
        reels[0] === reels[1] &&
        reels[1] === reels[2]
      ) {
        win = true;
        multiplier = 5;
      } else if (
        reels[0] === reels[1] ||
        reels[1] === reels[2] ||
        reels[0] === reels[2]
      ) {
        win = true;
        multiplier = 0.5;
      }

      const profit = win ? bet * multiplier : -bet;

      return {
        result: win ? "win" : "lose",
        profit: Math.round(profit * 100) / 100,
        details: {
          reels,
          multiplier,
        },
      };
    },

    blackjack: (bet) => {
      const card = () => {
        const value = Math.floor(Math.random() * 13) + 1;

        return {
          value: Math.min(value, 10),
          display: value,
        };
      };

      const playerCards = [card(), card()];
      const dealerCards = [card(), card()];

      const score = (cards) => {
        let total = cards.reduce(
          (sum, current) => sum + current.value,
          0
        );

        const aces = cards.filter(
          (current) => current.display === 1
        ).length;

        let aceUpgrades = 0;

        while (
          total <= 11 &&
          aceUpgrades < aces
        ) {
          total += 10;
          aceUpgrades++;
        }

        return total;
      };

      const playerScore = score(playerCards);
      const dealerScore = score(dealerCards);

      let result = "lose";
      let profit = -bet;

      if (
        playerScore === 21 &&
        playerCards.length === 2
      ) {
        result = "win";
        profit = bet * 2.5;
      } else if (playerScore > 21) {
        result = "lose";
        profit = -bet;
      } else if (dealerScore > 21) {
        result = "win";
        profit = bet;
      } else if (playerScore > dealerScore) {
        result = "win";
        profit = bet;
      } else if (playerScore === dealerScore) {
        result = "push";
        profit = 0;
      }

      return {
        result,
        profit: Math.round(profit * 100) / 100,
        details: {
          playerCards: playerCards.map(
            (card) => card.display
          ),
          dealerCards: dealerCards.map(
            (card) => card.display
          ),
          playerScore,
          dealerScore,
        },
      };
    },

    aviator: (bet, params) => {
      const crashPoint = 1 + Math.random() * 9;

      if (params.action !== "cashout") {
        return {
          result: "lose",
          profit: -bet,
          details: {
            crashPoint,
          },
        };
      }

      const cashout = Math.min(
        1 + Math.random() * 5,
        crashPoint
      );

      const win = cashout < crashPoint;

      return {
        result: win ? "win" : "lose",
        profit: win ? bet * cashout : -bet,
        details: {
          crashPoint,
          multiplier: win ? cashout : 0,
        },
      };
    },

    mines: (bet, params) => {
      const gridSize = 25;
      const mineCount = params.mines || 3;
      const mines = [];

      while (mines.length < mineCount) {
        const position = Math.floor(
          Math.random() * gridSize
        );

        if (!mines.includes(position)) {
          mines.push(position);
        }
      }

      const tile =
        params.tile ??
        Math.floor(Math.random() * gridSize);

      const hit = mines.includes(tile);

      return {
        result: hit ? "lose" : "win",
        profit: hit ? -bet : bet * 1.2,
        details: {
          mines,
          tile,
          hit,
        },
      };
    },

    crash: (bet, params) => {
      const crashPoint = 1 + Math.random() * 9;

      if (params.action !== "cashout") {
        return {
          result: "lose",
          profit: -bet,
          details: {
            crashPoint,
          },
        };
      }

      const cashout = Math.min(
        1 + Math.random() * 5,
        crashPoint
      );

      const win = cashout < crashPoint;

      return {
        result: win ? "win" : "lose",
        profit: win ? bet * cashout : -bet,
        details: {
          crashPoint,
          multiplier: win ? cashout : 0,
        },
      };
    },

    default: (bet) => {
      const win = Math.random() < 0.45;

      return {
        result: win ? "win" : "lose",
        profit: win ? bet * 1.9 : -bet,
        details: {},
      };
    },
  };

  // =========================================================
  // PLAY CASINO GAME
  // =========================================================

  const playCasinoGame = (gameId, params = {}) => {
    const game = GAMES.find(
      (item) => item.id === gameId
    );

    if (!game) return;

    if (casinoBetAmount < game.minBet) {
      alert(`Minimum bet is ${game.minBet} ETB`);
      return;
    }

    if (casinoBetAmount > game.maxBet) {
      alert(`Maximum bet is ${game.maxBet} ETB`);
      return;
    }

    if (casinoBetAmount > casinoBalance) {
      alert("Insufficient balance.");
      return;
    }

    setCasinoLoading(true);

    try {
      const logic =
        gameLogic[game.id] || gameLogic.default;

      const result = logic(
        casinoBetAmount,
        params
      );

      const newBalance =
        casinoBalance + result.profit;

      setCasinoBalance(newBalance);
      setCasinoResultData(result);
      setCasinoShowResultModal(true);

      setCasinoHistory((previous) => [
        {
          gameId,
          bet: casinoBetAmount,
          result: result.result,
          profit: result.profit,
          details: result.details,
          timestamp: new Date(),
        },
        ...previous,
      ].slice(0, 50));

      setCasinoBroadcasts((previous) => [
        {
          id: `CAS-${Date.now()}`,
          user: "SuperAdmin",
          game: game.name,
          stake: casinoBetAmount,
          profit: result.profit,
          outcome: result.result,
          time: currentTime.toLocaleTimeString(),
        },
        ...previous,
      ].slice(0, 20));
    } finally {
      setCasinoLoading(false);
    }
  };

  // =========================================================
  // FAVORITES
  // =========================================================

  const toggleCasinoFavorite = (gameId) => {
    setCasinoFavorites((previous) => {
      const next = previous.includes(gameId)
        ? previous.filter((id) => id !== gameId)
        : [...previous, gameId];

      localStorage.setItem(
        "shebaodds_admin_favorite_games",
        JSON.stringify(next)
      );

      return next;
    });
  };

  // =========================================================
  // CASINO CARD
  // =========================================================

  const renderCasinoGameCard = (game) => (
    <div
      key={game.id}
      className={`game-card ${
        selectedGame?.id === game.id
          ? "active"
          : ""
      }`}
      onClick={() => {
        setSelectedGame(game);
        setCasinoIsBetPanelOpen(true);
      }}
    >
      {["slot", "megaball", "lucky7"].includes(
        game.id
      ) && (
        <span className="badge hot">
          HOT
        </span>
      )}

      <button
        className={`favorite-btn ${
          casinoFavorites.includes(game.id)
            ? "active"
            : ""
        }`}
        onClick={(event) => {
          event.stopPropagation();
          toggleCasinoFavorite(game.id);
        }}
      >
        {casinoFavorites.includes(game.id)
          ? "★"
          : "☆"}
      </button>

      <span className="game-icon">
        {game.icon}
      </span>

      <span className="game-name">
        {game.name}
      </span>

      <span className="game-min-bet">
        {game.minBet} ETB
      </span>
    </div>
  );

  // =========================================================
  // CASINO GRID
  // =========================================================

  const renderCasinoGamesGrid = () => {
    const categories = [
      "crash",
      "classic",
      "table",
      "slots",
      "sports",
      "special",
    ];

    const categoryLabels = {
      crash: "💥 Crash",
      classic: "🃏 Classic",
      table: "🪑 Table",
      slots: "🎰 Slots",
      sports: "🏅 Sports",
      special: "✨ Special",
    };

    const favoriteGames = GAMES.filter(
      (game) =>
        casinoFavorites.includes(game.id)
    );

    const otherGames = GAMES.filter(
      (game) =>
        !casinoFavorites.includes(game.id)
    );

    return (
      <>
        {favoriteGames.length > 0 && (
          <div className="game-category">
            <h3 className="category-title">
              ⭐ Favorites
            </h3>

            <div className="game-grid">
              {favoriteGames.map(
                renderCasinoGameCard
              )}
            </div>
          </div>
        )}

        {categories.map((category) => {
          const gamesInCategory =
            otherGames.filter(
              (game) => game.cat === category
            );

          if (!gamesInCategory.length) {
            return null;
          }

          return (
            <div
              key={category}
              className="game-category"
            >
              <h3 className="category-title">
                {categoryLabels[category]}
                <small>
                  {gamesInCategory.length} games
                </small>
              </h3>

              <div className="game-grid">
                {gamesInCategory.map(
                  renderCasinoGameCard
                )}
              </div>
            </div>
          );
        })}
      </>
    );
  };

  // =========================================================
  // GAME UI
  // =========================================================

  const renderCasinoGameSpecificUI = (
    gameId
  ) => {
    switch (gameId) {
      case "dice":
        return (
          <div className="game-specific">
            <div className="game-symbols">
              🎲
              <span>VS</span>
              🎲
            </div>

            <button
              className="btn-play"
              onClick={() =>
                playCasinoGame("dice")
              }
              disabled={casinoLoading}
            >
              {casinoLoading
                ? "🎲 Rolling..."
                : "🎲 Roll Dice"}
            </button>
          </div>
        );

      case "coinflip":
        return (
          <div className="game-specific">
            <div className="coin-display">
              🪙
            </div>

            <div className="game-controls">
              <button
                className="btn-bet"
                onClick={() =>
                  playCasinoGame(
                    "coinflip",
                    { side: "heads" }
                  )
                }
                disabled={casinoLoading}
              >
                Heads
              </button>

              <button
                className="btn-bet"
                onClick={() =>
                  playCasinoGame(
                    "coinflip",
                    { side: "tails" }
                  )
                }
                disabled={casinoLoading}
              >
                Tails
              </button>
            </div>
          </div>
        );

      case "slot":
        return (
          <div className="game-specific">
            <div className="slot-reels">
              🍒 🍒 🍒
            </div>

            <button
              className="btn-play"
              onClick={() =>
                playCasinoGame("slot")
              }
              disabled={casinoLoading}
            >
              {casinoLoading
                ? "🔄 Spinning..."
                : "🎰 Spin"}
            </button>
          </div>
        );

      case "aviator":
        return (
          <div className="game-specific">
            <div className="aviator-multiplier">
              1.00x
            </div>

            <div className="game-controls">
              <button
                className="btn-bet"
                onClick={() =>
                  playCasinoGame("aviator", {
                    action: "bet",
                  })
                }
                disabled={casinoLoading}
              >
                ✈️ Place Bet
              </button>

              <button
                className="btn-cashout"
                onClick={() =>
                  playCasinoGame("aviator", {
                    action: "cashout",
                  })
                }
                disabled={casinoLoading}
              >
                💰 Cash Out
              </button>
            </div>
          </div>
        );

      default:
        return (
          <div className="game-specific">
            <div className="default-game-icon">
              {GAMES.find(
                (game) => game.id === gameId
              )?.icon || "🎮"}
            </div>

            <button
              className="btn-play"
              onClick={() =>
                playCasinoGame(gameId)
              }
              disabled={casinoLoading}
            >
              {casinoLoading
                ? "⏳ Playing..."
                : "▶️ Play Now"}
            </button>
          </div>
        );
    }
  };

  // =========================================================
  // STATS
  // =========================================================

  const stats = useMemo(() => {
    const totalUsers =
      12458 + users.length;

    const totalBalance =
      1257850 +
      users.reduce(
        (total, user) =>
          total + Number(user.balance || 0),
        0
      );

    const totalBets =
      8564 + liveBets.length;

    const totalDeposits =
      523600 +
      transactions
        .filter(
          (transaction) =>
            transaction.type === "Deposit" &&
            transaction.status === "Approved"
        )
        .reduce(
          (total, transaction) =>
            total +
            Number(transaction.amount || 0),
          0
        );

    const totalWithdrawals =
      186250 +
      transactions
        .filter(
          (transaction) =>
            transaction.type === "Withdrawal" &&
            transaction.status === "Approved"
        )
        .reduce(
          (total, transaction) =>
            total +
            Number(transaction.amount || 0),
          0
        );

    const profitToday =
      125750 +
      liveBets.reduce(
        (total, bet) =>
          total + Number(bet.stake || 0),
        0
      ) *
        0.15;

    return {
      users: totalUsers,
      balance: totalBalance,
      betsToday: totalBets,
      deposits: totalDeposits,
      withdrawals: totalWithdrawals,
      profitToday,
    };
  }, [users, transactions, liveBets]);

  // =========================================================
  // MODALS / FORMS
  // =========================================================

  const [modals, setModals] = useState({
    user: false,
    match: false,
    deposit: false,
    withdrawal: false,
    settings: false,
    betSlip: false,
  });

  const [newUserForm, setNewUserForm] =
    useState({
      id: "",
      email: "",
      role: "Player",
      balance: 100,
    });

  const [newMatchForm, setNewMatchForm] =
    useState({
      home: "",
      away: "",
      sport: "Football",
      date: "Today, 22:00",
      odds1: 1.9,
      oddsX: 3.2,
      odds2: 3.1,
    });

  const [newDepositForm, setNewDepositForm] =
    useState({
      user: "User1234",
      amount: 500,
      method: "TeleBirr",
    });

  const [newWithdrawForm, setNewWithdrawForm] =
    useState({
      user: "User1234",
      amount: 500,
      method: "TeleBirr",
    });

  // =========================================================
  // WEBSOCKET + SIMULATION
  // =========================================================

  useEffect(() => {
    let ws = null;
    let simulationInterval = null;

    try {
      ws = new WebSocket(wsUrl);

      ws.onmessage = (event) => {
        try {
          const payload =
            JSON.parse(event.data);

          if (!payload?.eventId) {
            return;
          }

          setMatches((previous) =>
            previous.map((match) => {
              if (
                match.id !== payload.eventId
              ) {
                return match;
              }

              const updatedOdds = {
                ...(match.odds || {}),
              };

              if (
                payload.markets &&
                Array.isArray(payload.markets)
              ) {
                const market =
                  payload.markets.find(
                    (item) =>
                      item.marketId === "1X2"
                  );

                if (
                  market?.odds &&
                  Array.isArray(market.odds)
                ) {
                  market.odds.forEach(
                    (item) => {
                      updatedOdds[item.outcome] =
                        Number(item.price);
                    }
                  );
                }
              }

              return {
                ...match,
                score: payload.score
                  ? {
                      home:
                        payload.score.home,
                      away:
                        payload.score.away,
                      elapsed:
                        payload.score.elapsed,
                    }
                  : match.score,
                odds: {
                  ...(match.odds || {}),
                  ...updatedOdds,
                },
                status:
                  payload.status ||
                  match.status,
              };
            })
          );
        } catch {
          // Ignore malformed WebSocket messages
        }
      };

      ws.onerror = () => {
        // WebSocket is optional
      };
    } catch {
      ws = null;
    }

    simulationInterval = setInterval(() => {
      setMatches((previous) =>
        previous.map((match) => {
          if (
            match.status !== "Live" ||
            !match.odds
          ) {
            return match;
          }

          if (Math.random() <= 0.6) {
            return match;
          }

          const keys = ["1", "X", "2"];

          const key =
            keys[
              Math.floor(
                Math.random() * keys.length
              )
            ];

          const oldValue =
            Number(match.odds[key] || 1.5);

          const change =
            Math.random() * 0.3 - 0.15;

          const newValue = Math.max(
            1.1,
            Number(
              (oldValue + change).toFixed(2)
            )
          );

          return {
            ...match,
            odds: {
              ...match.odds,
              [key]: newValue,
            },
          };
        })
      );
    }, 4000);

    return () => {
      if (ws) {
        try {
          ws.close();
        } catch {
          // Ignore close error
        }
      }

      if (simulationInterval) {
        clearInterval(simulationInterval);
      }
    };
  }, [wsUrl]);

  // =========================================================
  // CLOCK
  // =========================================================

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  // =========================================================
  // FILTERS
  // =========================================================

  const filteredMatches = useMemo(() => {
    const query =
      searchQuery.toLowerCase();

    return matches.filter((match) =>
      [
        match.homeTeam,
        match.awayTeam,
        match.sport,
      ].some((value) =>
        String(value || "")
          .toLowerCase()
          .includes(query)
      )
    );
  }, [matches, searchQuery]);

  const filteredTransactions = useMemo(() => {
    const query =
      searchQuery.toLowerCase();

    return transactions.filter(
      (transaction) =>
        String(transaction.user || "")
          .toLowerCase()
          .includes(query) ||
        String(transaction.method || "")
          .toLowerCase()
          .includes(query) ||
        String(transaction.status || "")
          .toLowerCase()
          .includes(query)
    );
  }, [transactions, searchQuery]);

  const filteredUsers = useMemo(() => {
    const query =
      searchQuery.toLowerCase();

    return users.filter(
      (user) =>
        String(user.id || "")
          .toLowerCase()
          .includes(query) ||
        String(user.email || "")
          .toLowerCase()
          .includes(query) ||
        String(user.role || "")
          .toLowerCase()
          .includes(query)
    );
  }, [users, searchQuery]);

  const filteredLiveBets = useMemo(() => {
    const query =
      searchQuery.toLowerCase();

    return liveBets.filter(
      (bet) =>
        String(bet.user || "")
          .toLowerCase()
          .includes(query) ||
        String(bet.match || "")
          .toLowerCase()
          .includes(query)
    );
  }, [liveBets, searchQuery]);

  // =========================================================
  // ACTIONS
  // =========================================================

  const handleApproveTransaction = (
    transactionId
  ) => {
    setTransactions((previous) =>
      previous.map((transaction) => {
        if (
          transaction.id !==
          transactionId
        ) {
          return transaction;
        }

        const targetUser =
          users.find(
            (user) =>
              user.id === transaction.user
          );

        if (targetUser) {
          setUsers((previousUsers) =>
            previousUsers.map((user) =>
              user.id ===
              transaction.user
                ? {
                    ...user,
                    balance:
                      user.balance +
                      (transaction.type ===
                      "Deposit"
                        ? transaction.amount
                        : -transaction.amount),
                  }
                : user
            )
          );
        }

        return {
          ...transaction,
          status: "Approved",
        };
      })
    );
  };

  const handleRejectTransaction = (
    transactionId
  ) => {
    setTransactions((previous) =>
      previous.map((transaction) =>
        transaction.id === transactionId
          ? {
              ...transaction,
              status: "Rejected",
            }
          : transaction
      )
    );
  };

  const handleDeleteMatch = (matchId) => {
    setMatches((previous) =>
      previous.filter(
        (match) => match.id !== matchId
      )
    );
  };

  const handleSettleBet = (
    betId,
    outcome
  ) => {
    const bet = liveBets.find(
      (item) => item.id === betId
    );

    setLiveBets((previous) =>
      previous.filter(
        (item) => item.id !== betId
      )
    );

    if (
      bet &&
      outcome === "Won"
    ) {
      const transactionId = `#TRX${Math.floor(
        1000 + Math.random() * 9000
      )}`;

      setTransactions((previous) => [
        {
          id: transactionId,
          user: bet.user,
          type: "Deposit",
          amount: bet.possibleWin,
          method: "Winnings Settle",
          status: "Approved",
          time: currentTime.toLocaleTimeString(
            [],
            {
              hour: "2-digit",
              minute: "2-digit",
            }
          ),
        },
        ...previous,
      ]);
    }
  };

  const handleAddUser = (event) => {
    event.preventDefault();

    if (
      !newUserForm.id ||
      !newUserForm.email
    ) {
      return;
    }

    setUsers((previous) => [
      ...previous,
      {
        id: newUserForm.id,
        email: newUserForm.email,
        role: newUserForm.role,
        balance: Number(
          newUserForm.balance
        ),
        joined: new Date()
          .toISOString()
          .split("T")[0],
      },
    ]);

    setNewUserForm({
      id: "",
      email: "",
      role: "Player",
      balance: 100,
    });

    setModals((previous) => ({
      ...previous,
      user: false,
    }));
  };

  const handleAddMatch = (event) => {
    event.preventDefault();

    if (
      !newMatchForm.home ||
      !newMatchForm.away
    ) {
      return;
    }

    const matchId = `sr:match:${
      100 + matches.length + 1
    }`;

    setMatches((previous) => [
      ...previous,
      {
        id: matchId,
        sport: newMatchForm.sport,
        homeTeam: newMatchForm.home,
        awayTeam: newMatchForm.away,
        status: "Upcoming",
        startTime: newMatchForm.date,
        odds: {
          "1": Number(
            newMatchForm.odds1
          ),
          X: Number(
            newMatchForm.oddsX
          ),
          "2": Number(
            newMatchForm.odds2
          ),
        },
        oddsUp: {},
        oddsDown: {},
      },
    ]);

    setNewMatchForm({
      home: "",
      away: "",
      sport: "Football",
      date: "Today, 22:00",
      odds1: 1.9,
      oddsX: 3.2,
      odds2: 3.1,
    });

    setModals((previous) => ({
      ...previous,
      match: false,
    }));
  };

  const handleDepositRequest = (event) => {
    event.preventDefault();

    const transactionId = `#TRX${Math.floor(
      1000 + Math.random() * 9000
    )}`;

    setTransactions((previous) => [
      {
        id: transactionId,
        user: newDepositForm.user,
        type: "Deposit",
        amount: Number(
          newDepositForm.amount
        ),
        method: newDepositForm.method,
        status: "Pending",
        time: currentTime.toLocaleTimeString(
          [],
          {
            hour: "2-digit",
            minute: "2-digit",
          }
        ),
      },
      ...previous,
    ]);

    setModals((previous) => ({
      ...previous,
      deposit: false,
    }));
  };

  const handleWithdrawRequest = (
    event
  ) => {
    event.preventDefault();

    const transactionId = `#TRX${Math.floor(
      1000 + Math.random() * 9000
    )}`;

    setTransactions((previous) => [
      {
        id: transactionId,
        user: newWithdrawForm.user,
        type: "Withdrawal",
        amount: Number(
          newWithdrawForm.amount
        ),
        method: newWithdrawForm.method,
        status: "Pending",
        time: currentTime.toLocaleTimeString(
          [],
          {
            hour: "2-digit",
            minute: "2-digit",
          }
        ),
      },
      ...previous,
    ]);

    setModals((previous) => ({
      ...previous,
      withdrawal: false,
    }));
  };

  // =========================================================
  // RENDER
  // =========================================================

  return (
    <div className="min-h-screen bg-[#090D16] text-slate-100 font-sans antialiased flex">

      {/* =====================================================
          SIDEBAR
      ===================================================== */}

      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 bg-[#111625]/95 border-r border-slate-800 w-64 transition-transform duration-300 ${
          isSidebarOpen
            ? "translate-x-0"
            : "-translate-x-full"
        } lg:translate-x-0 flex flex-col justify-between`}
      >
        <div className="flex-1 overflow-y-auto py-5 px-4">

          {/* LOGO */}

          <div className="flex items-center gap-3 px-3 mb-8">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-[#EAB308] to-amber-600 flex items-center justify-center font-black text-slate-950 text-xl">
              SO
            </div>

            <div>
              <span className="text-lg font-black tracking-wider text-white">
                SHEBA
                <span className="text-amber-400">
                  ODDS
                </span>
              </span>

              <p className="text-[10px] uppercase font-bold text-slate-500 tracking-widest">
                Admin Panel
              </p>
            </div>
          </div>

          {/* NAVIGATION */}

          <nav className="space-y-1.5">

            <button
              onClick={() =>
                setActiveTab("dashboard")
              }
              className={`nav-button ${
                activeTab === "dashboard"
                  ? "active"
                  : ""
              }`}
            >
              <LayoutDashboard size={18} />
              <span>Dashboard</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("users")
              }
              className={`nav-button ${
                activeTab === "users"
                  ? "active"
                  : ""
              }`}
            >
              <Users size={18} />
              <span>Users</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("matches")
              }
              className={`nav-button ${
                activeTab === "matches"
                  ? "active"
                  : ""
              }`}
            >
              <Trophy size={18} />
              <span>Matches & Odds</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("bets")
              }
              className={`nav-button ${
                activeTab === "bets"
                  ? "active"
                  : ""
              }`}
            >
              <Receipt size={18} />
              <span>Bet Management</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("casino")
              }
              className={`nav-button ${
                activeTab === "casino"
                  ? "active"
                  : ""
              }`}
            >
              <Gamepad2 size={18} />
              <span>🎰 Casino</span>
            </button>

            {/* FINANCE */}

            <div>
              <button
                onClick={() =>
                  setIsFinanceExpanded(
                    (previous) =>
                      !previous
                  )
                }
                className="nav-button"
              >
                <div className="flex items-center gap-3.5">
                  <Landmark size={18} />
                  <span>Finance</span>
                </div>

                <ChevronDown
                  size={15}
                  className={`transition-transform ${
                    isFinanceExpanded
                      ? "rotate-180"
                      : ""
                  }`}
                />
              </button>

              {isFinanceExpanded && (
                <div className="pl-11 mt-1 space-y-1">

                  <button
                    onClick={() =>
                      setActiveTab("deposits")
                    }
                    className={`sub-nav ${
                      activeTab ===
                      "deposits"
                        ? "selected"
                        : ""
                    }`}
                  >
                    Deposits
                  </button>

                  <button
                    onClick={() =>
                      setActiveTab(
                        "withdrawals"
                      )
                    }
                    className={`sub-nav ${
                      activeTab ===
                      "withdrawals"
                        ? "selected"
                        : ""
                    }`}
                  >
                    Withdrawals
                  </button>

                  <button
                    onClick={() =>
                      setActiveTab(
                        "transactions"
                      )
                    }
                    className={`sub-nav ${
                      activeTab ===
                      "transactions"
                        ? "selected"
                        : ""
                    }`}
                  >
                    All Transactions
                  </button>

                </div>
              )}

            </div>

            {/* IMPORTANT:
                Finance wrapper is now correctly closed above.
            */}

            <button
              onClick={() =>
                setActiveTab("reports")
              }
              className={`nav-button ${
                activeTab === "reports"
                  ? "active"
                  : ""
              }`}
            >
              <FileBarChart2 size={18} />
              <span>Reports</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("bonuses")
              }
              className={`nav-button ${
                activeTab === "bonuses"
                  ? "active"
                  : ""
              }`}
            >
              <Gift size={18} />
              <span>Bonus Management</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("settings")
              }
              className={`nav-button ${
                activeTab === "settings"
                  ? "active"
                  : ""
              }`}
            >
              <Settings size={18} />
              <span>System Settings</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("admins")
              }
              className={`nav-button ${
                activeTab === "admins"
                  ? "active"
                  : ""
              }`}
            >
              <ShieldAlert size={18} />
              <span>Admin Management</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("logs")
              }
              className={`nav-button ${
                activeTab === "logs"
                  ? "active"
                  : ""
              }`}
            >
              <FileText size={18} />
              <span>Logs & Activity</span>
            </button>

            <button
              onClick={() =>
                setActiveTab("support")
              }
              className={`nav-button ${
                activeTab === "support"
                  ? "active"
                  : ""
              }`}
            >
              <Mail size={18} />
              <span>Support Messages</span>
            </button>

          </nav>
        </div>

        {/* SIDEBAR FOOTER */}

        <div className="p-4 border-t border-slate-800 bg-[#0F1321] flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-9 w-9 rounded-full bg-amber-500/20 border border-amber-400/30 flex items-center justify-center font-bold text-amber-400">
              SA
            </div>

            <div>
              <p className="text-xs font-bold text-white">
                Super Admin
              </p>

              <span className="text-[9px] text-emerald-400">
                ● Online
              </span>
            </div>
          </div>

          <button
            onClick={() =>
              alert(
                "Administrative settings profile launched."
              )
            }
            className="p-1.5 text-slate-400 hover:text-white"
          >
            <ChevronDown size={16} />
          </button>
        </div>
      </aside>

      {/* =====================================================
          MAIN
      ===================================================== */}

      <div className="flex-1 lg:pl-64 flex flex-col min-h-screen">

        {/* HEADER */}

        <header className="sticky top-0 z-30 h-16 flex items-center justify-between border-b border-slate-800 bg-[#111625]/95 px-4 sm:px-6 lg:px-8 backdrop-blur-md">

          <div className="flex items-center gap-4 flex-1">

            <button
              onClick={() =>
                setIsSidebarOpen(
                  (previous) =>
                    !previous
                )
              }
              className="lg:hidden p-2 text-slate-400"
            >
              <Activity size={20} />
            </button>

            <div className="relative w-full max-w-md hidden sm:block">
              <Search
                size={16}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
              />

              <input
                type="text"
                value={searchQuery}
                onChange={(event) =>
                  setSearchQuery(
                    event.target.value
                  )
                }
                placeholder="Search users, matches, transactions..."
                className="w-full bg-[#090D16] border border-slate-800 rounded-xl py-2 pl-9 pr-4 text-xs text-slate-300 focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="flex items-center gap-4">

            <div className="hidden md:flex items-center gap-3 text-slate-400 text-xs bg-[#151C2E] px-3 py-2 rounded-xl border border-slate-800">
              <span>
                {currentTime.toLocaleDateString(
                  [],
                  {
                    month: "short",
                    day: "numeric",
                    year: "numeric",
                  }
                )}
              </span>

              <span>|</span>

              <span className="font-mono text-amber-300 font-bold">
                {currentTime.toLocaleTimeString()}
              </span>
            </div>

            {/* NOTIFICATIONS */}

            <div className="relative">

              <button
                onClick={() =>
                  setIsNotificationsOpen(
                    (previous) =>
                      !previous
                  )
                }
                className="p-2 bg-[#151C2E] text-slate-300 rounded-xl border border-slate-800 relative"
              >
                <Bell size={18} />

                <span className="absolute -top-1 -right-1 h-4 w-4 bg-red-600 text-[9px] font-black text-white rounded-full flex items-center justify-center">
                  8
                </span>
              </button>

              {isNotificationsOpen && (
                <div className="absolute right-0 mt-2 w-80 bg-[#111625] border border-slate-800 rounded-xl shadow-2xl p-4 z-50">

                  <div className="flex justify-between border-b border-slate-800 pb-2 mb-3">
                    <span className="text-xs font-bold text-white">
                      System Notifications
                    </span>

                    <button
                      onClick={() =>
                        setIsNotificationsOpen(
                          false
                        )
                      }
                      className="text-xs text-amber-400"
                    >
                      Close
                    </button>
                  </div>

                  <div className="space-y-2">

                    <div className="p-2 bg-[#1C1F2E] rounded-lg">
                      <p className="font-semibold text-amber-300">
                        New Deposit Request
                      </p>

                      <p className="text-[10px] text-slate-400">
                        User1234 initiated
                        5,000 ETB deposit.
                      </p>
                    </div>

                    <div className="p-2 bg-[#1C1F2E] rounded-lg">
                      <p className="font-semibold text-rose-400">
                        Large Withdrawal Alert
                      </p>

                      <p className="text-[10px] text-slate-400">
                        User5678 requested
                        12,500 ETB.
                      </p>
                    </div>

                  </div>
                </div>
              )}
            </div>

            {/* PROFILE */}

            <div className="relative">

              <button
                onClick={() =>
                  setIsProfileOpen(
                    (previous) =>
                      !previous
                  )
                }
                className="flex items-center gap-2 bg-[#151C2E] border border-slate-800 p-1.5 pr-3 rounded-xl"
              >
                <div className="h-7 w-7 rounded-lg bg-amber-500 flex items-center justify-center font-black text-slate-950 text-xs">
                  SA
                </div>

                <div className="hidden sm:block text-left">
                  <p className="text-[11px] font-bold text-white">
                    Super Admin
                  </p>

                  <p className="text-[9px] text-slate-500">
                    Administrator
                  </p>
                </div>

                <ChevronDown
                  size={14}
                  className="text-slate-500"
                />
              </button>

              {isProfileOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-[#111625] border border-slate-800 rounded-xl shadow-2xl p-2 z-50">

                  <button
                    className="w-full text-left px-3 py-2 text-slate-300 hover:bg-slate-800 rounded-lg"
                    onClick={() =>
                      alert(
                        "Profile opened."
                      )
                    }
                  >
                    My Profile
                  </button>

                  <button
                    className="w-full text-left px-3 py-2 text-slate-300 hover:bg-slate-800 rounded-lg"
                    onClick={() =>
                      setActiveTab("logs")
                    }
                  >
                    Activity Logs
                  </button>

                  <hr className="border-slate-800 my-1" />

                  <button
                    onClick={() =>
                      alert(
                        "Log out successfully."
                      )
                    }
                    className="w-full text-left px-3 py-2 text-rose-400 hover:bg-rose-500/10 rounded-lg"
                  >
                    Sign Out
                  </button>

                </div>
              )}
            </div>

          </div>
        </header>

        {/* ===================================================
            CONTENT
        =================================================== */}

        <main className="flex-1 p-4 sm:p-6 lg:p-8">

          {/* MOBILE SEARCH */}

          <div className="relative w-full sm:hidden mb-6">
            <Search
              size={16}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
            />

            <input
              type="text"
              value={searchQuery}
              onChange={(event) =>
                setSearchQuery(
                  event.target.value
                )
              }
              placeholder="Search..."
              className="w-full bg-[#111625] border border-slate-800 rounded-xl py-2 pl-9 pr-4 text-xs text-slate-300"
            />
          </div>

          {/* =================================================
              SIMPLE PAGE VIEWS
          ================================================= */}

          {activeTab === "dashboard" && (
            <PageCard
              title="Dashboard"
              description="Welcome to the ShebaOdds administration dashboard."
            />
          )}

          {activeTab === "users" && (
            <PageCard
              title="Users"
              description={`${filteredUsers.length} users available.`}
            />
          )}

          {activeTab === "matches" && (
            <PageCard
              title="Matches & Odds"
              description={`${filteredMatches.length} matches available.`}
            />
          )}

          {activeTab === "bets" && (
            <PageCard
              title="Bet Management"
              description={`${filteredLiveBets.length} live bets available.`}
            />
          )}

          {activeTab === "deposits" && (
            <PageCard
              title="Deposits"
              description="Manage deposit requests."
            />
          )}

          {activeTab === "withdrawals" && (
            <PageCard
              title="Withdrawals"
              description="Manage withdrawal requests."
            />
          )}

          {activeTab === "transactions" && (
            <PageCard
              title="All Transactions"
              description={`${filteredTransactions.length} transactions available.`}
            />
          )}

          {activeTab === "reports" && (
            <PageCard
              title="Reports"
              description="Financial and betting reports."
            />
          )}

          {activeTab === "bonuses" && (
            <PageCard
              title="Bonus Management"
              description="Manage bonuses and promotions."
            />
          )}

          {activeTab === "settings" && (
            <PageCard
              title="System Settings"
              description={`Tax rate: ${systemSettings.taxRate}%`}
            />
          )}

          {activeTab === "admins" && (
            <PageCard
              title="Admin Management"
              description="Manage administrator accounts."
            />
          )}

          {activeTab === "logs" && (
            <PageCard
              title="Logs & Activity"
              description="Review system activity."
            />
          )}

          {activeTab === "support" && (
            <PageCard
              title="Support Messages"
              description="Manage support tickets."
            />
          )}

          {/* =================================================
              CASINO
          ================================================= */}

          {activeTab === "casino" && (
            <div className="casino-games-page">

              {/* CASINO HEADER */}

              <div className="casino-header">

                <div>
                  <h1>
                    🎰 Casino Games
                  </h1>

                  <span className="game-count">
                    {GAMES.length} Games
                  </span>
                </div>

                <div className="balance-box">
                  <span>
                    💰 Balance
                  </span>

                  <strong>
                    {casinoBalance.toLocaleString()} ETB
                  </strong>

                  <button
                    onClick={() =>
                      setCasinoBalance(
                        25000
                      )
                    }
                  >
                    <RefreshCcw size={16} />
                  </button>
                </div>

              </div>

              {/* GAME GRID */}

              <div className="games-container">
                {renderCasinoGamesGrid()}
              </div>

              {/* SELECTED GAME */}

              {selectedGame &&
                casinoIsBetPanelOpen && (
                  <div className="game-view">

                    <div className="game-view-header">

                      <h2>
                        {selectedGame.icon}{" "}
                        {selectedGame.name}
                      </h2>

                      <button
                        onClick={() => {
                          setSelectedGame(
                            null
                          );
                          setCasinoIsBetPanelOpen(
                            false
                          );
                        }}
                      >
                        ✕
                      </button>

                    </div>

                    <div className="game-area">
                      {renderCasinoGameSpecificUI(
                        selectedGame.id
                      )}
                    </div>

                    <button
                      className="tutorial-btn"
                      onClick={() =>
                        alert(
                          `How to play ${selectedGame.name}: Place your bet and try your luck!`
                        )
                      }
                    >
                      ❓ How to play
                    </button>

                  </div>
                )}

              {/* HISTORY */}

              <div className="game-history">

                <h4>
                  📜 Recent Casino Rounds
                </h4>

                <div className="history-list">

                  {casinoHistory
                    .slice(0, 10)
                    .map((game, index) => (
                      <div
                        key={index}
                        className={`history-item ${
                          game.result ===
                          "win"
                            ? "win"
                            : "lose"
                        }`}
                      >
                        <span>
                          {game.gameId}
                        </span>

                        <span>
                          {game.bet} ETB
                        </span>

                        <span>
                          {game.result ===
                          "win"
                            ? "✅"
                            : "❌"}
                        </span>

                        <strong>
                          {game.profit >= 0
                            ? "+"
                            : ""}
                          {game.profit} ETB
                        </strong>
                      </div>
                    ))}

                  {casinoHistory.length ===
                    0 && (
                    <p className="empty-history">
                      No casino rounds yet.
                    </p>
                  )}

                </div>
              </div>

            </div>
          )}
        </main>
      </div>

      {/* =====================================================
          CASINO BET PANEL
      ===================================================== */}

      {selectedGame &&
        casinoIsBetPanelOpen && (
          <div className="casino-bet-panel">

            <div className="game-info">
              <strong>
                {selectedGame.icon}{" "}
                {selectedGame.name}
              </strong>

              <small>
                Min: {selectedGame.minBet} ETB
              </small>
            </div>

            <div className="quick-bets">

              {[
                "10%",
                "25%",
                "50%",
                "100%",
              ].map((label) => {
                const value = Math.round(
                  casinoBalance *
                    (parseInt(label) /
                      100)
                );

                return (
                  <button
                    key={label}
                    onClick={() =>
                      setCasinoBetAmount(
                        Math.max(
                          value,
                          1
                        )
                      )
                    }
                  >
                    {label}
                  </button>
                );
              })}

            </div>

            <div className="bet-amounts">

              {[1, 2, 5, 10, 20, 50, 100, 500, 1000].map(
                (amount) => (
                  <button
                    key={amount}
                    className={
                      casinoBetAmount ===
                      amount
                        ? "selected"
                        : ""
                    }
                    onClick={() =>
                      setCasinoBetAmount(
                        amount
                      )
                    }
                  >
                    {amount}
                  </button>
                )
              )}

              <button
                className="max-button"
                onClick={() =>
                  setCasinoBetAmount(
                    Math.min(
                      casinoBalance,
                      selectedGame.maxBet
                    )
                  )
                }
              >
                MAX
              </button>

            </div>

            <div className="manual-input">

              <input
                type="number"
                value={casinoBetAmount}
                min="1"
                onChange={(event) =>
                  setCasinoBetAmount(
                    Math.max(
                      1,
                      Number(
                        event.target.value
                      )
                    )
                  )
                }
              />

              <span>ETB</span>
            </div>

            <button
              className="play-button"
              onClick={() =>
                playCasinoGame(
                  selectedGame.id
                )
              }
              disabled={
                casinoLoading ||
                casinoBetAmount >
                  casinoBalance
              }
            >
              {casinoLoading
                ? "⏳"
                : "▶️ Play"}
            </button>

            <button
              className="close-button"
              onClick={() => {
                setSelectedGame(null);
                setCasinoIsBetPanelOpen(
                  false
                );
              }}
            >
              ✕
            </button>

          </div>
        )}

      {/* =====================================================
          RESULT MODAL
      ===================================================== */}

      {casinoShowResultModal &&
        casinoResultData && (
          <div
            className="result-overlay"
            onClick={() =>
              setCasinoShowResultModal(
                false
              )
            }
          >
            <div
              className="result-modal"
              onClick={(event) =>
                event.stopPropagation()
              }
            >

              <div className="result-icon">
                {casinoResultData.result ===
                "win"
                  ? "🎉"
                  : "😔"}
              </div>

              <h2>
                {selectedGame?.name ||
                  "Casino Game"}
              </h2>

              <div
                className={`result-status ${
                  casinoResultData.result ===
                  "win"
                    ? "win"
                    : "lose"
                }`}
              >
                {casinoResultData.result ===
                "win"
                  ? "✅ WIN"
                  : "❌ LOSE"}
              </div>

              <div className="result-details">

                <div>
                  <span>Bet</span>
                  <strong>
                    {casinoBetAmount} ETB
                  </strong>
                </div>

                <div>
                  <span>Profit</span>
                  <strong>
                    {casinoResultData.profit}{" "}
                    ETB
                  </strong>
                </div>

                <div>
                  <span>New Balance</span>
                  <strong>
                    {casinoBalance} ETB
                  </strong>
                </div>

              </div>

              <button
                className="continue-button"
                onClick={() =>
                  setCasinoShowResultModal(
                    false
                  )
                }
              >
                Continue
              </button>

            </div>
          </div>
        )}

      {/* =====================================================
          STYLES
      ===================================================== */}

      <style>{`

        .nav-button {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: flex-start;
          gap: 14px;
          padding: 12px 16px;
          border-radius: 12px;
          color: #94a3b8;
          font-size: 14px;
          font-weight: 600;
          transition: all 0.2s;
        }

        .nav-button:hover {
          color: white;
          background: rgba(30, 41, 59, 0.5);
        }

        .nav-button.active {
          color: #fcd34d;
          background: linear-gradient(
            90deg,
            rgba(245, 158, 11, 0.2),
            transparent
          );
          border-left: 4px solid #fbbf24;
        }

        .sub-nav {
          width: 100%;
          text-align: left;
          padding: 9px 12px;
          border-radius: 8px;
          color: #94a3b8;
          font-size: 12px;
        }

        .sub-nav:hover {
          color: white;
          background: rgba(30, 41, 59, 0.5);
        }

        .sub-nav.selected {
          color: #fbbf24;
          background: rgba(245, 158, 11, 0.1);
        }

        .casino-games-page {
          background: #0b0e1a;
          border-radius: 16px;
          padding: 20px;
          color: white;
        }

        .casino-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 20px;
          padding: 20px;
          background: #1a1f33;
          border-radius: 16px;
          margin-bottom: 24px;
          border-bottom: 2px solid #f0b90b;
        }

        .casino-header h1 {
          margin: 0;
          color: #f0b90b;
          font-size: 24px;
          font-weight: 800;
        }

        .game-count {
          display: inline-block;
          margin-top: 6px;
          background: #1e2338;
          color: #94a3b8;
          padding: 5px 12px;
          border-radius: 20px;
          font-size: 12px;
        }

        .balance-box {
          display: flex;
          align-items: center;
          gap: 14px;
          background: #1e2338;
          padding: 10px 18px;
          border-radius: 30px;
          border: 1px solid rgba(240, 185, 11, 0.3);
        }

        .balance-box span {
          color: #94a3b8;
          font-size: 13px;
        }

        .balance-box strong {
          color: #f0b90b;
          font-size: 20px;
        }

        .balance-box button {
          color: #94a3b8;
          background: transparent;
          border: none;
          cursor: pointer;
        }

        .balance-box button:hover {
          color: #f0b90b;
        }

        .game-category {
          margin-bottom: 28px;
        }

        .category-title {
          color: #f0b90b;
          font-size: 18px;
          font-weight: 800;
          margin-bottom: 12px;
        }

        .category-title small {
          color: #64748b;
          font-size: 11px;
          margin-left: 8px;
        }

        .game-grid {
          display: grid;
          grid-template-columns: repeat(
            auto-fill,
            minmax(110px, 1fr)
          );
          gap: 12px;
        }

        .game-card {
          background: #151b2b;
          border-radius: 16px;
          padding: 16px 8px 12px;
          text-align: center;
          cursor: pointer;
          transition: 0.25s;
          border: 2px solid transparent;
          position: relative;
        }

        .game-card:hover {
          transform: translateY(-4px);
          border-color: rgba(
            240,
            185,
            11,
            0.4
          );
          background: #1c2338;
        }

        .game-card.active {
          border-color: #f0b90b;
          background: #1c2338;
        }

        .game-icon {
          display: block;
          font-size: 32px;
          margin-bottom: 6px;
        }

        .game-name {
          display: block;
          color: #ccd6f6;
          font-size: 11px;
          font-weight: 600;
        }

        .game-min-bet {
          display: block;
          margin-top: 4px;
          color: #8892b0;
          font-size: 9px;
        }

        .favorite-btn {
          position: absolute;
          top: 6px;
          left: 6px;
          background: transparent;
          border: none;
          color: #8892b0;
          cursor: pointer;
          font-size: 14px;
          z-index: 2;
        }

        .favorite-btn.active {
          color: #f0b90b;
        }

        .badge {
          position: absolute;
          top: 6px;
          right: 6px;
          font-size: 8px;
          padding: 2px 7px;
          border-radius: 10px;
        }

        .badge.hot {
          background: #f0b90b;
          color: #0b0e1a;
        }

        .game-view {
          margin-top: 24px;
          padding: 24px;
          background: #0f1322;
          border: 1px solid rgba(
            240,
            185,
            11,
            0.4
          );
          border-radius: 18px;
        }

        .game-view-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 16px;
        }

        .game-view-header h2 {
          color: #f0b90b;
          font-size: 22px;
          font-weight: 800;
        }

        .game-view-header button {
          color: #94a3b8;
          background: transparent;
          border: none;
          font-size: 20px;
          cursor: pointer;
        }

        .game-area {
          min-height: 220px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #1a1f33;
          border-radius: 14px;
          padding: 24px;
        }

        .game-specific {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 20px;
        }

        .game-symbols {
          font-size: 60px;
          display: flex;
          gap: 28px;
          align-items: center;
        }

        .game-symbols span {
          font-size: 18px;
          color: #64748b;
          font-weight: 800;
        }

        .coin-display {
          font-size: 80px;
        }

        .slot-reels {
          font-size: 55px;
          letter-spacing: 12px;
        }

        .aviator-multiplier {
          font-size: 50px;
          font-weight: 900;
          color: #fbbf24;
        }

        .default-game-icon {
          font-size: 70px;
        }

        .game-controls {
          display: flex;
          gap: 12px;
        }

        .btn-play,
        .btn-bet,
        .btn-cashout {
          border: none;
          border-radius: 30px;
          padding: 11px 22px;
          font-weight: 800;
          cursor: pointer;
        }

        .btn-play,
        .btn-bet {
          background: #fbbf24;
          color: #111827;
        }

        .btn-cashout {
          background: #10b981;
          color: white;
        }

        .btn-play:disabled,
        .btn-bet:disabled,
        .btn-cashout:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .tutorial-btn {
          display: block;
          margin: 16px auto 0;
          background: transparent;
          border: 1px solid #475569;
          color: #94a3b8;
          padding: 8px 16px;
          border-radius: 30px;
          cursor: pointer;
        }

        .tutorial-btn:hover {
          border-color: #fbbf24;
          color: #fbbf24;
        }

        .game-history {
          margin-top: 24px;
          padding: 18px;
          background: #1a1f33;
          border-radius: 14px;
          border: 1px solid #1e293b;
        }

        .game-history h4 {
          color: #fbbf24;
          margin-bottom: 12px;
        }

        .history-list {
          display: flex;
          flex-direction: column;
          gap: 8px;
          max-height: 220px;
          overflow-y: auto;
        }

        .history-item {
          display: grid;
          grid-template-columns: 1fr 1fr auto 1fr;
          gap: 10px;
          align-items: center;
          padding: 10px;
          background: #0b0e1a;
          border-radius: 8px;
          font-size: 12px;
        }

        .history-item.win {
          border-left: 4px solid #10b981;
        }

        .history-item.lose {
          border-left: 4px solid #ef4444;
        }

        .history-item strong {
          text-align: right;
        }

        .empty-history {
          color: #64748b;
          text-align: center;
          padding: 20px;
        }

        .casino-bet-panel {
          position: fixed;
          bottom: 0;
          left: 0;
          right: 0;
          z-index: 50;
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          justify-content: center;
          gap: 10px;
          padding: 14px;
          background: rgba(
            11,
            14,
            26,
            0.97
          );
          border-top: 1px solid
            rgba(240, 185, 11, 0.5);
          backdrop-filter: blur(10px);
        }

        .game-info {
          display: flex;
          flex-direction: column;
          text-align: center;
          min-width: 110px;
        }

        .game-info strong {
          color: #fbbf24;
        }

        .game-info small {
          color: #64748b;
        }

        .quick-bets,
        .bet-amounts {
          display: flex;
          gap: 5px;
          flex-wrap: wrap;
        }

        .quick-bets button,
        .bet-amounts button {
          background: #1e2338;
          border: 1px solid #334155;
          color: #cbd5e1;
          padding: 6px 9px;
          border-radius: 6px;
          font-size: 11px;
          cursor: pointer;
        }

        .bet-amounts button.selected {
          background: #fbbf24;
          color: #111827;
        }

        .bet-amounts .max-button {
          border-color: #ef4444;
          color: #ef4444;
        }

        .manual-input {
          display: flex;
          align-items: center;
          gap: 5px;
          background: #1e2338;
          border: 1px solid #334155;
          border-radius: 8px;
          padding: 5px 9px;
        }

        .manual-input input {
          width: 70px;
          background: transparent;
          border: none;
          outline: none;
          color: white;
        }

        .manual-input span {
          color: #64748b;
          font-size: 11px;
        }

        .play-button {
          background: #fbbf24;
          color: #111827;
          border: none;
          border-radius: 30px;
          padding: 10px 24px;
          font-weight: 900;
          cursor: pointer;
        }

        .play-button:disabled {
          opacity: 0.5;
        }

        .close-button {
          background: transparent;
          border: none;
          color: #94a3b8;
          font-size: 20px;
          cursor: pointer;
        }

        .result-overlay {
          position: fixed;
          inset: 0;
          z-index: 60;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 16px;
          background: rgba(0, 0, 0, 0.85);
          backdrop-filter: blur(5px);
        }

        .result-modal {
          width: 100%;
          max-width: 380px;
          background: #151b2b;
          border: 1px solid
            rgba(240, 185, 11, 0.3);
          border-radius: 20px;
          padding: 24px;
          text-align: center;
        }

        .result-icon {
          font-size: 50px;
          margin-bottom: 8px;
        }

        .result-modal h2 {
          color: white;
          margin-bottom: 12px;
          font-size: 20px;
        }

        .result-status {
          font-size: 34px;
          font-weight: 900;
          margin-bottom: 20px;
        }

        .result-status.win {
          color: #34d399;
        }

        .result-status.lose {
          color: #f43f5e;
        }

        .result-details {
          display: flex;
          flex-direction: column;
          gap: 10px;
          margin-bottom: 20px;
        }

        .result-details div {
          display: flex;
          justify-content: space-between;
          border-bottom: 1px solid #1e293b;
          padding-bottom: 8px;
          color: #94a3b8;
        }

        .result-details strong {
          color: white;
        }

        .continue-button {
          width: 100%;
          border: none;
          background: #fbbf24;
          color: #111827;
          padding: 12px;
          border-radius: 30px;
          font-weight: 900;
          cursor: pointer;
        }

        @media (max-width: 700px) {
          .casino-header {
            flex-direction: column;
            align-items: stretch;
          }

          .balance-box {
            justify-content: center;
          }

          .history-item {
            grid-template-columns: 1fr 1fr auto;
          }

          .history-item strong {
            text-align: left;
          }

          .casino-bet-panel {
            padding-bottom: 70px;
          }
        }

      `}</style>
    </div>
  );
}

// =========================================================
// SIMPLE PAGE COMPONENT
// =========================================================

function PageCard({
  title,
  description,
}) {
  return (
    <div className="p-6 bg-[#111625] rounded-xl border border-slate-800">
      <h2 className="text-xl font-bold text-amber-400 mb-2">
        {title}
      </h2>

      <p className="text-slate-400">
        {description}
      </p>
    </div>
  );
}