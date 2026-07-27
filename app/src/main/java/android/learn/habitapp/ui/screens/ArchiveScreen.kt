package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.components.ConfirmDialog
import android.learn.habitapp.ui.components.ErrorScreen
import android.learn.habitapp.ui.components.LoadingSpinner
import android.learn.habitapp.ui.components.SubScreenTopBar
import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArchiveScreen(
   habitViewModel: HabitViewModel,
   onBack: () -> Unit,
   onHabitClicked: (Int) -> Unit,
) {
   val archivedState by habitViewModel.archivedHabitUiState.collectAsStateWithLifecycle()
   var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

   Column(
      modifier = Modifier
         .fillMaxSize()
         .padding(horizontal = 16.dp)
   ) {
      SubScreenTopBar(title = "Archived Habits", onBack = onBack)

      when (val state = archivedState) {
         is UiState.Success -> {
            if (state.habits.isEmpty()) {
               ArchiveEmptyState()
            } else {
               LazyColumn(modifier = Modifier.fillMaxHeight()) {
                  items(state.habits, key = { it.id }) { habit ->
                     Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                     ) {
                        Box(modifier = Modifier.weight(1f)) {
                           ArchivableHabitRowNewUi(
                              habitId = habit.id,
                              habitName = habit.name,
                              streak = habit.currentStreak,
                              isToggled = habit.isDoneToday,
                              emoji = habit.emoji,
                              habitColor = habit.color,
                              isArchived = true,
                              onToggle = { habitViewModel.onHabitChecked(habit.id) },
                              onClickHabit = { onHabitClicked(habit.id) },
                              onArchive = { habitViewModel.onUndoArchive(habit.id) },
                           )
                        }
                        IconButton(onClick = { pendingDeleteId = habit.id }) {
                           Icon(
                              Icons.Outlined.DeleteForever,
                              contentDescription = "Delete forever",
                              tint = HabitColors.Danger
                           )
                        }
                     }
                  }
               }
            }
         }

         is UiState.Loading -> LoadingSpinner()
         is UiState.Error -> ErrorScreen(state.message)
      }
   }

   val habitPendingDelete = (archivedState as? UiState.Success)?.habits
      ?.firstOrNull { it.id == pendingDeleteId }

   if (habitPendingDelete != null) {
      ConfirmDialog(
         title = "Delete \"${habitPendingDelete.name}\" forever?",
         text = "This permanently deletes the habit and all of its history. This can't be undone.",
         confirmLabel = "Delete Forever",
         isDestructive = true,
         icon = Icons.Outlined.DeleteForever,
         onConfirm = {
            habitViewModel.onDeleteHabit(habitPendingDelete.id)
            pendingDeleteId = null
         },
         onDismiss = { pendingDeleteId = null }
      )
   }
}

@Composable
private fun ArchiveEmptyState() {
   Column(
      modifier = Modifier
         .fillMaxSize()
         .padding(top = 80.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
      Icon(
         Icons.Outlined.Inventory2,
         contentDescription = null,
         tint = HabitColors.TextSecondary,
         modifier = Modifier
      )
      Text(
         "No archived habits",
         style = MaterialTheme.typography.titleMedium,
         color = HabitColors.TextPrimary
      )
      Text(
         "Habits you archive from the Habits screen show up here. Swipe one to restore it.",
         style = MaterialTheme.typography.bodySmall,
         color = HabitColors.TextSecondary,
         textAlign = TextAlign.Center,
         modifier = Modifier.padding(horizontal = 32.dp)
      )
   }
}
