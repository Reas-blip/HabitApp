package android.learn.habitapp.notifications

import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class FrequencyEvaluatorTest {

    // A known Wednesday, so day-of-week based tests are deterministic.
    private val wednesday = LocalDate.of(2026, 7, 8)

    private fun habit(
        frequencyType: FrequencyType,
        customDays: String? = null,
        timesPerWeek: Int? = null,
        reminderTime: String? = null,
    ) = HabitEntity(
        name = "Test Habit",
        emoji = "🔥",
        frequencyType = frequencyType,
        customDays = customDays,
        timesPerWeek = timesPerWeek,
        reminderTime = reminderTime,
    )

    @Nested
    @DisplayName("parseCustomDays")
    inner class ParseCustomDays {

        @Test
        @DisplayName("parses a comma-separated list of day names")
        fun parsesCommaSeparatedDays() {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = "MONDAY,WEDNESDAY,FRIDAY")
            val result = FrequencyEvaluator.parseCustomDays(habit)
            assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), result)
        }

        @Test
        @DisplayName("returns an empty set when customDays is null")
        fun nullCustomDays_returnsEmptySet() {
            val habit = habit(FrequencyType.DAILY, customDays = null)
            assertTrue(FrequencyEvaluator.parseCustomDays(habit).isEmpty())
        }

        @Test
        @DisplayName("filters out blank entries from a malformed CSV string")
        fun blankEntries_areFiltered() {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = "MONDAY,,FRIDAY,")
            val result = FrequencyEvaluator.parseCustomDays(habit)
            assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), result)
        }
    }

    @Nested
    @DisplayName("shouldNotifyToday")
    inner class ShouldNotifyToday {

        private val repository = mockk<HabitRepository>()

        @Test
        @DisplayName("DAILY habits always notify, regardless of the day")
        fun daily_alwaysTrue() = runBlocking {
            val habit = habit(FrequencyType.DAILY)
            assertTrue(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("SPECIFIC_DAYS notifies when today is in the allowed set")
        fun specificDays_todayIncluded_returnsTrue() = runBlocking {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = "WEDNESDAY,FRIDAY")
            assertTrue(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("SPECIFIC_DAYS does not notify when today is not in the allowed set")
        fun specificDays_todayExcluded_returnsFalse() = runBlocking {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = "MONDAY,FRIDAY")
            assertFalse(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("SPECIFIC_DAYS with no configured days never notifies")
        fun specificDays_noDaysConfigured_returnsFalse() = runBlocking {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = null)
            assertFalse(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("TIMES_PER_WEEK notifies when the weekly target hasn't been met yet")
        fun timesPerWeek_belowTarget_returnsTrue() = runBlocking {
            val habit = habit(FrequencyType.TIMES_PER_WEEK, timesPerWeek = 3)
            coEvery { repository.getLogCountInRange(any(), any(), any()) } returns 1

            assertTrue(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("TIMES_PER_WEEK does not notify once the weekly target is already met")
        fun timesPerWeek_targetMet_returnsFalse() = runBlocking {
            val habit = habit(FrequencyType.TIMES_PER_WEEK, timesPerWeek = 3)
            coEvery { repository.getLogCountInRange(any(), any(), any()) } returns 3

            assertFalse(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("TIMES_PER_WEEK does not notify when completed count exceeds the target")
        fun timesPerWeek_targetExceeded_returnsFalse() = runBlocking {
            val habit = habit(FrequencyType.TIMES_PER_WEEK, timesPerWeek = 3)
            coEvery { repository.getLogCountInRange(any(), any(), any()) } returns 5

            assertFalse(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }

        @Test
        @DisplayName("TIMES_PER_WEEK defaults to a target of 1 when timesPerWeek is null")
        fun timesPerWeek_nullTarget_defaultsToOne() = runBlocking {
            val habit = habit(FrequencyType.TIMES_PER_WEEK, timesPerWeek = null)
            coEvery { repository.getLogCountInRange(any(), any(), any()) } returns 0

            assertTrue(FrequencyEvaluator.shouldNotifyToday(habit, repository, wednesday))
        }
    }

    @Nested
    @DisplayName("nextValidDate")
    inner class NextValidDate {

        private fun zonedAt(date: LocalDate, time: LocalTime) =
            ZonedDateTime.of(date, time, ZoneId.systemDefault())

        @Test
        @DisplayName("with no reminder time set, returns today's date")
        fun noReminderTime_returnsTodaysDate() {
            val habit = habit(FrequencyType.DAILY, reminderTime = null)
            val from = zonedAt(wednesday, LocalTime.of(9, 0))
            assertEquals(wednesday, FrequencyEvaluator.nextValidDate(habit, from))
        }

        @Test
        @DisplayName("DAILY, reminder time still upcoming today, returns today")
        fun daily_reminderStillUpcoming_returnsToday() {
            val habit = habit(FrequencyType.DAILY, reminderTime = "18:00")
            val from = zonedAt(wednesday, LocalTime.of(9, 0))
            assertEquals(wednesday, FrequencyEvaluator.nextValidDate(habit, from))
        }

        @Test
        @DisplayName("DAILY, reminder time already passed today, returns tomorrow")
        fun daily_reminderAlreadyPassed_returnsTomorrow() {
            val habit = habit(FrequencyType.DAILY, reminderTime = "08:00")
            val from = zonedAt(wednesday, LocalTime.of(9, 0))
            assertEquals(wednesday.plusDays(1), FrequencyEvaluator.nextValidDate(habit, from))
        }

        @Test
        @DisplayName("SPECIFIC_DAYS finds the next allowed day of the week, skipping disallowed ones")
        fun specificDays_findsNextAllowedDay() {
            // Wednesday, allowed days are Friday and Monday -> next is Friday (2 days later)
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = "FRIDAY,MONDAY", reminderTime = "09:00")
            val from = zonedAt(wednesday, LocalTime.of(8, 0))
            assertEquals(wednesday.plusDays(2), FrequencyEvaluator.nextValidDate(habit, from))
        }

        @Test
        @DisplayName("SPECIFIC_DAYS returns today when today itself is an allowed day and reminder hasn't passed")
        fun specificDays_todayIsAllowed_returnsToday() {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = "WEDNESDAY", reminderTime = "18:00")
            val from = zonedAt(wednesday, LocalTime.of(8, 0))
            assertEquals(wednesday, FrequencyEvaluator.nextValidDate(habit, from))
        }

        @Test
        @DisplayName("SPECIFIC_DAYS with no allowed days falls back to the start date instead of looping forever")
        fun specificDays_noAllowedDays_fallsBackToStartDate() {
            val habit = habit(FrequencyType.SPECIFIC_DAYS, customDays = null, reminderTime = "18:00")
            val from = zonedAt(wednesday, LocalTime.of(8, 0))
            assertEquals(wednesday, FrequencyEvaluator.nextValidDate(habit, from))
        }

        @Test
        @DisplayName("TIMES_PER_WEEK behaves like DAILY for scheduling purposes (fires daily)")
        fun timesPerWeek_firesDaily() {
            val habit = habit(FrequencyType.TIMES_PER_WEEK, timesPerWeek = 3, reminderTime = "08:00")
            val from = zonedAt(wednesday, LocalTime.of(9, 0))
            assertEquals(wednesday.plusDays(1), FrequencyEvaluator.nextValidDate(habit, from))
        }
    }
}
