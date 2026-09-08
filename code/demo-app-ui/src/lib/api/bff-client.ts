/**
 * BFF API Client - Wrapper around generated OpenAPI client
 *
 * This module provides a configured instance of the generated API clients
 * for use throughout the frontend application.
 */

import {
  Configuration,
  AuthenticationApi,
  ClaimsApi,
  AdminDashboardApi,
} from './generated';

function resolveBffBasePath(): string {
  if (process.env.USE_WIREMOCK_BFF === 'true') {
    return process.env.NEXT_PUBLIC_BFF_STUB_URL || 'http://localhost:8080';
  }
  return process.env.NEXT_PUBLIC_BFF_URL || 'http://localhost:8080';
}

/**
 * Get CSRF token from cookies
 */
function getCsrfToken(): string | null {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Middleware to add CSRF token to requests
 */
const csrfMiddleware = {
  pre: async (context: any) => {
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      context.init.headers = {
        ...context.init.headers,
        'X-XSRF-TOKEN': csrfToken,
      };
    }
    return context;
  },
};

/**
 * Custom fetch wrapper to handle 401 Unauthorized responses (token expiry)
 * Tries to refresh the token first, then logs out if refresh fails
 *
 * This runs at the fetch level, before the OpenAPI client processes responses,
 * so it doesn't interfere with response parsing.
 */
let isRefreshing = false;
let refreshPromise: Promise<boolean> | null = null;

async function fetchWithAuth(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  let response = await fetch(input, init);

  // Only handle 401 errors
  if (response.status === 401 && typeof window !== 'undefined') {
    try {
      // If already refreshing, wait for that to complete
      if (isRefreshing && refreshPromise) {
        const refreshed = await refreshPromise;
        if (refreshed) {
          // Retry the original request
          response = await fetch(input, init);
          return response;
        }
      } else if (!isRefreshing) {
        // Start refresh process
        isRefreshing = true;
        refreshPromise = (async () => {
          try {
            const refreshResponse = await fetch('/api/auth/refresh', {
              method: 'POST',
              credentials: 'include',
            });
            return refreshResponse.ok;
          } catch (error) {
            return false;
          } finally {
            isRefreshing = false;
            refreshPromise = null;
          }
        })();

        const refreshed = await refreshPromise;
        if (refreshed) {
          // Retry the original request with new token
          response = await fetch(input, init);
          return response;
        }
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
    }

    // Refresh failed - logout and redirect
    const { useAuthStore } = await import('@/src/features/auth/stores/use-auth-store');
    useAuthStore.getState().logout();
    window.location.href = '/login?session_expired=true';
  }

  return response;
}

const configuration = new Configuration({
  basePath: resolveBffBasePath(),
  credentials: 'include',
  middleware: [csrfMiddleware],
  fetchApi: fetchWithAuth,
});

export const authenticationApi = new AuthenticationApi(configuration);
export const claimsApi = new ClaimsApi(configuration);
export const adminDashboardApi = new AdminDashboardApi(configuration);

export {
  type SignupRequest,
  type LoginRequest,
  type UserResponse,
  type SubmitClaimRequest,
  type ClaimResponse,
  type ClaimSummaryResponse,
  type UpdateClaimStatusRequest,
  type DashboardStatsResponse,
  type ProblemDetail,
  ClaimStatus,
  UserRole,
} from './generated';
