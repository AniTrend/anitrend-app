package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.ui.review.ReviewImageAction
import com.mxt.anitrend.ui.review.ReviewList
import com.mxt.anitrend.ui.review.distinctReviewRows
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.ReviewVoteActionResult
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
    private val databaseHelper: DatabaseHelper,
    private val viewModel: ReviewViewModel,
    private val mediaId: Long,
    @KeyUtil.MediaType private val mediaType: String?,
    private val onReviewImageAction: (ReviewImageAction, ReviewRecord) -> Unit,
    private val onReviewClick: (View, ReviewRecord) -> Unit,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private val appContext = context
    private val reviewItems = mutableStateOf<List<ReviewRecord>>(emptyList())
    private val reviewListState = LazyListState()
    private val loadMoreRevision = mutableStateOf(0)
    private val appendGate = ManualGridAppendGate()
    private var currentPage = 1
    private var pageHasNext: Boolean? = null
    private var requestInFlight = false
    private var requestStartReviewIds: Set<Long> = emptySet()
    private var lastCompletedRequestId = Long.MIN_VALUE
    private var binding: FragmentListBinding? = null
    private var staleSnackbar: Snackbar? = null

    private val currentBinding: FragmentListBinding
        get() = checkNotNull(binding)

    /** Inflates and initializes the reviews view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)

        val composeView = ComposeView(appContext).apply {
            id = R.id.container
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val voteStates = viewModel.reviewVoteState.collectAsState().value
                AniTrendTheme {
                    ReviewList(
                        reviews = reviewItems.value,
                        isBanner = false,
                        voteStates = voteStates,
                        listState = reviewListState,
                        onVote = ::handleReviewVote,
                        onImageAction = onReviewImageAction,
                        onReviewClick = onReviewClick,
                        loadMoreRevision = loadMoreRevision.value,
                        onLoadMore = ::loadNextPage,
                    )
                }
            }
        }
        sectionBinding.refreshLayout.addView(
            composeView,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        sectionBinding.refreshLayout.scrollBoundary = { direction ->
            when (direction) {
                -1 -> reviewListState.canScrollBackward
                1 -> reviewListState.canScrollForward
                else -> false
            }
        }
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
                            is ReviewViewModel.UiState.Success -> render(state)
                            is ReviewViewModel.UiState.Error -> {
                                requestInFlight = false
                                appendGate.onRequestFailed()
                                showError(state.message)
                            }
                        }
                    }
                }
                launch {
                    viewModel.rateReviewEvents.collect { outcome ->
                        val failure = outcome.result as? MutationResult.Failure ?: return@collect
                        NotifyUtil.makeText(appContext, failure.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /** Activates the section and loads reviews when needed. */
    fun select() {
        if (reviewItems.value.isEmpty()) onRefresh() else showContent()
    }

    /** Refreshes the review list from its first page. */
    fun refresh() = onRefresh()

    /** Releases review view resources and dismisses transient feedback. */
    fun destroyView() {
        staleSnackbar?.dismiss()
        staleSnackbar = null
        binding?.refreshLayout?.scrollBoundary = null
        binding = null
    }

    /** Resets pagination and requests the first review page. */
    override fun onRefresh() {
        requestInFlight = false
        appendGate.onRequestFailed()
        currentPage = 1
        pageHasNext = null
        load(currentPage)
    }

    /** Requests the next review page. */
    override fun onLoad() = loadNextPage()

    private fun loadNextPage() {
        if (requestInFlight || !appendGate.canRequestNextPage) return
        if (pageHasNext == false) {
            stopRefreshIndicators()
            return
        }
        currentPage++
        currentBinding.refreshLayout.setLoading(true)
        load(currentPage)
    }

    private fun load(page: Int) {
        if (mediaId == 0L || requestInFlight) return
        requestInFlight = true
        appendGate.onRequestStarted()
        requestStartReviewIds = reviewItems.value.mapTo(mutableSetOf()) { it.id }
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        viewModel.load(mediaId = mediaId, type = type, page = page)
    }

    private fun handleReviewVote(review: ReviewRecord, target: ReviewRating) {
        when (viewModel.voteReview(review, target, databaseHelper.currentUser != null)) {
            ReviewVoteActionResult.LOGIN_REQUIRED ->
                NotifyUtil
                    .makeText(
                        appContext,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
            ReviewVoteActionResult.BUSY ->
                NotifyUtil
                    .makeText(appContext, R.string.busy_please_wait, Toast.LENGTH_SHORT)
                    .show()
            ReviewVoteActionResult.SUBMITTED -> Unit
        }
    }

    private fun render(state: ReviewViewModel.UiState.Success) {
        val content = state.content
        renderStaleState(state.isStale)
        pageHasNext = content.pageInfo?.hasNextPage()
        if (!content.isEmpty || state.replaceExisting) {
            reviewItems.value = distinctReviewRows(content.pageData)
        }

        if (!state.isLoading && lastCompletedRequestId != state.requestId) {
            lastCompletedRequestId = state.requestId
            requestInFlight = false
            val newReviewCount = if (state.requestedPage <= 1) {
                reviewItems.value.size
            } else {
                reviewItems.value.count { it.id !in requestStartReviewIds }
            }
            appendGate.onRequestSucceeded(newReviewCount)
            loadMoreRevision.value++
        }

        if (reviewItems.value.isEmpty() && state.replaceExisting) {
            showEmpty()
        } else {
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
