package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.audio.SoundEffects
import com.example.engine.GameEngine
import com.example.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdventureViewModel : ViewModel() {

  private val soundEffects = SoundEffects()

  private val _state = MutableStateFlow(GameEngine.newTitleState())
  val state: StateFlow<GameState> = _state.asStateFlow()

  fun startGame() {
    _state.update { current ->
      GameEngine.startNewGame(soundEnabled = current.soundEnabled)
    }
  }

  fun restartGame() {
    _state.update { current ->
      GameEngine.startNewGame(soundEnabled = current.soundEnabled)
    }
  }

  fun submitCommand(command: String) {
    _state.update { current ->
      val next = GameEngine.processCommand(current, command)
      next.lastSoundCue?.let { cue -> soundEffects.play(cue, next.soundEnabled) }
      next
    }
  }

  fun openSafeDialog() {
    _state.update { GameEngine.openSafeModal(it) }
  }

  fun dismissSafeDialog() {
    _state.update { GameEngine.dismissSafeModal(it) }
  }

  fun submitSafeCode(code: String) {
    _state.update { current ->
      val next = GameEngine.submitSafeCombination(current, code)
      next.lastSoundCue?.let { cue -> soundEffects.play(cue, next.soundEnabled) }
      next
    }
  }

  fun toggleSound() {
    _state.update { GameEngine.toggleSound(it) }
  }
}
