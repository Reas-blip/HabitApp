package android.learn.habitapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import android.learn.habitapp.ui.theme.HabitAppTheme
import android.learn.habitapp.ui.components.DrawerNavigation


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
   val habitViewModel: HabitViewModel by viewModels()


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      setContent {
         val navController = rememberNavController()
         HabitAppTheme {

            LaunchedEffect(Unit) {
               val habitId = intent.getIntExtra("openHabitId", -1)
               if (habitId != -1) {
                  habitViewModel.requestScrollTo(habitId)
                  // no navigation needed — HabitList is already the start destination
               }
            }
            val detailViewModel: HabitDetailViewModel = hiltViewModel()

            DrawerNavigation(
               habitViewModel = habitViewModel,
               navController = navController,
               detailViewModel = detailViewModel
            )
         }
      }
   }

   override fun onNewIntent(intent: Intent) {
      super.onNewIntent(intent)
      setIntent(intent)
      val habitId = intent.getIntExtra("openHabitId", -1)
      if (habitId != -1) {
         habitViewModel.requestScrollTo(habitId)
      }
   }
}
