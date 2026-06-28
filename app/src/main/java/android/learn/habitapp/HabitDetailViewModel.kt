package android.learn.habitapp

import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.ui.HabitUiState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HabitDetailViewModel @Inject constructor(
   private val repository: HabitRepository,
   savedStateHandle: SavedStateHandle
) : ViewModel() {

   private val habitId: Int? = savedStateHandle.toRoute<HabitDetail>().habitId
   private val _uiState = MutableStateFlow(
      HabitUiState(
         id = -1,
         name = "",
         emoji = "🎯",
         isDoneToday = false
      )
   )
   val uiState = _uiState.asStateFlow()

   init {
      if (habitId != null) {
         loadHabit(habitId)
      } else {
         newHabit()
      }
   }

   fun newHabit() {
      _uiState.update { it.copy(id = -1, name = "", emoji = "🎯", isDoneToday = false) }
   }

   fun loadHabit(id: Int) {
      viewModelScope.launch(Dispatchers.IO) {
         val habit = repository.loadHabitWithLogs(id)

         habit.collect { habitWithLogs ->
            val habit = habitWithLogs.habit
            val today: Long = getStartOfTodayTimestamp()
            _uiState.value = HabitUiState(
               name = habit.name,
               id = habit.id,
               emoji = habit.emoji,
               isDoneToday = habitWithLogs.logs.any { today == it.date },
            )
         }
      }
   }

   fun onNameChanged(name: String) {
      _uiState.update {
         it.copy(name = name)
      }
   }

   fun onEmojiChanged(emoji: String) {
      _uiState.update {
         it.copy(emoji = emoji)
      }
   }

   fun saveHabit() {
      viewModelScope.launch(Dispatchers.IO) {
         repository.insertHabit(
            when (_uiState.value.id) {
               -1 -> {
                  HabitEntity(
                     name = _uiState.value.name,
                     emoji = _uiState.value.emoji
                  )
               }

               else -> {
                  HabitEntity(
                     id = _uiState.value.id,
                     name = _uiState.value.name,
                     emoji = _uiState.value.emoji
                  )
               }

            }

         )
      }
   }
}