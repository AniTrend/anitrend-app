package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
    private val databaseHelper by lazy { DatabaseHelper() }

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
                            handleSuccess(state.content)
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
            userId = databaseHelper.currentUser?.id ?: userId
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
            )
        }
    }

    private fun isCurrentUser(userId: Long, userName: String?): Boolean = settings.isAuthenticated &&
        databaseHelper.currentUser != null &&
        (
            userName?.let { databaseHelper.currentUser?.name == it }
                ?: (userId != 0L && databaseHelper.currentUser?.id == userId)
            )

    private fun handleSuccess(value: PageContainer<FeedList>) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            val filtered = value.pageData.filter { !it.type.isNullOrBlank() }
            mScrollListener.getPageInfo()?.perPage = filtered.size
            onPostProcessed(filtered)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }
}
