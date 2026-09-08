import { z } from 'zod';

export const step1Schema = z.object({
  incidentDate: z
    .date({ error: 'Incident date is required' })
    .refine(
      (date) => date <= new Date(),
      'Incident date cannot be in the future'
    ),
  incidentLocation: z
    .string()
    .min(5, 'Location must be at least 5 characters')
    .max(200, 'Location cannot exceed 200 characters'),
  claimAmount: z
    .number({ error: 'Claim amount is required' })
    .min(0.01, 'Claim amount must be at least $0.01')
    .max(1000000, 'Claim amount cannot exceed $1,000,000'),
});

export const step2Schema = z.object({
  description: z
    .string()
    .min(10, 'Description must be at least 10 characters')
    .max(1000, 'Description cannot exceed 1000 characters'),
});

export const claimSubmissionSchema = step1Schema.merge(step2Schema);

export type ClaimFormData = z.infer<typeof claimSubmissionSchema>;
export type Step1FormData = z.infer<typeof step1Schema>;
export type Step2FormData = z.infer<typeof step2Schema>;
