package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.ui.theme.BeaconAmber
import com.example.ui.theme.DriftwoodText
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HarborPanel
import com.example.ui.theme.OnBeaconAmber
import com.example.ui.theme.Parchment
import com.example.ui.theme.StormAbyss

@Composable
fun TitleScreen(onStartGame: () -> Unit) {
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(StormAbyss)
  ) {
    Image(
      painter = painterResource(id = R.drawable.title_mystery_island_1795078607282),
      contentDescription = "Mystery Island lighthouse overlooking stormy waves",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    // Measured gradient scrim for WCAG AA contrast across lower editorial card
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors =
                listOf(
                  StormAbyss.copy(alpha = 0.25f),
                  StormAbyss.copy(alpha = 0.65f),
                  StormAbyss.copy(alpha = 0.96f),
                )
            )
          )
    )

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 28.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "Maritime Text Adventure",
        style = MaterialTheme.typography.labelMedium,
        color = BeaconAmber,
      )

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
          text = "Mystery Island",
          style = MaterialTheme.typography.displaySmall,
          color = Parchment,
        )

        Text(
          text =
            "You wake on cold basalt pebbles after a violent squall. The lighthouse stands dark, the gorge bridge is locked, and a rescue boat will only enter the reef once you restore the beacon.",
          style = MaterialTheme.typography.bodyLarge,
          color = Parchment.copy(alpha = 0.92f),
        )

        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .background(HarborPanel.copy(alpha = 0.88f), RoundedCornerShape(14.dp))
              .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
              .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          ObjectiveRow(index = "01", text = "Explore coastal coves, boathouse, and keeper's quarters.")
          HorizontalDivider(color = HairlineBorder)
          ObjectiveRow(index = "02", text = "Collect tools and rations; unlock the bridge and safe.")
          HorizontalDivider(color = HairlineBorder)
          ObjectiveRow(index = "03", text = "Restore the Fresnel beacon before your energy hits zero.")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
          onClick = onStartGame,
          modifier = Modifier.fillMaxWidth().height(54.dp),
          shape = RoundedCornerShape(12.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = BeaconAmber,
              contentColor = OnBeaconAmber,
            ),
        ) {
          Text(
            text = "Start Game",
            style = MaterialTheme.typography.titleMedium,
          )
        }
      }
    }
  }
}

@Composable
private fun ObjectiveRow(index: String, text: String) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = index,
      style = MaterialTheme.typography.labelMedium,
      color = BeaconAmber,
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = DriftwoodText,
    )
  }
}
