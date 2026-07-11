package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
   habitViewModel: HabitViewModel,
   screenTitle: String,
   onMenuClick: () -> Unit
) {
   val archivedHabitUiState by habitViewModel.archivedHabitUiState.collectAsStateWithLifecycle()

   val snackbarHostState = remember { SnackbarHostState() }
   val scope = rememberCoroutineScope()

   val unArchiveHabit: (Int) -> Unit = { habitId ->
      habitViewModel.onUndoArchive(habitId)
      scope.launch {
         val result = snackbarHostState.showSnackbar(
            message = "Habit Unarchived", actionLabel = "Undo", duration = SnackbarDuration.Short
         )
         if (result == SnackbarResult.ActionPerformed) {
            habitViewModel.onArchiveHabit(habitId)
         }
      }
   }
   Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
   ) { innerPadding ->

      Column(
         modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
      ) {
         Column(
            modifier = Modifier
               .fillMaxWidth()
               .statusBarsPadding()
         ) {
            AnimatedTopAppBar {
               // NORMAL STATE: Render the real top app bar
               TopAppBar(
                  title = { Text(screenTitle, style = MaterialTheme.typography.titleLarge) },
                  navigationIcon = {
                     // The three-line hamburger menu button
                     IconButton(onClick = onMenuClick) {
                        Icon(
                           imageVector = Icons.Default.Menu,
                           contentDescription = "Open Navigation Menu"
                        )
                     }
                  },
                  modifier = Modifier
                     .statusBarsPadding()
                     .animateContentSize(animationSpec = tween(5000))
               )

            }
         }      // --- MAIN BODY CONTENT AREA BELOW THE SEARCH SECTOR ---
         Box(
            modifier = Modifier
               .fillMaxWidth()
               .weight(1f)
         ) {
            MainBodyContent(
               archivedHabitUiState,
               habitViewModel = habitViewModel,
               onArchiveHabit = unArchiveHabit,
               onHabitClicked = {},
               onClickScrimBackground = {},
            )
         }
      }
   }
}
