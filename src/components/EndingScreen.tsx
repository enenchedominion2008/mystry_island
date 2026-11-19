import React, {useState} from 'react';
import {Anchor} from 'lucide-react';
import {
  countSolvedPuzzles,
  LIGHTHOUSE_ARTWORK_PATH,
  SHORE_ARTWORK_PATH,
} from '../game/engine';
import {GameState} from '../game/types';
import {LOCATIONS} from '../game/world';

interface EndingScreenProps {
  state: GameState;
  onPlayAgain: () => void;
}

export const EndingScreen: React.FC<EndingScreenProps> = ({state, onPlayAgain}) => {
  const [imgError, setImgError] = useState(false);
  const isVictory = state.phase === 'VICTORY';
  const artworkPath = isVictory ? LIGHTHOUSE_ARTWORK_PATH : SHORE_ARTWORK_PATH;

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#0D141B] text-[#EAE5D9] flex items-end">
      {!imgError ? (
        <img
          src={artworkPath}
          alt={isVictory ? 'Lighthouse Fresnel beacon restored' : 'Storm-swept shore'}
          referrerPolicy="no-referrer"
          onError={() => setImgError(true)}
          className="absolute inset-0 h-full w-full object-cover"
        />
      ) : (
        <div className="absolute inset-0 bg-gradient-to-br from-[#131D27] via-[#0D141B] to-[#192531] flex items-center justify-center">
          <Anchor className="h-24 w-24 text-[#E59A3C]/20" />
        </div>
      )}

      <div
        className="absolute inset-0 bg-gradient-to-b from-[#0D141B]/45 via-[#0D141B]/80 to-[#0D141B]"
        aria-hidden="true"
      />

      <div className="relative z-10 mx-auto w-full max-w-[720px] px-6 py-10 md:py-14">
        <p
          className={`font-[family-name:var(--font-mono)] text-xs font-medium ${
            isVictory ? 'text-[#56B895]' : 'text-[#E06C65]'
          }`}
        >
          {isVictory ? 'Rescued from Mystery Island' : 'Out of Energy'}
        </p>

        <h1 className="mt-2 font-[family-name:var(--font-display)] text-[clamp(2.25rem,4.5vw,3.5rem)] font-semibold leading-tight text-[#EAE5D9] [text-wrap:balance]">
          {isVictory ? 'The Beacon Shines Again' : 'Collapsed Before Dawn'}
        </h1>

        <p className="mt-3 text-base leading-relaxed text-[#EAE5D9]/90">{state.endingReason}</p>

        <div className="mt-6 rounded-2xl bg-[#192531]/92 border border-[#EAE5D9]/12 p-5 space-y-3">
          <StatRow label="Turns Taken" value={String(state.turn)} />
          <div className="h-px bg-[#EAE5D9]/10" />
          <StatRow label="Remaining Energy" value={`${state.energy} / ${state.maxEnergy}`} />
          <div className="h-px bg-[#EAE5D9]/10" />
          <StatRow
            label="Locations Charted"
            value={`${state.visitedLocations.length} / ${Object.keys(LOCATIONS).length}`}
          />
          <div className="h-px bg-[#EAE5D9]/10" />
          <StatRow label="Puzzles Solved" value={`${countSolvedPuzzles(state.flags)} / 4`} />
        </div>

        <div className="mt-6">
          <button
            type="button"
            onClick={onPlayAgain}
            className="inline-flex h-12 w-full sm:w-auto items-center justify-center rounded-xl bg-[#E59A3C] px-8 text-base font-semibold text-[#111820] hover:bg-[#F3B35B] focus-visible:outline-2 focus-visible:outline-[#EAE5D9] transition-colors cursor-pointer whitespace-nowrap"
          >
            Play Again
          </button>
        </div>
      </div>
    </div>
  );
};

const StatRow: React.FC<{label: string; value: string}> = ({label, value}) => (
  <div className="flex items-center justify-between text-sm">
    <span className="text-[#A9B3BC]">{label}</span>
    <span className="font-[family-name:var(--font-mono)] font-medium text-[#EAE5D9] tabular-nums">
      {value}
    </span>
  </div>
);
