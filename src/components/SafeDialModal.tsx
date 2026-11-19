import React, {useState} from 'react';
import {ChevronDown, ChevronUp} from 'lucide-react';

interface SafeDialModalProps {
  onSubmit: (code: string) => void;
  onClose: () => void;
}

export const SafeDialModal: React.FC<SafeDialModalProps> = ({onSubmit, onClose}) => {
  const [digits, setDigits] = useState<[number, number, number]>([0, 0, 0]);

  const stepDigit = (index: 0 | 1 | 2, delta: number) => {
    setDigits((prev) => {
      const next = [...prev] as [number, number, number];
      next[index] = (next[index] + delta + 10) % 10;
      return next;
    });
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="safe-dialog-title"
    >
      <div className="w-full max-w-sm rounded-2xl bg-[#192531] border border-[#EAE5D9]/20 p-6 shadow-2xl">
        <h2
          id="safe-dialog-title"
          className="font-[family-name:var(--font-display)] text-2xl font-semibold text-[#EAE5D9]"
        >
          Brass Wall Safe
        </h2>
        <p className="mt-1.5 text-sm text-[#A9B3BC]">
          Dial the 3-digit Storm Protocol combination from the Keeper&apos;s Logbook.
        </p>

        <div className="mt-6 flex items-center justify-center gap-4">
          {([0, 1, 2] as const).map((idx) => (
            <div
              key={idx}
              className="flex flex-col items-center rounded-xl bg-[#131D27] border border-[#EAE5D9]/15 px-3 py-2"
            >
              <button
                type="button"
                onClick={() => stepDigit(idx, 1)}
                aria-label={`Increase digit ${idx + 1}`}
                className="flex h-11 w-11 items-center justify-center rounded-lg text-[#E59A3C] hover:bg-[#21303E] focus-visible:outline-2 focus-visible:outline-[#E59A3C] transition-colors cursor-pointer"
              >
                <ChevronUp className="h-5 w-5" />
              </button>
              <span className="my-1 flex h-12 w-12 items-center justify-center rounded-lg bg-[#21303E] font-[family-name:var(--font-mono)] text-2xl font-semibold text-[#EAE5D9] tabular-nums">
                {digits[idx]}
              </span>
              <button
                type="button"
                onClick={() => stepDigit(idx, -1)}
                aria-label={`Decrease digit ${idx + 1}`}
                className="flex h-11 w-11 items-center justify-center rounded-lg text-[#E59A3C] hover:bg-[#21303E] focus-visible:outline-2 focus-visible:outline-[#E59A3C] transition-colors cursor-pointer"
              >
                <ChevronDown className="h-5 w-5" />
              </button>
            </div>
          ))}
        </div>

        <div className="mt-6 flex gap-3">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 h-11 rounded-lg border border-[#EAE5D9]/20 text-sm font-semibold text-[#EAE5D9] hover:bg-[#21303E] focus-visible:outline-2 focus-visible:outline-[#E59A3C] transition-colors cursor-pointer whitespace-nowrap"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => onSubmit(digits.join(''))}
            className="flex-1 h-11 rounded-lg bg-[#E59A3C] text-sm font-semibold text-[#111820] hover:bg-[#F3B35B] focus-visible:outline-2 focus-visible:outline-[#EAE5D9] transition-colors cursor-pointer whitespace-nowrap"
          >
            Unlock
          </button>
        </div>
      </div>
    </div>
  );
};
