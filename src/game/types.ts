export type Direction = 'NORTH' | 'SOUTH' | 'EAST' | 'WEST' | 'UP' | 'DOWN';

export type GamePhase = 'TITLE' | 'PLAYING' | 'VICTORY' | 'GAME_OVER';

export type LocationId =
  | 'STORM_SHORE'
  | 'SHIPWRECK_COVE'
  | 'MISTY_PINE_TRAIL'
  | 'OLD_BOATHOUSE'
  | 'CHASM_BRIDGE'
  | 'KEEPERS_COURTYARD'
  | 'KEEPERS_COTTAGE'
  | 'RESCUE_PIER'
  | 'LIGHTHOUSE_BASE'
  | 'LANTERN_ROOM';

export type ItemId =
  | 'IRON_CROWBAR'
  | 'TIN_OF_HARDTACK'
  | 'BRASS_BRIDGE_KEY'
  | 'LAMP_OIL_CANISTER'
  | 'DRIED_FIGS'
  | 'GINGER_TEA_THERMOS'
  | 'CLOCKWORK_CRANK'
  | 'STORMPROOF_MATCHES';

export type LogType = 'NARRATIVE' | 'COMMAND' | 'DISCOVERY' | 'WARNING' | 'CLUE';

export type SoundCue =
  | 'MOVE'
  | 'PICKUP'
  | 'SOLVE'
  | 'WARNING'
  | 'RESTORE'
  | 'VICTORY'
  | 'DEFEAT';

export interface Item {
  id: ItemId;
  name: string;
  shortName: string;
  description: string;
  energyRestore: number;
  isConsumable: boolean;
}

export interface RoomInteractable {
  id: string;
  label: string;
  description: string;
}

export interface Location {
  id: LocationId;
  name: string;
  subtitle: string;
  description: string;
  mapCol: number;
  mapRow: number;
  exits: Partial<Record<Direction, LocationId>>;
  initialItems: ItemId[];
  interactables: RoomInteractable[];
  artworkPath?: string;
}

export interface LogEntry {
  id: number;
  text: string;
  type: LogType;
}

export interface PuzzleFlags {
  chestOpened: boolean;
  bridgeUnlocked: boolean;
  safeUnlocked: boolean;
  cotRested: boolean;
  oilFilled: boolean;
  crankWound: boolean;
  beaconLit: boolean;
}

export interface ContextAction {
  id: string;
  label: string;
  isPrimary: boolean;
  command: string;
}

export interface GameState {
  phase: GamePhase;
  currentLocation: LocationId;
  energy: number;
  maxEnergy: number;
  turn: number;
  inventory: ItemId[];
  roomItems: Record<LocationId, ItemId[]>;
  visitedLocations: LocationId[];
  flags: PuzzleFlags;
  log: LogEntry[];
  nextLogId: number;
  showSafeDialog: boolean;
  soundEnabled: boolean;
  lastSoundCue: SoundCue | null;
  endingReason: string;
}
