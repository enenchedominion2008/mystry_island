package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GamePhase
import com.example.ui.AdventureViewModel
import com.example.ui.screens.AdventureScreen
import com.example.ui.screens.EndingScreen
import com.example.ui.screens.TitleScreen
import com.example.ui.theme.MysteryIslandTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MysteryIslandTheme {
        val viewModel: AdventureViewModel = viewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        when (state.phase) {
          GamePhase.TITLE -> TitleScreen(onStartGame = viewModel::startGame)
          GamePhase.PLAYING ->
            AdventureScreen(
              state = state,
              onSubmitCommand = viewModel::submitCommand,
              onOpenSafeDialog = viewModel::openSafeDialog,
              onDismissSafeDialog = viewModel::dismissSafeDialog,
              onSubmitSafeCode = viewModel::submitSafeCode,
              onToggleSound = viewModel::toggleSound,
              onRestart = viewModel::restartGame,
            )
          GamePhase.VICTORY,
          GamePhase.GAME_OVER ->
            EndingScreen(
              state = state,
              onPlayAgain = viewModel::restartGame,
            )
        }
      }
    }
  }
}
