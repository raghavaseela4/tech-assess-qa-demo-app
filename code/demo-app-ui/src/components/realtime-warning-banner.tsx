'use client';

import { useRealtimeStore } from '@/src/features/claims/stores/use-realtime-store';

export function RealtimeWarningBanner() {
  const connectionError = useRealtimeStore((state) => state.connectionError);

  if (!connectionError) return null;

  return (
    <div
      className="bg-amber-50 border-b border-amber-200 px-4 py-2 text-center text-sm text-amber-800 dark:bg-amber-900/20 dark:border-amber-800 dark:text-amber-200"
      role="alert"
    >
      Real-time updates unavailable. Please refresh page manually.
    </div>
  );
}
