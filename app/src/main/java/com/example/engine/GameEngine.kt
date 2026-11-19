package com.example.engine

import com.example.data.IslandWorld
import com.example.model.ContextAction
import com.example.model.Direction
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.ItemId
import com.example.model.LocationId
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.PuzzleFlags
import com.example.model.SoundCue

object GameEngine {

  private const val SAFE_COMBINATION = "483"

  fun newTitleState(soundEnabled: Boolean = true): GameState =
    GameState(
      phase = GamePhase.TITLE,
      soundEnabled = soundEnabled,
    )

  fun startNewGame(soundEnabled: Boolean = true): GameState {
    val startLocation = IslandWorld.locations.getValue(LocationId.STORM_SHORE)
    val initialState =
      GameState(
        phase = GamePhase.PLAYING,
        currentLocation = LocationId.STORM_SHORE,
        energy = 20,
        maxEnergy = 24,
        turn = 1,
        inventory = emptyList(),
        roomItems = IslandWorld.initialRoomItems(),
        visitedLocations = setOf(LocationId.STORM_SHORE),
        flags = PuzzleFlags(),
        soundEnabled = soundEnabled,
      )
    return initialState
      .appendLog(
        "Thunder rolls out toward the horizon. You wake bruised and shivering on dark pebbles as foam hisses around your boots.",
        LogType.NARRATIVE,
      )
      .appendLog(
        "Across the headland, the lighthouse stands dark, the gorge bridge is chained shut, and your strength won't last forever. Restore the beacon to summon the rescue boat.",
        LogType.CLUE,
      )
      .appendLog(
        "${startLocation.name} — ${startLocation.description}",
        LogType.NARRATIVE,
      )
      .describeRoomExtras(startLocation.id)
  }

  fun processCommand(state: GameState, rawInput: String): GameState {
    if (state.phase != GamePhase.PLAYING) return state
    val trimmed = rawInput.trim()
    if (trimmed.isEmpty()) return state

    val logged = state.copy(lastSoundCue = null).appendLog("> $trimmed", LogType.COMMAND)
    val normalized = trimmed.lowercase().replace(Regex("[^a-z0-9\\s]"), " ").trim()
    val tokens = normalized.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return logged

    val verb = tokens.first()
    val rest = tokens.drop(1).joinToString(" ")

    // Direct directional shortcuts
    parseDirection(normalized)?.let { dir ->
      return move(logged, dir)
    }

    return when (verb) {
      "go", "walk", "move", "head", "climb", "cross" -> {
        val dir = parseDirection(rest)
        if (dir != null) {
          move(logged, dir)
        } else if (rest.contains("pier") && logged.currentLocation == LocationId.KEEPERS_COURTYARD) {
          move(logged, Direction.SOUTH)
        } else if (rest.contains("cottage") && logged.currentLocation == LocationId.KEEPERS_COURTYARD) {
          move(logged, Direction.NORTH)
        } else if (rest.contains("lighthouse") && logged.currentLocation == LocationId.KEEPERS_COURTYARD) {
          move(logged, Direction.EAST)
        } else if (rest.contains("lantern") && logged.currentLocation == LocationId.LIGHTHOUSE_BASE) {
          move(logged, Direction.UP)
        } else {
          logged.appendLog(
            "Which direction? Try North, South, East, West, Up, or Down.",
            LogType.WARNING,
          )
        }
      }
      "look", "l", "survey" -> lookAround(logged)
      "inventory", "inv", "bag", "items", "i" -> showInventory(logged)
      "help", "?" -> showHelp(logged)
      "take", "get", "grab", "pick", "collect" -> takeItem(logged, rest)
      "examine", "inspect", "read", "check", "x", "search" -> examineTarget(logged, rest)
      "use", "apply", "pour", "wind", "fit", "strike", "pry", "unlock", "open" ->
        handleUseOrOpen(logged, verb, rest)
      "eat", "drink", "consume" -> consumeItem(logged, rest)
      "enter", "code", "dial", "combo" -> trySafeCode(logged, rest)
      "rest", "sleep", "nap" -> restOnCot(logged)
      "ring" -> ringBell(logged)
      "light", "ignite", "restore" -> igniteBeacon(logged)
      "board", "escape", "rescue" -> boardRescueBoat(logged)
      "open_safe_ui" -> openSafeModal(logged)
      else -> {
        // Allow typing bare 3-digit safe code in Keeper's Cottage
        if (normalized.matches(Regex("\\d{3}")) && logged.currentLocation == LocationId.KEEPERS_COTTAGE) {
          trySafeCode(logged, normalized)
        } else {
          logged.appendLog(
            "Unknown command \"$trimmed\". Try 'look', 'go north', 'take <item>', 'examine <thing>', 'use <item>', or 'help'.",
            LogType.WARNING,
          )
        }
      }
    }
  }

  fun openSafeModal(state: GameState): GameState {
    if (state.phase != GamePhase.PLAYING) return state
    if (state.currentLocation != LocationId.KEEPERS_COTTAGE) return state
    if (state.flags.safeUnlocked) {
      return state.appendLog("The brass wall safe already stands empty and open.", LogType.CLUE)
    }
    return state.copy(showSafeDialog = true)
  }

  fun dismissSafeModal(state: GameState): GameState = state.copy(showSafeDialog = false)

  fun submitSafeCombination(state: GameState, code: String): GameState {
    val closed = state.copy(showSafeDialog = false)
    return trySafeCode(closed, code)
  }

  fun toggleSound(state: GameState): GameState =
    state.copy(soundEnabled = !state.soundEnabled, lastSoundCue = null)

  fun availableContextActions(state: GameState): List<ContextAction> {
    if (state.phase != GamePhase.PLAYING) return emptyList()
    val loc = IslandWorld.locations.getValue(state.currentLocation)
    val actions = mutableListOf<ContextAction>()

    // Room items to pick up
    val itemsHere = state.roomItems[loc.id].orEmpty()
    for (itemId in itemsHere) {
      val item = IslandWorld.items.getValue(itemId)
      actions +=
        ContextAction(
          id = "take_${itemId.name}",
          label = "Take ${item.shortName}",
          isPrimary = true,
          command = "take ${item.name.lowercase()}",
        )
    }

    // Location-specific puzzle actions
    when (loc.id) {
      LocationId.STORM_SHORE -> {
        if (!state.flags.chestOpened) {
          if (ItemId.IRON_CROWBAR in state.inventory) {
            actions +=
              ContextAction(
                id = "pry_chest",
                label = "Pry Sea Chest (-1 Energy)",
                isPrimary = true,
                command = "use crowbar on chest",
              )
          } else {
            actions +=
              ContextAction(
                id = "examine_chest",
                label = "Inspect Sea Chest",
                command = "examine chest",
              )
          }
        }
      }
      LocationId.SHIPWRECK_COVE -> {
        actions +=
          ContextAction(
            id = "examine_launch",
            label = "Inspect Launch",
            command = "examine launch",
          )
      }
      LocationId.MISTY_PINE_TRAIL -> {
        actions +=
          ContextAction(
            id = "examine_signpost",
            label = "Read Signpost",
            command = "examine signpost",
          )
      }
      LocationId.OLD_BOATHOUSE -> {
        actions +=
          ContextAction(
            id = "examine_chalkboard",
            label = "Read Chalkboard",
            command = "examine chalkboard",
          )
      }
      LocationId.CHASM_BRIDGE -> {
        if (!state.flags.bridgeUnlocked) {
          if (ItemId.BRASS_BRIDGE_KEY in state.inventory) {
            actions +=
              ContextAction(
                id = "unlock_gate",
                label = "Unlock Bridge Gate",
                isPrimary = true,
                command = "use key on gate",
              )
          } else {
            actions +=
              ContextAction(
                id = "examine_gate",
                label = "Inspect Padlock",
                command = "examine gate",
              )
          }
        }
      }
      LocationId.KEEPERS_COURTYARD -> {
        actions +=
          ContextAction(
            id = "examine_plaque",
            label = "Read Sundial",
            command = "examine sundial",
          )
      }
      LocationId.KEEPERS_COTTAGE -> {
        actions +=
          ContextAction(
            id = "read_logbook",
            label = "Read Logbook",
            command = "examine logbook",
          )
        if (!state.flags.safeUnlocked) {
          actions +=
            ContextAction(
              id = "open_safe_ui",
              label = "Unlock Wall Safe",
              isPrimary = true,
              command = "open_safe_ui",
            )
        }
        if (!state.flags.cotRested) {
          actions +=
            ContextAction(
              id = "rest_cot",
              label = "Rest on Cot (+4 Energy)",
              command = "rest",
            )
        }
      }
      LocationId.RESCUE_PIER -> {
        if (state.flags.beaconLit) {
          actions +=
            ContextAction(
              id = "board_boat",
              label = "Board Rescue Boat",
              isPrimary = true,
              command = "board boat",
            )
        } else {
          actions +=
            ContextAction(
              id = "ring_bell",
              label = "Ring Signal Bell",
              command = "ring bell",
            )
        }
      }
      LocationId.LIGHTHOUSE_BASE -> {
        actions +=
          ContextAction(
            id = "read_checklist",
            label = "Read Ignition Notice",
            command = "examine notice",
          )
      }
      LocationId.LANTERN_ROOM -> {
        if (!state.flags.oilFilled && ItemId.LAMP_OIL_CANISTER in state.inventory) {
          actions +=
            ContextAction(
              id = "fill_oil",
              label = "Pour Lamp Oil",
              isPrimary = true,
              command = "use oil on beacon",
            )
        }
        if (!state.flags.crankWound && ItemId.CLOCKWORK_CRANK in state.inventory) {
          actions +=
            ContextAction(
              id = "wind_crank",
              label = "Wind Brass Crank (-1 Energy)",
              isPrimary = true,
              command = "use crank on beacon",
            )
        }
        if (
          state.flags.oilFilled &&
            state.flags.crankWound &&
            !state.flags.beaconLit &&
            ItemId.STORMPROOF_MATCHES in state.inventory
        ) {
          actions +=
            ContextAction(
              id = "light_beacon",
              label = "Ignite Beacon",
              isPrimary = true,
              command = "light beacon",
            )
        }
        actions +=
          ContextAction(
            id = "inspect_lens",
            label = "Inspect Fresnel Lens",
            command = "examine beacon",
          )
      }
    }

    return actions
  }

  private fun move(state: GameState, dir: Direction): GameState {
    val current = IslandWorld.locations.getValue(state.currentLocation)
    val nextId = current.exits[dir]
    if (nextId == null) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("You cannot go ${dir.label} from here.", LogType.WARNING)
    }

    // Check locked bridge gate
    if (
      current.id == LocationId.CHASM_BRIDGE &&
        dir == Direction.EAST &&
        !state.flags.bridgeUnlocked
    ) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "The iron gate is chained shut with a brass padlock stamped 'GORGE GATE'. You need the matching key before crossing.",
          LogType.WARNING,
        )
    }

    val nextLoc = IslandWorld.locations.getValue(nextId)
    val updated =
      state
        .copy(
          currentLocation = nextId,
          visitedLocations = state.visitedLocations + nextId,
          turn = state.turn + 1,
          lastSoundCue = SoundCue.MOVE,
        )
        .spendEnergy(1, "exhausted on the storm-slick trail")

    if (updated.phase == GamePhase.GAME_OVER) {
      return updated
    }

    val withArrival =
      updated
        .appendLog("${nextLoc.name} — ${nextLoc.description}", LogType.NARRATIVE)
        .describeRoomExtras(nextId)

    // Special atmospheric cue on Rescue Pier once beacon is lit
    return if (nextId == LocationId.RESCUE_PIER && withArrival.flags.beaconLit) {
      withArrival.appendLog(
        "Sweeping beams from the lighthouse cut through the mist! A white Coast Guard cutter is moored alongside the pier, its crew waving you aboard.",
        LogType.DISCOVERY,
      )
    } else if (nextId == LocationId.RESCUE_PIER) {
      withArrival.appendLog(
        "Far beyond the reef, a faint red masthead light bobs in the dark swell. The rescue boat will not risk the channel while the lighthouse remains dark.",
        LogType.CLUE,
      )
    } else {
      withArrival
    }
  }

  private fun lookAround(state: GameState): GameState {
    val loc = IslandWorld.locations.getValue(state.currentLocation)
    return state
      .appendLog("${loc.name} — ${loc.description}", LogType.NARRATIVE)
      .describeRoomExtras(loc.id)
  }

  private fun showInventory(state: GameState): GameState {
    if (state.inventory.isEmpty()) {
      return state.appendLog("Your canvas satchel is empty.", LogType.CLUE)
    }
    val names = state.inventory.joinToString(", ") { IslandWorld.items.getValue(it).name }
    return state.appendLog("In your bag: $names.", LogType.CLUE)
  }

  private fun showHelp(state: GameState): GameState =
    state.appendLog(
      "Commands: go <north/south/east/west/up/down>, look, take <item>, examine <object>, use <item>, eat/drink <food>, enter <code>, rest, light beacon, board boat, inventory. Moving and heavy physical actions cost 1 Energy; inspecting and reading are free.",
      LogType.CLUE,
    )

  private fun takeItem(state: GameState, target: String): GameState {
    val itemsHere = state.roomItems[state.currentLocation].orEmpty()
    if (itemsHere.isEmpty()) {
      return state.appendLog("There is nothing here to pick up.", LogType.WARNING)
    }
    val matchedId =
      if (target.isBlank() && itemsHere.size == 1) {
        itemsHere.first()
      } else {
        itemsHere.firstOrNull { itemId ->
          val item = IslandWorld.items.getValue(itemId)
          item.name.lowercase().contains(target) ||
            item.shortName.lowercase().contains(target) ||
            target.split(" ").any { word -> word.length > 2 && item.name.lowercase().contains(word) }
        }
      }

    if (matchedId == null) {
      val available = itemsHere.joinToString(", ") { IslandWorld.items.getValue(it).name }
      return state.appendLog("You don't see \"$target\" here. Visible items: $available.", LogType.WARNING)
    }

    val item = IslandWorld.items.getValue(matchedId)
    val newRoomItems = state.roomItems.toMutableMap()
    newRoomItems[state.currentLocation] = itemsHere - matchedId

    return state
      .copy(
        inventory = state.inventory + matchedId,
        roomItems = newRoomItems,
        turn = state.turn + 1,
        lastSoundCue = SoundCue.PICKUP,
      )
      .appendLog("Picked up ${item.name}. ${item.description}", LogType.DISCOVERY)
  }

  private fun examineTarget(state: GameState, target: String): GameState {
    if (target.isBlank() || target == "room" || target == "around") {
      return lookAround(state)
    }

    // Check inventory & room items first
    val matchingItemId =
      (state.inventory + state.roomItems[state.currentLocation].orEmpty()).firstOrNull { itemId ->
        val item = IslandWorld.items.getValue(itemId)
        item.name.lowercase().contains(target) || item.shortName.lowercase().contains(target)
      }
    if (matchingItemId != null) {
      val item = IslandWorld.items.getValue(matchingItemId)
      return state.appendLog("${item.name}: ${item.description}", LogType.CLUE)
    }

    // Check room interactables
    val loc = IslandWorld.locations.getValue(state.currentLocation)
    val interactable =
      loc.interactables.firstOrNull {
        it.id.lowercase().contains(target) ||
          it.label.lowercase().contains(target) ||
          target.contains(it.id.lowercase())
      }

    if (interactable != null) {
      return when (interactable.id) {
        "chest" -> {
          if (state.flags.chestOpened) {
            state.appendLog("The oak sea chest lies pried open and empty.", LogType.CLUE)
          } else {
            state.appendLog(interactable.description, LogType.CLUE)
          }
        }
        "gate" -> {
          if (state.flags.bridgeUnlocked) {
            state.appendLog("The iron gate hangs open; the brass padlock and chain lie beside the post.", LogType.CLUE)
          } else {
            state.appendLog(interactable.description, LogType.CLUE)
          }
        }
        "safe" -> {
          if (state.flags.safeUnlocked) {
            state.appendLog("The brass wall safe stands open and empty.", LogType.CLUE)
          } else {
            state.appendLog(
              "${interactable.description} Use 'enter <code>' or tap 'Unlock Wall Safe' once you learn the combination.",
              LogType.CLUE,
            )
          }
        }
        "beacon" -> describeBeaconStatus(state)
        else -> state.appendLog("${interactable.label}: ${interactable.description}", LogType.CLUE)
      }
    }

    return state.appendLog("You see nothing special about \"$target\" here.", LogType.WARNING)
  }

  private fun handleUseOrOpen(state: GameState, verb: String, rest: String): GameState {
    if (rest.isBlank()) {
      return state.appendLog("What would you like to $verb?", LogType.WARNING)
    }

    // Handle food consumption via "use"
    if (
      (rest.contains("hardtack") || rest.contains("biscuit")) &&
        ItemId.TIN_OF_HARDTACK in state.inventory
    ) {
      return consumeItem(state, "hardtack")
    }
    if ((rest.contains("fig") || rest.contains("pouch")) && ItemId.DRIED_FIGS in state.inventory) {
      return consumeItem(state, "figs")
    }
    if ((rest.contains("tea") || rest.contains("thermos")) && ItemId.GINGER_TEA_THERMOS in state.inventory) {
      return consumeItem(state, "tea")
    }

    // Handle chest opening on Storm Shore
    if (
      state.currentLocation == LocationId.STORM_SHORE &&
        (rest.contains("crowbar") || rest.contains("chest") || rest.contains("pry"))
    ) {
      return prySeaChest(state)
    }

    // Handle bridge gate unlocking at Chasm Bridge
    if (
      state.currentLocation == LocationId.CHASM_BRIDGE &&
        (rest.contains("key") || rest.contains("gate") || rest.contains("bridge") || rest.contains("padlock"))
    ) {
      return unlockBridgeGate(state)
    }

    // Handle Cottage Safe
    if (state.currentLocation == LocationId.KEEPERS_COTTAGE && rest.contains("safe")) {
      val digits = Regex("\\d{3}").find(rest)?.value
      return if (digits != null) trySafeCode(state, digits) else openSafeModal(state)
    }

    // Handle Lantern Room puzzle steps
    if (state.currentLocation == LocationId.LANTERN_ROOM) {
      if (rest.contains("oil") || rest.contains("canister") || rest.contains("reservoir")) {
        return fillLampOil(state)
      }
      if (rest.contains("crank") || rest.contains("clockwork") || rest.contains("wind")) {
        return windBrassCrank(state)
      }
      if (
        rest.contains("match") ||
          rest.contains("beacon") ||
          rest.contains("lens") ||
          rest.contains("lamp") ||
          rest.contains("mantle")
      ) {
        return igniteBeacon(state)
      }
    }

    // Handle Rescue Pier boat
    if (
      state.currentLocation == LocationId.RESCUE_PIER &&
        (rest.contains("boat") || rest.contains("cutter") || rest.contains("rescue"))
    ) {
      return boardRescueBoat(state)
    }

    return state.appendLog("You can't figure out how to $verb \"$rest\" here.", LogType.WARNING)
  }

  private fun prySeaChest(state: GameState): GameState {
    if (state.flags.chestOpened) {
      return state.appendLog("The sea chest is already open.", LogType.CLUE)
    }
    if (ItemId.IRON_CROWBAR !in state.inventory) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "The iron clasp is rusted solid. You'll need a sturdy pry tool like a crowbar.",
          LogType.WARNING,
        )
    }

    val afterEnergy =
      state
        .copy(
          flags = state.flags.copy(chestOpened = true),
          inventory = state.inventory + ItemId.BRASS_BRIDGE_KEY,
          turn = state.turn + 1,
          lastSoundCue = SoundCue.SOLVE,
        )
        .spendEnergy(1, "exhausted forcing the sea chest")

    if (afterEnergy.phase == GamePhase.GAME_OVER) return afterEnergy

    return afterEnergy.appendLog(
      "With a sharp crack of splintering oak, the Iron Crowbar snaps the rusted clasp! Inside lies a Heavy Brass Key stamped 'GORGE GATE'. (Added Heavy Brass Key to your bag.)",
      LogType.DISCOVERY,
    )
  }

  private fun unlockBridgeGate(state: GameState): GameState {
    if (state.flags.bridgeUnlocked) {
      return state.appendLog("The bridge gate is already unlocked.", LogType.CLUE)
    }
    if (ItemId.BRASS_BRIDGE_KEY !in state.inventory) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "The padlock won't budge without the key stamped 'GORGE GATE'.",
          LogType.WARNING,
        )
    }

    return state
      .copy(
        flags = state.flags.copy(bridgeUnlocked = true),
        inventory = state.inventory - ItemId.BRASS_BRIDGE_KEY,
        turn = state.turn + 1,
        lastSoundCue = SoundCue.SOLVE,
      )
      .appendLog(
        "The Heavy Brass Key turns with a satisfying clunk. The chains fall away and the iron gate swings open—you can now cross East over the Chasm Bridge!",
        LogType.DISCOVERY,
      )
  }

  private fun trySafeCode(state: GameState, rawCode: String): GameState {
    if (state.currentLocation != LocationId.KEEPERS_COTTAGE) {
      return state.appendLog("There is no combination safe here.", LogType.WARNING)
    }
    if (state.flags.safeUnlocked) {
      return state.appendLog("The brass wall safe is already unlocked.", LogType.CLUE)
    }
    val digits = rawCode.replace(Regex("[^0-9]"), "")
    if (digits.length != 3) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("The brass safe requires a 3-digit combination.", LogType.WARNING)
    }
    if (digits != SAFE_COMBINATION) {
      return state
        .copy(turn = state.turn + 1, lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "You dial $digits and tug the handle—nothing happens. Check the Keeper's Logbook for the three storm digits.",
          LogType.WARNING,
        )
    }

    return state
      .copy(
        flags = state.flags.copy(safeUnlocked = true),
        inventory = state.inventory + ItemId.CLOCKWORK_CRANK + ItemId.STORMPROOF_MATCHES,
        turn = state.turn + 1,
        lastSoundCue = SoundCue.SOLVE,
      )
      .appendLog(
        "Click! Dialing 4-8-3 releases the brass tumblers. Inside the safe you find a Clockwork Brass Crank and a brass tube of Stormproof Matches! (Added both to your bag.)",
        LogType.DISCOVERY,
      )
  }

  private fun restOnCot(state: GameState): GameState {
    if (state.currentLocation != LocationId.KEEPERS_COTTAGE) {
      return state.appendLog("There is nowhere dry to rest here.", LogType.WARNING)
    }
    if (state.flags.cotRested) {
      return state.appendLog(
        "You've already caught your breath here; lingering longer won't help.",
        LogType.CLUE,
      )
    }
    val restored = (state.energy + 4).coerceAtMost(state.maxEnergy)
    val gained = restored - state.energy
    return state
      .copy(
        energy = restored,
        flags = state.flags.copy(cotRested = true),
        turn = state.turn + 1,
        lastSoundCue = SoundCue.RESTORE,
      )
      .appendLog(
        "You catch your breath beneath the dry wool blanket for a few minutes (+${gained} Energy, now $restored/${state.maxEnergy}).",
        LogType.DISCOVERY,
      )
  }

  private fun fillLampOil(state: GameState): GameState {
    if (state.currentLocation != LocationId.LANTERN_ROOM) {
      return state.appendLog("The lighthouse burner is up in the Lantern Room.", LogType.WARNING)
    }
    if (state.flags.oilFilled) {
      return state.appendLog("The brass reservoir is already brimming with lamp oil.", LogType.CLUE)
    }
    if (ItemId.LAMP_OIL_CANISTER !in state.inventory) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("You don't have any lamp oil to pour into the reservoir.", LogType.WARNING)
    }
    return state
      .copy(
        flags = state.flags.copy(oilFilled = true),
        inventory = state.inventory - ItemId.LAMP_OIL_CANISTER,
        turn = state.turn + 1,
        lastSoundCue = SoundCue.SOLVE,
      )
      .appendLog(
        "You uncap the canister and pour fresh lamp oil into the brass reservoir until the wick draws clear fuel.",
        LogType.DISCOVERY,
      )
  }

  private fun windBrassCrank(state: GameState): GameState {
    if (state.currentLocation != LocationId.LANTERN_ROOM) {
      return state.appendLog("The Fresnel clockwork drive is up in the Lantern Room.", LogType.WARNING)
    }
    if (state.flags.crankWound) {
      return state.appendLog("The clockwork weight is already wound tight.", LogType.CLUE)
    }
    if (ItemId.CLOCKWORK_CRANK !in state.inventory) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("The drive shaft is missing its Clockwork Brass Crank.", LogType.WARNING)
    }

    val afterEnergy =
      state
        .copy(
          flags = state.flags.copy(crankWound = true),
          inventory = state.inventory - ItemId.CLOCKWORK_CRANK,
          turn = state.turn + 1,
          lastSoundCue = SoundCue.SOLVE,
        )
        .spendEnergy(1, "exhausted winding the heavy Fresnel clockwork")

    if (afterEnergy.phase == GamePhase.GAME_OVER) return afterEnergy

    return afterEnergy.appendLog(
      "You slot the Brass Crank onto the drive shaft and crank hard against the heavy counterweight. Gears mesh smoothly—the Fresnel mechanism is wound and ready!",
      LogType.DISCOVERY,
    )
  }

  private fun igniteBeacon(state: GameState): GameState {
    if (state.currentLocation != LocationId.LANTERN_ROOM) {
      return state.appendLog("You need to climb to the Lantern Room to ignite the beacon.", LogType.WARNING)
    }
    if (state.flags.beaconLit) {
      return state.appendLog(
        "The Fresnel beacon is already blazing across the sea! Head down to the Rescue Pier to board the cutter.",
        LogType.CLUE,
      )
    }
    if (!state.flags.oilFilled) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("The oil reservoir is bone dry. Pour Lamp Oil in before lighting the wick.", LogType.WARNING)
    }
    if (!state.flags.crankWound) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("The lens rotation mechanism isn't wound yet. Fit and wind the Brass Crank first.", LogType.WARNING)
    }
    if (ItemId.STORMPROOF_MATCHES !in state.inventory) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog("You need Stormproof Matches to ignite the burner.", LogType.WARNING)
    }

    val boostedEnergy = (state.energy + 3).coerceAtMost(state.maxEnergy)
    return state
      .copy(
        flags = state.flags.copy(beaconLit = true),
        inventory = state.inventory - ItemId.STORMPROOF_MATCHES,
        energy = boostedEnergy,
        turn = state.turn + 1,
        lastSoundCue = SoundCue.SOLVE,
      )
      .appendLog(
        "You strike a stormproof match and touch it to the primed mantle. A brilliant golden flame blooms inside the crystal Fresnel lens, sweeping a triumphant beam across the black ocean!",
        LogType.DISCOVERY,
      )
      .appendLog(
        "Seconds later, two deep foghorn blasts echo up from the harbor—the rescue boat has spotted your signal and is docking at the Rescue Pier! The warm lantern light steadies your hands (+3 Energy).",
        LogType.DISCOVERY,
      )
  }

  private fun ringBell(state: GameState): GameState {
    if (state.currentLocation != LocationId.RESCUE_PIER) {
      return state.appendLog("There is no harbor bell here.", LogType.WARNING)
    }
    return if (state.flags.beaconLit) {
      state
        .copy(lastSoundCue = SoundCue.MOVE)
        .appendLog(
          "You clang the bronze bell—the cutter's crew flashes a deck spotlight back from the pier end. You're ready to board!",
          LogType.DISCOVERY,
        )
    } else {
      state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "The bronze bell tolls across the dark water, swallowed by the surf. The rescue boat won't enter the reef channel until the lighthouse beacon is lit.",
          LogType.CLUE,
        )
    }
  }

  private fun boardRescueBoat(state: GameState): GameState {
    if (state.currentLocation != LocationId.RESCUE_PIER) {
      return state.appendLog("You can only board the rescue boat from the Rescue Pier.", LogType.WARNING)
    }
    if (!state.flags.beaconLit) {
      return state
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "The rescue cutter is still holding out beyond the reef. Restore the lighthouse beacon first!",
          LogType.WARNING,
        )
    }
    return state
      .copy(
        phase = GamePhase.VICTORY,
        lastSoundCue = SoundCue.VICTORY,
        endingReason =
          "With the golden beacon sweeping steadily over Mystery Island, you step onto the warm deck of the Coast Guard cutter and leave the storm-lashed rocks behind.",
      )
      .appendLog(
        "You step aboard the Coast Guard cutter as the crew pulls in the mooring lines. Behind you, Mystery Island's beacon sweeps steadily across the clearing sea.",
        LogType.DISCOVERY,
      )
  }

  private fun consumeItem(state: GameState, target: String): GameState {
    if (target.isBlank()) {
      return state.appendLog("What ration would you like to consume?", LogType.WARNING)
    }
    val matched =
      state.inventory.firstOrNull { itemId ->
        val item = IslandWorld.items.getValue(itemId)
        item.isConsumable &&
          (item.name.lowercase().contains(target) || item.shortName.lowercase().contains(target))
      }

    if (matched == null) {
      return state.appendLog("You don't have \"$target\" to eat or drink.", LogType.WARNING)
    }

    val item = IslandWorld.items.getValue(matched)
    val nextEnergy = (state.energy + item.energyRestore).coerceAtMost(state.maxEnergy)
    val gained = nextEnergy - state.energy

    return state
      .copy(
        energy = nextEnergy,
        inventory = state.inventory - matched,
        turn = state.turn + 1,
        lastSoundCue = SoundCue.RESTORE,
      )
      .appendLog(
        "You consume the ${item.name}, restoring +$gained Energy (now $nextEnergy/${state.maxEnergy}).",
        LogType.DISCOVERY,
      )
  }

  private fun describeBeaconStatus(state: GameState): GameState {
    val oilStatus = if (state.flags.oilFilled) "Oil reservoir full" else "Oil reservoir empty"
    val crankStatus = if (state.flags.crankWound) "Clockwork wound" else "Brass crank missing"
    val flameStatus = if (state.flags.beaconLit) "Beacon blazing" else "Mantle unlit"
    return state.appendLog(
      "Fresnel Lens status: $oilStatus · $crankStatus · $flameStatus.",
      LogType.CLUE,
    )
  }

  private fun GameState.describeRoomExtras(locationId: LocationId): GameState {
    val loc = IslandWorld.locations.getValue(locationId)
    val itemsHere = roomItems[locationId].orEmpty()
    var updated = this
    if (itemsHere.isNotEmpty()) {
      val itemList = itemsHere.joinToString(", ") { IslandWorld.items.getValue(it).name }
      updated = updated.appendLog("You notice here: $itemList.", LogType.DISCOVERY)
    }
    val exitsSummary =
      loc.exits.entries.joinToString(" · ") { (dir, dest) ->
        val destName = IslandWorld.locations.getValue(dest).name
        val lockNote =
          if (loc.id == LocationId.CHASM_BRIDGE && dir == Direction.EAST && !flags.bridgeUnlocked) {
            " (Locked)"
          } else {
            ""
          }
        "${dir.label}: $destName$lockNote"
      }
    return updated.appendLog("Exits — $exitsSummary", LogType.CLUE)
  }

  private fun GameState.spendEnergy(amount: Int, reason: String): GameState {
    val nextEnergy = (energy - amount).coerceAtLeast(0)
    if (nextEnergy <= 0) {
      return copy(
          energy = 0,
          phase = GamePhase.GAME_OVER,
          lastSoundCue = SoundCue.DEFEAT,
          endingReason =
            "Your last reserve of strength gives out ($reason). Cold rain settles over Mystery Island as the lighthouse remains dark.",
        )
        .appendLog(
          "Your knees buckle and the dark horizon spins. You have run out of energy before reaching rescue.",
          LogType.WARNING,
        )
    }
    val withEnergy = copy(energy = nextEnergy)
    return if (nextEnergy <= 4) {
      withEnergy
        .copy(lastSoundCue = SoundCue.WARNING)
        .appendLog(
          "Low energy warning ($nextEnergy/$maxEnergy left). Eat rations or rest soon.",
          LogType.WARNING,
        )
    } else {
      withEnergy
    }
  }

  private fun GameState.appendLog(text: String, type: LogType): GameState {
    val entry = LogEntry(id = nextLogId, text = text, type = type)
    return copy(log = log + entry, nextLogId = nextLogId + 1L)
  }

  private fun parseDirection(input: String): Direction? =
    when (input.trim()) {
      "n", "north" -> Direction.NORTH
      "s", "south" -> Direction.SOUTH
      "e", "east" -> Direction.EAST
      "w", "west" -> Direction.WEST
      "u", "up", "stairs", "staircase" -> Direction.UP
      "d", "down" -> Direction.DOWN
      else -> null
    }
}
