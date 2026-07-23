package android.learn.habitapp

import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.ui.components.ErrorScreen
import android.learn.habitapp.ui.components.LoadingSpinner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.DayOfWeek

@Composable
fun TodayHabitScreen(
   habitViewModel: HabitViewModel,
   onAddHabit: () -> Unit,
   onHabitClicked: (Int) -> Unit
) {
   val habitUiStateForToday by habitViewModel.habitUiStateForTodayDate.collectAsStateWithLifecycle()
   val todayScreenWeekStats by habitViewModel.todayScreenWeekStats.collectAsStateWithLifecycle()
   val weeklyCompletionByDay by habitViewModel.weeklyCompletionByDay.collectAsStateWithLifecycle()

   Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .background(MaterialTheme.colorScheme.surface)
         .padding(vertical = 8.dp.scaledHeight(), horizontal = 16.dp.scaledWidth())
   ) {
      OverallProgressCard(weekStats = todayScreenWeekStats)
      WeekOverviewCard(weeklyCompletionByDay)

      when (habitUiStateForToday) {
         is UiState.Success -> {
            HabitListNewUi(
               habitList = (habitUiStateForToday as UiState.Success).habits,
               onToggleHabitId = { habitId ->
                  habitViewModel.onHabitChecked(habitId)
               },
               onArchiveHabit = habitViewModel::onArchiveHabit,
               onHabitsReordered = habitViewModel::onHabitsReordered,
               onHabitItemClick = onHabitClicked,
               habitListTitle = "Today's Habits",
               onAddHabit = onAddHabit
            )

         }


         is UiState.Loading -> LoadingSpinner()

         is UiState.Error -> ErrorScreen((habitUiStateForToday as UiState.Error).message)
      }
   }
}

@Composable
fun OverallProgressCard(
   modifier: Modifier = Modifier,
   weekStats: OverviewStats,
) {

   val statTitleStyle = MaterialTheme.typography.headlineSmall.copy(
      fontWeight = FontWeight(700),
      color = MaterialTheme.colorScheme.onSecondaryContainer,
      textAlign = TextAlign.Center
   )

   val statSubtitleStyle = MaterialTheme.typography.labelSmall.copy(
      fontWeight = FontWeight(400),
      color = MaterialTheme.colorScheme.onSecondaryContainer,
      textAlign = TextAlign.Center
   )

   Column(
      verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = modifier
         .fillMaxWidth()
         .height(214.dp.scaledHeight())
         .background(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(size = 24.dp)
         )
         .padding(20.dp.scaledWidth())
         .padding(top = 4.dp.scaledHeight())
   ) {
      Row(
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.Top,
         modifier = Modifier.fillMaxWidth()
      ) {
         Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
         ) {
            Text(
               text = "Good Morning! 🌅",
               style = MaterialTheme.typography.titleSmall.copy(
//                     fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                  fontWeight = FontWeight(400),
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
               )
            )
            Text(
               text = "Keep the momentum going!",
               style = MaterialTheme.typography.bodySmall.copy(
//                     fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                  fontWeight = FontWeight(400),
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
               )
            )

         }

         HabitIconBadge(
            icon = painterResource(R.drawable.settings_icon),
            backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .1f),
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
            size = 36.dp.scaledWidth(),
            elevation = 0.dp

         )
      }
      Text(
         text = "Overall Progress".uppercase(),
         style = MaterialTheme.typography.bodySmall.copy(
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(600),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            letterSpacing = 0.6.sp,
         ),
         modifier = Modifier.padding(top = 12.dp.scaledHeight())
      )
      Row(
         horizontalArrangement = Arrangement.spacedBy(20.dp.scaledWidth(), Alignment.Start),
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier.padding(top = 12.dp.scaledHeight()),
      ) {

         OverViewProgressBar(
            titleText = "${weekStats.successRatePercent}%",
            titleStyle = MaterialTheme.typography.bodyLarge.copy(
               fontWeight = FontWeight(700),
               color = Color(0xFF1E2939),
            ),
            subtitleText = "This Week",
            subtitleStyle = TextStyle(
               fontSize = 9.sp,
               lineHeight = 13.5.sp,
               fontWeight = FontWeight(400),
               color = Color(0xFF99A1AF),
            ),
            progress = .6f
         )

         Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
         ) {
            HabitStatView(
               titleText = "${weekStats.grandTotalCompleted}",
               titleStyle = statTitleStyle,
               subtitleText = "Completed",
               subtitleStyle = statSubtitleStyle
            )
            HabitStatView(
               titleText = "${weekStats.remainingDue ?: 0}",
               titleStyle = statTitleStyle,
               subtitleText = "Due This Week",
               subtitleStyle = statSubtitleStyle
            )
            HabitStatView(
               titleText = "${weekStats.missedCount ?: 0}",
               titleStyle = statTitleStyle,
               subtitleText = "Missed",
               subtitleStyle = statSubtitleStyle
            )
         }

      }
   }
}

@Composable
fun WeekOverviewCard(weeklyCompletionByDay: Map<DayOfWeek, Boolean>) {
   val selectedDays = weeklyCompletionByDay.filter { entries ->
      entries.value
   }.keys

   Column(
      verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .shadow(elevation = 2.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .shadow(elevation = 3.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .fillMaxWidth()
         .height(115.dp.scaledHeight())
         .background(
            color = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(size = 16.dp)
         )
         .padding(16.dp.scaledWidth())
   ) {
      Text(
         text = "This Week".uppercase(),
         style = MaterialTheme.typography.labelMedium.copy(
//            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(600),
            color = Color(0xFF99A1AF),
            letterSpacing = 0.6.sp,
         )
      )
      Row(
         modifier = Modifier
            .padding(top = 12.dp.scaledHeight())
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
      ) {
         DayOfWeek.entries.forEach { day ->
            val selected = day in selectedDays

            Column(
               verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
               horizontalAlignment = Alignment.CenterHorizontally,
            ) {
               Text(
                  text = day.name.take(1), // M T W T F S S
                  style = MaterialTheme.typography.labelMedium.copy(
                     //                  fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                     fontWeight = FontWeight(500),
                     color = Color(0xFF99A1AF),
                  )
               )
               SelectedDayIcon(
                  icon = painterResource(R.drawable.check_icon),
                  backgroundColor = MaterialTheme.colorScheme.onPrimary,
                  iconTint = MaterialTheme.colorScheme.primaryContainer,
                  size = 32.dp,
                  isSelected = selected,
               )

            }
         }
      }
   }
}


@Composable
fun SelectedDayIcon(
   icon: Painter,
   backgroundColor: Color,
   modifier: Modifier = Modifier,
   iconTint: Color = Color.White,
   size: Dp = 28.dp,
   isSelected: Boolean = false,
   onToggle: () -> Unit = {},
) {

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
         .clip(CircleShape)
         .clickable(onClick = onToggle),
      contentAlignment = Alignment.Center
   ) {
      if (isSelected) {
         Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f) // icon ~50% of circle
         )

      } else {
         Box(
            Modifier
               .clip(CircleShape)
               .background(color = Color(0xFFF3F4F6))
         ) {
            Box(
               Modifier
                  .size(size * .25f)
                  .clip(CircleShape)
                  .background(color = Color(0xFFD1D5DC))
            )
         }
      }
   }
}

@Composable
fun HabitStatView(
   titleText: String,
   titleStyle: TextStyle,
   subtitleText: String,
   subtitleStyle: TextStyle,
   modifier: Modifier = Modifier,
) {
   Column(
      verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
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
