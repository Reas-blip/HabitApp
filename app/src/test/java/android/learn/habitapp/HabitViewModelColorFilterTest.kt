package android.learn.habitapp

import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitWithLogs
import android.learn.habitapp.data.repository.HabitRepository
import android.learn.habitapp.ui.UiState
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelColorFilterTest {

    private val RED = 0xFFFF0000.toInt()
    private val BLUE = 0xFF0000FF.toInt()
    private val GREEN = 0xFF00FF00.toInt()

    private lateinit var repository: HabitRepository
    private lateinit var viewModel: HabitViewModel

    private fun habitWithLogs(id: Int, name: String, color: Int?) = HabitWithLogs(
        habit = HabitEntity(id = id, name = name, emoji = "🔥", color = color),
        logs = emptyList()
    )

    private val sampleHabits = listOf(
        habitWithLogs(1, "Drink Water", RED),
        habitWithLogs(2, "Read", BLUE),
        habitWithLogs(3, "Stretch", RED),
        habitWithLogs(4, "Journal", null),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        every { repository.getHabitsWithLogs() } returns flowOf(sampleHabits)
        every { repository.getArchivedHabitsWithLogs() } returns flowOf(emptyList())
        every { repository.hasSeenSwipeHint } returns flowOf(true)
        viewModel = HabitViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Skips any leading Loading emissions and returns the first Success state. */
    private suspend fun ReceiveTurbine<UiState>.awaitSuccess(): UiState.Success {
        while (true) {
            val item = awaitItem()
            if (item is UiState.Success) return item
        }
    }

    @Nested
    @DisplayName("onColorFilterChanged toggle behavior (colorFilter state)")
    inner class ToggleBehavior {

        @Test
        @DisplayName("starts with no filter applied")
        fun initialState_noFilter() {
            assertNull(viewModel.colorFilter.value)
        }

        @Test
        @DisplayName("selecting a color sets the filter to that color")
        fun selectingColor_setsFilter() {
            viewModel.onColorFilterChanged(RED)
            assertEquals(RED, viewModel.colorFilter.value)
        }

        @Test
        @DisplayName("selecting the already-active color again clears the filter")
        fun selectingSameColorTwice_clearsFilter() {
            viewModel.onColorFilterChanged(RED)
            viewModel.onColorFilterChanged(RED)
            assertNull(viewModel.colorFilter.value)
        }

        @Test
        @DisplayName("selecting a different color while one is active switches to the new color")
        fun selectingDifferentColor_switchesFilter() {
            viewModel.onColorFilterChanged(RED)
            viewModel.onColorFilterChanged(BLUE)
            assertEquals(BLUE, viewModel.colorFilter.value)
        }
    }

    @Nested
    @DisplayName("displayedHabitUiState filtering effect")
    inner class FilteringEffect {

        @Test
        @DisplayName("with no filter, all habits are shown")
        fun noFilter_showsAllHabits() = runTest {
            viewModel.displayedHabitUiState.test {
                val state = awaitSuccess()
                assertEquals(4, state.habits.size)
            }
        }

        @Test
        @DisplayName("filtering by a color shows only habits with that color")
        fun filterByColor_showsOnlyMatchingHabits() = runTest {
            viewModel.displayedHabitUiState.test {
                awaitSuccess() // initial unfiltered state
                viewModel.onColorFilterChanged(RED)
                val filtered = awaitSuccess()
                assertEquals(2, filtered.habits.size)
                assertEquals(setOf(1, 3), filtered.habits.map { it.id }.toSet())
            }
        }

        @Test
        @DisplayName("filtering by a color with no matching habits returns an empty list")
        fun filterByUnusedColor_returnsEmptyList() = runTest {
            viewModel.displayedHabitUiState.test {
                awaitSuccess()
                viewModel.onColorFilterChanged(GREEN)
                val filtered = awaitSuccess()
                assertEquals(0, filtered.habits.size)
            }
        }

        @Test
        @DisplayName("clearing the filter (tapping the same color again) restores the full list")
        fun clearingFilter_restoresFullList() = runTest {
            viewModel.displayedHabitUiState.test {
                awaitSuccess()
                viewModel.onColorFilterChanged(RED)
                awaitSuccess()
                viewModel.onColorFilterChanged(RED) // toggle off
                val restored = awaitSuccess()
                assertEquals(4, restored.habits.size)
            }
        }

        @Test
        @DisplayName("switching directly from one color filter to another updates the results")
        fun switchingColors_updatesResults() = runTest {
            viewModel.displayedHabitUiState.test {
                awaitSuccess()
                viewModel.onColorFilterChanged(RED)
                awaitSuccess()
                viewModel.onColorFilterChanged(BLUE)
                val filtered = awaitSuccess()
                assertEquals(listOf(2), filtered.habits.map { it.id })
            }
        }
    }
}
