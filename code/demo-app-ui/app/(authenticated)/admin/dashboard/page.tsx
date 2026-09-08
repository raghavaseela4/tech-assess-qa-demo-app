'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { useDashboardStore } from '@/src/features/admin/stores/use-dashboard-store';
import { StatsCard } from '@/src/features/admin/components/stats-card';
import { CacheControls } from '@/src/features/admin/components/cache-controls';
import { PerformanceIndicator } from '@/src/features/admin/components/performance-indicator';

const AdminDashboardPage = () => {
  const router = useRouter();
  const stats = useDashboardStore((state) => state.stats);
  const isLoading = useDashboardStore((state) => state.isLoading);
  const error = useDashboardStore((state) => state.error);
  const lastLoadInfo = useDashboardStore((state) => state.lastLoadInfo);
  const fetchStats = useDashboardStore((state) => state.fetchStats);
  const clearError = useDashboardStore((state) => state.clearError);

  useEffect(() => {
    fetchStats(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Fetch once on mount - fetchStats is stable from Zustand

  useEffect(() => {
    if (error === 'ACCESS_DENIED') {
      toast.error('Access denied');
      router.push('/claims');
    }
  }, [error, router]);

  return (
    <div className="px-4 py-8">
      <div className="mx-auto max-w-7xl">
        {/* Page Header */}
        <div className="mb-8 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-text-primary">Admin Dashboard</h1>
          <button
            onClick={() => fetchStats(false)}
            disabled={isLoading}
            className="rounded-lg p-2 transition-colors hover:bg-surface-card disabled:opacity-50"
            aria-label="Refresh dashboard"
          >
            <svg
              className={`h-6 w-6 text-text-secondary ${isLoading ? 'animate-spin' : ''}`}
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182"
              />
            </svg>
          </button>
        </div>

        {/* Error Banner */}
        {error && error !== 'ACCESS_DENIED' && (
          <div
            className="mb-6 flex items-center justify-between rounded-lg border border-error bg-red-50 p-4 dark:bg-red-900/20"
            role="alert"
          >
            <p className="text-sm text-error">
              Failed to load dashboard. Please try again.
            </p>
            <button
              onClick={() => {
                clearError();
                fetchStats(false);
              }}
              className="text-sm font-medium text-error underline hover:no-underline"
            >
              Retry
            </button>
          </div>
        )}

        {/* Stats Cards Grid - Responsive */}
        <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-3">
          <StatsCard
            title="Total Users"
            value={stats?.totalUsers}
            subtitle="Registered users"
            icon="users"
            isLoading={isLoading}
          />
          <StatsCard
            title="Total Claims"
            value={stats?.totalClaims}
            subtitle="All claims submitted"
            icon="documents"
            isLoading={isLoading}
          />
          <StatsCard
            title="Claims by Status"
            claimsByStatus={stats?.claimsByStatus}
            icon="chart"
            isLoading={isLoading}
          />
        </div>

        {/* Cache Performance Demo Section */}
        <div className="rounded-lg border border-border bg-surface-card p-6">
          <h2 className="mb-4 text-xl font-bold text-text-primary">
            Cache Performance Demo
          </h2>
          <p
            className="mb-6 text-sm text-text-secondary cursor-help"
            title="🎓 Training Feature: This demonstrates Redis caching benefits. Cache hits retrieve data from memory (~100ms) while cache misses query the database (~800ms), showing an ~8x performance improvement."
          >
            🎓 Redis caching demonstration: Cache hits are ~8x faster than database queries
          </p>

          <CacheControls isLoading={isLoading} />

          {lastLoadInfo && (
            <PerformanceIndicator
              cacheHit={lastLoadInfo.cacheHit}
              queryTimeMs={lastLoadInfo.queryTimeMs}
              timestamp={lastLoadInfo.timestamp}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
