package android.learn.habitapp

import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.learn.habitapp.ui.theme.HabitAppTheme
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
   val habitViewModel: HabitViewModel by viewModels()
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      setContent {
         HabitAppTheme {
            HabitMainScreen(habitViewModel)
         }
      }
   }
}

@Composable
fun HabitItemScreen(habit: HabitUiState) {
   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .background(Color.Transparent)
   ) { innerPadding ->
      innerPadding
//      TextField()
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitMainScreen(habitViewModel: HabitViewModel) {
   val searchQuery by habitViewModel.searchQuery.collectAsState()
   val habitUiState by habitViewModel.habitsUiState.collectAsStateWithLifecycle()

   val habits by remember {
      derivedStateOf {
         (habitUiState as? UiState.Success)?.habits ?: emptyList()
      }
   }

   val filteredHabits by remember {
      derivedStateOf {
         if (searchQuery.isEmpty()) emptyList()
         else habits
      }
   }

   var isSearchExpanded by remember { mutableStateOf(false) }
   val focusManager = LocalFocusManager.current

   BackHandler(enabled = isSearchExpanded) {
      isSearchExpanded = false
      habitViewModel.onSearchQueryChange("")
      focusManager.clearFocus(force = true)
   }

   // Leave Scaffold's topBar blank so we can dynamically control the top area ourselves
   Scaffold { innerPadding ->
      SharedTransitionLayout() {
         Column(
            modifier = Modifier

               .animateContentSize()
               .fillMaxSize()
               .padding(bottom = innerPadding.calculateBottomPadding())
         )
         {
            if (!isSearchExpanded) {
               // NORMAL STATE: Render the real top app bar
               TopAppBar(
                  title = { Text("My Habits", style = MaterialTheme.typography.titleLarge) },
                  modifier = Modifier.statusBarsPadding()
               )
            } else {
               Spacer(modifier = Modifier.statusBarsPadding())
            }

            AnimatedContent(
               targetState = isSearchExpanded,
               transitionSpec = {
                  fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
               },
               label = "HeaderTransition"
            )
            { expaned ->
               val expand = expaned
               // --- THE DYNAMIC TOP AREA SYSTEM ---

               // Render the search bar right beneath it with padding and round corners
//               CustomSearchHabitBar(
//                  query = searchQuery,
//                  isExpanded = false,
//                  onExpandedChange = { isSearchExpanded = it },
//                  onQueryChange = { habitViewModel.onSearchQueryChange(it) },
//                  modifier = Modifier
//                     .sharedBounds(
//                        rememberSharedContentState(key = "search_bar_bounds"),
//                        animatedVisibilityScope = this@AnimatedContent
//                     )
//                     .padding(horizontal = 16.dp, vertical = 8.dp)
//               ) {
//                  /* No dropdown content needed when unexpanded */
//               }
//
//               } else {
               // EXPANDED STATE: The TopAppBar is completely gone!
               // The search bar is rendered at the absolute top, stretching flush to act as the header.
               CustomSearchHabitBar(
                  query = searchQuery,
                  isExpanded = isSearchExpanded,
                  onExpandedChange = { isSearchExpanded = it },
                  onQueryChange = { habitViewModel.onSearchQueryChange(it) },
                  modifier = Modifier
                     .sharedBounds(
                        rememberSharedContentState(key = "search_bar_bounds"),
                        animatedVisibilityScope = this@AnimatedContent
                     )
                     .padding(horizontal = 16.dp, vertical = 8.dp)
                     .fillMaxWidth()
               )
               {
                  // Dropdown search results appear right here
                  if (filteredHabits.isEmpty() && searchQuery.isNotEmpty()) {
                     Text(
                        text = "No habits match your search.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                     )
                  } else {
                     LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredHabits, key = { it.id }) { habit ->
                           Row(
                              modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable {
                                    habitViewModel.onHabitChecked(habit.id)
                                    isSearchExpanded = false
                                    focusManager.clearFocus()
                                 }
                                 .padding(16.dp),
                              verticalAlignment = Alignment.CenterVertically
                           ) {
                              Icon(
                                 imageVector = Icons.Default.Bolt,
                                 contentDescription = null,
                                 modifier = Modifier.padding(end = 12.dp),
                                 tint = MaterialTheme.colorScheme.primary
                              )
                              Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
                           }
                        }
                     }
                  }
               }
            }
            // --- MAIN BODY CONTENT AREA BELOW THE SEARCH SECTOR ---
            Box(
               modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f)
            ) {
               // Layer 1: Regular habit elements list
               when (habitUiState) {
                  is UiState.Success -> HabitList(habits) { habitId ->
                     habitViewModel.onHabitChecked(habitId)
                  }

                  is UiState.Loading -> LoadingSpinner()
                  is UiState.Error -> ErrorScreen((habitUiState as UiState.Error).message)
               }

               // Layer 2: Blackout scrim to blur/hide the list background layout when searching
               if (isSearchExpanded) {
                  Box(
                     modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                           interactionSource = remember { MutableInteractionSource() },
                           indication = null
                        ) {
                           isSearchExpanded = false
                           habitViewModel.onSearchQueryChange("")
                           focusManager.clearFocus()
                        }
                  )
               }
            }
         }
      }
   }
}

@Composable
fun CustomSearchHabitBar(
   query: String,
   isExpanded: Boolean,
   onExpandedChange: (Boolean) -> Unit,
   onQueryChange: (String) -> Unit,
   modifier: Modifier = Modifier,
   content: @Composable ColumnScope.() -> Unit
) {
   val focusManager = LocalFocusManager.current
   val focusRequester = remember { FocusRequester() }

   if (isExpanded) {
      LaunchedEffect(Unit) {
         focusRequester.requestFocus()
      }
   }

   Column(
      modifier = modifier
         .animateContentSize()
         // Dynamically flattens layout shape corners down to 0 when acting as the TopAppBar header
         .clip(RoundedCornerShape( 28.dp))
         .background(MaterialTheme.colorScheme.surfaceContainerHigh)
   ) {
      Row(
         modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         if (isExpanded) {
            IconButton(onClick = {
               onExpandedChange(false)
               onQueryChange("")
               focusManager.clearFocus()
            }) {
               Icon(
                  imageVector = Icons.Default.ArrowBackIosNew,
                  contentDescription = "Back",
                  tint = MaterialTheme.colorScheme.onSurface
               )
            }
         } else {
            Icon(
               imageVector = Icons.Default.Search,
               contentDescription = "Search",
               modifier = Modifier.padding(start = 16.dp, end = 8.dp),
               tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
         }

         TextField(
            value = query,
            onValueChange = {
               onQueryChange(it)
               if (!isExpanded && it.isNotEmpty()) {
                  onExpandedChange(true)
               }
            },
            placeholder = { Text("Search habits...") },
            modifier = Modifier
               .weight(1f)
               .focusRequester(focusRequester)
               .onFocusChanged { focusState ->
                  if (focusState.isFocused && !isExpanded) {
                     onExpandedChange(true)
                  }
               },
            colors = TextFieldDefaults.colors(
               focusedContainerColor = Color.Transparent,
               unfocusedContainerColor = Color.Transparent,
               disabledContainerColor = Color.Transparent,
               focusedIndicatorColor = Color.Transparent,
               unfocusedIndicatorColor = Color.Transparent,

            ),
            singleLine = true
         )
      }

      if (isExpanded && query.isNotEmpty()) {
         HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
         Column(
            modifier = Modifier
               .fillMaxWidth()
               .heightIn(max = 400.dp) // The dropdown menu results expansion boundaries
         ) {
            content()
         }
      }
   }
}

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

@Preview(showBackground = true)
@Composable
fun SearchHabitBarPreview() {
//   SearchHabitBar("") { }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHabitBar(
   query: String,
   isExpanded: Boolean,
   onExpandedChange: (Boolean) -> Unit,
   onQueryChange: (String) -> Unit,
   content: @Composable (ColumnScope.() -> Unit)
) {
   val focusManager = LocalFocusManager.current

   SearchBar(
      expanded = isExpanded,
      onExpandedChange = onExpandedChange,
      modifier = Modifier
         .fillMaxWidth()
         .padding(16.dp),
      inputField = {
         SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
               onExpandedChange(false)
               focusManager.clearFocus()
            },
            expanded = isExpanded,
            onExpandedChange = onExpandedChange,
            placeholder = { Text("Search habits...") },
            leadingIcon = {
               if (isExpanded)
                  Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
               else Icon(Icons.Default.Search, contentDescription = null)
            }
         )
      },
      content = content
   )
}

@Composable
fun HabitList(habitList: List<HabitUiState>, onToggleHabitId: (habitId: Int) -> Unit) {
   val hazeState = rememberHazeState()
   LazyColumn(
      modifier = Modifier.hazeSource(state = hazeState)
   ) {
      items(habitList, key = { habit -> habit.id }) { habit ->
         HabitRow(
            habitName = habit.name,
            isToggled = habit.isDoneToday,
            iconName = habit.iconName,
            hazeState = hazeState,
            onToggle = { onToggleHabitId(habit.id) })

      }

   }

}

@Composable
fun HabitRow(
   habitName: String,
   isToggled: Boolean,
   iconName: String,
   hazeState: HazeState,
   onToggle: () -> Unit
) {
   Row(
      modifier = Modifier
         .padding(8.dp)
         .fillMaxWidth()
         .hazeEffect(state = hazeState) {
            blurEffect {
               blurRadius = 20.dp
               colorEffects = listOf(HazeColorEffect.tint(Color.Black.copy(alpha = 0.5f)))
            }
         }
         .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
         .clip(RoundedCornerShape(20.dp))
         .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = {}) {
         Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "iconName",
//            modifier = Modifier.fillMaxHeight(1f)
         )
      }

      Text(
         text = habitName, style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.weight(1f))
      IconButton(onClick = onToggle) {
         if (isToggled) {
            Icon(
               imageVector = Icons.Filled.CheckCircle,
               contentDescription = "Task Completed",
               tint = Color(0xFF4CAF50) // Material Green
            )
         } else {
            Icon(
               imageVector = Icons.Outlined.Circle,
               contentDescription = "Mark as Complete",
               tint = Color.Gray
            )
         }
      }


   }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
   Text(
      text = "Hello $name!", modifier = modifier
   )
}

