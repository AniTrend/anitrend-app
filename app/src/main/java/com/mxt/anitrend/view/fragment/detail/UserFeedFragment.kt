package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
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

        /**
         * Resolves the user identity from the fragment arguments.
         *
         * The typed [UserScreenParam] wins when present; otherwise the legacy
         * containsKey semantics are preserved exactly: an explicit `arg_id` extra
         * resolves by id, otherwise the `arg_userName` extra resolves by name.
         */
        fun fromBundle(bundle: Bundle?): UserScreenParam? = resolve(
            typed = bundle?.screenParam<UserScreenParam>(),
            hasLegacyId = bundle?.containsKey(KeyUtil.arg_id) == true,
            legacyId = bundle?.getLong(KeyUtil.arg_id, 0L) ?: 0L,
            legacyName = bundle?.getString(KeyUtil.arg_userName),
        )

        @VisibleForTesting
        internal fun resolve(
            typed: UserScreenParam?,
            hasLegacyId: Boolean,
            legacyId: Long,
            legacyName: String?,
        ): UserScreenParam? {
            typed?.let { return it }
            return if (hasLegacyId) {
                UserScreenParam(userId = legacyId)
            } else {
                UserScreenParam(userId = 0L, initialName = legacyName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromBundle(arguments)?.let { args ->
            userId = args.userId
            userName = args.initialName
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
                pageLimit = NavigationArgs.intWithDefault(args.containsKey(KeyUtil.arg_page_limit), args.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
                isFollowing = NavigationArgs.optionalBoolean(args.containsKey(KeyUtil.arg_isFollowing), args.getBoolean(KeyUtil.arg_isFollowing)),
                type = NavigationArgs.resolveActivityType(args.getString(KeyUtil.arg_type)),
                isMixed = NavigationArgs.optionalBoolean(args.containsKey(KeyUtil.arg_isMixed), args.getBoolean(KeyUtil.arg_isMixed)),
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
