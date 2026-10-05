package com.mxt.anitrend.ui.screenshot

import androidx.fragment.app.FragmentActivity
import com.github.takahirom.roborazzi.captureRoboImage
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityMainBinding
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compact-window primary shell captures (360dp). Each test renders the
 * production `activity_main.xml` shell in one of the three brand themes with a
 * deterministic fixture state.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], application = ScreenshotTestApplication::class, qualifiers = "w360dp-h640dp")
class PrimaryShellScreenshotTest : ScreenshotTestBase() {

    private fun captureShell(
        themeRes: Int,
        fontScale: Float? = null,
        rtl: Boolean = false,
        configure: (ActivityMainBinding) -> Unit,
    ) {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        RuntimeEnvironment.setQualifiers(if (rtl) "ar-w360dp-h640dp" else "w360dp-h640dp")
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        fontScale?.let { ShellScreenshotFixtures.applyFontScale(activity, it) }
        val binding = ShellScreenshotFixtures.inflateShell(activity, themeRes)
        configure(binding)
        ShellScreenshotFixtures.layoutForCapture(binding.root, 360, 640)
        binding.root.captureRoboImage()
    }

    private fun ActivityMainBinding.renderAuthenticatedShell(selectedItem: Int) {
        appBarMain.customToolbar.toolbar.title = "Discover Anime"
        ShellScreenshotFixtures.inflateToolbarMenu(this)
        val navigation = ShellScreenshotFixtures.primaryNavigation(this)
        ShellScreenshotFixtures.applySignedInPrimaryVisibility(navigation)
        navigation.selectedItemId = selectedItem
    }

    @Test
    fun lightCompactShellWithSelectedAnimeItem() {
        captureShell(R.style.AppThemeLight) { binding ->
            binding.renderAuthenticatedShell(R.id.nav_anime)
        }
    }

    @Test
    fun darkCompactShellWithSelectedMangaItem() {
        captureShell(R.style.AppThemeDark) { binding ->
            binding.renderAuthenticatedShell(R.id.nav_manga)
        }
    }

    @Test
    fun blackCompactShellWithSelectedFeedItem() {
        captureShell(R.style.AppThemeBlack) { binding ->
            binding.renderAuthenticatedShell(R.id.nav_home_feed)
        }
    }

    @Test
    fun authenticatedCompactShellShowsBothLibraryItems() {
        captureShell(R.style.AppThemeLight) { binding ->
            binding.renderAuthenticatedShell(R.id.nav_myanime)
        }
    }

    @Test
    fun signedOutCompactShellHidesLibraryItems() {
        captureShell(R.style.AppThemeLight) { binding ->
            binding.appBarMain.customToolbar.toolbar.title = "Discover Anime"
            val menu = ShellScreenshotFixtures.inflateToolbarMenu(binding)
            menu.findItem(R.id.nav_sign_in)?.isVisible = true
            menu.findItem(R.id.nav_sign_out)?.isVisible = false
            val navigation = ShellScreenshotFixtures.primaryNavigation(binding)
            ShellScreenshotFixtures.applySignedOutPrimaryVisibility(navigation)
            navigation.selectedItemId = R.id.nav_anime
        }
    }

    @Test
    fun largeFontCompactShellKeepsBarLabelsReadable() {
        captureShell(R.style.AppThemeLight, fontScale = 1.3f) { binding ->
            binding.renderAuthenticatedShell(R.id.nav_mymanga)
        }
    }

    @Test
    fun rtlCompactShellKeepsPrimaryOrderMirrored() {
        captureShell(R.style.AppThemeLight, rtl = true) { binding ->
            binding.renderAuthenticatedShell(R.id.nav_manga)
        }
    }
}
