package android.learn.habitapp.ui.screens

import android.content.Context
import android.learn.habitapp.HabitDetailViewModel
import android.learn.habitapp.R
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.components.ColorPicker
import android.learn.habitapp.ui.components.ConfirmSaveDialog
import android.learn.habitapp.ui.components.EmojiButton
import android.learn.habitapp.ui.components.FrequencyPicker
import android.learn.habitapp.ui.components.HabitEmojiPickerSheet
import android.learn.habitapp.ui.components.ReminderPicker
import android.learn.habitapp.ui.theme.HabitColors
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ModeEditOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun HabitItemRoute(
   viewModel: HabitDetailViewModel,
   habitId: Int,
   onBack: () -> Unit,
) {
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   val uiStateHasChanged by viewModel.uiStateHasChanged.collectAsStateWithLifecycle()

   var hasNavigatedBack by remember { mutableStateOf(false) }
   val context: Context = LocalContext.current

   Surface(modifier = Modifier.fillMaxSize()) {
      HabitItemScreen(
         habit = uiState,
         stateHasChanged = uiStateHasChanged,
         onNameChange = viewModel::onNameChanged,
         onEmojiChange = viewModel::onEmojiChanged,
         onReminderTimeChange = viewModel::onReminderTimeChanged,
         onFrequencyTypeChange = viewModel::onFrequencyTypeChanged,
         onCustomDaysChange = viewModel::onCustomDaysChanged,
         onTimesPerWeekChange = viewModel::onTimesPerWeekChanged,
         onArchiveHabit = { viewModel.archiveHabit { onBack() } },
         onSave = {
            viewModel.saveHabit { onBack() }
            viewModel.resetUiStateHasChanged()
         },
         onRequestExactAlarmPermission = { viewModel.requestExactAlarmPermission(context) },
         canScheduleExactAlarms = viewModel::canScheduleExactAlarms,
         onResetStateHasChanged = viewModel::resetUiStateHasChanged,
         onColorChange = viewModel::onColorChanged,
      ) {
         if (!hasNavigatedBack) {
            hasNavigatedBack = true
            onBack()
         }
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItemScreen(
   habit: HabitUiState,
   stateHasChanged: Boolean,
   onNameChange: (String) -> Unit,
   onEmojiChange: (String) -> Unit,
   onColorChange: (Int?) -> Unit,
   onReminderTimeChange: (LocalTime?) -> Unit,
   canScheduleExactAlarms: () -> Boolean,
   onRequestExactAlarmPermission: () -> Unit,
   onFrequencyTypeChange: (FrequencyType) -> Unit,
   onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
   onTimesPerWeekChange: (Int) -> Unit,
   onArchiveHabit: () -> Unit,
   modifier: Modifier = Modifier,
   onResetStateHasChanged: () -> Unit,
   onSave: () -> Unit,
   onBack: () -> Unit,
) {
   val isNewItem = habit.id == -1
   val scope = rememberCoroutineScope()
   val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
   var showEmojiPicker by remember { mutableStateOf(false) }
   var showSaveDialog by remember { mutableStateOf(false) }
   val focusManager = LocalFocusManager.current
   val context: Context = LocalContext.current

   val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(
      label = "rounded corner"
   ) { enterExit ->
      when (enterExit) {
         EnterExitState.PreEnter -> 28.dp
         EnterExitState.Visible -> 0.dp
         EnterExitState.PostExit -> 28.dp
      }
   }

   val saveAction: () -> Unit = {
      if (habit.name.isBlank()) {
         Toast.makeText(
            context,
            "Please include the name of the habit",
            Toast.LENGTH_LONG
         ).show()
      } else {
         focusManager.clearFocus()
         onSave()
      }
   }

   with(LocalSharedTransitionScope.current) {
      Box(
         modifier = modifier
            .fillMaxSize()
            .background(
               Brush.verticalGradient(
                  listOf(
                     HabitColors.BackgroundTop,
                     HabitColors.BackgroundBottom
                  )
               )
            )
      ) {
         Column(
            modifier = Modifier
               .sharedBounds(
                  sharedContentState = rememberSharedContentState(
                     key = HabitSharedElementKey(
                        habit.id,
                        type = HabitSharedElementType.Bounds
                     )
                  ),
                  animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                  clipInOverlayDuringTransition = OverlayClip(
                     RoundedCornerShape(roundedCornerAnimation)
                  ),
                  resizeMode = if (isNewItem) {
                     SharedTransitionScope.ResizeMode.RemeasureToBounds
                  } else {
                     SharedTransitionScope.ResizeMode.scaleToBounds()
                  }
               )
               .fillMaxSize()
               .padding(horizontal = 20.dp)
               .verticalScroll(rememberScrollState())
         ) {
            HabitItemTopAppBar(
               habitId = habit.id,
               selectedEmoji = habit.emoji,
               onBackPressed = {
                  if (!stateHasChanged) {
                     focusManager.clearFocus()
                     onBack()
                  } else {
                     showSaveDialog = true
                  }
               },
               onArchiveHabit = onArchiveHabit,
               onEditIcon = {
                  focusManager.clearFocus()
                  showEmojiPicker = true
               },
               onSaveHabit = saveAction
            )

            Spacer(Modifier.height(10.dp))

            if (habit.currentStreak > 0) {
               StreakCard(
                  streak = habit.currentStreak,
                  modifier = Modifier.fillMaxWidth()
               )
               Spacer(Modifier.height(14.dp))
            }

            HabitTitleCard(
               title = habit.name,
               onValueChange = onNameChange
            )

            Spacer(Modifier.height(14.dp))

            SectionCard(
               title = "Frequency",
               subtitle = "How often should this habit repeat?"
            ) {
               FrequencyPicker(
                  frequencyType = habit.frequencyType,
                  customDays = habit.customDays,
                  timesPerWeek = habit.timesPerWeek,
                  onFrequencyTypeChange = onFrequencyTypeChange,
                  onCustomDaysChange = onCustomDaysChange,
                  onTimesPerWeekChange = onTimesPerWeekChange
               )
            }

            Spacer(Modifier.height(14.dp))

            SectionCard(
               title = "Reminder",
               subtitle = "Keep the habit visible at the right time."
            ) {
               ReminderPicker(
                  reminderTime = habit.reminderTime,
                  onReminderChange = onReminderTimeChange,
                  canScheduleExactAlarms = canScheduleExactAlarms,
                  onRequestExactAlarmPermission = onRequestExactAlarmPermission,
               )
            }

            Spacer(Modifier.height(14.dp))

            SectionCard(
               title = "Theme",
               subtitle = "Use a color that matches the habit mood."
            ) {
               ColorPicker(
                  selectedColor = habit.color,
                  onColorSelected = onColorChange
               )
            }

            Spacer(Modifier.height(18.dp))

            ActionRow(
               onSave = saveAction,
               onArchive = onArchiveHabit
            )

            Spacer(Modifier.height(24.dp))
         }
      }
   }

   BackHandler(enabled = stateHasChanged) {
      showSaveDialog = true
   }

   val onDiscardRequest: () -> Unit = {
      onResetStateHasChanged()
      showSaveDialog = false
      onBack()
   }
   val onDialogSave: () -> Unit = {
      onResetStateHasChanged()
      showSaveDialog = false
      onSave()
   }

   if (showSaveDialog) {
      ConfirmSaveDialog(
         onDiscardRequest = onDiscardRequest,
         onCancel = { showSaveDialog = false },
         onSave = onDialogSave,
         dialogTitle = "Discard Changes?",
         dialogText = "Are you sure you want to discard your changes?",
         icon = Icons.Outlined.Save
      )
   }

   if (showEmojiPicker) {
      HabitEmojiPickerSheet(
         sheetState = sheetState,
         onEmojiSelected = onEmojiChange,
         onDismissRequest = {
            showEmojiPicker = false
         },
         onCloseSheet = {
            scope.launch {
               sheetState.hide()
            }.invokeOnCompletion {
               if (!sheetState.isVisible) {
                  showEmojiPicker = false
               }
            }
         }
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitItemTopAppBar(
   habitId: Int,
   selectedEmoji: String,
   onBackPressed: () -> Unit,
   onArchiveHabit: () -> Unit,
   onEditIcon: () -> Unit,
   onSaveHabit: () -> Unit
) {
   with(LocalSharedTransitionScope.current) {
      with(LocalAnimatedVisibilityScope.current) {
         TopAppBar(
            modifier = Modifier
               .animateEnterExit(
                  enter = fadeIn() + slideInVertically { fullHeight -> fullHeight },
                  exit = fadeOut() + slideOutVertically { fullHeight -> fullHeight }
               )
               .skipToLookaheadPosition(),
            title = {
               Column {
                  Text(
                     text = "Edit habit",
                     style = MaterialTheme.typography.headlineSmall,
                     color = HabitColors.TextPrimary
                  )
                  Text(
                     text = "Shape your routine",
                     style = MaterialTheme.typography.bodySmall,
                     color = HabitColors.TextSecondary
                  )
               }
            },
            navigationIcon = {
               IconButton(
                  onClick = onBackPressed,
                  colors = IconButtonDefaults.iconButtonColors(
                     containerColor = HabitColors.Surface,
                     contentColor = HabitColors.TextPrimary
                  )
               ) {
                  Icon(
                     imageVector = Icons.Outlined.ArrowBack,
                     contentDescription = stringResource(R.string.back_button)
                  )
               }
            },
            actions = {
               Box(contentAlignment = Alignment.Center) {
                  var emojiSize by remember { mutableStateOf(IntSize.Zero) }
                  val density = LocalDensity.current

                  val editButtonSize = remember(emojiSize, density) {
                     (emojiSize.width * 0.20f).dp
                  }

                  EmojiButton(
                     selectedEmoji = selectedEmoji,
                     modifier = Modifier
                        .onSizeChanged { emojiSize = it }
                        .sharedElement(
                           rememberSharedContentState(
                              key = HabitSharedElementKey(
                                 habitId,
                                 type = HabitSharedElementType.Emoji
                              )
                           ),
                           animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                        ),
                     onClickIcon = onEditIcon,
                  )

                  IconButton(
                     modifier = Modifier
                        .size(editButtonSize)
                        .align(Alignment.BottomEnd)
                        .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
                     onClick = onEditIcon,
                     colors = IconButtonDefaults.iconButtonColors(
                        containerColor = HabitColors.Primary,
                        contentColor = Color.White
                     )
                  ) {
                     Icon(
                        imageVector = Icons.Outlined.ModeEditOutline,
                        contentDescription = "Edit icon",
                        modifier = Modifier
                           .padding(3.dp)
                           .fillMaxSize()
                     )
                  }
               }

               Spacer(Modifier.width(10.dp))

               var showMenu by remember { mutableStateOf(false) }
               Box {
                  IconButton(
                     onClick = { showMenu = true },
                     colors = IconButtonDefaults.iconButtonColors(
                        containerColor = HabitColors.Surface,
                        contentColor = HabitColors.TextPrimary
                     )
                  ) {
                     Icon(Icons.Default.MoreVert, contentDescription = "More options")
                  }

                  DropdownMenu(
                     expanded = showMenu,
                     onDismissRequest = { showMenu = false }
                  ) {
                     DropdownMenuItem(
                        text = { Text("Archive habit") },
                        leadingIcon = {
                           Icon(Icons.Outlined.Archive, contentDescription = null)
                        },
                        onClick = {
                           showMenu = false
                           onArchiveHabit()
                        }
                     )
                  }
               }

               Spacer(Modifier.width(8.dp))

               IconButton(
                  onClick = onSaveHabit,
                  colors = IconButtonDefaults.iconButtonColors(
                     containerColor = HabitColors.Primary,
                     contentColor = Color.White
                  )
               ) {
                  Icon(
                     imageVector = Icons.Default.Check,
                     contentDescription = "Save"
                  )
               }
            },
            windowInsets = WindowInsets()
         )
      }
   }
}

@Composable
private fun StreakCard(
   streak: Int,
   modifier: Modifier = Modifier
) {
   Surface(
      modifier = modifier,
      shape = RoundedCornerShape(24.dp),
      color = HabitColors.PrimaryDark,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp
   ) {
      Row(
         modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         Box(
            modifier = Modifier
               .size(48.dp)
               .clip(RoundedCornerShape(16.dp))
               .background(HabitColors.Accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
         ) {
            Text("🔥", style = MaterialTheme.typography.headlineSmall)
         }

         Spacer(Modifier.width(14.dp))

         Column(modifier = Modifier.weight(1f)) {
            Text(
               text = "$streak day streak",
               style = MaterialTheme.typography.titleLarge,
               color = Color.White
            )
            Text(
               text = "Keep it going.",
               style = MaterialTheme.typography.bodyMedium,
               color = Color.White.copy(alpha = 0.78f)
            )
         }

         AssistChip(
            onClick = { },
            label = { Text("Active") },
            colors = AssistChipDefaults.assistChipColors(
               containerColor = HabitColors.Accent.copy(alpha = 0.18f),
               labelColor = Color.White
            ),
            border = null
         )
      }
   }
}

@Composable
private fun HabitTitleCard(
   title: String,
   onValueChange: (String) -> Unit
) {
   Surface(
      shape = RoundedCornerShape(24.dp),
      color = HabitColors.Surface,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      modifier = Modifier
         .fillMaxWidth()
         .border(
            width = 1.dp,
            color = HabitColors.SoftBorder,
            shape = RoundedCornerShape(24.dp)
         )
   ) {
      Column(modifier = Modifier.padding(16.dp)) {
         Text(
            text = "Habit name",
            style = MaterialTheme.typography.labelLarge,
            color = HabitColors.TextSecondary
         )
         Spacer(Modifier.height(6.dp))
         OutlinedTextField(
            value = title,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.titleLarge.copy(
               color = HabitColors.TextPrimary
            ),
            placeholder = {
               Text(
                  text = "Read for 20 minutes",
                  style = MaterialTheme.typography.titleLarge,
                  color = HabitColors.TextSecondary.copy(alpha = 0.65f)
               )
            },
            colors = OutlinedTextFieldDefaults.colors(
               focusedTextColor = HabitColors.TextPrimary,
               unfocusedTextColor = HabitColors.TextPrimary,
               focusedContainerColor = HabitColors.SurfaceTint,
               unfocusedContainerColor = HabitColors.SurfaceTint,
               focusedBorderColor = HabitColors.Primary,
               unfocusedBorderColor = HabitColors.Outline,
               cursorColor = HabitColors.Primary,
               focusedLabelColor = HabitColors.Primary,
               unfocusedLabelColor = HabitColors.TextSecondary
            )
         )
      }
   }
}

@Composable
private fun SectionCard(
   title: String,
   subtitle: String,
   content: @Composable () -> Unit
) {
   Surface(
      shape = RoundedCornerShape(24.dp),
      color = HabitColors.Surface,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      modifier = Modifier
         .fillMaxWidth()
         .border(
            width = 1.dp,
            color = HabitColors.SoftBorder,
            shape = RoundedCornerShape(24.dp)
         )
   ) {
      Column(modifier = Modifier.padding(16.dp)) {
         Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = HabitColors.TextPrimary
         )
         Spacer(Modifier.height(4.dp))
         Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = HabitColors.TextSecondary
         )
         Spacer(Modifier.height(14.dp))
         content()
      }
   }
}

@Composable
private fun ActionRow(
   onSave: () -> Unit,
   onArchive: () -> Unit
) {
   Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
   ) {
      Button(
         onClick = onSave,
         modifier = Modifier.weight(1f),
         shape = RoundedCornerShape(18.dp),
         contentPadding = PaddingValues(vertical = 14.dp),
      ) {
         Icon(Icons.Default.Check, contentDescription = null)
         Spacer(Modifier.width(8.dp))
         Text("Save")
      }
      OutlinedButton(
         onClick = onArchive,
         modifier = Modifier.weight(1f),
         shape = RoundedCornerShape(18.dp),
         contentPadding = PaddingValues(vertical = 14.dp),
         colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HabitColors.Danger
         )
      ) {
         Icon(Icons.Outlined.Archive, contentDescription = null)
         Spacer(Modifier.width(8.dp))
         Text("Archive")
      }
   }
}
