import { create } from 'zustand';
import {
  adminDashboardApi,
  type ClaimResponse,
  ClaimStatus,
} from '@/src/lib/api/bff-client';
import { ResponseError } from '@/src/lib/api/generated/runtime';

interface AdminClaimsStore {
  claims: ClaimResponse[];
  isLoading: boolean;
  error: string | null;
  statusFilter: ClaimStatus | null;
  searchQuery: string;

  fetchClaims: (statusFilter?: ClaimStatus | null) => Promise<void>;
  updateClaimStatus: (claimId: string, newStatus: ClaimStatus) => Promise<void>;
  setStatusFilter: (filter: ClaimStatus | null) => void;
  setSearchQuery: (query: string) => void;
  clearFilters: () => void;
  clearError: () => void;
}

export const useAdminClaimsStore = create<AdminClaimsStore>()((set, get) => ({
  claims: [],
  isLoading: false,
  error: null,
  statusFilter: null,
  searchQuery: '',

  fetchClaims: async (statusFilter?: ClaimStatus | null) => {
    set({ isLoading: true, error: null });

    try {
      const filterToUse = statusFilter !== undefined ? statusFilter : get().statusFilter;
      const params: { status?: ClaimStatus } = {};
      if (filterToUse) {
        params.status = filterToUse;
      }

      console.log('Fetching admin claims with params:', params);
      const data = await adminDashboardApi.listAllClaims(params);
      console.log('Admin claims received:', data);

      // Sort by createdAt DESC (newest first) - AC #7
      const sortedClaims = [...data].sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return dateB - dateA; // DESC: newest first
      });

      set({ claims: sortedClaims, isLoading: false });
      console.log('Admin claims store updated successfully');
    } catch (error) {
      console.error('Admin claims fetch error:', error);
      if (error instanceof ResponseError) {
        console.log('ResponseError status:', error.response.status);
        if (error.response.status === 403) {
          set({ error: 'ACCESS_DENIED', isLoading: false });
          return;
        }
        if (error.response.status === 400) {
          const body = await error.response.json().catch(() => null);
          const message = body?.detail || body?.message || 'Invalid request';
          set({ error: message, isLoading: false });
          return;
        }
      }

      set({
        error: 'Failed to load claims',
        isLoading: false,
      });
    }
  },

  updateClaimStatus: async (claimId: string, newStatus: ClaimStatus) => {
    const previousClaims = [...get().claims];

    // Optimistic update
    set({
      claims: get().claims.map((claim) =>
        claim.claimId === claimId ? { ...claim, status: newStatus } : claim
      ),
    });

    try {
      const updatedClaim = await adminDashboardApi.updateClaimStatus({
        claimId,
        updateClaimStatusRequest: { newStatus },
      });

      // Replace with server response
      set({
        claims: get().claims.map((claim) =>
          claim.claimId === claimId ? updatedClaim : claim
        ),
      });
    } catch (error) {
      // Rollback on error
      set({ claims: previousClaims });

      if (error instanceof ResponseError) {
        if (error.response.status === 403) {
          set({ error: 'ACCESS_DENIED' });
          throw error;
        }
        if (error.response.status === 400) {
          const body = await error.response.json().catch(() => null);
          const message = body?.detail || body?.message || 'Invalid status transition';
          set({ error: message });
          throw new Error(message);
        }
      }

      set({ error: 'Failed to update claim status' });
      throw error;
    }
  },

  setStatusFilter: (filter: ClaimStatus | null) => {
    set({ statusFilter: filter });
    get().fetchClaims(filter);
  },

  setSearchQuery: (query: string) => {
    set({ searchQuery: query });
  },

  clearFilters: () => {
    set({ statusFilter: null, searchQuery: '' });
    get().fetchClaims(null);
  },

  clearError: () => set({ error: null }),
}));
