package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/** Unified root-host destination for the former three-page trending pager. */
class TrendingFragment : MediaLatestList() {
    private enum class Section(
        val title: Int,
        val mediaType: String,
        val sort: String,
    ) {
        ANIME(R.string.trending_title_anime, KeyUtil.ANIME, KeyUtil.TRENDING + KeyUtil.DESC),
        MANGA(R.string.trending_title_manga, KeyUtil.MANGA, KeyUtil.TRENDING + KeyUtil.DESC),
        RECENTLY_ADDED(R.string.trending_title_recently_added, KeyUtil.ANIME, KeyUtil.ID + KeyUtil.DESC),
    }

    private lateinit var sectionSelector: ChipGroup
    private var section = Section.ANIME

    companion object {
        private const val STATE_SECTION = "trending_section"

        internal fun resolveSection(raw: String?): String = raw ?: Section.ANIME.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        section = savedInstanceState?.getString(STATE_SECTION)
            ?.let { raw -> runCatching { Section.valueOf(raw) }.getOrNull() }
            ?: Section.ANIME
        arguments = argumentsFor(section)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_trending, container, false)
        sectionSelector = root.findViewById(R.id.trending_section_selector)
        val listContainer = root.findViewById<ViewGroup>(R.id.trending_list_container)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SECTION, section.name)
        super.onSaveInstanceState(outState)
    }

    private fun buildSectionSelector() {
        Section.values().forEach { value ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(value.title)
                isCheckable = true
                setOnClickListener { selectSection(value) }
            }
            sectionSelector.addView(chip)
        }
        sectionSelector.check(sectionSelector.getChildAt(section.ordinal).id)
    }

    private fun selectSection(value: Section) {
        if (section == value) return
        section = value
        requestArgs = argumentsFor(value)
        arguments = Bundle(requestArgs)
        mAdapter.clearDataSet()
        mScrollListener.onRefreshPage()
        sectionSelector.check(sectionSelector.getChildAt(value.ordinal).id)
        showLoading()
        makeRequest()
    }

    private fun argumentsFor(value: Section): Bundle = Bundle().apply {
        putString(KeyUtil.arg_mediaType, value.mediaType)
        putString(KeyUtil.arg_sort, value.sort)
        putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
    }
}
