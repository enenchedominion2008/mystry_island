package com.example

import com.example.engine.GameEngine
import com.example.model.GamePhase
import com.example.model.ItemId
import com.example.model.LocationId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

  @Test
  fun fullWalkthrough_restoresBeaconAndEscapesOnRescueBoat() {
    var state = GameEngine.startNewGame(soundEnabled = false)
    assertEquals(GamePhase.PLAYING, state.phase)
    assertEquals(LocationId.STORM_SHORE, state.currentLocation)
    assertEquals(20, state.energy)

    // 1. Go East to Shipwreck Cove, collect Iron Crowbar & Hardtack
    state = GameEngine.processCommand(state, "go east")
    assertEquals(LocationId.SHIPWRECK_COVE, state.currentLocation)
    state = GameEngine.processCommand(state, "take crowbar")
    state = GameEngine.processCommand(state, "take hardtack")
    assertTrue(ItemId.IRON_CROWBAR in state.inventory)
    assertTrue(ItemId.TIN_OF_HARDTACK in state.inventory)

    // 2. Return West to Storm Shore, pry Sea Chest -> gain Brass Bridge Key
    state = GameEngine.processCommand(state, "west")
    state = GameEngine.processCommand(state, "use crowbar on chest")
    assertTrue(state.flags.chestOpened)
    assertTrue(ItemId.BRASS_BRIDGE_KEY in state.inventory)

    // 3. Head North to Trail -> North to Boathouse -> collect Lamp Oil
    state = GameEngine.processCommand(state, "north")
    assertEquals(LocationId.MISTY_PINE_TRAIL, state.currentLocation)
    state = GameEngine.processCommand(state, "north")
    assertEquals(LocationId.OLD_BOATHOUSE, state.currentLocation)
    state = GameEngine.processCommand(state, "take oil")
    assertTrue(ItemId.LAMP_OIL_CANISTER in state.inventory)

    // 4. Go South to Trail -> East to Chasm Bridge -> blocked until unlocked
    state = GameEngine.processCommand(state, "south")
    state = GameEngine.processCommand(state, "east")
    assertEquals(LocationId.CHASM_BRIDGE, state.currentLocation)

    val blockedCross = GameEngine.processCommand(state, "east")
    assertEquals(LocationId.CHASM_BRIDGE, blockedCross.currentLocation)
    assertFalse(blockedCross.flags.bridgeUnlocked)

    state = GameEngine.processCommand(blockedCross, "use key on gate")
    assertTrue(state.flags.bridgeUnlocked)

    // 5. Cross East to Courtyard -> North to Cottage -> eat hardtack & solve safe 483
    state = GameEngine.processCommand(state, "east")
    assertEquals(LocationId.KEEPERS_COURTYARD, state.currentLocation)
    state = GameEngine.processCommand(state, "north")
    assertEquals(LocationId.KEEPERS_COTTAGE, state.currentLocation)
    state = GameEngine.processCommand(state, "eat hardtack")

    val wrongSafe = GameEngine.processCommand(state, "enter 123")
    assertFalse(wrongSafe.flags.safeUnlocked)

    state = GameEngine.processCommand(wrongSafe, "enter 483")
    assertTrue(state.flags.safeUnlocked)
    assertTrue(ItemId.CLOCKWORK_CRANK in state.inventory)
    assertTrue(ItemId.STORMPROOF_MATCHES in state.inventory)

    // 6. South to Courtyard -> East to Lighthouse Base -> Up to Lantern Room
    state = GameEngine.processCommand(state, "south")
    state = GameEngine.processCommand(state, "east")
    assertEquals(LocationId.LIGHTHOUSE_BASE, state.currentLocation)
    state = GameEngine.processCommand(state, "up")
    assertEquals(LocationId.LANTERN_ROOM, state.currentLocation)

    // 7. Restore Beacon in 3 steps: Oil -> Crank -> Matches
    state = GameEngine.processCommand(state, "use oil on beacon")
    assertTrue(state.flags.oilFilled)
    state = GameEngine.processCommand(state, "use crank on beacon")
    assertTrue(state.flags.crankWound)
    state = GameEngine.processCommand(state, "light beacon")
    assertTrue(state.flags.beaconLit)

    // 8. Descend to Courtyard -> South to Rescue Pier -> Board Rescue Boat!
    state = GameEngine.processCommand(state, "down")
    state = GameEngine.processCommand(state, "west")
    state = GameEngine.processCommand(state, "south")
    assertEquals(LocationId.RESCUE_PIER, state.currentLocation)
    state = GameEngine.processCommand(state, "board boat")

    assertEquals(GamePhase.VICTORY, state.phase)
    assertTrue(state.energy > 0)
  }

  @Test
  fun boardingBeforeBeaconLit_isBlocked() {
    var state = GameEngine.startNewGame(soundEnabled = false)
    state = GameEngine.processCommand(state, "go east")
    state = GameEngine.processCommand(state, "take crowbar")
    state = GameEngine.processCommand(state, "west")
    state = GameEngine.processCommand(state, "use crowbar on chest")
    state = GameEngine.processCommand(state, "north")
    state = GameEngine.processCommand(state, "east")
    state = GameEngine.processCommand(state, "use key on gate")
    state = GameEngine.processCommand(state, "east")
    state = GameEngine.processCommand(state, "south")
    assertEquals(LocationId.RESCUE_PIER, state.currentLocation)

    state = GameEngine.processCommand(state, "board boat")
    assertEquals(GamePhase.PLAYING, state.phase)
    assertFalse(state.flags.beaconLit)
  }

  @Test
  fun exhaustingEnergy_triggersGameOver() {
    var state = GameEngine.startNewGame(soundEnabled = false)
    repeat(20) {
      if (state.phase == GamePhase.PLAYING) {
        val nextCmd = if (state.currentLocation == LocationId.STORM_SHORE) "east" else "west"
        state = GameEngine.processCommand(state, nextCmd)
      }
    }
    assertEquals(0, state.energy)
    assertEquals(GamePhase.GAME_OVER, state.phase)
  }
}
