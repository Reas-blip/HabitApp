package android.learn.habitapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.painterResource

// ─── Theme ───────────────────────────────────────────────────────────────────

val GreenDark    = Color(0xFF1B5E20)
val GreenMid     = Color(0xFF4CAF50)
val GreenLight   = Color(0xFFE8F5E9)
val GreenSurface = Color(0xFFF0F7F1)
val TextPrimary  = Color(0xFF111827)
val TextMuted    = Color(0xFF6B7280)
val CardWhite    = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFF3F4F6)

// ─── Data ─────────────────────────────────────────────────────────────────────

data class Habit(
   val id: Int,
   val name: String,
   val emoji: String,
   val category: String,
   val color: Color,
   val streak: Int,
   val completionPct: Int,
   val completed: Boolean = false,
)

val sampleHabits = listOf(
   Habit(1, "Read for 20 minutes", "📚", "Daily", Color(0xFF4CAF50), 12, 89, true),
   Habit(2, "Drink 2L of water",   "💧", "Daily", Color(0xFF2196F3),  8, 75, true),
   Habit(3, "Workout",             "🏋️", "Daily", Color(0xFFFF9800),  5, 65),
   Habit(4, "Meditate",            "🧘", "Daily", Color(0xFF9C27B0),  3, 48),
   Habit(5, "No sugar",            "🚫", "Daily", Color(0xFFF44336),  2, 40),
)

val weekDays   = listOf("M", "T", "W", "T", "F", "S", "S")
val weekDone   = listOf(true, true, true, true, false, true, false)
val chartData  = listOf(80, 90, 70, 100, 60, 85, 50)
val chartDays  = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

enum class Tab { TODAY, HABITS, STATS, MORE }

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun HabitTrackerApp() {
   var activeTab by remember { mutableStateOf(Tab.TODAY) }
   var habits by remember { mutableStateOf(sampleHabits) }

   val toggle = { id: Int ->
      habits = habits.map { if (it.id == id) it.copy(completed = !it.completed) else it }
   }

   Scaffold(
      containerColor = GreenSurface,
      bottomBar = { }
   ) { padding ->
      Box(Modifier.padding(padding)) {
         when (activeTab) {
            Tab.TODAY  -> TodayScreen(habits, toggle)
            Tab.HABITS -> HabitsScreen(habits, toggle)
            Tab.STATS  -> S1tatsScreen()
            Tab.MORE   -> MoreScreen()
         }
      }
   }
}

// ─── Bottom nav ──────────────────────────────────────────────────────────────


//
//@Composable
//fun BottomNavBar1(active: Tab, onSelect: (Tab) -> Unit) {
//   NavigationBar(containerColor = CardWhite, tonalElevation = 0.dp) {
//      navItems.forEach { item ->
//         val selected = item.tab == active
//         NavigationBarItem(
//            selected = selected,
//            onClick  = { onSelect(item.tab) },
//            icon     = {
//               Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                  Icon(
//                     if (selected) item.iconSelected else item.icon,
//                     contentDescription = item.label,
//                  )
//                  if (selected) {
//                     Spacer(Modifier.height(2.dp))
//                     Box(
//                        Modifier
//                           .size(4.dp)
//                           .clip(CircleShape)
//                           .background(GreenMid)
//                     )
//                  }
//               }
//            },
//            label    = { Text(item.label, fontSize = 10.sp) },
//            colors   = NavigationBarItemDefaults.colors(
//               selectedIconColor   = GreenDark,
//               selectedTextColor   = GreenDark,
//               unselectedIconColor = TextMuted,
//               unselectedTextColor = TextMuted,
//               indicatorColor      = Color.Transparent,
//            ),
//         )
//      }
//   }
//}

// ─── Circular Progress ───────────────────────────────────────────────────────

@Composable
fun CircularProgress(pct: Int, size: androidx.compose.ui.unit.Dp = 88.dp, strokeWidth: Float = 10f) {
   val animPct by animateFloatAsState(
      targetValue = pct / 100f,
      animationSpec = tween(800),
      label = "progress"
   )
   Box(contentAlignment = Alignment.Center) {
      Canvas(Modifier.size(size)) {
         val stroke = Stroke(strokeWidth, cap = StrokeCap.Round)
         drawArc(GreenLight, -90f, 360f, false, style = stroke)
         drawArc(GreenMid, -90f, 360f * animPct, false, style = stroke)
      }
   }
}

// ─── Today Screen ────────────────────────────────────────────────────────────

@Composable
fun TodayScreen(habits: List<Habit>, toggle: (Int) -> Unit) {
   val completed = habits.count { it.completed }
   val pct = (completed * 100 / habits.size)

   LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
   ) {
      // Header card
      item {
         Box(
            Modifier
               .fillMaxWidth()
               .clip(RoundedCornerShape(24.dp))
               .background(GreenDark)
               .padding(20.dp)
         ) {
            Column {
               Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Column {
                     Text("Good Morning! 🌅", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                     Text("Keep the momentum going!", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                  }
                  Box(
                     Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                     contentAlignment = Alignment.Center
                  ) {
                     Icon(Icons.Filled.Settings, null, tint = Color.White, modifier = Modifier.size(18.dp))
                  }
               }

               Spacer(Modifier.height(16.dp))
               Text("OVERALL PROGRESS", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
               Spacer(Modifier.height(12.dp))

               Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                  Box(contentAlignment = Alignment.Center) {
                     CircularProgress(pct, 84.dp)
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$pct%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("This Week", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                     }
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                     StatItem("${completed + 21}", "Completed")
                     StatItem("0", "In Progress")
                     StatItem("${habits.count { !it.completed } + 3}", "Skipped")
                  }
               }
            }
         }
      }

      // This Week
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column(Modifier.padding(16.dp)) {
               Text("THIS WEEK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
               Spacer(Modifier.height(12.dp))
               Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  weekDays.forEachIndexed { i, day ->
                     Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(day, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Box(
                           Modifier.size(32.dp).clip(CircleShape)
                              .background(if (weekDone[i]) GreenDark else Color(0xFFF3F4F6)),
                           contentAlignment = Alignment.Center
                        ) {
                           if (weekDone[i])
                              Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                           else
                              Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD1D5DB)))
                        }
                     }
                  }
               }
            }
         }
      }

      // Today's Habits header
      item {
         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Today's Habits", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            TextButton(onClick = {}) {
               Text("+ Add Habit", color = GreenDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
         }
      }

      // Habits list
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column {
               habits.forEachIndexed { i, habit ->
                  HabitRow(habit) { toggle(habit.id) }
                  if (i < habits.lastIndex) HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
               }
            }
         }
      }
   }
}

@Composable
fun StatItem(value: String, label: String) {
   Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
      Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
   }
}

// ─── Habits Screen ───────────────────────────────────────────────────────────

@Composable
fun HabitsScreen(habits: List<Habit>, toggle: (Int) -> Unit) {
   val completed = habits.count { it.completed }
   val pct = completed * 100 / habits.size

   LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
   ) {
      item {
         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Habits", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
               SmallIconButton(Icons.Filled.Search)
               SmallIconButton(Icons.Filled.MoreVert)
            }
         }
      }

      // Progress card
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column(Modifier.padding(16.dp)) {
               Text("Daily Progress", color = TextMuted, fontSize = 11.sp)
               Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                  Column {
                     Text("$pct%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                     Text("Great job! 😊", color = GreenMid, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                  }
                  Box(contentAlignment = Alignment.Center) {
                     CircularProgress(pct, 68.dp, 8f)
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$completed/${habits.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Done", color = TextMuted, fontSize = 9.sp)
                     }
                  }
               }
               Spacer(Modifier.height(12.dp))
               Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                  weekDone.forEach { done ->
                     Box(
                        Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                           .background(if (done) GreenMid else Color(0xFFF3F4F6))
                     )
                  }
               }
               Spacer(Modifier.height(4.dp))
               Row(Modifier.fillMaxWidth()) {
                  weekDays.forEach { d ->
                     Text(d, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 9.sp, color = TextMuted)
                  }
               }
            }
         }
      }

      // Habits list
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column {
               Row(
                  Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
               ) {
                  Text("Your Habits", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                  TextButton(onClick = {}) {
                     Icon(Icons.Filled.Add, null, Modifier.size(14.dp), tint = GreenDark)
                     Spacer(Modifier.width(2.dp))
                     Text("Add Habit", color = GreenDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }
               }
               habits.forEachIndexed { i, habit ->
                  HabitRow(habit) { toggle(habit.id) }
                  if (i < habits.lastIndex) HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
               }
            }
         }
      }
   }
}

@Composable
fun SmallIconButton(icon: ImageVector) {
   Box(
      Modifier.size(36.dp).clip(CircleShape).background(CardWhite),
      contentAlignment = Alignment.Center
   ) {
      Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
   }
}

@Composable
fun HabitRow(habit: Habit, onToggle: () -> Unit) {
   Row(
      Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
   ) {
      Box(
         Modifier.size(40.dp).clip(CircleShape).background(habit.color.copy(alpha = 0.12f)),
         contentAlignment = Alignment.Center
      ) {
         Text(habit.emoji, fontSize = 18.sp)
      }
      Column(Modifier.weight(1f)) {
         Text(
            habit.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (habit.completed) TextMuted else TextPrimary,
            textDecoration = if (habit.completed) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
         )
         Text(habit.category, fontSize = 11.sp, color = TextMuted)
      }
      Box(
         Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (habit.completed) GreenMid else Color.Transparent)
            .then(
               if (!habit.completed)
                  Modifier.background(Color.Transparent)
               else Modifier
            )
            .clickable(onClick = onToggle)
            .then(
               if (!habit.completed)
                  Modifier.background(Color(0xFFF3F4F6))
               else Modifier
            ),
         contentAlignment = Alignment.Center,
      ) {
         if (habit.completed)
            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
      }
   }
}

// ─── Stats Screen ────────────────────────────────────────────────────────────

@Composable
fun S1tatsScreen() {
   var range by remember { mutableStateOf("Week") }
   val ranges = listOf("Week", "Month", "Year")

   LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
   ) {
      item {
         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Stats", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
               Modifier.clip(CircleShape).background(CardWhite).padding(4.dp),
               horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
               ranges.forEach { r ->
                  val selected = r == range
                  Box(
                     Modifier
                        .clip(CircleShape)
                        .background(if (selected) GreenDark else Color.Transparent)
                        .clickable { range = r }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                     contentAlignment = Alignment.Center,
                  ) {
                     Text(r, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else TextMuted)
                  }
               }
            }
         }
      }

      // Overview
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column(Modifier.padding(16.dp)) {
               Text("OVERVIEW", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
               Spacer(Modifier.height(12.dp))
               Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                  OverviewStat("✅", "78%", "Success Rate")
                  OverviewStat("🔥", "12",  "Current Streak")
                  OverviewStat("🏆", "45",  "Longest Streak")
               }
            }
         }
      }

      // Bar chart
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column(Modifier.padding(16.dp)) {
               Text("HABIT COMPLETION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
               Spacer(Modifier.height(16.dp))
               Row(
                  Modifier.fillMaxWidth().height(120.dp),
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.SpaceBetween,
               ) {
                  chartData.forEachIndexed { i, value ->
                     val barColor = when {
                        value == 100 -> GreenDark
                        value >= 80  -> GreenMid
                        else         -> Color(0xFFA5D6A7)
                     }
                     val animH by animateFloatAsState(value / 100f, tween(600 + i * 50), label = "bar$i")
                     Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                           Modifier
                              .width(22.dp)
                              .height((100 * animH).dp)
                              .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                              .background(barColor)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(chartDays[i], fontSize = 9.sp, color = TextMuted)
                     }
                  }
               }
            }
         }
      }

      // Completion by habit
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
               Text("COMPLETION BY HABIT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
               sampleHabits.forEach { h ->
                  val animW by animateFloatAsState(h.completionPct / 100f, tween(800), label = "pct${h.id}")
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                     Box(
                        Modifier.size(28.dp).clip(CircleShape).background(h.color.copy(0.12f)),
                        contentAlignment = Alignment.Center
                     ) { Text(h.emoji, fontSize = 14.sp) }
                     Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                           Text(h.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                           Text("${h.completionPct}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFF3F4F6))) {
                           Box(Modifier.fillMaxWidth(animW).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(h.color))
                        }
                     }
                  }
               }
            }
         }
      }
   }
}

@Composable
fun OverviewStat(emoji: String, value: String, label: String) {
   Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Box(
         Modifier.size(40.dp).clip(CircleShape).background(GreenLight),
         contentAlignment = Alignment.Center
      ) { Text(emoji, fontSize = 18.sp) }
      Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
      Text(label, fontSize = 10.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
   }
}

// ─── More Screen ─────────────────────────────────────────────────────────────



@Composable
fun MoreScreen1() {
   LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
   ) {
      item { Text("More", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }

      // Profile
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Row(
               Modifier.fillMaxWidth().padding(16.dp),
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
               Box(
                  Modifier.size(48.dp).clip(CircleShape).background(GreenDark),
                  contentAlignment = Alignment.Center
               ) { Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
               Column(Modifier.weight(1f)) {
                  Text("Excellence Olenyi!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                  Text("Stay consistent, achieve more", fontSize = 11.sp, color = TextMuted)
               }
               Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFD1D5DB))
            }
         }
      }

      // Premium banner
      item {
         Box(
            Modifier
               .fillMaxWidth()
               .clip(RoundedCornerShape(20.dp))
               .background(Brush.horizontalGradient(listOf(GreenDark, GreenMid)))
               .padding(16.dp)
         ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
               Column(Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                     Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                     Text("Go Premium", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                  }
                  Text("Unlock advanced features and insights", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
               }
               Button(
                  onClick = {},
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  shape = CircleShape,
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
               ) {
                  Text("Upgrade", color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
               }
            }
         }
      }

      // Account menu
      item {
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
         ) {
            Column {
               Text(
                  "ACCOUNT",
                  Modifier.padding(start = 16.dp, top = 14.dp, bottom = 6.dp),
                  color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
               )
               moreMenuItems.forEachIndexed { i, item ->
                  Row(
                     Modifier
                        .fillMaxWidth()
                        .clickable {}
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.spacedBy(12.dp),
                  ) {
                     Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(item.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                     ) { Icon(painterResource(item.iconId), null, tint = item.color, modifier = Modifier.size(18.dp)) }
                     Column(Modifier.weight(1f)) {
                        Text(item.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                        Text(item.sub, fontSize = 11.sp, color = TextMuted)
                     }
                     Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(18.dp))
                  }
                  if (i < moreMenuItems.lastIndex) HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 64.dp))
               }
               Spacer(Modifier.height(4.dp))
            }
         }
      }

      // Privacy note
      item {
         Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp),
         ) {
            Icon(Icons.Filled.Shield, null, tint = TextMuted, modifier = Modifier.size(13.dp))
            Text("Your data is private and always protected.", color = TextMuted, fontSize = 11.sp)
         }
      }
   }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HabitTrackerPreview() {
   MaterialTheme { HabitTrackerApp() }
}
