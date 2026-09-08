/**
 * Custom hook for copy-to-clipboard functionality with toast notifications
 */

import toast from 'react-hot-toast';

export const useCopyClipboard = () => {
  const copyToClipboard = async (text: string, successMessage = 'Copied to clipboard') => {
    try {
      await navigator.clipboard.writeText(text);
      toast.success(successMessage);
    } catch {
      toast.error('Failed to copy');
    }
  };

  return { copyToClipboard };
};
