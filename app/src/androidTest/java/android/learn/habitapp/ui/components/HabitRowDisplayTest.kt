package android.learn.habitapp.ui.components

import android.learn.habitapp.ui.theme.HabitAppTheme
import android.learn.habitapp.util.DisableAnimationsRule
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * HabitRow is what actually renders a saved habit's name + emoji in the list, so this
 * is the most direct place to verify "what was saved is what gets shown" without needing
 * a full Room + Hilt round trip. It's the display contract: given this name and this
 * emoji, exactly that name and emoji appear on screen — nothing transformed, truncated,
 * or defaulted along the way.
 */
@RunWith(AndroidJUnit4::class)
class HabitRowDisplayTest {

    @get:Rule(order = 0)
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun setHabitRowContent(
        habitName: String,
        emoji: String,
        habitColor: Int? = null,
        streak: Int = 0,
        isToggled: Boolean = false,
    ) {
        composeTestRule.setContent {
            HabitAppTheme {
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(lazyListState) { _, _ -> }

                LazyColumn(state = lazyListState) {
                    item(key = 1) {
                        ReorderableItem(reorderableState, key = 1) {
                            HabitRow(
                                habitId = 1,
                                habitName = habitName,
                                streak = streak,
                                isToggled = isToggled,
                                emoji = emoji,
                                habitColor = habitColor,
                                reorderableScope = this,
                                onDragStopped = {},
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun displaysExactlyTheSavedNameAndEmoji() {
        setHabitRowContent(habitName = "Drink Water", emoji = "💧")

        composeTestRule.onNodeWithText("Drink Water").assertExists()
        composeTestRule.onNodeWithText("💧").assertExists()
    }

    @Test
    fun displaysADifferentNameAndEmoji_notTheFirstFixture() {
        // A second, distinct fixture proves the row reflects its actual input
        // rather than something hardcoded or left over from a previous test.
        setHabitRowContent(habitName = "Read 20 Minutes", emoji = "📚")

        composeTestRule.onNodeWithText("Read 20 Minutes").assertExists()
        composeTestRule.onNodeWithText("📚").assertExists()
        composeTestRule.onNodeWithText("Drink Water").assertDoesNotExist()
    }

    @Test
    fun longHabitNameIsStillDisplayedInFull() {
        val longName = "Meditate for fifteen minutes every single morning"
        setHabitRowContent(habitName = longName, emoji = "🧘")

        composeTestRule.onNodeWithText(longName).assertExists()
    }

    @Test
    fun editingToADifferentEmojiUpdatesWhatIsShown() {
        // Simulates picking a new emoji for an existing habit: same habit, new icon.
        // setContent can only be called once per test, so the emoji is hoisted as
        // mutable state and changed *after* the initial composition to trigger recomposition.
        var currentEmoji by mutableStateOf("📓")

        composeTestRule.setContent {
            HabitAppTheme {
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(lazyListState) { _, _ -> }

                LazyColumn(state = lazyListState) {
                    item(key = 1) {
                        ReorderableItem(reorderableState, key = 1) {
                            HabitRow(
                                habitId = 1,
                                habitName = "Journal",
                                streak = 0,
                                isToggled = false,
                                emoji = currentEmoji,
                                habitColor = null,
                                reorderableScope = this,
                                onDragStopped = {},
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNodeWithText("📓").assertExists()

        currentEmoji = "✍️"
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("✍️").assertExists()
        composeTestRule.onNodeWithText("📓").assertDoesNotExist()
    }
}
