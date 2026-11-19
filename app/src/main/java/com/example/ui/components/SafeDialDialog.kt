package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BeaconAmber
import com.example.ui.theme.DriftwoodText
import com.example.ui.theme.HairlineBorderStrong
import com.example.ui.theme.HarborPanel
import com.example.ui.theme.HarborPanelElevated
import com.example.ui.theme.OnBeaconAmber
import com.example.ui.theme.Parchment
import com.example.ui.theme.SeaSlate

@Composable
fun SafeDialDialog(
  onSubmit: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var d1 by remember { mutableIntStateOf(0) }
  var d2 by remember { mutableIntStateOf(0) }
  var d3 by remember { mutableIntStateOf(0) }

  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(HarborPanel, RoundedCornerShape(16.dp))
          .border(1.dp, HairlineBorderStrong, RoundedCornerShape(16.dp))
          .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "Brass Wall Safe",
        style = MaterialTheme.typography.titleLarge,
        color = Parchment,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Dial the 3-digit Storm Protocol code from the Keeper's Logbook.",
        style = MaterialTheme.typography.bodyMedium,
        color = DriftwoodText,
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        DialDigitColumn(value = d1, onChange = { d1 = it })
        DialDigitColumn(value = d2, onChange = { d2 = it })
        DialDigitColumn(value = d3, onChange = { d3 = it })
      }

      Spacer(modifier = Modifier.height(22.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(48.dp),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text(text = "Cancel", color = Parchment)
        }
        Button(
          onClick = { onSubmit("$d1$d2$d3") },
          modifier = Modifier.weight(1f).height(48.dp),
          shape = RoundedCornerShape(10.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = BeaconAmber,
              contentColor = OnBeaconAmber,
            ),
        ) {
          Text(text = "Unlock")
        }
      }
    }
  }
}

@Composable
private fun DialDigitColumn(
  value: Int,
  onChange: (Int) -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier
        .background(SeaSlate, RoundedCornerShape(12.dp))
        .border(1.dp, HairlineBorderStrong, RoundedCornerShape(12.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp),
  ) {
    IconButton(
      onClick = { onChange((value + 1) % 10) },
      modifier = Modifier.size(48.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.KeyboardArrowUp,
        contentDescription = "Increase digit",
        tint = BeaconAmber,
      )
    }
    Text(
      text = value.toString(),
      style = MaterialTheme.typography.displaySmall,
      color = Parchment,
      modifier =
        Modifier
          .background(HarborPanelElevated, RoundedCornerShape(8.dp))
          .padding(horizontal = 16.dp, vertical = 8.dp),
    )
    IconButton(
      onClick = { onChange((value + 9) % 10) },
      modifier = Modifier.size(48.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.KeyboardArrowDown,
        contentDescription = "Decrease digit",
        tint = BeaconAmber,
      )
    }
  }
}
