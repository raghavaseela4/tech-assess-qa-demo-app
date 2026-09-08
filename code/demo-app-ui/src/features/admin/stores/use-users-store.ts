import { create } from 'zustand';
import {
  adminDashboardApi,
  type UserResponse,
} from '@/src/lib/api/bff-client';
import { ResponseError } from '@/src/lib/api/generated/runtime';

interface UsersStore {
  users: UserResponse[];
  isLoading: boolean;
  error: string | null;
  searchQuery: string;

  fetchUsers: () => Promise<void>;
  setSearchQuery: (query: string) => void;
  clearError: () => void;
}

export const useUsersStore = create<UsersStore>()((set, get) => ({
  users: [],
  isLoading: false,
  error: null,
  searchQuery: '',

  fetchUsers: async () => {
    set({ isLoading: true, error: null });

    try {
      console.log('Fetching all users...');
      const data = await adminDashboardApi.listAllUsers();
      console.log('Users received:', data);

      // Sort by createdAt DESC (newest first)
      const sortedUsers = [...data].sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return dateB - dateA;
      });

      set({ users: sortedUsers, isLoading: false });
      console.log('Users store updated successfully');
    } catch (error) {
      console.error('Users fetch error:', error);
      if (error instanceof ResponseError) {
        console.log('ResponseError status:', error.response.status);
        if (error.response.status === 403) {
          set({ error: 'ACCESS_DENIED', isLoading: false });
          return;
        }
      }

      set({
        error: 'Failed to load users',
        isLoading: false,
      });
    }
  },

  setSearchQuery: (query: string) => {
    set({ searchQuery: query });
  },

  clearError: () => set({ error: null }),
}));
