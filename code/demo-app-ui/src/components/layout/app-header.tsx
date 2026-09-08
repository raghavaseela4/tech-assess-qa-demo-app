'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/src/features/auth/stores/use-auth-store';
import { ThemePicker } from '@/src/features/theme/components/theme-picker';
import { ConnectionStatusIndicator } from '@/src/components/connection-status-indicator';

export function AppHeader() {
  const router = useRouter();
  const { user, isAuthenticated, logout } = useAuthStore();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  if (!isAuthenticated) return null;

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      });
    } catch (error) {
      console.error('Logout error:', error);
    }

    logout();
    router.push('/login');
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <header className="flex items-center justify-between border-b border-border bg-surface-card px-6 py-3">
      <div className="flex items-center gap-6">
        <span className="text-lg font-semibold text-text-primary">Demo App</span>

        {isAdmin ? (
          <nav className="flex gap-4">
            <button
              onClick={() => router.push('/admin/dashboard')}
              className="text-sm font-medium text-text-secondary hover:text-primary transition-colors"
            >
              Dashboard
            </button>
            <button
              onClick={() => router.push('/admin/claims')}
              className="text-sm font-medium text-text-secondary hover:text-primary transition-colors"
            >
              Claims
            </button>
            <button
              onClick={() => router.push('/admin/users')}
              className="text-sm font-medium text-text-secondary hover:text-primary transition-colors"
            >
              Users
            </button>
          </nav>
        ) : (
          <nav className="flex gap-4">
            <button
              onClick={() => router.push('/claims')}
              className="text-sm font-medium text-text-secondary hover:text-primary transition-colors"
            >
              My Claims
            </button>
            <button
              onClick={() => router.push('/claims/new')}
              className="text-sm font-medium text-text-secondary hover:text-primary transition-colors"
            >
              Submit Claim
            </button>
          </nav>
        )}
      </div>

      <div className="flex items-center gap-4">
        <ConnectionStatusIndicator />
        <ThemePicker />
        <span className="text-sm text-text-secondary">Welcome, {user?.name}</span>
        <button
          onClick={handleLogout}
          disabled={isLoggingOut}
          className="rounded-md bg-button-secondary-bg px-3 py-1.5 text-sm font-medium text-button-secondary-text transition-colors hover:bg-button-secondary-hover disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isLoggingOut ? 'Logging out...' : 'Logout'}
        </button>
      </div>
    </header>
  );
}
