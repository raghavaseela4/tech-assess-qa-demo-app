import { ClaimStatus } from '@/src/lib/api/bff-client';

export const VALID_TRANSITIONS: Record<ClaimStatus, ClaimStatus[]> = {
  SUBMITTED: ['UNDER_REVIEW', 'REJECTED'],
  UNDER_REVIEW: ['APPROVED', 'REJECTED', 'SUBMITTED'],
  APPROVED: ['CLOSED'],
  REJECTED: ['CLOSED'],
  CLOSED: [],
};

export const getValidTransitions = (currentStatus: ClaimStatus): ClaimStatus[] => {
  return VALID_TRANSITIONS[currentStatus] ?? [];
};

export const STATUS_LABELS: Record<ClaimStatus, string> = {
  SUBMITTED: 'Submitted',
  UNDER_REVIEW: 'Under Review',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  CLOSED: 'Closed',
};
