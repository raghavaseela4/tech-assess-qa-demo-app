'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import {
  loginSchema,
  type LoginFormData,
} from '@/src/features/auth/schemas/auth-schemas';
import { useAuthStore } from '@/src/features/auth/stores/use-auth-store';
import { PasswordField } from '@/src/features/auth/components/password-field';

export default function LoginPage() {
  const router = useRouter();
  const login = useAuthStore((s) => s.login);
  const [serverError, setServerError] = useState('');

  // Check for session expiry message
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      if (params.get('session_expired') === 'true') {
        setServerError('Your session has expired. Please log in again.');
        // Clean up URL
        window.history.replaceState({}, '', '/login');
      }
    }
  }, []);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setServerError('');

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
        credentials: 'include',
      });

      if (!response.ok) {
        if (response.status === 401) {
          setServerError('Invalid email or password');
          return;
        }
        throw new Error('Login failed');
      }

      const result = await response.json();
      login({
        userId: result.userId,
        email: result.email,
        name: result.name,
        role: result.role,
      });

      // Use window.location instead of router.push to ensure cookies are sent with the request
      // This fixes the issue where middleware doesn't see the just-set cookie during client-side navigation
      if (result.role === 'ADMIN') {
        window.location.href = '/admin/dashboard';
      } else {
        window.location.href = '/claims';
      }
    } catch (error) {
      console.error('Login error:', error);
      setServerError('Network error, please try again');
    }
  };

  return (
    <>
      <h1 className="mb-6 text-center text-2xl font-bold text-text-primary">
        Log In
      </h1>

      {serverError && (
        <p className="mb-4 rounded-md bg-error-bg p-3 text-sm text-error" role="alert">
          {serverError}
        </p>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate data-testid="login-form">
        <div className="space-y-1">
          <label htmlFor="email" className="block text-sm font-medium text-text-secondary">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            aria-invalid={!!errors.email}
            aria-describedby={errors.email ? 'email-error' : undefined}
            {...register('email')}
            className="w-full rounded-lg border border-input-border bg-input-bg px-4 py-2 text-text-primary transition-colors placeholder:text-text-muted focus:border-transparent focus:outline-none focus:ring-2 focus:ring-input-focus disabled:cursor-not-allowed disabled:bg-input-disabled-bg"
          />
          {errors.email && (
            <p id="email-error" className="text-sm text-error" role="alert">
              {errors.email.message}
            </p>
          )}
        </div>

        <PasswordField
          register={register}
          fieldName="password"
          autoComplete="current-password"
          error={errors.password?.message}
        />

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-primary px-4 py-2 font-medium text-on-primary transition-colors hover:bg-primary-hover active:bg-primary-active disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting ? 'Logging in...' : 'Log In'}
        </button>

        {serverError && (
          <input type="hidden" data-testid="login-error" value={serverError} />
        )}
      </form>

      <p className="mt-4 text-center text-sm text-text-secondary">
        Don&apos;t have an account?{' '}
        <Link href="/signup" className="font-medium text-link hover:text-link-hover">
          Sign up
        </Link>
      </p>
    </>
  );
}
