'use client';

import { useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { useAdminClaimsStore } from '@/src/features/admin/stores/use-admin-claims-store';
import { AdminClaimsFilters } from '@/src/features/admin/components/admin-claims-filters';
import { AdminClaimsTable } from '@/src/features/admin/components/admin-claims-table';
import { AdminClaimsCards } from '@/src/features/admin/components/admin-claims-cards';
import { ClaimsListSkeleton } from '@/src/features/claims/components/claims-list-skeleton';
import { AdminClaimsErrorBoundary } from '@/src/features/admin/components/admin-claims-error-boundary';

const AdminClaimsPageContent = () => {
  const router = useRouter();
  const claims = useAdminClaimsStore((state) => state.claims);
  const isLoading = useAdminClaimsStore((state) => state.isLoading);
  const error = useAdminClaimsStore((state) => state.error);
  const searchQuery = useAdminClaimsStore((state) => state.searchQuery);
  const fetchClaims = useAdminClaimsStore((state) => state.fetchClaims);
  const clearError = useAdminClaimsStore((state) => state.clearError);

  useEffect(() => {
    fetchClaims();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Fetch once on mount - fetchClaims is stable from Zustand

  useEffect(() => {
    if (error === 'ACCESS_DENIED') {
      toast.error('Access denied');
      router.push('/claims');
    }
  }, [error, router]);

  const filteredClaims = useMemo(() => {
    if (!searchQuery) return claims;
    return claims.filter((claim) =>
      claim.userId.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [claims, searchQuery]);

  return (
    <div className="px-4 py-8">
      <div className="mx-auto max-w-7xl">
        {/* Header */}
        <div className="mb-6 flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold text-text-primary">Claims Management</h1>
            {!isLoading && (
              <span
                className="rounded-full bg-surface-secondary px-3 py-1 text-sm font-medium text-text-secondary"
                aria-live="polite"
                aria-atomic="true"
              >
                {filteredClaims.length} claims
              </span>
            )}
          </div>
        </div>

        {/* Filters */}
        <AdminClaimsFilters />

        {/* Error Banner */}
        {error && error !== 'ACCESS_DENIED' && (
          <div
            className="mb-6 flex items-center justify-between rounded-lg border border-error bg-red-50 p-4 dark:bg-red-900/20"
            role="alert"
          >
            <p className="text-sm text-error">
              Failed to load claims. Please try again.
            </p>
            <button
              onClick={() => {
                clearError();
                fetchClaims();
              }}
              className="text-sm font-medium text-error underline hover:no-underline"
            >
              Retry
            </button>
          </div>
        )}

        {/* Loading State */}
        {isLoading && <ClaimsListSkeleton />}

        {/* Empty State */}
        {!isLoading && !error && filteredClaims.length === 0 && (
          <div className="flex flex-col items-center justify-center px-4 py-16">
            <div className="mb-6 text-6xl" aria-hidden="true">
              📋
            </div>
            <h2 className="mb-2 text-2xl font-semibold text-text-primary">
              No claims found
            </h2>
            <p className="max-w-md text-center text-text-secondary">
              No claims match your current filters. Try adjusting your search criteria.
            </p>
          </div>
        )}

        {/* Claims List */}
        {!isLoading && !error && filteredClaims.length > 0 && (
          <>
            <div className="hidden md:block">
              <AdminClaimsTable claims={filteredClaims} />
            </div>
            <div className="md:hidden">
              <AdminClaimsCards claims={filteredClaims} />
            </div>
          </>
        )}
      </div>
    </div>
  );
};

const AdminClaimsPage = () => {
  return (
    <AdminClaimsErrorBoundary>
      <AdminClaimsPageContent />
    </AdminClaimsErrorBoundary>
  );
};

export default AdminClaimsPage;
