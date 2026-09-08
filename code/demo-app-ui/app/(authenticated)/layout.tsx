import { AppHeader } from '@/src/components/layout/app-header';
import { RealtimeProvider } from '@/src/components/realtime-provider';
import { RealtimeWarningBanner } from '@/src/components/realtime-warning-banner';

export default function AuthenticatedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-surface">
      <RealtimeProvider />
      <AppHeader />
      <RealtimeWarningBanner />
      <main>{children}</main>
    </div>
  );
}
