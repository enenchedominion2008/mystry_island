import React from 'react';
import {Direction, GameState, LocationId} from '../game/types';
import {LOCATIONS} from '../game/world';

interface IslandMapProps {
  state: GameState;
}

const SHORT_NAMES: Record<LocationId, string> = {
  STORM_SHORE: 'Shore',
  SHIPWRECK_COVE: 'Cove',
  MISTY_PINE_TRAIL: 'Trail',
  OLD_BOATHOUSE: 'Boathouse',
  CHASM_BRIDGE: 'Bridge',
  KEEPERS_COURTYARD: 'Courtyard',
  KEEPERS_COTTAGE: 'Cottage',
  RESCUE_PIER: 'Pier',
  LIGHTHOUSE_BASE: 'Tower',
  LANTERN_ROOM: 'Lantern',
};

export const IslandMap: React.FC<IslandMapProps> = ({state}) => {
  const padX = 48;
  const padY = 34;
  const stepX = 88;
  const stepY = 74;
  const width = padX * 2 + stepX * 3;
  const height = padY * 2 + stepY * 2 + 12;

  const getCoord = (col: number, row: number) => ({
    x: padX + col * stepX,
    y: padY + row * stepY,
  });

  const visited = new Set(state.visitedLocations);

  const edges: Array<{
    key: string;
    from: {x: number; y: number};
    to: {x: number; y: number};
    known: boolean;
    lockedBridge: boolean;
  }> = [];

  (Object.values(LOCATIONS) as Array<(typeof LOCATIONS)[LocationId]>).forEach((loc) => {
    const from = getCoord(loc.mapCol, loc.mapRow);
    const fromVisited = visited.has(loc.id);
    (Object.entries(loc.exits) as [Direction, LocationId][]).forEach(([dir, destId]) => {
      if (dir === 'EAST' || dir === 'SOUTH' || dir === 'UP') {
        const dest = LOCATIONS[destId];
        const to = getCoord(dest.mapCol, dest.mapRow);
        edges.push({
          key: `${loc.id}-${destId}`,
          from,
          to,
          known: fromVisited || visited.has(destId),
          lockedBridge:
            loc.id === 'CHASM_BRIDGE' && dir === 'EAST' && !state.flags.bridgeUnlocked,
        });
      }
    });
  });

  return (
    <div className="w-full">
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="w-full h-auto rounded-lg bg-[#0D141B] border border-[#EAE5D9]/12"
        role="img"
        aria-label="Chart of Mystery Island"
      >
        {edges.map((edge) => (
          <line
            key={edge.key}
            x1={edge.from.x}
            y1={edge.from.y}
            x2={edge.to.x}
            y2={edge.to.y}
            stroke={
              edge.lockedBridge
                ? '#E06C65'
                : edge.known
                  ? 'rgba(169, 179, 188, 0.55)'
                  : 'rgba(119, 133, 148, 0.22)'
            }
            strokeWidth={edge.lockedBridge ? 2.5 : 1.75}
            strokeDasharray={edge.lockedBridge ? '5 4' : undefined}
          />
        ))}

        {(Object.values(LOCATIONS) as Array<(typeof LOCATIONS)[LocationId]>).map((loc) => {
          const {x, y} = getCoord(loc.mapCol, loc.mapRow);
          const isCurrent = loc.id === state.currentLocation;
          const isVisited = visited.has(loc.id);
          const isLitBeacon = loc.id === 'LANTERN_ROOM' && state.flags.beaconLit;

          return (
            <g key={loc.id}>
              {isLitBeacon && (
                <circle cx={x} cy={y} r={19} fill="rgba(229, 154, 60, 0.22)" />
              )}
              <circle
                cx={x}
                cy={y}
                r={isCurrent ? 9.5 : 7}
                fill={
                  isCurrent
                    ? '#E59A3C'
                    : isLitBeacon
                      ? '#56B895'
                      : isVisited
                        ? '#21303E'
                        : '#131D27'
                }
                stroke={
                  isCurrent
                    ? '#EAE5D9'
                    : isVisited
                      ? '#A9B3BC'
                      : 'rgba(119, 133, 148, 0.45)'
                }
                strokeWidth={1.5}
              />
              {(isVisited || isCurrent) && (
                <text
                  x={x}
                  y={y + 20}
                  textAnchor="middle"
                  fill={isCurrent ? '#F3B35B' : '#EAE5D9'}
                  fontSize="9.5"
                  fontFamily="var(--font-mono)"
                  fontWeight={isCurrent ? '600' : '500'}
                >
                  {SHORT_NAMES[loc.id]}
                </text>
              )}
            </g>
          );
        })}
      </svg>

      <div className="mt-2.5 flex flex-wrap items-center justify-between gap-2 text-xs text-[#A9B3BC]">
        <span>
          {state.visitedLocations.length} / {Object.keys(LOCATIONS).length} charted
        </span>
        <span>Amber: You · Dashed Red: Locked Bridge</span>
      </div>
    </div>
  );
};
