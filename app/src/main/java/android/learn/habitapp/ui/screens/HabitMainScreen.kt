package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.components.ColorFilterRow
import android.learn.habitapp.ui.components.ErrorScreen
import android.learn.habitapp.ui.components.HabitList
import android.learn.habitapp.ui.components.LoadingSpinner
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitMainScreen(
   habitViewModel: HabitViewModel,
   onCreateHabit: () -> Unit,
   onHabitClicked: (Int) -> Unit,
   screenTitle: String,
   onMenuClick: () -> Unit
) {

   val searchQuery by habitViewModel.searchQuery.collectAsState()
   val habitUiState by habitViewModel.habitUiState.collectAsStateWithLifecycle()
   val hasSeenSwipeHint by habitViewModel.hasSeenSwipeHint.collectAsStateWithLifecycle()

   val displayedHabitUiState by habitViewModel.displayedHabitUiState.collectAsStateWithLifecycle()

   val filteredHabitUiState by habitViewModel.filteredHabitUiState.collectAsStateWithLifecycle()
   val habits by remember {
      derivedStateOf {
         (habitUiState as? UiState.Success)?.habits ?: emptyList()
      }
   }

   val filteredHabits by remember {
      derivedStateOf {
         if (searchQuery.isEmpty()) emptyList()
         else when (filteredHabitUiState) {
            is UiState.Success -> (filteredHabitUiState as UiState.Success).habits
            else -> emptyList()
         }
      }
   }

   val scrollToHabitId by habitViewModel.scrollToHabitId.collectAsStateWithLifecycle()
   var isSearchExpanded by remember { mutableStateOf(false) }
   val focusManager = LocalFocusManager.current
   val animationDuration = 200

   val showFab = (habitUiState is UiState.Success) && !isSearchExpanded

   BackHandler(enabled = isSearchExpanded) {
      isSearchExpanded = false
      habitViewModel.onSearchQueryChange("")
      focusManager.clearFocus(force = true)
   }

   val snackbarHostState = remember { SnackbarHostState() }
   val scope = rememberCoroutineScope()

   val onArchiveHabit: (Int) -> Unit = { habitId ->
      habitViewModel.onArchiveHabit(habitId)
      scope.launch {
         val result = snackbarHostState.showSnackbar(
            message = "Habit archived", actionLabel = "Undo", duration = SnackbarDuration.Short
         )
         if (result == SnackbarResult.ActionPerformed) {
            delay(100)
            habitViewModel.onUndoArchive(habitId)
         }
      }
   }
   val availableColors by remember {
      derivedStateOf {
         habits.mapNotNull { it.color }.distinct()
      }
   }
   Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      floatingActionButton = { if (showFab) FloatingActionButton(onCreateHabit) }
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
            AnimatedTopAppBar(isSearchExpanded, animationDuration) {
               TopAppBar(
                  title = { Text(screenTitle, style = MaterialTheme.typography.titleLarge) },
                  navigationIcon = {
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
            SearchScreen(
               searchQuery,
               isSearchExpanded,
               onExpandedChange = { isSearchExpanded = it },
               animationDuration,
               filteredHabits,
               onQueryChange = { habitViewModel.onSearchQueryChange(it) },
               onHabitClicked = { searchHabitId ->
                  habitViewModel.requestScrollTo(searchHabitId)
                  onHabitClicked(searchHabitId)
                  isSearchExpanded = false
               },
            )
         }
         // MAIN BODY CONTENT AREA BELOW THE SEARCH SECTOR
         Box(
            modifier = Modifier
               .fillMaxWidth()
               .weight(1f)
         ) {
            MainBodyContent(
               displayedHabitUiState,
               scrollToHabitId,
               habitViewModel,
               hasSeenSwipeHint,
               onArchiveHabit,
               onHabitClicked,
               isSearchExpanded,
               availableColors = availableColors,
            ) {
               isSearchExpanded = false
               habitViewModel.onSearchQueryChange("")
               focusManager.clearFocus()
            }
         }
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

@Composable
internal fun MainBodyContent(
   habitUiState: UiState,
   scrollToHabitId: Int? = null,
   habitViewModel: HabitViewModel,
   hasSeenSwipeHint: Boolean = true,
   onArchiveHabit: (Int) -> Unit,
   onHabitClicked: (Int) -> Unit,
   isSearchExpanded: Boolean = false,
   availableColors: List<Int> = emptyList(),
   onClickScrimBackground: () -> Unit,
) {
   val colorFilter by habitViewModel.colorFilter.collectAsStateWithLifecycle()

   // Distinct colors pulled from the FULL unfiltered list, so chips don't disappear
   // once you've filtered down to one color

   Column(Modifier.fillMaxSize()) {
      AnimatedTopAppBar(isSearchExpanded) {
         ColorFilterRow(
            availableColors = availableColors,
            selectedColor = colorFilter,
            onColorSelected = { habitViewModel.onColorFilterChanged(it) }
         )
      }
      when (habitUiState) {
         is UiState.Success -> HabitList(
            habitList = habitUiState.habits,
            scrollToHabitId = scrollToHabitId,
            onScrollHandled = habitViewModel::onScrollHandled,
            hasSeenSwipeHint = hasSeenSwipeHint,
            onHintShown = habitViewModel::markSwipeHintSeen,
            onToggleHabitId = { habitId ->
               habitViewModel.onHabitChecked(habitId)
            },
            onArchiveHabit = onArchiveHabit,
            onHabitsReordered = { habitList -> habitViewModel.onHabitsReordered(habitList) }) { habitId ->
            onHabitClicked(
               habitId
            )
         }

         is UiState.Loading -> LoadingSpinner()

         is UiState.Error -> ErrorScreen(habitUiState.message)
      }
   }
   // Layer 2: Blackout scrim to blur/hide the list background layout when searching
   if (isSearchExpanded) {
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
               interactionSource = remember { MutableInteractionSource() }, indication = null
            ) {
               onClickScrimBackground()
            })
   }
}

@Composable
private fun FloatingActionButton(
   onCreateHabit: () -> Unit,
) {
   val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(label = "rounded corner") { enterExit ->
      when (enterExit) {
         EnterExitState.PreEnter -> 0.dp
         EnterExitState.Visible -> 20.dp
         EnterExitState.PostExit -> 20.dp
      }
   }

   with(LocalAnimatedVisibilityScope.current) {
      with(LocalSharedTransitionScope.current) {
         FloatingActionButton(
            onClick = onCreateHabit,
            shape = RoundedCornerShape(roundedCornerAnimation),
            modifier = Modifier
               .renderInSharedTransitionScopeOverlay(
                  zIndexInOverlay = 1f,
               )
               .animateEnterExit(
                  enter = fadeIn() + slideInVertically() { it },
                  exit = fadeOut() + slideOutVertically() { it })
               .sharedBounds(
                  sharedContentState = rememberSharedContentState(
                     HabitSharedElementKey(
                        habitId = -1, type = HabitSharedElementType.Bounds
                     )
                  ), animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

                  clipInOverlayDuringTransition = OverlayClip(
                     RoundedCornerShape(roundedCornerAnimation)
                  ), resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
               )

         ) {
            IconButton(onClick = onCreateHabit) {
               Icon(
                  tint = MaterialTheme.colorScheme.onPrimary,
                  imageVector = Icons.Outlined.Add,
                  contentDescription = "Add Icon",
                  modifier = Modifier
                     .fillMaxSize()
                     .padding(3.dp)
               )
            }
         }
      }
   }
}

@Composable
private fun SearchScreen(
   searchQuery: String,
   isSearchExpanded: Boolean,
   onExpandedChange: (Boolean) -> Unit,
   animationDuration: Int,
   filteredHabits: List<HabitUiState>,
   onHabitClicked: (Int) -> Unit,
   onQueryChange: (String) -> Unit
) {
   CustomSearchHabitBar(
      query = searchQuery,
      isExpanded = isSearchExpanded,
      onExpandedChange = onExpandedChange,
      onQueryChange = onQueryChange,
      modifier = Modifier
         .fillMaxWidth()
         .padding(horizontal = 16.dp, vertical = 8.dp)

         .animateContentSize(
            animationSpec = tween(
               durationMillis = animationDuration, easing = FastOutSlowInEasing
            )
         )
   ) {
      // Dropdown search results appear right here
      if (filteredHabits.isEmpty() && searchQuery.isNotEmpty()) {
         Text(
            text = "No habits match your search.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
         )
      } else {

         with(LocalSharedTransitionScope.current) {

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
               items(filteredHabits, key = { it.id }) { habit ->
                  val searchHabitId = habit.id
                  Row(
                     modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                           onHabitClicked(searchHabitId)
                        }
                        .sharedBounds(
                           sharedContentState = rememberSharedContentState(
                              key = HabitSharedElementKey(
                                 searchHabitId, type = HabitSharedElementType.Bounds
                              )
                           ),
                           animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                        )
                        .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
internal fun ColumnScope.AnimatedTopAppBar(
   isSearchExpanded: Boolean = false,
   animationDuration: Int = 200,
   measureTopAppBarHeight: (Int) -> Unit = {},
   content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
   val density = LocalDensity.current
   AnimatedVisibility(
      visible = !isSearchExpanded,
      enter = fadeIn(animationSpec = tween(animationDuration, easing = LinearOutSlowInEasing)) + scaleIn(
         initialScale = 0.92f, animationSpec = tween(animationDuration)
      ) + expandVertically(animationSpec = tween(animationDuration, easing = LinearOutSlowInEasing)),
      exit =
         // 1. Visually disappear FIRST (runs from 0ms to 150ms)
         fadeOut(
            animationSpec = tween(
               50, easing = FastOutLinearInEasing
            )
         ) + scaleOut(targetScale = 0.92f, animationSpec = tween(50)) +

                 // 2. Collapse the layout SECOND (waits for 150ms, then runs for 250ms)
                 shrinkVertically(
                    animationSpec = tween(
                       durationMillis = 150,
                       delayMillis = 50, // Matches the duration of the fadeOut!
                       easing = FastOutLinearInEasing
                    )
                 ),
      modifier = Modifier.onGloballyPositioned { coordinates ->
         val topAppBarHeightInt = with(density) { coordinates.size.height }
         measureTopAppBarHeight(topAppBarHeightInt)
      },
      content = content
   )
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
         .background(Color.Transparent)
      // Dynamically flattens layout shape corners down to 0 when acting as the TopAppBar header
   ) {
      Row(
         Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         Row(
            modifier = Modifier
               .padding(end = 10.dp)
               .weight(1f)
               .clip(RoundedCornerShape(28.dp))
               .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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
         AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
         ) {
            TextButton(onClick = {
               onExpandedChange(false)
               onQueryChange("")
               focusManager.clearFocus()
            }) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
         }
      }
      if (isExpanded && query.isNotEmpty()) {
//         HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
         Column(
            modifier = Modifier
               .fillMaxWidth()
               .clip(RoundedCornerShape(15.dp))
               .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = .5f))
               .wrapContentHeight()
//               .heightIn(max = 900.dp) // The dropdown menu results expansion boundaries


         ) {
            content()

         }

      }

   }
}
