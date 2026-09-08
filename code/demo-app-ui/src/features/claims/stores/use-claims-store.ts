import { create } from 'zustand';
import {
  claimsApi,
  type ClaimSummaryResponse,
  type ClaimStatus,
} from '@/src/lib/api/bff-client';
import { ResponseError } from '@/src/lib/api/generated/runtime';

interface ClaimsStore {
  claims: ClaimSummaryResponse[];
  isLoading: boolean;
  error: string | null;

  fetchClaims: () => Promise<void>;
  addClaim: (claim: ClaimSummaryResponse) => void;
  updateClaimStatus: (claimId: string, status: ClaimStatus) => void;
  clearError: () => void;
}

export const useClaimsStore = create<ClaimsStore>()((set) => ({
  claims: [],
  isLoading: false,
  error: null,

  fetchClaims: async () => {
    set({ isLoading: true, error: null });

    try {
      const claims = await claimsApi.listClaims();

      const sortedClaims = claims.sort(
        (a, b) => b.createdAt.getTime() - a.createdAt.getTime()
      );

      set({ claims: sortedClaims, isLoading: false });
    } catch (error) {
      if (error instanceof ResponseError && error.response.status === 401) {
        set({ isLoading: false, error: 'UNAUTHORIZED' });
        return;
      }

      set({
        isLoading: false,
        error: 'Failed to fetch claims',
      });
    }
  },

  addClaim: (claim) =>
    set((state) => ({
      claims: [claim, ...state.claims],
    })),

  updateClaimStatus: (claimId, status) =>
    set((state) => ({
      claims: state.claims.map((claim) =>
        claim.claimId === claimId ? { ...claim, status } : claim
      ),
    })),

  clearError: () => set({ error: null }),
}));
