import { test, expect, Page, Locator } from '@playwright/test';

type StatusCounts = Record<string, number>;
type ClaimDetail = { claimId: string; status: string };

async function getStatusCounts(scope: Page | Locator): Promise<{ total: number; byStatus: StatusCounts }> {
  const badges = await scope.getByRole('status', { name: /Claim status:/ }).all();
  const byStatus: StatusCounts = {};
  for (const badge of badges) {
    const label = (await badge.getAttribute('aria-label')) ?? '';
    const status = label.replace('Claim status:', '').trim();
    byStatus[status] = (byStatus[status] ?? 0) + 1;
  }
  return { total: badges.length, byStatus };
}

async function getClaimDetails(table: Locator): Promise<ClaimDetail[]> {
  const rows = await table.getByRole('row').all();
  const details: ClaimDetail[] = [];
  for (const row of rows) {
    const idCode = row.getByRole('code').first();
    if ((await idCode.count()) === 0) continue;
    const claimId = (await idCode.innerText()).trim();
    const statusBadge = row.getByRole('status', { name: /Claim status:/ });
    const label = (await statusBadge.getAttribute('aria-label')) ?? '';
    const status = label.replace('Claim status:', '').trim();
    details.push({ claimId, status });
  }
  return details;
}

function logSummary(label: string, summary: { total: number; byStatus: StatusCounts }) {
  console.log(`\n[${label}] Total claims: ${summary.total}`);
  for (const [status, count] of Object.entries(summary.byStatus)) {
    console.log(`  ${status}: ${count}`);
  }
}

function logFullList(label: string, details: ClaimDetail[]) {
  console.log(`\n[${label}] Full claim list (${details.length} claims):`);
  for (const d of details) {
    console.log(`  ${d.claimId}  ->  ${d.status}`);
  }
}

// If the app's WebSocket drops, it shows an offline banner and stops
// pushing updates entirely ("Connection status: Offline" / "Real-time
// updates unavailable. Please refresh page manually."). Waiting on a live
// UI update after this point hangs forever, since nothing will ever
// re-render on its own. Detect it and do what the banner asks: refresh.
//
// NOTE: this is masking a real app reliability gap (no auto-reconnect, no
// polling fallback) — see the writeup after this script.
async function ensureConnected(page: Page) {
  const offlineBanner = page.getByRole('alert', { name: /Real-time updates unavailable/ });
  if (await offlineBanner.isVisible().catch(() => false)) {
    console.warn('WebSocket disconnected mid-test — reloading page to recover.');
    await page.reload();
    await page.getByRole('heading', { name: 'Claims Management' }).waitFor();
  }
}

async function waitForTableLoaded(page: Page, table: Locator) {
  const loading = page.getByRole('status', { name: 'Loading claims' });
  await loading.waitFor({ state: 'hidden' }).catch(() => {});
  await table.waitFor();
}

async function submitOneClaim(page: Page, description: string): Promise<string> {
  await page.getByRole('button', { name: 'Submit Claim' }).click();
  await page.getByRole('textbox', { name: 'When did the incident occur? *' }).fill('2026-09-01');
  await page.getByRole('textbox', { name: 'Where did the incident occur' }).fill('Taman desa');
  await page.getByRole('spinbutton', { name: 'Claim Amount *' }).fill('50');
  await page.getByTestId('wizard-step-1').getByRole('button', { name: 'Next' }).click();
  await page.getByRole('textbox', { name: 'Describe what happened *' }).fill(description);
  await page.getByTestId('wizard-step-2').getByRole('button', { name: 'Next' }).click();
  await page.getByTestId('wizard-step-3').getByRole('button', { name: 'Submit Claim' }).click();
  await page.getByRole('heading', { name: 'My Claims' }).waitFor();

  const row = page.getByRole('row').filter({ hasText: description });
  await expect(row).toBeVisible();
  const claimId = await row.getByRole('code').first().innerText();
  return claimId.trim();
}

async function changeStatus(
  page: Page,
  table: Locator,
  confirmBtn: Locator,
  claimId: string,
  status: 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED',
  expectedLabel: 'Under Review' | 'Approved' | 'Rejected'
) {
  await ensureConnected(page);

  const row = table.getByRole('row').filter({ hasText: claimId });
  await expect(row).toBeVisible();
  await row.getByRole('combobox', { name: /Update status/ }).selectOption(status);
  await confirmBtn.click();
  await expect(confirmBtn).not.toBeVisible();

  await ensureConnected(page); // the drop can also happen right after confirming

  const settledRow = table.getByRole('row').filter({ hasText: claimId });
  await expect(settledRow.getByRole('status', { name: `Claim status: ${expectedLabel}` }))
    .toBeVisible({ timeout: 15_000 });
}

test('admin approves one claim and rejects another', async ({ browser }) => {
  test.setTimeout(120_000);

  // ---------- CLAIMANT SIDE ----------
  const claimantContext = await browser.newContext();
  const claimantPage = await claimantContext.newPage();
  await claimantPage.goto('http://localhost:3001/login');
  await claimantPage.getByRole('textbox', { name: 'Email' }).fill('claimant@demo.com');
  await claimantPage.getByRole('textbox', { name: 'Password' }).fill('Claimant123!');
  await claimantPage.getByRole('button', { name: 'Log In' }).click();

  await claimantPage.getByRole('heading', { name: 'My Claims' }).waitFor();
  const claimantTable = claimantPage.getByRole('table');
  await claimantTable.waitFor();

  const claimantBefore = await getStatusCounts(claimantPage);
  logSummary('Claimant - before', claimantBefore);

  const approveId = await submitOneClaim(claimantPage, `E2E Approve ${Date.now()}`);
  const rejectId = await submitOneClaim(claimantPage, `E2E Reject ${Date.now()}`);

  const claimantAfter = await getStatusCounts(claimantPage);
  logSummary('Claimant - after submitting 2 claims', claimantAfter);
  expect(claimantAfter.total).toBe(claimantBefore.total + 2);

  const claimantFullList = await getClaimDetails(claimantTable);
  logFullList('Claimant - full claim list', claimantFullList);

  await claimantContext.close();

  // ---------- ADMIN SIDE ----------
  const adminContext = await browser.newContext();
  const page = await adminContext.newPage();
  await page.goto('http://localhost:3001/login');
  await page.getByRole('textbox', { name: 'Email' }).fill('admin@demo.com');
  await page.getByRole('textbox', { name: 'Password' }).fill('Admin123!');
  await page.getByRole('button', { name: 'Log In' }).click();
  await page.getByRole('button', { name: 'Claims' }).click();

  await page.getByRole('heading', { name: 'Claims Management' }).waitFor();
  const table = page.getByRole('table');
  await waitForTableLoaded(page, table);
  const confirmBtn = page.getByRole('button', { name: 'Confirm' });

  const adminBefore = await getStatusCounts(table);
  logSummary('Admin - before', adminBefore);
  expect(Object.values(adminBefore.byStatus).reduce((a, b) => a + b, 0)).toBe(adminBefore.total);
  expect(adminBefore.total).toBe(claimantAfter.total);

  await changeStatus(page, table, confirmBtn, approveId, 'UNDER_REVIEW', 'Under Review');
  await changeStatus(page, table, confirmBtn, approveId, 'APPROVED', 'Approved');
  await changeStatus(page, table, confirmBtn, rejectId, 'REJECTED', 'Rejected');

  await ensureConnected(page);
  await waitForTableLoaded(page, table);

  const adminAfter = await getStatusCounts(table);
  logSummary('Admin - after actions', adminAfter);

  expect(adminAfter.total).toBe(adminBefore.total);
  expect(adminAfter.byStatus['Approved'] ?? 0).toBe((adminBefore.byStatus['Approved'] ?? 0) + 1);
  expect(adminAfter.byStatus['Rejected'] ?? 0).toBe((adminBefore.byStatus['Rejected'] ?? 0) + 1);
  expect(adminAfter.byStatus['Submitted'] ?? 0).toBe((adminBefore.byStatus['Submitted'] ?? 0) - 2);

  const adminFullList = await getClaimDetails(table);
  logFullList('Admin - full claim list after actions', adminFullList);

  expect(adminAfter.total).toBe(claimantAfter.total);

  const approvedEntry = adminFullList.find(c => c.claimId === approveId);
  const rejectedEntry = adminFullList.find(c => c.claimId === rejectId);
  expect(approvedEntry?.status).toBe('Approved');
  expect(rejectedEntry?.status).toBe('Rejected');

  await page.getByRole('button', { name: 'Logout' }).click();
  await adminContext.close();
});