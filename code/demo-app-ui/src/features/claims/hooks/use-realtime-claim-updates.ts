'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { useAuthStore } from '@/src/features/auth/stores/use-auth-store';

export interface WebSocketMessage {
  type: 'CLAIM_STATUS_CHANGED' | 'CLAIM_SUBMITTED';
  claimId: string;
  newStatus?: string;
  oldStatus?: string;
  timestamp: string;
  correlationId: string;
}

type ConnectionState = 'connected' | 'disconnected' | 'reconnecting';

interface UseRealtimeClaimUpdatesResult {
  isConnected: boolean;
  connectionState: ConnectionState;
  lastMessage: WebSocketMessage | null;
  connectionError: boolean;
}

const MAX_RETRIES = 5;
const BASE_DELAY_MS = 1000;

function getWebSocketUrl(): string {
  const bffUrl = process.env.NEXT_PUBLIC_BFF_URL || 'http://localhost:8090';
  const wsProtocol = bffUrl.startsWith('https') ? 'wss' : 'ws';
  const host = bffUrl.replace(/^https?:\/\//, '');
  return `${wsProtocol}://${host}/api/ws/claims`;
}

export function useRealtimeClaimUpdates(
  onMessage?: (message: WebSocketMessage) => void
): UseRealtimeClaimUpdatesResult {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [connectionState, setConnectionState] = useState<ConnectionState>('disconnected');
  const [lastMessage, setLastMessage] = useState<WebSocketMessage | null>(null);
  const [connectionError, setConnectionError] = useState(false);

  const websocketRef = useRef<WebSocket | null>(null);
  const retryCountRef = useRef(0);
  const retryTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onMessageRef = useRef(onMessage);
  onMessageRef.current = onMessage;

  const cleanup = useCallback(() => {
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
    if (websocketRef.current) {
      websocketRef.current.close();
      websocketRef.current = null;
    }
  }, []);

  const connect = useCallback(() => {
    if (!isAuthenticated) return;

    cleanup();
    const url = getWebSocketUrl();

    try {
      const websocket = new WebSocket(url);
      websocketRef.current = websocket;

      websocket.onopen = () => {
        console.log('✅ WebSocket connected successfully');
        retryCountRef.current = 0;
        setConnectionState('connected');
        setConnectionError(false);
      };

      websocket.onmessage = (event: MessageEvent) => {
        try {
          const message = JSON.parse(event.data as string) as WebSocketMessage;
          console.log('📨 WebSocket message received:', message);
          setLastMessage(message);
          onMessageRef.current?.(message);
        } catch (error) {
          // Issue #3: Log parse errors for debugging (non-JSON messages like pings are expected)
          if (typeof event.data === 'string' && !event.data.startsWith('ping')) {
            console.error('WebSocket message parse error:', error, 'Raw data:', event.data);
          }
        }
      };

      websocket.onclose = () => {
        websocketRef.current = null;

        if (retryCountRef.current < MAX_RETRIES) {
          setConnectionState('reconnecting');
          const delay = Math.min(BASE_DELAY_MS * Math.pow(2, retryCountRef.current), 16000);
          retryCountRef.current += 1;
          retryTimeoutRef.current = setTimeout(connect, delay);
        } else {
          setConnectionState('disconnected');
          setConnectionError(true);
        }
      };

      websocket.onerror = (error) => {
        // Issue #3: Log WebSocket errors for debugging
        console.error('WebSocket error occurred:', error);
        // onclose will fire after onerror, reconnect logic handled there
      };
    } catch (error) {
      // Issue #3: Log connection errors for debugging
      console.error('WebSocket connection error:', error);
      setConnectionState('disconnected');
      setConnectionError(true);
    }
  }, [isAuthenticated, cleanup]);

  useEffect(() => {
    if (isAuthenticated) {
      connect();
    }

    return cleanup;
  }, [isAuthenticated, connect, cleanup]);

  return {
    isConnected: connectionState === 'connected',
    connectionState,
    lastMessage,
    connectionError,
  };
}
