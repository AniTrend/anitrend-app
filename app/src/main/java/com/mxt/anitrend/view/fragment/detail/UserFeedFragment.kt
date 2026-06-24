package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/11/26.
 * user profile targeted feeds
 */
class UserFeedFragment : FeedListFragment() {
    private var userId: Long = 0
    private var userName: String? = null

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

    override fun makeRequest() {
        if (presenter.settings.isAuthenticated && presenter.isCurrentUser(userId, userName)) {
            userId = presenter.database.currentUser?.id ?: userId
        }

        if (userId > 0) {
            val params = viewModel?.params ?: return
            params.applyBaseFeedRequestArguments(arguments)
            params.remove(KeyUtil.arg_userId)
            params.putLong(KeyUtil.arg_userId, userId)
            params.putInt(KeyUtil.arg_page, presenter.currentPage)
            viewModel?.requestData(KeyUtil.FEED_LIST_REQ, context ?: return)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onUserChange(consumer: BaseConsumer<UserBase>) {
        if (consumer.requestMode == KeyUtil.USER_BASE_REQ && userId == 0L) {
            val user = consumer.changeModel
            if (user != null) {
                userId = user.id
                makeRequest()
            }
        }
    }
}
