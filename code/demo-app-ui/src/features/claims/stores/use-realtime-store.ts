import { create } from 'zustand';
import type { WebSocketMessage } from '../hooks/use-realtime-claim-updates';

type ConnectionState = 'connected' | 'disconnected' | 'reconnecting';

interface RealtimeStore {
  connectionState: ConnectionState;
  connectionError: boolean;
  lastMessage: WebSocketMessage | null;
  recentlyUpdatedClaimIds: Set<string>;
  highlightTimeouts: Map<string, ReturnType<typeof setTimeout>>;

  setConnectionState: (state: ConnectionState) => void;
  setConnectionError: (error: boolean) => void;
  setLastMessage: (message: WebSocketMessage | null) => void;
  markClaimUpdated: (claimId: string) => void;
  clearClaimUpdated: (claimId: string) => void;
  clearAllTimeouts: () => void;
}

export const useRealtimeStore = create<RealtimeStore>()((set, get) => ({
  connectionState: 'disconnected',
  connectionError: false,
  lastMessage: null,
  recentlyUpdatedClaimIds: new Set(),
  highlightTimeouts: new Map(),

  setConnectionState: (connectionState) => set({ connectionState }),
  setConnectionError: (connectionError) => set({ connectionError }),
  setLastMessage: (lastMessage) => set({ lastMessage }),

  markClaimUpdated: (claimId) => {
    // Issue #2: Fix memory leak by tracking timeout IDs
    const state = get();

    // Clear existing timeout for this claimId if it exists
    const existingTimeout = state.highlightTimeouts.get(claimId);
    if (existingTimeout) {
      clearTimeout(existingTimeout);
    }

    const updated = new Set(state.recentlyUpdatedClaimIds);
    updated.add(claimId);

    // Auto-clear after 2 seconds and track timeout ID
    const timeoutId = setTimeout(() => {
      const current = new Set(get().recentlyUpdatedClaimIds);
      current.delete(claimId);

      const timeouts = new Map(get().highlightTimeouts);
      timeouts.delete(claimId);

      set({
        recentlyUpdatedClaimIds: current,
        highlightTimeouts: timeouts,
      });
    }, 2000);

    const timeouts = new Map(state.highlightTimeouts);
    timeouts.set(claimId, timeoutId);

    set({
      recentlyUpdatedClaimIds: updated,
      highlightTimeouts: timeouts,
    });
  },

  clearClaimUpdated: (claimId) => {
    const state = get();

    // Clear timeout if it exists
    const timeoutId = state.highlightTimeouts.get(claimId);
    if (timeoutId) {
      clearTimeout(timeoutId);
    }

    const updated = new Set(state.recentlyUpdatedClaimIds);
    updated.delete(claimId);

    const timeouts = new Map(state.highlightTimeouts);
    timeouts.delete(claimId);

    set({
      recentlyUpdatedClaimIds: updated,
      highlightTimeouts: timeouts,
    });
  },

  clearAllTimeouts: () => {
    const state = get();
    state.highlightTimeouts.forEach((timeoutId) => clearTimeout(timeoutId));
    set({
      highlightTimeouts: new Map(),
      recentlyUpdatedClaimIds: new Set(),
    });
  },
}));
