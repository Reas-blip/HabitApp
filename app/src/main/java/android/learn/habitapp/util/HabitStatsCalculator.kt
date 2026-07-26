package android.learn.habitapp.util

import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitStatItem
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.local.OverviewStats
import android.learn.habitapp.data.local.Timeframe
import android.learn.habitapp.util.FrequencyEvaluator.parseCustomDays
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Simple result of any "how many were due vs done" question. */
data class RangeStats(val due: Int, val done: Int) {
   val percent: Int get() = if (due == 0) 0 else (done * 100 / due)
}

private fun Long.toLocalDate(): LocalDate =
   Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

object HabitStatsCalculator {

   // ── FUNCTION 1: was this ONE habit-version due on this ONE date? ──────────
   // Every "which version applies on this date" question lives here, and ONLY here.
   fun isDueOn(habit: HabitEntity, date: LocalDate): Boolean {
      val createdDate = habit.createdAt.toLocalDate()
      if (date.isBefore(createdDate)) return false

      val replacedDate = habit.replacedAt?.toLocalDate()
      // "not before" (>=), not "after" (>) — the day it was replaced belongs
      // to the NEW version, not this one. This is the boundary that caused
      // the double-count bug in the old version.
      if (replacedDate != null && !date.isBefore(replacedDate)) return false

      return when (habit.frequencyType) {
         FrequencyType.DAILY -> true
         FrequencyType.SPECIFIC_DAYS -> date.dayOfWeek in parseCustomDays(habit)
         FrequencyType.TIMES_PER_WEEK -> true // not used — TIMES_PER_WEEK has no single "due day"
      }
   }


   fun isScheduledToday(habit: HabitEntity): Boolean {
      return isDueOn(habit, LocalDate.now(ZoneId.systemDefault()))
   }

   // ── FUNCTION 2: count due/done for ONE habit-version over a date range ────
   fun countDayBased(
      habit: HabitEntity,
      logDates: Set<LocalDate>,
      start: LocalDate,
      end: LocalDate
   ): RangeStats {
      var due = 0
      var done = 0
      var day = start
      while (!day.isAfter(end)) {
         if (isDueOn(habit, day)) {
            due++
            if (day in logDates) done++
         }
         day = day.plusDays(1)
      }
      return RangeStats(due, done)
   }

   // Sums countDayBased across every DAILY/SPECIFIC_DAYS version in a habit family.
   // No manual clipping needed — isDueOn already returns false outside each
   // version's own window, so inactive versions just contribute zero.
   fun countDayBasedFamily(
      versions: List<HabitWithLogs>,
      start: LocalDate,
      end: LocalDate
   ): RangeStats {
      var due = 0
      var done = 0
      versions
         .filter { it.habit.frequencyType != FrequencyType.TIMES_PER_WEEK }
         .forEach { v ->
            val logDates = v.logs.map { it.date.toLocalDate() }.toSet()
            val r = countDayBased(v.habit, logDates, start, end)
            due += r.due
            done += r.done
         }
      return RangeStats(due, done)
   }

   // How many days of [weekStart, weekEnd] does this habit-version actually cover,
   // clamped to the [start, end] range we were asked about? Used only to decide
   // which version "owns" a week when a frequency change happens mid-week.
   private fun overlapDays(habit: HabitEntity, weekStart: LocalDate, weekEnd: LocalDate): Long {
      val habitStart = habit.createdAt.toLocalDate()
      val habitEnd = habit.replacedAt?.toLocalDate()?.minusDays(1) ?: weekEnd

      val overlapStart = maxOf(weekStart, habitStart)
      val overlapEnd = minOf(weekEnd, habitEnd)
      return if (overlapStart.isAfter(overlapEnd)) 0L
      else overlapEnd.toEpochDay() - overlapStart.toEpochDay() + 1
   }

   // TIMES_PER_WEEK — a fundamentally different shape (weekly target, not daily due-ness).
   // See the walkthrough below for exactly what this does and why.
   fun countWeeklyFamily(
      versions: List<HabitWithLogs>,
      start: LocalDate,
      end: LocalDate
   ): RangeStats {
      val weeklyVersions = versions.filter { it.habit.frequencyType == FrequencyType.TIMES_PER_WEEK }
      if (weeklyVersions.isEmpty()) return RangeStats(0, 0)

      val allLogDates = weeklyVersions.flatMap { it.logs }.map { it.date.toLocalDate() }.toSet()

      var due = 0
      var done = 0
      var weekStart = start.with(DayOfWeek.MONDAY)

      while (!weekStart.isAfter(end)) {
         val weekEnd = weekStart.plusDays(6)
         val clippedWeekEnd = minOf(weekEnd, end) // don't look past what we were asked for

         val activeHabit = weeklyVersions
            .map { it.habit }
            .maxByOrNull { overlapDays(it, weekStart, clippedWeekEnd) }

         if (activeHabit != null && overlapDays(activeHabit, weekStart, clippedWeekEnd) > 0) {
            val fullTarget = activeHabit.timesPerWeek ?: 1

            // If this week isn't over yet (clippedWeekEnd < the week's natural Sunday),
            // scale the target down to how much of the week has actually elapsed —
            // otherwise a 3x/week habit shows "due: 3" on Monday morning, before
            // there's been any real chance to complete it.
            val elapsedDays = (clippedWeekEnd.toEpochDay() - weekStart.toEpochDay() + 1)
               .coerceAtLeast(0)
            val target = if (clippedWeekEnd.isBefore(weekEnd)) {
               kotlin.math.ceil(fullTarget * elapsedDays / 7.0).toInt().coerceAtMost(fullTarget)
            } else {
               fullTarget
            }

            val completedInWeek = allLogDates.count { logDate ->
               !logDate.isBefore(weekStart) && !logDate.isAfter(clippedWeekEnd) &&
                       !logDate.isBefore(start)
            }
            due += target
            done += minOf(completedInWeek, target)
         }

         weekStart = weekStart.plusWeeks(1)
      }

      return RangeStats(due, done)
   }

   // ── The one function everything else calls ────────────────────────────────
   fun statsForFamily(
      versions: List<HabitWithLogs>,
      start: LocalDate,
      end: LocalDate
   ): RangeStats {
      val dayBased = countDayBasedFamily(versions, start, end)
      val weekly = countWeeklyFamily(versions, start, end)
      return RangeStats(dayBased.due + weekly.due, dayBased.done + weekly.done)
   }

   // ── Whole-list convenience: stats per habit family + grand totals ─────────
   fun calculateOverview(
      allHabitsWithLogs: List<HabitWithLogs>,
      start: LocalDate,
      end: LocalDate
   ): Map<String, RangeStats> {
      return allHabitsWithLogs
         .groupBy { it.habit.groupId }
         .mapValues { (_, versions) -> statsForFamily(versions, start, end) }
   }

   // Shared by weeklyCompletionByDay and the streak functions below — the single
   // source of truth for "on this date, were the due habits completed?"
   // true = everything due that day was done, false = something was missed,
   // null = nothing was actually due (TIMES_PER_WEEK families never count here,
   // same exclusion as countDayBasedFamily — they're judged per-week, not per-day).
   private fun familyStatusOnDate(
      families: Map<String, List<HabitWithLogs>>,
      date: LocalDate
   ): Boolean? {
      var anyApplicable = false
      var allCompleted = true

      families.values.forEach { versions ->
         val activeVersion = versions.firstOrNull { v ->
            v.habit.frequencyType != FrequencyType.TIMES_PER_WEEK && isDueOn(v.habit, date)
         } ?: return@forEach

         anyApplicable = true
         val logDates = activeVersion.logs.map { it.date.toLocalDate() }.toSet()
         if (date !in logDates) allCompleted = false
      }

      return if (!anyApplicable) null else allCompleted
   }

   // For each day of a week: true = every applicable habit was completed,
   // false = at least one was missed, null = nothing was actually due that day.
   // TIMES_PER_WEEK is skipped here — it has no single "due day," it's judged
   // per-week by countWeeklyFamily instead.
   fun weeklyCompletionByDay(
      allHabitsWithLogs: List<HabitWithLogs>,
      weekStart: LocalDate = LocalDate.now(ZoneId.systemDefault()).with(DayOfWeek.MONDAY)
   ): Map<DayOfWeek, Boolean?> {
      val families = allHabitsWithLogs.groupBy { it.habit.groupId }
      return DayOfWeek.entries.associateWith { dayOfWeek ->
         familyStatusOnDate(families, weekStart.with(dayOfWeek))
      }
   }

   // Consecutive days, walking backward from today, where everything due was
   // done. A day with nothing due (null) is skipped — it neither extends nor
   // breaks the streak. Today itself gets a pass if it's not finished yet
   // (status == false), so the streak isn't punished mid-day; a genuine miss
   // on any earlier day stops the count.
   fun calculateOverallCurrentStreak(
      allHabitsWithLogs: List<HabitWithLogs>,
      today: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): Int {
      if (allHabitsWithLogs.isEmpty()) return 0
      val families = allHabitsWithLogs.groupBy { it.habit.groupId }
      val lowerBound = allHabitsWithLogs.minOf { it.habit.createdAt.toLocalDate() }

      var current = today
      var streak = 0
      var isFirstDay = true

      while (!current.isBefore(lowerBound)) {
         val status = familyStatusOnDate(families, current)

         if (isFirstDay && status == false) {
            // today isn't over yet — give it a pass rather than treating
            // "not done YET" the same as "missed"
            isFirstDay = false
            current = current.minusDays(1)
            continue
         }
         isFirstDay = false

         when (status) {
            true -> { streak++; current = current.minusDays(1) }
            null -> current = current.minusDays(1) // nothing due — skip, don't break
            false -> return streak
         }
      }
      return streak
   }

   // Longest run of consecutive days (across all history) where everything
   // due was completed. Same null-skipping rule as the current streak.
   fun calculateOverallLongestStreak(
      allHabitsWithLogs: List<HabitWithLogs>,
      today: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): Int {
      if (allHabitsWithLogs.isEmpty()) return 0
      val families = allHabitsWithLogs.groupBy { it.habit.groupId }
      val lowerBound = allHabitsWithLogs.minOf { it.habit.createdAt.toLocalDate() }

      var longestStreak = 0
      var currentStreak = 0
      var date = lowerBound

      while (!date.isAfter(today)) {
         when (familyStatusOnDate(families, date)) {
            true -> { currentStreak++; longestStreak = maxOf(longestStreak, currentStreak) }
            false -> currentStreak = 0
            null -> Unit // nothing due — doesn't grow or break the run
         }
         date = date.plusDays(1)
      }
      return longestStreak
   }

   // ── Timeframe → date range, used only by calculateStats below ─────────────
   private fun getDateRange(timeframe: Timeframe, refDate: LocalDate): Pair<LocalDate, LocalDate> =
      when (timeframe) {
         Timeframe.DAY -> refDate to refDate
         Timeframe.WEEK -> {
            val s = refDate.with(DayOfWeek.MONDAY)
            s to s.plusDays(6)
         }
         Timeframe.MONTH -> {
            val s = refDate.withDayOfMonth(1)
            s to s.plusMonths(1).minusDays(1)
         }
         Timeframe.YEAR -> {
            val s = refDate.withDayOfYear(1)
            s to s.plusYears(1).minusDays(1)
         }
      }

   // Due-but-not-done, counted only over days that have FULLY elapsed
   // (up through yesterday) — so a habit due later today doesn't get
   // flagged as "missed" before the day is even over.
   private fun missedCount(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframe: Timeframe,
      referenceDate: LocalDate
   ): Int {
      val (start, end) = getDateRange(timeframe, referenceDate)
      val clippedEnd = minOf(end, referenceDate.minusDays(1))
      if (clippedEnd.isBefore(start)) return 0

      var due = 0
      var done = 0
      allHabitsWithLogs.groupBy { it.habit.groupId }.values.forEach { versions ->
         val r = statsForFamily(versions, start, clippedEnd)
         due += r.due
         done += r.done
      }
      return due - done
   }

   // Due-but-not-done, counted from today through the rest of the timeframe —
   // e.g. "12 things left to do this month."
   private fun remainingDue(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframe: Timeframe,
      referenceDate: LocalDate
   ): Int {
      val (start, end) = getDateRange(timeframe, referenceDate)
      val clippedStart = maxOf(start, referenceDate)
      if (clippedStart.isAfter(end)) return 0

      var due = 0
      var done = 0
      allHabitsWithLogs.groupBy { it.habit.groupId }.values.forEach { versions ->
         val r = statsForFamily(versions, clippedStart, end)
         due += r.due
         done += r.done
      }
      return due - done
   }

   // ── The UI-facing entry point — builds OverviewStats from RangeStats ──────
   // Everything here is composition: no new due/done logic, just calling
   // statsForFamily per habit family and packaging the results.
   fun calculateStats(
      allHabitsWithLogs: List<HabitWithLogs>,
      timeframe: Timeframe,
      referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
   ): OverviewStats {
      val (start, end) = getDateRange(timeframe, referenceDate)
      val clippedEnd = minOf(end, referenceDate) // never count future days as due yet

      var grandDue = 0
      var grandDone = 0

      val habitStatItems = allHabitsWithLogs
         .groupBy { it.habit.groupId }
         .map { (groupId, versions) ->
            // display info (name/emoji/color) comes from whichever version is newest —
            // due/done math still runs across ALL versions via statsForFamily
            val latest = versions.maxByOrNull { it.habit.createdAt }!!.habit
            val stats = statsForFamily(versions, start, clippedEnd)
            grandDue += stats.due
            grandDone += stats.done

            HabitStatItem(
               habitId = latest.id,
               groupId = groupId,
               habitName = latest.name,
               habitEmoji = latest.emoji,
               color = latest.color,
               completionPercent = stats.percent,
               totalDue = stats.due,
               totalCompleted = stats.done
            )
         }

      return OverviewStats(
         successRatePercent = RangeStats(grandDue, grandDone).percent,
         grandTotalDue = grandDue,
         grandTotalCompleted = grandDone,
         habitStats = habitStatItems,
         missedCount = missedCount(allHabitsWithLogs, timeframe, referenceDate),
         remainingDue = remainingDue(allHabitsWithLogs, timeframe, referenceDate)
      )
   }
}
