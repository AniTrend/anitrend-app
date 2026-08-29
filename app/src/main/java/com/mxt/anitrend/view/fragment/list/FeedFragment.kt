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

/** Unified root-host destination for the former three-page feed pager. */
class FeedFragment : FeedListFragment() {
    private enum class Section(
        val title: Int,
        val isFollowing: Boolean? = null,
        val type: String? = null,
        val isMixed: Boolean? = null,
    ) {
        PROGRESS(R.string.feed_title_progress, isFollowing = true, type = KeyUtil.MEDIA_LIST),
        STATUS(R.string.feed_title_status, isFollowing = true, type = KeyUtil.TEXT),
        PUBLIC_STATUS(R.string.feed_title_public_status, isMixed = true),
    }

    private lateinit var sectionSelector: ChipGroup
    private var section = Section.PROGRESS

    companion object {
        private const val STATE_SECTION = "feed_section"

        internal fun resolveSection(raw: String?): String = raw ?: Section.PROGRESS.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        section = savedInstanceState?.getString(STATE_SECTION)
            ?.let { raw -> runCatching { Section.valueOf(raw) }.getOrNull() }
            ?: Section.PROGRESS
        arguments = argumentsFor(section)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        sectionSelector = root.findViewById(R.id.feed_section_selector)
        val listContainer = root.findViewById<ViewGroup>(R.id.feed_list_container)
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
        arguments = argumentsFor(value)
        clearRenderedFeedItems()
        mScrollListener.onRefreshPage()
        sectionSelector.check(sectionSelector.getChildAt(value.ordinal).id)
        showLoading()
        makeRequest()
    }

    private fun argumentsFor(value: Section): Bundle = Bundle().apply {
        putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        value.isFollowing?.let { putBoolean(KeyUtil.arg_isFollowing, it) }
        value.type?.let { putString(KeyUtil.arg_type, it) }
        value.isMixed?.let { putBoolean(KeyUtil.arg_isMixed, it) }
        putBoolean(KeyUtil.arg_asHtml, false)
    }
}
