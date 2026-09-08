'use client';

import { useDashboardStore } from '@/src/features/admin/stores/use-dashboard-store';
import { useState, useRef } from 'react';

interface CacheControlsProps {
  isLoading: boolean;
}

const LoadingSpinner = ({ className }: { className?: string }) => (
  <svg
    className={`h-5 w-5 animate-spin ${className ?? ''}`}
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    viewBox="0 0 24 24"
    aria-hidden="true"
  >
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
    <path
      className="opacity-75"
      fill="currentColor"
      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
    />
  </svg>
);

export const CacheControls = ({ isLoading }: CacheControlsProps) => {
  const fetchStats = useDashboardStore((state) => state.fetchStats);
  const flushCache = useDashboardStore((state) => state.flushCache);
  const [buttonError, setButtonError] = useState<string | null>(null);
  const flushTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const handleLoadFromCache = async () => {
    setButtonError(null);
    try {
      await fetchStats(false);
    } catch (error) {
      setButtonError('Failed to load stats');
      console.error('Failed to load from cache:', error);
    }
  };

  const handleFlushAndReload = async () => {
    // Debounce: prevent rapid flush spam (DOS protection)
    if (flushTimeoutRef.current) {
      return; // Ignore if already flushing
    }

    setButtonError(null);
    flushTimeoutRef.current = setTimeout(() => {
      flushTimeoutRef.current = null;
    }, 3000); // 3 second debounce

    try {
      await flushCache();
    } catch (error) {
      setButtonError('Failed to flush cache');
      console.error('Failed to flush cache:', error);
      if (flushTimeoutRef.current) {
        clearTimeout(flushTimeoutRef.current);
        flushTimeoutRef.current = null;
      }
    }
  };

  return (
    <div className="mb-6">
      {buttonError && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800 dark:border-red-800 dark:bg-red-900/20 dark:text-red-200">
          {buttonError}
        </div>
      )}
      <div className="flex flex-col gap-4 sm:flex-row">
        <button
        onClick={handleLoadFromCache}
        disabled={isLoading}
        className="flex-1 rounded-lg bg-primary px-6 py-3 font-medium text-on-primary transition-colors hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-50"
        aria-label="Load dashboard stats from cache"
      >
        {isLoading ? (
          <span className="flex items-center justify-center">
            <LoadingSpinner className="mr-2" />
            Loading...
          </span>
        ) : (
          'Load from Cache'
        )}
      </button>

      <button
        onClick={handleFlushAndReload}
        disabled={isLoading}
        className="flex-1 rounded-lg border-2 border-red-600 bg-surface-card px-6 py-3 font-medium text-red-600 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50 dark:hover:bg-red-900/20"
        aria-label="Flush cache and reload dashboard stats from database"
      >
        {isLoading ? (
          <span className="flex items-center justify-center">
            <LoadingSpinner className="mr-2" />
            Flushing...
          </span>
        ) : (
          'Flush Cache & Reload'
        )}
      </button>
      </div>
    </div>
  );
};
