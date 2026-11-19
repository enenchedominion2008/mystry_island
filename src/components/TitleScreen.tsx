import React, {useState} from 'react';
import {Compass} from 'lucide-react';
import {TITLE_ARTWORK_PATH} from '../game/engine';

interface TitleScreenProps {
  onStartGame: () => void;
}

export const TitleScreen: React.FC<TitleScreenProps> = ({onStartGame}) => {
  const [imgError, setImgError] = useState(false);

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#0D141B] text-[#EAE5D9] flex flex-col justify-between">
      {!imgError ? (
        <img
          src={TITLE_ARTWORK_PATH}
          alt="Mystery Island lighthouse overlooking stormy twilight waves"
          referrerPolicy="no-referrer"
          onError={() => setImgError(true)}
          className="absolute inset-0 h-full w-full object-cover"
        />
      ) : (
        <div className="absolute inset-0 bg-gradient-to-br from-[#131D27] via-[#0D141B] to-[#192531] flex items-center justify-center">
          <Compass className="h-24 w-24 text-[#E59A3C]/20" />
        </div>
      )}

      <div
        className="absolute inset-0 bg-gradient-to-b from-[#0D141B]/45 via-[#0D141B]/70 to-[#0D141B]"
        aria-hidden="true"
      />

      <div className="relative z-10 mx-auto flex min-h-screen w-full max-w-[1160px] flex-col justify-between px-6 py-8 md:px-12 md:py-12">
        <div className="flex items-center justify-between">
          <span className="font-[family-name:var(--font-mono)] text-xs tracking-wide text-[#E59A3C]">
            Maritime Text Adventure
          </span>
        </div>

        <div className="my-auto py-10 max-w-2xl">
          <h1 className="font-[family-name:var(--font-display)] text-[clamp(2.5rem,5vw,4rem)] font-semibold leading-[1.08] tracking-tight text-[#EAE5D9] [text-wrap:balance]">
            Mystery Island
          </h1>
          <p className="mt-4 text-base md:text-lg leading-relaxed text-[#EAE5D9]/90 max-w-[60ch]">
            You wake on cold basalt pebbles after a violent squall. The lighthouse stands dark,
            the gorge bridge is locked, and a rescue boat will only enter the reef once you
            restore the beacon.
          </p>

          <div className="mt-8 rounded-2xl bg-[#192531]/90 border border-[#EAE5D9]/12 p-5 md:p-6 space-y-3.5">
            <div className="flex items-baseline gap-4">
              <span className="font-[family-name:var(--font-mono)] text-xs font-semibold text-[#E59A3C]">
                01
              </span>
              <p className="text-sm text-[#A9B3BC]">
                Explore storm-swept coves, the boathouse, keeper&apos;s quarters, and the tower.
              </p>
            </div>
            <div className="h-px bg-[#EAE5D9]/10" />
            <div className="flex items-baseline gap-4">
              <span className="font-[family-name:var(--font-mono)] text-xs font-semibold text-[#E59A3C]">
                02
              </span>
              <p className="text-sm text-[#A9B3BC]">
                Collect salvaged tools and rations; unlock the chained bridge and cottage safe.
              </p>
            </div>
            <div className="h-px bg-[#EAE5D9]/10" />
            <div className="flex items-baseline gap-4">
              <span className="font-[family-name:var(--font-mono)] text-xs font-semibold text-[#E59A3C]">
                03
              </span>
              <p className="text-sm text-[#A9B3BC]">
                Restore the Fresnel beacon and reach the Rescue Pier before your energy hits zero.
              </p>
            </div>
          </div>

          <div className="mt-8">
            <button
              type="button"
              onClick={onStartGame}
              className="inline-flex h-12 items-center justify-center rounded-xl bg-[#E59A3C] px-8 text-base font-semibold text-[#111820] hover:bg-[#F3B35B] focus-visible:outline-2 focus-visible:outline-[#EAE5D9] transition-colors cursor-pointer whitespace-nowrap"
            >
              Start Game
            </button>
          </div>
        </div>

        <div className="text-xs text-[#A9B3BC]">
          Play with quick action buttons or type classic commands like{' '}
          <span className="font-[family-name:var(--font-mono)] text-[#EAE5D9]">go north</span>,{' '}
          <span className="font-[family-name:var(--font-mono)] text-[#EAE5D9]">take crowbar</span>,
          or <span className="font-[family-name:var(--font-mono)] text-[#EAE5D9]">look</span>.
        </div>
      </div>
    </div>
  );
};
