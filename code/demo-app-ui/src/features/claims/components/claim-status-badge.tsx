'use client';

import type { ClaimStatus } from '@/src/lib/api/bff-client';

interface ClaimStatusBadgeProps {
  status: ClaimStatus;
}

const statusConfig: Record<
  ClaimStatus,
  { bg: string; text: string; label: string }
> = {
  SUBMITTED: {
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-800 dark:text-blue-200',
    label: 'Submitted',
  },
  UNDER_REVIEW: {
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-800 dark:text-amber-200',
    label: 'Under Review',
  },
  APPROVED: {
    bg: 'bg-green-100 dark:bg-green-900/30',
    text: 'text-green-800 dark:text-green-200',
    label: 'Approved',
  },
  REJECTED: {
    bg: 'bg-red-100 dark:bg-red-900/30',
    text: 'text-red-800 dark:text-red-200',
    label: 'Rejected',
  },
  CLOSED: {
    bg: 'bg-gray-100 dark:bg-gray-900/30',
    text: 'text-gray-800 dark:text-gray-200',
    label: 'Closed',
  },
};

export const ClaimStatusBadge = ({ status }: ClaimStatusBadgeProps) => {
  const config = statusConfig[status];

  // Type safety: handle invalid status gracefully
  if (!config) {
    return (
      <span
        className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1 text-sm font-medium text-gray-800 dark:bg-gray-900/30 dark:text-gray-200"
        role="status"
        aria-label="Claim status: Unknown"
      >
        Unknown
      </span>
    );
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-sm font-medium transition-colors duration-500 ${config.bg} ${config.text}`}
      role="status"
      aria-label={`Claim status: ${config.label}`}
    >
      {config.label}
    </span>
  );
};
