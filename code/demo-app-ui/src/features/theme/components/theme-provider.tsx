'use client';

import { useEffect } from 'react';
import { useThemeStore } from '@/src/features/theme/stores/use-theme-store';

export function ThemeProvider() {
  const theme = useThemeStore((state) => state.theme);
  const isDark = useThemeStore((state) => state.isDark);

  useEffect(() => {
    const root = document.documentElement;

    root.classList.remove('theme-default', 'theme-corporate', 'theme-warm');
    root.classList.add(`theme-${theme}`);

    if (isDark) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [theme, isDark]);

  return null;
}
