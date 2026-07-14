package android.learn.habitapp.ui.components

import android.learn.habitapp.ui.theme.HabitAppTheme
import android.learn.habitapp.util.DisableAnimationsRule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmSaveDialogTest {

    // Order matters: animations must be off *before* the Compose rule starts the activity.
    @get:Rule(order = 0)
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun setDialogContent(
        onDiscardRequest: () -> Unit = {},
        onCancel: () -> Unit = {},
        onSave: () -> Unit = {},
        dialogTitle: String = "Discard Changes?",
        dialogText: String = "Are you sure you want to discard your changes?",
    ) {
        composeTestRule.setContent {
            HabitAppTheme {
                ConfirmSaveDialog(
                    onDiscardRequest = onDiscardRequest,
                    onCancel = onCancel,
                    onSave = onSave,
                    dialogTitle = dialogTitle,
                    dialogText = dialogText,
                    icon = Icons.Outlined.Save,
                )
            }
        }
        // Compose's own clock is also paused, on top of the system animations rule,
        // so any enter/exit animation on the Dialog itself can't leave the tree mid-transition.
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun displaysTheGivenTitleAndBodyText() {
        setDialogContent(
            dialogTitle = "Discard Changes?",
            dialogText = "Are you sure you want to discard your changes?",
        )

        composeTestRule.onNodeWithText("Discard Changes?").assertExists()
        composeTestRule.onNodeWithText("Are you sure you want to discard your changes?").assertExists()
    }

    @Test
    fun tappingSave_invokesOnSaveOnly() {
        var saveClicked = false
        var cancelClicked = false
        var discardClicked = false

        setDialogContent(
            onSave = { saveClicked = true },
            onCancel = { cancelClicked = true },
            onDiscardRequest = { discardClicked = true },
        )

        composeTestRule.onNodeWithText("Save").performClick()

        assert(saveClicked) { "Expected onSave to be invoked" }
        assert(!cancelClicked) { "onCancel should not fire when Save is tapped" }
        assert(!discardClicked) { "onDiscardRequest should not fire when Save is tapped" }
    }

    @Test
    fun tappingCancel_invokesOnCancelOnly() {
        var saveClicked = false
        var cancelClicked = false
        var discardClicked = false

        setDialogContent(
            onSave = { saveClicked = true },
            onCancel = { cancelClicked = true },
            onDiscardRequest = { discardClicked = true },
        )

        composeTestRule.onNodeWithText("Cancel").performClick()

        assert(cancelClicked) { "Expected onCancel to be invoked" }
        assert(!saveClicked) { "onSave should not fire when Cancel is tapped" }
        assert(!discardClicked) { "onDiscardRequest should not fire when Cancel is tapped" }
    }

    @Test
    fun tappingDiscard_invokesOnDiscardRequestOnly() {
        var saveClicked = false
        var cancelClicked = false
        var discardClicked = false

        setDialogContent(
            onSave = { saveClicked = true },
            onCancel = { cancelClicked = true },
            onDiscardRequest = { discardClicked = true },
        )

        composeTestRule.onNodeWithText("Discard").performClick()

        assert(discardClicked) { "Expected onDiscardRequest to be invoked" }
        assert(!saveClicked) { "onSave should not fire when Discard is tapped" }
        assert(!cancelClicked) { "onCancel should not fire when Discard is tapped" }
    }

    @Test
    fun displaysADifferentTitleAndTextWhenGivenDifferentValues() {
        setDialogContent(
            dialogTitle = "Unsaved Habit",
            dialogText = "You have unsaved edits to this habit.",
        )

        composeTestRule.onNodeWithText("Unsaved Habit").assertExists()
        composeTestRule.onNodeWithText("You have unsaved edits to this habit.").assertExists()
        composeTestRule.onNodeWithText("Discard Changes?").assertDoesNotExist()
    }
}
