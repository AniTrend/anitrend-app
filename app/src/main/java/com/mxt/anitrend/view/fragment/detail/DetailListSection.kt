package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.group.RecyclerItem

/**
 * View-only list controller used by detail destination local sections.
 *
 * It owns list widgets, pagination presentation, and adapter binding, but not
 * navigation or repository state. The parent Fragment supplies page requests
 * and translates item actions into destination operations.
 *
 * The lifecycle and rendering callbacks remain together intentionally because
 * they coordinate one list's view state and pagination behavior.
 */
@Suppress("TooManyFunctions")
class DetailListSection(
    private val context: Context,
    private val adapter: ListAdapter<RecyclerItem, RecyclerView.ViewHolder>,
    private val onLoadPage: (Int) -> Unit,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {

    private var binding: FragmentListBinding? = null
    private var layoutManager: StaggeredGridLayoutManager? = null
    private val scrollListener = RecyclerScrollListener()
    private var hasLoaded = false

    /** Returns the number of items currently held by the adapter. */
    val itemCount: Int
        get() = adapter.itemCount

    /** Inflates and initializes the list section view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        layoutManager = StaggeredGridLayoutManager(
            context.resources.getInteger(R.integer.grid_giphy_x3),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.recyclerView.layoutManager = layoutManager
        sectionBinding.recyclerView.setHasFixedSize(true)
        sectionBinding.recyclerView.isNestedScrollingEnabled = true
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        sectionBinding.refreshLayout.setPermitLoad(true)
        layoutManager?.let { manager ->
            scrollListener.initListener(
                manager,
                object : RecyclerLoadListener {
                    override fun onLoadMore() = onLoad()
                },
            )
        }
        sectionBinding.recyclerView.addOnScrollListener(scrollListener)
        return sectionBinding.root
    }

    /** Activates the section, loading its first page when needed. */
    fun select() {
        val current = binding ?: return
        if (!hasLoaded || adapter.itemCount == 0) {
            current.stateLayout.showLoading()
            onRefresh()
        } else {
            current.stateLayout.showContent()
        }
    }

    /** Requests a refresh of the current list page. */
    fun refresh() {
        onRefresh()
    }

    /** Shows the list loading state. */
    fun renderLoading() {
        binding?.stateLayout?.showLoading()
    }

    /** Renders [items] and updates the list pagination state. */
    fun render(
        items: List<RecyclerItem>,
        pageInfo: PageInfo?,
        isEmpty: Boolean,
    ) {
        val current = binding ?: return
        hasLoaded = true
        scrollListener.setPageInfo(pageInfo)
        adapter.submitList(items.toList())
        current.recyclerView.adapter = adapter
        if (current.refreshLayout.isRefreshing()) {
            current.refreshLayout.setRefreshing(false)
        }
        if (current.refreshLayout.isLoading()) {
            current.refreshLayout.setLoading(false)
        }
        if (items.isEmpty() || isEmpty) {
            current.stateLayout.showError(
                context.getCompatDrawable(R.drawable.ic_emoji_sweat),
                context.getString(R.string.layout_empty_response),
                context.getString(R.string.try_again),
            ) { onRefresh() }
        } else {
            current.stateLayout.showContent()
        }
    }

    /** Shows [message] as the list error and exposes the retry action. */
    fun renderError(message: String) {
        val current = binding ?: return
        if (current.refreshLayout.isRefreshing()) {
            current.refreshLayout.setRefreshing(false)
        }
        if (current.refreshLayout.isLoading()) {
            current.refreshLayout.setLoading(false)
        }
        current.stateLayout.showError(
            context.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            context.getString(R.string.try_again),
        ) { onRefresh() }
    }

    /** Saves pagination state into [outState] under [key]. */
    fun saveState(outState: android.os.Bundle, key: String) {
        outState.putInt("$key.page", scrollListener.currentPage)
        outState.putInt("$key.offset", scrollListener.currentOffset)
    }

    /** Restores pagination state from [savedState] under [key]. */
    fun restoreState(savedState: android.os.Bundle?, key: String) {
        savedState ?: return
        scrollListener.currentPage = savedState.getInt("$key.page", 1)
        scrollListener.currentOffset = savedState.getInt("$key.offset", 0)
    }

    /** Releases list view resources and clears the view binding. */
    fun destroyView() {
        binding?.recyclerView?.clearOnScrollListeners()
        binding = null
        layoutManager = null
    }

    /** Resets pagination and requests the first page. */
    override fun onRefresh() {
        scrollListener.onRefreshPage()
        onLoadPage(scrollListener.currentPage)
    }

    /** Requests the currently selected page for an append operation. */
    override fun onLoad() {
        binding?.refreshLayout?.setLoading(true)
        onLoadPage(scrollListener.currentPage)
    }
}
