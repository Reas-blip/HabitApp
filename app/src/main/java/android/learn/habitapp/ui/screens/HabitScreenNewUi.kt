package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.R
import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.components.ColorFilterRow
import android.learn.habitapp.ui.components.ErrorScreen
import android.learn.habitapp.ui.components.LoadingSpinner
import android.learn.habitapp.ui.theme.HabitColors
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.DayOfWeek
import kotlin.math.abs

@Composable
fun HabitOverview(

   todayStats: OverviewStats,
   weeklyCompletionByDay: Map<DayOfWeek, Boolean?>,
   modifier: Modifier = Modifier,
) {
   Column(
      modifier = modifier
         .fillMaxWidth()
         .shadow(
            elevation = 4.dp, // Adjust this value to make the shadow larger/smaller
            shape = RoundedCornerShape(16.dp),
            clip = true // This handles the clipping, so you can remove your .clip() modifier
         )
         .background(color = Color.White)
         .padding(all = 16.dp.scaledWidth())
   ) {
      Text(
         "Daily Progress",
         style = MaterialTheme.typography.labelMedium.copy(color = HabitColors.TextSecondary),
         modifier = Modifier.align(alignment = Alignment.Start)
      )
      Row(
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier.padding(top = 4.dp.scaledHeight())
      ) {
         Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
         ) {
            Text(
               text = "${todayStats.successRatePercent}%",
               style = MaterialTheme.typography.headlineMedium.copy(
                  fontSize = 30.sp,
                  fontWeight = FontWeight(700),
                  color = Color(0xFF101828),
               )
            )
            Text(
               text = "Great job! 😊",
               style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight(500),

               )
            )
         }
         OverViewProgressBar(
            titleText = "${todayStats.grandTotalCompleted}/${todayStats.grandTotalDue}",
            titleStyle = MaterialTheme.typography.bodyLarge.copy(
               fontWeight = FontWeight(700),
               color = Color(0xFF1E2939),
            ),
            subtitleText = "Done",
            subtitleStyle = TextStyle(
               fontSize = 9.sp,
               lineHeight = 13.5.sp,
               fontWeight = FontWeight(400),
               color = HabitColors.TextSecondary,
            ),
            progress = if (todayStats.grandTotalDue == 0) 0f else todayStats.grandTotalCompleted / todayStats.grandTotalDue.toFloat(),
            size = 80.dp
         )

      }

      Row(
         modifier = modifier
            .padding(top = 10.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
      ) {

         val selectedDays = weeklyCompletionByDay.filter { entries ->
            entries.value == true
         }.keys
         DayOfWeek.entries.forEach { day ->
            val selected = day in selectedDays
            Surface(
               shape = RoundedCornerShape(50f),
               color = if (selected) HabitColors.PrimaryLight
               else Color(0x68989898),
               modifier = Modifier
                  .width(42.dp.scaledWidth())
                  .height(18.dp.scaledHeight())
            ) {
               Box(contentAlignment = Alignment.Center) {
                  Text(
                     day.name.take(1), // M T W T F S S
                     color = if (selected) HabitColors.PrimaryDark
                     else  HabitColors.Surface,
                     style = MaterialTheme.typography.labelMedium
                  )
               }
            }
         }
      }
   }
}

@Composable
fun OverViewProgressBar(
   titleText: String,
   titleStyle: TextStyle,
   subtitleText: String,
   subtitleStyle: TextStyle,
   progress: Float,
   modifier: Modifier = Modifier,
   size: Dp = 80.dp,
) {
   val animatedProgress by animateFloatAsState(
      targetValue = progress,
      // You can use the standard progress animation spec or a custom tween(500)
      animationSpec = spring(
      ),
      label = "OverviewProgressAnimation"
   )
   Box(
      contentAlignment = Alignment.Center,
      modifier = modifier
         .size(size.scaledWidth()) // Total size of the widget
         .padding(4.dp.scaledWidth())
   ) {

      CircularProgressIndicator(
         progress = { animatedProgress },
         modifier = Modifier.fillMaxSize(),
         color = HabitColors.Accent,
         trackColor = Color(0xFFE8F5E9),
         strokeWidth = 10.dp,
         gapSize = (-10).dp
      )
      Column(
         verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
         horizontalAlignment = Alignment.CenterHorizontally,
         modifier = Modifier

      ) {
         Text(
            text = titleText,
            style = titleStyle
         )
         Text(
            text = subtitleText,
            style = subtitleStyle
         )
      }
   }

}


@Composable
fun HabitIconBadge(
   icon: Painter,
   backgroundColor: Color,
   modifier: Modifier = Modifier,
   iconModifier: Modifier = Modifier,
   iconTint: Color = Color.White,
   size: Dp = 40.dp,
   elevation: Dp = 2.dp,
   shape: RoundedCornerShape = CircleShape,
   onClickIcon: () -> Unit = {}

) {
   Box(
      modifier = Modifier
         .size(size)
         .shadow(
            elevation = elevation, // Adjust this value to make the shadow larger/smaller
            shape = shape,
            clip = true // This handles the clipping, so you can remove your .clip() modifier
         )
         .background(color = backgroundColor)
         .clickable(onClick = onClickIcon)
         .then(modifier),
      contentAlignment = Alignment.Center
   ) {
      Icon(
         painter = icon,
         contentDescription = null,
         tint = iconTint,
         modifier = Modifier.size(size * 0.5f).then(iconModifier) // icon ~50% of circle
      )
   }
}


@Composable
fun SelectedIcon(
   icon: Painter,
   backgroundColor: Color,
   iconTint: Color = Color.White,
   size: Dp = 28.dp,
   modifier: Modifier = Modifier,
   isSelected: Boolean = false,
   onToggle: () -> Unit = {},
) {
   val borderModifier: Modifier = if (isSelected) {
      Modifier
   } else {
      Modifier.border(
         width = 2.dp,
         color = Color(0xFFE5E7EB),
         shape = CircleShape
      )
   }
   Box(
      modifier = modifier
         .size(size)
         .background(
            color = if (isSelected) {
               backgroundColor
            } else {
               Color.Transparent
            }, shape = CircleShape
         )
         .clickable(
            onClick = onToggle,
            indication = null,
            interactionSource = null
         )
         .clip(CircleShape)
         .then(borderModifier),
      contentAlignment = Alignment.Center
   ) {
      if (isSelected) {
         Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f) // icon ~50% of circle
         )

      }
   }
}

@Preview(showBackground = true)
@Composable
fun TestPreview() {

   val screenSize = LocalConfiguration.current
   val width = screenSize.screenWidthDp.dp
   val height = screenSize.screenHeightDp.dp
   Row(
      modifier = Modifier.wrapContentHeight(),
      horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
      verticalAlignment = Alignment.CenterVertically,
   ) {
      val iconHeight = height * .015f
      val contentColor = HabitColors.Surface
      Icon(
         imageVector = Icons.Outlined.Add,
         modifier = Modifier.size(iconHeight),
         contentDescription = "Add Habit Button",
         tint = contentColor
      )
      Text("Add Habits", color = contentColor)
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xF0F7F1FF)
@Composable
fun TopBarPreview() {
   TopAppBar(
      title = { Text("Habits") },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      actions = {
         HabitIconBadge(
            icon = painterResource(R.drawable.search_icon),
            backgroundColor =  HabitColors.Surface,
            iconTint = Color(0xFF4A5565),
            elevation = 1.dp

         ) {}

         Spacer(Modifier.size(10.dp))
         HabitIconBadge(
            icon = rememberVectorPainter(Icons.Filled.MoreVert),
            backgroundColor =  HabitColors.Surface,
            iconTint = Color(0xFF4A5565),
            elevation = 1.dp
         ) {}
      }

   )
}

@Composable
fun HabitScreenTopAppBar(onSearchClick: () -> Unit, onMoreClick: () -> Unit) {
   Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .background(Color.Transparent)
         .fillMaxWidth()
   ) {
      Text(
         text = "Habits",
         style = MaterialTheme.typography.titleLarge.copy(
            lineHeight = 28.sp,
//                  fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(700),
            color = Color(0xFF101828),
         )
      )

      with(LocalSharedTransitionScope.current) {
         Row(Modifier.background(Color.Transparent)) {
            HabitIconBadge(
               icon = painterResource(R.drawable.search_icon),
               backgroundColor =  HabitColors.Surface,
               iconTint = Color(0xFF4A5565),
               size = 40.dp.scaledWidth(),
               elevation = 5.dp,
               modifier = Modifier.sharedBounds(
                  sharedContentState = rememberSharedContentState(
                     "search_bar"
                  ), animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

                  resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
               ),
               iconModifier = Modifier.sharedElement(
                  sharedContentState = rememberSharedContentState(
                     "search_bar_icon"
                  ), animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
               )
            ) { onSearchClick() }
            Spacer(Modifier.size(10.dp))
            HabitIconBadge(
               icon = rememberVectorPainter(Icons.Filled.MoreVert),
               backgroundColor =  HabitColors.Surface,
               iconTint = Color(0xFF4A5565),
               elevation = 5.dp,
               onClickIcon = onMoreClick
            )
         }
      }

   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
   habitViewModel: HabitViewModel,
   onAddHabit: () -> Unit,
   onHabitClicked: (Int) -> Unit,
   onSearchClick: () -> Unit,
   onNavigateToArchive: () -> Unit,
) {
   val habitUiState by habitViewModel.displayedHabitUiState.collectAsStateWithLifecycle()
   val statsForToday by habitViewModel.habitScreenDayStats.collectAsStateWithLifecycle()
   val weeklyCompletionByDay by habitViewModel.weeklyCompletionByDay.collectAsStateWithLifecycle()

   var showOptionsDialog by remember { mutableStateOf(false) }

   Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .background(Color.Transparent)
         .padding(vertical = 8.dp.scaledHeight(), horizontal = 16.dp.scaledWidth())
   ) {

      HabitScreenTopAppBar(
         onSearchClick = onSearchClick,
         onMoreClick = { showOptionsDialog = true },
      )
      HabitOverview(
         todayStats = statsForToday,
         weeklyCompletionByDay = weeklyCompletionByDay
      )

      when (val state = habitUiState) {
         is UiState.Success -> {
            HabitListNewUi(
               habitList = state.habits,
               onToggleHabitId = { habitId ->
                  habitViewModel.onHabitChecked(habitId)
               },
               onArchiveHabit = habitViewModel::onArchiveHabit,
               onHabitsReordered = habitViewModel::onHabitsReordered,
               onAddHabit = onAddHabit,
               onHabitItemClick = onHabitClicked,
               habitListTitle = "Today's Habits",
            )

         }

         is UiState.Loading -> LoadingSpinner()

         is UiState.Error -> ErrorScreen(state.message)
      }
   }

   if (showOptionsDialog) {
      val fullHabitState by habitViewModel.habitUiState.collectAsStateWithLifecycle()
      val colorFilter by habitViewModel.colorFilter.collectAsStateWithLifecycle()
      val availableColors = remember(fullHabitState) {
         (fullHabitState as? UiState.Success)?.habits
            ?.mapNotNull { it.color }
            ?.distinct()
            ?: emptyList()
      }

      HabitScreenOptionsDialog(
         availableColors = availableColors,
         selectedColor = colorFilter,
         onColorSelected = habitViewModel::onColorFilterChanged,
         onNavigateToArchive = {
            showOptionsDialog = false
            onNavigateToArchive()
         },
         onDismiss = { showOptionsDialog = false }
      )
   }
}

@Composable
private fun HabitScreenOptionsDialog(
   availableColors: List<Int>,
   selectedColor: Int?,
   onColorSelected: (Int?) -> Unit,
   onNavigateToArchive: () -> Unit,
   onDismiss: () -> Unit,
) {
   Dialog(onDismissRequest = onDismiss) {
      Surface(
         shape = RoundedCornerShape(20.dp),
         color = HabitColors.Surface,
         modifier = Modifier.fillMaxWidth()
      ) {
         Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
               "Options",
               style = MaterialTheme.typography.titleMedium.copy(color = HabitColors.TextPrimary),
               modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            Row(
               modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onNavigateToArchive() }
                  .padding(horizontal = 20.dp, vertical = 14.dp),
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
               Icon(
                  Icons.Outlined.Archive,
                  contentDescription = null,
                  tint = HabitColors.PrimaryDark
               )
               Text(
                  "Archived Habits",
                  color = HabitColors.TextPrimary,
                  style = MaterialTheme.typography.bodyLarge
               )
            }

            HorizontalDivider(color = HabitColors.SoftBorder)

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
               Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
               ) {
                  Row(
                     horizontalArrangement = Arrangement.spacedBy(14.dp),
                     verticalAlignment = Alignment.CenterVertically
                  ) {
                     Icon(
                        Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = HabitColors.PrimaryDark
                     )
                     Text(
                        "Filter by Color",
                        color = HabitColors.TextPrimary,
                        style = MaterialTheme.typography.bodyLarge
                     )
                  }
                  if (selectedColor != null) {
                     Text(
                        "Clear",
                        color = HabitColors.Danger,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { onColorSelected(null) }
                     )
                  }
               }

               if (availableColors.isNotEmpty()) {
                  Spacer(Modifier.height(10.dp))
                  ColorFilterRow(
                     availableColors = availableColors,
                     selectedColor = selectedColor,
                     onColorSelected = onColorSelected,
                     modifier = Modifier.padding(horizontal = 0.dp)
                  )
               } else {
                  Spacer(Modifier.height(6.dp))
                  Text(
                     "Add a color to a habit to filter by it.",
                     color = HabitColors.TextSecondary,
                     style = MaterialTheme.typography.bodySmall
                  )
               }
            }
         }
      }
   }
}

//
//@Preview(showBackground = true)
//@Composable
//fun HabitListPreview() {
//   HabitList()
//}

@Composable
fun DailyProgress() {

}

@Composable
fun HabitListNewUi(
   habitList: List<HabitUiState>,
   onToggleHabitId: (habitId: Int) -> Unit,
   onArchiveHabit: (Int) -> Unit,
   onHabitsReordered: (List<Int>) -> Unit,
   onHabitItemClick: (Int) -> Unit,
   habitListTitle: String,
   onAddHabit: () -> Unit,
) {

   Column(
      modifier = Modifier
         .fillMaxWidth()
         .fillMaxHeight()
         .shadow(
            elevation = 1.dp, // Adjust this value to make the shadow larger/smaller
            shape = RoundedCornerShape(16.dp),
            clip = true // This handles the clipping, so you can remove your .clip() modifier
         )
         .background(
            color =  HabitColors.Surface,
         )
   ) {
      Row(
         modifier = Modifier
            .fillMaxWidth()
            .height(44.dp.scaledHeight())
            .padding(
               start = 16.dp.scaledWidth(),
               top = 16.dp.scaledHeight(),
               end = 16.dp.scaledWidth(),
               bottom = 8.dp.scaledHeight()
            ),
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.CenterVertically,
      ) {

         Text(
            text = habitListTitle,
            style = MaterialTheme.typography.titleMedium.copy(
               color = Color(0xFF1E2938),
            )
         )
         Row(

            modifier = Modifier
               .fillMaxHeight()
               .clickable(onClick = onAddHabit),
            horizontalArrangement = Arrangement.spacedBy(4.dp.scaledWidth(), Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
         ) {
            Icon(
               imageVector = Icons.Outlined.Add,
               modifier = Modifier.size(13.dp.scaledWidth()),
               contentDescription = "Add Habit Button",
               tint = HabitColors.PrimaryDark
            )
            Text(
               "Add Habits",
               style = MaterialTheme.typography.labelSmall.copy(color = HabitColors.PrimaryDark),

               )
         }

      }

      var localOrder by remember(habitList) { mutableStateOf(habitList) }
      val lazyListState = rememberLazyListState()

      val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
         localOrder = localOrder.toMutableList().apply {
            add(to.index, removeAt(from.index))
         }
      }
      LazyColumn(state = lazyListState, modifier = Modifier.fillMaxHeight()) {
         items(localOrder, key = { habit -> habit.id }) { habit ->
            ReorderableItem(reorderableState, key = habit.id) { isDragging ->

               ArchivableHabitRowNewUi(
                  habitId = habit.id,
                  habitName = habit.name,
                  streak = habit.currentStreak,
                  isToggled = habit.isDoneToday,
                  emoji = habit.emoji,
                  habitColor = habit.color,
                  isArchived = habit.isArchived,
                  onToggle = { onToggleHabitId(habit.id) },
                  onClickHabit = { onHabitItemClick(habit.id) },
                  onArchive = { onArchiveHabit(habit.id) },
                  reorderableScope = this@ReorderableItem,
                  onDragStopped = { onHabitsReordered(localOrder.map { it.id }) },
                  modifier = Modifier.graphicsLayer {
                     scaleX = if (isDragging) 1.03f else 1f
                     scaleY = if (isDragging) 1.03f else 1f
                     shadowElevation = if (isDragging) 8f else 0f
                  })
            }
         }
      }
   }
}

data class UiScale(
   val width: Float,
   val height: Float
)

val LocalUiScale = compositionLocalOf { UiScale(1f, 1f) }

@Composable
fun ProvideUiScale(
   referenceWidthDp: Float = 360f,
   referenceHeightDp: Float = 800f,
   content: @Composable () -> Unit
) {
   val config = LocalConfiguration.current
   val widthScale = (config.screenWidthDp / referenceWidthDp).coerceIn(0.85f, 1.3f)
   val heightScale = (config.screenHeightDp / referenceHeightDp).coerceIn(0.85f, 1.3f)

   CompositionLocalProvider(LocalUiScale provides UiScale(widthScale, heightScale)) {
      content()
   }
}

@Composable
fun Dp.scaledWidth(): Dp = this * LocalUiScale.current.width

@Composable
fun Dp.scaledHeight(): Dp = this * LocalUiScale.current.height


@Composable
fun HabitRowNewUi(
   habitId: Int,
   habitName: String,
   streak: Int,
   isToggled: Boolean,
   emoji: String,
   habitColor: Int?,
   modifier: Modifier = Modifier,
   onToggle: () -> Unit = {},
   onClickHabit: () -> Unit = {},
   reorderableScope: ReorderableCollectionItemScope? = null,
   onDragStopped: () -> Unit,
) {
   val interactionSource = remember { MutableInteractionSource() }
   with(LocalSharedTransitionScope.current) {
      Row(
         horizontalArrangement = Arrangement.spacedBy(12.dp.scaledWidth(), Alignment.Start),
         verticalAlignment = Alignment.CenterVertically,
         modifier = modifier
            .padding(horizontal = 16.dp.scaledWidth(), vertical = 12.dp.scaledHeight())
            .combinedClickable(
               interactionSource = interactionSource,
               indication = null, // avoid double ripple; row bg already provides feedback
               onClick = { onClickHabit() },
               onLongClick = { /* no-op: draggableHandle listens to the same source */ })
            .then(
               if (reorderableScope != null) {
                  with(reorderableScope) {
                     Modifier.longPressDraggableHandle(
                        onDragStopped = onDragStopped
                     )
                  }
               } else {
                  Modifier
               })
      ) {
         EmojiIcon(
            selectedEmoji = emoji, onClickIcon = onClickHabit, modifier = Modifier
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

         Column(modifier = Modifier.weight(1f)) {
            Text(
               text = habitName,
               color = if (isToggled) Color(0xff99a1af) else Color(0xFF101828),
               textDecoration = if (isToggled) TextDecoration.LineThrough else TextDecoration.None,
               lineHeight = 1.43.em,
               style = MaterialTheme.typography.titleMedium,
               maxLines = 1,
               overflow = TextOverflow.Ellipsis
            )
            Text(
               text = "Daily",
               color = Color(0xff99a1af),
               lineHeight = 1.33.em,
               style = MaterialTheme.typography.bodySmall
            )
         }

         SelectedIcon(
            icon = rememberVectorPainter(Icons.Rounded.Check),
            backgroundColor = HabitColors.Accent,
            size = 28.dp.scaledWidth(),
            isSelected = isToggled,
            onToggle = onToggle,
            modifier = Modifier

         )
      }
   }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivableHabitRowNewUi(
   habitId: Int,
   habitName: String,
   streak: Int,
   isToggled: Boolean,
   emoji: String,
   habitColor: Int?,
   onToggle: () -> Unit,
   onClickHabit: () -> Unit,
   onArchive: () -> Unit,
   modifier: Modifier = Modifier,
   reorderableScope: ReorderableCollectionItemScope? = null,
   onDragStopped: () -> Unit = {},
   isArchived: Boolean
) {
   val dismissState = rememberSwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      confirmValueChange = { true },
      positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
   )

   var previousValue by remember { mutableStateOf(dismissState.settledValue) }

   LaunchedEffect(dismissState.settledValue) {
      val prev = previousValue
      previousValue = dismissState.settledValue

      if (prev == SwipeToDismissBoxValue.Settled && dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
         onArchive()
      } else {
         dismissState.snapTo(SwipeToDismissBoxValue.Settled)
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
               .fillMaxSize()
               .background(Color(0xFFECF4EE)), // outer box still fills, but is fully transparent
            contentAlignment = Alignment.CenterEnd
         ) {

            Row(
               modifier = Modifier
                  .width(revealedWidth) // ← only the revealed sliver gets colored
                  .padding(vertical = 1.dp)
                  .fillMaxSize()
                  .background(
                     if (isArchived) Color(0xFF19C760) else HabitColors.Danger,
                     RoundedCornerShape(20.dp)
                  )
                  .clip(RoundedCornerShape(20.dp)),

               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.Center
            ) {
               if (revealedWidth > 40.dp) {
                  Icon(
                     imageVector = if (isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                     contentDescription = if (isArchived) "UnArchive" else "Archive",
                     tint = HabitColors.Surface,
                     modifier = Modifier.graphicsLayer {
                        alpha = dismissState.progress
                     })
               }
            }
         }
      }) {
      with(LocalSharedTransitionScope.current) {
         HabitRowNewUi(
            habitId = habitId,
            habitName = habitName,
            streak = streak,
            isToggled = isToggled,
            emoji = emoji,
            modifier = Modifier
               .background(HabitColors.Surface)
               .clip(RoundedCornerShape(4.dp))
               .border(
                  width = 0.5.dp,
                  color = Color(0xFFECF4EE),
                  shape = RoundedCornerShape(4.dp)
               )
               .sharedBounds(
                  sharedContentState = rememberSharedContentState(
                     key = HabitSharedElementKey(
                        habitId, type = HabitSharedElementType.Bounds
                     )
                  ),
                  animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

                  clipInOverlayDuringTransition = OverlayClip(
                     RoundedCornerShape(roundedCornerAnimation)
                  ),
               ),
            onToggle = onToggle,
            onClickHabit = onClickHabit, // ← the scope, threaded down
            reorderableScope = reorderableScope,
            onDragStopped = onDragStopped,
            habitColor = habitColor,
         )
      }
   }
}

@Composable
fun NewHabitRow(modifier: Modifier = Modifier) {

   Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp.scaledWidth(), Alignment.Start),
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier.padding(horizontal = 16.dp.scaledWidth(), vertical = 12.dp.scaledHeight())
   ) {
      EmojiIconDisplay("📚")

      Column(modifier = Modifier.weight(1f)) {
         Text(
            text = "Read for 20 minutes",
            color = Color(0xff99a1af),
            textDecoration = TextDecoration.LineThrough,
            lineHeight = 1.43.em,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
         )
         Text(
            text = "Daily",
            color = Color(0xff99a1af),
            lineHeight = 1.33.em,
            style = MaterialTheme.typography.bodySmall
         )
      }

      SelectedIcon(
         icon = rememberVectorPainter(Icons.Rounded.Check),
         backgroundColor = HabitColors.Accent,
         size = 28.dp.scaledWidth(),
      )

   }
}

@Composable
private fun EmojiIconDisplay(
   selectedEmoji: String,
   modifier: Modifier = Modifier,
) {

   Box(
      contentAlignment = Alignment.Center,
      modifier = modifier
         .requiredSize(size = 40.dp.scaledWidth())
         .clip(shape = CircleShape)
         .background(color = Color(0xff4caf50).copy(alpha = 0.13f))
   ) {
      Text(
         text = selectedEmoji,
         color = Color(0xff111827),
         lineHeight = 1.56.em,
         style = TextStyle(
            fontSize = 18.sp
         )
      )
   }
}

@Composable
private fun EmojiIcon(
   selectedEmoji: String,
   modifier: Modifier = Modifier,
   onClickIcon: () -> Unit = {},
) {

   Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
         .requiredSize(size = 40.dp.scaledWidth())
         .clip(shape = CircleShape)
         .background(color = Color(0xff4caf50).copy(alpha = 0.13f))
         .clickable(onClick = onClickIcon)
   ) {
      Text(
         text = selectedEmoji,
         color = Color(0xff111827),
         lineHeight = 1.56.em,
         style = TextStyle(
            fontSize = 18.sp
         )
      )
   }
}
