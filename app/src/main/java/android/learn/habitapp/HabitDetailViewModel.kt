
package android.learn.habitapp

import android.content.Context
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.navigation.HabitDetail
import android.learn.habitapp.notifications.AlarmScheduler
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.util.calculateCurrentStreak
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
import java.util.UUID
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
         id = -1, name = "", emoji = "🎯", isDoneToday = false, isArchived = false
      )
   )
   val uiState = _uiState.asStateFlow()

   private val _uiStateHasChanged = MutableStateFlow(false)
   val uiStateHasChanged = _uiStateHasChanged.asStateFlow()

   // Retain original habit reference to detect schedule edits and pass along groupId/createdAt
   private var originalHabit: HabitEntity? = null

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
      originalHabit = null
      _uiState.update { it.copy(id = -1, name = "", emoji = "🎯", isDoneToday = false, reminderTime = null) }
   }

   fun loadHabit(id: Int) {
      viewModelScope.launch(Dispatchers.IO) {
         val habitWithLogs = repository.loadHabitWithLogs(id)
         val habit = habitWithLogs.habit
         originalHabit = habit

         val today = getStartOfTodayTimestamp()
         val logDates = habitWithLogs.logs.map { it.date }

         _uiState.value = HabitUiState(
            id = habit.id,
            name = habit.name,
            emoji = habit.emoji,
            isArchived = habit.isArchived,
            sortOrder = habit.sortOrder,
            isDoneToday = habitWithLogs.logs.any { today == it.date },
            frequencyType = habit.frequencyType,
            customDays = habit.customDays?.split(",")?.filter { it.isNotBlank() }
               ?.map { DayOfWeek.valueOf(it) }?.toSet() ?: emptySet(),
            timesPerWeek = habit.timesPerWeek,
            reminderTime = habit.reminderTime?.let { LocalTime.parse(it) },
            color = habit.color,
            currentStreak = calculateCurrentStreak(logDates),
         )
      }
   }

   fun saveHabit(onCompleted: () -> Unit) {
      viewModelScope.launch(Dispatchers.IO) {
         val state = _uiState.value
         val formattedCustomDays = state.customDays
            .takeIf { it.isNotEmpty() }
            ?.joinToString(",") { it.name }

         val now = System.currentTimeMillis()

         if (state.id == -1) {
            // NEW HABIT: id = 0 lets Room autogenerate the primary key
            val newEntity = HabitEntity(
               id = 0,
               groupId = UUID.randomUUID().toString(),
               name = state.name,
               emoji = state.emoji,
               sortOrder = state.sortOrder,
               frequencyType = state.frequencyType,
               customDays = formattedCustomDays,
               timesPerWeek = state.timesPerWeek,
               reminderTime = state.reminderTime?.toString(),
               color = state.color,
               isArchived = false,
               isReplaced = false,
               createdAt = now
            )

            val generatedId = repository.insertHabit(newEntity).toInt()
            if (state.reminderTime != null) {
               alarmScheduler.schedule(newEntity.copy(id = generatedId))
            }
         } else {
            val orig = originalHabit
            val isFrequencyChanged = orig != null && (
                    orig.frequencyType != state.frequencyType ||
                            orig.customDays != formattedCustomDays ||
                            orig.timesPerWeek != state.timesPerWeek
                    )

            if (isFrequencyChanged ) {
               // 1. Mark current habit version as REPLACED
               val replacedOldEntity = orig.copy(
                  isReplaced = true,
                  replacedAt = now
               )
               repository.updateHabit(replacedOldEntity)
               alarmScheduler.cancel(orig.id)

               // 2. Insert new version carrying over the existing groupId
               val newVersionEntity = HabitEntity(
                  id = 0,
                  groupId = orig.groupId,
                  name = state.name,
                  emoji = state.emoji,
                  sortOrder = state.sortOrder,
                  frequencyType = state.frequencyType,
                  customDays = formattedCustomDays,
                  timesPerWeek = state.timesPerWeek,
                  reminderTime = state.reminderTime?.toString(),
                  color = state.color,
                  isArchived = false,
                  isReplaced = false,
                  createdAt = now
               )

               val generatedId = repository.insertHabit(newVersionEntity)
               if (state.reminderTime != null) {
                  alarmScheduler.schedule(newVersionEntity.copy(id = generatedId.toInt()))
               }
            } else {
               // COSMETIC UPDATE: Simple in-place update (Name, Emoji, Color, Reminder)
               val updatedEntity = HabitEntity(
                  id = state.id,
                  groupId = orig?.groupId ?: UUID.randomUUID().toString(),
                  name = state.name,
                  emoji = state.emoji,
                  sortOrder = state.sortOrder,
                  frequencyType = state.frequencyType,
                  customDays = formattedCustomDays,
                  timesPerWeek = state.timesPerWeek,
                  reminderTime = state.reminderTime?.toString(),
                  color = state.color,
                  isArchived = state.isArchived,
                  isReplaced = orig?.isReplaced ?: false,
                  replacedAt = orig?.replacedAt,
                  createdAt = orig?.createdAt ?: now
               )

               repository.updateHabit(updatedEntity)

               if (state.reminderTime != null) {
                  alarmScheduler.schedule(updatedEntity)
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

   fun archiveHabit(onCompleted: () -> Unit) {
      viewModelScope.launch {
         if (_uiState.value.id != -1) {
            repository.archiveHabit(_uiState.value.id)
            alarmScheduler.cancel(_uiState.value.id)
         }
         withContext(Dispatchers.Main) { onCompleted() }
      }
   }

   private inline fun <T> MutableStateFlow<T>.customUpdate(
      function: (T) -> T
   ) {
      this.update { function(it) }
      setUiStateHasChanged()
   }

   fun onNameChanged(name: String) {
      _uiState.customUpdate { it.copy(name = name) }
   }

   fun onColorChanged(color: Int?) {
      _uiState.customUpdate { it.copy(color = color) }
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

   fun canScheduleExactAlarms(): Boolean = alarmScheduler.canScheduleExactAlarms()

   fun requestExactAlarmPermission(context: Context) =
      alarmScheduler.requestExactAlarmPermission(context)
}
