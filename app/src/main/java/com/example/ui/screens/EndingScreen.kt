package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.IslandWorld
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.ui.theme.BeaconAmber
import com.example.ui.theme.DriftwoodText
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HarborPanel
import com.example.ui.theme.OnBeaconAmber
import com.example.ui.theme.Parchment
import com.example.ui.theme.SeafoamSage
import com.example.ui.theme.StormAbyss
import com.example.ui.theme.StormRust

@Composable
fun EndingScreen(
  state: GameState,
  onPlayAgain: () -> Unit,
) {
  val isVictory = state.phase == GamePhase.VICTORY
  val accentColor = if (isVictory) SeafoamSage else StormRust
  val backdropRes =
    if (isVictory) {
      R.drawable.loc_lighthouse_beacon_1795078608978
    } else {
      R.drawable.loc_storm_shore_1795078608054
    }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(StormAbyss)
  ) {
    Image(
      painter = painterResource(id = backdropRes),
      contentDescription = if (isVictory) "Lighthouse beacon restored" else "Storm-swept shore",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors =
                listOf(
                  StormAbyss.copy(alpha = 0.45f),
                  StormAbyss.copy(alpha = 0.85f),
                  StormAbyss.copy(alpha = 0.98f),
                )
            )
          )
    )

    BoxWithConstraints(
      modifier =
        Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(min = maxHeight)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Bottom,
      ) {
      Text(
        text = if (isVictory) "Rescued from Mystery Island" else "Out of Energy",
        style = MaterialTheme.typography.labelMedium,
        color = accentColor,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (isVictory) "The Beacon Shines Again" else "Collapsed Before Dawn",
        style = MaterialTheme.typography.displaySmall,
        color = Parchment,
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = state.endingReason,
        style = MaterialTheme.typography.bodyLarge,
        color = Parchment.copy(alpha = 0.9f),
      )

      Spacer(modifier = Modifier.height(20.dp))

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(HarborPanel.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
            .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        StatLine(label = "Turns Taken", value = "${state.turn}")
        HorizontalDivider(color = HairlineBorder)
        StatLine(label = "Remaining Energy", value = "${state.energy} / ${state.maxEnergy}")
        HorizontalDivider(color = HairlineBorder)
        StatLine(
          label = "Locations Charted",
          value = "${state.visitedLocations.size} / ${IslandWorld.locations.size}",
        )
        HorizontalDivider(color = HairlineBorder)
        StatLine(label = "Puzzles Solved", value = "${state.flags.solvedCount} / 4")
      }

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onPlayAgain,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = BeaconAmber,
            contentColor = OnBeaconAmber,
          ),
      ) {
        Text(
          text = "Play Again",
          style = MaterialTheme.typography.titleMedium,
        )
      }
      }
    }
  }
}

@Composable
private fun StatLine(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = DriftwoodText,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelMedium,
      color = Parchment,
    )
  }
}
