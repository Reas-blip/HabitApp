package android.learn.habitapp.ui


sealed class UiState {
   object Loading : UiState()
   data class Success(val habits: List<HabitUiState>) : UiState()
   data class Error(val message: String) : UiState()
}
data class HabitUiState (
   val id: Int,
   val name: String,
   val iconName: String,
   val isDoneToday: Boolean
)