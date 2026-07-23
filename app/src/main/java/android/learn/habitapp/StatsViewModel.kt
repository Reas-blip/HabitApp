
package android.learn.habitapp.ui.stats

import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.data.local.Timeframe
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.util.HabitStatsCalculator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class StatsUiState(
   val selectedTimeframe: Timeframe = Timeframe.WEEK,
   val referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
   val stats: OverviewStats? = null,
   val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
   private val repository: HabitRepository
) : ViewModel() {

   private val _selectedTimeframe = MutableStateFlow(Timeframe.WEEK)
   private val _referenceDate = MutableStateFlow(LocalDate.now(ZoneId.systemDefault()))

   // Reactively recompute stats whenever data, timeframe, or date changes
   val uiState: StateFlow<StatsUiState> = combine(
      repository.getAllHabitsWithLogs(), // Flow<List<HabitWithLogs>>
      _selectedTimeframe,
      _referenceDate
   ) { allHabitsWithLogs, timeframe, refDate ->

      val overviewStats = HabitStatsCalculator.calculateStats(
         allHabitsWithLogs = allHabitsWithLogs,
         timeframe = timeframe,
         referenceDate = refDate
      )

      StatsUiState(
         selectedTimeframe = timeframe,
         referenceDate = refDate,
         stats = overviewStats,
         isLoading = false
      )
   }
      .flowOn(Dispatchers.Default) // Perform calculation off the UI thread
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5_000),
         initialValue = StatsUiState()
      )

   fun onTimeframeSelected(timeframe: Timeframe) {
      _selectedTimeframe.value = timeframe
   }

   fun onPreviousDate() {
      _referenceDate.value = when (_selectedTimeframe.value) {
         Timeframe.DAY -> _referenceDate.value.minusDays(1)
         Timeframe.WEEK -> _referenceDate.value.minusWeeks(1)
         Timeframe.MONTH -> _referenceDate.value.minusMonths(1)
         Timeframe.YEAR -> _referenceDate.value.minusYears(1)
      }
   }

   fun onNextDate() {
      _referenceDate.value = when (_selectedTimeframe.value) {
         Timeframe.DAY -> _referenceDate.value.plusDays(1)
         Timeframe.WEEK -> _referenceDate.value.plusWeeks(1)
         Timeframe.MONTH -> _referenceDate.value.plusMonths(1)
         Timeframe.YEAR -> _referenceDate.value.plusYears(1)
      }
   }
}
