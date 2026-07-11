package android.learn.habitapp.ui.components

import android.learn.habitapp.HabitDetailViewModel
import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.navigation.ArchivedHabitScreen
import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.navigation.HabitListScreen
import android.learn.habitapp.navigation.animatedComposable
import android.learn.habitapp.ui.screens.ArchiveScreen
import android.learn.habitapp.ui.screens.HabitItemRoute
import android.learn.habitapp.ui.screens.HabitMainScreen
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DrawerNavigation(
   habitViewModel: HabitViewModel,
   navController: NavHostController,
   detailViewModel: HabitDetailViewModel
) {
   val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
   val coroutineScope = rememberCoroutineScope()
   var selectedItem by remember { mutableIntStateOf(0) }

   val items = listOf("Dashboard", "Archived")
   val icons = listOf(Icons.Default.Home, Icons.Default.Archive)

   // 2. Wrap everything in the ModalNavigationDrawer container
   ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
         // This defines what the sliding sidebar looks like inside
         ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
            Spacer(Modifier.height(16.dp))
            Text(
               text = "My App Menu",
               style = MaterialTheme.typography.titleMedium,
               modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
            )

            items.forEachIndexed { index, item ->
               IOSInsetListRow(
                  icon = icons[index],
                  title = item,
                  selected = selectedItem == index,
                  onClick = {
                     selectedItem = index
                     coroutineScope.launch { drawerState.close() }
                     if (selectedItem == 0) {
                        navController.navigate(HabitListScreen)
                     } else {
                        navController.navigate(ArchivedHabitScreen)
                     }
                     // Close the drawer smoothly after clicking an option
                  },
                  modifier = Modifier
                     .padding(NavigationDrawerItemDefaults.ItemPadding)
                     .padding(8.dp)
               )
            }
         }
      }
   ) {
      NavHost(
         navController = navController,
         startDestination = HabitListScreen,

         ) {

         val onMenuClick: () -> Unit = {
            coroutineScope.launch {
               drawerState.open()
            }
         }
         animatedComposable<HabitListScreen> {
            HabitMainScreen(
               habitViewModel = habitViewModel,
               onCreateHabit = {
                  detailViewModel.newHabit()
                  navController.navigate(HabitDetail())
               },
               onHabitClicked = { habitId ->
                  detailViewModel.loadHabit(habitId)
                  navController.navigate(HabitDetail(habitId))
               },
               screenTitle = "Habits",
               onMenuClick = onMenuClick
            )

         }
         animatedComposable<ArchivedHabitScreen> {
            ArchiveScreen(
               habitViewModel = habitViewModel,
               screenTitle = "Archived Habits",
               onMenuClick = onMenuClick
            )
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

