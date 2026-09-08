'use client';

import { useState } from 'react';
import toast from 'react-hot-toast';
import { ClaimStatus } from '@/src/lib/api/bff-client';
import { getValidTransitions, STATUS_LABELS } from '../utils/claim-status-transitions';
import { useAdminClaimsStore } from '../stores/use-admin-claims-store';
import { ConfirmationDialog } from './confirmation-dialog';

interface ClaimStatusSelectProps {
  claimId: string;
  currentStatus: ClaimStatus;
}

export const ClaimStatusSelect = ({ claimId, currentStatus }: ClaimStatusSelectProps) => {
  const updateClaimStatus = useAdminClaimsStore((state) => state.updateClaimStatus);
  const [isUpdating, setIsUpdating] = useState(false);
  const [pendingStatus, setPendingStatus] = useState<ClaimStatus | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const validTransitions = getValidTransitions(currentStatus);

  if (validTransitions.length === 0) {
    return (
      <span className="text-xs text-text-muted" aria-label="No status transitions available">
        --
      </span>
    );
  }

  const handleChange = async (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newStatus = event.target.value as ClaimStatus;
    if (!newStatus) return;

    setPendingStatus(newStatus);
    setShowConfirmation(true);
    event.target.value = ''; // Reset select
  };

  const handleConfirm = async () => {
    if (!pendingStatus) return;

    setShowConfirmation(false);
    setIsUpdating(true);
    try {
      await updateClaimStatus(claimId, pendingStatus);
      toast.success(`Claim status updated to ${STATUS_LABELS[pendingStatus]}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to update status';
      toast.error(message);
    } finally {
      setIsUpdating(false);
      setPendingStatus(null);
    }
  };

  const handleCancel = () => {
    setShowConfirmation(false);
    setPendingStatus(null);
  };

  return (
    <>
      <select
        onChange={handleChange}
        disabled={isUpdating}
        value=""
        className="rounded border border-border-subtle bg-surface-secondary px-2 py-1 text-xs text-text-primary focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50"
        aria-label={`Update status for claim ${claimId.substring(0, 8)}`}
        data-testid={`status-select-${claimId.substring(0, 8)}`}
      >
        <option value="" disabled>
          Update Status
        </option>
        {validTransitions.map((status) => (
          <option key={status} value={status}>
            {STATUS_LABELS[status]}
          </option>
        ))}
      </select>

      <ConfirmationDialog
        isOpen={showConfirmation}
        title="Confirm Status Change"
        message={pendingStatus ? `Change status to ${STATUS_LABELS[pendingStatus]}?` : ''}
        confirmLabel="Confirm"
        cancelLabel="Cancel"
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    </>
  );
};
