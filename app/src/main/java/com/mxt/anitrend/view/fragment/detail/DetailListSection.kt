package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentDetailListSectionBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.ui.detail.DetailGridRow
import com.mxt.anitrend.ui.detail.DetailGridScrollPosition
import com.mxt.anitrend.ui.detail.DetailListAppendResult
import com.mxt.anitrend.ui.detail.DetailListGrid
import com.mxt.anitrend.ui.detail.appendDetailRows
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.util.KeyUtil

/** View-only Compose grid controller for grouped detail destination sections. */
@Suppress("TooManyFunctions")
class DetailListSection(
    private val context: Context,
    private val onLoadPage: (Int) -> Unit,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {

    private var binding: FragmentDetailListSectionBinding? = null
    private val gridState = LazyGridState()
    private val rows = mutableStateOf<List<DetailGridRow>>(emptyList(), referentialEqualityPolicy())
    private val pendingScrollPosition = mutableStateOf<DetailGridScrollPosition?>(null)
    private val appendGate = ManualGridAppendGate()
    private var pageInfo: PageInfo? = null
    private var hasLoaded = false
    private var currentPage = 1
    private var currentOffset = 0
    private var lastEmissionIdentity: Any? = null
    private var lastRequestFailed = false
    private var hasSettledUnproductivePage = false

    /** Inflates the Compose grid and binds refresh/load gestures to its scroll state. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentDetailListSectionBinding.inflate(inflater, container, false)
        binding = sectionBinding
        val composeHost: ComposeView = sectionBinding.container
        composeHost.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        sectionBinding.refreshLayout.setPermitLoad(false)
        sectionBinding.refreshLayout.scrollBoundary = { direction ->
            when (direction) {
                -1 -> gridState.canScrollBackward
                1 -> gridState.canScrollForward
                else -> false
            }
        }
        composeHost.setContent {
            AniTrendTheme {
                DetailListGrid(
                    rows = rows.value,
                    gridState = gridState,
                    callbackTarget = composeHost,
                    onLoadMore = ::onLoad,
                    pendingScrollPosition = pendingScrollPosition.value,
                    canRestoreScrollPosition = canRestoreScrollPosition(),
                    onScrollPositionRestored = { pendingScrollPosition.value = null },
                )
            }
        }
        return sectionBinding.root
    }

    /** Activates the section, loading its first page when needed. */
    fun select() {
        val current = binding ?: return
        if (!hasLoaded || rows.value.isEmpty()) {
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

    /** Replaces the rendered state with an already accumulated full snapshot. */
    internal fun renderSnapshot(
        items: List<DetailGridRow>,
        pageInfo: PageInfo?,
        isEmpty: Boolean,
        emissionIdentity: Any,
    ) {
        val current = binding ?: return
        if (!acceptEmission(emissionIdentity)) return

        val updatedRows = items.toList()
        val addedContentCount =
            if (isEmpty) {
                0
            } else if (currentPage == 1) {
                updatedRows.count { it !is DetailGridRow.Header }
            } else {
                val oldKeys = HashSet<String>(rows.value.size)
                rows.value.forEach { oldKeys.add(it.key) }
                updatedRows.count { it !is DetailGridRow.Header && it.key !in oldKeys }
            }
        rows.value = updatedRows
        this.pageInfo = pageInfo
        hasLoaded = true
        lastRequestFailed = false
        hasSettledUnproductivePage = addedContentCount == 0
        appendGate.onRequestSucceeded(addedContentCount)
        finishRefreshIndicators(current)
        showContentOrEmpty(current, updatedRows.isEmpty())
    }

    /** Appends one page payload by stable scoped row key without mutating prior snapshots. */
    internal fun renderPage(
        items: List<DetailGridRow>,
        pageInfo: PageInfo?,
        isEmpty: Boolean,
        emissionIdentity: Any,
    ) {
        val current = binding ?: return
        if (!acceptEmission(emissionIdentity)) return

        val appendResult =
            if (currentPage == 1) {
                items.toList().let { snapshot ->
                    DetailListAppendResult(
                        rows = snapshot,
                        addedContentCount = snapshot.count { it !is DetailGridRow.Header },
                    )
                }
            } else {
                appendDetailRows(rows.value, items)
            }
        rows.value = appendResult.rows
        this.pageInfo = pageInfo
        hasLoaded = true
        lastRequestFailed = false
        hasSettledUnproductivePage = appendResult.addedContentCount == 0 || isEmpty
        appendGate.onRequestSucceeded(if (isEmpty) 0 else appendResult.addedContentCount)
        finishRefreshIndicators(current)
        showContentOrEmpty(current, appendResult.rows.isEmpty())
    }

    /** Shows [message] as the list error and exposes the retry action. */
    fun renderError(message: String) {
        val current = binding ?: return
        lastRequestFailed = true
        appendGate.onRequestFailed()
        finishRefreshIndicators(current)
        current.stateLayout.showError(
            context.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            context.getString(R.string.try_again),
        ) { onRefresh() }
    }

    /** Saves pagination and visible-grid position into [outState] under [key]. */
    fun saveState(outState: android.os.Bundle, key: String) {
        outState.putInt("$key.page", currentPage)
        outState.putInt("$key.offset", currentOffset)
        outState.putInt("$key.firstVisibleItemIndex", gridState.firstVisibleItemIndex)
        outState.putInt("$key.firstVisibleItemScrollOffset", gridState.firstVisibleItemScrollOffset)
    }

    /** Restores pagination and visible-grid position from [savedState] under [key]. */
    fun restoreState(savedState: android.os.Bundle?, key: String) {
        savedState ?: return
        currentPage = savedState.getInt("$key.page", 1)
        currentOffset = savedState.getInt("$key.offset", 0)
        pendingScrollPosition.value =
            DetailGridScrollPosition(
                firstVisibleItemIndex = savedState.getInt("$key.firstVisibleItemIndex", 0),
                firstVisibleItemScrollOffset = savedState.getInt("$key.firstVisibleItemScrollOffset", 0),
            )
    }

    /** Releases refresh listeners and clears the view binding. */
    fun destroyView() {
        binding?.apply {
            refreshLayout.setOnRefreshAndLoadListener(null)
            refreshLayout.scrollBoundary = null
        }
        binding = null
    }

    /** Resets pagination and requests the first page. */
    override fun onRefresh() {
        currentPage = 1
        currentOffset = 0
        pageInfo = null
        lastEmissionIdentity = null
        lastRequestFailed = false
        hasSettledUnproductivePage = false
        appendGate.onRequestStarted()
        onLoadPage(currentPage)
    }

    /** Requests the next page only after a productive prior page. */
    override fun onLoad() {
        val current = binding ?: return
        if (!hasLoaded || !appendGate.canRequestNextPage || pageInfo?.hasNextPage() != true) return

        currentPage++
        currentOffset += KeyUtil.PAGING_LIMIT
        lastEmissionIdentity = null
        lastRequestFailed = false
        hasSettledUnproductivePage = false
        appendGate.onRequestStarted()
        current.refreshLayout.setLoading(true)
        onLoadPage(currentPage)
    }

    private fun acceptEmission(identity: Any): Boolean {
        if (identity === lastEmissionIdentity) return false
        lastEmissionIdentity = identity
        return true
    }

    private fun finishRefreshIndicators(current: FragmentDetailListSectionBinding) {
        if (current.refreshLayout.isRefreshing()) current.refreshLayout.setRefreshing(false)
        if (current.refreshLayout.isLoading()) current.refreshLayout.setLoading(false)
    }

    private fun showContentOrEmpty(
        current: FragmentDetailListSectionBinding,
        isEmpty: Boolean,
    ) {
        if (isEmpty) {
            current.stateLayout.showError(
                context.getCompatDrawable(R.drawable.ic_emoji_sweat),
                context.getString(R.string.layout_empty_response),
                context.getString(R.string.try_again),
            ) { onRefresh() }
        } else {
            current.stateLayout.showContent()
        }
    }

    private fun canRestoreScrollPosition(): Boolean {
        val position = pendingScrollPosition.value ?: return false
        return rows.value.size > position.firstVisibleItemIndex ||
            pageInfo?.hasNextPage() != true ||
            hasSettledUnproductivePage ||
            lastRequestFailed
    }
}
