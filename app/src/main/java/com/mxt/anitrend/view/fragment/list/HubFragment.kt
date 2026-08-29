package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.navigation.extension.navigateToWatchList
import com.mxt.anitrend.util.CompatUtil

/** Root-host entry for suggestions and its independently navigable feed page. */
class HubFragment : SuggestionListFragment() {
    private lateinit var sectionSelector: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_media_sections, container, false)
        sectionSelector = root.findViewById(R.id.media_section_selector)
        val listContainer = root.findViewById<ViewGroup>(R.id.media_list_container)
        val binding = FragmentListBinding.inflate(inflater, listContainer, true)
        swipeRefreshLayout = binding.refreshLayout
        recyclerView = binding.recyclerView
        stateLayout = binding.stateLayout
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true
        mLayoutManager = StaggeredGridLayoutManager(
            resources.getInteger(mColumnSize),
            StaggeredGridLayoutManager.VERTICAL,
        )
        recyclerView.layoutManager = mLayoutManager
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, it) }
        buildSectionSelector()
        return root
    }

    private fun buildSectionSelector() {
        val suggestionsChip = Chip(requireContext()).apply {
            id = View.generateViewId()
            text = getString(R.string.hub_title_suggestions)
            isCheckable = true
        }
        val feedChip = Chip(requireContext()).apply {
            id = View.generateViewId()
            text = getString(R.string.hub_title_most_popular)
            isCheckable = true
            setOnClickListener { findNavController().navigateToWatchList(popular = true) }
        }
        sectionSelector.addView(suggestionsChip)
        sectionSelector.addView(feedChip)
        sectionSelector.check(suggestionsChip.id)
    }
}
