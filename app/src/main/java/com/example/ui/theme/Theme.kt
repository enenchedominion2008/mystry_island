package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MysteryIslandColorScheme =
  darkColorScheme(
    primary = BeaconAmber,
    onPrimary = OnBeaconAmber,
    primaryContainer = BeaconAmberDim,
    onPrimaryContainer = BeaconAmberBright,
    secondary = SeafoamSage,
    onSecondary = OnBeaconAmber,
    secondaryContainer = SeafoamSageDim,
    onSecondaryContainer = SeafoamSage,
    tertiary = TideBlue,
    onTertiary = OnBeaconAmber,
    background = StormAbyss,
    onBackground = Parchment,
    surface = HarborPanel,
    onSurface = Parchment,
    surfaceVariant = HarborPanelElevated,
    onSurfaceVariant = DriftwoodText,
    outline = HairlineBorder,
    outlineVariant = HairlineBorderStrong,
    error = StormRust,
    onError = OnBeaconAmber,
    errorContainer = StormRustDim,
    onErrorContainer = StormRust,
  )

@Composable
fun MysteryIslandTheme(content: @Composable () -> Unit) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = MysteryIslandColorScheme,
    typography = MysteryIslandTypography,
    content = content,
  )
}
