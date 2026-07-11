package android.learn.habitapp.ui.components

import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import android.util.Log
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs


@Composable
fun HabitList(
   habitList: List<HabitUiState>,
   scrollToHabitId: Int?,
   onScrollHandled: () -> Unit,
   hasSeenSwipeHint: Boolean,
   onHintShown: () -> Unit,
   onToggleHabitId: (habitId: Int) -> Unit,
   onArchiveHabit: (Int) -> Unit,
   onHabitsReordered: (List<Int>) -> Unit,
   onHabitItemClick: (Int) -> Unit,
) {
   var localOrder by remember(habitList) { mutableStateOf(habitList) }
   val lazyListState = rememberLazyListState()


   val scope = rememberCoroutineScope()
   LaunchedEffect(scrollToHabitId, localOrder) {
      if (scrollToHabitId != null) {
         val index = localOrder.indexOfFirst { it.id == scrollToHabitId }
         if (index != -1) {
            val isAlreadyVisible =
               lazyListState.layoutInfo.visibleItemsInfo.any { it.index == index }

            if (!isAlreadyVisible) {

               lazyListState.scrollToItem(index)

            }
            onScrollHandled()
         }
      }
   }
   // Stable reference to whichever habit was first from the SOURCE list —
   // doesn't shift just because a drag temporarily reorders things.
   val hintHabitId = remember(habitList) { habitList.firstOrNull()?.id }

   val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
      localOrder = localOrder.toMutableList().apply {
         add(to.index, removeAt(from.index))
      }
   }


   LazyColumn(state = lazyListState, modifier = Modifier.fillMaxHeight()) {
      items(localOrder, key = { habit -> habit.id }) { habit ->
         Log.d("COLOR", habit.color.toString())
         ReorderableItem(reorderableState, key = habit.id) { isDragging ->
            val isHintTarget = habit.id == hintHabitId // stable, no longer position-dependent

            val rowContent: @Composable (Modifier) -> Unit = { rowModifier ->
               ArchivableHabitRow(
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
                  modifier = rowModifier.graphicsLayer {
                     scaleX = if (isDragging) 1.03f else 1f
                     scaleY = if (isDragging) 1.03f else 1f
                     shadowElevation = if (isDragging) 8f else 0f
                  })
            }

            if (isHintTarget) {
               SwipeHintOverlay(
                  hasSeenHint = hasSeenSwipeHint, onHintShown = onHintShown
               ) { hintModifier ->
                  rowContent(hintModifier)
               }
            } else {
               rowContent(Modifier)
            }
         }
      }
   }
}

@Composable
fun SwipeHintOverlay(
   hasSeenHint: Boolean, onHintShown: () -> Unit, content: @Composable (Modifier) -> Unit
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
   streak: Int,
   isToggled: Boolean,
   emoji: String,
   habitColor: Int?,
   onToggle: () -> Unit,
   onClickHabit: () -> Unit,
   onArchive: () -> Unit,
   modifier: Modifier = Modifier,
   reorderableScope: ReorderableCollectionItemScope,
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
            modifier = Modifier.fillMaxSize(), // outer box still fills, but is fully transparent
            contentAlignment = Alignment.CenterEnd
         ) {
            Row(
               modifier = Modifier
                  .width(revealedWidth) // ← only the revealed sliver gets colored
                  .padding(vertical = 8.dp)
                  .padding(end = 8.dp)
                  .fillMaxSize()
                  .background(
                     if (isArchived) Color.Green else MaterialTheme.colorScheme.errorContainer,
                     RoundedCornerShape(20.dp)
                  )
                  .clip(RoundedCornerShape(20.dp)),

               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.Center
            ) {
               Icon(
                  imageVector = if (isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                  contentDescription = if (isArchived) "UnArchive" else "Archive",
                  tint = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.graphicsLayer {
                     alpha = dismissState.progress
                  })
            }
         }
      }) {

      with(LocalSharedTransitionScope.current) {
         HabitRow(
            habitId = habitId,
            habitName = habitName,
            streak = streak,
            isToggled = isToggled,
            emoji = emoji,
            modifier = Modifier

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
fun HabitRow(
   habitId: Int,
   habitName: String,
   streak: Int,
   isToggled: Boolean,
   emoji: String,
   habitColor: Int?,
//   hazeState: HazeState,
   modifier: Modifier = Modifier,
   onToggle: () -> Unit = {},
   onClickHabit: () -> Unit = {},
   reorderableScope: ReorderableCollectionItemScope,
   onDragStopped: () -> Unit,
) {

   val backgroundColor =
      habitColor?.let { Color(it).copy(alpha = 0.25f) } ?: Color.White.copy(alpha = 0.2f)

   val interactionSource = remember { MutableInteractionSource() }
   with(LocalSharedTransitionScope.current) {

      Row(
         modifier = Modifier

            .padding(10.dp)
            .fillMaxWidth()
            .then(modifier)
//         .hazeEffect(state = hazeState) {
//            blurEffect {
//               blurRadius = 20.dp
//               colorEffects = listOf(HazeColorEffect.tint(Color.Black.copy(alpha = 0.5f)))
//            }
            .clip(RoundedCornerShape(20.dp))
            .background(
               Brush.linearGradient(
                  colors = listOf(
                     backgroundColor.copy(alpha = 0.22f), backgroundColor.copy(alpha = 0.08f)
                  )
               )
            )
            .border(
               width = 0.5.dp, brush = Brush.linearGradient(
                  colors = listOf(
                     backgroundColor.copy(alpha = 0.6f), backgroundColor.copy(alpha = 0.1f)
                  )
               ), shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
               interactionSource = interactionSource,
               indication = null, // avoid double ripple; row bg already provides feedback
               onClick = { onClickHabit() },
               onLongClick = { /* no-op: draggableHandle listens to the same source */ })
            .then(
               with(reorderableScope) {
                  Modifier.longPressDraggableHandle(
                     onDragStopped = onDragStopped
                  )
               })

            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         // In HabitRow, right after the drag handle / before EmojiButton
         if (habitColor != null) {
            Box(
               modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(habitColor))
            )
            Spacer(Modifier.width(8.dp))
         }
         EmojiButton(
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
         Spacer(modifier = Modifier.weight(.1f))
         Text(
            text = habitName, style = MaterialTheme.typography.titleMedium
         )
         if (streak > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
               text = "🔥 $streak",
               style = MaterialTheme.typography.labelMedium,
               color = MaterialTheme.colorScheme.onSurfaceVariant
            )
         }
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
