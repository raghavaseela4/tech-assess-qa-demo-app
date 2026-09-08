'use client';

import { useRealtimeStore } from '@/src/features/claims/stores/use-realtime-store';

export function ConnectionStatusIndicator() {
  const connectionState = useRealtimeStore((state) => state.connectionState);
  const connectionError = useRealtimeStore((state) => state.connectionError);

  const config = connectionError
    ? { dotColor: 'bg-amber-500', label: 'Offline', title: 'Real-time updates unavailable. Max reconnection attempts reached.' }
    : connectionState === 'connected'
      ? { dotColor: 'bg-green-500', label: 'Live', title: 'Connected to real-time updates' }
      : connectionState === 'reconnecting'
        ? { dotColor: 'bg-amber-400 animate-pulse', label: 'Reconnecting...', title: 'Attempting to reconnect to real-time updates' }
        : { dotColor: 'bg-gray-400', label: 'Offline', title: 'Not connected to real-time updates' };

  return (
    <div
      className="flex items-center gap-1.5"
      title={config.title}
      role="status"
      aria-label={`Connection status: ${config.label}`}
    >
      <span
        className={`inline-block h-2 w-2 rounded-full ${config.dotColor}`}
        aria-hidden="true"
      />
      <span className="text-xs text-text-secondary">{config.label}</span>
    </div>
  );
}
