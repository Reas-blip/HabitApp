package android.learn.habitapp.util

import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.data.local.Timeframe
import java.time.LocalDate
import java.time.ZoneId

// Get stats specifically for TODAY
fun getTodayStats(allHabitsWithLogs: List<HabitWithLogs>): OverviewStats {
   return HabitStatsCalculator.calculateStats(
      allHabitsWithLogs = allHabitsWithLogs,
      timeframe = Timeframe.DAY,
      referenceDate = LocalDate.now(ZoneId.systemDefault())
   )
}

// Get stats specifically for THIS WEEK (Mon - Sun)
fun getCurrentWeekStats(allHabitsWithLogs: List<HabitWithLogs>): OverviewStats {
   return HabitStatsCalculator.calculateStats(
      allHabitsWithLogs = allHabitsWithLogs,
      timeframe = Timeframe.WEEK,
      referenceDate = LocalDate.now(ZoneId.systemDefault())
   )
}

// Generic helper function for any target timeframe today
fun getStatsForCurrent(
   allHabitsWithLogs: List<HabitWithLogs>,
   timeframe: Timeframe
): OverviewStats {
   return HabitStatsCalculator.calculateStats(
      allHabitsWithLogs = allHabitsWithLogs,
      timeframe = timeframe,
      referenceDate = LocalDate.now(ZoneId.systemDefault())
   )
}
