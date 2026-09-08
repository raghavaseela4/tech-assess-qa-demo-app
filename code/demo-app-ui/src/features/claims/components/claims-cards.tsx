'use client';

import { ClaimStatusBadge } from './claim-status-badge';
import { useCopyClipboard } from '../hooks/use-copy-clipboard';
import { formatDate, formatCurrency } from '../utils/formatting';
import type { ClaimSummaryResponse } from '@/src/lib/api/bff-client';
import { useRealtimeStore } from '../stores/use-realtime-store';

interface ClaimsCardsProps {
  claims: ClaimSummaryResponse[];
  onViewDetails: (claimId: string) => void;
}

export const ClaimsCards = ({ claims, onViewDetails }: ClaimsCardsProps) => {
  const { copyToClipboard } = useCopyClipboard();
  const recentlyUpdatedClaimIds = useRealtimeStore((state) => state.recentlyUpdatedClaimIds);

  const handleCopyClaimId = (claimId: string) => {
    copyToClipboard(claimId, 'Claim ID copied');
  };

  return (
    <div className="space-y-4">
      {claims.map((claim) => (
        <div
          key={claim.claimId}
          className={`rounded-lg border border-border bg-surface-card p-4 transition-colors hover:border-primary/50 ${recentlyUpdatedClaimIds.has(claim.claimId) ? 'bg-yellow-100 dark:bg-yellow-900/30' : ''}`}
        >
          <div className="mb-3 flex items-start justify-between">
            <div className="flex items-center gap-2">
              <code className="font-mono text-xs text-text-muted">
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
                aria-label="Copy Claim ID"
                className="text-text-muted hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
              >
                📋
              </button>
            </div>
            <ClaimStatusBadge status={claim.status} />
          </div>

          <div className="mb-2 text-sm text-text-secondary">
            {formatDate(claim.incidentDate)}
          </div>

          <p className="mb-3 line-clamp-2 text-sm text-text-primary">
            {claim.description}
          </p>

          <div className="flex items-center justify-between">
            <span className="text-lg font-semibold text-text-primary">
              {formatCurrency(claim.claimAmount)}
            </span>
            <button
              onClick={() => onViewDetails(claim.claimId)}
              data-testid="view-details-button"
              className="text-sm font-medium text-link underline hover:text-link-hover"
            >
              View Details
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};
