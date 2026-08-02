package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import com.mxt.anitrend.viewmodel.UserFeedViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/26.
 * user profile targeted feeds
 */
class UserFeedFragment : FeedListFragment() {
    private var userId: Long = 0
    private var userName: String? = null

    private val settings: Settings by inject()
    private val userRepository: UserRepository by inject()

    private val userFeedViewModel: UserFeedViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): UserFeedFragment {
            val args = Bundle(params)
            return UserFeedFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            if (args.containsKey(KeyUtil.arg_id)) {
                userId = args.getLong(KeyUtil.arg_id)
            } else {
                userName = args.getString(KeyUtil.arg_userName)
            }
        }
        isMenuDisabled = true
        isFeed = false
    }

    override fun onToggleLike(feedId: Long) {
        userFeedViewModel.toggleLike(feedId)
    }

    override fun onDeleteFeed(feedId: Long) {
        userFeedViewModel.deleteFeed(feedId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userFeedViewModel.state.collect { state ->
                    when (state) {
                        is UserFeedViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is UserFeedViewModel.UiState.Success -> {
                            handleSuccess(state.items, state.pageInfo)
                        }
                        is UserFeedViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        if (settings.isAuthenticated && isCurrentUser(userId, userName)) {
            userId = userRepository.cachedCurrentUser?.id ?: userId
        }
        if (userId > 0) {
            val args = arguments ?: return
            userFeedViewModel.load(
                userId = userId.toInt(),
                page = mScrollListener.currentPage,
                pageLimit = args.getInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT),
                isFollowing = if (args.containsKey(KeyUtil.arg_isFollowing)) args.getBoolean(KeyUtil.arg_isFollowing) else null,
                type = args.getString(KeyUtil.arg_type)?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() },
                isMixed = if (args.containsKey(KeyUtil.arg_isMixed)) args.getBoolean(KeyUtil.arg_isMixed) else null,
                currentUserId = currentUserId(),
            )
        }
    }

    private fun isCurrentUser(userId: Long, userName: String?): Boolean = settings.isAuthenticated &&
        userRepository.cachedCurrentUser != null &&
        (
            userName?.let { userRepository.cachedCurrentUser?.name == it }
                ?: (userId != 0L && userRepository.cachedCurrentUser?.id == userId)
            )
}
