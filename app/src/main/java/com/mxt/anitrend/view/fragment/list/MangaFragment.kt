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
import com.mxt.anitrend.util.media.MediaBrowseUtil

/** Unified root-host destination for the former manga browse pager. */
class MangaFragment : MediaBrowseFragment() {
    private enum class Section(
        val title: Int,
        val sort: String?,
        val filterEnabled: Boolean,
    ) {
        MANGA_LIST(R.string.manga_title_manga_list, null, true),
        RECENTLY_ADDED(R.string.manga_title_recently_added, KeyUtil.ID + KeyUtil.DESC, false),
    }

    private lateinit var sectionSelector: ChipGroup
    private var section = Section.MANGA_LIST

    companion object {
        private const val STATE_SECTION = "manga_section"

        internal fun resolveSection(raw: String?): String = raw ?: Section.MANGA_LIST.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        section = savedInstanceState?.getString(STATE_SECTION)
            ?.let { raw -> runCatching { Section.valueOf(raw) }.getOrNull() }
            ?: Section.MANGA_LIST
        arguments = argumentsFor(section)
        super.onCreate(savedInstanceState)
    }

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
        putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
        value.sort?.let { putString(KeyUtil.arg_sort, it) }
        putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        putParcelable(KeyUtil.arg_media_util, MediaBrowseUtil(value.filterEnabled))
    }
}
