package com.mxt.anitrend.view.fragment.detail

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.review.ReviewImageAction
import com.mxt.anitrend.ui.review.ReviewList
import com.mxt.anitrend.ui.review.distinctReviewRows
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.view.sheet.BottomReviewReader
import com.mxt.anitrend.viewmodel.BrowseReviewViewModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.viewmodel.ReviewVoteActionResult
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/10/30.
 * Media review browse
 */
@Suppress("TooManyFunctions") // Lifecycle, navigation, and review list responsibilities stay centralized.
open class BrowseReviewFragment : FragmentBaseList<ReviewRecord, PageContainer<ReviewRecord>>() {
    @KeyUtil.MediaType
    protected var mediaType: String? = null

    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()

    private val browseReviewViewModel: BrowseReviewViewModel by viewModel()

    private val reviewItems = mutableStateOf<List<ReviewRecord>>(emptyList())
    private val reviewListState = LazyListState()
    private val loadMoreRevision = mutableStateOf(0)
    private val appendGate = ManualGridAppendGate()
    private var requestInFlight = false
    private var requestStartReviewIds: Set<Long> = emptySet()
    private var lastCompletedRequestId = Long.MIN_VALUE
    private var staleSnackbar: Snackbar? = null

    /** Factory and argument helpers for the review browse destination. */
    companion object {
        /** Creates a review browse fragment for the supplied media type. */
        @JvmStatic
        fun newInstance(
            @KeyUtil.MediaType mediaType: String,
        ): BrowseReviewFragment {
            val args =
                Bundle().apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                }
            return BrowseReviewFragment().apply {
                arguments = args
            }
        }

        /**
         * Documented legacy channel: the review media type is filter state, not
         * identity. It stays on arg_mediaType (set by the local section selector)
         * as presentation state. Reads mirror the pre-refactor getter exactly
         * (absent → null).
         */
        fun fromBundle(bundle: Bundle?): String? = resolveLegacyType(bundle?.getString(KeyUtil.arg_mediaType))

        @VisibleForTesting
        internal fun resolveLegacyType(raw: String?): String? = raw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaType = fromBundle(arguments)
        isPager = true
        isFilterableEnabled = true
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_genre).isVisible = false
        menu.findItem(R.id.action_tag).isVisible = false
        menu.findItem(R.id.action_type).isVisible = false
        menu.findItem(R.id.action_year).isVisible = false
        menu.findItem(R.id.action_status).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                val reviewSortTypes = KeyUtil.ReviewSortType
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(reviewSortTypes, settings.reviewSort),
                    CompatUtil.capitalizeWords(reviewSortTypes),
                ) { dialog, _ ->
                    settings.reviewSort =
                        reviewSortTypes.getOrNull(dialog.selectedIndex)
                }
                return true
            }
            R.id.action_order -> {
                val sortOrders = arrayOf(KeyUtil.ASC, KeyUtil.DESC)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_order,
                    CompatUtil.getIndexOf(sortOrders, settings.sortOrder),
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                ) { dialog, _ ->
                    settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: settings.sortOrder,
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachComposeList()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseReviewViewModel.state.collect { state ->
                    when (state) {
                        is BrowseReviewViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is BrowseReviewViewModel.UiState.Success -> handleSuccess(state)
                        is BrowseReviewViewModel.UiState.Error -> {
                            requestInFlight = false
                            appendGate.onRequestFailed()
                            showError(state.message)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseReviewViewModel.rateReviewEvents.collect { outcome ->
                    val failure = outcome.result as? MutationResult.Failure ?: return@collect
                    context?.let { host ->
                        NotifyUtil.makeText(host, failure.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun attachComposeList() {
        val host =
            ComposeView(requireContext()).apply {
                id = R.id.container
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val voteStates = browseReviewViewModel.reviewVoteState.collectAsState().value
                    AniTrendTheme {
                        ReviewList(
                            reviews = reviewItems.value,
                            isBanner = true,
                            voteStates = voteStates,
                            listState = reviewListState,
                            onVote = ::handleReviewVote,
                            onImageAction = ::handleReviewImageAction,
                            onReviewClick = ::handleReadMoreClick,
                            loadMoreRevision = loadMoreRevision.value,
                            onLoadMore = ::loadNextPage,
                        )
                    }
                }
            }
        swipeRefreshLayout.addView(
            host,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        swipeRefreshLayout.scrollBoundary = { direction ->
            when (direction) {
                -1 -> reviewListState.canScrollBackward
                1 -> reviewListState.canScrollForward
                else -> false
            }
        }
    }

    private fun handleReviewVote(review: ReviewRecord, target: ReviewRating) {
        when (browseReviewViewModel.voteReview(review, target, databaseHelper.currentUser != null)) {
            ReviewVoteActionResult.LOGIN_REQUIRED -> context?.let { host ->
                NotifyUtil
                    .makeText(
                        host,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
            ReviewVoteActionResult.BUSY -> context?.let { host ->
                NotifyUtil.makeText(host, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            }
            ReviewVoteActionResult.SUBMITTED -> Unit
        }
    }

    private fun handleReadMoreClick(target: View, review: ReviewRecord) {
        val position = reviewItems.value.indexOfFirst { it.id == review.id }
        if (position >= 0 && target.id == R.id.review_read_more) {
            mBottomSheet =
                BottomReviewReader
                    .Builder()
                    .setReview(review)
                    .setOnUserClick(::navigateToProfile)
                    .setTitle(R.string.drawer_title_reviews)
                    .build()
            showBottomSheet()
        }
    }

    private fun loadNextPage() {
        if (requestInFlight || !appendGate.canRequestNextPage) return
        if (pageState.getPageInfo()?.hasNextPage() == false) {
            setLimitReached()
            return
        }
        pageState.currentPage++
        pageState.currentOffset += KeyUtil.PAGING_LIMIT
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }

    override fun onLoad() = loadNextPage()

    override fun onRefresh() {
        requestInFlight = false
        appendGate.onRequestFailed()
        super.onRefresh()
    }

    override fun onStart() {
        super.onStart()
        if (reviewItems.value.isEmpty()) {
            showLoading()
            onRefresh()
        } else {
            updateUI()
        }
    }

    private fun renderStaleState(isStale: Boolean) {
        if (isStale) {
            if (staleSnackbar?.isShown == true) return
            staleSnackbar =
                Snackbar
                    .make(stateLayout, R.string.review_stale_message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.review_stale_refresh) {
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

    override fun onDestroyView() {
        staleSnackbar?.dismiss()
        staleSnackbar = null
        swipeRefreshLayout.scrollBoundary = null
        super.onDestroyView()
    }

    override fun updateUI() {
        if (reviewItems.value.isNotEmpty()) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            } else if (swipeRefreshLayout.isLoading()) {
                swipeRefreshLayout.setLoading(false)
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    protected fun clearReviewItems() {
        reviewItems.value = emptyList()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (key != null && isFilterableEnabled && GraphUtil.isKeyFilter(key)) {
            clearReviewItems()
            showLoading()
            onRefresh()
        }
    }

    override fun makeRequest() {
        if (requestInFlight) return
        requestInFlight = true
        appendGate.onRequestStarted()
        requestStartReviewIds = reviewItems.value.mapTo(mutableSetOf()) { it.id }
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val sort = settings.reviewSort + settings.sortOrder
        browseReviewViewModel.load(
            type = type,
            page = pageState.currentPage,
            sort = sort,
        )
    }

    private fun handleSuccess(state: BrowseReviewViewModel.UiState.Success) {
        renderStaleState(state.isStale)
        val value = state.content
        if (value.hasPageInfo()) {
            pageState.setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty || state.replaceExisting) {
            reviewItems.value = distinctReviewRows(value.pageData)
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
        if (value.isEmpty && !state.replaceExisting && isPager) {
            setLimitReached()
        }
        updateUI()
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<ReviewRecord>?) = Unit

    private fun handleReviewImageAction(action: ReviewImageAction, review: ReviewRecord) {
        when (action) {
            ReviewImageAction.OPEN_MEDIA -> review.media?.let { media ->
                navigateToMedia(MediaScreenParam(media.id, media.type))
            }
            ReviewImageAction.OPEN_PROFILE -> review.user?.id?.let { navigateToProfile(UserScreenParam(it)) }
            ReviewImageAction.LONG_PRESS_MEDIA -> {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    val mediaId = review.media?.id ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(mediaId)
                            .build(host)
                    mediaActionUtil.startSeriesAction()
                } else {
                    context?.let {
                        NotifyUtil
                            .makeText(
                                it,
                                R.string.info_login_req,
                                R.drawable.ic_group_add_grey_600_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            }
        }
    }
}
