package android.learn.habitapp

import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.data.local.Timeframe
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.ui.HabitUiState
import android.learn.habitapp.ui.UiState
import android.learn.habitapp.util.HabitBackupDto
import android.learn.habitapp.util.BackupPayload
import android.learn.habitapp.util.HabitStatsCalculator.calculateOverallCurrentStreak
import android.learn.habitapp.util.HabitStatsCalculator.calculateOverallLongestStreak
import android.learn.habitapp.util.HabitStatsCalculator.isScheduledToday
import android.learn.habitapp.util.HabitStatsCalculator.weeklyCompletionByDay
import android.learn.habitapp.util.backupJson
import android.learn.habitapp.util.calculateCurrentStreak
import android.learn.habitapp.util.getCurrentWeekStats
import android.learn.habitapp.util.getStatsForCurrent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

   // ── SINGLE SOURCE OF TRUTH ──────────────────────────────────────────
   // The only Room query for habit lists anywhere in this ViewModel.
   // Shared (via stateIn) so every downstream flow below reads from the
   // same one subscription instead of re-querying the database.
   private val rawHabitsWithLogs: StateFlow<List<HabitWithLogs>> =
      habitRepository.getAllHabitsRaw()
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

   // Active, non-archived, non-replaced — replaces every old getHabitsWithLogs() call.
   private val activeHabitsWithLogs: Flow<List<HabitWithLogs>> = rawHabitsWithLogs
      .map { list -> list.filter { !it.habit.isArchived && !it.habit.isReplaced } }

   // Archived, non-replaced — replaces getArchivedHabitsWithLogs().
   private val archivedHabitsWithLogs: Flow<List<HabitWithLogs>> = rawHabitsWithLogs
      .map { list -> list.filter { it.habit.isArchived && !it.habit.isReplaced } }

   // Not archived, but INCLUDES replaced versions — needed for accurate historical
   // stats across frequency changes (this is what getAllHabitsWithLogs() used to be).
   private val statsHabitsWithLogs: Flow<List<HabitWithLogs>> = rawHabitsWithLogs
      .map { list -> list.filter { !it.habit.isArchived } }

   // ── DERIVED UI STATE ─────────────────────────────────────────────────

   val habitUiState: StateFlow<UiState> = activeHabitsWithLogs
      .map { UiState.Success(transformToUiState(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   val archivedHabitUiState: StateFlow<UiState> = archivedHabitsWithLogs
      .map { UiState.Success(transformToUiState(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   private val _colorFilter = MutableStateFlow<Int?>(null)
   val colorFilter = _colorFilter.asStateFlow()

   val displayedHabitUiState: StateFlow<UiState> = combine(
      habitUiState, _colorFilter
   ) { rawState, filterColor ->
      if (rawState is UiState.Success) {
         val habits = rawState.habits
         if (filterColor == null) UiState.Success(habits)
         else UiState.Success(habits.filter { it.color == filterColor })
      } else rawState
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   private val _searchQuery = MutableStateFlow("")
   val searchQuery = _searchQuery.asStateFlow()

   val filteredHabitUiState: StateFlow<UiState> = combine(
      habitUiState, _searchQuery
   ) { rawState, query ->
      if (rawState is UiState.Success) {
         val habits = rawState.habits
         if (query.isEmpty()) UiState.Success(habits)
         else UiState.Success(habits.filter { it.name.contains(query, ignoreCase = true) })
      } else rawState
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   // "Today's habits" — same active source as habitUiState, just further filtered.
   // No repository dependency needed: shouldNotifyToday takes logs directly now.
   val habitUiStateForTodayDate: StateFlow<UiState> = activeHabitsWithLogs
      .map { list -> list.filter { isScheduledToday(it.habit) } }
      .map { UiState.Success(transformToUiState(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

   // ── STATS ────────────────────────────────────────────────────────────

   private val _selectedTimeframe = MutableStateFlow(Timeframe.WEEK)
   val selectedTimeframe = _selectedTimeframe.asStateFlow()

   fun onStatScreenTimeframeChange(timeframe: Timeframe) {
      _statScreenTimeframe.value = timeframe
   }

   val overviewStats: StateFlow<OverviewStats> = combine(
      statsHabitsWithLogs, _selectedTimeframe
   ) { habitsWithLogs, timeframe ->
      getStatsForCurrent(habitsWithLogs, timeframe)
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      OverviewStats(0, 0, 0, emptyList())
   )

   val todayScreenWeekStats: StateFlow<OverviewStats> = activeHabitsWithLogs
      .map { habitsWithLogs -> getCurrentWeekStats(habitsWithLogs) }
      .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(5000),
         OverviewStats(0, 0, 0, emptyList())
      )

   val habitScreenDayStats: StateFlow<OverviewStats> = activeHabitsWithLogs
      .map { habitsWithLogs -> getStatsForCurrent(habitsWithLogs, Timeframe.DAY) }
      .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(5000),
         OverviewStats(0, 0, 0, emptyList())
      )

   val weeklyCompletionByDay: StateFlow<Map<DayOfWeek, Boolean?>> = statsHabitsWithLogs
      .map { habitsWithLogs -> weeklyCompletionByDay(habitsWithLogs) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

   val streakPair: StateFlow<Pair<Int, Int>> = statsHabitsWithLogs
      .map { habitsWithLogs ->
         calculateOverallCurrentStreak(habitsWithLogs) to calculateOverallLongestStreak(
            habitsWithLogs
         )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0 to 0)

   private val _statScreenTimeframe = MutableStateFlow(Timeframe.WEEK)
   val statScreenTimeframe = _statScreenTimeframe.asStateFlow()
   val statScreenOverviewStats: StateFlow<OverviewStats> = combine(
      statsHabitsWithLogs, _statScreenTimeframe
   ) { habitsWithLogs, timeframe ->
      getStatsForCurrent(habitsWithLogs, timeframe)
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      OverviewStats(0, 0, 0, emptyList())
   )


   // ── ACTIONS ──────────────────────────────────────────────────────────

   private val _scrollToHabitId = MutableStateFlow<Int?>(null)
   val scrollToHabitId = _scrollToHabitId.asStateFlow()

   val hasSeenSwipeHint: StateFlow<Boolean> = habitRepository.hasSeenSwipeHint
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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
            val today = getStartOfTodayTimestamp()
            val isDoneToday = habitRepository.getLogCountForDate(habitId, today) > 0

            if (isDoneToday) {
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

   // ── BACKUP & RESTORE ───────────────────────────────────────────────
   // Suspend (not fire-and-forget like the actions above) because the
   // caller needs the JSON text back to write it to a file it owns —
   // the ViewModel has no Context/Uri access to do that part itself.

   /** Snapshot of every habit — active, archived, and every replaced
    * frequency version — so a restore reconstructs full history, not
    * just what's currently visible. */
   suspend fun exportBackupJson(): String {
      val allHabits = habitRepository.getAllHabitsRaw().first()
      val dtos = allHabits.map { habitWithLogs ->
         val habit = habitWithLogs.habit
         HabitBackupDto(
            groupId = habit.groupId,
            name = habit.name,
            emoji = habit.emoji,
            frequencyType = habit.frequencyType.name,
            customDays = habit.customDays,
            timesPerWeek = habit.timesPerWeek,
            reminderTime = habit.reminderTime,
            color = habit.color,
            sortOrder = habit.sortOrder,
            isArchived = habit.isArchived,
            isReplaced = habit.isReplaced,
            replacedAt = habit.replacedAt,
            createdAt = habit.createdAt,
            logDates = habitWithLogs.logs.map { it.date }
         )
      }
      val payload = BackupPayload(exportedAt = System.currentTimeMillis(), habits = dtos)
      return backupJson.encodeToString(payload)
   }

   /** Adds every habit from the backup as a NEW row — never reuses the
    * backup's original id, since insertHabit uses OnConflictStrategy.REPLACE
    * and an id collision would silently overwrite an existing habit. This
    * is always an additive merge, never a wipe-and-replace. Returns the
    * number of habits imported; throws on malformed JSON so the caller
    * (which owns the UI) can show what went wrong. */
   suspend fun importBackupJson(json: String): Int {
      val payload = backupJson.decodeFromString<BackupPayload>(json)
      payload.habits.forEach { dto ->
         val newId = habitRepository.insertHabit(
            HabitEntity(
               id = 0,
               groupId = dto.groupId,
               name = dto.name,
               emoji = dto.emoji,
               frequencyType = FrequencyType.valueOf(dto.frequencyType),
               customDays = dto.customDays,
               timesPerWeek = dto.timesPerWeek,
               reminderTime = dto.reminderTime,
               color = dto.color,
               sortOrder = dto.sortOrder,
               isArchived = dto.isArchived,
               isReplaced = dto.isReplaced,
               replacedAt = dto.replacedAt,
               createdAt = dto.createdAt
            )
         )
         dto.logDates.forEach { logDate ->
            habitRepository.insertHabitLog(HabitLogsEntity(habitId = newId.toInt(), date = logDate))
         }
      }
      return payload.habits.size
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