package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.IslandWorld
import com.example.engine.GameEngine
import com.example.model.ContextAction
import com.example.model.Direction
import com.example.model.GameState
import com.example.model.LocationId
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.ui.components.IslandMapDialog
import com.example.ui.components.SafeDialDialog
import com.example.ui.theme.BeaconAmber
import com.example.ui.theme.BeaconAmberBright
import com.example.ui.theme.DriftwoodText
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineBorderStrong
import com.example.ui.theme.HarborPanel
import com.example.ui.theme.HarborPanelElevated
import com.example.ui.theme.OnBeaconAmber
import com.example.ui.theme.Parchment
import com.example.ui.theme.SeaSlate
import com.example.ui.theme.SeafoamSage
import com.example.ui.theme.SeafoamSageDim
import com.example.ui.theme.StormAbyss
import com.example.ui.theme.StormRust
import com.example.ui.theme.StormRustDim

private enum class ConsoleTab(val label: String) {
  ACTIONS("Actions"),
  MOVE("Move"),
  BAG("Bag"),
}

@Composable
fun AdventureScreen(
  state: GameState,
  onSubmitCommand: (String) -> Unit,
  onOpenSafeDialog: () -> Unit,
  onDismissSafeDialog: () -> Unit,
  onSubmitSafeCode: (String) -> Unit,
  onToggleSound: () -> Unit,
  onRestart: () -> Unit,
) {
  var commandInput by remember { mutableStateOf("") }
  var selectedTab by remember { mutableStateOf(ConsoleTab.ACTIONS) }
  var showMap by remember { mutableStateOf(false) }
  var showRestartConfirm by remember { mutableStateOf(false) }
  var bannerExpanded by remember { mutableStateOf(true) }

  val listState = rememberLazyListState()
  val location = IslandWorld.locations.getValue(state.currentLocation)
  val contextActions = remember(state) { GameEngine.availableContextActions(state) }

  LaunchedEffect(state.log.size) {
    if (state.log.isNotEmpty()) {
      listState.animateScrollToItem(state.log.lastIndex)
    }
  }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(StormAbyss)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .imePadding()
  ) {
    // 1. Clean 3-Zone Top Bar + Energy Strip
    TopStatusHeader(
      state = state,
      onOpenMap = { showMap = true },
      onToggleSound = onToggleSound,
      onRequestRestart = {
        if (state.turn > 1) showRestartConfirm = true else onRestart()
      },
    )

    // 2. Collapsible Location Artwork Banner
    LocationHeaderBanner(
      locationName = location.name,
      subtitle = location.subtitle,
      artworkRes = location.artworkRes,
      expanded = bannerExpanded,
      onToggleExpand = { bannerExpanded = !bannerExpanded },
    )

    // 3. Narrative Log
    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f).fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(state.log, key = { it.id }) { entry ->
        LogEntryRow(entry = entry)
      }
    }

    // 4. Interactive Action Deck + Text Command Bar
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(SeaSlate)
          .border(1.dp, HairlineBorder)
          .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      ConsoleTabBar(
        selected = selectedTab,
        bagCount = state.inventory.size,
        onSelect = { selectedTab = it },
      )

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(max = 172.dp)
            .verticalScroll(rememberScrollState())
      ) {
        when (selectedTab) {
          ConsoleTab.ACTIONS ->
            ActionsTabContent(
              actions = contextActions,
              onActionClick = { action ->
                if (action.command == "open_safe_ui") {
                  onOpenSafeDialog()
                } else {
                  onSubmitCommand(action.command)
                }
              },
              onLook = { onSubmitCommand("look") },
            )
          ConsoleTab.MOVE ->
            MoveTabContent(
              state = state,
              onMove = { dir -> onSubmitCommand("go ${dir.label.lowercase()}") },
            )
          ConsoleTab.BAG ->
            BagTabContent(
              state = state,
              onUseItem = { cmd -> onSubmitCommand(cmd) },
            )
        }
      }

      CommandInputBar(
        value = commandInput,
        onValueChange = { commandInput = it },
        onSubmit = {
          if (commandInput.isNotBlank()) {
            onSubmitCommand(commandInput)
            commandInput = ""
          }
        },
      )
    }
  }

  if (showMap) {
    IslandMapDialog(
      state = state,
      onDismiss = { showMap = false },
    )
  }

  if (state.showSafeDialog) {
    SafeDialDialog(
      onSubmit = onSubmitSafeCode,
      onDismiss = onDismissSafeDialog,
    )
  }

  if (showRestartConfirm) {
    AlertDialog(
      onDismissRequest = { showRestartConfirm = false },
      containerColor = HarborPanel,
      titleContentColor = Parchment,
      textContentColor = DriftwoodText,
      title = { Text(text = "Restart Adventure?") },
      text = {
        Text(text = "Your current progress and collected items on Mystery Island will be reset.")
      },
      confirmButton = {
        Button(
          onClick = {
            showRestartConfirm = false
            onRestart()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BeaconAmber, contentColor = OnBeaconAmber),
        ) {
          Text(text = "Restart")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRestartConfirm = false }) {
          Text(text = "Cancel", color = Parchment)
        }
      },
    )
  }
}

@Composable
private fun TopStatusHeader(
  state: GameState,
  onOpenMap: () -> Unit,
  onToggleSound: () -> Unit,
  onRequestRestart: () -> Unit,
) {
  val animatedEnergyRatio by
    animateFloatAsState(
      targetValue = (state.energy.toFloat() / state.maxEnergy.toFloat()).coerceIn(0f, 1f),
      animationSpec = tween(durationMillis = 200),
      label = "energyRatio",
    )
  val energyColor =
    when {
      state.isLowEnergy -> StormRust
      state.energy <= 10 -> BeaconAmber
      else -> SeafoamSage
    }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(HarborPanel)
        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "Mystery Island",
        style = MaterialTheme.typography.titleLarge,
        color = Parchment,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onOpenMap, modifier = Modifier.size(44.dp)) {
          Icon(
            imageVector = Icons.Rounded.Map,
            contentDescription = "Open island map",
            tint = Parchment,
          )
        }
        IconButton(onClick = onToggleSound, modifier = Modifier.size(44.dp)) {
          Icon(
            imageVector =
              if (state.soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
            contentDescription = if (state.soundEnabled) "Mute audio" else "Enable audio",
            tint = if (state.soundEnabled) DriftwoodText else StormRust,
          )
        }
        IconButton(onClick = onRequestRestart, modifier = Modifier.size(44.dp)) {
          Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Restart game",
            tint = DriftwoodText,
          )
        }
      }
    }

    // Unboxed telemetry metadata with middot separators
    Row(
      modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text =
          buildString {
            append("Energy ${state.energy} / ${state.maxEnergy}")
            if (state.isLowEnergy) append(" · LOW ENERGY")
          },
        style = MaterialTheme.typography.labelMedium,
        color = energyColor,
      )

      Text(
        text =
          "Turn ${state.turn} · Beacon ${if (state.flags.beaconLit) "Lit" else "Dark"}",
        style = MaterialTheme.typography.labelMedium,
        color = if (state.flags.beaconLit) BeaconAmberBright else DriftwoodText,
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    LinearProgressIndicator(
      progress = { animatedEnergyRatio },
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(end = 8.dp)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
      color = energyColor,
      trackColor = HarborPanelElevated,
    )
  }
}

@Composable
private fun LocationHeaderBanner(
  locationName: String,
  subtitle: String,
  artworkRes: Int?,
  expanded: Boolean,
  onToggleExpand: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    AnimatedVisibility(visible = expanded && artworkRes != null) {
      if (artworkRes != null) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(124.dp)
              .background(SeaSlate)
        ) {
          Image(
            painter = painterResource(id = artworkRes),
            contentDescription = locationName,
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
                        StormAbyss.copy(alpha = 0.1f),
                        StormAbyss.copy(alpha = 0.85f),
                      )
                  )
                )
          )
          Row(
            modifier =
              Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = locationName,
                style = MaterialTheme.typography.titleLarge,
                color = Parchment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = BeaconAmber,
              )
            }
            IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
              Icon(
                imageVector = Icons.Rounded.ExpandLess,
                contentDescription = "Collapse scene artwork",
                tint = Parchment,
              )
            }
          }
        }
      }
    }

    if (!expanded || artworkRes == null) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(SeaSlate)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = locationName,
            style = MaterialTheme.typography.titleMedium,
            color = Parchment,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "· $subtitle",
            style = MaterialTheme.typography.labelMedium,
            color = DriftwoodText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        if (artworkRes != null) {
          IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
            Icon(
              imageVector = Icons.Rounded.ExpandMore,
              contentDescription = "Show scene artwork",
              tint = DriftwoodText,
            )
          }
        }
      }
    }

    HorizontalDivider(color = HairlineBorder)
  }
}

@Composable
private fun LogEntryRow(entry: LogEntry) {
  when (entry.type) {
    LogType.COMMAND -> {
      Text(
        text = entry.text,
        style = MaterialTheme.typography.labelMedium,
        color = BeaconAmber,
        modifier = Modifier.padding(top = 4.dp),
      )
    }
    LogType.DISCOVERY -> {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(SeafoamSageDim, RoundedCornerShape(10.dp))
            .border(1.dp, SeafoamSage.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        Text(
          text = entry.text,
          style = MaterialTheme.typography.bodyLarge,
          color = Parchment,
        )
      }
    }
    LogType.WARNING -> {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(StormRustDim, RoundedCornerShape(10.dp))
            .border(1.dp, StormRust.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        Text(
          text = entry.text,
          style = MaterialTheme.typography.bodyLarge,
          color = Parchment,
        )
      }
    }
    LogType.CLUE -> {
      Text(
        text = entry.text,
        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
        color = DriftwoodText,
      )
    }
    LogType.NARRATIVE -> {
      Text(
        text = entry.text,
        style = MaterialTheme.typography.bodyLarge,
        color = Parchment,
      )
    }
  }
}

@Composable
private fun ConsoleTabBar(
  selected: ConsoleTab,
  bagCount: Int,
  onSelect: (ConsoleTab) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(StormAbyss, RoundedCornerShape(10.dp))
        .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
        .padding(3.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    ConsoleTab.entries.forEach { tab ->
      val isSelected = tab == selected
      val label = if (tab == ConsoleTab.BAG) "Bag ($bagCount)" else tab.label
      Button(
        onClick = { onSelect(tab) },
        modifier = Modifier.weight(1f).height(40.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = if (isSelected) HarborPanelElevated else StormAbyss,
            contentColor = if (isSelected) BeaconAmberBright else DriftwoodText,
          ),
        elevation = null,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionsTabContent(
  actions: List<ContextAction>,
  onActionClick: (ContextAction) -> Unit,
  onLook: () -> Unit,
) {
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    actions.forEach { action ->
      if (action.isPrimary) {
        Button(
          onClick = { onActionClick(action) },
          modifier = Modifier.height(44.dp),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = BeaconAmber,
              contentColor = OnBeaconAmber,
            ),
        ) {
          Text(
            text = action.label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
          )
        }
      } else {
        OutlinedButton(
          onClick = { onActionClick(action) },
          modifier = Modifier.height(44.dp),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment),
        ) {
          Text(
            text = action.label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }

    OutlinedButton(
      onClick = onLook,
      modifier = Modifier.height(44.dp),
      shape = RoundedCornerShape(10.dp),
      contentPadding = PaddingValues(horizontal = 14.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = DriftwoodText),
    ) {
      Text(
        text = "Look Around",
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        softWrap = false,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoveTabContent(
  state: GameState,
  onMove: (Direction) -> Unit,
) {
  val loc = IslandWorld.locations.getValue(state.currentLocation)
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    loc.exits.forEach { (dir, destId) ->
      val destName = IslandWorld.locations.getValue(destId).name
      val isLocked =
        loc.id == LocationId.CHASM_BRIDGE &&
          dir == Direction.EAST &&
          !state.flags.bridgeUnlocked

      OutlinedButton(
        onClick = { onMove(dir) },
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors =
          ButtonDefaults.outlinedButtonColors(
            contentColor = if (isLocked) StormRust else Parchment,
          ),
      ) {
        Text(
          text =
            if (isLocked) {
              "${dir.shortLabel}: $destName (Locked)"
            } else {
              "${dir.shortLabel}: $destName (-1)"
            },
          style = MaterialTheme.typography.labelLarge,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun BagTabContent(
  state: GameState,
  onUseItem: (String) -> Unit,
) {
  if (state.inventory.isEmpty()) {
    Text(
      text = "Your canvas satchel is empty. Explore the island to gather tools and rations.",
      style = MaterialTheme.typography.bodyMedium,
      color = DriftwoodText,
      modifier = Modifier.padding(vertical = 8.dp),
    )
    return
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    state.inventory.forEach { itemId ->
      val item = IslandWorld.items.getValue(itemId)
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(HarborPanel, RoundedCornerShape(10.dp))
            .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Parchment,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = item.description,
            style = MaterialTheme.typography.labelSmall,
            color = DriftwoodText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (item.isConsumable) {
          Button(
            onClick = { onUseItem("use ${item.shortName.lowercase()}") },
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = SeafoamSage,
                contentColor = OnBeaconAmber,
              ),
          ) {
            Text(
              text = "+${item.energyRestore} Energy",
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              softWrap = false,
            )
          }
        } else {
          OutlinedButton(
            onClick = { onUseItem("examine ${item.shortName.lowercase()}") },
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
          ) {
            Text(
              text = "Inspect",
              style = MaterialTheme.typography.labelLarge,
              color = Parchment,
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CommandInputBar(
  value: String,
  onValueChange: (String) -> Unit,
  onSubmit: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.weight(1f),
      singleLine = true,
      placeholder = {
        Text(
          text = "Type command (e.g. go north, look, help)...",
          style = MaterialTheme.typography.bodyMedium,
          color = DriftwoodText.copy(alpha = 0.7f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      },
      textStyle = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
      keyboardActions = KeyboardActions(onSend = { onSubmit() }),
      shape = RoundedCornerShape(10.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BeaconAmber,
          unfocusedBorderColor = HairlineBorderStrong,
          focusedContainerColor = HarborPanel,
          unfocusedContainerColor = HarborPanel,
          cursorColor = BeaconAmber,
        ),
    )

    Button(
      onClick = onSubmit,
      enabled = value.isNotBlank(),
      modifier = Modifier.size(52.dp),
      shape = RoundedCornerShape(10.dp),
      contentPadding = PaddingValues(0.dp),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = BeaconAmber,
          contentColor = OnBeaconAmber,
          disabledContainerColor = HarborPanelElevated,
          disabledContentColor = DriftwoodText,
        ),
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.Send,
        contentDescription = "Send command",
      )
    }
  }
}
