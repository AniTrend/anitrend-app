package com.mxt.anitrend.view.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.databinding.ItemSettingsCategoryCardBinding

/**
 * Root destination of the settings navigation graph.
 *
 * Renders the stable category list and forwards identity-only navigation
 * arguments (category ids) to the category destinations.
 */
class SettingsHubFragment : Fragment() {

    private var binding: FragmentSettingsM3Binding? = null

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
        ).forEach { category ->
            val cardBinding = ItemSettingsCategoryCardBinding.inflate(layoutInflater, sectionHost, false)
            cardBinding.categoryTitle.setText(category.titleRes)
            cardBinding.categorySummary.setText(category.summaryRes)
            cardBinding.root.contentDescription = context.getString(category.titleRes)
            cardBinding.root.setOnClickListener { openCategory(category.id) }
            sectionHost.addView(cardBinding.root)
        }
    }

    private fun openCategory(categoryId: String) {
        val navController = findNavController()
        if (categoryId == SettingsCategoryRegistry.CUSTOMIZE) {
            navController.navigate(R.id.action_settings_hub_to_customize)
        } else {
            navController.navigate(
                R.id.action_settings_hub_to_category,
                bundleOf(SettingsCategoryRegistry.ARG_CATEGORY_ID to categoryId),
            )
        }
    }
}
