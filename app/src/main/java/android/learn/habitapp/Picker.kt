package android.learn.habitapp

import android.learn.habitapp.data.emoji.HabitEmoji
import android.learn.habitapp.data.emoji.HabitEmojiData
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.repository.HabitEmojiRepository
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HabitColorPalette = listOf(
   0xFFE57373.toInt(), // red
   0xFFFFB74D.toInt(), // orange
   0xFFFFF176.toInt(), // yellow
   0xFF81C784.toInt(), // green
   0xFF4FC3F7.toInt(), // light blue
   0xFF7986CB.toInt(), // indigo
   0xFFBA68C8.toInt(), // purple
   0xFFF06292.toInt(), // pink
)

@Composable
fun ColorPicker(
   selectedColor: Int?,
   onColorSelected: (Int?) -> Unit,
   modifier: Modifier = Modifier
) {
   Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      Text(
         "Color",
         style = MaterialTheme.typography.labelLarge,
         color = MaterialTheme.colorScheme.onSurfaceVariant,
         modifier = Modifier.padding(bottom = 8.dp)
      )

      LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
         item {
            // "None" option — resets to default theme color
            ColorSwatch(
               color = null,
               isSelected = selectedColor == null,
               onClick = { onColorSelected(null) }
            )
         }
         items(HabitColorPalette) { colorInt ->
            ColorSwatch(
               color = Color(colorInt),
               isSelected = selectedColor == colorInt,
               onClick = { onColorSelected(colorInt) }
            )
         }
      }
   }
}

@Composable
private fun ColorSwatch(
   color: Color?,
   isSelected: Boolean,
   onClick: () -> Unit
) {
   Box(
      modifier = Modifier
         .size(40.dp)
         .clip(CircleShape)
         .background(color ?: MaterialTheme.colorScheme.surfaceContainerHigh)
         .border(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = CircleShape
         )
         .clickable { onClick() },
      contentAlignment = Alignment.Center
   ) {
      if (color == null) {
         Icon(
            Icons.Outlined.Block,
            contentDescription = "No color",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
         )
      } else if (isSelected) {
         Icon(
            Icons.Default.Check,
            contentDescription = "Selected",
            modifier = Modifier.size(20.dp),
            tint = Color.White
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEmojiPickerSheet(
   onEmojiSelected: (String) -> Unit,
   onDismissRequest: () -> Unit,
   onCloseSheet: () -> Unit,
   sheetState: SheetState = rememberModalBottomSheetState()
) {
   var searchQuery by rememberSaveable {
      mutableStateOf("")
   }

   var selectedCategory by rememberSaveable {
      mutableStateOf("Fitness")
   }
   val categories = HabitEmojiData.categories

   val currentCategory = categories.first {
      it.name == selectedCategory
   }


   val IosSnappySpring = spring<Dp>(
      dampingRatio = 0.75f,      // Bouncy enough to feel alive, tight enough to remain professional
      stiffness = 600f          // Rapid acceleration towards the target size
   )
   ModalBottomSheet(
      modifier = Modifier,
      onDismissRequest = onDismissRequest,
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
   ) {
      Column(
         modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
      ) {
         Text(
            text = "Choose Habit Icon",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
         )
         val focusManager = LocalFocusManager.current

         LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
         ) {
            item("search") {
               val screenWidth = LocalConfiguration.current.screenWidthDp.dp

               var isFocused by remember { mutableStateOf(false) }

               val animatedWidth by animateDpAsState(
                  targetValue = if (isFocused) screenWidth * 0.6f else 120.dp,
                  animationSpec = IosSnappySpring, // Smooth 300ms transition
                  label = "SearchBarWidth"
               )
               // Track the scaling font size starting from a base value (e.g., 14sp or 16sp)
               var currentFontSize by remember { mutableFloatStateOf(14f) }
               // Reset text size when query clears or focus shifts to avoid getting permanently stuck small
               LaunchedEffect(searchQuery) {
                  if (searchQuery.isEmpty()) currentFontSize = 14f
               }

               BasicTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  modifier = Modifier
                     .height(40.dp)
                     .width(animatedWidth)
                     .onFocusChanged { isFocused = it.isFocused },
                  singleLine = true,
                  cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                  textStyle = TextStyle(
                     fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                     fontWeight = MaterialTheme.typography.displaySmall.fontWeight,
                     fontSize = currentFontSize.sp,                 // Dynamic Font Size
                     lineHeight = currentFontSize.sp,               // FORCE cursor to match text height
                     platformStyle = PlatformTextStyle(
                        includeFontPadding = false                 // Strips weird OS font padding misalignment
                     ),
                     color = MaterialTheme.colorScheme.onSurface
                  ),
                  decorationBox = { innerTextField ->
                     Row(
                        modifier = Modifier
                           .background(
                              color = if (isFocused) Color.Transparent else Color(0xFF615D6B),
                              shape = RoundedCornerShape(50)
                           )
                           .border(
                              width = if (isFocused) 1.dp else 0.dp,
                              color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                              shape = RoundedCornerShape(50)
                           )
                           .padding(horizontal = 12.dp)
                           .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                     ) {
                        Icon(
                           Icons.Default.Search,
                           contentDescription = null,
                           tint = if (isFocused) MaterialTheme.colorScheme.primary else Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                           modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart
                        ) {
                           if (searchQuery.isEmpty()) {
                              Text(
                                 text = "Search emojis", style = TextStyle(
                                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                                    fontWeight = MaterialTheme.typography.displaySmall.fontWeight,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = currentFontSize.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    color = Color.White.copy(alpha = 0.6f)
                                 )
                              )
                           }
                           innerTextField()
                        }
                     }
                  })
            }

            items(categories) { category ->
               FilterChip(selected = selectedCategory == category.name, onClick = {
                  focusManager.clearFocus()
                  selectedCategory = category.name
               }, label = { Text("${category.icon} ${category.name}") })
            }
         }
         AnimatedContent(targetState = selectedCategory) { category ->

            val filteredEmojis: List<HabitEmoji> = remember(
               category, searchQuery
            ) {
               val items = HabitEmojiRepository.getByCategory(selectedCategory)
               HabitEmojiRepository.search(searchQuery, items)
            }
            LazyVerticalGrid(
               modifier = Modifier
                  .height(300.dp)
                  .clickable(
                     interactionSource = remember { MutableInteractionSource() }, indication = null
                  ) {
                     focusManager.clearFocus()
                  },
               columns = GridCells.Adaptive(56.dp),
               horizontalArrangement = Arrangement.spacedBy(8.dp),
               verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

               items(
                  filteredEmojis,
                  key = { it.emoji + it.keywords.joinToString() },
               ) { emoji ->

                  FilledTonalIconButton(
                     onClick = {
                        onEmojiSelected(emoji.emoji)
                        onCloseSheet()
                     }) {
                     Text(
                        emoji.emoji, fontSize = 24.sp
                     )
                  }
               }
            }
         }
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPicker(
   reminderTime: LocalTime?,
   onReminderChange: (LocalTime?) -> Unit,
   canScheduleExactAlarms: () -> Boolean,      // ← new param
   onRequestExactAlarmPermission: () -> Unit,
   modifier: Modifier = Modifier
) {
   var showPicker by remember { mutableStateOf(false) }

   var showPermissionRequest by remember { mutableStateOf(false) }

   if (showPermissionRequest) {
      NotificationPermissionHandler { granted ->
         showPermissionRequest = false
         showPicker = true // open the time picker regardless of the result
      }
   }
   Row(
      modifier = modifier
         .fillMaxWidth()
         .padding(horizontal = 16.dp, vertical = 8.dp)
         .clickable { showPermissionRequest = true },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
   ) {
      Column {
         Text(
            "Reminder",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
         )
         Text(reminderTime?.let {
            it.format(DateTimeFormatter.ofPattern("h:mm a"))
         } ?: "Off", style = MaterialTheme.typography.bodyLarge)
      }
      if (reminderTime != null) {
         IconButton(onClick = { onReminderChange(null) }) {
            Icon(Icons.Default.Close, contentDescription = "Remove reminder")
         }
      }
   }

   if (showPicker) {
      val initialTime = reminderTime ?: LocalTime.of(9, 0)
      val timePickerState = rememberTimePickerState(
         initialHour = initialTime.hour, initialMinute = initialTime.minute, is24Hour = false
      )

      Dialog(onDismissRequest = { showPicker = false }) {
         Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
         ) {
            Column(
               modifier = Modifier.padding(24.dp),
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               TimePicker(state = timePickerState)
               Row(
                  modifier = Modifier
                     .fillMaxWidth()
                     .padding(top = 16.dp),
                  horizontalArrangement = Arrangement.End
               ) {
                  TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                  TextButton(onClick = {
                     onReminderChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                     showPicker = false

                     if (!canScheduleExactAlarms()) {
                        onRequestExactAlarmPermission()
                     }
                  }) { Text("Set") }
               }
            }
         }
      }
   }
}

@Composable
fun FrequencyPicker(
   frequencyType: FrequencyType,
   customDays: Set<DayOfWeek>,
   timesPerWeek: Int?,
   onFrequencyTypeChange: (FrequencyType) -> Unit,
   onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
   onTimesPerWeekChange: (Int) -> Unit,
   modifier: Modifier = Modifier
) {
   Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      Text(
         "Frequency",
         style = MaterialTheme.typography.labelLarge,
         color = MaterialTheme.colorScheme.onSurfaceVariant,
         modifier = Modifier.padding(bottom = 8.dp)
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
         FrequencyType.entries.forEach { type ->
            FilterChip(
               selected = frequencyType == type,
               onClick = { onFrequencyTypeChange(type) },
               label = { Text(type.label()) })
         }
      }

      AnimatedVisibility(visible = frequencyType == FrequencyType.SPECIFIC_DAYS) {
         DayOfWeekSelector(
            selectedDays = customDays, onDayToggle = { day ->
               onCustomDaysChange(
                  if (day in customDays) customDays - day else customDays + day
               )
            }, modifier = Modifier.padding(top = 12.dp)
         )
      }

      AnimatedVisibility(visible = frequencyType == FrequencyType.TIMES_PER_WEEK) {
         TimesPerWeekStepper(
            value = timesPerWeek ?: 1,
            onValueChange = onTimesPerWeekChange,
            modifier = Modifier.padding(top = 12.dp)
         )
      }
   }
}

private fun FrequencyType.label(): String = when (this) {
   FrequencyType.DAILY -> "Daily"
   FrequencyType.SPECIFIC_DAYS -> "Specific days"
   FrequencyType.TIMES_PER_WEEK -> "X per week"
}

@Composable
private fun DayOfWeekSelector(

   selectedDays: Set<DayOfWeek>, onDayToggle: (DayOfWeek) -> Unit, modifier: Modifier = Modifier
) {
   Row(
      modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
   ) {
      DayOfWeek.entries.forEach { day ->
         val selected = day in selectedDays
         Surface(
            onClick = { onDayToggle(day) },
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(40.dp)
         ) {
            Box(contentAlignment = Alignment.Center) {
               Text(
                  day.name.take(1), // M T W T F S S
                  color = if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.labelMedium
               )
            }
         }
      }
   }
}

@Composable
private fun TimesPerWeekStepper(
   value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier
) {
   Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
   ) {
      IconButton(onClick = { if (value > 1) onValueChange(value - 1) }) {
         Icon(Icons.Default.Remove, contentDescription = "Decrease")
      }
      Text(
         "$value ${if (value == 1) "time" else "times"} / week",
         style = MaterialTheme.typography.bodyLarge
      )
      IconButton(onClick = { if (value < 7) onValueChange(value + 1) }) {
         Icon(Icons.Default.Add, contentDescription = "Increase")
      }
   }
}
