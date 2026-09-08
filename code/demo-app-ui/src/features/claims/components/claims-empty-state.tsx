'use client';

import { useRouter } from 'next/navigation';

export const ClaimsEmptyState = () => {
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center px-4 py-16">
      <div className="mb-6 text-6xl" aria-hidden="true">
        📋
      </div>

      <h2 className="mb-2 text-2xl font-semibold text-text-primary">
        No claims yet
      </h2>

      <p className="mb-6 max-w-md text-center text-text-secondary">
        You haven&apos;t submitted any insurance claims. Start by submitting
        your first claim.
      </p>

      <button
        onClick={() => router.push('/claims/new')}
        className="rounded-lg bg-primary px-6 py-3 font-medium text-white transition-colors hover:bg-primary-hover"
      >
        Submit Your First Claim
      </button>
    </div>
  );
};
