'use client';

import { useState } from 'react';
import type { UseFormRegister, FieldValues, Path } from 'react-hook-form';

interface PasswordFieldProps<T extends FieldValues> {
  register: UseFormRegister<T>;
  fieldName: Path<T>;
  label?: string;
  autoComplete?: 'new-password' | 'current-password';
  error?: string;
}

export const PasswordField = <T extends FieldValues>({
  register,
  fieldName,
  label = 'Password',
  autoComplete = 'current-password',
  error,
}: PasswordFieldProps<T>) => {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="space-y-1">
      <label htmlFor={fieldName} className="block text-sm font-medium text-text-secondary">
        {label}
      </label>
      <div className="relative">
        <input
          id={fieldName}
          type={showPassword ? 'text' : 'password'}
          autoComplete={autoComplete}
          aria-invalid={!!error}
          aria-describedby={error ? `${fieldName}-error` : undefined}
          {...register(fieldName)}
          className="w-full rounded-lg border border-input-border bg-input-bg px-4 py-2 pr-12 text-text-primary transition-colors placeholder:text-text-muted focus:border-transparent focus:outline-none focus:ring-2 focus:ring-input-focus disabled:cursor-not-allowed disabled:bg-input-disabled-bg"
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          aria-label={showPassword ? 'Hide password' : 'Show password'}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-text-muted hover:text-text-secondary"
        >
          {showPassword ? 'Hide' : 'Show'}
        </button>
      </div>
      {error && (
        <p id={`${fieldName}-error`} className="text-sm text-error" role="alert">
          {error}
        </p>
      )}
    </div>
  );
};
