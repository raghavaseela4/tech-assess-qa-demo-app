import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import toast from 'react-hot-toast';
import { AdminClaimsTable } from '../admin-claims-table';
import type { ClaimResponse } from '@/src/lib/api/bff-client';

// AdminClaimsTable's own imports pull in real child components that each have
// their own network/side-effect concerns (clipboard writes, status-update API
// calls). Mocking them keeps this a true component test of AdminClaimsTable's
// own rendering and event-wiring logic, not an integration test of everything
// underneath it — those concerns already have their own coverage (Playwright
// E2E for ClaimStatusSelect's real update flow, contract tests for the Kafka
// side of a status change).
vi.mock('react-hot-toast', () => ({
  default: vi.fn(),
}));

const mockCopyToClipboard = vi.fn();
vi.mock('@/src/features/claims/hooks/use-copy-clipboard', () => ({
  useCopyClipboard: () => ({ copyToClipboard: mockCopyToClipboard }),
}));

vi.mock('../claim-status-select', () => ({
  ClaimStatusSelect: ({ claimId, currentStatus }: { claimId: string; currentStatus: string }) => (
    <div data-testid="claim-status-select" data-claim-id={claimId} data-current-status={currentStatus} />
  ),
}));

function buildClaim(overrides: Partial<ClaimResponse> = {}): ClaimResponse {
  return {
    claimId: '3e452ab9-25d1-4fe4-ae6e-35c4e879e2cf',
    userId: '4cbe839c-abc1-4a11-9a1a-111111111111',
    incidentDate: new Date('2026-09-08'),
    incidentLocation: 'Taman Desa',
    description: 'Accident car',
    claimAmount: 51.0,
    status: 'APPROVED',
    createdAt: new Date('2026-09-08T10:00:00Z'),
    updatedAt: new Date('2026-09-08T10:00:00Z'),
    ...overrides,
  } as ClaimResponse;
}

describe('AdminClaimsTable', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders one row per claim with truncated IDs, formatted date, and formatted amount', () => {
    const claim = buildClaim();
    render(<AdminClaimsTable claims={[claim]} />);

    expect(screen.getByText(claim.claimId.substring(0, 8))).toBeInTheDocument();
    expect(screen.getByText(claim.userId.substring(0, 8))).toBeInTheDocument();
    expect(screen.getByText('Sep 8, 2026')).toBeInTheDocument();
    expect(screen.getByText('$51.00')).toBeInTheDocument();
  });

  it('renders exactly as many rows as claims passed in', () => {
    const claims = [
      buildClaim({ claimId: 'aaaaaaaa-0000-0000-0000-000000000001' }),
      buildClaim({ claimId: 'bbbbbbbb-0000-0000-0000-000000000002' }),
      buildClaim({ claimId: 'cccccccc-0000-0000-0000-000000000003' }),
    ];
    render(<AdminClaimsTable claims={claims} />);

    // header row + 3 data rows
    expect(screen.getAllByRole('row')).toHaveLength(4);
  });

  it('passes the correct claimId and currentStatus down to ClaimStatusSelect', () => {
    const claim = buildClaim({ status: 'SUBMITTED' });
    render(<AdminClaimsTable claims={[claim]} />);

    const select = screen.getByTestId('claim-status-select');
    expect(select).toHaveAttribute('data-claim-id', claim.claimId);
    expect(select).toHaveAttribute('data-current-status', 'SUBMITTED');
  });

  it('copies the FULL (untruncated) claim ID when the copy button is clicked, not the 8-char display value', async () => {
    const user = userEvent.setup();
    const claim = buildClaim();
    render(<AdminClaimsTable claims={[claim]} />);

    await user.click(screen.getByRole('button', { name: 'Copy full Claim ID' }));

    expect(mockCopyToClipboard).toHaveBeenCalledWith(claim.claimId, 'Claim ID copied');
  });

  it('copies the full user ID when the copy button is clicked', async () => {
    const user = userEvent.setup();
    const claim = buildClaim();
    render(<AdminClaimsTable claims={[claim]} />);

    await user.click(screen.getByRole('button', { name: 'Copy full User ID' }));

    expect(mockCopyToClipboard).toHaveBeenCalledWith(claim.userId, 'User ID copied');
  });

  it(
    'BUG: clicking "View Details" only shows a placeholder toast and does nothing else — ' +
      'no modal opens, no claim data is fetched or displayed. This is the same defect ' +
      'documented in the QA Assessment Report (Bug 1) and confirmed live during manual ' +
      'testing; this test proves it at the component level so it cannot silently regress ' +
      'back to "not even a toast" or silently start working without the test being updated.',
    async () => {
      const user = userEvent.setup();
      const claim = buildClaim();
      render(<AdminClaimsTable claims={[claim]} />);

      await user.click(screen.getByRole('button', { name: 'View Details' }));

      expect(toast).toHaveBeenCalledWith('Details view coming soon');
      expect(toast).toHaveBeenCalledTimes(1);

      // Nothing resembling a details view (modal, dialog, expanded panel) exists.
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      expect(screen.queryByText(claim.description)).not.toBeInTheDocument();
      expect(screen.queryByText(claim.incidentLocation)).not.toBeInTheDocument();
    }
  );
});
