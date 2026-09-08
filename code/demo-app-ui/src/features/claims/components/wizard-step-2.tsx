'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { step2Schema, type Step2FormData } from '../schemas/claim-schemas';
import { useClaimWizardStore } from '../stores/use-claim-wizard-store';

export function WizardStep2() {
  const { formData, updateFormData, nextStep, previousStep, fieldErrors } =
    useClaimWizardStore();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid },
  } = useForm<Step2FormData>({
    resolver: zodResolver(step2Schema),
    mode: 'onChange',
    defaultValues: {
      description: formData.description ?? '',
    },
  });

  const descriptionValue = watch('description', '');
  const characterCount = descriptionValue.length;

  const onSubmit = (data: Step2FormData) => {
    updateFormData(data);
    nextStep();
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6" noValidate data-testid="wizard-step-2">
      <h2 className="text-2xl font-semibold text-text-primary">
        Supporting Details
      </h2>

      <div className="space-y-1">
        <label
          htmlFor="description"
          className="block text-sm font-medium text-text-secondary"
        >
          Describe what happened <span className="text-error">*</span>
        </label>
        <p className="mb-2 text-sm text-text-muted">
          Be as detailed as possible. Include what happened, when, and any
          relevant circumstances.
        </p>
        <textarea
          id="description"
          rows={8}
          minLength={10}
          maxLength={1000}
          placeholder="Describe the incident in detail..."
          aria-invalid={!!(errors.description || fieldErrors.description)}
          aria-describedby={
            errors.description || fieldErrors.description
              ? 'description-error description-counter'
              : 'description-counter'
          }
          className="w-full resize-none rounded-lg border border-input-border bg-input-bg px-4 py-2 text-text-primary transition-colors placeholder:text-text-muted focus:border-transparent focus:outline-none focus:ring-2 focus:ring-input-focus"
          {...register('description')}
        />
        <div className="flex items-center justify-between">
          {(errors.description || fieldErrors.description) && (
            <p id="description-error" className="text-sm text-error" role="alert">
              {errors.description?.message || fieldErrors.description}
            </p>
          )}
          <p
            id="description-counter"
            className={`ml-auto text-xs ${
              characterCount < 10
                ? 'text-error'
                : characterCount > 900
                  ? 'text-primary'
                  : 'text-text-muted'
            }`}
          >
            {characterCount}/1000
          </p>
        </div>
      </div>

      <div className="flex justify-between">
        <button
          type="button"
          onClick={previousStep}
          className="rounded-lg bg-button-secondary-bg px-6 py-2 font-medium text-button-secondary-text transition-colors hover:bg-button-secondary-hover"
        >
          Back
        </button>
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
