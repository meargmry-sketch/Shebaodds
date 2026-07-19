/**
 * ShebaOdds Enterprise Server - JavaScript Entry Point Wrapper
 * Registers the runtime ts-node compilation hooks to run the system TypeScript natively.
 */
try {
  require('ts-node').register();
  require('./server.ts');
} catch (err) {
  console.warn('⚠️ [LOADER WARN] ts-node not pre-loaded. Attempting fallback direct TypeScript import wrapper...');
  try {
    require('./server');
  } catch (fallbackErr) {
    console.error('💥 [LOADER ERROR] Failed to initialize TS execution engine:', fallbackErr);
    process.exit(1);
  }
}
