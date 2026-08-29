package com.mxt.anitrend.view.fragment.detail

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

/** Unified root-host destination for the former anime and manga browse-review pager. */
class ReviewBrowseFragment : BrowseReviewFragment() {
    private enum class Section(
        val title: Int,
        val mediaType: String,
    ) {
        ANIME(R.string.anime, KeyUtil.ANIME),
        MANGA(R.string.manga, KeyUtil.MANGA),
    }

    private lateinit var sectionSelector: ChipGroup
    private var section = Section.ANIME

    companion object {
        private const val STATE_SECTION = "review_browse_section"

        internal fun resolveSection(raw: String?): String = raw ?: Section.ANIME.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        section = savedInstanceState?.getString(STATE_SECTION)
            ?.let { raw -> runCatching { Section.valueOf(raw) }.getOrNull() }
            ?: Section.ANIME
        arguments = Bundle().apply { putString(KeyUtil.arg_mediaType, section.mediaType) }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_review, container, false)
        sectionSelector = root.findViewById(R.id.review_section_selector)
        val listContainer = root.findViewById<ViewGroup>(R.id.review_list_container)
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
        mediaType = value.mediaType
        arguments = Bundle().apply { putString(KeyUtil.arg_mediaType, value.mediaType) }
        reviewAdapter?.submitList(emptyList())
        mScrollListener.onRefreshPage()
        sectionSelector.check(sectionSelector.getChildAt(value.ordinal).id)
        showLoading()
        makeRequest()
    }
}
