'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { ErrorBoundary } from '@/src/components/error-boundary';
import { useClaimWizardStore } from '@/src/features/claims/stores/use-claim-wizard-store';
import { ProgressIndicator } from '@/src/features/claims/components/progress-indicator';
import { WizardStep1 } from '@/src/features/claims/components/wizard-step-1';
import { WizardStep2 } from '@/src/features/claims/components/wizard-step-2';
import { WizardStep3 } from '@/src/features/claims/components/wizard-step-3';

export default function NewClaimPage() {
  const router = useRouter();
  const { step, formData, resetWizard } = useClaimWizardStore();
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);

  const handleCancel = () => {
    const hasData = Object.values(formData).some(
      (value) => value !== undefined && value !== '' && value !== null
    );

    if (hasData) {
      setShowCancelConfirm(true);
    } else {
      resetWizard();
      router.push('/claims');
    }
  };

  const confirmCancel = () => {
    setShowCancelConfirm(false);
    resetWizard();
    router.push('/claims');
  };

  return (
    <div className="px-4 py-8">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-text-primary">
            Submit New Claim
          </h1>
          <button
            type="button"
            onClick={handleCancel}
            className="text-sm text-text-secondary underline hover:text-text-primary"
          >
            Cancel
          </button>
        </div>

        <ProgressIndicator currentStep={step} />

        <ErrorBoundary>
          <div className="rounded-xl border border-border bg-surface-card p-8 shadow-sm">
            {step === 1 && <WizardStep1 />}
            {step === 2 && <WizardStep2 />}
            {step === 3 && <WizardStep3 />}
          </div>
        </ErrorBoundary>
      </div>

      {showCancelConfirm && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
          role="dialog"
          aria-modal="true"
          aria-labelledby="cancel-dialog-title"
        >
          <div className="mx-4 max-w-sm rounded-lg bg-surface-card p-6 shadow-lg">
            <h2
              id="cancel-dialog-title"
              className="mb-2 text-lg font-semibold text-text-primary"
            >
              Cancel Claim Submission?
            </h2>
            <p className="mb-4 text-sm text-text-secondary">
              Are you sure? Your progress will be lost.
            </p>
            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowCancelConfirm(false)}
                className="rounded-lg bg-button-secondary-bg px-4 py-2 text-sm font-medium text-button-secondary-text transition-colors hover:bg-button-secondary-hover"
              >
                Keep Editing
              </button>
              <button
                type="button"
                onClick={confirmCancel}
                className="rounded-lg bg-error px-4 py-2 text-sm font-medium text-white transition-colors hover:opacity-90"
              >
                Discard & Leave
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
