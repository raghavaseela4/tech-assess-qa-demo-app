'use client';

import { ClaimStatusBadge } from './claim-status-badge';
import { useCopyClipboard } from '../hooks/use-copy-clipboard';
import {
  formatDate,
  formatCurrency,
  truncateDescription,
} from '../utils/formatting';
import type { ClaimSummaryResponse } from '@/src/lib/api/bff-client';
import { useRealtimeStore } from '../stores/use-realtime-store';

interface ClaimsTableProps {
  claims: ClaimSummaryResponse[];
  onViewDetails: (claimId: string) => void;
}

export const ClaimsTable = ({ claims, onViewDetails }: ClaimsTableProps) => {
  const { copyToClipboard } = useCopyClipboard();
  const recentlyUpdatedClaimIds = useRealtimeStore((state) => state.recentlyUpdatedClaimIds);

  const handleCopyClaimId = (claimId: string) => {
    copyToClipboard(claimId, 'Claim ID copied');
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b border-border-subtle bg-surface-secondary">
            <th className="px-4 py-3 text-left text-sm font-semibold text-text-secondary">
              Claim ID
            </th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-text-secondary">
              Date
            </th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-text-secondary">
              Description
            </th>
            <th className="px-4 py-3 text-right text-sm font-semibold text-text-secondary">
              Amount
            </th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-text-secondary">
              Status
            </th>
            <th className="px-4 py-3 text-right text-sm font-semibold text-text-secondary">
              Actions
            </th>
          </tr>
        </thead>
        <tbody>
          {claims.map((claim) => (
            <tr
              key={claim.claimId}
              className={`border-b border-border-subtle transition-colors hover:bg-surface-secondary/50 ${recentlyUpdatedClaimIds.has(claim.claimId) ? 'bg-yellow-100 dark:bg-yellow-900/30' : ''}`}
            >
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  <code className="font-mono text-sm text-text-primary">
                    {claim.claimId.substring(0, 8)}
                  </code>
                  <button
                    onClick={() => handleCopyClaimId(claim.claimId)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        handleCopyClaimId(claim.claimId);
                      }
                    }}
                    aria-label="Copy full Claim ID"
                    className="text-text-muted transition-colors hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
                    title="Copy full Claim ID"
                  >
                    📋
                  </button>
                </div>
              </td>

              <td className="px-4 py-3 text-sm text-text-primary">
                {formatDate(claim.incidentDate)}
              </td>

              <td className="max-w-xs px-4 py-3 text-sm text-text-primary">
                <span title={claim.description}>
                  {truncateDescription(claim.description)}
                </span>
              </td>

              <td className="px-4 py-3 text-right text-sm font-medium text-text-primary">
                {formatCurrency(claim.claimAmount)}
              </td>

              <td className="px-4 py-3">
                <ClaimStatusBadge status={claim.status} />
              </td>

              <td className="px-4 py-3 text-right">
                <button
                  onClick={() => onViewDetails(claim.claimId)}
                  data-testid="view-details-button"
                  className="text-sm font-medium text-link underline hover:text-link-hover"
                >
                  View Details
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
