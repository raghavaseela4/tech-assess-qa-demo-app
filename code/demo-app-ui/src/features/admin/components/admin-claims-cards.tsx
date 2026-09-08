'use client';

import { memo } from 'react';
import toast from 'react-hot-toast';
import { ClaimStatusBadge } from '@/src/features/claims/components/claim-status-badge';
import { useCopyClipboard } from '@/src/features/claims/hooks/use-copy-clipboard';
import { formatDate, formatCurrency } from '@/src/features/claims/utils/formatting';
import type { ClaimResponse } from '@/src/lib/api/bff-client';
import { ClaimStatusSelect } from './claim-status-select';
import { CopyIcon } from './copy-icon';

interface AdminClaimsCardsProps {
  claims: ClaimResponse[];
}

const AdminClaimsCardsComponent = ({ claims }: AdminClaimsCardsProps) => {
  const { copyToClipboard } = useCopyClipboard();

  const handleCopyId = (id: string, label: string) => {
    copyToClipboard(id, `${label} copied`);
  };

  const handleViewDetails = () => {
    toast('Details view coming soon');
  };

  return (
    <div className="space-y-4">
      {claims.map((claim) => (
        <div
          key={claim.claimId}
          className="rounded-lg border border-border bg-surface-card p-4 transition-colors hover:border-primary/50"
        >
          <div className="mb-3 flex items-start justify-between">
            <div>
              <div className="mb-1 flex items-center gap-2">
                <code className="font-mono text-xs text-text-muted">
                  {claim.claimId.substring(0, 8)}
                </code>
                <button
                  onClick={() => handleCopyId(claim.claimId, 'Claim ID')}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      handleCopyId(claim.claimId, 'Claim ID');
                    }
                  }}
                  aria-label="Copy Claim ID"
                  className="text-text-muted hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
                >
                  <CopyIcon />
                </button>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs text-text-muted">User:</span>
                <code className="font-mono text-xs text-text-muted">
                  {claim.userId.substring(0, 8)}
                </code>
                <button
                  onClick={() => handleCopyId(claim.userId, 'User ID')}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      handleCopyId(claim.userId, 'User ID');
                    }
                  }}
                  aria-label="Copy User ID"
                  className="text-text-muted hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
                >
                  <CopyIcon />
                </button>
              </div>
            </div>
            <ClaimStatusBadge status={claim.status} />
          </div>

          <div className="mb-2 text-sm text-text-secondary">
            {formatDate(claim.incidentDate)}
          </div>

          <div className="flex items-center justify-between">
            <span className="text-lg font-semibold text-text-primary">
              {formatCurrency(claim.claimAmount)}
            </span>
            <div className="flex items-center gap-2">
              <ClaimStatusSelect
                claimId={claim.claimId}
                currentStatus={claim.status}
              />
              <button
                onClick={handleViewDetails}
                className="text-sm font-medium text-link underline hover:text-link-hover"
              >
                View Details
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export const AdminClaimsCards = memo(AdminClaimsCardsComponent);
