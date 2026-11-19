import React, {useEffect, useMemo, useRef, useState} from 'react';
import {Anchor, Compass, HelpCircle, Map as MapIcon, Send, Volume2, VolumeX, X} from 'lucide-react';
import {availableContextActions} from '../game/engine';
import {Direction, GameState, LocationId, LogEntry} from '../game/types';
import {DIRECTION_META, ITEMS, LOCATIONS} from '../game/world';
import {IslandMap} from './IslandMap';
import {SafeDialModal} from './SafeDialModal';

interface AdventureViewProps {
  state: GameState;
  onSubmitCommand: (cmd: string) => void;
  onOpenSafeDialog: () => void;
  onDismissSafeDialog: () => void;
  onSubmitSafeCode: (code: string) => void;
  onToggleSound: () => void;
  onRestart: () => void;
}

type ConsoleTab = 'ACTIONS' | 'MOVE' | 'BAG';

export const AdventureView: React.FC<AdventureViewProps> = ({
  state,
  onSubmitCommand,
  onOpenSafeDialog,
  onDismissSafeDialog,
  onSubmitSafeCode,
  onToggleSound,
  onRestart,
}) => {
  const [input, setInput] = useState('');
  const [history, setHistory] = useState<string[]>([]);
  const [historyIdx, setHistoryIdx] = useState<number>(-1);
  const [activeTab, setActiveTab] = useState<ConsoleTab>('ACTIONS');
  const [showMobileMap, setShowMobileMap] = useState(false);
  const [showRestartConfirm, setShowRestartConfirm] = useState(false);
  const [brokenArtwork, setBrokenArtwork] = useState<Record<string, boolean>>({});

  const logEndRef = useRef<HTMLDivElement | null>(null);
  const location = LOCATIONS[state.currentLocation];
  const contextActions = useMemo(() => availableContextActions(state), [state]);

  useEffect(() => {
    logEndRef.current?.scrollIntoView({behavior: 'smooth'});
  }, [state.log.length]);

  const energyPct = Math.max(0, Math.min(100, (state.energy / state.maxEnergy) * 100));
  const isLowEnergy = state.energy <= 5;
  const energyTextColor = isLowEnergy
    ? 'text-[#E06C65]'
    : state.energy <= 10
      ? 'text-[#E59A3C]'
      : 'text-[#56B895]';
  const energyBarColor = isLowEnergy
    ? 'bg-[#E06C65]'
    : state.energy <= 10
      ? 'bg-[#E59A3C]'
      : 'bg-[#56B895]';

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = input.trim();
    if (!trimmed) return;
    setHistory((prev) => [trimmed, ...prev.slice(0, 29)]);
    setHistoryIdx(-1);
    onSubmitCommand(trimmed);
    setInput('');
  };

  const handleInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'ArrowUp' && history.length > 0) {
      e.preventDefault();
      const nextIdx = Math.min(historyIdx + 1, history.length - 1);
      setHistoryIdx(nextIdx);
      setInput(history[nextIdx]);
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (historyIdx <= 0) {
        setHistoryIdx(-1);
        setInput('');
      } else {
        const nextIdx = historyIdx - 1;
        setHistoryIdx(nextIdx);
        setInput(history[nextIdx]);
      }
    }
  };

  const artworkSrc = location.artworkPath;
  const hasValidArtwork = Boolean(artworkSrc && !brokenArtwork[artworkSrc]);

  return (
    <div className="min-h-screen bg-[#0D141B] text-[#EAE5D9] flex flex-col">
      {/* 3-Zone Top Bar */}
      <header className="border-b border-[#EAE5D9]/10 bg-[#131D27]">
        <div className="mx-auto flex max-w-[1200px] items-center justify-between gap-8 px-4 sm:px-6 py-3.5">
          <span className="font-[family-name:var(--font-display)] text-xl font-semibold tracking-tight text-[#EAE5D9] whitespace-nowrap shrink-0">
            Mystery Island
          </span>

          <nav className="flex items-center gap-5 text-sm font-medium text-[#A9B3BC]">
            <button
              type="button"
              onClick={() => setShowMobileMap(true)}
              className="lg:hidden inline-flex items-center gap-1.5 hover:text-[#EAE5D9] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              <MapIcon className="h-4 w-4" />
              <span>Chart</span>
            </button>
            <button
              type="button"
              onClick={() => onSubmitCommand('help')}
              className="inline-flex items-center gap-1.5 hover:text-[#EAE5D9] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              <HelpCircle className="h-4 w-4" />
              <span>Help</span>
            </button>
            <button
              type="button"
              onClick={onToggleSound}
              aria-label={state.soundEnabled ? 'Mute sound' : 'Enable sound'}
              className="inline-flex items-center gap-1.5 hover:text-[#EAE5D9] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              {state.soundEnabled ? (
                <Volume2 className="h-4 w-4 text-[#EAE5D9]" />
              ) : (
                <VolumeX className="h-4 w-4 text-[#E06C65]" />
              )}
              <span className="hidden sm:inline">
                {state.soundEnabled ? 'Audio On' : 'Muted'}
              </span>
            </button>
          </nav>

          <div className="shrink-0">
            <button
              type="button"
              onClick={() => (state.turn > 1 ? setShowRestartConfirm(true) : onRestart())}
              className="h-9 rounded-lg border border-[#EAE5D9]/20 px-4 text-xs font-semibold text-[#EAE5D9] hover:bg-[#21303E] focus-visible:outline-2 focus-visible:outline-[#E59A3C] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              Restart
            </button>
          </div>
        </div>
      </header>

      {/* Main 2-Column Expedition Layout */}
      <main className="mx-auto grid w-full max-w-[1200px] flex-1 grid-cols-1 lg:grid-cols-[380px_1fr] gap-6 px-4 sm:px-6 py-5 items-stretch">
        {/* Left Sidebar: Location Scene, Energy, Checklist, Cartography */}
        <aside className="flex flex-col gap-5">
          {/* Scene Card */}
          <div className="relative h-44 sm:h-48 lg:h-44 overflow-hidden rounded-2xl border border-[#EAE5D9]/12 bg-[#16202A]">
            {hasValidArtwork && artworkSrc ? (
              <img
                src={artworkSrc}
                alt={location.name}
                referrerPolicy="no-referrer"
                onError={() =>
                  setBrokenArtwork((prev) => ({...prev, [artworkSrc]: true}))
                }
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="h-full w-full bg-gradient-to-br from-[#192531] via-[#131D27] to-[#0D141B] flex items-center justify-center">
                {location.id === 'RESCUE_PIER' ? (
                  <Anchor className="h-14 w-14 text-[#E59A3C]/25" />
                ) : (
                  <Compass className="h-14 w-14 text-[#EAE5D9]/15" />
                )}
              </div>
            )}
            <div
              className="absolute inset-0 bg-gradient-to-t from-[#0D141B] via-[#0D141B]/45 to-transparent"
              aria-hidden="true"
            />
            <div className="absolute bottom-0 left-0 right-0 p-4">
              <p className="font-[family-name:var(--font-mono)] text-xs text-[#E59A3C]">
                {location.subtitle}
              </p>
              <h1 className="font-[family-name:var(--font-display)] text-2xl font-semibold text-[#EAE5D9] leading-tight">
                {location.name}
              </h1>
            </div>
          </div>

          {/* Energy & Status Meter */}
          <section className="rounded-2xl bg-[#16202A] border border-[#EAE5D9]/12 p-4 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2 font-[family-name:var(--font-mono)] text-xs tabular-nums">
              <span className={energyTextColor}>
                Energy {state.energy} / {state.maxEnergy}
                {isLowEnergy ? ' · LOW ENERGY' : ''}
              </span>
              <span className={state.flags.beaconLit ? 'text-[#F3B35B]' : 'text-[#A9B3BC]'}>
                Turn {state.turn} · Beacon {state.flags.beaconLit ? 'Lit' : 'Dark'}
              </span>
            </div>

            <div className="h-2 w-full overflow-hidden rounded-full bg-[#21303E]">
              <div
                className={`h-full transition-all duration-200 ${energyBarColor}`}
                style={{width: `${energyPct}%`}}
              />
            </div>

            <div className="pt-1 grid grid-cols-2 gap-x-4 gap-y-1.5 text-xs">
              <MilestoneItem label="Sea Chest" done={state.flags.chestOpened} />
              <MilestoneItem label="Bridge Gate" done={state.flags.bridgeUnlocked} />
              <MilestoneItem label="Wall Safe" done={state.flags.safeUnlocked} />
              <MilestoneItem label="Beacon Lit" done={state.flags.beaconLit} />
            </div>
          </section>

          {/* Desktop Cartography Chart */}
          <section className="hidden lg:block rounded-2xl bg-[#16202A] border border-[#EAE5D9]/12 p-4">
            <h2 className="mb-3 text-sm font-semibold text-[#EAE5D9]">Island Chart</h2>
            <IslandMap state={state} />
          </section>
        </aside>

        {/* Right Workspace: Narrative Chronicle & Interactive Console */}
        <section className="flex flex-col rounded-2xl bg-[#16202A] border border-[#EAE5D9]/12 overflow-hidden min-h-[500px] lg:h-[calc(100vh-116px)]">
          {/* Narrative Log */}
          <div className="flex-1 overflow-y-auto px-5 py-5 space-y-3.5">
            {state.log.map((entry) => (
              <LogRow key={entry.id} entry={entry} />
            ))}
            <div ref={logEndRef} />
          </div>

          {/* Interactive Deck & Command Bar */}
          <div className="border-t border-[#EAE5D9]/10 bg-[#131D27] p-4 space-y-3">
            {/* Segmented Mode Tabs */}
            <div className="flex items-center gap-1 rounded-xl bg-[#0D141B] border border-[#EAE5D9]/10 p-1">
              {(['ACTIONS', 'MOVE', 'BAG'] as ConsoleTab[]).map((tab) => {
                const isSelected = activeTab === tab;
                const label =
                  tab === 'BAG'
                    ? `Bag (${state.inventory.length})`
                    : tab === 'MOVE'
                      ? 'Move'
                      : 'Actions';
                return (
                  <button
                    key={tab}
                    type="button"
                    onClick={() => setActiveTab(tab)}
                    className={`flex-1 h-9 rounded-lg text-xs font-semibold transition-colors whitespace-nowrap cursor-pointer ${
                      isSelected
                        ? 'bg-[#21303E] text-[#F3B35B]'
                        : 'text-[#A9B3BC] hover:text-[#EAE5D9]'
                    }`}
                  >
                    {label}
                  </button>
                );
              })}
            </div>

            {/* Tab Panel */}
            <div className="max-h-40 overflow-y-auto pr-1">
              {activeTab === 'ACTIONS' && (
                <div className="flex flex-wrap gap-2">
                  {contextActions.map((action) => (
                    <button
                      key={action.id}
                      type="button"
                      onClick={() =>
                        action.command === 'open_safe_ui'
                          ? onOpenSafeDialog()
                          : onSubmitCommand(action.command)
                      }
                      className={`h-10 rounded-lg px-3.5 text-sm font-semibold transition-colors whitespace-nowrap cursor-pointer ${
                        action.isPrimary
                          ? 'bg-[#E59A3C] text-[#111820] hover:bg-[#F3B35B]'
                          : 'border border-[#EAE5D9]/20 bg-[#192531] text-[#EAE5D9] hover:bg-[#21303E]'
                      }`}
                    >
                      {action.label}
                    </button>
                  ))}
                  <button
                    type="button"
                    onClick={() => onSubmitCommand('look')}
                    className="h-10 rounded-lg border border-[#EAE5D9]/15 px-3.5 text-sm font-medium text-[#A9B3BC] hover:bg-[#21303E] hover:text-[#EAE5D9] transition-colors whitespace-nowrap cursor-pointer"
                  >
                    Look Around
                  </button>
                </div>
              )}

              {activeTab === 'MOVE' && (
                <div className="flex flex-wrap gap-2">
                  {(Object.entries(location.exits) as [Direction, LocationId][]).map(
                    ([dir, destId]) => {
                      const destName = LOCATIONS[destId].name;
                      const isLocked =
                        location.id === 'CHASM_BRIDGE' &&
                        dir === 'EAST' &&
                        !state.flags.bridgeUnlocked;
                      return (
                        <button
                          key={dir}
                          type="button"
                          onClick={() =>
                            onSubmitCommand(`go ${DIRECTION_META[dir].label.toLowerCase()}`)
                          }
                          className={`h-10 rounded-lg border px-3.5 text-sm font-medium transition-colors whitespace-nowrap cursor-pointer ${
                            isLocked
                              ? 'border-[#E06C65]/40 bg-[#E06C65]/10 text-[#E06C65] hover:bg-[#E06C65]/20'
                              : 'border-[#EAE5D9]/20 bg-[#192531] text-[#EAE5D9] hover:bg-[#21303E]'
                          }`}
                        >
                          {DIRECTION_META[dir].shortLabel}: {destName}
                          {isLocked ? ' (Locked)' : ' (-1)'}
                        </button>
                      );
                    },
                  )}
                </div>
              )}

              {activeTab === 'BAG' && (
                <div className="space-y-2">
                  {state.inventory.length === 0 ? (
                    <p className="py-2 text-sm text-[#A9B3BC]">
                      Your canvas satchel is empty. Explore the island to gather tools and rations.
                    </p>
                  ) : (
                    state.inventory.map((itemId) => {
                      const item = ITEMS[itemId];
                      return (
                        <div
                          key={itemId}
                          className="flex items-center justify-between gap-3 rounded-xl bg-[#192531] border border-[#EAE5D9]/10 px-3.5 py-2.5"
                        >
                          <div className="min-w-0">
                            <p className="text-sm font-semibold text-[#EAE5D9] truncate">
                              {item.name}
                            </p>
                            <p className="text-xs text-[#A9B3BC] line-clamp-1">
                              {item.description}
                            </p>
                          </div>
                          {item.isConsumable ? (
                            <button
                              type="button"
                              onClick={() =>
                                onSubmitCommand(`use ${item.shortName.toLowerCase()}`)
                              }
                              className="h-9 shrink-0 rounded-lg bg-[#56B895] px-3 text-xs font-semibold text-[#111820] hover:opacity-90 transition-opacity whitespace-nowrap cursor-pointer"
                            >
                              +{item.energyRestore} Energy
                            </button>
                          ) : (
                            <button
                              type="button"
                              onClick={() =>
                                onSubmitCommand(`examine ${item.shortName.toLowerCase()}`)
                              }
                              className="h-9 shrink-0 rounded-lg border border-[#EAE5D9]/20 px-3 text-xs font-semibold text-[#EAE5D9] hover:bg-[#21303E] transition-colors whitespace-nowrap cursor-pointer"
                            >
                              Inspect
                            </button>
                          )}
                        </div>
                      );
                    })
                  )}
                </div>
              )}
            </div>

            {/* Typed Text Command Bar */}
            <form onSubmit={handleSend} className="flex items-center gap-2">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleInputKeyDown}
                placeholder="Type a command (e.g. go north, look, take crowbar, help)..."
                aria-label="Adventure command input"
                className="h-11 flex-1 rounded-xl border border-[#EAE5D9]/20 bg-[#192531] px-4 text-sm text-[#EAE5D9] placeholder:text-[#778594] focus:border-[#E59A3C] focus:outline-none"
              />
              <button
                type="submit"
                disabled={!input.trim()}
                aria-label="Send command"
                className="inline-flex h-11 w-12 shrink-0 items-center justify-center rounded-xl bg-[#E59A3C] text-[#111820] hover:bg-[#F3B35B] disabled:bg-[#21303E] disabled:text-[#778594] transition-colors cursor-pointer disabled:cursor-not-allowed"
              >
                <Send className="h-4 w-4" />
              </button>
            </form>
          </div>
        </section>
      </main>

      {/* Mobile Chart Dialog */}
      {showMobileMap && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 lg:hidden"
          role="dialog"
          aria-modal="true"
          aria-label="Island Chart"
        >
          <div className="w-full max-w-md rounded-2xl bg-[#192531] border border-[#EAE5D9]/20 p-5">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="font-[family-name:var(--font-display)] text-xl font-semibold text-[#EAE5D9]">
                Mystery Island Chart
              </h2>
              <button
                type="button"
                onClick={() => setShowMobileMap(false)}
                aria-label="Close chart"
                className="flex h-10 w-10 items-center justify-center rounded-lg text-[#A9B3BC] hover:bg-[#21303E] hover:text-[#EAE5D9]"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <IslandMap state={state} />
          </div>
        </div>
      )}

      {/* Safe Combination Dialog */}
      {state.showSafeDialog && (
        <SafeDialModal onSubmit={onSubmitSafeCode} onClose={onDismissSafeDialog} />
      )}

      {/* Restart Confirmation */}
      {showRestartConfirm && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4"
          role="dialog"
          aria-modal="true"
        >
          <div className="w-full max-w-sm rounded-2xl bg-[#192531] border border-[#EAE5D9]/20 p-6">
            <h2 className="font-[family-name:var(--font-display)] text-xl font-semibold text-[#EAE5D9]">
              Restart Adventure?
            </h2>
            <p className="mt-2 text-sm text-[#A9B3BC]">
              Your current progress and collected items on Mystery Island will be reset.
            </p>
            <div className="mt-6 flex gap-3">
              <button
                type="button"
                onClick={() => setShowRestartConfirm(false)}
                className="flex-1 h-11 rounded-lg border border-[#EAE5D9]/20 text-sm font-semibold text-[#EAE5D9] hover:bg-[#21303E] cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowRestartConfirm(false);
                  onRestart();
                }}
                className="flex-1 h-11 rounded-lg bg-[#E59A3C] text-sm font-semibold text-[#111820] hover:bg-[#F3B35B] cursor-pointer"
              >
                Restart
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const MilestoneItem: React.FC<{label: string; done: boolean}> = ({label, done}) => (
  <div className="flex items-center justify-between text-[#A9B3BC]">
    <span>{label}</span>
    <span
      className={`font-[family-name:var(--font-mono)] ${
        done ? 'text-[#56B895]' : 'text-[#778594]'
      }`}
    >
      {done ? 'Done' : 'Pending'}
    </span>
  </div>
);

const LogRow: React.FC<{entry: LogEntry}> = ({entry}) => {
  switch (entry.type) {
    case 'COMMAND':
      return (
        <p className="pt-1 font-[family-name:var(--font-mono)] text-xs font-medium text-[#E59A3C]">
          {entry.text}
        </p>
      );
    case 'DISCOVERY':
      return (
        <div className="rounded-xl bg-[#56B895]/12 border border-[#56B895]/30 px-4 py-3 text-[15px] leading-relaxed text-[#EAE5D9] max-w-[70ch]">
          {entry.text}
        </div>
      );
    case 'WARNING':
      return (
        <div className="rounded-xl bg-[#E06C65]/12 border border-[#E06C65]/35 px-4 py-3 text-[15px] leading-relaxed text-[#EAE5D9] max-w-[70ch]">
          {entry.text}
        </div>
      );
    case 'CLUE':
      return (
        <p className="text-sm italic leading-relaxed text-[#A9B3BC] max-w-[70ch]">
          {entry.text}
        </p>
      );
    case 'NARRATIVE':
    default:
      return (
        <p className="text-[15px] leading-relaxed text-[#EAE5D9] max-w-[70ch]">
          {entry.text}
        </p>
      );
  }
};
