package com.mxt.anitrend.view.fragment.list

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.TapTargetUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.CommentActivity
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/11/07.
 * Home page feed base
 */
open class FeedListFragment : FragmentBaseList<FeedList, PageContainer<FeedList>, BasePresenter>(),
    BaseConsumer.onRequestModelChange<FeedList> {

    protected lateinit var queryContainer: co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            queryContainerBuilder: co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder
        ): FeedListFragment {
            val args = Bundle(params).apply {
                putParcelable(KeyUtil.arg_graph_params, queryContainerBuilder)
            }
            return FeedListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        queryContainer = arguments?.parcelable(KeyUtil.arg_graph_params)
            ?: GraphUtil.getDefaultQuery(true)
        isPager = true
        isFeed = true
        mColumnSize = R.integer.single_list_x1
        hasSubscriber = true
        mAdapter = FeedAdapter(ctx)
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_post -> {
                mBottomSheet = BottomSheetComposer.Builder()
                    .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                    .setTitle(R.string.menu_title_new_activity_post)
                    .build()
                showBottomSheet()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        injectAdapter()
        if (!TapTargetUtil.isActive(KeyUtil.KEY_POST_TYPE_TIP) && isFeed) {
            if (presenter.settings.shouldShowTipFor(KeyUtil.KEY_POST_TYPE_TIP)) {
                val host = activity ?: return
                TapTargetUtil.buildDefault(host, R.string.tip_status_post_title, R.string.tip_status_post_text, R.id.action_post)
                    .setPromptStateChangeListener { _, state ->
                        if (state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED ||
                            state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                        ) {
                            presenter.settings.disableTipFor(KeyUtil.KEY_POST_TYPE_TIP)
                        }
                        if (state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED)
                            TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, true)
                    }.show()
                TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, false)
            }
        }
    }

    override fun makeRequest() {
        val ctx = context ?: return
        queryContainer.putVariable(KeyUtil.arg_page, presenter.currentPage)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.FEED_LIST_REQ, ctx)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<FeedList>) {
        when (consumer.requestMode) {
            KeyUtil.MUT_SAVE_TEXT_FEED,
            KeyUtil.MUT_SAVE_MESSAGE_FEED -> {
                if (consumer.changeModel == null) {
                    swipeRefreshLayout.setRefreshing(true)
                    onRefresh()
                } else {
                    val pair = CompatUtil.findIndexOf(mAdapter.data, consumer.changeModel).orElse(null)
                    if (pair != null) {
                        val pairIndex = pair.first
                        mAdapter.onItemChanged(consumer.changeModel, pairIndex)
                    }
                }
            }
            KeyUtil.MUT_DELETE_FEED -> {
                val pair = CompatUtil.findIndexOf(mAdapter.data, consumer.changeModel).orElse(null)
                if (pair != null) {
                    val pairIndex = pair.first
                    mAdapter.onItemRemoved(pairIndex)
                }
            }
        }
    }

    override fun onChanged(content: PageContainer<FeedList>?) {
        if (content != null) {
            if (content.hasPageInfo())
                presenter.setPageInfo(content.pageInfo)
            if (!content.isEmpty)
                onPostProcessed(GraphUtil.filterFeedList(presenter, content.pageData))
            else
                onPostProcessed(emptyList())
        } else
            onPostProcessed(emptyList())
        if (mAdapter.itemCount < 1)
            onPostProcessed(null)
    }

    override fun onItemClick(target: View, data: IntPair<FeedList>) {
        when (target.id) {
            R.id.series_image -> {
                val series = data.second.media ?: return
                val host = activity ?: return
                val intent = Intent(host, MediaActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, series.id)
                    putExtra(KeyUtil.arg_mediaType, series.type)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.widget_comment -> {
                val host = activity ?: return
                val intent = Intent(host, CommentActivity::class.java).apply {
                    putExtra(KeyUtil.arg_model, data.second)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.widget_edit -> {
                mBottomSheet = BottomSheetComposer.Builder().setUserActivity(data.second)
                    .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                    .setTitle(R.string.edit_status_title)
                    .build()
                showBottomSheet()
            }
            R.id.widget_users -> {
                val likes = data.second.likes.orEmpty()
                if (likes.isNotEmpty()) {
                    mBottomSheet = BottomSheetUsers.Builder()
                        .setModel(likes)
                        .setTitle(R.string.title_bottom_sheet_likes)
                        .build()
                    showBottomSheet()
                } else
                    activity?.let {
                        NotifyUtil.makeText(it, R.string.text_no_likes, Toast.LENGTH_SHORT).show()
                    }
            }
            R.id.user_avatar -> {
                val user = data.second.user
                if (user != null) {
                    val host = activity ?: return
                    val intent = Intent(host, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_id, user.id)
                    }
                    CompatUtil.startRevealAnim(host, target, intent)
                }
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<FeedList>) {
        when (target.id) {
            R.id.series_image -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    data.second.media?.let { media ->
                        mediaActionUtil = MediaActionUtil.Builder()
                            .setId(media.id).build(host)
                        mediaActionUtil.startSeriesAction()
                    }
                } else {
                    context?.let {
                        NotifyUtil.makeText(
                            it,
                            R.string.info_login_req,
                            R.drawable.ic_group_add_grey_600_18dp,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        menu.findItem(R.id.action_bookmark).isVisible = true
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val selected = actionMode?.selectedItems.orEmpty()
        when (item.itemId) {
            R.id.action_bookmark -> Unit
            R.id.action_delete -> Unit
        }
        return true
    }
}
