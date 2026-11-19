package com.example.model

import androidx.annotation.DrawableRes

enum class Direction(val label: String, val shortLabel: String) {
  NORTH("North", "N"),
  SOUTH("South", "S"),
  EAST("East", "E"),
  WEST("West", "W"),
  UP("Up", "U"),
  DOWN("Down", "D"),
}

enum class GamePhase {
  TITLE,
  PLAYING,
  VICTORY,
  GAME_OVER,
}

enum class LocationId {
  STORM_SHORE,
  SHIPWRECK_COVE,
  MISTY_PINE_TRAIL,
  OLD_BOATHOUSE,
  CHASM_BRIDGE,
  KEEPERS_COURTYARD,
  KEEPERS_COTTAGE,
  RESCUE_PIER,
  LIGHTHOUSE_BASE,
  LANTERN_ROOM,
}

enum class ItemId {
  IRON_CROWBAR,
  TIN_OF_HARDTACK,
  BRASS_BRIDGE_KEY,
  LAMP_OIL_CANISTER,
  DRIED_FIGS,
  GINGER_TEA_THERMOS,
  CLOCKWORK_CRANK,
  STORMPROOF_MATCHES,
}

enum class LogType {
  NARRATIVE,
  COMMAND,
  DISCOVERY,
  WARNING,
  CLUE,
}

data class Item(
  val id: ItemId,
  val name: String,
  val shortName: String,
  val description: String,
  val energyRestore: Int = 0,
  val isConsumable: Boolean = false,
)

data class RoomInteractable(
  val id: String,
  val label: String,
  val description: String,
)

data class Location(
  val id: LocationId,
  val name: String,
  val subtitle: String,
  val description: String,
  val mapCol: Int,
  val mapRow: Int,
  val exits: Map<Direction, LocationId>,
  val initialItems: List<ItemId> = emptyList(),
  val interactables: List<RoomInteractable> = emptyList(),
  @DrawableRes val artworkRes: Int? = null,
)

data class LogEntry(
  val id: Long,
  val text: String,
  val type: LogType,
)

data class PuzzleFlags(
  val chestOpened: Boolean = false,
  val bridgeUnlocked: Boolean = false,
  val safeUnlocked: Boolean = false,
  val cotRested: Boolean = false,
  val oilFilled: Boolean = false,
  val crankWound: Boolean = false,
  val beaconLit: Boolean = false,
) {
  val solvedCount: Int
    get() =
      listOf(chestOpened, bridgeUnlocked, safeUnlocked, beaconLit).count { it }
}

data class ContextAction(
  val id: String,
  val label: String,
  val isPrimary: Boolean = false,
  val command: String,
)

enum class SoundCue {
  MOVE,
  PICKUP,
  SOLVE,
  WARNING,
  RESTORE,
  VICTORY,
  DEFEAT,
}

data class GameState(
  val phase: GamePhase = GamePhase.TITLE,
  val currentLocation: LocationId = LocationId.STORM_SHORE,
  val energy: Int = 20,
  val maxEnergy: Int = 24,
  val turn: Int = 1,
  val inventory: List<ItemId> = emptyList(),
  val roomItems: Map<LocationId, List<ItemId>> = emptyMap(),
  val visitedLocations: Set<LocationId> = setOf(LocationId.STORM_SHORE),
  val flags: PuzzleFlags = PuzzleFlags(),
  val log: List<LogEntry> = emptyList(),
  val nextLogId: Long = 1L,
  val showSafeDialog: Boolean = false,
  val soundEnabled: Boolean = true,
  val lastSoundCue: SoundCue? = null,
  val endingReason: String = "",
) {
  val isLowEnergy: Boolean
    get() = energy <= 5
}
