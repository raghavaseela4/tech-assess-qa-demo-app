'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { ClaimStatus } from '@/src/lib/api/bff-client';
import { STATUS_LABELS } from '../utils/claim-status-transitions';
import { useAdminClaimsStore } from '../stores/use-admin-claims-store';

const ALL_STATUSES = Object.values(ClaimStatus) as ClaimStatus[];

export const AdminClaimsFilters = () => {
  const statusFilter = useAdminClaimsStore((state) => state.statusFilter);
  const searchQuery = useAdminClaimsStore((state) => state.searchQuery);
  const setStatusFilter = useAdminClaimsStore((state) => state.setStatusFilter);
  const setSearchQuery = useAdminClaimsStore((state) => state.setSearchQuery);
  const clearFilters = useAdminClaimsStore((state) => state.clearFilters);

  const [localSearch, setLocalSearch] = useState(searchQuery);
  const debounceRef = useRef<number | undefined>(undefined);

  useEffect(() => {
    setLocalSearch(searchQuery);
  }, [searchQuery]);

  const handleSearchChange = useCallback((value: string) => {
    setLocalSearch(value);
    if (debounceRef.current !== undefined) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = window.setTimeout(() => {
      setSearchQuery(value);
    }, 300);
  }, [setSearchQuery]);

  useEffect(() => {
    return () => {
      if (debounceRef.current !== undefined) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  const hasActiveFilters = statusFilter !== null || searchQuery !== '';

  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center">
      <div className="flex-1 sm:max-w-xs">
        <label htmlFor="status-filter" className="sr-only">
          Filter by status
        </label>
        <select
          id="status-filter"
          value={statusFilter ?? ''}
          onChange={(e) =>
            setStatusFilter(e.target.value ? (e.target.value as ClaimStatus) : null)
          }
          className="w-full rounded-lg border border-border-subtle bg-surface-secondary px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary"
          aria-label="Filter claims by status"
        >
          <option value="">All Statuses</option>
          {ALL_STATUSES.map((status) => (
            <option key={status} value={status}>
              {STATUS_LABELS[status]}
            </option>
          ))}
        </select>
      </div>

      <div className="flex-1 sm:max-w-xs">
        <label htmlFor="search-user-id" className="sr-only">
          Search by User ID
        </label>
        <input
          id="search-user-id"
          type="text"
          placeholder="Search by User ID"
          value={localSearch}
          onChange={(e) => handleSearchChange(e.target.value)}
          className="w-full rounded-lg border border-border-subtle bg-surface-secondary px-3 py-2 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:ring-2 focus:ring-primary"
          aria-label="Search claims by user ID"
        />
      </div>

      {hasActiveFilters && (
        <button
          onClick={clearFilters}
          className="text-sm font-medium text-link underline hover:text-link-hover"
        >
          Clear Filters
        </button>
      )}
    </div>
  );
};
