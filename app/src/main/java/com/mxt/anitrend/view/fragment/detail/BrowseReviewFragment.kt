package com.mxt.anitrend.view.fragment.detail

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.sheet.BottomReviewReader
import com.mxt.anitrend.viewmodel.BrowseReviewViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/10/30.
 * Media review browse
 */
class BrowseReviewFragment : FragmentBaseList<ReviewRecord, PageContainer<ReviewRecord>>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null

    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()

    private val browseReviewViewModel: BrowseReviewViewModel by viewModel()

    private var reviewAdapter: ReviewAdapter? = null

    companion object {
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
         * identity. It stays on arg_mediaType (set per tab by ReviewPageAdapter)
         * until a browse-review state model is designed. Reads mirror the
         * pre-refactor getter exactly (absent → null).
         */
        fun fromBundle(bundle: Bundle?): String? = resolveLegacyType(bundle?.getString(KeyUtil.arg_mediaType))

        @VisibleForTesting
        internal fun resolveLegacyType(raw: String?): String? = raw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        mediaType = fromBundle(arguments)
        isPager = true
        mColumnSize = R.integer.single_list_x1
        isFilterableEnabled = true
        reviewAdapter =
            ReviewAdapter(
                context = ctx,
                currentUser = databaseHelper.currentUser,
                onRateReviewAction = browseReviewViewModel::rateReview,
            ).also { adapter ->
                adapter.clickListener = this
            }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseReviewViewModel.state.collect { state ->
                    when (state) {
                        is BrowseReviewViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is BrowseReviewViewModel.UiState.Success -> {
                            handleSuccess(state.content, state.replaceExisting)
                        }
                        is BrowseReviewViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseReviewViewModel.rateReviewEvents.collect { outcome ->
                    reviewAdapter?.onRateReviewResult(outcome.reviewId, outcome.result)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showLoading()
        if ((reviewAdapter?.itemCount ?: 0) < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun updateUI() {
        val adapter = reviewAdapter ?: return
        if (adapter.itemCount > 0) {
            if (recyclerView.adapter !== adapter) {
                recyclerView.adapter = adapter
            }
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

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (key != null && isFilterableEnabled && GraphUtil.isKeyFilter(key)) {
            showLoading()
            reviewAdapter?.submitList(emptyList())
            onRefresh()
        }
    }

    override fun makeRequest() {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val sort = settings.reviewSort + settings.sortOrder
        browseReviewViewModel.load(
            type = type,
            page = mScrollListener.currentPage,
            sort = sort,
        )
    }

    private fun handleSuccess(
        value: PageContainer<ReviewRecord>,
        replaceExisting: Boolean,
    ) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            reviewAdapter?.submitList(value.pageData) { updateUI() }
        } else if (replaceExisting) {
            reviewAdapter?.submitList(emptyList()) { updateUI() }
        } else if (isPager) {
            setLimitReached()
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<ReviewRecord>?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<ReviewRecord>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                val mediaBase: MediaSummaryRecord? = data.value.media
                val host = activity ?: return
                val intent = MediaActivity.newIntent(host, mediaBase?.id ?: return, mediaBase?.type)
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.review_read_more -> {
                mBottomSheet =
                    BottomReviewReader
                        .Builder()
                        .setReview(data.value)
                        .setTitle(R.string.drawer_title_reviews)
                        .build()
                showBottomSheet()
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<ReviewRecord>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.value.media?.id ?: return)
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
