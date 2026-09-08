import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  const description = `Bike Crashes ${Date.now()}`;
  await page.goto('http://localhost:3001/login');
  await page.getByRole('textbox', { name: 'Email' }).click();
  await page.getByRole('textbox', { name: 'Email' }).fill('claimant@demo.com');
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill('Claimant123!');
  await page.getByRole('button', { name: 'Log In' }).click();
  await page.getByRole('button', { name: 'Submit Claim' }).click();
  await page.getByRole('textbox', { name: 'When did the incident occur? *' }).fill('2026-09-01');
  await page.getByRole('textbox', { name: 'Where did the incident occur' }).click();
  await page.getByRole('textbox', { name: 'Where did the incident occur' }).fill('Taman desa');
  await page.getByRole('spinbutton', { name: 'Claim Amount *' }).click();
  await page.getByRole('spinbutton', { name: 'Claim Amount *' }).fill('50');
  await page.getByTestId('wizard-step-1').getByRole('button', { name: 'Next' }).click();
  await page.getByRole('textbox', { name: 'Describe what happened *' }).click();
  await page.getByRole('textbox', { name: 'Describe what happened *' }).fill(description);
  await page.getByTestId('wizard-step-2').getByRole('button', { name: 'Next' }).click();
  await page.getByTestId('wizard-step-3').getByRole('button', { name: 'Submit Claim' }).click();

  // TODO: assert the new claim appears in My Claims
  const row = page.getByRole('row', { name: description });
  await expect(row).toBeVisible();
  await expect(row).toContainText('Submitted');
});