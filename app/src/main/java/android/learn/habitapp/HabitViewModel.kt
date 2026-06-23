package android.learn.habitapp

import android.R.attr.action
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
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
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(private val habitRepository: HabitRepository) : ViewModel() {

   val _habitUiState = MutableStateFlow<UiState>(UiState.Loading)
   private val _searchQuery = MutableStateFlow("")
   val searchQuery = _searchQuery.asStateFlow()

   val habitsUiState = combine(
      habitRepository.getHabitWithLogs(),
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

   fun onSearchQueryChange(newQuery: String) {
      _searchQuery.value = newQuery
   }
   private fun loadHabits() {
      viewModelScope.launch(Dispatchers.IO) {
         habitRepository.getHabitWithLogs().collect { rawData ->
            _habitUiState.value = UiState.Success(transformToUiState(rawData))
         }
      }
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

   fun onAddHabit(name: String, iconName: String) {
      viewModelScope.launch(Dispatchers.IO) {
         habitRepository.insertHabit(
            HabitEntity(
               name = name,
               iconName = iconName
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
            iconName = habit.iconName
         )
      }
   }

   fun getStartOfTodayTimestamp(): Long {
      return LocalDate.now(ZoneId.systemDefault())
         .atStartOfDay(ZoneId.systemDefault())
         .toInstant()
         .toEpochMilli()
   }


}