'use client';

interface PerformanceIndicatorProps {
  cacheHit: boolean;
  queryTimeMs: number;
  timestamp: Date;
}

export const PerformanceIndicator = ({
  cacheHit,
  queryTimeMs,
  timestamp,
}: PerformanceIndicatorProps) => {
  return (
    <div className="mt-6 rounded-lg border border-border bg-surface p-4">
      <div className="flex items-center gap-4">
        {cacheHit ? (
          <svg className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
          </svg>
        ) : (
          <svg className="h-8 w-8 text-orange-600" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 6.375c0 2.278-3.694 4.125-8.25 4.125S3.75 8.653 3.75 6.375m16.5 0c0-2.278-3.694-4.125-8.25-4.125S3.75 4.097 3.75 6.375m16.5 0v11.25c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125V6.375m16.5 0v3.75m-16.5-3.75v3.75m16.5 0v3.75C20.25 16.153 16.556 18 12 18s-8.25-1.847-8.25-4.125v-3.75m16.5 0c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125" />
          </svg>
        )}

        <div className="flex-1">
          <p className="text-sm text-text-secondary">Last load:</p>
          <p className="text-lg font-bold">
            {cacheHit ? (
              <>
                <span className="text-green-700 dark:text-green-400">Cached</span>{' '}
                in <span data-testid="query-time" className="text-green-900 dark:text-green-300">{queryTimeMs}ms</span>
              </>
            ) : (
              <>
                <span className="text-orange-700 dark:text-orange-400">Database</span>{' '}
                in <span data-testid="query-time" className="text-orange-900 dark:text-orange-300">{queryTimeMs}ms</span>
              </>
            )}
          </p>
        </div>

        <div>
          {cacheHit ? (
            <span className="rounded-full bg-green-100 px-3 py-1 text-sm font-medium text-green-800 dark:bg-green-900/30 dark:text-green-200">
              Cache Hit ⚡
            </span>
          ) : (
            <span className="rounded-full bg-orange-100 px-3 py-1 text-sm font-medium text-orange-800 dark:bg-orange-900/30 dark:text-orange-200">
              Cache Miss
            </span>
          )}
        </div>
      </div>

      <p className="mt-2 text-xs text-text-muted">
        Updated: {timestamp.toLocaleTimeString()}
      </p>
    </div>
  );
};
