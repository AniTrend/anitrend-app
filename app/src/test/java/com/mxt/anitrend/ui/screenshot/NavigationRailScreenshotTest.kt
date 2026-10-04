package com.mxt.anitrend.ui.screenshot

import androidx.fragment.app.FragmentActivity
import com.github.takahirom.roborazzi.captureRoboImage
import com.mxt.anitrend.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Expanded-window primary shell captures (w600dp). The rail variant of
 * `activity_main.xml` must expose the same five destinations beside the
 * content host.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], application = ScreenshotTestApplication::class, qualifiers = "w600dp-h640dp")
class NavigationRailScreenshotTest : ScreenshotTestBase() {

    @Test
    fun railShellShowsFiveDestinationsWithSelectedAnimeItem() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        RuntimeEnvironment.setQualifiers("w600dp-h640dp")
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val binding = ShellScreenshotFixtures.inflateShell(activity, R.style.AppThemeLight)
        binding.appBarMain.customToolbar.toolbar.title = "Discover Anime"
        ShellScreenshotFixtures.inflateToolbarMenu(binding)
        val navigation = ShellScreenshotFixtures.primaryNavigation(binding)
        ShellScreenshotFixtures.applySignedInPrimaryVisibility(navigation)
        navigation.selectedItemId = R.id.nav_anime
        ShellScreenshotFixtures.layoutForCapture(binding.root, 600, 640)
        binding.root.captureRoboImage()
    }

    @Test
    fun railShellSignedOutHidesLibraryItems() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        RuntimeEnvironment.setQualifiers("w600dp-h640dp")
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val binding = ShellScreenshotFixtures.inflateShell(activity, R.style.AppThemeDark)
        binding.appBarMain.customToolbar.toolbar.title = "Discover Anime"
        val menu = ShellScreenshotFixtures.inflateToolbarMenu(binding)
        menu.findItem(R.id.nav_sign_in)?.isVisible = true
        menu.findItem(R.id.nav_sign_out)?.isVisible = false
        val navigation = ShellScreenshotFixtures.primaryNavigation(binding)
        ShellScreenshotFixtures.applySignedOutPrimaryVisibility(navigation)
        navigation.selectedItemId = R.id.nav_manga
        ShellScreenshotFixtures.layoutForCapture(binding.root, 600, 640)
        binding.root.captureRoboImage()
    }
}
