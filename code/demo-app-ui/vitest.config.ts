import { defineConfig } from 'vitest/config';
import path from 'node:path';

// Vitest, @testing-library/react, jest-dom, user-event, and jsdom were already
// present in package.json's devDependencies — and the "test"/"test:watch"/
// "test:coverage" npm scripts already existed too — but no vitest.config.ts
// had ever been written, so `npm test` had nothing to actually run. This
// wires up what was already installed rather than adding new dependencies.
export default defineConfig({
  esbuild: {
    jsx: 'automatic',
    jsxImportSource: 'react',
  },
  resolve: {
    alias: {
      // Mirrors tsconfig.json's "@/*": ["./*"] path alias.
      '@': path.resolve(__dirname, './'),
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.ts'],
    css: true,
    // Vitest's default file discovery also matches *.spec.ts, which collides
    // with the Playwright specs under e2e/ (two different test() globals from
    // two different frameworks) — scope Vitest explicitly to component tests
    // only, and leave e2e/ entirely to Playwright.
    include: ['src/**/*.test.{ts,tsx}'],
    exclude: ['e2e/**', 'node_modules/**', '.next/**'],
  },
});
