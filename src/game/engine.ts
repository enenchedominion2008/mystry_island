import {
  ContextAction,
  Direction,
  GameState,
  LocationId,
  LogEntry,
  LogType,
  PuzzleFlags,
} from './types';
import {createInitialRoomItems, DIRECTION_META, ITEMS, LOCATIONS} from './world';

const SAFE_COMBINATION = '483';

export const TITLE_ARTWORK_PATH = '/src/assets/images/title_mystery_island_1795078607282.jpg';
export const SHORE_ARTWORK_PATH = '/src/assets/images/loc_storm_shore_1795078608054.jpg';
export const LIGHTHOUSE_ARTWORK_PATH = '/src/assets/images/loc_lighthouse_beacon_1795078608978.jpg';

export function countSolvedPuzzles(flags: PuzzleFlags): number {
  return [
    flags.chestOpened,
    flags.bridgeUnlocked,
    flags.safeUnlocked,
    flags.beaconLit,
  ].filter(Boolean).length;
}

export function newTitleState(soundEnabled = true): GameState {
  return {
    phase: 'TITLE',
    currentLocation: 'STORM_SHORE',
    energy: 20,
    maxEnergy: 24,
    turn: 1,
    inventory: [],
    roomItems: createInitialRoomItems(),
    visitedLocations: ['STORM_SHORE'],
    flags: {
      chestOpened: false,
      bridgeUnlocked: false,
      safeUnlocked: false,
      cotRested: false,
      oilFilled: false,
      crankWound: false,
      beaconLit: false,
    },
    log: [],
    nextLogId: 1,
    showSafeDialog: false,
    soundEnabled,
    lastSoundCue: null,
    endingReason: '',
  };
}

export function startNewGame(soundEnabled = true): GameState {
  const startLoc = LOCATIONS.STORM_SHORE;
  let state: GameState = {
    ...newTitleState(soundEnabled),
    phase: 'PLAYING',
  };

  state = appendLog(
    state,
    'Thunder rolls out toward the horizon. You wake bruised and shivering on dark pebbles as foam hisses around your boots.',
    'NARRATIVE',
  );
  state = appendLog(
    state,
    "Across the headland, the lighthouse stands dark, the gorge bridge is chained shut, and your strength won't last forever. Restore the beacon to summon the rescue boat.",
    'CLUE',
  );
  state = appendLog(state, `${startLoc.name} — ${startLoc.description}`, 'NARRATIVE');
  return describeRoomExtras(state, startLoc.id);
}

export function processCommand(state: GameState, rawInput: string): GameState {
  if (state.phase !== 'PLAYING') return state;
  const trimmed = rawInput.trim();
  if (!trimmed) return state;

  const logged = appendLog({...state, lastSoundCue: null}, `> ${trimmed}`, 'COMMAND');
  const normalized = trimmed.toLowerCase().replace(/[^a-z0-9\s]/g, ' ').trim();
  const tokens = normalized.split(/\s+/).filter(Boolean);
  if (tokens.length === 0) return logged;

  const verb = tokens[0];
  const rest = tokens.slice(1).join(' ');

  const directDir = parseDirection(normalized);
  if (directDir) {
    return move(logged, directDir);
  }

  switch (verb) {
    case 'go':
    case 'walk':
    case 'move':
    case 'head':
    case 'climb':
    case 'cross': {
      const dir = parseDirection(rest);
      if (dir) return move(logged, dir);
      if (rest.includes('pier') && logged.currentLocation === 'KEEPERS_COURTYARD') {
        return move(logged, 'SOUTH');
      }
      if (rest.includes('cottage') && logged.currentLocation === 'KEEPERS_COURTYARD') {
        return move(logged, 'NORTH');
      }
      if (rest.includes('lighthouse') && logged.currentLocation === 'KEEPERS_COURTYARD') {
        return move(logged, 'EAST');
      }
      if (rest.includes('lantern') && logged.currentLocation === 'LIGHTHOUSE_BASE') {
        return move(logged, 'UP');
      }
      return appendLog(
        logged,
        'Which direction? Try North, South, East, West, Up, or Down.',
        'WARNING',
      );
    }
    case 'look':
    case 'l':
    case 'survey':
      return lookAround(logged);
    case 'inventory':
    case 'inv':
    case 'bag':
    case 'items':
    case 'i':
      return showInventory(logged);
    case 'help':
    case '?':
      return showHelp(logged);
    case 'take':
    case 'get':
    case 'grab':
    case 'pick':
    case 'collect':
      return takeItem(logged, rest);
    case 'examine':
    case 'inspect':
    case 'read':
    case 'check':
    case 'x':
    case 'search':
      return examineTarget(logged, rest);
    case 'use':
    case 'apply':
    case 'pour':
    case 'wind':
    case 'fit':
    case 'strike':
    case 'pry':
    case 'unlock':
    case 'open':
      return handleUseOrOpen(logged, verb, rest);
    case 'eat':
    case 'drink':
    case 'consume':
      return consumeItem(logged, rest);
    case 'enter':
    case 'code':
    case 'dial':
    case 'combo':
      return trySafeCode(logged, rest);
    case 'rest':
    case 'sleep':
    case 'nap':
      return restOnCot(logged);
    case 'ring':
      return ringBell(logged);
    case 'light':
    case 'ignite':
    case 'restore':
      return igniteBeacon(logged);
    case 'board':
    case 'escape':
    case 'rescue':
      return boardRescueBoat(logged);
    case 'open_safe_ui':
      return openSafeModal(logged);
    default:
      if (/^\d{3}$/.test(normalized) && logged.currentLocation === 'KEEPERS_COTTAGE') {
        return trySafeCode(logged, normalized);
      }
      return appendLog(
        logged,
        `Unknown command "${trimmed}". Try 'look', 'go north', 'take <item>', 'examine <object>', 'use <item>', or 'help'.`,
        'WARNING',
      );
  }
}

export function openSafeModal(state: GameState): GameState {
  if (state.phase !== 'PLAYING' || state.currentLocation !== 'KEEPERS_COTTAGE') {
    return state;
  }
  if (state.flags.safeUnlocked) {
    return appendLog(state, 'The brass wall safe already stands empty and open.', 'CLUE');
  }
  return {...state, showSafeDialog: true};
}

export function dismissSafeModal(state: GameState): GameState {
  return {...state, showSafeDialog: false};
}

export function submitSafeCombination(state: GameState, code: string): GameState {
  return trySafeCode({...state, showSafeDialog: false}, code);
}

export function toggleSound(state: GameState): GameState {
  return {...state, soundEnabled: !state.soundEnabled, lastSoundCue: null};
}

export function availableContextActions(state: GameState): ContextAction[] {
  if (state.phase !== 'PLAYING') return [];
  const loc = LOCATIONS[state.currentLocation];
  const actions: ContextAction[] = [];

  for (const itemId of state.roomItems[loc.id]) {
    const item = ITEMS[itemId];
    actions.push({
      id: `take_${itemId}`,
      label: `Take ${item.shortName}`,
      isPrimary: true,
      command: `take ${item.name.toLowerCase()}`,
    });
  }

  switch (loc.id) {
    case 'STORM_SHORE':
      if (!state.flags.chestOpened) {
        if (state.inventory.includes('IRON_CROWBAR')) {
          actions.push({
            id: 'pry_chest',
            label: 'Pry Sea Chest (-1 Energy)',
            isPrimary: true,
            command: 'use crowbar on chest',
          });
        } else {
          actions.push({
            id: 'examine_chest',
            label: 'Inspect Sea Chest',
            isPrimary: false,
            command: 'examine chest',
          });
        }
      }
      break;
    case 'SHIPWRECK_COVE':
      actions.push({
        id: 'examine_launch',
        label: 'Inspect Launch',
        isPrimary: false,
        command: 'examine launch',
      });
      break;
    case 'MISTY_PINE_TRAIL':
      actions.push({
        id: 'examine_signpost',
        label: 'Read Signpost',
        isPrimary: false,
        command: 'examine signpost',
      });
      break;
    case 'OLD_BOATHOUSE':
      actions.push({
        id: 'examine_chalkboard',
        label: 'Read Chalkboard',
        isPrimary: false,
        command: 'examine chalkboard',
      });
      break;
    case 'CHASM_BRIDGE':
      if (!state.flags.bridgeUnlocked) {
        if (state.inventory.includes('BRASS_BRIDGE_KEY')) {
          actions.push({
            id: 'unlock_gate',
            label: 'Unlock Bridge Gate',
            isPrimary: true,
            command: 'use key on gate',
          });
        } else {
          actions.push({
            id: 'examine_gate',
            label: 'Inspect Padlock',
            isPrimary: false,
            command: 'examine gate',
          });
        }
      }
      break;
    case 'KEEPERS_COURTYARD':
      actions.push({
        id: 'examine_plaque',
        label: 'Read Sundial',
        isPrimary: false,
        command: 'examine sundial',
      });
      break;
    case 'KEEPERS_COTTAGE':
      actions.push({
        id: 'read_logbook',
        label: 'Read Logbook',
        isPrimary: false,
        command: 'examine logbook',
      });
      if (!state.flags.safeUnlocked) {
        actions.push({
          id: 'open_safe_ui',
          label: 'Unlock Wall Safe',
          isPrimary: true,
          command: 'open_safe_ui',
        });
      }
      if (!state.flags.cotRested) {
        actions.push({
          id: 'rest_cot',
          label: 'Rest on Cot (+4 Energy)',
          isPrimary: false,
          command: 'rest',
        });
      }
      break;
    case 'RESCUE_PIER':
      if (state.flags.beaconLit) {
        actions.push({
          id: 'board_boat',
          label: 'Board Rescue Boat',
          isPrimary: true,
          command: 'board boat',
        });
      } else {
        actions.push({
          id: 'ring_bell',
          label: 'Ring Signal Bell',
          isPrimary: false,
          command: 'ring bell',
        });
      }
      break;
    case 'LIGHTHOUSE_BASE':
      actions.push({
        id: 'read_checklist',
        label: 'Read Ignition Notice',
        isPrimary: false,
        command: 'examine notice',
      });
      break;
    case 'LANTERN_ROOM':
      if (!state.flags.oilFilled && state.inventory.includes('LAMP_OIL_CANISTER')) {
        actions.push({
          id: 'fill_oil',
          label: 'Pour Lamp Oil',
          isPrimary: true,
          command: 'use oil on beacon',
        });
      }
      if (!state.flags.crankWound && state.inventory.includes('CLOCKWORK_CRANK')) {
        actions.push({
          id: 'wind_crank',
          label: 'Wind Brass Crank (-1 Energy)',
          isPrimary: true,
          command: 'use crank on beacon',
        });
      }
      if (
        state.flags.oilFilled &&
        state.flags.crankWound &&
        !state.flags.beaconLit &&
        state.inventory.includes('STORMPROOF_MATCHES')
      ) {
        actions.push({
          id: 'light_beacon',
          label: 'Ignite Beacon',
          isPrimary: true,
          command: 'light beacon',
        });
      }
      actions.push({
        id: 'inspect_lens',
        label: 'Inspect Fresnel Lens',
        isPrimary: false,
        command: 'examine beacon',
      });
      break;
  }

  return actions;
}

function move(state: GameState, dir: Direction): GameState {
  const current = LOCATIONS[state.currentLocation];
  const nextId = current.exits[dir];
  if (!nextId) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      `You cannot go ${DIRECTION_META[dir].label} from here.`,
      'WARNING',
    );
  }

  if (current.id === 'CHASM_BRIDGE' && dir === 'EAST' && !state.flags.bridgeUnlocked) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      "The iron gate is chained shut with a brass padlock stamped 'GORGE GATE'. You need the matching key before crossing.",
      'WARNING',
    );
  }

  const nextLoc = LOCATIONS[nextId];
  const visited = state.visitedLocations.includes(nextId)
    ? state.visitedLocations
    : [...state.visitedLocations, nextId];

  const moved = spendEnergy(
    {
      ...state,
      currentLocation: nextId,
      visitedLocations: visited,
      turn: state.turn + 1,
      lastSoundCue: 'MOVE',
    },
    1,
    'exhausted on the storm-slick trail',
  );

  if (moved.phase === 'GAME_OVER') return moved;

  const withArrival = describeRoomExtras(
    appendLog(moved, `${nextLoc.name} — ${nextLoc.description}`, 'NARRATIVE'),
    nextId,
  );

  if (nextId === 'RESCUE_PIER' && withArrival.flags.beaconLit) {
    return appendLog(
      withArrival,
      'Sweeping beams from the lighthouse cut through the mist! A white Coast Guard cutter is moored alongside the pier, its crew waving you aboard.',
      'DISCOVERY',
    );
  }
  if (nextId === 'RESCUE_PIER') {
    return appendLog(
      withArrival,
      'Far beyond the reef, a faint red masthead light bobs in the dark swell. The rescue boat will not risk the channel while the lighthouse remains dark.',
      'CLUE',
    );
  }
  return withArrival;
}

function lookAround(state: GameState): GameState {
  const loc = LOCATIONS[state.currentLocation];
  return describeRoomExtras(
    appendLog(state, `${loc.name} — ${loc.description}`, 'NARRATIVE'),
    loc.id,
  );
}

function showInventory(state: GameState): GameState {
  if (state.inventory.length === 0) {
    return appendLog(state, 'Your canvas satchel is empty.', 'CLUE');
  }
  const names = state.inventory.map((id) => ITEMS[id].name).join(', ');
  return appendLog(state, `In your bag: ${names}.`, 'CLUE');
}

function showHelp(state: GameState): GameState {
  return appendLog(
    state,
    'Commands: go <north/south/east/west/up/down>, look, take <item>, examine <object>, use <item>, eat/drink <food>, enter <code>, rest, light beacon, board boat, inventory. Moving and heavy physical actions cost 1 Energy; inspecting and reading are free.',
    'CLUE',
  );
}

function takeItem(state: GameState, target: string): GameState {
  const itemsHere = state.roomItems[state.currentLocation];
  if (itemsHere.length === 0) {
    return appendLog(state, 'There is nothing here to pick up.', 'WARNING');
  }

  const matchedId =
    !target && itemsHere.length === 1
      ? itemsHere[0]
      : itemsHere.find((itemId) => {
          const item = ITEMS[itemId];
          const lower = item.name.toLowerCase();
          return (
            lower.includes(target) ||
            item.shortName.toLowerCase().includes(target) ||
            target.split(' ').some((word) => word.length > 2 && lower.includes(word))
          );
        });

  if (!matchedId) {
    const available = itemsHere.map((id) => ITEMS[id].name).join(', ');
    return appendLog(
      state,
      `You don't see "${target}" here. Visible items: ${available}.`,
      'WARNING',
    );
  }

  const item = ITEMS[matchedId];
  return appendLog(
    {
      ...state,
      inventory: [...state.inventory, matchedId],
      roomItems: {
        ...state.roomItems,
        [state.currentLocation]: itemsHere.filter((id) => id !== matchedId),
      },
      turn: state.turn + 1,
      lastSoundCue: 'PICKUP',
    },
    `Picked up ${item.name}. ${item.description}`,
    'DISCOVERY',
  );
}

function examineTarget(state: GameState, target: string): GameState {
  if (!target || target === 'room' || target === 'around') {
    return lookAround(state);
  }

  const combinedItems = [...state.inventory, ...state.roomItems[state.currentLocation]];
  const matchingItemId = combinedItems.find((itemId) => {
    const item = ITEMS[itemId];
    return (
      item.name.toLowerCase().includes(target) || item.shortName.toLowerCase().includes(target)
    );
  });

  if (matchingItemId) {
    const item = ITEMS[matchingItemId];
    return appendLog(state, `${item.name}: ${item.description}`, 'CLUE');
  }

  const loc = LOCATIONS[state.currentLocation];
  const interactable = loc.interactables.find(
    (entry) =>
      entry.id.toLowerCase().includes(target) ||
      entry.label.toLowerCase().includes(target) ||
      target.includes(entry.id.toLowerCase()),
  );

  if (interactable) {
    switch (interactable.id) {
      case 'chest':
        return appendLog(
          state,
          state.flags.chestOpened
            ? 'The oak sea chest lies pried open and empty.'
            : interactable.description,
          'CLUE',
        );
      case 'gate':
        return appendLog(
          state,
          state.flags.bridgeUnlocked
            ? 'The iron gate hangs open; the brass padlock and chain lie beside the post.'
            : interactable.description,
          'CLUE',
        );
      case 'safe':
        return appendLog(
          state,
          state.flags.safeUnlocked
            ? 'The brass wall safe stands open and empty.'
            : `${interactable.description} Use 'enter <code>' or click 'Unlock Wall Safe' once you learn the combination.`,
          'CLUE',
        );
      case 'beacon':
        return describeBeaconStatus(state);
      default:
        return appendLog(state, `${interactable.label}: ${interactable.description}`, 'CLUE');
    }
  }

  return appendLog(state, `You see nothing special about "${target}" here.`, 'WARNING');
}

function handleUseOrOpen(state: GameState, verb: string, rest: string): GameState {
  if (!rest) {
    return appendLog(state, `What would you like to ${verb}?`, 'WARNING');
  }

  if (
    (rest.includes('hardtack') || rest.includes('biscuit')) &&
    state.inventory.includes('TIN_OF_HARDTACK')
  ) {
    return consumeItem(state, 'hardtack');
  }
  if ((rest.includes('fig') || rest.includes('pouch')) && state.inventory.includes('DRIED_FIGS')) {
    return consumeItem(state, 'figs');
  }
  if (
    (rest.includes('tea') || rest.includes('thermos')) &&
    state.inventory.includes('GINGER_TEA_THERMOS')
  ) {
    return consumeItem(state, 'tea');
  }

  if (
    state.currentLocation === 'STORM_SHORE' &&
    (rest.includes('crowbar') || rest.includes('chest') || rest.includes('pry'))
  ) {
    return prySeaChest(state);
  }

  if (
    state.currentLocation === 'CHASM_BRIDGE' &&
    (rest.includes('key') ||
      rest.includes('gate') ||
      rest.includes('bridge') ||
      rest.includes('padlock'))
  ) {
    return unlockBridgeGate(state);
  }

  if (state.currentLocation === 'KEEPERS_COTTAGE' && rest.includes('safe')) {
    const match = rest.match(/\d{3}/);
    return match ? trySafeCode(state, match[0]) : openSafeModal(state);
  }

  if (state.currentLocation === 'LANTERN_ROOM') {
    if (rest.includes('oil') || rest.includes('canister') || rest.includes('reservoir')) {
      return fillLampOil(state);
    }
    if (rest.includes('crank') || rest.includes('clockwork') || rest.includes('wind')) {
      return windBrassCrank(state);
    }
    if (
      rest.includes('match') ||
      rest.includes('beacon') ||
      rest.includes('lens') ||
      rest.includes('lamp') ||
      rest.includes('mantle')
    ) {
      return igniteBeacon(state);
    }
  }

  if (
    state.currentLocation === 'RESCUE_PIER' &&
    (rest.includes('boat') || rest.includes('cutter') || rest.includes('rescue'))
  ) {
    return boardRescueBoat(state);
  }

  return appendLog(state, `You can't figure out how to ${verb} "${rest}" here.`, 'WARNING');
}

function prySeaChest(state: GameState): GameState {
  if (state.flags.chestOpened) {
    return appendLog(state, 'The sea chest is already open.', 'CLUE');
  }
  if (!state.inventory.includes('IRON_CROWBAR')) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      "The iron clasp is rusted solid. You'll need a sturdy pry tool like a crowbar.",
      'WARNING',
    );
  }

  const afterEnergy = spendEnergy(
    {
      ...state,
      flags: {...state.flags, chestOpened: true},
      inventory: [...state.inventory, 'BRASS_BRIDGE_KEY'],
      turn: state.turn + 1,
      lastSoundCue: 'SOLVE',
    },
    1,
    'exhausted forcing the sea chest',
  );

  if (afterEnergy.phase === 'GAME_OVER') return afterEnergy;

  return appendLog(
    afterEnergy,
    "With a sharp crack of splintering oak, the Iron Crowbar snaps the rusted clasp! Inside lies a Heavy Brass Key stamped 'GORGE GATE'. (Added Heavy Brass Key to your bag.)",
    'DISCOVERY',
  );
}

function unlockBridgeGate(state: GameState): GameState {
  if (state.flags.bridgeUnlocked) {
    return appendLog(state, 'The bridge gate is already unlocked.', 'CLUE');
  }
  if (!state.inventory.includes('BRASS_BRIDGE_KEY')) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      "The padlock won't budge without the key stamped 'GORGE GATE'.",
      'WARNING',
    );
  }

  return appendLog(
    {
      ...state,
      flags: {...state.flags, bridgeUnlocked: true},
      inventory: state.inventory.filter((id) => id !== 'BRASS_BRIDGE_KEY'),
      turn: state.turn + 1,
      lastSoundCue: 'SOLVE',
    },
    'The Heavy Brass Key turns with a satisfying clunk. The chains fall away and the iron gate swings open—you can now cross East over the Chasm Bridge!',
    'DISCOVERY',
  );
}

function trySafeCode(state: GameState, rawCode: string): GameState {
  if (state.currentLocation !== 'KEEPERS_COTTAGE') {
    return appendLog(state, 'There is no combination safe here.', 'WARNING');
  }
  if (state.flags.safeUnlocked) {
    return appendLog(state, 'The brass wall safe is already unlocked.', 'CLUE');
  }
  const digits = rawCode.replace(/[^0-9]/g, '');
  if (digits.length !== 3) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      'The brass safe requires a 3-digit combination.',
      'WARNING',
    );
  }
  if (digits !== SAFE_COMBINATION) {
    return appendLog(
      {...state, turn: state.turn + 1, lastSoundCue: 'WARNING'},
      `You dial ${digits} and tug the handle—nothing happens. Check the Keeper's Logbook for the three storm digits.`,
      'WARNING',
    );
  }

  return appendLog(
    {
      ...state,
      flags: {...state.flags, safeUnlocked: true},
      inventory: [...state.inventory, 'CLOCKWORK_CRANK', 'STORMPROOF_MATCHES'],
      turn: state.turn + 1,
      lastSoundCue: 'SOLVE',
    },
    'Click! Dialing 4-8-3 releases the brass tumblers. Inside the safe you find a Clockwork Brass Crank and a brass tube of Stormproof Matches! (Added both to your bag.)',
    'DISCOVERY',
  );
}

function restOnCot(state: GameState): GameState {
  if (state.currentLocation !== 'KEEPERS_COTTAGE') {
    return appendLog(state, 'There is nowhere dry to rest here.', 'WARNING');
  }
  if (state.flags.cotRested) {
    return appendLog(
      state,
      "You've already caught your breath here; lingering longer won't help.",
      'CLUE',
    );
  }
  const restored = Math.min(state.energy + 4, state.maxEnergy);
  const gained = restored - state.energy;
  return appendLog(
    {
      ...state,
      energy: restored,
      flags: {...state.flags, cotRested: true},
      turn: state.turn + 1,
      lastSoundCue: 'RESTORE',
    },
    `You catch your breath beneath the dry wool blanket for a few minutes (+${gained} Energy, now ${restored}/${state.maxEnergy}).`,
    'DISCOVERY',
  );
}

function fillLampOil(state: GameState): GameState {
  if (state.currentLocation !== 'LANTERN_ROOM') {
    return appendLog(state, 'The lighthouse burner is up in the Lantern Room.', 'WARNING');
  }
  if (state.flags.oilFilled) {
    return appendLog(state, 'The brass reservoir is already brimming with lamp oil.', 'CLUE');
  }
  if (!state.inventory.includes('LAMP_OIL_CANISTER')) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      "You don't have any lamp oil to pour into the reservoir.",
      'WARNING',
    );
  }
  return appendLog(
    {
      ...state,
      flags: {...state.flags, oilFilled: true},
      inventory: state.inventory.filter((id) => id !== 'LAMP_OIL_CANISTER'),
      turn: state.turn + 1,
      lastSoundCue: 'SOLVE',
    },
    'You uncap the canister and pour fresh lamp oil into the brass reservoir until the wick draws clear fuel.',
    'DISCOVERY',
  );
}

function windBrassCrank(state: GameState): GameState {
  if (state.currentLocation !== 'LANTERN_ROOM') {
    return appendLog(state, 'The Fresnel clockwork drive is up in the Lantern Room.', 'WARNING');
  }
  if (state.flags.crankWound) {
    return appendLog(state, 'The clockwork weight is already wound tight.', 'CLUE');
  }
  if (!state.inventory.includes('CLOCKWORK_CRANK')) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      'The drive shaft is missing its Clockwork Brass Crank.',
      'WARNING',
    );
  }

  const afterEnergy = spendEnergy(
    {
      ...state,
      flags: {...state.flags, crankWound: true},
      inventory: state.inventory.filter((id) => id !== 'CLOCKWORK_CRANK'),
      turn: state.turn + 1,
      lastSoundCue: 'SOLVE',
    },
    1,
    'exhausted winding the heavy Fresnel clockwork',
  );

  if (afterEnergy.phase === 'GAME_OVER') return afterEnergy;

  return appendLog(
    afterEnergy,
    'You slot the Brass Crank onto the drive shaft and crank hard against the heavy counterweight. Gears mesh smoothly—the Fresnel mechanism is wound and ready!',
    'DISCOVERY',
  );
}

function igniteBeacon(state: GameState): GameState {
  if (state.currentLocation !== 'LANTERN_ROOM') {
    return appendLog(state, 'You need to climb to the Lantern Room to ignite the beacon.', 'WARNING');
  }
  if (state.flags.beaconLit) {
    return appendLog(
      state,
      'The Fresnel beacon is already blazing across the sea! Head down to the Rescue Pier to board the cutter.',
      'CLUE',
    );
  }
  if (!state.flags.oilFilled) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      'The oil reservoir is bone dry. Pour Lamp Oil in before lighting the wick.',
      'WARNING',
    );
  }
  if (!state.flags.crankWound) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      "The lens rotation mechanism isn't wound yet. Fit and wind the Brass Crank first.",
      'WARNING',
    );
  }
  if (!state.inventory.includes('STORMPROOF_MATCHES')) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      'You need Stormproof Matches to ignite the burner.',
      'WARNING',
    );
  }

  const boostedEnergy = Math.min(state.energy + 3, state.maxEnergy);
  const litState = appendLog(
    {
      ...state,
      flags: {...state.flags, beaconLit: true},
      inventory: state.inventory.filter((id) => id !== 'STORMPROOF_MATCHES'),
      energy: boostedEnergy,
      turn: state.turn + 1,
      lastSoundCue: 'SOLVE',
    },
    'You strike a stormproof match and touch it to the primed mantle. A brilliant golden flame blooms inside the crystal Fresnel lens, sweeping a triumphant beam across the black ocean!',
    'DISCOVERY',
  );

  return appendLog(
    litState,
    'Seconds later, two deep foghorn blasts echo up from the harbor—the rescue boat has spotted your signal and is docking at the Rescue Pier! The warm lantern light steadies your hands (+3 Energy).',
    'DISCOVERY',
  );
}

function ringBell(state: GameState): GameState {
  if (state.currentLocation !== 'RESCUE_PIER') {
    return appendLog(state, 'There is no harbor bell here.', 'WARNING');
  }
  if (state.flags.beaconLit) {
    return appendLog(
      {...state, lastSoundCue: 'MOVE'},
      "You clang the bronze bell—the cutter's crew flashes a deck spotlight back from the pier end. You're ready to board!",
      'DISCOVERY',
    );
  }
  return appendLog(
    {...state, lastSoundCue: 'WARNING'},
    "The bronze bell tolls across the dark water, swallowed by the surf. The rescue boat won't enter the reef channel until the lighthouse beacon is lit.",
    'CLUE',
  );
}

function boardRescueBoat(state: GameState): GameState {
  if (state.currentLocation !== 'RESCUE_PIER') {
    return appendLog(state, 'You can only board the rescue boat from the Rescue Pier.', 'WARNING');
  }
  if (!state.flags.beaconLit) {
    return appendLog(
      {...state, lastSoundCue: 'WARNING'},
      'The rescue cutter is still holding out beyond the reef. Restore the lighthouse beacon first!',
      'WARNING',
    );
  }
  return appendLog(
    {
      ...state,
      phase: 'VICTORY',
      lastSoundCue: 'VICTORY',
      endingReason:
        'With the golden beacon sweeping steadily over Mystery Island, you step onto the warm deck of the Coast Guard cutter and leave the storm-lashed rocks behind.',
    },
    "You step aboard the Coast Guard cutter as the crew pulls in the mooring lines. Behind you, Mystery Island's beacon sweeps steadily across the clearing sea.",
    'DISCOVERY',
  );
}

function consumeItem(state: GameState, target: string): GameState {
  if (!target) {
    return appendLog(state, 'What ration would you like to consume?', 'WARNING');
  }
  const matched = state.inventory.find((itemId) => {
    const item = ITEMS[itemId];
    return (
      item.isConsumable &&
      (item.name.toLowerCase().includes(target) || item.shortName.toLowerCase().includes(target))
    );
  });

  if (!matched) {
    return appendLog(state, `You don't have "${target}" to eat or drink.`, 'WARNING');
  }

  const item = ITEMS[matched];
  const nextEnergy = Math.min(state.energy + item.energyRestore, state.maxEnergy);
  const gained = nextEnergy - state.energy;

  return appendLog(
    {
      ...state,
      energy: nextEnergy,
      inventory: state.inventory.filter((id) => id !== matched),
      turn: state.turn + 1,
      lastSoundCue: 'RESTORE',
    },
    `You consume the ${item.name}, restoring +${gained} Energy (now ${nextEnergy}/${state.maxEnergy}).`,
    'DISCOVERY',
  );
}

function describeBeaconStatus(state: GameState): GameState {
  const oilStatus = state.flags.oilFilled ? 'Oil reservoir full' : 'Oil reservoir empty';
  const crankStatus = state.flags.crankWound ? 'Clockwork wound' : 'Brass crank missing';
  const flameStatus = state.flags.beaconLit ? 'Beacon blazing' : 'Mantle unlit';
  return appendLog(
    state,
    `Fresnel Lens status: ${oilStatus} · ${crankStatus} · ${flameStatus}.`,
    'CLUE',
  );
}

function describeRoomExtras(state: GameState, locationId: LocationId): GameState {
  const loc = LOCATIONS[locationId];
  const itemsHere = state.roomItems[locationId];
  let updated = state;
  if (itemsHere.length > 0) {
    const itemList = itemsHere.map((id) => ITEMS[id].name).join(', ');
    updated = appendLog(updated, `You notice here: ${itemList}.`, 'DISCOVERY');
  }
  const exitsSummary = (Object.entries(loc.exits) as [Direction, LocationId][])
    .map(([dir, dest]) => {
      const destName = LOCATIONS[dest].name;
      const lockNote =
        loc.id === 'CHASM_BRIDGE' && dir === 'EAST' && !updated.flags.bridgeUnlocked
          ? ' (Locked)'
          : '';
      return `${DIRECTION_META[dir].label}: ${destName}${lockNote}`;
    })
    .join(' · ');

  return appendLog(updated, `Exits — ${exitsSummary}`, 'CLUE');
}

function spendEnergy(state: GameState, amount: number, reason: string): GameState {
  const nextEnergy = Math.max(0, state.energy - amount);
  if (nextEnergy <= 0) {
    return appendLog(
      {
        ...state,
        energy: 0,
        phase: 'GAME_OVER',
        lastSoundCue: 'DEFEAT',
        endingReason: `Your last reserve of strength gives out (${reason}). Cold rain settles over Mystery Island as the lighthouse remains dark.`,
      },
      'Your knees buckle and the dark horizon spins. You have run out of energy before reaching rescue.',
      'WARNING',
    );
  }
  const withEnergy = {...state, energy: nextEnergy};
  if (nextEnergy <= 4) {
    return appendLog(
      {...withEnergy, lastSoundCue: 'WARNING'},
      `Low energy warning (${nextEnergy}/${state.maxEnergy} left). Eat rations or rest soon.`,
      'WARNING',
    );
  }
  return withEnergy;
}

function appendLog(state: GameState, text: string, type: LogType): GameState {
  const entry: LogEntry = {id: state.nextLogId, text, type};
  return {
    ...state,
    log: [...state.log, entry],
    nextLogId: state.nextLogId + 1,
  };
}

function parseDirection(input: string): Direction | null {
  switch (input.trim()) {
    case 'n':
    case 'north':
      return 'NORTH';
    case 's':
    case 'south':
      return 'SOUTH';
    case 'e':
    case 'east':
      return 'EAST';
    case 'w':
    case 'west':
      return 'WEST';
    case 'u':
    case 'up':
    case 'stairs':
    case 'staircase':
      return 'UP';
    case 'd':
    case 'down':
      return 'DOWN';
    default:
      return null;
  }
}
