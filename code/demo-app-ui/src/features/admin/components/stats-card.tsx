'use client';

interface StatsCardProps {
  title: string;
  value?: number;
  subtitle?: string;
  icon: 'users' | 'documents' | 'chart';
  claimsByStatus?: { [key: string]: number };
  isLoading: boolean;
}

const statusColors: Record<string, { bg: string; text: string; label: string }> = {
  SUBMITTED: { bg: 'bg-blue-100 dark:bg-blue-900/30', text: 'text-blue-800 dark:text-blue-200', label: 'Submitted' },
  UNDER_REVIEW: { bg: 'bg-amber-100 dark:bg-amber-900/30', text: 'text-amber-800 dark:text-amber-200', label: 'Under Review' },
  APPROVED: { bg: 'bg-green-100 dark:bg-green-900/30', text: 'text-green-800 dark:text-green-200', label: 'Approved' },
  REJECTED: { bg: 'bg-red-100 dark:bg-red-900/30', text: 'text-red-800 dark:text-red-200', label: 'Rejected' },
  CLOSED: { bg: 'bg-gray-100 dark:bg-gray-900/30', text: 'text-gray-800 dark:text-gray-200', label: 'Closed' },
};

const icons: Record<string, React.ReactNode> = {
  users: (
    <svg className="h-6 w-6 text-primary" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" aria-hidden="true">
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 19.128a9.38 9.38 0 0 0 2.625.372 9.337 9.337 0 0 0 4.121-.952 4.125 4.125 0 0 0-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 0 1 8.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0 1 11.964-3.07M12 6.375a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0Zm8.25 2.25a2.625 2.625 0 1 1-5.25 0 2.625 2.625 0 0 1 5.25 0Z" />
    </svg>
  ),
  documents: (
    <svg className="h-6 w-6 text-primary" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" aria-hidden="true">
      <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z" />
    </svg>
  ),
  chart: (
    <svg className="h-6 w-6 text-primary" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" aria-hidden="true">
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" />
    </svg>
  ),
};

export const StatsCard = ({
  title,
  value,
  subtitle,
  icon,
  claimsByStatus,
  isLoading,
}: StatsCardProps) => {
  return (
    <div className="rounded-lg border border-border bg-surface-card p-6 shadow-sm transition-shadow hover:shadow-md" data-testid={`stats-card-${icon}`}>
      <div className="mb-4 flex items-start justify-between">
        <h3 className="text-sm font-medium text-text-secondary">{title}</h3>
        {icons[icon]}
      </div>

      {isLoading ? (
        <div className="space-y-2">
          <div className="h-10 animate-pulse rounded bg-gray-200 dark:bg-gray-700" />
          <div className="h-4 w-3/4 animate-pulse rounded bg-gray-200 dark:bg-gray-700" />
        </div>
      ) : claimsByStatus ? (
        <div className="space-y-2">
          {Object.entries(statusColors).map(([status, config]) => (
            <div key={status} className="flex items-center justify-between">
              <span className="text-sm text-text-secondary">{config.label}</span>
              <span
                className={`rounded-full px-2 py-1 text-xs font-medium ${config.bg} ${config.text}`}
              >
                {claimsByStatus[status] ?? 0}
              </span>
            </div>
          ))}
        </div>
      ) : (
        <>
          <p className="mb-2 text-4xl font-bold text-text-primary">{value ?? 0}</p>
          {subtitle && <p className="text-sm text-text-muted">{subtitle}</p>}
        </>
      )}
    </div>
  );
};
