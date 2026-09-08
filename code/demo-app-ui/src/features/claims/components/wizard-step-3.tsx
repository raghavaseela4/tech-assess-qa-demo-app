'use client';

import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { useClaimWizardStore } from '../stores/use-claim-wizard-store';

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
}

function formatDate(date: Date): string {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
}

export function WizardStep3() {
  const router = useRouter();
  const { formData, isSubmitting, error, submitClaim, previousStep, resetWizard } =
    useClaimWizardStore();

  const handleSubmit = async () => {
    const result = await submitClaim();

    if (result) {
      toast.success(
        `Claim submitted successfully! Claim ID: ${result.claimId}`
      );
      resetWizard();
      router.push('/claims');
    } else {
      const currentError = useClaimWizardStore.getState().error;
      toast.error(currentError || 'Failed to submit claim. Please try again.');
    }
  };

  return (
    <div className="space-y-6" data-testid="wizard-step-3">
      <h2 className="text-2xl font-semibold text-text-primary">
        Review Your Claim
      </h2>

      <p className="text-sm text-text-secondary">
        Please review your information before submitting. You can go back to
        edit if needed.
      </p>

      <div className="space-y-4 rounded-lg bg-surface p-6">
        <div>
          <h3 className="mb-1 text-sm font-medium text-text-muted">
            Incident Date
          </h3>
          <p className="text-lg text-text-primary">
            {formData.incidentDate ? formatDate(formData.incidentDate) : 'N/A'}
          </p>
        </div>

        <div>
          <h3 className="mb-1 text-sm font-medium text-text-muted">
            Location
          </h3>
          <p className="text-lg text-text-primary">
            {formData.incidentLocation || 'N/A'}
          </p>
        </div>

        <div>
          <h3 className="mb-1 text-sm font-medium text-text-muted">
            Claim Amount
          </h3>
          <p className="text-lg font-semibold text-text-primary">
            {formData.claimAmount != null
              ? formatCurrency(formData.claimAmount)
              : 'N/A'}
          </p>
        </div>

        <div>
          <h3 className="mb-1 text-sm font-medium text-text-muted">
            Description
          </h3>
          <p className="whitespace-pre-wrap text-base text-text-primary">
            {formData.description || 'N/A'}
          </p>
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-error bg-error-bg p-4">
          <p className="text-sm text-error" role="alert">
            {error}
          </p>
        </div>
      )}

      <div className="flex justify-between">
        <button
          type="button"
          onClick={previousStep}
          disabled={isSubmitting}
          className="rounded-lg bg-button-secondary-bg px-6 py-2 font-medium text-button-secondary-text transition-colors hover:bg-button-secondary-hover disabled:cursor-not-allowed disabled:opacity-50"
        >
          Back
        </button>
        <button
          type="button"
          onClick={handleSubmit}
          disabled={isSubmitting}
          className="flex items-center gap-2 rounded-lg bg-primary px-8 py-3 text-lg font-semibold text-on-primary transition-colors hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting ? (
            <>
              <svg
                className="h-5 w-5 animate-spin"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                  fill="none"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
              Submitting...
            </>
          ) : (
            'Submit Claim'
          )}
        </button>
      </div>
    </div>
  );
}
