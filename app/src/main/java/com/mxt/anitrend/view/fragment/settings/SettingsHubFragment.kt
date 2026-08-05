package com.mxt.anitrend.view.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.databinding.ItemSettingsCategoryCardBinding
import com.mxt.anitrend.util.Settings
import org.koin.android.ext.android.inject

/**
 * Root destination of the settings navigation graph.
 *
 * Renders the stable category list and forwards identity-only navigation
 * arguments (category ids) to the category destinations. The authenticated
 * Account category routes to its own dedicated destination instead of the
 * legacy category fragment.
 */
class SettingsHubFragment : Fragment() {

    private var binding: FragmentSettingsM3Binding? = null

    private val settings by inject<Settings>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsM3Binding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderCategories()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun renderCategories() {
        val binding = binding ?: return
        val context = requireContext()
        val sectionHost = binding.settingsSections
        sectionHost.removeAllViews()

        SettingsCategoryRegistry.categories(
            isFirebaseVisible = FirebaseApp.getApps(context).isNotEmpty(),
            isAuthenticated = settings.isAuthenticated,
        ).forEach { category ->
            val cardBinding = ItemSettingsCategoryCardBinding.inflate(layoutInflater, sectionHost, false)
            cardBinding.categoryTitle.setText(category.titleRes)
            cardBinding.categorySummary.setText(category.summaryRes)
            cardBinding.categoryIcon.setImageResource(iconForCategory(category.id))
            cardBinding.root.contentDescription = context.getString(category.titleRes)
            cardBinding.root.setOnClickListener { openCategory(category.id) }
            sectionHost.addView(cardBinding.root)
        }
    }

    @DrawableRes
    private fun iconForCategory(categoryId: String): Int = when (categoryId) {
        SettingsCategoryRegistry.ACCOUNT -> R.drawable.ic_account_circle_grey_600_24dp
        SettingsCategoryRegistry.CUSTOMIZE -> R.drawable.ic_format_color_fill_grey_600_24dp
        SettingsCategoryRegistry.APPEARANCE -> R.drawable.ic_format_size_grey_600_24dp
        SettingsCategoryRegistry.CONTENT -> R.drawable.ic_format_list_bulleted_grey_600_24dp
        SettingsCategoryRegistry.GENERAL -> R.drawable.ic_build_grey_600_24dp
        SettingsCategoryRegistry.NOTIFICATIONS -> R.drawable.ic_notifications_active_grey_600_24dp
        SettingsCategoryRegistry.DATA_SYNC -> R.drawable.ic_sync_grey_600_24dp
        SettingsCategoryRegistry.PRIVACY -> R.drawable.ic_privacy_grey_600_24dp
        SettingsCategoryRegistry.ACCESSIBILITY -> R.drawable.ic_touch_app_grey_600_24dp
        else -> R.drawable.ic_build_grey_600_24dp
    }

    private fun openCategory(categoryId: String) {
        val navController = findNavController()
        when (categoryId) {
            SettingsCategoryRegistry.ACCOUNT -> navController.navigate(R.id.action_settings_hub_to_account)
            SettingsCategoryRegistry.CUSTOMIZE -> navController.navigate(R.id.action_settings_hub_to_customize)
            else -> navController.navigate(
                R.id.action_settings_hub_to_category,
                bundleOf(SettingsCategoryRegistry.ARG_CATEGORY_ID to categoryId),
            )
        }
    }
}
