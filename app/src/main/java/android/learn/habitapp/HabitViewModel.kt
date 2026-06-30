package android.learn.habitapp

import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.SelectedHabit
import android.learn.habitapp.ui.UiState
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// Simple sealed class to hold your events
sealed class UiEvent {
   data class ShowError(val message: String) : UiEvent()
}

@HiltViewModel
class HabitViewModel @Inject constructor(private val habitRepository: HabitRepository) :
   ViewModel() {

   private val _uiEvent = MutableSharedFlow<UiEvent>()
   val uiEvent = _uiEvent.asSharedFlow()

   // 1. The Source of Truth (Database-backed)
   // We update this ONLY when the database changes

   val habitUiState: StateFlow<UiState> = habitRepository.getHabitsWithLogs()
      .map { rawData ->
         UiState.Success(transformToUiState(rawData))
      }
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000), // Automatically stops listening if user leaves the screen
         initialValue = UiState.Loading // The default state while the database loads
      )

   // 2. The Search Query
   private val _searchQuery = MutableStateFlow("")
   val searchQuery = _searchQuery.asStateFlow()

   // 3. The Filtered View: filters according the searchQuery
   // This is derived automatically from the two inputs above.
   val filteredHabitUiState: StateFlow<UiState> = combine(
      habitUiState,
      _searchQuery
   ) { rawState, query ->
      if (rawState is UiState.Success) {
         val habits = rawState.habits
         if (query.isEmpty()) {
            UiState.Success(habits)
         } else {
            UiState.Success(habits.filter { it.name.contains(query, ignoreCase = true) })
         }
      } else {
         rawState // Pass through Loading or Error states
      }
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)



   fun onSearchQueryChange(newQuery: String) {
      _searchQuery.value = newQuery
   }

   suspend fun loadHabit(habitId: Int): HabitUiState = withContext(Dispatchers.IO) {
//      val habit = HabitEntity("")
      val habit = habitRepository.load(habitId)
      return@withContext HabitUiState(
         id = habit.id,
         name = habit.name,
         isDoneToday = true,
         emoji = habit.emoji
      )
   }

   fun onHabitChecked(habitId: Int) {
      viewModelScope.launch(Dispatchers.IO) {
         try {
            val uiState = habitUiState.value as? UiState.Success ?: return@launch
            val habit = uiState.habits.find { it.id == habitId } ?: return@launch
            val today = getStartOfTodayTimestamp()

            if (habit.isDoneToday) {
               habitRepository.deleteHabitLog(habitId, today)
            } else {
               habitRepository.insertHabitLog(HabitLogsEntity(habitId = habitId, date = today))
            }
         } catch (e: Exception) {
            _uiEvent.emit(UiEvent.ShowError("Could not update habit: ${e.message}"))
         }


      }
   }

   fun onDeleteHabit(habitId: Int) {
      viewModelScope.launch(Dispatchers.IO) {

         habitRepository.deleteHabit(habitId = habitId)

      }
   }

   private fun transformToUiState(habitWithLogs: List<HabitWithLogs>): List<HabitUiState> {
      val today = getStartOfTodayTimestamp()

      return habitWithLogs.map {
         val habit = it.habit
         HabitUiState(
            id = habit.id,
            name = habit.name,
            isDoneToday = it.logs.any { it.date == today },
            emoji = habit.emoji
         )
      }
   }

}

fun getStartOfTodayTimestamp(): Long {
   return LocalDate.now(ZoneId.systemDefault())
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant()
      .toEpochMilli()
}