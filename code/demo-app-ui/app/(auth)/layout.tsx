import { ThemePicker } from '@/src/features/theme/components/theme-picker';

export default function AuthLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="relative flex min-h-screen items-center justify-center bg-surface">
      <div className="absolute right-4 top-4">
        <ThemePicker />
      </div>
      <div className="w-full max-w-md rounded-lg bg-surface-card p-8 shadow-[0_4px_6px_-1px_var(--color-shadow)]">
        {children}
      </div>
    </div>
  );
}
