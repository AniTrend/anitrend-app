package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import com.mxt.anitrend.viewmodel.MediaFeedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/24.
 * Media feed list fragment for media types, both anime and manga
 */
class MediaFeedFragment : FeedListFragment() {
    private val mediaFeedViewModel: MediaFeedViewModel by viewModel()

    private var mediaId: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaFeedFragment {
            val args =
                Bundle(params).apply {
                    putLong(KeyUtil.arg_mediaId, params.getLong(KeyUtil.arg_id))
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                }
            return MediaFeedFragment().apply {
                arguments = args
            }
        }

        /**
         * Resolves the media identity from the fragment arguments.
         *
         * The typed [MediaScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_mediaId] extra is bridged with its exact raw value
         * (0 or negative ids pass through, mirroring the pre-refactor getter).
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyMediaId = bundle?.getLong(KeyUtil.arg_mediaId, 0) ?: 0,
        )

        @VisibleForTesting
        internal fun resolve(typed: MediaScreenParam?, legacyMediaId: Long): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy value.
            }
            return MediaScreenParam(mediaId = legacyMediaId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMenuDisabled = true
        isFeed = false
        mediaId = fromBundle(arguments)?.mediaId ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaFeedViewModel.state.collect { state ->
                    when (state) {
                        is MediaFeedViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaFeedViewModel.UiState.Success -> {
                            handleSuccess(state.items, state.pageInfo)
                        }
                        is MediaFeedViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        val args = arguments ?: return
        mediaFeedViewModel.load(
            mediaId = mediaId,
            isFollowing = NavigationArgs.booleanWithDefault(args.containsKey(KeyUtil.arg_isFollowing), args.getBoolean(KeyUtil.arg_isFollowing), default = true),
            page = mScrollListener.currentPage,
            pageLimit = NavigationArgs.intWithDefault(args.containsKey(KeyUtil.arg_page_limit), args.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
            currentUserId = currentUserId(),
        )
    }

    override fun onToggleLike(feedId: Long) {
        mediaFeedViewModel.toggleLike(feedId)
    }

    override fun onDeleteFeed(feedId: Long) {
        mediaFeedViewModel.deleteFeed(feedId)
    }
}
