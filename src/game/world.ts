import {Direction, Item, ItemId, Location, LocationId} from './types';

export const DIRECTION_META: Record<Direction, {label: string; shortLabel: string}> = {
  NORTH: {label: 'North', shortLabel: 'N'},
  SOUTH: {label: 'South', shortLabel: 'S'},
  EAST: {label: 'East', shortLabel: 'E'},
  WEST: {label: 'West', shortLabel: 'W'},
  UP: {label: 'Up', shortLabel: 'U'},
  DOWN: {label: 'Down', shortLabel: 'D'},
};

export const ITEMS: Record<ItemId, Item> = {
  IRON_CROWBAR: {
    id: 'IRON_CROWBAR',
    name: 'Iron Crowbar',
    shortName: 'Crowbar',
    description:
      "A heavy forged pry bar salvaged from a ship's locker. Useful for forcing rusted clasps.",
    energyRestore: 0,
    isConsumable: false,
  },
  TIN_OF_HARDTACK: {
    id: 'TIN_OF_HARDTACK',
    name: 'Tin of Hardtack',
    shortName: 'Hardtack',
    description: 'Salt-baked ship biscuits sealed in waxed tin. Dry, but restores +5 Energy.',
    energyRestore: 5,
    isConsumable: true,
  },
  BRASS_BRIDGE_KEY: {
    id: 'BRASS_BRIDGE_KEY',
    name: 'Heavy Brass Key',
    shortName: 'Brass Key',
    description: "A tarnished warded key stamped 'GORGE GATE'. Fits the bridge padlock.",
    energyRestore: 0,
    isConsumable: false,
  },
  LAMP_OIL_CANISTER: {
    id: 'LAMP_OIL_CANISTER',
    name: 'Canister of Lamp Oil',
    shortName: 'Lamp Oil',
    description:
      'A copper-spouted canister sloshing with refined kerosene for the lighthouse burner.',
    energyRestore: 0,
    isConsumable: false,
  },
  DRIED_FIGS: {
    id: 'DRIED_FIGS',
    name: 'Pouch of Dried Figs',
    shortName: 'Dried Figs',
    description: 'Sweet preserved figs kept dry in oiled linen. Restores +4 Energy.',
    energyRestore: 4,
    isConsumable: true,
  },
  GINGER_TEA_THERMOS: {
    id: 'GINGER_TEA_THERMOS',
    name: 'Thermos of Ginger Tea',
    shortName: 'Ginger Tea',
    description: "Still faintly warm spiced tea from the keeper's stove. Restores +7 Energy.",
    energyRestore: 7,
    isConsumable: true,
  },
  CLOCKWORK_CRANK: {
    id: 'CLOCKWORK_CRANK',
    name: 'Clockwork Brass Crank',
    shortName: 'Brass Crank',
    description: 'A precision-toothed winding crank for rotating the lighthouse Fresnel lens.',
    energyRestore: 0,
    isConsumable: false,
  },
  STORMPROOF_MATCHES: {
    id: 'STORMPROOF_MATCHES',
    name: 'Stormproof Matches',
    shortName: 'Matches',
    description: 'Thick red-tipped maritime matches sealed in a waterproof brass tube.',
    energyRestore: 0,
    isConsumable: false,
  },
};

export const LOCATIONS: Record<LocationId, Location> = {
  STORM_SHORE: {
    id: 'STORM_SHORE',
    name: 'Storm-Swept Shore',
    subtitle: 'Southern Pebble Cove',
    description:
      'Cold basalt pebbles glisten under a breaking twilight sky. Tangled kelp and splintered driftwood litter the tideline where the waves cast you ashore. High on the ridge above, the unlit silhouette of the lighthouse looms over the surf.',
    mapCol: 0,
    mapRow: 2,
    exits: {
      NORTH: 'MISTY_PINE_TRAIL',
      EAST: 'SHIPWRECK_COVE',
    },
    initialItems: [],
    interactables: [
      {
        id: 'chest',
        label: 'Sea Chest',
        description:
          'An oak-and-iron sea chest wedged among the stones. Its rusted clasp is swollen shut from saltwater.',
      },
    ],
    artworkPath: '/src/assets/images/loc_storm_shore_1795078608054.jpg',
  },
  SHIPWRECK_COVE: {
    id: 'SHIPWRECK_COVE',
    name: 'Shipwreck Cove',
    subtitle: 'Jagged Sea Stacks',
    description:
      'Half of a cedar fishing launch lies wedged between black sea stacks, its ribs creaking in the receding swell. Spray mists the rocks around a shattered stern locker.',
    mapCol: 1,
    mapRow: 2,
    exits: {
      WEST: 'STORM_SHORE',
    },
    initialItems: ['IRON_CROWBAR', 'TIN_OF_HARDTACK'],
    interactables: [
      {
        id: 'launch',
        label: 'Wrecked Launch',
        description:
          "Scratched onto a loose deck plank in charcoal: 'Sea chest washed down from the boathouse—only iron opens iron.'",
      },
    ],
  },
  MISTY_PINE_TRAIL: {
    id: 'MISTY_PINE_TRAIL',
    name: 'Misty Pine Trail',
    subtitle: 'Western Ridge Path',
    description:
      'Wind-bent Sitka spruces drip rainwater onto a mossy stone path. To the north stands a weathered timber boathouse; to the east, the trail narrows toward the coastal chasm.',
    mapCol: 0,
    mapRow: 1,
    exits: {
      SOUTH: 'STORM_SHORE',
      NORTH: 'OLD_BOATHOUSE',
      EAST: 'CHASM_BRIDGE',
    },
    initialItems: [],
    interactables: [
      {
        id: 'signpost',
        label: 'Weathered Signpost',
        description:
          'Three faded wooden arms point: North to BOATHOUSE, East to GORGE BRIDGE & LIGHTHOUSE, South to COVE.',
      },
    ],
  },
  OLD_BOATHOUSE: {
    id: 'OLD_BOATHOUSE',
    name: 'Old Boathouse',
    subtitle: 'Sheltered Tidal Inlet',
    description:
      'A cedar boathouse leans over dark, glassy water. Coiled hemp ropes hang from iron pegs beside a scarred pine workbench smelling faintly of kerosene and salt.',
    mapCol: 0,
    mapRow: 0,
    exits: {
      SOUTH: 'MISTY_PINE_TRAIL',
    },
    initialItems: ['LAMP_OIL_CANISTER', 'DRIED_FIGS'],
    interactables: [
      {
        id: 'chalkboard',
        label: 'Slate Chalkboard',
        description:
          "Neat white chalk reads: 'Spare lamp oil stocked here. Bridge key kept in sea chest—if chest washed out to shore, check cove wreckage for pry bar. — M. Vance'",
      },
    ],
  },
  CHASM_BRIDGE: {
    id: 'CHASM_BRIDGE',
    name: 'Chasm Bridge',
    subtitle: 'Gorge Crossing',
    description:
      'White foam roars thirty feet below a narrow granite chasm. A timber suspension bridge spans the gap toward the lighthouse headland, guarded on this side by a spiked iron gate.',
    mapCol: 1,
    mapRow: 1,
    exits: {
      WEST: 'MISTY_PINE_TRAIL',
      EAST: 'KEEPERS_COURTYARD',
    },
    initialItems: [],
    interactables: [
      {
        id: 'gate',
        label: 'Iron Gate',
        description:
          "Heavy chain links bind the iron gate shut, secured by a thick brass padlock stamped 'GORGE GATE'.",
      },
    ],
    artworkPath: '/src/assets/images/loc_gorge_bridge_1795078607559.jpg',
  },
  KEEPERS_COURTYARD: {
    id: 'KEEPERS_COURTYARD',
    name: "Keeper's Courtyard",
    subtitle: 'Headland Terrace',
    description:
      'Low stone walls shelter a courtyard of salt-stunted grass. To the north sits a whitewashed stone cottage; to the east rises the granite lighthouse tower; to the south steps descend toward the leeward Rescue Pier.',
    mapCol: 2,
    mapRow: 1,
    exits: {
      WEST: 'CHASM_BRIDGE',
      NORTH: 'KEEPERS_COTTAGE',
      EAST: 'LIGHTHOUSE_BASE',
      SOUTH: 'RESCUE_PIER',
    },
    initialItems: [],
    interactables: [
      {
        id: 'plaque',
        label: 'Stone Sundial',
        description:
          "A bronze rim around the sundial reads: 'When the storm darkens the lens, consult the Keeper's Log inside the cottage for the safe combination.'",
      },
    ],
  },
  KEEPERS_COTTAGE: {
    id: 'KEEPERS_COTTAGE',
    name: "Keeper's Cottage",
    subtitle: 'Stone Quarters',
    description:
      'The cottage smells of damp wool and cedar embers. Beside a tidy oak table stands a dry canvas cot, and recessed into the chimney masonry is a sturdy brass wall safe.',
    mapCol: 2,
    mapRow: 0,
    exits: {
      SOUTH: 'KEEPERS_COURTYARD',
    },
    initialItems: ['GINGER_TEA_THERMOS'],
    interactables: [
      {
        id: 'logbook',
        label: "Keeper's Logbook",
        description:
          "The final page reads: 'Storm Protocol — Safe Code: 1st digit = wrecks upon Reef Rock (4). 2nd digit = bells struck at midnight watch (8). 3rd digit = crystal tiers in the lantern crown (3).'",
      },
      {
        id: 'safe',
        label: 'Brass Wall Safe',
        description: 'A heavy brass safe set into the stonework with a three-digit dial.',
      },
      {
        id: 'cot',
        label: 'Dry Cot',
        description: 'A dry woolen blanket folded neatly over a sturdy wooden cot.',
      },
    ],
  },
  RESCUE_PIER: {
    id: 'RESCUE_PIER',
    name: 'Rescue Pier',
    subtitle: 'Leeward Harbor',
    description:
      'Weathered timber pilings extend into a sheltered cove. Beyond the jagged reef mouth, dark swells rise and fall under the clearing mist.',
    mapCol: 2,
    mapRow: 2,
    exits: {
      NORTH: 'KEEPERS_COURTYARD',
    },
    initialItems: [],
    interactables: [
      {
        id: 'bell',
        label: 'Signal Bell',
        description: 'A heavy bronze harbor bell mounted beside the mooring bollard.',
      },
    ],
  },
  LIGHTHOUSE_BASE: {
    id: 'LIGHTHOUSE_BASE',
    name: 'Lighthouse Base',
    subtitle: 'Granite Tower Entry',
    description:
      'Massive dressed-granite blocks curve upward into the mist. Inside the arched iron doorway, a spiral cast-iron staircase climbs steeply toward the lantern room above.',
    mapCol: 3,
    mapRow: 1,
    exits: {
      WEST: 'KEEPERS_COURTYARD',
      UP: 'LANTERN_ROOM',
    },
    initialItems: [],
    interactables: [
      {
        id: 'checklist',
        label: 'Ignition Notice',
        description:
          "A riveted brass plate reads: 'Beacon Ignition Order: 1. Fill reservoir with Lamp Oil. 2. Fit & wind the Clockwork Crank. 3. Ignite mantle with Stormproof Matches.'",
      },
    ],
  },
  LANTERN_ROOM: {
    id: 'LANTERN_ROOM',
    name: 'Lantern Room',
    subtitle: 'Lighthouse Crown',
    description:
      'High above the crashing surf, arched storm panes frame the darkening horizon. At the center stands the towering brass Fresnel lens assembly—dark, cold, and silent.',
    mapCol: 3,
    mapRow: 0,
    exits: {
      DOWN: 'LIGHTHOUSE_BASE',
    },
    initialItems: [],
    interactables: [
      {
        id: 'beacon',
        label: 'Fresnel Lens',
        description:
          'A multi-tiered crystal and brass beacon apparatus mounted on clockwork gears.',
      },
    ],
    artworkPath: '/src/assets/images/loc_lighthouse_beacon_1795078608978.jpg',
  },
};

export function createInitialRoomItems(): Record<LocationId, ItemId[]> {
  const result = {} as Record<LocationId, ItemId[]>;
  for (const key of Object.keys(LOCATIONS) as LocationId[]) {
    result[key] = [...LOCATIONS[key].initialItems];
  }
  return result;
}
