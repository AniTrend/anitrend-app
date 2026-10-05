package com.mxt.anitrend.ui.screenshot

import androidx.core.view.drawToBitmap
import androidx.fragment.app.FragmentActivity
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.SheetSeriesManageM3Binding
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The manage-list sheet is the reference implementation of the AniTrend
 * Material 3 language; its real sheet layout and the shared dialog surface are
 * captured here.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], application = ScreenshotTestApplication::class, qualifiers = "w360dp-h640dp")
class ManageSheetScreenshotTest : ScreenshotTestBase() {

    @Test
    fun manageSheetSurfaceUsesSectionCardsAndStickyActions() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        RuntimeEnvironment.setQualifiers("w360dp-h640dp")
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val binding = SheetSeriesManageM3Binding.inflate(
            android.view.LayoutInflater.from(ShellScreenshotFixtures.themed(activity, R.style.AppThemeLight)),
        )
        ShellScreenshotFixtures.layoutForCapture(binding.root, 360, 640)
        binding.root.captureRoboImage()
    }

    @Test
    fun materialDialogSurfaceUsesRoleActions() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        RuntimeEnvironment.setQualifiers("w360dp-h640dp")
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val dialog = MaterialAlertDialogBuilder(ShellScreenshotFixtures.themed(activity, R.style.AppThemeLight))
            .setTitle(R.string.menu_title_status)
            .setSingleChoiceItems(arrayOf("Watching", "Completed"), 0) { _, _ -> }
            .setNegativeButton(R.string.Close, null)
            .show()
        val decor = dialog.window!!.decorView
        ShellScreenshotFixtures.layoutForCapture(decor, 360, 640)
        decor.drawToBitmap().captureRoboImage()
    }
}
