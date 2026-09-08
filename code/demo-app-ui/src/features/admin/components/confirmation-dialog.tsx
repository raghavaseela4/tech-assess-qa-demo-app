'use client';

import { useEffect, useRef } from 'react';

interface ConfirmationDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export const ConfirmationDialog = ({
  isOpen,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
}: ConfirmationDialogProps) => {
  const dialogRef = useRef<HTMLDivElement>(null);
  const confirmButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      // Focus the confirm button when dialog opens
      confirmButtonRef.current?.focus();

      // Trap focus within dialog
      const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Escape') {
          onCancel();
        }
      };

      document.addEventListener('keydown', handleKeyDown);
      return () => document.removeEventListener('keydown', handleKeyDown);
    }
  }, [isOpen, onCancel]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="dialog-title"
      aria-describedby="dialog-description"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onCancel();
        }
      }}
    >
      <div
        ref={dialogRef}
        className="w-full max-w-md rounded-lg border border-border bg-surface-card p-6 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <h2
          id="dialog-title"
          className="mb-4 text-xl font-semibold text-text-primary"
        >
          {title}
        </h2>
        <p id="dialog-description" className="mb-6 text-text-secondary">
          {message}
        </p>
        <div className="flex justify-end gap-3">
          <button
            onClick={onCancel}
            className="rounded-lg border border-border-subtle bg-surface-secondary px-4 py-2 text-sm font-medium text-text-primary hover:bg-surface-secondary/80 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
          >
            {cancelLabel}
          </button>
          <button
            ref={confirmButtonRef}
            onClick={onConfirm}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-white hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};
