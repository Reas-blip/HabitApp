package android.learn.habitapp

import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.navigation.animatedComposable
import android.learn.habitapp.ui.screens.HabitItemRoute
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.getValue

@Serializable
sealed interface Destination {


   @Serializable
   data object Today : Destination

   @Serializable
   data object More : Destination

   @Serializable
   data object Stats : Destination

   @Serializable
   data object Habits : Destination


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
               Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                     painterResource(item.iconId),
                     contentDescription = item.label,
                  )
                  if (isSelected) {
                     Spacer(Modifier.height(2.dp))
                     Box(
                        Modifier
                           .size(4.dp)
                           .clip(CircleShape)
                           .background(GreenMid)
                     )
                  }
               }
            },
            label = { Text(item.label, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
               selectedTextColor = MaterialTheme.colorScheme.onPrimary,
               unselectedIconColor = TextMuted,
               unselectedTextColor = TextMuted,
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
         BottomNavBar(
            navController,
            currentDestination
         )
      }
   ) { innerPadding ->
      NavHost(
         navController = navController,
         startDestination = Destination.Today,
         modifier = Modifier
            .background(Color.Transparent)
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
                  navController.navigate(HabitDetail(habitId))
               },
            )
         }

         animatedComposable<Destination.Habits> {
            HabitScreen(
               habitViewModel = habitViewModel,
               onAddHabit = {
                  detailViewModel.newHabit()
                  navController.navigate(HabitDetail())
               },
               onHabitClicked = { habitId ->
                  detailViewModel.loadHabit(habitId)
                  navController.navigate(HabitDetail(habitId))
               },
            )
         }

         animatedComposable<Destination.Stats> {
            StatsScreen(
               habitViewModel = habitViewModel
            )
         }

         animatedComposable<Destination.More> {
            MoreScreen()
         }

         animatedComposable<HabitDetail> { backStackEntry ->

            val detailArgs = backStackEntry.toRoute<HabitDetail>()

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


      }
   }
}