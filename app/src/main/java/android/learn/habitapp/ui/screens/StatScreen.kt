package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.data.local.HabitStatItem
import android.learn.habitapp.data.local.Timeframe
import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


val statSegementedControl = mapOf(
   "Week" to Timeframe.WEEK,
   "Month" to Timeframe.MONTH,
   "Year" to Timeframe.YEAR
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(habitViewModel: HabitViewModel) {

   val statScreenOverviewStats by habitViewModel.statScreenOverviewStats.collectAsStateWithLifecycle()
   val streakPair by habitViewModel.streakPair.collectAsStateWithLifecycle()

   Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .background(Color.Transparent)
         .padding(vertical = 8.dp.scaledHeight(), horizontal = 16.dp.scaledWidth())
   ) {

      StatsTopAppBar(
         habitViewModel::onStatScreenTimeframeChange
      )

      HabitStatOverview(
         currentStreak = streakPair.first,
         longestStreak = streakPair.second,
         percent = statScreenOverviewStats.successRatePercent
      )
      CompletionByHabit(statScreenOverviewStats.habitStats)


   }


}


@Preview
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SegmentedControl(
   segments: List<String> = listOf("Analytics", "Security", "Settings"),
   initialSelected: String = "Analytics",
   onSegmentSelected: (String) -> Unit = {}
) {
   // 1. Properly hoist and track state matching your signature
   var selectedSegment by remember { mutableStateOf(initialSelected) }

   val coroutineScope = rememberCoroutineScope()
   SharedTransitionLayout {
      Row(
         horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier
            .width(180.dp) // Adjusted slightly from 182.dp to comfortably fit 4 long labels
            .height(38.dp) // Adjusted slightly from 32.dp to accommodate 4.dp padding + comfortable heights
            .shadow(
               elevation = 2.dp, // Adjust this value to make the shadow larger/smaller
               shape = RoundedCornerShape(50),
               clip = true // This handles the clipping, so you can remove your .clip() modifier
            )
            .background(color = HabitColors.Surface)
            .padding(4.dp)
      ) {
         segments.forEach { segment ->

            val isSelected = selectedSegment == segment

            Box(
               contentAlignment = Alignment.Center,
               modifier = Modifier
                  .weight(1f) // Distributes segment slots evenly
                  .fillMaxHeight()
                  .clip(RoundedCornerShape(50))
                  .clickable(
                     interactionSource = remember { MutableInteractionSource() },
                     indication = null // Disables default ripple for a cleaner slide look
                  ) {
                     selectedSegment = segment
                     onSegmentSelected(segment)
                  }) {
               // 2. AnimatedContent coordinates the swap between "Selected" and "Unselected" visuals
               AnimatedContent(
                  targetState = isSelected, transitionSpec = {
                     // Fade the underlying content quickly so the green blob stands out
                     fadeIn(animationSpec = tween(120)) togetherWith fadeOut(
                        animationSpec = tween(
                           120
                        )
                     )
                  }, label = "segment_indicator_$segment"
               ) { targetSelected ->
                  if (targetSelected) {
                     // Green indicator capsule (Active State)
                     Box(
                        modifier = Modifier
                           .fillMaxSize()
                           // 3. Shared bounds morphs this container to its new peer position
                           .sharedBounds(
                              sharedContentState = rememberSharedContentState(key = "active_indicator_blob"),
                              animatedVisibilityScope = this@AnimatedContent,
                              // Customize the speed/stiffness of the blob stretch
                              boundsTransform = { _, _ ->
                                 spring(stiffness = 380f, dampingRatio = 0.78f)
                              },
                              resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                           )
                           .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50))
                     )
                  }
               }
               // Text layered on top of active state
               Box(
                  modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
               ) {
                  val textColor = remember { Animatable(Color.Gray) }
                  LaunchedEffect(isSelected) {
                     if (isSelected) textColor.animateTo(Color.White)
                  }
                  Text(
                     text = segment,
                     color = if (isSelected) Color.White else Color.Gray,
                     fontSize = 12.sp,
                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                     modifier = Modifier.renderInSharedTransitionScopeOverlay(
                        zIndexInOverlay = 1f, // Higher z-index renders on top of lower ones
                        renderInOverlay = { isTransitionActive } // Only overlay while transition is active
                     )
                     // 2. Optional: Animate its enter/exit fade while the transition happens
//                           .animateEnterExit(
//                              enter = fadeIn(animationSpec = tween(30)),
//                              exit = fadeOut(animationSpec = tween(30))
//                           )
                  )
               }
            }
         }
      }
   }
}

@Composable
fun StatsTopAppBar(
   onSegmentSelected: (Timeframe) -> Unit
) {

   val segments: List<String> = statSegementedControl.keys.toList()
   Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .wrapContentHeight()
         .fillMaxWidth()
         .background(Color.Transparent)
   ) {
      Text(
         text = "Stats", style = MaterialTheme.typography.titleLarge.copy(
//            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(700),
            color = Color(0xFF101828),
         )
      )
      SegmentedControl(
         segments = segments,
         initialSelected = segments[0]
      ) { timeframeName ->
         onSegmentSelected(statSegementedControl.getValue(timeframeName))
      }


   }
}

@Composable
fun HabitStatOverview(
   currentStreak: Int,
   longestStreak: Int,
   percent: Int
) {
   Column(
      verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .shadow(
            elevation = 2.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000)
         )
         .shadow(elevation = 3.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .fillMaxWidth()
         .height(200.dp)
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(size = 16.dp))
         .padding(16.dp)
   ) {
      Text(
         text = "Overview".uppercase(),
         style = MaterialTheme.typography.labelMedium.copy(
//            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(600),
            color = HabitColors.TextSecondary,
            letterSpacing = 0.6.sp,
         )
      )
      Row(
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.Top,
         modifier = Modifier
            .padding(top = 28.dp.scaledWidth())
//            .padding(16.dp)
            .fillMaxWidth()
      ) {
         Stat(
            emoji = "✅",
            statInfo = "$percent",
            statDescription = "Success Rate",
            modifier = Modifier.weight(1f)

         )

         Stat(
            emoji = "🔥",
            statInfo = "$currentStreak",
            statDescription = "Current Streak",
                    modifier = Modifier.weight(1f)
         )

         Stat(
            emoji = "🏆",
            statInfo = "$longestStreak",
            statDescription = "Longest Streak",
            modifier = Modifier.weight(1f)
         )
      }

   }
}

@Composable
fun CompletionByHabit(
   habitStatsList: List<HabitStatItem>
) {
   Column(
      verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .shadow(elevation = 2.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .shadow(elevation = 3.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .fillMaxWidth()
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(size = 16.dp))
         .padding(16.dp)
   ) {
      Text(
         text = "Completion by Habit".uppercase(),
         style = MaterialTheme.typography.labelMedium.copy(
//            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(600),
            color = HabitColors.TextSecondary,
            letterSpacing = 0.6.sp,
         )
      )

      habitStatsList.forEach {
         val colorInt = it.color ?: 0xFF4CAF50
         HabitCompletion(
            habitName = it.habitName,
            habitEmoji = it.habitEmoji,
            color = Color(colorInt.toInt()),
            completionPercent = it.completionPercent
         )
      }


   }

}

@Composable
fun HabitCompletion(
   habitName: String,
   habitEmoji: String,
   color: Color,
   completionPercent: Int
) {

   val animatedPercent by animateIntAsState(
      completionPercent,
      animationSpec = spring(),
      label = "HabitCompletionProgressBar",
   )
   Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .wrapContentHeight()
         .fillMaxWidth()
   ) {
      Box(
         contentAlignment = Alignment.Center,
         modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = .2f))
      ) {
         Text(
            text = habitEmoji, style = TextStyle(
               fontSize = 12.sp,
               lineHeight = 18.sp,
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
               fontWeight = FontWeight(400),
               color = Color(0xFF111827),
            )
         )

      }
      Column(
         modifier = Modifier.fillMaxWidth()
      ) {
         Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
         ) {
            Text(
               text = habitName,
               style = MaterialTheme.typography.labelMedium.copy(
                  color = Color(0xFF364153),
               )

            )
            Text(
               text = "$completionPercent%",
               style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight(700),
                  color = Color(0xFF6A7282),
               )
            )
         }

         Spacer(Modifier.size(4.dp))
         LinearProgressIndicator(
            progress = { (animatedPercent / 100f) },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            gapSize = (-10).dp,
            trackColor = Color(0xFFF3F4F6),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            drawStopIndicator = {}
         )
      }

   }


}

@Preview(showBackground = true)
@Composable
fun Stat(
   emoji: String = "✅",
   statInfo: String = "78%",
   statDescription: String = "Success Rate",
   modifier: Modifier = Modifier
) {
   Column(
      verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
         .fillMaxHeight()
   ) {
      Box(
         contentAlignment = Alignment.Center,
         modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFFE8F5E9))
      ) {
         Text(
            text = emoji, style = TextStyle(
               fontSize = 18.sp,
               lineHeight = 28.sp,
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
               fontWeight = FontWeight(400),
               color = Color(0xFF111827),
            )
         )

      }

      Text(
         text = statInfo, style = MaterialTheme.typography.titleLarge.copy(
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(700),
            color = Color(0xFF101828),
         )
      )
      Text(
         text = statDescription, style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 12.5.sp,
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(400),
            color = HabitColors.TextSecondary,
            textAlign = TextAlign.Center,
         )
      )
   }
}

