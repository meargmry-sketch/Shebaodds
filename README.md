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
