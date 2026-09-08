import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const protectedPaths = ['/claims', '/admin'];

/**
 * Middleware for protected route authentication.
 *
 * SECURITY NOTE: This middleware ONLY checks for cookie existence (client-side UX guard).
 * Actual JWT validation (signature, expiry, claims) happens server-side in BFF API.
 * This prevents flash of unauthenticated content but is NOT a security control.
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const isProtected = protectedPaths.some((path) => pathname.startsWith(path));
  if (!isProtected) {
    return NextResponse.next();
  }

  // Check cookie existence only - server validates JWT properly
  const accessToken = request.cookies.get('access_token');
  const refreshToken = request.cookies.get('refresh_token');

  // Allow access if either access token OR refresh token exists
  // If only refresh token exists, client-side code will handle refresh
  if (!accessToken && !refreshToken) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/claims/:path*', '/admin/:path*'],
};
