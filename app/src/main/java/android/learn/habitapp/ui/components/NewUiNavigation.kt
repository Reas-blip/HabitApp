package android.learn.habitapp.ui.components

import android.learn.habitapp.HabitDetailViewModel
import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.R
import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.navigation.animatedComposable
import android.learn.habitapp.ui.screens.ArchiveScreen
import android.learn.habitapp.ui.screens.BackupRestoreScreen
import android.learn.habitapp.ui.screens.HabitItemRoute
import android.learn.habitapp.ui.screens.HabitScreen
import android.learn.habitapp.ui.screens.MoreScreen
import android.learn.habitapp.ui.screens.RemindersScreen
import android.learn.habitapp.ui.screens.SearchScreenRoute
import android.learn.habitapp.ui.screens.SettingsScreen
import android.learn.habitapp.ui.screens.StatsScreen
import android.learn.habitapp.ui.screens.TodayHabitScreen
import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

   @Serializable
   data class HabitDetail(
      val habitId: Int? = null, val currentToken: Long = 0L
   ) : Destination


   @Serializable
   data object Search : Destination

   @Serializable
   data object Today : Destination

   @Serializable
   data object More : Destination

   @Serializable
   data object Stats : Destination

   @Serializable
   data object Habits : Destination

   @Serializable
   data object Archive : Destination

   @Serializable
   data object Reminders : Destination

   @Serializable
   data object Settings : Destination

   @Serializable
   data object BackupRestore : Destination


}

@Serializable
data class NavItem(val route: Destination, val label: String, val iconId: Int)

val navItems = listOf(
   NavItem(Destination.Today, "Today", R.drawable.check_icon),
   NavItem(Destination.Habits, "Habits", R.drawable.book_icon),
   NavItem(Destination.Stats, "Stats", R.drawable.three_bar_icon),
   NavItem(Destination.More, "More", R.drawable.more_icon),
)

@Composable
fun BottomNavBar(
   navController: NavController,
   currentDestination: NavDestination?,
) {
   NavigationBar(containerColor = MaterialTheme.colorScheme.onSurface, tonalElevation = 0.dp) {
      navItems.forEach { item ->

         val isSelected = currentDestination?.hasRoute(item.route::class) == true
         NavigationBarItem(

            selected = isSelected,
            onClick = {
               if (!isSelected) {
                  navController.navigate(item.route) {
                     launchSingleTop = true
                     restoreState = true
                     popUpTo(
                        navController.graph.startDestinationId
                     ) {
                        saveState = true
                     }
                  }
               }
            },
            icon = {
               Icon(
                  painterResource(item.iconId),
                  contentDescription = item.label,
                  tint = if (isSelected) HabitColors.Primary else HabitColors.TextSecondary
               )
            },
            label = {
               Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(item.label, fontSize = 10.sp, color = if (isSelected) HabitColors.Primary else HabitColors.TextSecondary)

                  if (isSelected) {
                     Spacer(Modifier.height(2.dp))
                     Box(
                        Modifier
                           .size(4.dp)
                           .clip(CircleShape)
                           .background(HabitColors.Primary)
                     )
                  }
               }
            },
            colors = NavigationBarItemDefaults.colors(
               selectedTextColor = MaterialTheme.colorScheme.onPrimary,
               unselectedIconColor = HabitColors.SurfaceTintDark,
               unselectedTextColor = HabitColors.SurfaceTintDark,
               indicatorColor = Color.Transparent,
            ),
         )
      }
   }
}

@Composable
fun HabitAppNewUi(
   habitViewModel: HabitViewModel,
   detailViewModel: HabitDetailViewModel
) {
   val navController = rememberNavController()
   val navBackStackEntry by navController.currentBackStackEntryAsState()
   val currentDestination = navBackStackEntry?.destination
   val coroutineScope = rememberCoroutineScope()
   Scaffold(
      bottomBar = {
         val showBottomBar = currentDestination?.let {
               !( it.hasRoute(Destination.HabitDetail::class) ||
                  it.hasRoute(Destination.Search::class) ||
                  it.hasRoute(Destination.Archive::class) ||
                  it.hasRoute(Destination.Reminders::class) ||
                  it.hasRoute(Destination.Settings::class) ||
                  it.hasRoute(Destination.BackupRestore::class) )
         } ?: false
         if (showBottomBar) BottomNavBar(
            navController,
            currentDestination
         )
      }
   ) { innerPadding ->
      NavHost(
         navController = navController,
         startDestination = Destination.Today,
         modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(innerPadding)
      ) {

         animatedComposable<Destination.Today> {
            TodayHabitScreen(
               habitViewModel = habitViewModel,
               onAddHabit = {
                  detailViewModel.newHabit()
                  navController.navigate(HabitDetail())
               },
               onHabitClicked = { habitId ->
                  detailViewModel.loadHabit(habitId)
                  navController.navigate(Destination.HabitDetail(habitId))
               },
            )
         }

         animatedComposable<Destination.Habits> {
            HabitScreen(
               habitViewModel = habitViewModel,
               onAddHabit = {
                  detailViewModel.newHabit()
                  navController.navigate(Destination.HabitDetail())
               },
               onHabitClicked = { habitId ->
                  detailViewModel.loadHabit(habitId)
                  navController.navigate(Destination.HabitDetail(habitId))
               },
               onSearchClick = {
                  navController.navigate(Destination.Search)
               },
               onNavigateToArchive = {
                  navController.navigate(Destination.Archive)
               }
            )
         }

         animatedComposable<Destination.Stats> {
            StatsScreen(
               habitViewModel = habitViewModel
            )
         }

         animatedComposable<Destination.More> {
            MoreScreen(
               onNavigateToHabits = {
                  navController.navigate(Destination.Habits) {
                     launchSingleTop = true
                     popUpTo(navController.graph.startDestinationId) { saveState = true }
                  }
               },
               onNavigateToStats = {
                  navController.navigate(Destination.Stats) {
                     launchSingleTop = true
                     popUpTo(navController.graph.startDestinationId) { saveState = true }
                  }
               },
               onNavigateToReminders = { navController.navigate(Destination.Reminders) },
               onNavigateToSettings = { navController.navigate(Destination.Settings) },
               onNavigateToBackup = { navController.navigate(Destination.BackupRestore) },
            )
         }

         animatedComposable<Destination.Archive> {
            ArchiveScreen(
               habitViewModel = habitViewModel,
               onBack = { navController.popBackStack() },
               onHabitClicked = { habitId ->
                  detailViewModel.loadHabit(habitId)
                  navController.navigate(Destination.HabitDetail(habitId))
               }
            )
         }

         animatedComposable<Destination.Reminders> {
            RemindersScreen(
               habitViewModel = habitViewModel,
               onBack = { navController.popBackStack() },
               onHabitClicked = { habitId ->
                  detailViewModel.loadHabit(habitId)
                  navController.navigate(Destination.HabitDetail(habitId))
               }
            )
         }

         animatedComposable<Destination.Settings> {
            val settingsContext = LocalContext.current
            SettingsScreen(
               onBack = { navController.popBackStack() },
               canScheduleExactAlarms = detailViewModel::canScheduleExactAlarms,
               onRequestExactAlarmPermission = {
                  detailViewModel.requestExactAlarmPermission(settingsContext)
               }
            )
         }

         animatedComposable<Destination.BackupRestore> {
            BackupRestoreScreen(
               habitViewModel = habitViewModel,
               onBack = { navController.popBackStack() }
            )
         }

         animatedComposable<Destination.HabitDetail> { backStackEntry ->

            val detailArgs = backStackEntry.toRoute<Destination.HabitDetail>()

            HabitItemRoute(
               habitId = detailArgs.habitId ?: -1,
               viewModel = detailViewModel,
            ) {
               coroutineScope.launch {
                  delay(50)
                  navController.popBackStack()

               }
            }
         }
         animatedComposable<Destination.Search> {
            SearchScreenRoute(habitViewModel, onBackPressed = {
               navController.popBackStack()
            }) { habitId ->
               detailViewModel.loadHabit(habitId)
               navController.navigate(Destination.HabitDetail(habitId))
            }
         }


      }
   }
}