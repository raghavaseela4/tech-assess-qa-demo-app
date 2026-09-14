import { test, expect } from '@playwright/test';

test('admin approves one claim and rejects another', async ({ page }) => {
  await page.goto('http://localhost:3001/login');
  await page.getByRole('textbox', { name: 'Email' }).click();
  await page.getByRole('textbox', { name: 'Email' }).fill('admin@demo.com');
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill('Admin123!');
  await page.getByRole('button', { name: 'Log In' }).click();
  await page.getByRole('button', { name: 'Claims' }).click();

  const table = page.getByRole('table');

  const submittedRows = table.getByRole('row').filter({
    has: page.getByRole('status', { name: 'Claim status: Submitted' }),
  });
  await expect(submittedRows.first()).toBeVisible();
  expect(await submittedRows.count()).toBeGreaterThanOrEqual(2);

  // Pin down the FIRST claim's id right now, before its status changes —
  // reading the testid attribute gives us a stable target for every
  // subsequent step on this specific claim.
  const firstSelect = submittedRows.nth(0).getByRole('combobox', { name: /Update status/ });
  const firstTestId = await firstSelect.getAttribute('data-testid'); // e.g. "status-select-e4c891fa"
  const approveDropdown = table.getByTestId(firstTestId!);

  // Pin down the SECOND claim's id too, before either row moves.
  const secondSelect = submittedRows.nth(1).getByRole('combobox', { name: /Update status/ });
  const secondTestId = await secondSelect.getAttribute('data-testid');
  const rejectDropdown = table.getByTestId(secondTestId!);

  const confirmBtn = page.getByRole('button', { name: 'Confirm' });

  // Both dropdowns now always point at the SAME two claims, scoped to the
  // table specifically — avoids the strict-mode collision if the same
  // testid exists twice in the DOM (e.g. a hidden mobile-card duplicate).
  await approveDropdown.selectOption('UNDER_REVIEW');
  await confirmBtn.click();
  await expect(confirmBtn).not.toBeVisible(); // wait for this update to fully settle before the next one

  await approveDropdown.selectOption('APPROVED');
  await confirmBtn.click();
  await expect(confirmBtn).not.toBeVisible();

  await rejectDropdown.selectOption('REJECTED');
  await confirmBtn.click();
  await expect(confirmBtn).not.toBeVisible();

  await page.getByRole('button', { name: 'Logout' }).click();
  await page.close();
});