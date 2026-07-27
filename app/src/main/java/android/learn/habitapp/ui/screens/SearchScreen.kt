package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.R
import android.learn.habitapp.ui.components.dropShadow
import android.learn.habitapp.navigation.HabitSharedElementKey
import android.learn.habitapp.navigation.HabitSharedElementType
import android.learn.habitapp.navigation.LocalAnimatedVisibilityScope
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.theme.HabitColors
import android.learn.habitapp.ui.theme.LocalSharedTransitionScope
import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CustomSearchHabitBarNewUi(
   query: String,
   isExpanded: Boolean,
   onExpandedChange: (Boolean) -> Unit,
   onQueryChange: (String) -> Unit,
   onCloseSearch: () -> Unit,
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
         horizontalArrangement = Arrangement.spacedBy(12.dp),
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),

         ) {
         with(LocalSharedTransitionScope.current) {
            Row(
               modifier = Modifier
                  .height(40.dp)
                  .weight(1f)
                  .dropShadow(
                     shape = RoundedCornerShape(28.dp),
                  )
                  .clip(RoundedCornerShape(28.dp))
                  .background(HabitColors.Surface)
                  .sharedBounds(
                     sharedContentState = rememberSharedContentState(
                        "search_bar"
                     ), animatedVisibilityScope = LocalAnimatedVisibilityScope.current,

                     resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                  )
                  .padding(horizontal = 16.dp),
               verticalAlignment = Alignment.CenterVertically

            ) {

               Icon(
                  painter = painterResource(R.drawable.search_icon),
                  contentDescription = "Search",
                  modifier = Modifier
                     .size(20.dp.scaledWidth())
                     .sharedElement(
                        sharedContentState = rememberSharedContentState("search_bar_icon"),
                        animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                     ),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
               )

               SearchTextField(
                  query = query,
                  onQueryChange = onQueryChange,
                  isExpanded = isExpanded,
                  onExpandedChange = onExpandedChange,
                  focusRequester = focusRequester,
                  modifier = Modifier
                     .weight(1f)
//                     .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
               )

            }
         }
         HabitIconBadge(
            rememberVectorPainter(Icons.Default.Close),
            backgroundColor = HabitColors.Surface,
            iconTint = HabitColors.PrimaryDark,
            size = 40.dp,
            elevation = 2.dp,
            onClickIcon = onCloseSearch,
         )
      }
      if (isExpanded && query.isNotEmpty()) {
//         HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
         Column(
            modifier = Modifier
               .padding(8.dp)
               .fillMaxWidth()
               .dropShadow(
                  shape = RoundedCornerShape(16.dp),
                  blur = 8.dp,
                  offsetY = 0.dp,
                  offsetX = 0.dp,
                  spread = (-2).dp
               )
               .clip(RoundedCornerShape(16.dp))
               .background(HabitColors.Surface)
               .wrapContentHeight()
//               .heightIn(max = 900.dp) // The dropdown menu results expansion boundaries


         ) {
            content()

         }

      }

   }
}

@Composable
fun SearchTextField(
   query: String,
   onQueryChange: (String) -> Unit,
   isExpanded: Boolean,
   onExpandedChange: (Boolean) -> Unit,
   focusRequester: FocusRequester,
   modifier: Modifier = Modifier,
   placeholder: String = "Search habits...",
   textColor: Color = Color(0xFF1E2938)
) {
   val interactionSource = remember { MutableInteractionSource() }

   BasicTextField(
      value = query,
      onValueChange = {
         onQueryChange(it)
         if (!isExpanded && it.isNotEmpty()) {
            onExpandedChange(true)
         }
      },
      modifier = modifier
         .focusRequester(focusRequester)
         .onFocusChanged { focusState ->
            if (focusState.isFocused && !isExpanded) {
               onExpandedChange(true)
            }
         },
      textStyle = MaterialTheme.typography.bodyLarge.copy(
         color = textColor
      ),
      singleLine = true,
      interactionSource = interactionSource,
      cursorBrush = SolidColor(textColor),
      decorationBox = { innerTextField ->
         TextFieldDefaults.DecorationBox(
            value = query,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = { Text(placeholder) },
            colors = TextFieldDefaults.colors(
               focusedContainerColor = Color.Transparent,
               unfocusedContainerColor = Color.Transparent,
               disabledContainerColor = Color.Transparent,
               focusedIndicatorColor = Color.Transparent,
               unfocusedIndicatorColor = Color.Transparent,
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
         )
      }
   )
}

@Composable
fun SearchScreenRoute(
   habitViewModel: HabitViewModel,
   onBackPressed: () -> Unit,
   onHabitClicked: (Int) -> Unit,
) {
   val searchQuery by habitViewModel.searchQuery.collectAsState()
   val filteredHabitUiState by habitViewModel.filteredHabitUiState.collectAsStateWithLifecycle()
   val filteredHabits by remember {
      derivedStateOf {
         if (searchQuery.isEmpty()) emptyList()
         else when (filteredHabitUiState) {
            is UiState.Success -> (filteredHabitUiState as UiState.Success).habits
            else -> emptyList()
         }
      }
   }
   var isSearchExpanded by remember { mutableStateOf(true) }
   val focusManager = LocalFocusManager.current
   val animationDuration = 200

   BackHandler(enabled = isSearchExpanded) {
      onBackPressed()
      habitViewModel.onSearchQueryChange("")
   }
   SearchScreen(
      searchQuery,
      isSearchExpanded,
      onExpandedChange = { isSearchExpanded = it },
      animationDuration,
      filteredHabits,
      onCloseSearch = {
         isSearchExpanded = false
         habitViewModel.onSearchQueryChange("")
         focusManager.clearFocus(force = true)
         onBackPressed()
      },
      onQueryChange = { habitViewModel.onSearchQueryChange(it) },
      onHabitClicked = { searchHabitId ->
         habitViewModel.requestScrollTo(searchHabitId)
         onHabitClicked(searchHabitId)
         isSearchExpanded = false
      },
   )
}

@Composable
private fun SearchScreen(
   searchQuery: String,
   isSearchExpanded: Boolean,
   onExpandedChange: (Boolean) -> Unit,
   animationDuration: Int,
   filteredHabits: List<HabitUiState>,
   onCloseSearch: () -> Unit,
   onHabitClicked: (Int) -> Unit,
   onQueryChange: (String) -> Unit,
) {
   CustomSearchHabitBarNewUi(
      query = searchQuery,
      isExpanded = isSearchExpanded,
      onExpandedChange = onExpandedChange,
      onQueryChange = onQueryChange,
      onCloseSearch = onCloseSearch,
      modifier = Modifier
         .fillMaxWidth()
         .padding(horizontal = 8.dp, vertical = 8.dp)

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
                        tint = HabitColors.PrimaryDark
                     )
                     Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
                  }
               }
            }
         }
      }
   }
}
