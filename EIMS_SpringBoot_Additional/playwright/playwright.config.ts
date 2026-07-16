import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 15_000,
  expect: { timeout: 3_000 },
  outputDir: './results/artifacts',
  reporter: [['./additional-reporter.ts']],
  use: {
    baseURL: process.env.EIMS_BASE_URL ?? 'http://127.0.0.1:18080',
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'off',
  },
});
