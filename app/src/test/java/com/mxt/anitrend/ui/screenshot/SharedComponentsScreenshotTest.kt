package com.mxt.anitrend.ui.screenshot

import android.app.Activity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ItemLoadStateFooterBinding
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Representative shared component states rendered from production layouts and
 * the established AniTrend Material 3 styles.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], application = ScreenshotTestApplication::class, qualifiers = "w360dp-h640dp")
class SharedComponentsScreenshotTest {

    private fun host(themeRes: Int = R.style.AppThemeLight): Pair<Activity, LinearLayout> {
        val activity: Activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val themed = ShellScreenshotFixtures.themed(activity, themeRes)
        val container = LinearLayout(themed).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        return activity to container
    }

    @Test
    fun buttonVariantsUseRolePairs() {
        val (_, container) = host()
        val themed = container.context
        listOf(
            MaterialButton(themed).apply {
                setText(R.string.button_try_again)
            },
            MaterialButton(themed, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                setText(R.string.button_try_again)
            },
            MaterialButton(themed, null, com.google.android.material.R.attr.materialButtonTonalStyle).apply {
                setText(R.string.button_try_again)
            },
            MaterialButton(themed, null, com.google.android.material.R.attr.materialButtonStyle).apply {
                setText(R.string.button_try_again)
            },
        ).forEach { container.addView(it) }
        container.captureRoboImage()
    }

    @Test
    fun outlinedTextFieldShowsHintAndValue() {
        val (_, container) = host()
        val inflater = LayoutInflater.from(container.context)
        val field = TextInputLayout(container.context, null, com.google.android.material.R.attr.textInputStyle)
        field.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        field.hint = "Search"
        val edit = TextInputEditText(field.context)
        edit.setText("cowboy bebop")
        field.addView(edit)
        container.addView(field)
        inflater.inflate(R.layout.item_settings_divider, container, false).let(container::addView)
        container.captureRoboImage()
    }

    @Test
    fun materialSwitchShowsCheckedAndUncheckedStates() {
        val (_, container) = host()
        val themed = container.context
        val checked = MaterialSwitch(themed).apply {
            isChecked = true
            text = "Clear notification on dismiss"
        }
        val unchecked = MaterialSwitch(themed).apply {
            isChecked = false
            isEnabled = false
            text = "Notification work around"
        }
        container.addView(checked)
        container.addView(unchecked)
        container.captureRoboImage()
    }

    @Test
    fun filterChipsUseCheckedAndDefaultRoles() {
        val (_, container) = host()
        val themed = container.context
        val group = ChipGroup(themed)
        listOf("Watching", "Completed", "Planning").forEachIndexed { index, label ->
            val chip = Chip(themed, null, com.google.android.material.R.attr.chipStyle)
            chip.setTextAppearance(R.style.Widget_AniTrend_ManageSheet_CustomListChip)
            chip.text = label
            chip.isChecked = index == 0
            group.addView(chip)
        }
        container.addView(group)
        container.captureRoboImage()
    }

    @Test
    fun loadingIndicatorRendersContainedStyle() {
        val (_, container) = host(R.style.AppThemeBlack)
        val indicator = CircularProgressIndicator(container.context)
        container.addView(indicator)
        container.captureRoboImage()
    }

    @Test
    fun loadStateFooterLoadingStateShowsProgress() {
        val (_, container) = host()
        val inflater = LayoutInflater.from(container.context)
        val footer = ItemLoadStateFooterBinding.inflate(inflater, container, false)
        footer.loadingProgress.visibility = android.view.View.VISIBLE
        footer.loadingText.visibility = android.view.View.VISIBLE
        footer.retryButton.visibility = android.view.View.GONE
        container.addView(footer.root)
        container.captureRoboImage()
    }

    @Test
    fun loadStateFooterErrorStateShowsRetryAction() {
        val (_, container) = host()
        val inflater = LayoutInflater.from(container.context)
        val footer = ItemLoadStateFooterBinding.inflate(inflater, container, false)
        footer.loadingProgress.visibility = android.view.View.GONE
        footer.loadingText.visibility = android.view.View.GONE
        footer.retryButton.visibility = android.view.View.VISIBLE
        container.addView(footer.root)
        container.captureRoboImage()
    }
}