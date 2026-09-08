'use client';

import { useClaimRealtimeHandler } from '@/src/features/claims/hooks/use-claim-realtime-handler';

export function RealtimeProvider() {
  useClaimRealtimeHandler();
  return null;
}
