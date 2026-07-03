package android.learn.habitapp

import android.learn.habitapp.data.emoji.HabitEmoji
import android.learn.habitapp.data.emoji.HabitEmojiData
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.repository.HabitEmojiRepository
import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.navigation.HabitList
import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.navigation.animatedComposable
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.theme.HabitAppTheme
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.ModeEditOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
   val habitViewModel: HabitViewModel by viewModels()


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      setContent {
         val coroutineScope = rememberCoroutineScope()
         val navController = rememberNavController()
         HabitAppTheme {

            val viewModel: HabitDetailViewModel = hiltViewModel()
            NavHost(
               navController = navController,
               startDestination = HabitList,

               ) {

               animatedComposable<HabitList> {

                  HabitMainScreen(
                     habitViewModel = habitViewModel,
                     onCreateHabit = {
                        viewModel.newHabit()
                        navController.navigate(HabitDetail())
                     }
                  ) { habitId ->
                     viewModel.loadHabit(habitId)
                     navController.navigate(HabitDetail(habitId))
                  }
               }

               animatedComposable<HabitDetail> { backStackEntry ->

                  val detailArgs = backStackEntry.toRoute<HabitDetail>()

                  HabitItemRoute(
                     habitId = detailArgs.habitId ?: -1,
                     viewModel = viewModel,
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
   }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEmojiPickerSheet(
   onEmojiSelected: (String) -> Unit,
   onDismissRequest: () -> Unit,
   onCloseSheet: () -> Unit,
   sheetState: SheetState = rememberModalBottomSheetState()
) {
   var searchQuery by rememberSaveable {
      mutableStateOf("")
   }

   var selectedCategory by rememberSaveable {
      mutableStateOf("Fitness")
   }
   val categories = HabitEmojiData.categories

   val currentCategory = categories.first {
      it.name == selectedCategory
   }

   val filteredEmojis: List<HabitEmoji> = remember(
      currentCategory, searchQuery
   ) {
      val items = HabitEmojiRepository.getByCategory(selectedCategory)
      HabitEmojiRepository.search(searchQuery, items)
   }

   val IosSnappySpring = spring<Dp>(
      dampingRatio = 0.75f,      // Bouncy enough to feel alive, tight enough to remain professional
      stiffness = 600f          // Rapid acceleration towards the target size
   )
   ModalBottomSheet(
      modifier = Modifier,
      onDismissRequest = onDismissRequest,
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
   ) {
      Column(
         modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
      ) {
         Text(
            text = "Choose Habit Icon",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
         )
         val focusManager = LocalFocusManager.current
         LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
         ) {
            item("search") {
               val screenWidth = LocalConfiguration.current.screenWidthDp.dp

               var isFocused by remember { mutableStateOf(false) }

               val animatedWidth by animateDpAsState(
                  targetValue = if (isFocused) screenWidth * 0.6f else 120.dp,
                  animationSpec = IosSnappySpring, // Smooth 300ms transition
                  label = "SearchBarWidth"
               )
               // Track the scaling font size starting from a base value (e.g., 14sp or 16sp)
               var currentFontSize by remember { mutableStateOf(14f) }
               // Reset text size when query clears or focus shifts to avoid getting permanently stuck small
               LaunchedEffect(searchQuery) {
                  if (searchQuery.isEmpty()) currentFontSize = 14f
               }

               BasicTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  modifier = Modifier
                     .height(40.dp)
                     .width(animatedWidth)
                     .onFocusChanged { isFocused = it.isFocused },
                  singleLine = true,
                  cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                  textStyle = TextStyle(
                     fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                     fontWeight = MaterialTheme.typography.displaySmall.fontWeight,
                     fontSize = currentFontSize.sp,                 // Dynamic Font Size
                     lineHeight = currentFontSize.sp,               // FORCE cursor to match text height
                     platformStyle = PlatformTextStyle(
                        includeFontPadding = false                 // Strips weird OS font padding misalignment
                     ),
                     color = MaterialTheme.colorScheme.onSurface
                  ),
                  decorationBox = { innerTextField ->
                     Row(
                        modifier = Modifier
                           .background(
                              color = if (isFocused) Color.Transparent else Color(0xFF615D6B),
                              shape = RoundedCornerShape(50)
                           )
                           .border(
                              width = if (isFocused) 1.dp else 0.dp,
                              color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                              shape = RoundedCornerShape(50)
                           )
                           .padding(horizontal = 12.dp)
                           .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                     ) {
                        Icon(
                           Icons.Default.Search,
                           contentDescription = null,
                           tint = if (isFocused) MaterialTheme.colorScheme.primary else Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                           modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart
                        ) {
                           if (searchQuery.isEmpty()) {
                              Text(
                                 text = "Search emojis", style = TextStyle(
                                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                                    fontWeight = MaterialTheme.typography.displaySmall.fontWeight,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = currentFontSize.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    color = Color.White.copy(alpha = 0.6f)
                                 )
                              )
                           }
                           innerTextField()
                        }
                     }
                  })
            }

            items(categories) { category ->
               FilterChip(selected = selectedCategory == category.name, onClick = {
                  focusManager.clearFocus()
                  selectedCategory = category.name
               }, label = { Text("${category.icon} ${category.name}") })
            }
         }
         LazyVerticalGrid(
            modifier = Modifier
               .height(300.dp)
               .clickable(
                  interactionSource = remember { MutableInteractionSource() }, indication = null
               ) {
                  focusManager.clearFocus()
               },
            columns = GridCells.Adaptive(56.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
         ) {

            items(
               filteredEmojis,
               key = { it.emoji + it.keywords.joinToString() },
            ) { emoji ->

               FilledTonalIconButton(
                  onClick = {
                     onEmojiSelected(emoji.emoji)
                     onCloseSheet()
                  }) {
                  Text(
                     emoji.emoji, fontSize = 24.sp
                  )
               }
            }
         }
      }
   }
}

@Composable
fun HabitItemRoute(
   viewModel: HabitDetailViewModel,
   habitId: Int,
   onBack: () -> Unit,
) {
   val uiState by viewModel.uiState.collectAsState()

   var hasNavigatedBack by remember { mutableStateOf(false) }
   Surface(
      Modifier.fillMaxSize()
   ) {
      Log.d("Tag", "wonder if it will work$habitId")

      HabitItemScreen(
         uiState,
         onNameChange = viewModel::onNameChanged,
         onEmojiChange = viewModel::onEmojiChanged,
         onReminderTimeChange = viewModel::onReminderTimeChanged,
         onFrequencyTypeChange = viewModel::onFrequencyTypeChanged,
         onCustomDaysChange = viewModel::onCustomDaysChanged,
         onTimesPerWeekChange = viewModel::onTimesPerWeekChanged,
         onArchiveHabit = { viewModel.archiveHabit { onBack() } },
         onSave = { viewModel.saveHabit { onBack() } },
      ) {
         if (!hasNavigatedBack) {
            hasNavigatedBack = true
            onBack()
         }
      }
   }


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitItemScreen(
   habit: HabitUiState,
   onNameChange: (String) -> Unit,
   onEmojiChange: (String) -> Unit,
   modifier: Modifier = Modifier,
   onReminderTimeChange: (LocalTime?) -> Unit,
   onFrequencyTypeChange: (FrequencyType) -> Unit,
   onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
   onTimesPerWeekChange: (Int) -> Unit,
   onArchiveHabit: () -> Unit,
   onSave: () -> Unit,
   onBack: () -> Unit,
) {
   val isNewItem = habit.id == -1
   val scope = rememberCoroutineScope()
   val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
   var showEmojiPicker by remember { mutableStateOf(false) }
   val focusManager = LocalFocusManager.current
   var clickTriggeredSheetOpen by remember { mutableStateOf(false) }
   val isKeyboardVisible = WindowInsets.isImeVisible
   val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(label = "rounded corner") { enterExit ->
      when (enterExit) {
         EnterExitState.PreEnter -> 20.dp
         EnterExitState.Visible -> 0.dp
         EnterExitState.PostExit -> 20.dp
      }

   }
   with(LocalSharedTransitionScope.current) {
      Column(
         Modifier
            .sharedBounds(
               sharedContentState = rememberSharedContentState(
                  key = HabitSharedElementKey(
                     habit.id,
                     type = HabitSharedElementType.Bounds
                  )
               ),
               animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

               clipInOverlayDuringTransition = OverlayClip(
                  RoundedCornerShape(roundedCornerAnimation)
               ),
               resizeMode = if (isNewItem) SharedTransitionScope.ResizeMode.RemeasureToBounds else SharedTransitionScope.ResizeMode.scaleToBounds()
            )
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
      ) {
         HabitItemTopAppBar(
            habit.id,
            habit.emoji, onBackPressed = {
               focusManager.clearFocus()
               onBack()
            }, onEditIcon = {
               focusManager.clearFocus()
               showEmojiPicker = true

            }, onSaveHabit = {
               focusManager.clearFocus()
               onSave() // If your onSave doesn't trigger navigation automatically, call safeNavigateBack() here too.
            },
            onArchiveHabit = onArchiveHabit
         )

         TextField(
            value = habit.name,
            onValueChange = onNameChange,
            textStyle = MaterialTheme.typography.titleLarge,
            placeholder = {
               Text(
                  "Title",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White.copy(alpha = .5f)
               )
            },
            modifier = Modifier.fillMaxWidth()
//            .focusRequester(focusRequester)
//            .onFocusChanged { focusState ->
//               if (focusState.isFocused && !isExpanded) {
//                  onExpandedChange(true)
//               }
//            }
            ,
            colors = TextFieldDefaults.colors(
               focusedContainerColor = Color.Transparent,
               unfocusedContainerColor = Color.Transparent,
               disabledContainerColor = Color.Transparent,
               focusedIndicatorColor = Color.Transparent,
               unfocusedIndicatorColor = Color.Transparent,


               ),
            singleLine = true
         )
         FrequencyPicker(
            frequencyType = habit.frequencyType,
            customDays = habit.customDays,
            timesPerWeek = habit.timesPerWeek,
            onFrequencyTypeChange = onFrequencyTypeChange,
            onCustomDaysChange = onCustomDaysChange,
            onTimesPerWeekChange = onTimesPerWeekChange
         )

         ReminderPicker(
            reminderTime = habit.reminderTime,
            onReminderChange = onReminderTimeChange
         )

      }

   }
//   LaunchedEffect(isKeyboardVisible, clickTriggeredSheetOpen) {
//         if (clickTriggeredSheetOpen && !isKeyboardVisible) {
//            // The keyboard is officially gone! Now it's safe to open the sheet
//            showEmojiPicker = true
//            clickTriggeredSheetOpen = false // Reset trigger
//         }
//      }
   if (showEmojiPicker) {

      HabitEmojiPickerSheet(
         sheetState = sheetState,
         onEmojiSelected = onEmojiChange,
         onDismissRequest = {
            // This handles hardware back buttons or tapping outside the sheet
            showEmojiPicker = false
         },
         onCloseSheet = {
            // This lets us trigger a beautiful closing animation from inside the sheet
            scope.launch {
               sheetState.hide() // 1. Animate down
            }.invokeOnCompletion {
               if (!sheetState.isVisible) {
                  showEmojiPicker = false // 2. Remove from UI once hidden
               }
            }
         })
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitItemTopAppBar(
   habitId: Int,
   selectedEmoji: String,
   onBackPressed: () -> Unit,
   onArchiveHabit: () -> Unit,
   onEditIcon: () -> Unit,
   onSaveHabit: () -> Unit

) {

   with(LocalSharedTransitionScope.current) {
      with(LocalAnimatedVisibilityScope.current) {
         TopAppBar(
            modifier = Modifier

               .animateEnterExit(
                  enter = fadeIn() + slideInVertically { fullHeight -> fullHeight }
               )
               .skipToLookaheadPosition()
               .padding(10.dp), title = {
               Text("Edit habit", Modifier)
            },
            navigationIcon = {
               IconButton(onClick = onBackPressed, modifier = Modifier) {
                  Icon(
                     imageVector = Icons.Outlined.ArrowBack,
                     contentDescription = stringResource(R.string.back_button),
                     modifier = Modifier.fillMaxSize(.6f)
                  )
               }
            }, actions = {
               Box(
                  modifier = Modifier, contentAlignment = Alignment.Center

               ) {
                  var emojiSize by remember { mutableStateOf(IntSize.Zero) }
                  val density = LocalDensity.current

                  val editButtonSize = remember(emojiSize, density) {
                     with(density) {
                        (emojiSize.width * 0.5f).toDp()
                     }
                  }
                  EmojiButton(
                     selectedEmoji,
                     modifier = Modifier
                        .onSizeChanged { emojiSize = it }
                        .sharedElement(
                           rememberSharedContentState(
                              key = HabitSharedElementKey(
                                 habitId,
                                 type = HabitSharedElementType.Emoji
                              )
                           ),
                           animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                        ),
                     onEditIcon,
                  )
                  IconButton(
                     modifier = Modifier
                        .size(editButtonSize)
                        .align(Alignment.BottomEnd)
                        .renderInSharedTransitionScopeOverlay(
                           zIndexInOverlay = 1f,
                        )
                        .animateEnterExit(
                           enter = fadeIn() + slideInVertically() { it },
                           exit = fadeOut() + slideOutVertically() { it }
                        ),
                     onClick = onEditIcon,
                     colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.onPrimary)
                  ) {
                     Icon(
                        imageVector = Icons.Outlined.ModeEditOutline,
                        contentDescription = "Edit Icon",
                        modifier = Modifier
                           .padding(3.dp)
                           .fillMaxSize()
                     )
                  }

               }
               Spacer(Modifier.width(30.dp))
               var showMenu by remember { mutableStateOf(false) }
               Box {
                  IconButton(onClick = { showMenu = true }) {
                     Icon(Icons.Default.MoreVert, contentDescription = "More options")
                  }
                  DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                     DropdownMenuItem(
                        text = { Text("Archive habit") },
                        leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                        onClick = {
                           showMenu = false
                           onArchiveHabit()
                        }
                     )
                  }
               }
               IconButton(onClick = onSaveHabit, modifier = Modifier) {
                  Icon(
                     imageVector = Icons.Default.Check,
                     contentDescription = "Save",
                     modifier = Modifier.fillMaxSize(.8f)
                  )
               }
            })
      }
   }
}

@Composable
private fun EmojiButton(
   selectedEmoji: String,
   modifier: Modifier = Modifier,
   onClickIcon: () -> Unit = {},
) {
   IconButton(
      onClick = onClickIcon,
      colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surface),
      modifier = Modifier.then(modifier)
   ) {
      Text(selectedEmoji, style = MaterialTheme.typography.headlineSmall)
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitMainScreen(
   habitViewModel: HabitViewModel,
   onCreateHabit: () -> Unit,
   onHabitClicked: (Int) -> Unit
) {

   val searchQuery by habitViewModel.searchQuery.collectAsState()
   val habitUiState by habitViewModel.habitUiState.collectAsStateWithLifecycle()
   val hasSeenSwipeHint by habitViewModel.hasSeenSwipeHint.collectAsStateWithLifecycle()

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

   var isSearchExpanded by remember { mutableStateOf(false) }
   val focusManager = LocalFocusManager.current
   val animationDuration = 200

   val showFab = (habitUiState is UiState.Success) && !isSearchExpanded

   BackHandler(enabled = isSearchExpanded) {
      isSearchExpanded = false
      habitViewModel.onSearchQueryChange("")
      focusManager.clearFocus(force = true)
   }

   val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(label = "rounded corner") { enterExit ->
      when (enterExit) {
         EnterExitState.PreEnter -> 0.dp
         EnterExitState.Visible -> 20.dp
         EnterExitState.PostExit -> 20.dp
      }

   }
   // Leave Scaffold's topBar blank so we can dynamically control the top area ourselves
   Scaffold(
      floatingActionButton = {
         if (showFab) {
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
                           exit = fadeOut() + slideOutVertically() { it }
                        )
                        .sharedBounds(
                           sharedContentState = rememberSharedContentState(
                              HabitSharedElementKey(
                                 habitId = -1,
                                 type = HabitSharedElementType.Bounds
                              )
                           ),
                           animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

                           clipInOverlayDuringTransition = OverlayClip(
                              RoundedCornerShape(roundedCornerAnimation)
                           ),
                           resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
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
      }) { innerPadding ->
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
            AnimatedTopAppBar(isSearchExpanded, animationDuration)



            CustomSearchHabitBar(
               query = searchQuery,
               isExpanded = isSearchExpanded,
               onExpandedChange = { isSearchExpanded = it },
               onQueryChange = { habitViewModel.onSearchQueryChange(it) },
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
                  with(LocalSharedTransitionScope.current)
                  {
                     LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredHabits, key = { it.id }) { habit ->
                           Row(
                              modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable {
                                    onHabitClicked(habit.id)
//                                 isSearchExpanded = false
//                                 habitViewModel.onSearchQueryChange("")
//                                 focusManager.clearFocus()
                                 }
                                 .sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                       key = HabitSharedElementKey(
                                          habit.id,
                                          type = HabitSharedElementType.Bounds
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
         }      // --- MAIN BODY CONTENT AREA BELOW THE SEARCH SECTOR ---
         Box(
            modifier = Modifier
               .fillMaxWidth()
               .weight(1f)
         ) {
            if (!isSearchExpanded) {  // Layer 1: Regular habit elements list
               when (habitUiState) {
                  is UiState.Success -> HabitList(
                     habits,
                     onToggleHabitId = { habitId ->
                        habitViewModel.onHabitChecked(habitId)
                     },
                     hasSeenSwipeHint = hasSeenSwipeHint,
                     onHintShown = habitViewModel::markSwipeHintSeen,
                     onArchiveHabit = { habitId ->
                        habitViewModel.onArchiveHabit(habitId)
                     },
                     onHabitsReordered = { habitList -> habitViewModel.onHabitsReordered(habitList) }
                  ) { habitId ->
                     onHabitClicked(
                        habitId
                     )
                  }

                  is UiState.Loading -> LoadingSpinner()

                  is UiState.Error -> ErrorScreen((habitUiState as UiState.Error).message)
               }
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
                     })
            }
         }
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.AnimatedTopAppBar(
   isSearchExpanded: Boolean, animationDuration: Int, measureTopAppBarHeight: (Int) -> Unit = {}
) {
   val density = LocalDensity.current
   AnimatedVisibility(
      visible = !isSearchExpanded,
      enter = fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) + scaleIn(
         initialScale = 0.92f, animationSpec = tween(300)
      ) + expandVertically(animationSpec = tween(300, easing = LinearOutSlowInEasing)),
      exit =
         // 1. Visually disappear FIRST (runs from 0ms to 150ms)
         fadeOut(
            animationSpec = tween(
               150, easing = FastOutLinearInEasing
            )
         ) + scaleOut(targetScale = 0.92f, animationSpec = tween(150)) +

                 // 2. Collapse the layout SECOND (waits for 150ms, then runs for 250ms)
                 shrinkVertically(
                    animationSpec = tween(
                       durationMillis = 250,
                       delayMillis = 150, // Matches the duration of the fadeOut!
                       easing = FastOutLinearInEasing
                    )
                 ),
      modifier = Modifier.onGloballyPositioned { coordinates ->
         val topAppBarHeightInt = with(density) { coordinates.size.height }
         measureTopAppBarHeight(topAppBarHeightInt)
      }

   ) {
      // NORMAL STATE: Render the real top app bar
      TopAppBar(
         title = { Text("My Habits", style = MaterialTheme.typography.titleLarge) },
         modifier = Modifier
            .statusBarsPadding()
            .animateContentSize(animationSpec = tween(5000))
      )
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

//@Preview(showBackground = true)
//@Composable
//fun SearchHabitBarPreview() {
////   SearchHabitBar("") { }
//}

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
               if (isExpanded) Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
               else Icon(Icons.Default.Search, contentDescription = null)
            })
      },
      content = content
   )
}

@Composable
fun HabitList(
   habitList: List<HabitUiState>,
   hasSeenSwipeHint: Boolean,
   onHintShown: () -> Unit,
   onToggleHabitId: (habitId: Int) -> Unit,
   onArchiveHabit: (Int) -> Unit,
   onHabitsReordered: (List<Int>) -> Unit,
   onHabitItemClick: (Int) -> Unit,
) {
   // Local mutable copy so drag feels instant, before the DB round-trip confirms
   var localOrder by remember(habitList) { mutableStateOf(habitList) }
   val lazyListState = rememberLazyListState()

   val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
      localOrder = localOrder.toMutableList().apply {
         add(to.index, removeAt(from.index))
      }
   }

   LazyColumn(state = lazyListState) {
      items(localOrder, key = { habit -> habit.id }) { habit ->
         ReorderableItem(reorderableState, key = habit.id) { isDragging ->
            val isFirst = localOrder.firstOrNull()?.id == habit.id

            val rowContent: @Composable (Modifier) -> Unit = { rowModifier ->
               ArchivableHabitRow(
                  habitId = habit.id,
                  habitName = habit.name,
                  isToggled = habit.isDoneToday,
                  emoji = habit.emoji,
                  onToggle = { onToggleHabitId(habit.id) },
                  onClickHabit = { onHabitItemClick(habit.id) },
                  onArchive = { onArchiveHabit(habit.id) },
                  modifier = rowModifier
                     .longPressDraggableHandle(
                        onDragStopped = {
                           onHabitsReordered(localOrder.map { it.id })
                        }
                     )
                     .graphicsLayer {
                        scaleX = if (isDragging) 1.03f else 1f
                        scaleY = if (isDragging) 1.03f else 1f
                        shadowElevation = if (isDragging) 8f else 0f
                     }
               )
            }

            if (isFirst) {
               SwipeHintOverlay(
                  hasSeenHint = hasSeenSwipeHint,
                  onHintShown = onHintShown
               ) { hintModifier -> rowContent(hintModifier) }
            } else {
               rowContent(Modifier)
            }
         }
      }
   }
}

@Composable
fun SwipeHintOverlay(
   hasSeenHint: Boolean,
   onHintShown: () -> Unit,
   content: @Composable (Modifier) -> Unit
) {
   val offsetX = remember { Animatable(0f) }

   LaunchedEffect(hasSeenHint) {
      if (!hasSeenHint) {
         delay(600) // let the list settle in first
         offsetX.animateTo(-60f, animationSpec = tween(400, easing = FastOutSlowInEasing))
         offsetX.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
         onHintShown()
      }
   }

   content(Modifier.offset { IntOffset(offsetX.value.toInt(), 0) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivableHabitRow(
   habitId: Int,
   habitName: String,
   isToggled: Boolean,
   emoji: String,
   onToggle: () -> Unit,
   onClickHabit: () -> Unit,
   onArchive: () -> Unit,
   modifier: Modifier = Modifier
) {
   val dismissState = rememberSwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      confirmValueChange = { true }, // no side effects here anymore — just allow the state change
      positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
   )

   LaunchedEffect(dismissState.settledValue) {
      if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
         onArchive()
      }
   }
   val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(label = "rounded corner") { enterExit ->
      when (enterExit) {
         EnterExitState.PreEnter -> 0.dp
         EnterExitState.Visible -> 20.dp
         EnterExitState.PostExit -> 20.dp
      }

   }
   SwipeToDismissBox(
      state = dismissState,
      modifier = modifier,
      enableDismissFromStartToEnd = false, // only right-to-left swipe archives
      backgroundContent = {
         val density = LocalDensity.current
         // Offset is negative when swiping end-to-start (right to left)
         val offsetPx = remember {
            derivedStateOf {
               try {
                  dismissState.requireOffset()
               } catch (e: IllegalStateException) {
                  0f
               }
            }
         }
         val revealedWidth = with(density) { abs(offsetPx.value).toDp() }
         Box(
            modifier = Modifier
               .fillMaxSize(), // outer box still fills, but is fully transparent
            contentAlignment = Alignment.CenterEnd
         ) {
            Row(
               modifier = Modifier
                  .width(revealedWidth) // ← only the revealed sliver gets colored
                  .padding(vertical = 8.dp)
                  .fillMaxSize()
                  .background(
                     MaterialTheme.colorScheme.errorContainer,
                     RoundedCornerShape(20.dp)
                  )
                  .clip(RoundedCornerShape(20.dp))
                  ,

               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.Center
            ) {
               Icon(
                  imageVector = Icons.Outlined.Archive,
                  contentDescription = "Archive",
                  tint = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.graphicsLayer {
                     alpha = dismissState.progress
                  }
               )
            }
         }
      }
   ) {

      with(LocalSharedTransitionScope.current) {
         HabitRow(
            habitId = habitId,
            habitName = habitName,
            isToggled = isToggled,
            emoji = emoji,
            onToggle = onToggle,
            onClickHabit = onClickHabit,
            modifier = Modifier.sharedBounds(
               sharedContentState = rememberSharedContentState(
                  key = HabitSharedElementKey(
                     habitId,
                     type = HabitSharedElementType.Bounds
                  )
               ),
               animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

               clipInOverlayDuringTransition = OverlayClip(
                  RoundedCornerShape(roundedCornerAnimation)
               ),
            )
         )
      }
   }
}

@Composable
fun HabitRow(
   habitId: Int,
   habitName: String,
   isToggled: Boolean,
   emoji: String,
//   hazeState: HazeState,
   onToggle: () -> Unit = {},
   modifier: Modifier = Modifier,
   onClickHabit: () -> Unit = {},
) {
   with(LocalSharedTransitionScope.current) {
      Row(
         modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
//         .hazeEffect(state = hazeState) {
//            blurEffect {
//               blurRadius = 20.dp
//               colorEffects = listOf(HazeColorEffect.tint(Color.Black.copy(alpha = 0.5f)))
//            }
//         }
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClickHabit() }
            .then(modifier)
            .padding(16.dp),
         verticalAlignment = Alignment.CenterVertically) {
         EmojiButton(
            selectedEmoji = emoji,
            onClickIcon = onClickHabit,
            modifier = Modifier
               .sharedElement(
                  rememberSharedContentState(
                     key = HabitSharedElementKey(
                        habitId, type = HabitSharedElementType.Emoji
                     )
                  ),
                  animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
               )
               .skipToLookaheadSize()
         )
         Spacer(modifier = Modifier.weight(.1f))
         Text(
            text = habitName, style = MaterialTheme.typography.titleMedium
         )
         Spacer(modifier = Modifier.weight(.9f))
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPicker(
   reminderTime: LocalTime?,
   onReminderChange: (LocalTime?) -> Unit,
   modifier: Modifier = Modifier
) {
   var showPicker by remember { mutableStateOf(false) }

   Row(
      modifier = modifier
         .fillMaxWidth()
         .padding(horizontal = 16.dp, vertical = 8.dp)
         .clickable { showPicker = true },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
   ) {
      Column {
         Text(
            "Reminder",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
         )
         Text(
            reminderTime?.let {
               it.format(DateTimeFormatter.ofPattern("h:mm a"))
            } ?: "Off",
            style = MaterialTheme.typography.bodyLarge
         )
      }
      if (reminderTime != null) {
         IconButton(onClick = { onReminderChange(null) }) {
            Icon(Icons.Default.Close, contentDescription = "Remove reminder")
         }
      }
   }

   if (showPicker) {
      val initialTime = reminderTime ?: LocalTime.of(9, 0)
      val timePickerState = rememberTimePickerState(
         initialHour = initialTime.hour,
         initialMinute = initialTime.minute,
         is24Hour = false
      )

      Dialog(onDismissRequest = { showPicker = false }) {
         Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
         ) {
            Column(
               modifier = Modifier.padding(24.dp),
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               TimePicker(state = timePickerState)
               Row(
                  modifier = Modifier
                     .fillMaxWidth()
                     .padding(top = 16.dp),
                  horizontalArrangement = Arrangement.End
               ) {
                  TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                  TextButton(onClick = {
                     onReminderChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                     showPicker = false
                  }) { Text("Set") }
               }
            }
         }
      }
   }
}

@Composable
fun FrequencyPicker(
   frequencyType: FrequencyType,
   customDays: Set<DayOfWeek>,
   timesPerWeek: Int?,
   onFrequencyTypeChange: (FrequencyType) -> Unit,
   onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
   onTimesPerWeekChange: (Int) -> Unit,
   modifier: Modifier = Modifier
) {
   Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      Text(
         "Frequency",
         style = MaterialTheme.typography.labelLarge,
         color = MaterialTheme.colorScheme.onSurfaceVariant,
         modifier = Modifier.padding(bottom = 8.dp)
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
         FrequencyType.entries.forEach { type ->
            FilterChip(
               selected = frequencyType == type,
               onClick = { onFrequencyTypeChange(type) },
               label = { Text(type.label()) }
            )
         }
      }

      AnimatedVisibility(visible = frequencyType == FrequencyType.SPECIFIC_DAYS) {
         DayOfWeekSelector(
            selectedDays = customDays,
            onDayToggle = { day ->
               onCustomDaysChange(
                  if (day in customDays) customDays - day else customDays + day
               )
            },
            modifier = Modifier.padding(top = 12.dp)
         )
      }

      AnimatedVisibility(visible = frequencyType == FrequencyType.TIMES_PER_WEEK) {
         TimesPerWeekStepper(
            value = timesPerWeek ?: 1,
            onValueChange = onTimesPerWeekChange,
            modifier = Modifier.padding(top = 12.dp)
         )
      }
   }
}

private fun FrequencyType.label(): String = when (this) {
   FrequencyType.DAILY -> "Daily"
   FrequencyType.SPECIFIC_DAYS -> "Specific days"
   FrequencyType.TIMES_PER_WEEK -> "X per week"
}

@Composable
private fun DayOfWeekSelector(

   selectedDays: Set<DayOfWeek>,
   onDayToggle: (DayOfWeek) -> Unit,
   modifier: Modifier = Modifier
) {
   Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
   ) {
      DayOfWeek.entries.forEach { day ->
         val selected = day in selectedDays
         Surface(
            onClick = { onDayToggle(day) },
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(40.dp)
         ) {
            Box(contentAlignment = Alignment.Center) {
               Text(
                  day.name.take(1), // M T W T F S S
                  color = if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.labelMedium
               )
            }
         }
      }
   }
}

@Composable
private fun TimesPerWeekStepper(
   value: Int,
   onValueChange: (Int) -> Unit,
   modifier: Modifier = Modifier
) {
   Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
   ) {
      IconButton(onClick = { if (value > 1) onValueChange(value - 1) }) {
         Icon(Icons.Default.Remove, contentDescription = "Decrease")
      }
      Text(
         "$value ${if (value == 1) "time" else "times"} / week",
         style = MaterialTheme.typography.bodyLarge
      )
      IconButton(onClick = { if (value < 7) onValueChange(value + 1) }) {
         Icon(Icons.Default.Add, contentDescription = "Increase")
      }
   }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
   Text(
      text = "Hello $name!", modifier = modifier
   )
}