'use client';

import { useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { useUsersStore } from '@/src/features/admin/stores/use-users-store';
import { ClaimsListSkeleton } from '@/src/features/claims/components/claims-list-skeleton';

const AdminUsersPage = () => {
  const router = useRouter();
  const users = useUsersStore((state) => state.users);
  const isLoading = useUsersStore((state) => state.isLoading);
  const error = useUsersStore((state) => state.error);
  const searchQuery = useUsersStore((state) => state.searchQuery);
  const setSearchQuery = useUsersStore((state) => state.setSearchQuery);
  const fetchUsers = useUsersStore((state) => state.fetchUsers);
  const clearError = useUsersStore((state) => state.clearError);

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (error === 'ACCESS_DENIED') {
      toast.error('Access denied');
      router.push('/claims');
    }
  }, [error, router]);

  const filteredUsers = useMemo(() => {
    if (!searchQuery) return users;
    const query = searchQuery.toLowerCase();
    return users.filter(
      (user) =>
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query) ||
        user.role.toLowerCase().includes(query)
    );
  }, [users, searchQuery]);

  return (
    <div className="px-4 py-8">
      <div className="mx-auto max-w-7xl">
        {/* Header */}
        <div className="mb-6 flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold text-text-primary">User Management</h1>
            {!isLoading && (
              <span
                className="rounded-full bg-surface-secondary px-3 py-1 text-sm font-medium text-text-secondary"
                aria-live="polite"
                aria-atomic="true"
              >
                {filteredUsers.length} users
              </span>
            )}
          </div>
        </div>

        {/* Search */}
        <div className="mb-6">
          <label htmlFor="user-search" className="sr-only">
            Search users
          </label>
          <input
            id="user-search"
            type="text"
            placeholder="Search by name, email, or role..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full rounded-lg border border-border-default bg-surface-primary px-4 py-2 text-text-primary placeholder-text-secondary focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>

        {/* Error Banner */}
        {error && error !== 'ACCESS_DENIED' && (
          <div
            className="mb-6 flex items-center justify-between rounded-lg border border-error bg-red-50 p-4 dark:bg-red-900/20"
            role="alert"
          >
            <p className="text-sm text-error">
              Failed to load users. Please try again.
            </p>
            <button
              onClick={() => {
                clearError();
                fetchUsers();
              }}
              className="text-sm font-medium text-error underline hover:no-underline"
            >
              Retry
            </button>
          </div>
        )}

        {/* Loading State */}
        {isLoading && <ClaimsListSkeleton />}

        {/* Empty State */}
        {!isLoading && !error && filteredUsers.length === 0 && (
          <div className="flex flex-col items-center justify-center px-4 py-16">
            <div className="mb-6 text-6xl" aria-hidden="true">
              👥
            </div>
            <h2 className="mb-2 text-2xl font-semibold text-text-primary">
              No users found
            </h2>
            <p className="max-w-md text-center text-text-secondary">
              {searchQuery
                ? 'No users match your search criteria. Try a different search term.'
                : 'No users in the system yet.'}
            </p>
          </div>
        )}

        {/* Users Table */}
        {!isLoading && !error && filteredUsers.length > 0 && (
          <div className="overflow-hidden rounded-lg border border-border-default bg-surface-primary shadow">
            <table className="min-w-full divide-y divide-border-default">
              <thead className="bg-surface-secondary">
                <tr>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-text-secondary"
                  >
                    Name
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-text-secondary"
                  >
                    Email
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-text-secondary"
                  >
                    Role
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-text-secondary"
                  >
                    Created At
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-default bg-surface-primary">
                {filteredUsers.map((user) => (
                  <tr
                    key={user.userId}
                    className="hover:bg-surface-secondary transition-colors"
                  >
                    <td className="whitespace-nowrap px-6 py-4">
                      <div className="flex items-center">
                        <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                          <span className="text-sm font-medium">
                            {user.name.charAt(0).toUpperCase()}
                          </span>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-text-primary">
                            {user.name}
                          </div>
                          <div className="text-xs text-text-secondary">
                            ID: {user.userId.substring(0, 8)}...
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4">
                      <div className="text-sm text-text-primary">{user.email}</div>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4">
                      <span
                        className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
                          user.role === 'ADMIN'
                            ? 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300'
                            : 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300'
                        }`}
                      >
                        {user.role}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-text-secondary">
                      {new Date(user.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminUsersPage;
