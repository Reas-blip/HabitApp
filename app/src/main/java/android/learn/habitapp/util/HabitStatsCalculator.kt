package android.learn.habitapp.util

import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.local.HabitStatItem
import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.data.local.Timeframe
import android.learn.habitapp.getStartOfTodayTimestamp
import android.learn.habitapp.util.FrequencyEvaluator.parseCustomDays
import android.learn.habitapp.util.HabitStatsCalculator.getDateRange
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.collections.first

enum class DayStatus { Completed, Missed, NotApplicable }
object HabitStatsCalculator {

   fun isScheduledForToday(habit: HabitEntity, logs: List<HabitLogsEntity>, today: LocalDate = LocalDate.now(ZoneId.systemDefault())): Boolean {
      val logDates = logs.map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
      return when (habit.frequencyType) {
         FrequencyType.DAILY -> true
         FrequencyType.SPECIFIC_DAYS -> today.dayOfWeek in parseCustomDays(habit)
         FrequencyType.TIMES_PER_WEEK -> {
            val (weekStart, _) = getDateRange(Timeframe.WEEK, today)
            logDates.count { !it.isBefore(weekStart) && it.isBefore(today) } < (habit.timesPerWeek ?: 1)
         }
      }
   }

   fun weeklyCompletionByDay(
      habitsWithLogs: List<HabitWithLogs>,
      weekStart: LocalDate = LocalDate.now(ZoneId.systemDefault()).with(DayOfWeek.MONDAY)
   ): Map<DayOfWeek, Boolean> {
      val weekEnd = weekStart.plusDays(6)

      return DayOfWeek.entries.associateWith { day ->
         val date = weekStart.with(day)
         // "complete" for this day = every applicable habit was done (or no habits were due)
         habitsWithLogs
            .filter { it.habit.frequencyType != FrequencyType.TIMES_PER_WEEK } // excluded, per above
            .all { habitWithLogs ->
               val logDates = habitWithLogs.logs.map { it.date }.toSet()
               val status = HabitStatsCalculator.evaluateRange(
                  habitWithLogs.habit, logDates, date, date
               ).first()
               status != DayStatus.Missed // NotApplicable or Completed both count as "not a failure"
            }
      }
   }

   /**
    * For a single habit, checks a date range and classifies each date as:
    * - not applicable (habit didn't exist yet, or wasn't due that day per frequency)
    * - completed (a log exists)
    * - missed (was due, habit existed, but no log)
    */
   fun evaluateRange(
      habit: HabitEntity,
      logDates: Set<Long>, // the habit's log dates, as epoch-day-start millis
      startDate: LocalDate,
      endDate: LocalDate
   ): List<DayStatus> {
      val createdDate = Instant.ofEpochMilli(habit.createdAt)
         .atZone(ZoneId.systemDefault()).toLocalDate()

      val results = mutableListOf<DayStatus>()
      var current = startDate

      while (!current.isAfter(endDate)) {
         val dateMillis = current.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

         val status = when {
            current.isBefore(createdDate) -> DayStatus.NotApplicable
            !isDueOn(habit, current) -> DayStatus.NotApplicable
            dateMillis in logDates -> DayStatus.Completed
            else -> DayStatus.Missed
         }
         results.add(status)
         current = current.plusDays(1)
      }
      return results
   }

   private fun isDueOn(habit: HabitEntity, date: LocalDate): Boolean {
      return when (habit.frequencyType) {
         FrequencyType.DAILY -> true
         FrequencyType.SPECIFIC_DAYS -> {
            val allowedDays = FrequencyEvaluator.parseCustomDays(habit)
            date.dayOfWeek in allowedDays
         }

         FrequencyType.TIMES_PER_WEEK -> true
         // TIMES_PER_WEEK has no fixed days — every day is "eligible," but whether
         // it counts as a genuine miss is fuzzier (see note below)
      }
   }

   /** Missed count — only counts days from the timeframe's start up through TODAY,
    *  never future days within the same period, so it doesn't falsely count
    *  "hasn't happened yet" as "missed." */
   private fun getMissedCount(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframe: Timeframe,
      referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): Int {
      val (start, end) = getDateRange(timeframe, referenceDate)
      val clippedEnd = minOf(
         end,
         referenceDate.minusDays(1)
      ) // never look past today, even if the timeframe extends further

      if (clippedEnd.isBefore(start)) return 0 // timeframe hasn't started yet relative to referenceDate

      val stats = calculateStatsForRange(allHabitsWithLogs, start, clippedEnd)
      return stats.grandTotalDue - stats.grandTotalCompleted
   }

// HabitStatsHelpers.kt

   /** How many habit-completions are still outstanding from TODAY through
    *  the end of the given timeframe (e.g. "12 things left to do this month"). */
   private fun getRemainingDueForTimeframe(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframe: Timeframe,
      referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): Int {
      val (start, end) = HabitStatsCalculator.getDateRange(timeframe, referenceDate)
      val clippedStart = maxOf(start, referenceDate) // never look before today

      if (clippedStart.isAfter(end)) return 0 // timeframe already fully in the past relative to referenceDate

      val stats = HabitStatsCalculator.calculateStatsForRange(allHabitsWithLogs, clippedStart, end)
      return stats.grandTotalDue - stats.grandTotalCompleted
   }

   fun calculateStats(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframe: Timeframe,
      referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): OverviewStats {
      val latestVersionsOfHabitsWithLogs = allHabitsWithLogs.filter { it.habit.isReplaced == false }

      val missedHabits = getMissedCount(
         allHabitsWithLogs = latestVersionsOfHabitsWithLogs,
         timeframe = timeframe,
      )

      val remainingDueForTimeframe = getRemainingDueForTimeframe(
         allHabitsWithLogs = latestVersionsOfHabitsWithLogs,
         timeframe = timeframe,
      )

      val (start, end) = getDateRange(timeframe, referenceDate)
      val clippedEnd = minOf(end, referenceDate)
      return calculateStatsForRange(allHabitsWithLogs, start, clippedEnd)
         .copy(
            missedCount = missedHabits,
            remainingDue = remainingDueForTimeframe
         )
   }

   /** Same logic as before, but takes an explicit range instead of deriving one from a Timeframe. */
   fun calculateStatsForRange(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframeStart: LocalDate,
      timeframeEnd: LocalDate
   ): OverviewStats {
      val groupedByFamily = allHabitsWithLogs.groupBy { it.habit.groupId }


      var grandTotalDue = 0
      var grandTotalCompleted = 0

      val habitStatItems = groupedByFamily.map { (groupId, versions) ->
         val latestVersion = versions.maxByOrNull { it.habit.createdAt }!!.habit


         val timesPerWeekVersions =
            versions.filter { it.habit.frequencyType == FrequencyType.TIMES_PER_WEEK }
         val otherVersions =
            versions.filter { it.habit.frequencyType != FrequencyType.TIMES_PER_WEEK }

         var familyDue = 0
         var familyCompleted = 0

         otherVersions.forEach { item ->
            val habit = item.habit
            val habitStart = Instant.ofEpochMilli(habit.createdAt)
               .atZone(ZoneId.systemDefault()).toLocalDate()
            val habitEnd = habit.replacedAt?.let {
               Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            } ?: timeframeEnd

            val effectiveStart = maxOf(timeframeStart, habitStart)
            val effectiveEnd = minOf(timeframeEnd, habitEnd)

            if (!effectiveStart.isAfter(effectiveEnd)) {
               val (due, completed) = calculateDayBasedCounts(item, effectiveStart, effectiveEnd)
               familyDue += due
               familyCompleted += completed
            }
         }

         if (timesPerWeekVersions.isNotEmpty()) {
            val (due, completed) = calculateTimesPerWeekFamilyCounts(
               versions = timesPerWeekVersions,
               startDate = timeframeStart,
               endDate = timeframeEnd
            )
            familyDue += due
            familyCompleted += completed
         }

         grandTotalDue += familyDue
         grandTotalCompleted += familyCompleted

         val percent =
            if (familyDue == 0) 0 else ((familyCompleted.toFloat() / familyDue) * 100).toInt()

         HabitStatItem(
            habitId = latestVersion.id,
            groupId = groupId,
            habitName = latestVersion.name,
            habitEmoji = latestVersion.emoji,
            color = latestVersion.color,
            completionPercent = percent.coerceIn(0, 100),
            totalDue = familyDue,
            totalCompleted = familyCompleted
         )
      }

      val overallSuccessRate =
         if (grandTotalDue == 0) 0 else ((grandTotalCompleted.toFloat() / grandTotalDue) * 100).toInt()

      return OverviewStats(
         successRatePercent = overallSuccessRate.coerceIn(0, 100),
         grandTotalDue = grandTotalDue,
         grandTotalCompleted = grandTotalCompleted,
         habitStats = habitStatItems
      )
   }

   // Make this internal (not private) so the wrapper functions below can call it directly
   internal fun getDateRange(timeframe: Timeframe, refDate: LocalDate): Pair<LocalDate, LocalDate> {
      return when (timeframe) {
         Timeframe.DAY -> refDate to refDate
         Timeframe.WEEK -> {
            val start = refDate.with(DayOfWeek.MONDAY)
            start to start.plusDays(6)
         }

         Timeframe.MONTH -> {
            val start = refDate.withDayOfMonth(1)
            start to start.plusMonths(1).minusDays(1)
         }

         Timeframe.YEAR -> {
            val start = refDate.withDayOfYear(1)
            start to start.plusYears(1).minusDays(1)
         }
      }
   }

   /** DAILY / SPECIFIC_DAYS — safe to compute per-version, since a single day never spans a version boundary. */
   private fun calculateDayBasedCounts(
      item: HabitWithLogs,
      startDate: LocalDate,
      endDate: LocalDate
   ): Pair<Int, Int> {
      val habit = item.habit
      val logDates = item.logs.map {
         Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
      }.toSet()

      var due = 0
      var completed = 0

      when (habit.frequencyType) {
         FrequencyType.DAILY -> {
            var curr = startDate
            while (!curr.isAfter(endDate)) {
               due++
               if (curr in logDates) completed++
               curr = curr.plusDays(1)
            }
         }

         FrequencyType.SPECIFIC_DAYS -> {
            val allowedDays = parseCustomDays(habit)
            var curr = startDate
            while (!curr.isAfter(endDate)) {
               if (curr.dayOfWeek in allowedDays) {
                  due++
                  if (curr in logDates) completed++
               }
               curr = curr.plusDays(1)
            }
         }

         FrequencyType.TIMES_PER_WEEK -> {
            // Unreachable — filtered out before this function is called.
         }
      }

      return due to completed
   }

   /**
    * TIMES_PER_WEEK — handled at the family level, across all versions at once.
    * For each calendar week in range, picks whichever version was active for the
    * most days of that week and uses ITS target — so a mid-week frequency change
    * counts that week exactly once, not once per version.
    */
   private fun calculateTimesPerWeekFamilyCounts(
      versions: List<HabitWithLogs>,
      startDate: LocalDate,
      endDate: LocalDate
   ): Pair<Int, Int> {
      var due = 0
      var completed = 0
      var weekStart = startDate.with(DayOfWeek.MONDAY)

      val allLogDatesInFamily = versions.flatMap { it.logs }.map {
         Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
      }

      while (!weekStart.isAfter(endDate)) {
         val weekEnd = weekStart.plusDays(6)

         val activeVersion = versions
            .mapNotNull { item ->
               val habit = item.habit
               val habitStart = Instant.ofEpochMilli(habit.createdAt)
                  .atZone(ZoneId.systemDefault()).toLocalDate()
               val habitEnd = habit.replacedAt?.let {
                  Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
               } ?: weekEnd

               val overlapStart = maxOf(weekStart, habitStart)
               val overlapEnd = minOf(weekEnd, habitEnd)
               val overlapDays = if (overlapStart.isAfter(overlapEnd)) {
                  -1L
               } else {
                  overlapEnd.toEpochDay() - overlapStart.toEpochDay() + 1
               }
               if (overlapDays > 0) habit to overlapDays else null
            }
            .maxByOrNull { it.second }
            ?.first

         if (activeVersion != null) {
            val target = activeVersion.timesPerWeek ?: 1
            val completionsThisWeek = allLogDatesInFamily.count { date ->
               !date.isBefore(weekStart) && !date.isAfter(weekEnd) &&
                       !date.isBefore(startDate) && !date.isAfter(endDate)
            }
            due += target
            completed += minOf(completionsThisWeek, target)
         }

         weekStart = weekStart.plusWeeks(1)
      }

      return due to completed
   }

   fun shouldNotifyToday(
      item: HabitWithLogs,
      today: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): Boolean {
      val habit = item.habit
      val logDates = item.logs.map {
         Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
      }.toSet()

      return when (habit.frequencyType) {
         FrequencyType.DAILY -> true
         FrequencyType.SPECIFIC_DAYS -> today.dayOfWeek in parseCustomDays(habit)
         FrequencyType.TIMES_PER_WEEK -> {
            val (weekStart, _) = getDateRange(Timeframe.WEEK, today)
            val completedThisWeek = logDates.count { date ->
               !date.isBefore(weekStart) && date.isBefore(today)
            }
            completedThisWeek < (habit.timesPerWeek ?: 1)
         }
      }
   }

}
/**
 * "Any habit done" streak — counts consecutive days where AT LEAST ONE
 * habit (any habit) was completed, regardless of which one. Not per-habit.
 */
fun calculateOverallCurrentStreak(
   allHabitsWithLogs: List<HabitWithLogs>,
   timeframe: Timeframe = Timeframe.YEAR
): Int {
   val allLogMillis = allHabitsWithLogs
      .flatMap { it.logs }
      .map { it.date }

   if (allLogMillis.isEmpty()) return 0

   val (timeframeStart, _) = getDateRange(timeframe, LocalDate.now(ZoneId.systemDefault()))

   val filteredLogMillis = allLogMillis.filter {
      val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
      !date.isBefore(timeframeStart) // inclusive, per the fix from last message
   }

   if (filteredLogMillis.isEmpty()) return 0

   val oneDayMillis = 24 * 60 * 60 * 1000L
   val sortedDesc = filteredLogMillis.toSortedSet(compareByDescending { it })
   val today = getStartOfTodayTimestamp()

   var expected = today
   if (expected !in sortedDesc) expected -= oneDayMillis // today not done yet, count from yesterday

   var streak = 0
   for (date in sortedDesc) {
      when {
         date == expected -> {
            streak++
            expected -= oneDayMillis
         }
         date < expected -> return streak // gap found
      }
   }
   return streak
}
/**
 * "Any habit done" longest streak — longest run of consecutive days where
 * AT LEAST ONE habit (any habit) was completed, across the whole history
 * (or clipped to a timeframe, if provided).
 */
fun calculateOverallLongestStreak(
   allHabitsWithLogs: List<HabitWithLogs>,
   timeframe: Timeframe = Timeframe.YEAR
): Int {
   val allLogMillis = allHabitsWithLogs
      .flatMap { it.logs }
      .map { it.date }

   if (allLogMillis.isEmpty()) return 0

   val (timeframeStart, _) = getDateRange(timeframe, LocalDate.now(ZoneId.systemDefault()))

   val sortedDates = allLogMillis
      .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
      .filter { !it.isBefore(timeframeStart) }
      .distinct()
      .sorted()

   if (sortedDates.isEmpty()) return 0

   var longest = 1
   var current = 1

   for (i in 1 until sortedDates.size) {
      current = if (sortedDates[i] == sortedDates[i - 1].plusDays(1)) {
         current + 1
      } else {
         1
      }
      longest = maxOf(longest, current)
   }

   return longest
}