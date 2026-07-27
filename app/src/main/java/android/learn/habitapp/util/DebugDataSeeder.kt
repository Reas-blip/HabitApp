package android.learn.habitapp.util

import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.repository.HabitRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

// DebugDataSeeder.kt — only ever called from a debug-gated button, never in production flow
object DebugDataSeeder {

   suspend fun seed(repository: HabitRepository) {
      repository.deleteAllLogs()
      repository.deleteAllHabits()

      val today = LocalDate.now(ZoneId.systemDefault())
      val zone = ZoneId.systemDefault()
      fun LocalDate.toMillis() = this.atStartOfDay(zone).toInstant().toEpochMilli()

      // Habit 1: DAILY, created 40 days ago, ~85% completion rate with random gaps
      val dailyId = repository.insertHabit(
         HabitEntity(
            groupId = UUID.randomUUID().toString(),
            name = "Drink Water", emoji = "💧",
            frequencyType = FrequencyType.DAILY,
            createdAt = today.minusDays(40).toMillis()
         )
      ).toInt()
      (0..40).forEach { daysAgo ->
         val date = today.minusDays(daysAgo.toLong())
         if (Random.nextFloat() < 0.85f) {
            repository.insertHabitLog(HabitLogsEntity(habitId = dailyId, date = date.toMillis()))
         }
      }

      // Habit 2: DAILY, created 15 days ago, PERFECT streak (tests current/longest streak logic cleanly)
      val perfectId = repository.insertHabit(
         HabitEntity(
            groupId = UUID.randomUUID().toString(),
            name = "Meditate", emoji = "🧘",
            frequencyType = FrequencyType.DAILY,
            createdAt = today.minusDays(15).toMillis()
         )
      ).toInt()
      (0..15).forEach { daysAgo ->
         repository.insertHabitLog(
            HabitLogsEntity(
               habitId = perfectId,
               date = today.minusDays(daysAgo.toLong()).toMillis()
            )
         )
      }

      // Habit 3: SPECIFIC_DAYS (Mon/Wed/Fri), created 60 days ago — tests isDueOn logic
      val specificId = repository.insertHabit(
         HabitEntity(
            groupId = UUID.randomUUID().toString(),
            name = "Gym", emoji = "🏋️",
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            customDays = "MONDAY,WEDNESDAY,FRIDAY",
            createdAt = today.minusDays(60).toMillis()
         )
      ).toInt()
      (0..60).forEach { daysAgo ->
         val date = today.minusDays(daysAgo.toLong())
         if (date.dayOfWeek in setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            && Random.nextFloat() < 0.75f) {
            repository.insertHabitLog(HabitLogsEntity(habitId = specificId, date = date.toMillis()))
         }
      }

      // Habit 4: TIMES_PER_WEEK (3x), created 30 days ago — tests the weekly-target logic
      val weeklyId = repository.insertHabit(
         HabitEntity(
            groupId = UUID.randomUUID().toString(),
            name = "Call Family", emoji = "📞",
            frequencyType = FrequencyType.TIMES_PER_WEEK,
            timesPerWeek = 3,
            createdAt = today.minusDays(30).toMillis()
         )
      ).toInt()
      var weekStart = today.minusDays(30).with(DayOfWeek.MONDAY)
      while (!weekStart.isAfter(today)) {
         val completionsThisWeek = Random.nextInt(0, 5) // sometimes under, sometimes over target
         val daysInWeek = (0..6).map { weekStart.plusDays(it.toLong()) }.filter { !it.isAfter(today) }
         daysInWeek.shuffled().take(minOf(completionsThisWeek, daysInWeek.size)).forEach { date ->
            repository.insertHabitLog(HabitLogsEntity(habitId = weeklyId, date = date.toMillis()))
         }
         weekStart = weekStart.plusWeeks(1)
      }

      // Habit 5: DAILY, created TODAY — the exact edge case that caused your earlier bug
      val newTodayId = repository.insertHabit(
         HabitEntity(
            groupId = UUID.randomUUID().toString(),
            name = "Journal", emoji = "📓",
            frequencyType = FrequencyType.DAILY,
            createdAt = today.toMillis()
         )
      ).toInt()
      // Deliberately leave uncompleted — tests that "created today, not done yet" doesn't show as missed

      // Habit 6: ARCHIVED habit — tests that archived habits are excluded from active views
      val archivedId = repository.insertHabit(
         HabitEntity(
            groupId = UUID.randomUUID().toString(),
            name = "Old Habit", emoji = "🗑️",
            frequencyType = FrequencyType.DAILY,
            isArchived = true,
            createdAt = today.minusDays(50).toMillis()
         )
      ).toInt()
   }
}