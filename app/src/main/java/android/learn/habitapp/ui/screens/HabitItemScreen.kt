package android.learn.habitapp.ui.screens

import android.content.Context
import android.learn.habitapp.ui.components.ColorPicker
import android.learn.habitapp.ui.components.FrequencyPicker
import android.learn.habitapp.HabitDetailViewModel
import android.learn.habitapp.ui.components.HabitEmojiPickerSheet
import android.learn.habitapp.R
import android.learn.habitapp.ui.components.ReminderPicker
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.components.ConfirmSaveDialog
import android.learn.habitapp.ui.components.EmojiButton
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import android.util.Log
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ModeEditOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
   Surface(
      Modifier.fillMaxSize()
   ) {
      Log.d("Tag", "wonder if it will work$habitId")

      HabitItemScreen(
         uiState,
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
   val focusManager = LocalFocusManager.current
   var showSaveDialog by remember { mutableStateOf(false) }
   val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(label = "rounded corner") { enterExit ->
      when (enterExit) {
         EnterExitState.PreEnter -> 20.dp
         EnterExitState.Visible -> 0.dp
         EnterExitState.PostExit -> 20.dp
      }

   }
   val context: Context = LocalContext.current
   val onSave: () -> Unit = {
      if (habit.name.isEmpty()) {
         Toast
            .makeText(
               context,
               "Please include the name of the habit",
               Toast.LENGTH_LONG
            )
            .show()
      } else {
         focusManager.clearFocus()
         onSave() // If your onSave doesn't trigger navigation automatically, call safeNavigateBack() here too.
      }
   }
   with(LocalSharedTransitionScope.current) {
      Column(
         Modifier
            .sharedBounds(
               sharedContentState = rememberSharedContentState(
                  key = HabitSharedElementKey(
                     habit.id, type = HabitSharedElementType.Bounds
                  )
               ),
               animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

               clipInOverlayDuringTransition = OverlayClip(
                  RoundedCornerShape(roundedCornerAnimation)
               ),
               resizeMode = if (isNewItem) SharedTransitionScope.ResizeMode.RemeasureToBounds else SharedTransitionScope.ResizeMode.scaleToBounds()
            )
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
      ) {

         HabitItemTopAppBar(
            habit.id, habit.emoji, onBackPressed = {
               if (!stateHasChanged) {
                  focusManager.clearFocus()
                  onBack()
               } else {
                  showSaveDialog = true
               }
            }, onEditIcon = {
               focusManager.clearFocus()
               showEmojiPicker = true

            }, onSaveHabit = onSave, onArchiveHabit = onArchiveHabit
         )
         if (habit.currentStreak > 0) {
            Row(
               modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
               verticalAlignment = Alignment.CenterVertically
            ) {
               Text("🔥", style = MaterialTheme.typography.headlineSmall)
               Spacer(Modifier.width(8.dp))
               Column {
                  Text(
                     "${habit.currentStreak} day streak",
                     style = MaterialTheme.typography.titleMedium
                  )
                  Text(
                     "Keep it going!",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
               }
            }
         }

         TextField(
            value = habit.name,
            onValueChange = onNameChange,
            textStyle = MaterialTheme.typography.titleLarge,
            placeholder = {
               Text(
                  "Title",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White.copy(alpha = .5f)
               )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
               focusedContainerColor = Color.Transparent,
               unfocusedContainerColor = Color.Transparent,
               disabledContainerColor = Color.Transparent,
               focusedIndicatorColor = Color.Transparent,
               unfocusedIndicatorColor = Color.Transparent,


               ),
            singleLine = true
         )
         FrequencyPicker(
            frequencyType = habit.frequencyType,
            customDays = habit.customDays,
            timesPerWeek = habit.timesPerWeek,
            onFrequencyTypeChange = onFrequencyTypeChange,
            onCustomDaysChange = onCustomDaysChange,
            onTimesPerWeekChange = onTimesPerWeekChange
         )

         ReminderPicker(
            reminderTime = habit.reminderTime,
            onReminderChange = onReminderTimeChange,
            canScheduleExactAlarms = canScheduleExactAlarms,
            onRequestExactAlarmPermission = onRequestExactAlarmPermission,
         )
         ColorPicker(
            selectedColor = habit.color, onColorSelected = onColorChange
         )

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
   val onCancel: () -> Unit = { showSaveDialog = false }
   if (showSaveDialog) {
      ConfirmSaveDialog(
         onDiscardRequest = onDiscardRequest,
         onCancel = onCancel,
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
            // This handles hardware back buttons or tapping outside the sheet
            showEmojiPicker = false
         },
         onCloseSheet = {
            // This lets us trigger a beautiful closing animation from inside the sheet
            scope.launch {
               sheetState.hide() // 1. Animate down
            }.invokeOnCompletion {
               if (!sheetState.isVisible) {
                  showEmojiPicker = false // 2. Remove from UI once hidden
               }
            }
         })
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
                  enter = fadeIn() + slideInVertically { fullHeight -> fullHeight })
               .skipToLookaheadPosition()
               .padding(10.dp), title = {
               Text("Edit habit", Modifier)
            }, navigationIcon = {
               IconButton(onClick = onBackPressed, modifier = Modifier) {
                  Icon(
                     imageVector = Icons.Outlined.ArrowBack,
                     contentDescription = stringResource(R.string.back_button),
                     modifier = Modifier.fillMaxSize(.6f)
                  )
               }
            }, actions = {
               Box(
                  modifier = Modifier, contentAlignment = Alignment.Center

               ) {
                  var emojiSize by remember { mutableStateOf(IntSize.Zero) }
                  val density = LocalDensity.current

                  val editButtonSize = remember(emojiSize, density) {
                     with(density) {
                        (emojiSize.width * 0.5f).toDp()
                     }
                  }
                  EmojiButton(
                     selectedEmoji,
                     modifier = Modifier
                        .onSizeChanged { emojiSize = it }
                        .sharedElement(
                           rememberSharedContentState(
                              key = HabitSharedElementKey(
                                 habitId, type = HabitSharedElementType.Emoji
                              )
                           ),
                           animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                        ),
                     onEditIcon,
                  )
                  IconButton(
                     modifier = Modifier
                        .size(editButtonSize)
                        .align(Alignment.BottomEnd)
                        .renderInSharedTransitionScopeOverlay(
                           zIndexInOverlay = 1f,
                        )
                        .animateEnterExit(
                           enter = fadeIn() + slideInVertically() { it },
                           exit = fadeOut() + slideOutVertically() { it }),
                     onClick = onEditIcon,
                     colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.onPrimary)
                  ) {
                     Icon(
                        imageVector = Icons.Outlined.ModeEditOutline,
                        contentDescription = "Edit Icon",
                        modifier = Modifier
                           .padding(3.dp)
                           .fillMaxSize()
                     )
                  }

               }
               Spacer(Modifier.width(30.dp))
               var showMenu by remember { mutableStateOf(false) }
               Box {
                  IconButton(onClick = { showMenu = true }) {
                     Icon(Icons.Default.MoreVert, contentDescription = "More options")
                  }
                  DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                     DropdownMenuItem(
                        text = { Text("Archive habit") },
                        leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                        onClick = {
                           showMenu = false
                           onArchiveHabit()
                        })
                  }
               }
               IconButton(onClick = onSaveHabit, modifier = Modifier) {
                  Icon(
                     imageVector = Icons.Default.Check,
                     contentDescription = "Save",
                     modifier = Modifier.fillMaxSize(.8f)
                  )
               }
            })
      }
   }
}
