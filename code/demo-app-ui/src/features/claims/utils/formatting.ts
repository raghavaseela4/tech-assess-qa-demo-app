/**
 * Formatting utilities for claims display
 */

export const MAX_DESCRIPTION_LENGTH = 50;

export const formatDate = (date: Date): string => {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
};

export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

export const truncateDescription = (
  description: string,
  maxLength: number = MAX_DESCRIPTION_LENGTH
): string => {
  if (description.length <= maxLength) return description;
  return description.substring(0, maxLength) + '...';
};
