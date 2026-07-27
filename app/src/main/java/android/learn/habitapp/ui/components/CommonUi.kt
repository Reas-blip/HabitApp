package android.learn.habitapp.ui.components

import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ErrorScreen(errorMessage: String) {
   Box(contentAlignment = Alignment.Center) {
      Text(errorMessage)
   }
}

@Composable
fun LoadingSpinner() {
   Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
   }

}

/**
 * Shared header for full-screen sub-pages reached via the More tab or the
 * Habits screen's overflow menu (Archive, Reminders, Settings, Backup & Restore) —
 * keeps their look consistent with each other without duplicating this Row everywhere.
 */
@Composable
fun SubScreenTopBar(
   title: String,
   onBack: () -> Unit,
   modifier: Modifier = Modifier,
) {
   Row(
      modifier = modifier
         .fillMaxWidth()
         .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
   ) {
      IconButton(
         onClick = onBack,
         colors = IconButtonDefaults.iconButtonColors(
            containerColor = HabitColors.Surface,
            contentColor = HabitColors.TextPrimary
         ),
         modifier = Modifier.size(40.dp)
      ) {
         Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
      }
      Text(
         text = title,
         style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight(700),
            color = HabitColors.TextPrimary,
         )
      )
   }
}
