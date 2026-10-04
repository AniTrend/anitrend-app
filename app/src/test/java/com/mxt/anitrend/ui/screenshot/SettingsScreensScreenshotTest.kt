package com.mxt.anitrend.ui.screenshot

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.takahirom.roborazzi.captureRoboImage
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.databinding.ItemSettingsCategoryCardBinding
import com.mxt.anitrend.databinding.ItemSettingsRowSwitchBinding
import com.mxt.anitrend.databinding.ItemSettingsRowValueBinding
import com.mxt.anitrend.databinding.ItemSettingsSectionCardBinding
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import com.mxt.anitrend.view.fragment.settings.SettingsRow
import com.mxt.anitrend.view.fragment.settings.SettingsSections
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Settings hub and category captures rendered from the production settings
 * layouts and the canonical registry metadata. The Customize category proves
 * there is exactly one visible editor for each persisted key.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], application = ScreenshotTestApplication::class, qualifiers = "w360dp-h640dp")
class SettingsScreensScreenshotTest {

    private fun host(): Pair<Activity, FragmentSettingsM3Binding> {
        val activity: Activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        ShellScreenshotFixtures.applyDeterministicEnvironment(activity)
        val themed = ShellScreenshotFixtures.themed(activity, R.style.AppThemeLight)
        return activity to FragmentSettingsM3Binding.inflate(
            android.view.LayoutInflater.from(themed),
        )
    }

    @Test
    fun settingsHubShowsCategories() {
        val (_, binding) = host()
        val sectionHost = binding.settingsSections
        SettingsCategoryRegistry.categories(isFirebaseVisible = true, isAuthenticated = true).forEach { category ->
            val card = ItemSettingsCategoryCardBinding.inflate(
                sectionHost.context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE)
                    as android.view.LayoutInflater,
                sectionHost,
                false,
            )
            card.categoryIcon.setImageResource(R.drawable.ic_format_color_fill_grey_600_24dp)
            card.categoryTitle.setText(category.titleRes)
            card.categorySummary.setText(category.summaryRes)
            sectionHost.addView(card.root)
        }
        binding.root.captureRoboImage()
    }

    private fun captureCategory(categoryId: String) {
        val (_, binding) = host()
        val inflater = binding.root.context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE)
            as android.view.LayoutInflater
        val sectionHost: LinearLayout = binding.settingsSections
        val section = SettingsSections.build(
            isFirebaseVisible = true,
            isUpdateChannelVisible = true,
            isAdultContentVisible = true,
        ).first { it.id == categoryId }

        val sectionBinding = ItemSettingsSectionCardBinding.inflate(inflater, sectionHost, false)
        sectionBinding.sectionTitle.setText(section.titleRes)
        sectionBinding.sectionSummary.setText(section.summaryRes)
        sectionBinding.sectionIcon.setImageResource(R.drawable.ic_format_color_fill_grey_600_24dp)
        val content = sectionBinding.sectionContent

        section.rows.filter { it.visible }.forEach { row ->
            when (row) {
                is SettingsRow.Choice, is SettingsRow.Info -> {
                    val rowBinding = ItemSettingsRowValueBinding.inflate(inflater, content, false)
                    val titleRes = when (row) {
                        is SettingsRow.Choice -> row.titleRes
                        is SettingsRow.Info -> row.titleRes
                        else -> 0
                    }
                    val summaryRes = when (row) {
                        is SettingsRow.Choice -> row.summaryRes
                        is SettingsRow.Info -> row.summaryRes
                        else -> null
                    }
                    rowBinding.rowTitle.setText(titleRes)
                    summaryRes?.let {
                        rowBinding.rowSummary.setText(it)
                        rowBinding.rowSummary.visibility = View.VISIBLE
                    } ?: run { rowBinding.rowSummary.visibility = View.GONE }
                    if (row is SettingsRow.Choice) {
                        rowBinding.rowValue.visibility = View.VISIBLE
                        rowBinding.rowChevron.visibility = View.VISIBLE
                        val values = rowBinding.root.resources.getStringArray(row.valuesRes)
                        val labels = rowBinding.root.resources.getStringArray(row.entriesRes)
                        val index = values.indexOf(row.defaultValue).coerceAtLeast(0)
                        rowBinding.rowValue.text = labels[index]
                    } else {
                        rowBinding.rowValue.visibility = View.GONE
                        rowBinding.rowChevron.visibility = View.GONE
                    }
                    content.addView(rowBinding.root)
                }
                is SettingsRow.Toggle -> {
                    val rowBinding = ItemSettingsRowSwitchBinding.inflate(inflater, content, false)
                    rowBinding.rowTitle.setText(row.titleRes)
                    row.summaryRes(row.defaultValue)?.let {
                        rowBinding.rowSummary.setText(it)
                        rowBinding.rowSummary.visibility = View.VISIBLE
                    } ?: run { rowBinding.rowSummary.visibility = View.GONE }
                    rowBinding.rowSwitch.isChecked = row.defaultValue
                    rowBinding.rowSwitch.isEnabled = row.enabled
                    content.addView(rowBinding.root)
                }
            }
        }
        sectionHost.addView(sectionBinding.root)
        binding.root.captureRoboImage()
    }

    @Test
    fun customizeCategoryShowsSingleEditorPerKey() = captureCategory(SettingsCategoryRegistry.CUSTOMIZE)

    @Test
    fun appearanceCategory() = captureCategory(SettingsCategoryRegistry.APPEARANCE)

    @Test
    fun contentCategory() = captureCategory(SettingsCategoryRegistry.CONTENT)

    @Test
    fun generalCategory() = captureCategory(SettingsCategoryRegistry.GENERAL)

    @Test
    fun notificationsCategory() = captureCategory(SettingsCategoryRegistry.NOTIFICATIONS)

    @Test
    fun dataSyncCategory() = captureCategory(SettingsCategoryRegistry.DATA_SYNC)

    @Test
    fun privacyCategory() = captureCategory(SettingsCategoryRegistry.PRIVACY)

    @Test
    fun accessibilityCategory() = captureCategory(SettingsCategoryRegistry.ACCESSIBILITY)
}