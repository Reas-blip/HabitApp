package android.learn.habitapp

import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.data.local.Timeframe
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.util.FrequencyEvaluator.shouldNotifyToday
import android.learn.habitapp.util.HabitStatsCalculator.weeklyCompletionByDay
import android.learn.habitapp.util.calculateCurrentStreak
import android.learn.habitapp.util.calculateOverallCurrentStreak
import android.learn.habitapp.util.calculateOverallLongestStreak
import android.learn.habitapp.util.getCurrentWeekStats
import android.learn.habitapp.util.getStatsForCurrent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

sealed class UiEvent {
   data class ShowError(val message: String) : UiEvent()
}

@HiltViewModel
class HabitViewModel @Inject constructor(private val habitRepository: HabitRepository) :
   ViewModel() {

   private val _uiEvent = MutableSharedFlow<UiEvent>()
   val uiEvent = _uiEvent.asSharedFlow()

   val archivedHabitUiState: StateFlow<UiState> =
      habitRepository.getArchivedHabitsWithLogs().map { rawData ->
         UiState.Success(transformToUiState(rawData))
      }.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = UiState.Loading
      )

   private val _colorFilter = MutableStateFlow<Int?>(null)
   val colorFilter = _colorFilter.asStateFlow()
   val habitUiState: StateFlow<UiState> = habitRepository.getHabitsWithLogs().map { rawData ->
      UiState.Success(transformToUiState(rawData))
   }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading
   )

   val displayedHabitUiState: StateFlow<UiState> = combine(
      habitUiState, _colorFilter
   ) { rawState, filterColor ->
      if (rawState is UiState.Success) {
         val habits = rawState.habits
         if (filterColor == null) {
            UiState.Success(habits)
         } else {
            UiState.Success(habits.filter { it.color == filterColor })
         }
      } else {
         rawState
      }
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)


   private val _searchQuery = MutableStateFlow("")
   val searchQuery = _searchQuery.asStateFlow()

   val filteredHabitUiState: StateFlow<UiState> = combine(
      habitUiState, _searchQuery
   ) { rawState, query ->
      if (rawState is UiState.Success) {
         val habits = rawState.habits
         if (query.isEmpty()) {
            UiState.Success(habits)
         } else {
            UiState.Success(habits.filter { it.name.contains(query, ignoreCase = true) })
         }
      } else {
         rawState
      }
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   val habitUiStateForTodayDate: StateFlow<UiState> =
      habitRepository.getHabitsWithLogs().map { rawData ->
         UiState.Success(transformToUiState(rawData.filter { habitWithLogs ->
            shouldNotifyToday(habitWithLogs.habit, habitRepository)
         }))
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   private val _selectedTimeframe = MutableStateFlow(Timeframe.WEEK)
   val selectedTimeframe = _selectedTimeframe.asStateFlow()

   fun onTimeframeChanged(timeframe: Timeframe) {
      _selectedTimeframe.value = timeframe
   }

   val overviewStats: StateFlow<OverviewStats> = combine(
      habitRepository.getAllHabitsWithLogs(), // the raw List<HabitWithLogs> Flow
      _selectedTimeframe
   ) { habitsWithLogs, timeframe ->
      getStatsForCurrent(habitsWithLogs, timeframe)
   }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = OverviewStats(
         0,
         0,
         0,
         emptyList()
      ) // adjust to your actual OverviewStats constructor
   )

   val hasSeenSwipeHint: StateFlow<Boolean> = habitRepository.hasSeenSwipeHint.stateIn(
      viewModelScope, SharingStarted.WhileSubscribed(5000), true
   )
   private val _scrollToHabitId = MutableStateFlow<Int?>(null)
   val scrollToHabitId = _scrollToHabitId.asStateFlow()

   val todayScreenWeekStats: StateFlow<OverviewStats> = habitRepository.getHabitsWithLogs()
      .map { habitsWithLogs -> getCurrentWeekStats(habitsWithLogs) }
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = OverviewStats(0, 0, 0, emptyList())
      )
   val habitScreenDayStats: StateFlow<OverviewStats> = habitRepository.getHabitsWithLogs()
      .map { habitsWithLogs -> getStatsForCurrent(habitsWithLogs, timeframe = Timeframe.DAY) }
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = OverviewStats(0, 0, 0, emptyList())
      )

   val weeklyCompletionByDay: StateFlow<Map<DayOfWeek, Boolean>> =
      habitRepository.getHabitsWithLogs()
         .map { habitsWithLogs -> weeklyCompletionByDay(habitsWithLogs) }
         .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
         )

   val streakPair: StateFlow<Pair<Int, Int>> = habitRepository.getAllHabitsWithLogs()
      .map { habitsWithLogs ->
         val currentStreak = calculateOverallCurrentStreak(habitsWithLogs)
         val longestStreak = calculateOverallLongestStreak(habitsWithLogs)
         currentStreak to longestStreak
      }
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = 0 to 0
      )


   private val _statScreenTimeframe = MutableStateFlow(Timeframe.WEEK)
   val statScreenOverviewStats: StateFlow<OverviewStats> = habitRepository.getAllHabitsWithLogs()
      .map{ habitWithLogs ->
         getStatsForCurrent(habitWithLogs, _statScreenTimeframe.value)

      }.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = OverviewStats(0, 0, 0, emptyList())
      )

   fun onStatScreenTimeframeChange(timeframe: Timeframe) {
      _statScreenTimeframe.value = timeframe
   }

   fun onColorFilterChanged(color: Int?) {

      _colorFilter.value = if (_colorFilter.value == color) null else color
   }

   fun requestScrollTo(habitId: Int) {
      _scrollToHabitId.value = habitId
   }

   fun onScrollHandled() {
      _scrollToHabitId.value = null
   }

   fun markSwipeHintSeen() {
      viewModelScope.launch { habitRepository.setSwipeHintSeen() }
   }

   fun onSearchQueryChange(newQuery: String) {
      _searchQuery.value = newQuery
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

   fun onHabitsReordered(idsInOrder: List<Int>) {
      viewModelScope.launch {
         habitRepository.updateSortOrders(idsInOrder)
      }
   }

   fun onArchiveHabit(habitId: Int) {
      viewModelScope.launch {
         habitRepository.archiveHabit(habitId)
      }
   }

   fun onUndoArchive(habitId: Int) {
      viewModelScope.launch {
         habitRepository.unarchiveHabit(habitId)
      }
   }

   fun onDeleteHabit(habitId: Int) {
      viewModelScope.launch(Dispatchers.IO) {

         habitRepository.deleteHabit(habitId = habitId)

      }
   }

   private fun transformToUiState(habitWithLogs: List<HabitWithLogs>): List<HabitUiState> {
      val today = getStartOfTodayTimestamp()

      return habitWithLogs.map { habitWithLogs ->
         val habit = habitWithLogs.habit
         val logDates = habitWithLogs.logs.map { log -> log.date }
         HabitUiState(
            id = habit.id,
            name = habit.name,
            emoji = habit.emoji,
            isArchived = habit.isArchived,
            isDoneToday = habitWithLogs.logs.any { today == it.date },
            sortOrder = habit.sortOrder,
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

}

fun getStartOfTodayTimestamp(): Long {
   return LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant()
      .toEpochMilli()
}