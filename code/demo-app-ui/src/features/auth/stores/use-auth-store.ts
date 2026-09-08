import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
  userId: string;
  email: string;
  name: string;
  role: 'CLAIMANT' | 'ADMIN';
}

interface AuthStore {
  user: User | null;
  isAuthenticated: boolean;
  login: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,

      login: (user) => set({ user, isAuthenticated: true }),

      logout: () => set({ user: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage',
    }
  )
);
