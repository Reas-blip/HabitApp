package android.learn.habitapp.ui

import android.learn.habitapp.data.local.FrequencyType
import androidx.compose.ui.geometry.Rect
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime


sealed class UiState {
   object Loading : UiState()
   data class Success(val habits: List<HabitUiState>) : UiState()
   data class Error(val message: String) : UiState()
}
@Serializable
data class HabitUiState(
   val id: Int,
   val name: String,
   val emoji: String,
   val isDoneToday: Boolean,
   val frequencyType: FrequencyType = FrequencyType.DAILY,
   val customDays: Set<DayOfWeek> = emptySet(),
   val timesPerWeek: Int? = null,
   @Serializable(with = LocalTimeSerializer::class)
   val reminderTime: LocalTime? = null,
   val color: Int? = null,
   val currentStreak: Int = 0
)
@Serializable
data class SelectedHabit(
   val habitId: Int,
   @Contextual
   val bounds: Rect,
   val expanded: Boolean
)
enum class CardState {
   COMPACT, EXPANDED
}
