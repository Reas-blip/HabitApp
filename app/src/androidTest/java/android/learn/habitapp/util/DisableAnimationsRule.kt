package android.learn.habitapp.util

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Disables the three global animation scales (window, transition, animator) for the
 * duration of a test, then restores whatever they were before.
 *
 * Compose UI tests already pause animations via `composeTestRule.mainClock`, but a lot
 * of flakiness in real projects comes from *system* animations (activity transitions,
 * dialog enter/exit, dropdown menus) that live outside Compose's clock. Turning these
 * off at the OS level for the test process removes that source of flakiness entirely.
 *
 * Usage:
 * ```
 * @get:Rule(order = 0)
 * val disableAnimationsRule = DisableAnimationsRule()
 *
 * @get:Rule(order = 1)
 * val composeTestRule = createComposeRule()
 * ```
 */
class DisableAnimationsRule : TestWatcher() {

    private val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

    override fun starting(description: Description) {
        setAnimationScales(0.0f)
    }

    override fun finished(description: Description) {
        setAnimationScales(1.0f)
    }

    private fun setAnimationScales(scale: Float) {
        listOf(
            "window_animation_scale",
            "transition_animation_scale",
            "animator_duration_scale",
        ).forEach { setting ->
            uiAutomation.executeShellCommand("settings put global $setting $scale").close()
        }
    }
}
