package android.learn.habitapp//package android.learn.habitapp
//
//import android.learn.habitapp.navigation.HabitSharedElementKey
//import android.learn.habitapp.navigation.HabitSharedElementType
//import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
//import android.learn.habitapp.ui.HabitUiState
//import android.learn.habitapp.ui.theme.HabitAppTheme
//import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.EnterExitState
//import androidx.compose.animation.ExperimentalSharedTransitionApi
//import androidx.compose.animation.SharedTransitionLayout
//import androidx.compose.animation.core.animateDp
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//
//@OptIn(ExperimentalSharedTransitionApi::class)
//@Preview(showBackground = true, widthDp = 360, heightDp = 720)
//@Composable
//fun HabitTransitionInteractivePreview() {
//   HabitAppTheme {
//      // 1. Maintain a state to toggle between the list row and detail screen
//      var showDetail by remember { mutableStateOf(false) }
//
//      // Mock data matching your HabitUiState structure
//      val mockHabit = remember {
//         HabitUiState(
//            id = 1,
//            name = "Read 10 Pages",
//            emoji = "📚",
//            isDoneToday = false
//         )
//      }
//
//      // 2. Wrap everything in the root SharedTransitionLayout
//      SharedTransitionLayout {
//         CompositionLocalProvider(
//            LocalSharedTransitionScope provides this@SharedTransitionLayout
//         ) {
//            // 3. Use AnimatedContent to drive the transition and generate the visibility scope
//            AnimatedContent(
//               targetState = showDetail,
//               label = "screen_switch_transition"
//            ) { isDetailVisible ->
//
//               CompositionLocalProvider(
//                  LocalAnimatedVisibilityScope provides this@AnimatedContent
//               ) {
//                  if (isDetailVisible) {
//                     // Render Detail Screen
//                     HabitItemScreen(
//                        habit = mockHabit,
//                        currentToken1 = currentToken,
//                        onNameChange = {},
//                        onEmojiChange = {},
//                        onSave = { showDetail = false },
//                        onBack = { showDetail = false }
//                     )
//                  } else {
//                     // Render List Container holding the row element
//                     Box(
//                        modifier = Modifier
//                           .fillMaxSize()
//                           .padding(16.dp),
//                        contentAlignment = Alignment.TopCenter
//                     ) {
//                        // Mirror the exact animations/bounds setup from your HabitList
//                        val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(
//                           label = "preview_corner"
//                        ) { enterExit ->
//                           when (enterExit) {
//                              EnterExitState.PreEnter -> 20.dp
//                              EnterExitState.Visible -> 20.dp
//                              EnterExitState.PostExit -> 20.dp
//                           }
//                        }
//
//                        HabitRow(
//                           habitId = mockHabit.id,
//                           habitName = mockHabit.name,
//                           isToggled = mockHabit.isDoneToday,
//                           emoji = mockHabit.emoji,
//                           onClickHabit = { showDetail = true },
//                           modifier = Modifier.sharedBounds(
//                              sharedContentState = rememberSharedContentState(
//                                 key = HabitSharedElementKey(
//                                    habitId = mockHabit.id,
//                                    type = HabitSharedElementType.Bounds
//                                 )
//                              ),
//                              animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
//                              clipInOverlayDuringTransition = OverlayClip(
//                                 RoundedCornerShape(roundedCornerAnimation)
//                              )
//                           )
//                        )
//                     }
//                  }
//               }
//            }
//         }
//      }
//   }
//}
