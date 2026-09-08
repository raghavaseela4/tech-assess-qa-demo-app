'use client';

import { useEffect, useRef, useState } from 'react';
import { ClaimStatusBadge } from './claim-status-badge';
import { formatDate, formatCurrency } from '../utils/formatting';
import { claimsApi } from '@/src/lib/api/bff-client';
import type { ClaimResponse } from '@/src/lib/api/bff-client';

interface ClaimDetailModalProps {
  isOpen: boolean;
  claimId: string | null;
  onClose: () => void;
}

export const ClaimDetailModal = ({
  isOpen,
  claimId,
  onClose,
}: ClaimDetailModalProps) => {
  const [claim, setClaim] = useState<ClaimResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const modalRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<Element | null>(null);

  useEffect(() => {
    if (isOpen && claimId) {
      triggerRef.current = document.activeElement;
      setIsLoading(true);
      setError(null);
      setClaim(null);

      claimsApi
        .getClaim({ claimId })
        .then((data) => {
          setClaim(data);
          setIsLoading(false);
        })
        .catch((err) => {
          if (err?.response?.status === 401 || err?.response?.status === 403) {
            setError('You are not authorized to view this claim.');
          } else if (err?.response?.status === 404) {
            setError('Claim not found.');
          } else {
            setError('Failed to load claim details. Please try again.');
          }
          setIsLoading(false);
        });
    }
  }, [isOpen, claimId]);

  useEffect(() => {
    if (isOpen) {
      closeButtonRef.current?.focus();
    }
  }, [isOpen, isLoading]);

  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
        return;
      }

      if (e.key === 'Tab' && modalRef.current) {
        const focusableElements = modalRef.current.querySelectorAll<HTMLElement>(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (e.shiftKey) {
          if (document.activeElement === firstElement) {
            e.preventDefault();
            lastElement?.focus();
          }
        } else {
          if (document.activeElement === lastElement) {
            e.preventDefault();
            firstElement?.focus();
          }
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  useEffect(() => {
    if (!isOpen && triggerRef.current instanceof HTMLElement) {
      triggerRef.current.focus();
      triggerRef.current = null;
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
      data-testid="claim-detail-modal"
    >
      <div
        ref={modalRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="claim-detail-title"
        className="mx-4 w-full max-w-lg rounded-lg border border-border bg-surface-card p-6 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="mb-4 flex items-center justify-between">
          <h2
            id="claim-detail-title"
            className="text-xl font-semibold text-text-primary"
          >
            Claim Details
          </h2>
          <button
            ref={closeButtonRef}
            onClick={onClose}
            aria-label="Close"
            className="text-text-secondary transition-colors hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
          >
            ✕
          </button>
        </div>

        {/* Loading State */}
        {isLoading && (
          <div className="flex items-center justify-center py-12" role="status">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
            <span className="sr-only">Loading claim details...</span>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="rounded-lg bg-red-50 p-4 text-error dark:bg-red-900/20" role="alert">
            <p>{error}</p>
          </div>
        )}

        {/* Claim Details */}
        {claim && !isLoading && !error && (
          <div className="space-y-4">
            <DetailField label="Claim ID">
              <code className="font-mono text-sm">{claim.claimId}</code>
            </DetailField>

            <DetailField label="Status">
              <ClaimStatusBadge status={claim.status} />
            </DetailField>

            <DetailField label="Incident Date">
              {formatDate(claim.incidentDate)}
            </DetailField>

            <DetailField label="Incident Location">
              {claim.incidentLocation}
            </DetailField>

            <DetailField label="Description">
              {claim.description}
            </DetailField>

            <DetailField label="Claim Amount">
              {formatCurrency(claim.claimAmount)}
            </DetailField>

            <DetailField label="Date Submitted">
              {formatDate(claim.createdAt)}
            </DetailField>

            <DetailField label="Last Updated">
              {formatDate(claim.updatedAt)}
            </DetailField>
          </div>
        )}
      </div>
    </div>
  );
};

const DetailField = ({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) => (
  <div>
    <dt className="text-sm text-text-secondary">{label}</dt>
    <dd className="mt-1 text-text-primary">{children}</dd>
  </div>
);
