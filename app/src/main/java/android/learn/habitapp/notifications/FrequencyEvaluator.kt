// FrequencyEvaluator.kt
package android.learn.habitapp.notifications

import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.repository.HabitRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object FrequencyEvaluator {

    fun parseCustomDays(habit: HabitEntity): Set<DayOfWeek> {
        return habit.customDays
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { DayOfWeek.valueOf(it) }
            ?.toSet() ?: emptySet()
    }

    /** Should today's reminder actually be shown, given the habit's frequency rules? */
    suspend fun shouldNotifyToday(
        habit: HabitEntity,
        repository: HabitRepository,
        today: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): Boolean {
        return when (habit.frequencyType) {
            FrequencyType.DAILY -> true

            FrequencyType.SPECIFIC_DAYS -> today.dayOfWeek in parseCustomDays(habit)

            FrequencyType.TIMES_PER_WEEK -> {
                val (weekStart, weekEnd) = getCurrentWeekRange(today)
                val completedThisWeek = repository.getLogCountInRange(habit.id, weekStart, weekEnd)
                completedThisWeek < (habit.timesPerWeek ?: 1)
            }
        }
    }

    /** Next date this habit is actually due, used when scheduling the alarm itself. */
    fun nextValidDate(habit: HabitEntity, from: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): LocalDate {

        val time = habit.reminderTime?.let { LocalTime.parse(it) } ?: return from.toLocalDate()
        val todayAlreadyPassed = from.toLocalTime().isAfter(time)
        val startDate = if (todayAlreadyPassed) from.toLocalDate().plusDays(1) else from.toLocalDate()
        return when (habit.frequencyType) {
            FrequencyType.DAILY, FrequencyType.TIMES_PER_WEEK -> startDate
            // TIMES_PER_WEEK has no fixed day, so the alarm just fires daily;
            // shouldNotifyToday() is what actually decides whether to show it.

            FrequencyType.SPECIFIC_DAYS -> {
                val allowedDays = parseCustomDays(habit)
                var candidate = startDate
                repeat(7) {
                    if (candidate.dayOfWeek in allowedDays) return candidate
                    candidate = candidate.plusDays(1)
                }
                startDate // fallback if somehow no days are selected
            }
        }
    }

    private fun getCurrentWeekRange(today: LocalDate): Pair<Long, Long> {
        val monday = today.with(DayOfWeek.MONDAY)
        val sunday = monday.plusDays(6)

        val start = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = sunday.atStartOfDay(ZoneId.systemDefault())
            .plusDays(1).minusSeconds(1)
            .toInstant().toEpochMilli()

        return start to end
    }
}