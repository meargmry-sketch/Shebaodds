import requests
import time
import random
import json
from datetime import datetime

# ==========================================================
# CONFIGURATION
# ==========================================================
TELEGRAM_TOKEN = "7921295854:AAG1eJ2sIEyRZxQRsrCeDvcbFFK1QTEYzyM"
CHAT_ID = "6591377041"

# ==========================================================
# 51+ CASINO GAMES DATA
# ==========================================================
CASINO_GAMES = [
    {"id": "dice", "name": "Dice", "icon": "🎲", "category": "Table"},
    {"id": "aviator", "name": "Aviator", "icon": "✈️", "category": "Crash"},
    {"id": "coinflip", "name": "CoinFlip", "icon": "🪙", "category": "Crash"},
    {"id": "plinko", "name": "Plinko", "icon": "📉", "category": "Crash"},
    {"id": "blackjack", "name": "Blackjack", "icon": "🃏", "category": "Classic"},
    {"id": "roulette", "name": "Roulette", "icon": "🎡", "category": "Table"},
    {"id": "mines", "name": "Mines", "icon": "💣", "category": "Crash"},
    {"id": "crash", "name": "Crash", "icon": "📈", "category": "Crash"},
    {"id": "tower", "name": "Tower", "icon": "🏗️", "category": "Classic"},
    {"id": "keno", "name": "Keno", "icon": "🔢", "category": "Slots"},
    {"id": "baccarat", "name": "Baccarat", "icon": "♣️", "category": "Table"},
    {"id": "wheel", "name": "Wheel of Fortune", "icon": "🎰", "category": "Table"},
    {"id": "hilo", "name": "Hilo", "icon": "⬆️⬇️", "category": "Classic"},
    {"id": "sicbo", "name": "Sic Bo", "icon": "🎲🎲🎲", "category": "Table"},
    {"id": "videopoker", "name": "Video Poker", "icon": "🃏", "category": "Classic"},
    {"id": "bingo", "name": "Bingo", "icon": "🎯", "category": "Slots"},
    {"id": "craps", "name": "Craps", "icon": "🎲", "category": "Table"},
    {"id": "dragontiger", "name": "Dragon Tiger", "icon": "🐉🐯", "category": "Table"},
    {"id": "andarbahar", "name": "Andar Bahar", "icon": "🃏", "category": "Table"},
    {"id": "teenpatti", "name": "Teen Patti", "icon": "♠️", "category": "Classic"},
    {"id": "lucky7", "name": "Lucky 7", "icon": "🍀7️⃣", "category": "Slots"},
    {"id": "scratch", "name": "Scratch Card", "icon": "🎫", "category": "Slots"},
    {"id": "football", "name": "Football Prediction", "icon": "⚽", "category": "Sports"},
    {"id": "basketball", "name": "Basketball Prediction", "icon": "🏀", "category": "Sports"},
    {"id": "horseracing", "name": "Horse Racing", "icon": "🐎", "category": "Sports"},
    {"id": "spinwin", "name": "Spin & Win", "icon": "🌀", "category": "Special"},
    {"id": "slot", "name": "Slot Machine", "icon": "🎰", "category": "Slots"},
    {"id": "reddog", "name": "Red Dog", "icon": "🐕", "category": "Classic"},
    {"id": "war", "name": "War", "icon": "⚔️", "category": "Table"},
    {"id": "paigow", "name": "Pai Gow Poker", "icon": "🀄️", "category": "Table"},
    {"id": "diceduels", "name": "Dice Duels", "icon": "⚔️🎲", "category": "Crash"},
    {"id": "penalty", "name": "Penalty", "icon": "⚽", "category": "Sports"},
    {"id": "chickenroad", "name": "Chicken Road", "icon": "🐔", "category": "Crash"},
    {"id": "chickenshot", "name": "Chicken Shot", "icon": "🔫🐔", "category": "Crash"},
    {"id": "megaball", "name": "Mega Ball", "icon": "⚾", "category": "Slots"},
    {"id": "pokerdice", "name": "Poker Dice", "icon": "🎲", "category": "Classic"},
    {"id": "lightningdice", "name": "Lightning Dice", "icon": "⚡🎲", "category": "Crash"},
    {"id": "carroulette", "name": "Car Roulette", "icon": "🚗", "category": "Table"},
    {"id": "knockout", "name": "Knock Out", "icon": "🥊", "category": "Sports"},
    {"id": "rummy", "name": "Rummy", "icon": "🃏", "category": "Classic"},
    {"id": "darts", "name": "Darts", "icon": "🎯", "category": "Special"},
    {"id": "tennis", "name": "Tennis", "icon": "🎾", "category": "Sports"},
    {"id": "baseball", "name": "Baseball", "icon": "⚾", "category": "Sports"},
    {"id": "greyhound", "name": "Greyhound Racing", "icon": "🐕‍🦺", "category": "Sports"},
    {"id": "motorbike", "name": "Motorbike Racing", "icon": "🏍️", "category": "Sports"},
    {"id": "cricket", "name": "Cricket", "icon": "🏏", "category": "Sports"},
    {"id": "roulette360", "name": "Roulette 360", "icon": "🎡", "category": "Table"},
    {"id": "megawheel", "name": "Mega Wheel", "icon": "🎡", "category": "Table"},
    {"id": "monopoly", "name": "Monopoly", "icon": "🎩", "category": "Table"},
    {"id": "virtualsports", "name": "Virtual Sports", "icon": "🎮", "category": "Sports"},
    {"id": "texasholdem", "name": "Texas Hold'em", "icon": "♠️", "category": "Classic"}
]

# ==========================================================
# TELEGRAM SENDER
# ==========================================================
def send_message(text):
    url = f"https://api.telegram.org/bot{TELEGRAM_TOKEN}/sendMessage"
    payload = {
        "chat_id": CHAT_ID,
        "text": text,
        "parse_mode": "HTML"
    }
    try:
        requests.post(url, data=payload)
    except Exception as e:
        print("Error sending message:", e)

# ==========================================================
# SPORTS DATA (Static example – replace with real API later)
# ==========================================================
def get_match_data():
    return [
        {
            "league": "Premier League",
            "home": "Manchester City",
            "away": "Chelsea",
            "odds": "1.65 - 4.20 - 5.00",
            "prediction": "Over 2.5 Goals"
        },
        {
            "league": "La Liga",
            "home": "Barcelona",
            "away": "Real Betis",
            "odds": "1.80 - 3.80 - 4.60",
            "prediction": "BTTS"
        }
    ]

# ==========================================================
# CASINO FUNCTIONS
# ==========================================================
def get_random_casino_message():
    """Returns a casino promotional or result message."""
    game = random.choice(CASINO_GAMES)
    win_amount = random.randint(10, 5000)
    multiplier = round(random.uniform(1.1, 10.0), 2)

    message_types = [
        f"🎰 <b>Big Win Alert!</b>\nUser just won <b>{win_amount} ETB</b> on {game['icon']} {game['name']}!",
        f"🔥 {game['icon']} <b>{game['name']}</b> is LIVE!\nMultiplier: <b>{multiplier}x</b>\nCategory: {game['category']}\nPlay now!",
        f"💥 A player cashed out at <b>{multiplier}x</b> on {game['icon']} {game['name']}!",
        f"🎯 {game['icon']} <b>{game['name']}</b>\nWin rate: {random.randint(40, 70)}%\nTry your luck now!",
        f"🌟 New player just hit a <b>{win_amount} ETB</b> jackpot on {game['icon']} {game['name']}!",
        f"📊 {game['icon']} {game['name']} is trending!\nAverage multiplier: {multiplier}x\nJump in!",
    ]

    return random.choice(message_types)

def get_daily_casino_promo():
    """Returns a daily casino promotion message."""
    games_sample = random.sample(CASINO_GAMES, 3)
    game_names = ", ".join([f"{g['icon']} {g['name']}" for g in games_sample])
    return f"🎰 <b>Today's Casino Promo</b>\n\nPlay these hot games:\n{game_names}\n\nGet up to <b>100%</b> deposit bonus! 💰"

def get_casino_stats():
    """Returns fake casino statistics."""
    total_players = random.randint(100, 1500)
    total_bets = random.randint(500, 5000)
    total_wins = random.randint(200, 2500)
    biggest_win = random.randint(1000, 50000)

    return (
        f"📊 <b>Casino Stats</b>\n"
        f"👥 Total Players: <b>{total_players}</b>\n"
        f"🎯 Total Bets: <b>{total_bets}</b>\n"
        f"🏆 Total Wins: <b>{total_wins}</b>\n"
        f"💰 Biggest Win Today: <b>{biggest_win} ETB</b>"
    )

# ==========================================================
# MAIN BOT LOOP
# ==========================================================
def run_bot():
    cycle = 0
    while True:
        cycle += 1
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"🕒 Bot cycle {cycle} at {now}")

        # -----------------------------
        # 1. Send sports matches
        # -----------------------------
        matches = get_match_data()
        for match in matches:
            message = (
                f"⚽ <b>{match['league']}</b>\n"
                f"{match['home']} vs {match['away']}\n"
                f"<b>Odds:</b> {match['odds']}\n"
                f"<b>Prediction:</b> {match['prediction']}\n"
            )
            send_message(message)

        # -----------------------------
        # 2. Send casino messages (rotating)
        # -----------------------------
        # Every 2nd cycle: send casino stats
        if cycle % 2 == 0:
            send_message(get_casino_stats())

        # Every 3rd cycle: send casino promo
        if cycle % 3 == 0:
            send_message(get_daily_casino_promo())

        # Always send a random casino game alert
        send_message(get_random_casino_message())

        # If it's the weekend (Saturday or Sunday), send extra bonus
        weekday = datetime.now().weekday()
        if weekday >= 5:  # 5 = Saturday, 6 = Sunday
            send_message(
                "🎉 <b>Weekend Casino Special!</b>\n\n"
                "Double odds on all Slots and Crash games!\n"
                "Play now and win big! 🚀"
            )

        # -----------------------------
        # 3. Wait 1 hour (3600 seconds)
        # -----------------------------
        time.sleep(3600)

# ==========================================================
# ENTRY POINT
# ==========================================================
if __name__ == "__main__":
    run_bot()