package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomReviewReader
import com.mxt.anitrend.viewmodel.ReviewViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/28.
 * Reviews for a given series
 */
class ReviewFragment : FragmentBaseList<ReviewRecord, PageContainer<ReviewRecord>>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()

    private val reviewViewModel: ReviewViewModel by viewModel()

    private var reviewAdapter: ReviewAdapter? = null
    private var staleSnackbar: Snackbar? = null

    companion object {
        /**
         * Resolves the media identity from the fragment arguments.
         *
         * The typed [MediaScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] / [KeyUtil.arg_mediaType] extras are bridged with
         * their exact raw values (0 or negative ids pass through, mirroring the
         * pre-refactor getter). A typed param present but invalid falls back to the
         * legacy raw values.
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(typed: MediaScreenParam?, legacyId: Long, legacyType: String?): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy values.
            }
            return MediaScreenParam(mediaId = legacyId, mediaType = legacyType)
        }

        @JvmStatic
        fun newInstance(args: Bundle): ReviewFragment = ReviewFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        fromBundle(arguments)?.let { args ->
            mediaId = args.mediaId
            mediaType = args.mediaType
        }
        reviewAdapter =
            ReviewAdapter(
                context = ctx,
                currentUser = databaseHelper.currentUser,
                onRateReviewAction = reviewViewModel::rateReview,
                isMediaType = true,
            ).also { adapter ->
                adapter.clickListener = this
            }
        mColumnSize = R.integer.single_list_x1
        isPager = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reviewViewModel.state.collect { state ->
                    when (state) {
                        is ReviewViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is ReviewViewModel.UiState.Success -> {
                            renderStaleState(state.isStale)
                            handleSuccess(state.content, state.replaceExisting)
                        }
                        is ReviewViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reviewViewModel.rateReviewEvents.collect { outcome ->
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
        super.onDestroyView()
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
        if (mediaId == 0L) {
            return
        }
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        reviewViewModel.load(
            mediaId = mediaId,
            type = type,
            page = mScrollListener.currentPage,
        )
    }

    private fun handleSuccess(
        content: PageContainer<ReviewRecord>,
        replaceExisting: Boolean,
    ) {
        if (content.hasPageInfo()) {
            setPageInfo(content.pageInfo)
        }
        if (!content.isEmpty) {
            reviewAdapter?.submitList(content.pageData) { updateUI() }
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
            R.id.user_avatar -> {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    val intent =
                        Intent(host, ProfileActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyUtil.arg_id, data.value.user?.id ?: return)
                        }
                    CompatUtil.startRevealAnim(host, target, intent)
                } else {
                    context?.let {
                        NotifyUtil
                            .makeText(
                                it,
                                R.string.info_login_req,
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
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
