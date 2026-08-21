<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/adec3182-16d4-49ec-be19-8bffd17d4047

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

---

## SHEBAODDS — Casino, Multi-language & Admin Ticket Review (this update)

### What's new
- **Brand mark**: SVG crown logo (`assets/branding/shebaodds-crown-logo.svg` full lockup,
  `assets/branding/shebaodds-crown-mark.svg` icon-only) plus a reusable `Logo.jsx`
  (`CrownMark`, `BrandLockup`) used across the sidebar, footer, and loading screen —
  replacing the placeholder 🦁 emoji.
- **Casino page (`CasinoGamesPage.jsx`)**: 60 casino games across Slots, Table Games,
  Live Casino, Crash & Instant, and Jackpot, with search/category filters and a
  "Play Demo" modal.
- **Multi-language (`LanguageContext.jsx` + `/locales`)**: English, Amharic, Afaan
  Oromo, Tigrinya, Somali, Arabic (RTL-aware), and French. `en`, `am`, `fr`, and `ar`
  are fully translated; `om`, `ti`, and `so` currently cover navigation/auth/wallet
  terms with the rest falling back to English — have a native speaker review these
  before shipping production copy.
- **Admin ticket review (`AdminDepositReview.jsx`)**: a sandboxed screen for
  reviewing mobile-money (Telebirr/CBE Birr-style) deposit/withdrawal requests by
  matching the ticket/reference number and amount the user submitted.

### Real-money boundary — please read
The 60 casino games are UI/demo shells only — no real wagering logic runs in the
frontend. `AdminDepositReview.jsx` is also a **sandbox**: its Approve/Reject buttons
only update local component state and never call the server or move a real balance.

This codebase separately already contains a real wallet engine (`walletRoutes.ts`,
`MongoDBWalletEngine.ts`) and a real admin approval endpoint
(`POST /api/admin/transactions/:id/approve` in `adminRoutes.ts`) that **does** write
to a user's actual balance. Both files now have inline comments marking this
boundary. Connecting the demo ticket-review screen to that real endpoint — or
wiring `processDeposit`/`processWithdrawal` to an actual payment gateway or
game-provider API — is a licensing and compliance decision for whoever operates
this platform, and isn't something this update turns on by default.

---

## Stabilization pass #2 — Render build failure fixes

This addressed the actual `npm run build` failure log from Render, working against
this repo directly (not the earlier flat zip, which had diverged from this one).

### Root cause of ~35 of ~45 errors
`package.json` pinned `mongoose: "^8.0.0"` and `typescript: "^5.7.2"` with open
caret ranges. TypeScript 5.5+ has a well-documented incompatibility with Mongoose
8's bundled type overloads, producing `error TS2349: This expression is not
callable` / "union type has signatures, but none... compatible" on almost every
`.find()`/`.update()`/`.insertMany()` call across the codebase. **Fixed** by
constraining `typescript` to `>=5.3.0 <5.5.0`. This is a well-established class of
fix, not something verified against a live `npm install` here (no network access)
— run `npm run build` and let me know if any of this class of error remains.

### Distinct bugs (not the version issue) fixed individually
- `server.ts` imported `./serverBootstrap`; the file is `bootstrap.ts`. Build
  failed immediately on this alone.
- `authRoutes.ts` imported from `../models/User` / `../utils/passwordValidator` —
  paths that don't exist in this flat repo (fixed to `./User`, `./passwordValidator`)
  — and imported a `UserDocument` type `User.ts` never exported (added as a
  `export type UserDocument = IUser` alias).
- `bettingRoutes.ts` used `Schema` and `router` without importing/declaring
  either — `const router = express.Router()` was missing outright.
- `bettingRoutes.ts` and `Match.ts` each independently registered a **duplicate
  `CasinoGame` Mongoose model** under the same name. Consolidated to the one in
  `Match.ts`; `bettingRoutes.ts` now imports it.
- `passwordValidator.ts`: `let strength = PASSWORD_STRENGTH.WEAK` inferred the
  narrow literal type `"weak"`, so every later reassignment (`"fair"`, `"good"`,
  etc.) was a type error. Fixed with an explicit union type annotation.
- `adminTransactionRoutes.ts` had a manual wallet-init object missing 3 fields
  (`totalLost`, `totalBonusReceived`, `totalCashbackReceived`) required by
  `IUser`'s wallet type.
- `@google/generative-ai` was pinned at `^0.1.3`, predating the
  `systemInstruction` option used in `expressApiGateway.ts`. Bumped to `^0.2.1`.
- Two seed-data `insertMany()` calls (`expressApiGateway.ts`,
  `scripts/seed.ts`) pass intentionally-minimal literals against overly strict
  Document-shaped parameter types — cast at the call site rather than padding
  seed data with 50+ fake Document properties.

### Reapplied from the earlier stabilization pass
This repo had diverged before the first stabilization pass landed, so it was
reapplied here: `expressApiGateway.ts`'s duplicate auth/wallet routes were
shadowing the real `authRoutes.ts`/`walletRoutes.ts` handlers (fixed by mount
order in `server.ts`, with a warning comment left in both files), only Amharic
had a `/locales` route (now serves all languages via `express.static`), and
`/api/notifications` didn't exist anywhere (added an honest empty stub).

### Verified
Every `.ts` and `.jsx` file in this repo syntax-checks cleanly (via esbuild), and
every relative import (including `../` paths in `scripts/`) resolves to a real
file and a real exported name. **Not verified**: a live `tsc`/`npm run build`
pass, since this environment has no network access to install `mongoose` and the
rest of the (large) dependency list. Please run the real build and send me
anything that's still failing.
