'use client';

import { useCallback, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/src/features/auth/stores/use-auth-store';
import { useClaimsStore } from '../stores/use-claims-store';
import { useAdminClaimsStore } from '@/src/features/admin/stores/use-admin-claims-store';
import { useDashboardStore } from '@/src/features/admin/stores/use-dashboard-store';
import { useRealtimeStore } from '../stores/use-realtime-store';
import {
  useRealtimeClaimUpdates,
  type WebSocketMessage,
} from './use-realtime-claim-updates';
import type { ClaimStatus } from '@/src/lib/api/bff-client';

const VALID_CLAIM_STATUSES: ClaimStatus[] = [
  'SUBMITTED',
  'UNDER_REVIEW',
  'APPROVED',
  'REJECTED',
  'CLOSED',
];

function truncateClaimId(claimId: string): string {
  return claimId.length >= 8 ? claimId.substring(0, 8) : claimId;
}

export function useClaimRealtimeHandler() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const updateClaimantStatus = useClaimsStore((state) => state.updateClaimStatus);
  const fetchAdminClaims = useAdminClaimsStore((state) => state.fetchClaims);
  const fetchStats = useDashboardStore((state) => state.fetchStats);
  const markClaimUpdated = useRealtimeStore((state) => state.markClaimUpdated);

  const handleMessage = useCallback(
    (message: WebSocketMessage) => {
      const isAdmin = user?.role === 'ADMIN';
      console.log('🔄 Processing WebSocket message:', message.type, 'for claim:', message.claimId, 'isAdmin:', isAdmin);

      if (message.type === 'CLAIM_STATUS_CHANGED') {
        // Validate status before type casting (Issue #4: Unsafe Type Casting)
        if (!message.newStatus || !VALID_CLAIM_STATUSES.includes(message.newStatus as ClaimStatus)) {
          console.error('Invalid claim status received:', message.newStatus);
          return;
        }

        console.log('✨ Updating claim status:', message.claimId, '→', message.newStatus);
        updateClaimantStatus(message.claimId, message.newStatus as ClaimStatus);
        markClaimUpdated(message.claimId);

        if (isAdmin) {
          // Issue #5: Add error handling for fetchAdminClaims
          fetchAdminClaims().catch((error) => {
            console.error('Failed to refresh admin claims:', error);
          });

          toast(`Claim ${truncateClaimId(message.claimId)}… status → ${message.newStatus}`, {
            duration: 5000,
            position: 'bottom-right',
          });
        } else {
          toast.success(
            `Your claim #${truncateClaimId(message.claimId)}… status changed to ${message.newStatus}`,
            {
              duration: 5000,
              id: `claim-update-${message.claimId}`,
            }
          );
        }
      }

      if (message.type === 'CLAIM_SUBMITTED') {
        if (isAdmin) {
          // Issue #5: Add error handling for API calls
          Promise.all([
            fetchAdminClaims().catch((error) => {
              console.error('Failed to refresh admin claims:', error);
            }),
            fetchStats().catch((error) => {
              console.error('Failed to refresh stats:', error);
            }),
          ]);

          toast(`New claim submitted: ${truncateClaimId(message.claimId)}…`, {
            duration: 5000,
            position: 'bottom-right',
          });
        }
      }
    },
    [router, user?.role, updateClaimantStatus, fetchAdminClaims, fetchStats, markClaimUpdated]
  );

  const result = useRealtimeClaimUpdates(handleMessage);

  // Sync connection state to the realtime store for ConnectionStatusIndicator
  useEffect(() => {
    useRealtimeStore.getState().setConnectionState(result.connectionState);
  }, [result.connectionState]);

  useEffect(() => {
    useRealtimeStore.getState().setConnectionError(result.connectionError);
  }, [result.connectionError]);

  return result;
}
