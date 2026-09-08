'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useClaimsStore } from '@/src/features/claims/stores/use-claims-store';
import { ClaimsTable } from '@/src/features/claims/components/claims-table';
import { ClaimsCards } from '@/src/features/claims/components/claims-cards';
import { ClaimsEmptyState } from '@/src/features/claims/components/claims-empty-state';
import { ClaimsListSkeleton } from '@/src/features/claims/components/claims-list-skeleton';
import { ClaimDetailModal } from '@/src/features/claims/components/claim-detail-modal';

const ClaimsPage = () => {
  const router = useRouter();
  const [selectedClaimId, setSelectedClaimId] = useState<string | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  const handleViewDetails = useCallback((claimId: string) => {
    setSelectedClaimId(claimId);
    setIsDetailModalOpen(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setIsDetailModalOpen(false);
    setSelectedClaimId(null);
  }, []);
  const claims = useClaimsStore((state) => state.claims);
  const isLoading = useClaimsStore((state) => state.isLoading);
  const error = useClaimsStore((state) => state.error);
  const fetchClaims = useClaimsStore((state) => state.fetchClaims);
  const clearError = useClaimsStore((state) => state.clearError);

  useEffect(() => {
    fetchClaims();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Fetch once on mount - fetchClaims is stable from Zustand

  useEffect(() => {
    if (error === 'UNAUTHORIZED') {
      router.push('/login');
    }
  }, [error, router]);

  return (
    <div className="px-4 py-8">
      <div className="mx-auto max-w-7xl">
        {/* Header */}
        <div className="mb-6 flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
          <h1 className="text-3xl font-bold text-text-primary">My Claims</h1>
          <button
            onClick={() => router.push('/claims/new')}
            className="rounded-lg bg-primary px-6 py-2 font-medium text-white transition-colors hover:bg-primary-hover"
          >
            Submit New Claim
          </button>
        </div>

        {/* Error Banner */}
        {error && error !== 'UNAUTHORIZED' && (
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
        {!isLoading && !error && claims.length === 0 && <ClaimsEmptyState />}

        {/* Claims List */}
        {!isLoading && !error && claims.length > 0 && (
          <>
            <div className="hidden md:block">
              <ClaimsTable claims={claims} onViewDetails={handleViewDetails} />
            </div>
            <div className="md:hidden">
              <ClaimsCards claims={claims} onViewDetails={handleViewDetails} />
            </div>
          </>
        )}
      </div>

      <ClaimDetailModal
        isOpen={isDetailModalOpen}
        claimId={selectedClaimId}
        onClose={handleCloseModal}
      />
    </div>
  );
};

export default ClaimsPage;
