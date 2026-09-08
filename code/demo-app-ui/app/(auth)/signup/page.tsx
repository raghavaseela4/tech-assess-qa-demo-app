'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import toast from 'react-hot-toast';
import {
  signupSchema,
  type SignupFormData,
} from '@/src/features/auth/schemas/auth-schemas';
import { PasswordField } from '@/src/features/auth/components/password-field';

export default function SignupPage() {
  const router = useRouter();
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  });

  const onSubmit = async (data: SignupFormData) => {
    setServerError('');

    try {
      const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        if (response.status === 409) {
          setError('email', { message: 'Email already exists' });
          return;
        }

        if (response.status === 400) {
          const error = await response.json();
          setServerError(error.detail || 'Invalid input. Please check your fields.');
          return;
        }

        throw new Error('Signup failed');
      }

      toast.success('Account created! Please log in.');
      router.push('/login');
    } catch (error) {
      console.error('Signup error:', error);
      setServerError('Network error, please try again');
    }
  };

  return (
    <>
      <h1 className="mb-6 text-center text-2xl font-bold text-text-primary">
        Create Account
      </h1>

      {serverError && (
        <p className="mb-4 rounded-md bg-error-bg p-3 text-sm text-error" role="alert">
          {serverError}
        </p>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="space-y-1">
          <label htmlFor="name" className="block text-sm font-medium text-text-secondary">
            Name
          </label>
          <input
            id="name"
            type="text"
            autoComplete="name"
            aria-invalid={!!errors.name}
            aria-describedby={errors.name ? 'name-error' : undefined}
            {...register('name')}
            className="w-full rounded-lg border border-input-border bg-input-bg px-4 py-2 text-text-primary transition-colors placeholder:text-text-muted focus:border-transparent focus:outline-none focus:ring-2 focus:ring-input-focus disabled:cursor-not-allowed disabled:bg-input-disabled-bg"
          />
          {errors.name && (
            <p id="name-error" className="text-sm text-error" role="alert">
              {errors.name.message}
            </p>
          )}
        </div>

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
          autoComplete="new-password"
          error={errors.password?.message}
        />

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-primary px-4 py-2 font-medium text-on-primary transition-colors hover:bg-primary-hover active:bg-primary-active disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting ? 'Creating account...' : 'Sign Up'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-text-secondary">
        Already have an account?{' '}
        <Link href="/login" className="font-medium text-link hover:text-link-hover">
          Log in
        </Link>
      </p>
    </>
  );
}
