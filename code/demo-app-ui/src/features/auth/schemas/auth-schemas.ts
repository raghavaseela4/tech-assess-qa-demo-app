import { z } from 'zod';

export const signupSchema = z.object({
  name: z.string().min(1, 'Name is required').max(255, 'Name cannot exceed 255 characters'),
  email: z.string().email('Invalid email format'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain an uppercase letter')
    .regex(/[0-9]/, 'Password must contain a number'),
});

export type SignupFormData = z.infer<typeof signupSchema>;

export const loginSchema = z.object({
  email: z.string().email('Invalid email format'),
  password: z.string().min(1, 'Password is required'),
});

export type LoginFormData = z.infer<typeof loginSchema>;
