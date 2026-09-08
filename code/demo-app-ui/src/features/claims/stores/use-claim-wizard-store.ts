import { create } from 'zustand';
import type { ClaimFormData } from '../schemas/claim-schemas';
import type { ClaimResponse } from '@/src/lib/api/bff-client';
import { claimsApi } from '@/src/lib/api/bff-client';
import { ResponseError } from '@/src/lib/api/generated/runtime';

type WizardStep = 1 | 2 | 3;

interface ClaimWizardStore {
  step: WizardStep;
  formData: Partial<ClaimFormData>;
  isSubmitting: boolean;
  error: string | null;
  fieldErrors: Record<string, string>;

  setStep: (step: WizardStep) => void;
  nextStep: () => void;
  previousStep: () => void;
  updateFormData: (data: Partial<ClaimFormData>) => void;
  submitClaim: () => Promise<ClaimResponse | null>;
  resetWizard: () => void;
}

const initialState = {
  step: 1 as WizardStep,
  formData: {},
  isSubmitting: false,
  error: null,
  fieldErrors: {},
};

export const useClaimWizardStore = create<ClaimWizardStore>()((set, get) => ({
  ...initialState,

  setStep: (step) => set({ step }),

  nextStep: () =>
    set((state) => ({
      step: Math.min(state.step + 1, 3) as WizardStep,
    })),

  previousStep: () =>
    set((state) => ({
      step: Math.max(state.step - 1, 1) as WizardStep,
    })),

  updateFormData: (data) =>
    set((state) => ({
      formData: { ...state.formData, ...data },
    })),

  submitClaim: async () => {
    const { formData } = get();
    set({ isSubmitting: true, error: null, fieldErrors: {} });

    try {
      const response = await claimsApi.submitClaim({
        submitClaimRequest: {
          incidentDate: formData.incidentDate!,
          incidentLocation: formData.incidentLocation!,
          description: formData.description!,
          claimAmount: formData.claimAmount!,
        },
      });

      set({ isSubmitting: false });
      return response;
    } catch (error) {
      if (error instanceof ResponseError) {
        if (error.response.status === 400) {
          try {
            const body = await error.response.json();
            set({
              isSubmitting: false,
              error: body.detail || 'Validation failed',
              fieldErrors: body.fieldErrors || {},
            });
          } catch {
            set({ isSubmitting: false, error: 'Validation failed' });
          }
          return null;
        }
      }

      set({
        isSubmitting: false,
        error: 'Failed to submit claim. Please try again.',
      });
      return null;
    }
  },

  resetWizard: () => set(initialState),
}));
