package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.R
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.components.ErrorScreen
import android.learn.habitapp.ui.components.LoadingSpinner
import android.learn.habitapp.ui.components.SubScreenTopBar
import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(
   habitViewModel: HabitViewModel,
   onBack: () -> Unit,
   onHabitClicked: (Int) -> Unit,
) {
   val habitState by habitViewModel.habitUiState.collectAsStateWithLifecycle()

   Column(
      modifier = Modifier
         .fillMaxSize()
         .padding(horizontal = 16.dp)
   ) {
      SubScreenTopBar(title = "Reminders", onBack = onBack)

      when (val state = habitState) {
         is UiState.Success -> {
            val withReminders = remember(state.habits) {
               state.habits
                  .filter { it.reminderTime != null }
                  .sortedBy { it.reminderTime }
            }

            if (withReminders.isEmpty()) {
               RemindersEmptyState()
            } else {
               Text(
                  "Tap a habit to change or clear its reminder.",
                  style = MaterialTheme.typography.bodySmall,
                  color = HabitColors.TextSecondary,
                  modifier = Modifier.padding(vertical = 8.dp)
               )
               LazyColumn {
                  items(withReminders, key = { it.id }) { habit ->
                     ReminderRow(habit = habit, onClick = { onHabitClicked(habit.id) })
                  }
               }
            }
         }

         is UiState.Loading -> LoadingSpinner()
         is UiState.Error -> ErrorScreen(state.message)
      }
   }
}

@Composable
private fun ReminderRow(habit: HabitUiState, onClick: () -> Unit) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .clickable(onClick = onClick)
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(14.dp))
         .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
   ) {
      Box(
         modifier = Modifier
            .background(color = HabitColors.PrimaryLight, shape = CircleShape)
            .padding(8.dp)
      ) {
         Text(habit.emoji, style = MaterialTheme.typography.titleMedium)
      }

      Column(modifier = Modifier.weight(1f)) {
         Text(
            habit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = HabitColors.TextPrimary,
            maxLines = 1
         )
         Text(
            habit.reminderTime!!.format(DateTimeFormatter.ofPattern("h:mm a")),
            style = MaterialTheme.typography.bodySmall,
            color = HabitColors.TextSecondary
         )
      }

      Icon(
         painter = painterResource(R.drawable.right_arrow_icon),
         contentDescription = null,
         tint = HabitColors.TextSecondary,
         modifier = Modifier
      )
   }
}

@Composable
private fun RemindersEmptyState() {
   Column(
      modifier = Modifier
         .fillMaxSize()
         .padding(top = 80.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
      Icon(
         Icons.Outlined.NotificationsNone,
         contentDescription = null,
         tint = HabitColors.TextSecondary
      )
      Text(
         "No reminders set",
         style = MaterialTheme.typography.titleMedium,
         color = HabitColors.TextPrimary
      )
      Text(
         "Open any habit and set a time to get reminded about it.",
         style = MaterialTheme.typography.bodySmall,
         color = HabitColors.TextSecondary,
         textAlign = TextAlign.Center,
         modifier = Modifier.padding(horizontal = 32.dp)
      )
   }
}
