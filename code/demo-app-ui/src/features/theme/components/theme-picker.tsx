'use client';

import { useThemeStore, type ThemeName } from '@/src/features/theme/stores/use-theme-store';

const themes: { name: ThemeName; label: string; swatchClass: string }[] = [
  { name: 'default', label: 'Blue', swatchClass: 'theme-swatch-default' },
  { name: 'corporate', label: 'Teal', swatchClass: 'theme-swatch-corporate' },
  { name: 'warm', label: 'Amber', swatchClass: 'theme-swatch-warm' },
];

export function ThemePicker() {
  const theme = useThemeStore((state) => state.theme);
  const isDark = useThemeStore((state) => state.isDark);
  const setTheme = useThemeStore((state) => state.setTheme);
  const toggleDark = useThemeStore((state) => state.toggleDark);

  return (
    <div
      role="group"
      aria-label="Theme and appearance settings"
      className="flex items-center gap-2"
    >
      {themes.map((option) => (
        <button
          key={option.name}
          onClick={() => setTheme(option.name)}
          aria-label={`Switch to ${option.label} theme`}
          aria-pressed={theme === option.name}
          title={option.label}
          className={`
            relative flex h-7 w-7 items-center justify-center rounded-full
            border-2 transition-all
            focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2
            ${theme === option.name ? 'border-current' : 'border-transparent'}
          `}
          style={{
            color: theme === option.name ? `var(--swatch-${option.name})` : 'transparent'
          }}
        >
          <span
            className={`block h-4 w-4 rounded-full ${option.swatchClass}`}
          />
          {theme === option.name && (
            <span className="absolute -bottom-0.5 -right-0.5 flex h-3 w-3 items-center justify-center rounded-full bg-on-primary text-[8px]">
              ✓
            </span>
          )}
        </button>
      ))}

      <button
        onClick={toggleDark}
        aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
        className="ml-1 rounded-md px-2 py-1 text-sm text-text-secondary transition-colors hover:bg-button-secondary-bg focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1"
      >
        {isDark ? '☀️' : '🌙'}
      </button>
    </div>
  );
}
