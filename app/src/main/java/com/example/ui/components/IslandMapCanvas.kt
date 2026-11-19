package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.IslandWorld
import com.example.model.Direction
import com.example.model.GameState
import com.example.model.LocationId
import com.example.ui.theme.BeaconAmber
import com.example.ui.theme.DriftwoodText
import com.example.ui.theme.HairlineBorderStrong
import com.example.ui.theme.HarborPanel
import com.example.ui.theme.HarborPanelElevated
import com.example.ui.theme.MonoFont
import com.example.ui.theme.MutedSlateText
import com.example.ui.theme.Parchment
import com.example.ui.theme.SeaSlate
import com.example.ui.theme.SeafoamSage
import com.example.ui.theme.StormRust

@Composable
fun IslandMapDialog(
  state: GameState,
  onDismiss: () -> Unit,
) {
  val textMeasurer = rememberTextMeasurer()

  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(HarborPanel, RoundedCornerShape(16.dp))
          .border(1.dp, HairlineBorderStrong, RoundedCornerShape(16.dp))
          .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Mystery Island Chart",
            style = MaterialTheme.typography.titleLarge,
            color = Parchment,
          )
          Text(
            text = "${state.visitedLocations.size} / ${IslandWorld.locations.size} locations charted",
            style = MaterialTheme.typography.labelMedium,
            color = DriftwoodText,
          )
        }
        TextButton(onClick = onDismiss) {
          Text(text = "Close", color = BeaconAmber)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Canvas(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(SeaSlate, RoundedCornerShape(12.dp))
            .border(1.dp, HairlineBorderStrong, RoundedCornerShape(12.dp))
            .padding(18.dp)
      ) {
        val cols = 4
        val rows = 3
        val cellW = size.width / (cols - 1).coerceAtLeast(1)
        val cellH = size.height / (rows - 1).coerceAtLeast(1)

        fun nodeCenter(col: Int, row: Int): Offset =
          Offset(x = col * cellW, y = row * cellH)

        // Draw edges first
        IslandWorld.locations.values.forEach { loc ->
          val from = nodeCenter(loc.mapCol, loc.mapRow)
          val fromVisited = loc.id in state.visitedLocations
          loc.exits.forEach { (dir, destId) ->
            // Draw each undirected edge once
            if (dir == Direction.EAST || dir == Direction.SOUTH || dir == Direction.UP) {
              val dest = IslandWorld.locations.getValue(destId)
              val to = nodeCenter(dest.mapCol, dest.mapRow)
              val edgeKnown = fromVisited || destId in state.visitedLocations
              val isLockedBridge =
                loc.id == LocationId.CHASM_BRIDGE &&
                  dir == Direction.EAST &&
                  !state.flags.bridgeUnlocked

              drawLine(
                color =
                  when {
                    isLockedBridge -> StormRust
                    edgeKnown -> DriftwoodText.copy(alpha = 0.55f)
                    else -> MutedSlateText.copy(alpha = 0.2f)
                  },
                start = from,
                end = to,
                strokeWidth = if (isLockedBridge) 3.dp.toPx() else 2.dp.toPx(),
                pathEffect =
                  if (isLockedBridge) {
                    PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                  } else {
                    null
                  },
              )
            }
          }
        }

        // Draw location nodes
        IslandWorld.locations.values.forEach { loc ->
          val center = nodeCenter(loc.mapCol, loc.mapRow)
          val isCurrent = loc.id == state.currentLocation
          val isVisited = loc.id in state.visitedLocations
          val isBeaconLitNode = loc.id == LocationId.LANTERN_ROOM && state.flags.beaconLit

          if (isBeaconLitNode) {
            drawCircle(
              color = BeaconAmber.copy(alpha = 0.25f),
              radius = 22.dp.toPx(),
              center = center,
            )
          }

          val fillColor =
            when {
              isCurrent -> BeaconAmber
              isBeaconLitNode -> SeafoamSage
              isVisited -> HarborPanelElevated
              else -> SeaSlate
            }
          val strokeColor =
            when {
              isCurrent -> Parchment
              isVisited -> DriftwoodText
              else -> MutedSlateText.copy(alpha = 0.45f)
            }

          drawCircle(
            color = fillColor,
            radius = if (isCurrent) 13.dp.toPx() else 10.dp.toPx(),
            center = center,
          )
          drawCircle(
            color = strokeColor,
            radius = if (isCurrent) 13.dp.toPx() else 10.dp.toPx(),
            center = center,
            style = Stroke(width = 1.6.dp.toPx()),
          )

          if (isVisited || isCurrent) {
            drawShortLabel(
              textMeasurer = textMeasurer,
              label = shortMapName(loc.id),
              center = center,
              isCurrent = isCurrent,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Amber: You · Light Ring: Visited · Dashed Red: Locked Bridge",
          style = MaterialTheme.typography.labelSmall,
          color = DriftwoodText,
        )
      }
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShortLabel(
  textMeasurer: TextMeasurer,
  label: String,
  center: Offset,
  isCurrent: Boolean,
) {
  val measured =
    textMeasurer.measure(
      text = label,
      style =
        TextStyle(
          fontFamily = MonoFont,
          fontSize = 9.sp,
          fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
          color = if (isCurrent) BeaconAmber else Parchment,
        ),
    )
  val x = (center.x - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width)
  val y = (center.y + 15.dp.toPx()).coerceAtMost(size.height - measured.size.height)
  drawText(
    textLayoutResult = measured,
    topLeft = Offset(x, y),
  )
}

private fun shortMapName(id: LocationId): String =
  when (id) {
    LocationId.STORM_SHORE -> "Shore"
    LocationId.SHIPWRECK_COVE -> "Cove"
    LocationId.MISTY_PINE_TRAIL -> "Trail"
    LocationId.OLD_BOATHOUSE -> "Boathouse"
    LocationId.CHASM_BRIDGE -> "Bridge"
    LocationId.KEEPERS_COURTYARD -> "Courtyard"
    LocationId.KEEPERS_COTTAGE -> "Cottage"
    LocationId.RESCUE_PIER -> "Pier"
    LocationId.LIGHTHOUSE_BASE -> "Tower"
    LocationId.LANTERN_ROOM -> "Lantern"
  }
