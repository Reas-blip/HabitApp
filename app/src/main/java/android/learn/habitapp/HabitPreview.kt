package android.learn.habitapp//package android.learn.habitapp
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//
////@OptIn(ExperimentalSharedTransitionApi::class)
////@Preview(showBackground = true, widthDp = 360, heightDp = 720)
////@Composable
////fun HabitTransitionInteractivePreview() {
////   HabitAppTheme {
////      // 1. Maintain a state to toggle between the list row and detail screen
////      var showDetail by remember { mutableStateOf(false) }
////
////      // Mock data matching your HabitUiState structure
////      val mockHabit = remember {
////         HabitUiState(
////            id = 1,
////            name = "Read 10 Pages",
////            emoji = "📚",
////            isDoneToday = false
////         )
////      }
////
////      // 2. Wrap everything in the root SharedTransitionLayout
////      SharedTransitionLayout {
////         CompositionLocalProvider(
////            LocalSharedTransitionScope provides this@SharedTransitionLayout
////         ) {
////            // 3. Use AnimatedContent to drive the transition and generate the visibility scope
////            AnimatedContent(
////               targetState = showDetail,
////               label = "screen_switch_transition"
////            ) { isDetailVisible ->
////
////               CompositionLocalProvider(
////                  LocalAnimatedVisibilityScope provides this@AnimatedContent
////               ) {
////                  if (isDetailVisible) {
////                     // Render Detail Screen
////                     HabitItemScreen(
////                        habit = mockHabit,
////                        currentToken1 = currentToken,
////                        onNameChange = {},
////                        onEmojiChange = {},
////                        onSave = { showDetail = false },
////                        onBack = { showDetail = false }
////                     )
////                  } else {
////                     // Render List Container holding the row element
////                     Box(
////                        modifier = Modifier
////                           .fillMaxSize()
////                           .padding(16.dp),
////                        contentAlignment = Alignment.TopCenter
////                     ) {
////                        // Mirror the exact animations/bounds setup from your HabitList
////                        val roundedCornerAnimation by LocalAnimatedVisibilityScope.current.transition.animateDp(
////                           label = "preview_corner"
////                        ) { enterExit ->
////                           when (enterExit) {
////                              EnterExitState.PreEnter -> 20.dp
////                              EnterExitState.Visible -> 20.dp
////                              EnterExitState.PostExit -> 20.dp
////                           }
////                        }
////
////                        HabitRow(
////                           habitId = mockHabit.id,
////                           habitName = mockHabit.name,
////                           isToggled = mockHabit.isDoneToday,
////                           emoji = mockHabit.emoji,
////                           onClickHabit = { showDetail = true },
////                           modifier = Modifier.sharedBounds(
////                              sharedContentState = rememberSharedContentState(
////                                 key = HabitSharedElementKey(
////                                    habitId = mockHabit.id,
////                                    type = HabitSharedElementType.Bounds
////                                 )
////                              ),
////                              animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
////                              clipInOverlayDuringTransition = OverlayClip(
////                                 RoundedCornerShape(roundedCornerAnimation)
////                              )
////                           )
////                        )
////                     }
////                  }
////               }
////            }
////         }
////      }
////   }
////}
//
//@Preview
//@Composable
//fun ArchivableHabitRowPreview(
//
//) {
//   ArchivableHabitRow(
//      habitId = 0,
//      habitName = "Name",
//      isToggled = true,
//      emoji = "🎉",
//      onToggle = {},
//      onClickHabit = {},
//      onArchive = {},
//      modifier = Modifier,
//      reorderableScope = null,
//      onDragStopped = {},
//   )
//
//}
