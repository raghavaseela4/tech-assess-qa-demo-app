'use client';

interface ProgressIndicatorProps {
  currentStep: 1 | 2 | 3;
}

const stepLabels = ['Incident Details', 'Supporting Details', 'Review'];

export function ProgressIndicator({ currentStep }: ProgressIndicatorProps) {
  return (
    <div className="mb-8" role="navigation" aria-label="Wizard progress">
      <div className="flex justify-between mb-2">
        {stepLabels.map((label, index) => {
          const stepNumber = index + 1;
          const isCurrent = stepNumber === currentStep;
          const isComplete = stepNumber < currentStep;

          return (
            <div
              key={label}
              className={`text-sm font-medium ${
                isCurrent
                  ? 'text-primary'
                  : isComplete
                    ? 'text-success'
                    : 'text-text-muted'
              }`}
              aria-current={isCurrent ? 'step' : undefined}
            >
              Step {stepNumber}: {label}
            </div>
          );
        })}
      </div>

      <div className="h-2 rounded-full bg-input-border overflow-hidden">
        <div
          className="h-full bg-primary transition-all duration-300"
          style={{ width: `${(currentStep / 3) * 100}%` }}
          role="progressbar"
          aria-valuenow={currentStep}
          aria-valuemin={1}
          aria-valuemax={3}
          aria-label={`Step ${currentStep} of 3`}
        />
      </div>
    </div>
  );
}
