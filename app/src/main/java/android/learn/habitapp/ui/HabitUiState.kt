package android.learn.habitapp.ui

import androidx.compose.ui.geometry.Rect
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


sealed class UiState {
   object Loading : UiState()
   data class Success(val habits: List<HabitUiState>) : UiState()
   data class Error(val message: String) : UiState()
}
@Serializable
data class HabitUiState (
   val id: Int,
   val name: String,
   val emoji: String,
   val isDoneToday: Boolean
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
