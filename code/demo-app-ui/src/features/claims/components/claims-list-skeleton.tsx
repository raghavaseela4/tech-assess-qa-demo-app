export const ClaimsListSkeleton = () => {
  return (
    <div className="space-y-4" role="status" aria-label="Loading claims">
      {[1, 2, 3].map((index) => (
        <div
          key={index}
          className="animate-pulse rounded-lg border border-border bg-surface-card p-4"
        >
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 space-y-2">
              <div className="h-4 w-1/4 rounded bg-surface" />
              <div className="h-3 w-3/4 rounded bg-surface" />
              <div className="h-3 w-1/2 rounded bg-surface" />
            </div>
            <div className="h-6 w-24 rounded-full bg-surface" />
          </div>
        </div>
      ))}
      <span className="sr-only">Loading your claims...</span>
    </div>
  );
};
