package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import io.github.wax911.library.model.request.QueryContainerBuilder
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
        fun newInstance(params: Bundle, queryContainer: QueryContainerBuilder): UserFeedFragment {
            val args = Bundle(params).apply {
                putParcelable(KeyUtil.arg_graph_params, queryContainer)
            }
            return UserFeedFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            if (args.containsKey(KeyUtil.arg_id))
                userId = args.getLong(KeyUtil.arg_id)
            else
                userName = args.getString(KeyUtil.arg_userName)
        }
        isMenuDisabled = true
        isFeed = false
    }

    override fun makeRequest() {
        if (presenter.settings.isAuthenticated && presenter.isCurrentUser(userId, userName)) {
            userId = presenter.database.currentUser?.id ?: userId
        }

        if (userId > 0)
            queryContainer.putVariable(KeyUtil.arg_userId, userId)
        else
            queryContainer.putVariable(KeyUtil.arg_userName, userName)

        if (queryContainer.containsKey(KeyUtil.arg_userId))
            super.makeRequest()
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
