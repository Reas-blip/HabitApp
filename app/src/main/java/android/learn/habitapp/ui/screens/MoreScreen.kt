package android.learn.habitapp.ui.screens

import android.learn.habitapp.R
import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoreScreen(
   onNavigateToHabits: () -> Unit = {},
   onNavigateToStats: () -> Unit = {},
   onNavigateToReminders: () -> Unit = {},
   onNavigateToSettings: () -> Unit = {},
   onNavigateToBackup: () -> Unit = {},
) {

   Column(
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .background(Color.Transparent)
         .padding(vertical = 8.dp.scaledHeight(), horizontal = 16.dp.scaledWidth())
   ) {
      MoreTopAppBar()
      AccountNameCard()
      MoreMenuCard(
         onItemClick = { label ->
            when (label) {
               "Habits" -> onNavigateToHabits()
               "Stats & Insights" -> onNavigateToStats()
               "Reminders" -> onNavigateToReminders()
               "Settings" -> onNavigateToSettings()
               "Backup & Restore" -> onNavigateToBackup()
            }
         }
      )

   }
}

@Composable
fun MoreTopAppBar() {
   Text(
      text = "More", style = MaterialTheme.typography.titleLarge.copy(
//         fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
         fontWeight = FontWeight(700),
         color = Color(0xFF101828),
      )
   )
}

@Composable
fun AccountNameCard() {
   Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .shadow(
            elevation = 2.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000)
         )
         .shadow(elevation = 3.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .fillMaxWidth()
         .height(80.dp.scaledHeight())
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(size = 16.dp))
         .padding(16.dp.scaledWidth())
   ) {

      HabitIconBadge(
         icon = painterResource(R.drawable.account_icon),
         backgroundColor = Color(0xFF1B5E20),
         size = 48.dp.scaledWidth()
      )
      Column(
         verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
         horizontalAlignment = Alignment.Start,
         modifier = Modifier.wrapContentSize()
      ) {

         Text(
            text = "Excellence Okeniyi!", style = MaterialTheme.typography.titleSmall.copy(
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
               fontWeight = FontWeight(700),
               color = HabitColors.TextPrimary,
            )
         )
         Text(
            text = "Stay consistent, achieve more",
            style = MaterialTheme.typography.titleSmall.copy(
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
               color = HabitColors.TextSecondary,
            )
         )
      }

   }
}


data class MenuItem(val iconId: Int , val label: String, val sub: String, val color: Color)
val moreMenuItems = listOf(
   MenuItem(R.drawable.book_icon,   "Habits",           "Manage your habits",    Color(0xFF4CAF50)),
   MenuItem(R.drawable.three_bar_icon,   "Stats & Insights", "Analyze your progress", Color(0xFF2196F3)),
   MenuItem(R.drawable.bell_icon, "Reminders",    "Never miss a habit",    Color(0xFFFF9800)),
   MenuItem(R.drawable.settings_icon,   "Settings",         "App preferences",       Color(0xFF9C27B0)),
   MenuItem(R.drawable.database_icon,    "Backup & Restore", "Manage your data",      Color(0xFF607D8B)),
)

@Preview
@Composable
fun MoreMenuCard(onItemClick: (String) -> Unit = {}) {
   Column(
      verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
         .shadow(elevation = 2.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .shadow(elevation = 3.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
         .fillMaxWidth()
         .wrapContentHeight()
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(size = 16.dp))
   ) {
      // Child views.
      Text(
         text = "Account".uppercase(),
         style = MaterialTheme.typography.bodySmall.copy(
//            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(600),
            color = HabitColors.TextSecondary,
            letterSpacing = 0.6.sp,
         ),
         modifier = Modifier
            .padding(horizontal = 16.dp.scaledWidth())
            .padding(top = 16.dp.scaledWidth(), bottom = 8.dp.scaledWidth())
      )
      moreMenuItems.forEach {
        MoreMenuItem(it.label, it.sub, painterResource(it.iconId), it.color, onClick = { onItemClick(it.label) })
      }

   }
}

@Composable
fun MoreMenuItem(
   title: String,
   subTitle: String,
   icon: Painter,
   color: Color,
   onClick: () -> Unit = {},
) {
   Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .border(width = 1.dp, color = Color(0xFFF9FAFB))
         .clickable { onClick() }
         .padding(horizontal = 16.dp, vertical = 14.dp)
         .wrapContentHeight()
         .fillMaxWidth()
   ) {
      HabitIconBadge(
         icon = icon,
         backgroundColor = color.copy(alpha = .08f),
         iconTint = color,
         size = 36.dp,
         shape = RoundedCornerShape(40),
         elevation = 0.dp,
         onClickIcon = {}

      )
      Column(
         verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
         horizontalAlignment = Alignment.Start,
      ) {
         // Child views.
         Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
               fontWeight = FontWeight(600),
               color = HabitColors.TextPrimary,
            )
         )
         Text(
            text = subTitle,
            style = MaterialTheme.typography.bodySmall.copy(
//               fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
               fontWeight = FontWeight(500),
               color = HabitColors.TextSecondary,
            )
         )
      }

   }

}