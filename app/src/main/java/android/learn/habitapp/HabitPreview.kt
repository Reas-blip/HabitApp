
package android.learn.habitapp

import android.learn.habitapp.ui.CardState
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.theme.HabitAppTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateRect
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HabitAnimationDetailPreview() {
   // 1. WRAPPED IN THEME to prevent Material 3 crashes
   HabitAppTheme {
      val density = LocalDensity.current
      val screen = LocalConfiguration.current

      val screenWidthPx = with(density) { screen.screenWidthDp.dp.toPx() }
      val screenHeightPx = with(density) { screen.screenHeightDp.dp.toPx() }

      // Mock the starting bounds of a row in a list
      val startBounds = remember(density) {
         Rect(
            left = with(density) { 16.dp.toPx() },
            top = with(density) { 150.dp.toPx() },
            right = with(density) { (360.dp - 16.dp).toPx() },
            bottom = with(density) { 230.dp.toPx() }
         )
      }

      var expanded by remember { mutableStateOf(false) }

      val currentState = if (expanded) CardState.EXPANDED else CardState.COMPACT
      val transition = updateTransition(targetState = currentState, label = "card_morph")

      val bounds by transition.animateRect(
         transitionSpec = { spring(dampingRatio = 0.8f, stiffness = 300f) },
         label = "bounds"
      ) { state ->
         if (state == CardState.EXPANDED) Rect(0f, 0f, screenWidthPx, screenHeightPx)
         else startBounds
      }

      val corner by transition.animateDp(
         transitionSpec = { tween(300) },
         label = "corner"
      ) { state -> if (state == CardState.EXPANDED) 0.dp else 20.dp }

      Box(
         Modifier
            .fillMaxSize()
            .background(Color.LightGray)
      ) {
         Text(
            text = "Background Item (Should be hidden when expanded)",
            modifier = Modifier.padding(top = 100.dp, start = 16.dp)
         )

         Box(
            modifier = Modifier
               .offset { IntOffset(bounds.left.toInt(), bounds.top.toInt()) }
               .size(
                  width = with(density) { bounds.width.toDp() },
                  height = with(density) { bounds.height.toDp() }
               )
               .clip(RoundedCornerShape(corner))
               .background(MaterialTheme.colorScheme.surface)
         ) {
            transition.AnimatedContent(
               transitionSpec = {
                  fadeIn(tween(200)) togetherWith fadeOut(tween(200))
               },
               contentKey = { it },
               modifier = Modifier.fillMaxSize()
            ) { state ->
               if (state == CardState.COMPACT) {
                  HabitRow(
                     habitId = 1,
                     habitName = "Learn Jetpack Compose",
                     isToggled = false,
                     emoji = "🚀",
                     modifier = Modifier.fillMaxSize(),
                     onClickHabit = { expanded = true }
                  )
               } else {
                  HabitItemScreen(
                     habit = HabitUiState(
                        id = 1,
                        name = "Learn Jetpack Compose",
                        emoji = "🚀",
                        isDoneToday = false
                     ),
                     onNameChange = {},
                     onEmojiChange = {},
                     onSave = { expanded = false },
                     onBack = { expanded = false },
                     modifier = Modifier.fillMaxSize()
                  )
               }
            }
         }
      }
   }
}
