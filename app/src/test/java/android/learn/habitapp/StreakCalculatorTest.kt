package android.learn.habitapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StreakCalculatorTest {

    private val oneDay = 24 * 60 * 60 * 1000L

    // Fixed reference point so tests never depend on the real clock.
    private val today = 1_000_000L * oneDay
    private fun daysAgo(n: Int) = today - (n * oneDay)

    @Nested
    @DisplayName("empty or trivial input")
    inner class EmptyInput {

        @Test
        @DisplayName("returns 0 when there are no logged dates at all")
        fun emptyList_returnsZero() {
            assertEquals(0, calculateCurrentStreak(emptyList(), today))
        }

        @Test
        @DisplayName("a single log today counts as a streak of 1")
        fun singleLogToday_returnsOne() {
            assertEquals(1, calculateCurrentStreak(listOf(today), today))
        }

        @Test
        @DisplayName("a single log yesterday (today not yet logged) still counts as 1")
        fun singleLogYesterday_todayNotLoggedYet_returnsOne() {
            assertEquals(1, calculateCurrentStreak(listOf(daysAgo(1)), today))
        }
    }

    @Nested
    @DisplayName("continuous streaks")
    inner class ContinuousStreaks {

        @Test
        @DisplayName("counts a run of consecutive days ending today")
        fun consecutiveDaysEndingToday_countsAll() {
            val logs = listOf(today, daysAgo(1), daysAgo(2), daysAgo(3))
            assertEquals(4, calculateCurrentStreak(logs, today))
        }

        @Test
        @DisplayName("counts a run of consecutive days ending yesterday, when today isn't logged yet")
        fun consecutiveDaysEndingYesterday_todayNotLoggedYet_countsAll() {
            val logs = listOf(daysAgo(1), daysAgo(2), daysAgo(3))
            assertEquals(3, calculateCurrentStreak(logs, today))
        }

        @Test
        @DisplayName("order of input dates doesn't matter")
        fun unsortedInput_stillCountsCorrectly() {
            val logs = listOf(daysAgo(2), today, daysAgo(1), daysAgo(3))
            assertEquals(4, calculateCurrentStreak(logs, today))
        }

        @Test
        @DisplayName("duplicate timestamps for the same day don't double-count")
        fun duplicateDates_countedOnce() {
            val logs = listOf(today, today, daysAgo(1), daysAgo(1))
            assertEquals(2, calculateCurrentStreak(logs, today))
        }
    }

    @Nested
    @DisplayName("gaps break the streak")
    inner class GapsBreakStreak {

        @Test
        @DisplayName("a gap of one skipped day stops the count at the break")
        fun oneDayGap_stopsAtBreak() {
            // logged today, yesterday, then a gap, then day-before-yesterday-minus-more
            val logs = listOf(today, daysAgo(1), daysAgo(3), daysAgo(4))
            assertEquals(2, calculateCurrentStreak(logs, today))
        }

        @Test
        @DisplayName("a habit not logged today or yesterday has a streak of 0")
        fun missedTodayAndYesterday_returnsZero() {
            val logs = listOf(daysAgo(2), daysAgo(3))
            assertEquals(0, calculateCurrentStreak(logs, today))
        }

        @Test
        @DisplayName("only an old, unrelated log date far in the past gives a streak of 0")
        fun onlyOldLog_returnsZero() {
            assertEquals(0, calculateCurrentStreak(listOf(daysAgo(30)), today))
        }
    }

    @Nested
    @DisplayName("edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("a long unbroken streak is counted in full")
        fun longStreak_countsAllDays() {
            val logs = (0..99).map { daysAgo(it) }
            assertEquals(100, calculateCurrentStreak(logs, today))
        }

        @Test
        @DisplayName("a future-dated log (clock skew / bad data) is ignored, not treated as extending the streak")
        fun futureDatedLog_isIgnored() {
            val logs = listOf(today + oneDay, today, daysAgo(1))
            assertEquals(2, calculateCurrentStreak(logs, today))
        }
    }
}
