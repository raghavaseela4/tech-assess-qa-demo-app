import { create } from 'zustand';
import {
  adminDashboardApi,
  type DashboardStatsResponse,
} from '@/src/lib/api/bff-client';
import { ResponseError } from '@/src/lib/api/generated/runtime';

export interface LoadInfo {
  cacheHit: boolean;
  queryTimeMs: number;
  timestamp: Date;
}

interface DashboardStore {
  stats: DashboardStatsResponse | null;
  isLoading: boolean;
  error: string | null;
  lastLoadInfo: LoadInfo | null;

  fetchStats: (bypassCache?: boolean) => Promise<void>;
  flushCache: () => Promise<void>;
  clearError: () => void;
}

export const useDashboardStore = create<DashboardStore>()((set, get) => ({
  stats: null,
  isLoading: false,
  error: null,
  lastLoadInfo: null,

  fetchStats: async (bypassCache = false) => {
    set({ isLoading: true, error: null });

    try {
      console.log('Fetching dashboard stats...');
      const data = await adminDashboardApi.getDashboardStats({ bypassCache });
      console.log('Dashboard stats received:', data);

      set({
        stats: data,
        lastLoadInfo: {
          cacheHit: data.cacheHit,
          queryTimeMs: data.queryTimeMs,
          timestamp: new Date(),
        },
        isLoading: false,
      });
      console.log('Dashboard store updated successfully');
    } catch (error) {
      console.error('Dashboard fetch error:', error);
      if (error instanceof ResponseError) {
        console.log('ResponseError status:', error.response.status);
        if (error.response.status === 403) {
          set({ error: 'ACCESS_DENIED', isLoading: false });
          return;
        }
      }

      set({
        error: 'Failed to load dashboard stats',
        isLoading: false,
      });
    }
  },

  flushCache: async () => {
    set({ isLoading: true, error: null });

    try {
      await adminDashboardApi.flushDashboardCache();
      await get().fetchStats(true);
    } catch (error) {
      if (error instanceof ResponseError) {
        if (error.response.status === 403) {
          set({ error: 'ACCESS_DENIED', isLoading: false });
          return;
        }
      }

      set({
        error: 'Failed to flush cache',
        isLoading: false,
      });
    }
  },

  clearError: () => set({ error: null }),
}));
