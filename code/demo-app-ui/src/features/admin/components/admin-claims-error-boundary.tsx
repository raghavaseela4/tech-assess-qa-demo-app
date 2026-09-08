'use client';

import { Component, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class AdminClaimsErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('AdminClaimsErrorBoundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="px-4 py-8">
          <div className="mx-auto max-w-7xl">
            <div
              className="rounded-lg border border-error bg-red-50 p-6 dark:bg-red-900/20"
              role="alert"
            >
              <h2 className="mb-2 text-xl font-semibold text-error">
                Something went wrong
              </h2>
              <p className="mb-4 text-sm text-error">
                An error occurred while loading the claims management interface. Please try
                refreshing the page.
              </p>
              {this.state.error && (
                <details className="mb-4">
                  <summary className="cursor-pointer text-sm font-medium text-error">
                    Error details
                  </summary>
                  <pre className="mt-2 overflow-auto rounded bg-surface-secondary p-2 text-xs text-text-muted">
                    {this.state.error.message}
                  </pre>
                </details>
              )}
              <button
                onClick={() => window.location.reload()}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-white hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
              >
                Reload Page
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
