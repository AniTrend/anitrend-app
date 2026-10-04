package com.mxt.anitrend.ui.screenshot

import android.widget.ListView
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.github.takahirom.roborazzi.captureRoboImage
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.CustomToolbarBinding
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The toolbar secondary overflow surface: the four secondary root destinations
 * sit in the `action_discover` submenu of the real toolbar menu. The host is a
 * resumed activity with the production toolbar so the overflow popup renders
 * through the production menu presenter without starting the NavHost.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], application = ScreenshotTestApplication::class, qualifiers = "w360dp-h640dp")
class ToolbarOverflowScreenshotTest : ScreenshotTestBase() {

    @Test
    fun toolbarOverflowOpenShowsSecondaryDestinations() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        RuntimeEnvironment.setQualifiers("w360dp-h640dp")
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val binding = CustomToolbarBinding.inflate(
            android.view.LayoutInflater.from(ShellScreenshotFixtures.themed(activity, R.style.AppThemeLight)),
        )
        binding.toolbar.title = "Discover Anime"
        val menu = binding.toolbar.menu
        menu.clear()
        androidx.appcompat.view.SupportMenuInflater(binding.toolbar.context).inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.nav_sign_out)?.isVisible = true
        menu.findItem(R.id.nav_sign_in)?.isVisible = false

        activity.setContentView(binding.root)

        val shown = binding.toolbar.showOverflowMenu()
        assertTrue("toolbar overflow must open for the fixture", shown)

        onView(allOf(isAssignableFrom(ListView::class.java), isDisplayed()))
            .captureRoboImage()
    }
}
