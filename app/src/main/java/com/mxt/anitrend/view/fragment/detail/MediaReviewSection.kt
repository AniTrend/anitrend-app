package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.ReviewViewModel
import kotlinx.coroutines.launch

/**
 * View-only paged reviews section used by the media destination.
 *
 * The constructor callbacks and grouped state helpers intentionally preserve
 * the destination's existing review actions and lifecycle.
 */
@Suppress("LongParameterList", "TooManyFunctions")
class MediaReviewSection(
    context: Context,
    databaseHelper: DatabaseHelper,
    private val viewModel: ReviewViewModel,
    private val mediaId: Long,
    @KeyUtil.MediaType private val mediaType: String?,
    private val onReviewClick: (View, ReviewRecord) -> Unit,
    private val onReviewLongClick: (View, ReviewRecord) -> Unit,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private val appContext = context
    private val scrollListener = RecyclerScrollListener()
    private val adapter = ReviewAdapter(
        context = context,
        currentUser = databaseHelper.currentUser,
        onRateReviewAction = viewModel::rateReview,
        isMediaType = true,
    ).also { reviewAdapter ->
        reviewAdapter.clickListener = object : ItemClickListener<ReviewRecord> {
            override fun onItemClick(target: View, data: IndexedValue<ReviewRecord>) {
                onReviewClick(target, data.value)
            }

            override fun onItemLongClick(target: View, data: IndexedValue<ReviewRecord>) {
                onReviewLongClick(target, data.value)
            }
        }
    }
    private var binding: FragmentListBinding? = null
    private var staleSnackbar: Snackbar? = null

    private val currentBinding: FragmentListBinding
        get() = checkNotNull(binding)

    /** Inflates and initializes the reviews view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        val layoutManager = StaggeredGridLayoutManager(
            appContext.resources.getInteger(R.integer.single_list_x1),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.recyclerView.layoutManager = layoutManager
        sectionBinding.recyclerView.setHasFixedSize(true)
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        scrollListener.initListener(
            layoutManager,
            object : RecyclerLoadListener {
                override fun onLoadMore() = loadNextPage()
            },
        )
        sectionBinding.recyclerView.addOnScrollListener(scrollListener)
        sectionBinding.recyclerView.adapter = adapter
        sectionBinding.stateLayout.showLoading()
        return sectionBinding.root
    }

    /** Starts collecting review state and rate events for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (state) {
                            is ReviewViewModel.UiState.Loading -> showLoading()
                            is ReviewViewModel.UiState.Success -> render(state.content, state.replaceExisting, state.isStale)
                            is ReviewViewModel.UiState.Error -> showError(state.message)
                        }
                    }
                }
                launch {
                    viewModel.rateReviewEvents.collect { outcome ->
                        adapter.onRateReviewResult(outcome.reviewId, outcome.result)
                    }
                }
            }
        }
    }

    /** Activates the section and loads reviews when needed. */
    fun select() {
        if (adapter.itemCount == 0) onRefresh() else showContent()
    }

    /** Refreshes the review list from its first page. */
    fun refresh() = onRefresh()

    /** Releases review view resources and dismisses transient feedback. */
    fun destroyView() {
        staleSnackbar?.dismiss()
        staleSnackbar = null
        currentBinding.recyclerView.clearOnScrollListeners()
        binding = null
    }

    /** Resets pagination and requests the first review page. */
    override fun onRefresh() {
        scrollListener.onRefreshPage()
        load(scrollListener.currentPage)
    }

    /** Requests the next review page. */
    override fun onLoad() = loadNextPage()

    private fun loadNextPage() {
        currentBinding.refreshLayout.setLoading(true)
        load(scrollListener.currentPage)
    }

    private fun load(page: Int) {
        if (mediaId == 0L) return
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        viewModel.load(mediaId = mediaId, type = type, page = page)
    }

    private fun render(content: PageContainer<ReviewRecord>, replaceExisting: Boolean, isStale: Boolean) {
        renderStaleState(isStale)
        scrollListener.setPageInfo(content.pageInfo)
        if (!content.isEmpty) {
            adapter.submitList(content.pageData) { showContent() }
        } else if (replaceExisting) {
            adapter.submitList(emptyList()) { showEmpty() }
        } else {
            stopRefreshIndicators()
            showContent()
        }
    }

    private fun renderStaleState(isStale: Boolean) {
        if (isStale) {
            if (staleSnackbar?.isShown == true) return
            staleSnackbar = Snackbar.make(
                currentBinding.stateLayout,
                R.string.review_stale_message,
                Snackbar.LENGTH_INDEFINITE,
            ).setAction(R.string.review_stale_refresh) {
                staleSnackbar = null
                showLoading()
                onRefresh()
            }
            staleSnackbar?.show()
        } else {
            staleSnackbar?.dismiss()
            staleSnackbar = null
        }
    }

    private fun showLoading() {
        currentBinding.stateLayout.showLoading()
    }

    private fun showContent() {
        stopRefreshIndicators()
        currentBinding.stateLayout.showContent()
    }

    private fun showEmpty() {
        stopRefreshIndicators()
        currentBinding.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_sweat),
            appContext.getString(R.string.layout_empty_response),
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun showError(message: String) {
        stopRefreshIndicators()
        currentBinding.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun stopRefreshIndicators() {
        currentBinding.refreshLayout.setRefreshing(false)
        currentBinding.refreshLayout.setLoading(false)
    }
}
