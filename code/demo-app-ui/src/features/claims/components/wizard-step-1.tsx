'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { step1Schema, type Step1FormData } from '../schemas/claim-schemas';
import { useClaimWizardStore } from '../stores/use-claim-wizard-store';

const inputClassName =
  'w-full rounded-lg border border-input-border bg-input-bg px-4 py-2 text-text-primary transition-colors placeholder:text-text-muted focus:border-transparent focus:outline-none focus:ring-2 focus:ring-input-focus';

export function WizardStep1() {
  const { formData, updateFormData, nextStep, fieldErrors } =
    useClaimWizardStore();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid },
  } = useForm<Step1FormData>({
    resolver: zodResolver(step1Schema),
    mode: 'onChange',
    defaultValues: {
      incidentDate: formData.incidentDate,
      incidentLocation: formData.incidentLocation ?? '',
      claimAmount: formData.claimAmount,
    },
  });

  const locationValue = watch('incidentLocation', '');

  const onSubmit = (data: Step1FormData) => {
    updateFormData(data);
    nextStep();
  };

  const todayString = new Date().toISOString().split('T')[0];

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6" noValidate data-testid="wizard-step-1">
      <h2 className="text-2xl font-semibold text-text-primary">
        Incident Details
      </h2>

      <div className="space-y-1">
        <label
          htmlFor="incidentDate"
          className="block text-sm font-medium text-text-secondary"
        >
          When did the incident occur? <span className="text-error">*</span>
        </label>
        <input
          id="incidentDate"
          type="date"
          max={todayString}
          aria-invalid={!!(errors.incidentDate || fieldErrors.incidentDate)}
          aria-describedby={
            errors.incidentDate || fieldErrors.incidentDate
              ? 'incidentDate-error'
              : undefined
          }
          className={inputClassName}
          {...register('incidentDate', { valueAsDate: true })}
        />
        {(errors.incidentDate || fieldErrors.incidentDate) && (
          <p id="incidentDate-error" className="text-sm text-error" role="alert">
            {errors.incidentDate?.message || fieldErrors.incidentDate}
          </p>
        )}
      </div>

      <div className="space-y-1">
        <label
          htmlFor="incidentLocation"
          className="block text-sm font-medium text-text-secondary"
        >
          Where did the incident occur? <span className="text-error">*</span>
        </label>
        <input
          id="incidentLocation"
          type="text"
          placeholder="e.g., 123 Main Street, Springfield"
          minLength={5}
          maxLength={200}
          aria-invalid={!!(errors.incidentLocation || fieldErrors.incidentLocation)}
          aria-describedby={
            errors.incidentLocation || fieldErrors.incidentLocation
              ? 'incidentLocation-error incidentLocation-counter'
              : 'incidentLocation-counter'
          }
          className={inputClassName}
          {...register('incidentLocation')}
        />
        <div className="flex items-center justify-between">
          {(errors.incidentLocation || fieldErrors.incidentLocation) && (
            <p
              id="incidentLocation-error"
              className="text-sm text-error"
              role="alert"
            >
              {errors.incidentLocation?.message || fieldErrors.incidentLocation}
            </p>
          )}
          <p
            id="incidentLocation-counter"
            className={`text-xs ${(errors.incidentLocation || fieldErrors.incidentLocation) ? '' : 'ml-auto'} text-text-muted`}
          >
            {locationValue.length}/200
          </p>
        </div>
      </div>

      <div className="space-y-1">
        <label
          htmlFor="claimAmount"
          className="block text-sm font-medium text-text-secondary"
        >
          Claim Amount <span className="text-error">*</span>
        </label>
        <div className="relative">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary">
            $
          </span>
          <input
            id="claimAmount"
            type="number"
            step="0.01"
            min="0.01"
            max="1000000"
            placeholder="0.00"
            aria-invalid={!!(errors.claimAmount || fieldErrors.claimAmount)}
            aria-describedby={
              errors.claimAmount || fieldErrors.claimAmount
                ? 'claimAmount-error'
                : undefined
            }
            className={`${inputClassName} pl-8`}
            {...register('claimAmount', { valueAsNumber: true })}
          />
        </div>
        {(errors.claimAmount || fieldErrors.claimAmount) && (
          <p id="claimAmount-error" className="text-sm text-error" role="alert">
            {errors.claimAmount?.message || fieldErrors.claimAmount}
          </p>
        )}
        <p className="text-xs text-text-muted">
          Minimum: $0.01 | Maximum: $1,000,000
        </p>
      </div>

      <div className="flex justify-end">
        <button
          type="submit"
          disabled={!isValid}
          className="rounded-lg bg-primary px-6 py-2 font-medium text-on-primary transition-colors hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </form>
  );
}
