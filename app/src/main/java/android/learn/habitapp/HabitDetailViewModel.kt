package android.learn.habitapp

import android.content.Context
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.notifications.AlarmScheduler
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
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject


@HiltViewModel
class HabitDetailViewModel @Inject constructor(
   private val repository: HabitRepository,
   savedStateHandle: SavedStateHandle,
   private val alarmScheduler: AlarmScheduler,
) : ViewModel() {

   private val habitId: Int? = savedStateHandle.toRoute<HabitDetail>().habitId
   private val _uiState = MutableStateFlow(
      HabitUiState(
         id = -1, name = "", emoji = "🎯", isDoneToday = false
      )
   )
   val uiState = _uiState.asStateFlow()

   private val _uiStateHasChanged = MutableStateFlow(false)
   val uiStateHasChanged = _uiStateHasChanged.asStateFlow()

   private fun setUiStateHasChanged() {
      _uiStateHasChanged.value = true
   }

   fun resetUiStateHasChanged() {
      _uiStateHasChanged.value = false
   }

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
         val habitWithLogs = repository.loadHabitWithLogs(id)
         val habit = habitWithLogs.habit
         val today = getStartOfTodayTimestamp()

         val logDates = habitWithLogs.logs.map { it.date }
         _uiState.value = HabitUiState(
            id = habit.id,
            name = habit.name,
            emoji = habit.emoji,
            isDoneToday = habitWithLogs.logs.any { today == it.date },
            frequencyType = habit.frequencyType,
            customDays = habit.customDays?.split(",")?.filter { it.isNotBlank() }
               ?.map { DayOfWeek.valueOf(it) }?.toSet() ?: emptySet(),
            timesPerWeek = habit.timesPerWeek,
            reminderTime = habit.reminderTime?.let { LocalTime.parse(it) },
            color = habit.color,
            currentStreak = calculateCurrentStreak(logDates)
         )
      }
   }

   fun saveHabit(onCompleted: () -> Unit) {
      viewModelScope.launch(Dispatchers.IO) {
         val state = _uiState.value
         val entity =
            HabitEntity(
               id = if (state.id == -1) 0 else state.id, // 0 lets Room autogenerate on insert
               name = state.name,
               emoji = state.emoji,
               frequencyType = state.frequencyType,
               customDays = state.customDays.takeIf { it.isNotEmpty() }
                  ?.joinToString(",") { it.name },
               timesPerWeek = state.timesPerWeek,
               reminderTime = state.reminderTime?.toString(), // "HH:mm"
               color = state.color
            )
         when (state.id) {
            -1 -> {
               repository.insertHabit(entity) // needs insertHabit to return the generated id — see note
               state.reminderTime?.let {
                  alarmScheduler.schedule(
                     entity
                  )
               }
            }

            else -> {
               repository.updateHabit(entity)
               if (state.reminderTime != null) {
                  alarmScheduler.schedule(entity)
               } else {
                  alarmScheduler.cancel(state.id)
               }
            }
         }

         withContext(Dispatchers.Main) {
            onCompleted()
         }
      }
   }

   // HabitDetailViewModel
   fun archiveHabit(onCompleted: () -> Unit) {
      viewModelScope.launch {
         if (_uiState.value.id != -1) {
            repository.archiveHabit(_uiState.value.id)
         }
         withContext(Dispatchers.Main) { onCompleted() }
      }
   }

   private inline fun <T> MutableStateFlow<T>.customUpdate(
      function: (T) -> T
   ): Unit {
      this.update { function(it) }
      setUiStateHasChanged()
   }

   fun onNameChanged(name: String) {
      _uiState.customUpdate { it.copy(name = name) }
   }

   fun onEmojiChanged(emoji: String) {
      _uiState.customUpdate { it.copy(emoji = emoji) }

   }

   fun onFrequencyTypeChanged(type: FrequencyType) {
      _uiState.customUpdate { it.copy(frequencyType = type) }

   }

   fun onCustomDaysChanged(days: Set<DayOfWeek>) {
      _uiState.customUpdate { it.copy(customDays = days) }

   }

   fun onTimesPerWeekChanged(times: Int) {
      _uiState.customUpdate { it.copy(timesPerWeek = times) }

   }

   fun onReminderTimeChanged(time: LocalTime?) {
      _uiState.customUpdate { it.copy(reminderTime = time) }

   }

   // HabitDetailViewModel
   fun canScheduleExactAlarms(): Boolean = alarmScheduler.canScheduleExactAlarms()
   fun requestExactAlarmPermission(context: Context) =
      alarmScheduler.requestExactAlarmPermission(context)

}