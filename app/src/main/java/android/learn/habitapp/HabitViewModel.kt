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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(private val habitRepository: HabitRepository) : ViewModel() {

   val _habitUiState = MutableStateFlow<UiState>(UiState.Loading)
   private val _searchQuery = MutableStateFlow("")
   val searchQuery = _searchQuery.asStateFlow()

   val habitsUiState = combine(
      habitRepository.getHabitsWithLogs(),
      _searchQuery
   ) { rawData, query ->
      val transformed = transformToUiState(rawData)
      if (query.isEmpty()) {
         UiState.Success(transformed)
      } else {
         UiState.Success(transformed.filter { it.name.contains(query, ignoreCase = true) })
      }
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   init {
      loadHabits()
   }
   private val _selectedHabit = MutableStateFlow(

      SelectedHabit(habitId = -1, bounds = Rect.Zero, expanded = false)
   )
   val selectedHabit = _selectedHabit.asStateFlow()

   fun selectHabit(habitId: Int, bounds: Rect) {
      _selectedHabit.value = SelectedHabit(habitId, bounds, false)
   }
   fun clearSelection() {
      // Just set expanded to false, DON'T reset the ID yet
      _selectedHabit.value = _selectedHabit.value.copy(expanded = false)
   }

   fun resetToIdle() {
      // ONLY call this when the animation is 100% finished
      _selectedHabit.value = SelectedHabit(habitId = -1, bounds = Rect.Zero, expanded = false)
   }
   fun onSearchQueryChange(newQuery: String) {
      _searchQuery.value = newQuery
   }
   private fun loadHabits() {
      viewModelScope.launch(Dispatchers.Default) {
         habitRepository.getHabitsWithLogs().collect { rawData ->
            _habitUiState.value = UiState.Success(transformToUiState(rawData))
         }
      }
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
            val uiState = _habitUiState.value as? UiState.Success ?: return@launch
            val habit = uiState.habits.find { it.id == habitId } ?: return@launch
            val today = getStartOfTodayTimestamp()

            if (habit.isDoneToday) {
               habitRepository.deleteHabitLog(habitId, today)
            } else {
               habitRepository.insertHabitLog(HabitLogsEntity(habitId = habitId, date = today))
            }
         } catch (e: Exception) {
            _habitUiState.value = UiState.Error("An Error occurred: $e")
         }


      }
   }

   fun onDeleteHabit(habitId: Int) {
      viewModelScope.launch(Dispatchers.IO) {

         habitRepository.deleteHabit(habitId = habitId)

      }
   }

   fun onAddHabit(name: String, emoji: String) {
      viewModelScope.launch(Dispatchers.IO) {
         habitRepository.insertHabit(
            HabitEntity(
               name = name,
               emoji = emoji
            )
         )
      }
   }


   fun onSaveHabit(habitId: Int, name: String, emoji: String) {
      viewModelScope.launch(Dispatchers.IO) {
         habitRepository.insertHabit(
            HabitEntity(
               id = habitId,
               name = name,
               emoji = emoji
            )
         )
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

   fun expandSelected() {
      _selectedHabit.value =  _selectedHabit.value.copy(expanded = true)
   }


}
fun getStartOfTodayTimestamp(): Long {
   return LocalDate.now(ZoneId.systemDefault())
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant()
      .toEpochMilli()
}