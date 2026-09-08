'use client';

import { memo } from 'react';
import toast from 'react-hot-toast';
import { ClaimStatusBadge } from '@/src/features/claims/components/claim-status-badge';
import { useCopyClipboard } from '@/src/features/claims/hooks/use-copy-clipboard';
import {
  formatDate,
  formatCurrency,
} from '@/src/features/claims/utils/formatting';
import type { ClaimResponse } from '@/src/lib/api/bff-client';
import { ClaimStatusSelect } from './claim-status-select';
import { CopyIcon } from './copy-icon';

interface AdminClaimsTableProps {
  claims: ClaimResponse[];
}

const AdminClaimsTableComponent = ({ claims }: AdminClaimsTableProps) => {
  const { copyToClipboard } = useCopyClipboard();

  const handleCopyId = (id: string, label: string) => {
    copyToClipboard(id, `${label} copied`);
  };

  const handleViewDetails = () => {
    toast('Details view coming soon');
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
              User ID
            </th>
            <th className="px-4 py-3 text-left text-sm font-semibold text-text-secondary">
              Incident Date
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
              className="border-b border-border-subtle transition-colors hover:bg-surface-secondary/50"
            >
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  <code className="font-mono text-sm text-text-primary">
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
                    aria-label="Copy full Claim ID"
                    className="text-text-muted transition-colors hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
                    title="Copy full Claim ID"
                  >
                    <CopyIcon />
                  </button>
                </div>
              </td>

              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  <code className="font-mono text-sm text-text-primary">
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
                    aria-label="Copy full User ID"
                    className="text-text-muted transition-colors hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
                    title="Copy full User ID"
                  >
                    <CopyIcon />
                  </button>
                </div>
              </td>

              <td className="px-4 py-3 text-sm text-text-primary">
                {formatDate(claim.incidentDate)}
              </td>

              <td className="px-4 py-3 text-right text-sm font-medium text-text-primary">
                {formatCurrency(claim.claimAmount)}
              </td>

              <td className="px-4 py-3">
                <ClaimStatusBadge status={claim.status} />
              </td>

              <td className="px-4 py-3 text-right">
                <div className="flex items-center justify-end gap-2">
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
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export const AdminClaimsTable = memo(AdminClaimsTableComponent);
