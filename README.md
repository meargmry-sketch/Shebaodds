# Not currently used by App.jsx

These three files are a **second, more modular** attempt at the same UI that
`App.jsx` already implements as one big self-contained file:

- `App.jsx` defines its own inline `BetSlip()` and `CasinoGames()` components
  (search for `function BetSlip()` and `function CasinoGames()` inside it) and
  never imports anything from this folder.
- `SportsbookHeader.jsx` imports `./BetSlip` (present here) and `./CasinoGames`
  (**does not exist anywhere in the repo** — this file will not compile if you
  wire it in as-is).
- `LiveUpcomingMatches.jsx` is misnamed: its actual content (sidebar nav with
  Users/Trophy/Finance/Withdrawals/Reports, a bets/deposits table, etc.) is an
  **admin dashboard**, not a live-matches widget. It matches the "Bet Master
  Admin Panel" mockup, not the player-facing app.

Nothing currently imports these three files, so they don't block the build.
Before wiring any of them in, decide: keep the single-file `App.jsx`
approach, or switch to this modular version (which needs a `CasinoGames.jsx`
written and `LiveUpcomingMatches.jsx` renamed/moved into a separate admin
app)? Pick one — don't try to use both, they duplicate the same screens.
