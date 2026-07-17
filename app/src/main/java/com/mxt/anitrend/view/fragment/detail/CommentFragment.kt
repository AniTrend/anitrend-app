package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.CommentAdapter
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.fragment.FragmentBaseComment
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.extension.hideKeyboard
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/11/16.
 * Comment fragment
 */
class CommentFragment :
    FragmentBaseComment(),
    BaseConsumer.onRequestModelChange<FeedReply> {
    private lateinit var feedAdapter: FeedAdapter

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): CommentFragment = CommentFragment().apply {
            arguments = params
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            if (args.containsKey(KeyUtil.arg_model)) {
                feedList = args.parcelable(KeyUtil.arg_model)
            }
            if (args.containsKey(KeyUtil.arg_id)) {
                userActivityId = args.getLong(KeyUtil.arg_id)
            }
        }
        mColumnSize = R.integer.single_list_x1
        hasSubscriber = true
        setInflateMenu(R.menu.custom_menu)
        mAdapter = CommentAdapter(ctx)
        feedAdapter = FeedAdapter(ctx)
        setPresenter(WidgetPresenter<FeedList>(ctx))
        setViewModel(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_favourite).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (feedList != null) {
            when (item.itemId) {
                R.id.action_share -> {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, feedList?.siteUrl)
                            type = "text/plain"
                        }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                }
            }
        } else {
            context?.let {
                NotifyUtil.makeText(it, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        composerWidget.lifecycle = viewLifecycleOwner.lifecycle
        composerWidget.itemClickListener =
            object : ItemClickListener<Any> {
                override fun onItemClick(
                    target: View,
                    data: IndexedValue<Any>,
                ) {
                    when (target.id) {
                        R.id.insert_emoticon -> Unit
                        R.id.insert_gif -> {
                            mBottomSheet =
                                BottomSheetGiphy
                                    .Builder()
                                    .setTitle(R.string.title_bottom_sheet_giphy)
                                    .build()

                            showBottomSheet()
                        }
                        R.id.widget_flipper -> activity?.hideKeyboard()
                        else -> {
                            context?.let {
                                DialogUtil.createDialogAttachMedia(target.id, composerWidget.editor, it)
                            }
                        }
                    }
                }

                override fun onItemLongClick(
                    target: View,
                    data: IndexedValue<Any>,
                ) = Unit
            }
        super.onStart()
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val currentFeed = feedList
        if (currentFeed != null) {
            userActivityId = currentFeed.id
            initExtraComponents()
        }

        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, userActivityId)
            putBoolean(KeyUtil.arg_asHtml, false)
        }
        viewModel?.requestData(KeyUtil.FEED_LIST_REPLY_REQ, ctx)
    }

    override fun onBackPress(): Boolean {
        if (composerWidget.editBoxHasFocus(true)) {
            return true
        }
        return super.onBackPress()
    }

    private fun initExtraComponents() {
        val feedList = feedList ?: return
        composerWidget.setModel(feedList, KeyUtil.MUT_SAVE_FEED_REPLY)

        if (feedAdapter.itemCount < 1) {
            feedAdapter.onItemsInserted(listOf(feedList))
            feedAdapter.setClickListener(
                object : ItemClickListener<FeedList> {
                    override fun onItemClick(
                        target: View,
                        data: IndexedValue<FeedList>,
                    ) {
                        when (target.id) {
                            R.id.series_image -> {
                                val media = data.value.media ?: return
                                val host = activity ?: return
                                val intent =
                                    Intent(host, MediaActivity::class.java).apply {
                                        putExtra(KeyUtil.arg_id, media.id)
                                        putExtra(KeyUtil.arg_mediaType, media.type)
                                    }
                                CompatUtil.startRevealAnim(host, target, intent)
                            }
                            R.id.widget_users -> {
                                val likes = data.value.likes.orEmpty()
                                if (likes.isNotEmpty()) {
                                    mBottomSheet =
                                        BottomSheetUsers
                                            .Builder()
                                            .setModel(likes)
                                            .setTitle(R.string.title_bottom_sheet_likes)
                                            .build()
                                    showBottomSheet()
                                } else {
                                    activity?.let {
                                        NotifyUtil.makeText(it, R.string.text_no_likes, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            R.id.user_avatar -> {
                                data.value.user?.let { user ->
                                    val host = activity ?: return
                                    val intent =
                                        Intent(host, ProfileActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra(KeyUtil.arg_id, user.id)
                                        }
                                    CompatUtil.startRevealAnim(host, target, intent)
                                }
                            }
                            R.id.recipient_avatar -> {
                                data.value.recipient?.let { recipient ->
                                    val host = activity ?: return
                                    val intent =
                                        Intent(host, ProfileActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra(KeyUtil.arg_id, recipient.id)
                                        }
                                    CompatUtil.startRevealAnim(host, target, intent)
                                }
                            }
                            R.id.messenger_avatar -> {
                                data.value.messenger?.let { messenger ->
                                    val host = activity ?: return
                                    val intent =
                                        Intent(host, ProfileActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra(KeyUtil.arg_id, messenger.id)
                                        }
                                    CompatUtil.startRevealAnim(host, target, intent)
                                }
                            }
                        }
                    }

                    override fun onItemLongClick(
                        target: View,
                        data: IndexedValue<FeedList>,
                    ) {
                        when (target.id) {
                            R.id.series_image -> {
                                if (presenter.settings.isAuthenticated) {
                                    val host = activity ?: return
                                    data.value.media?.let { media ->
                                        mediaActionUtil =
                                            MediaActionUtil
                                                .Builder()
                                                .setId(media.id)
                                                .build(host)
                                        mediaActionUtil.startSeriesAction()
                                    }
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
                },
            )
        }
        if (originRecycler.adapter == null) {
            originRecycler.adapter = feedAdapter
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<FeedReply>) {
        when (consumer.requestMode) {
            KeyUtil.MUT_SAVE_FEED_REPLY -> {
                if (consumer.changeModel == null) {
                    if (mAdapter.itemCount > 1) {
                        swipeRefreshLayout.setRefreshing(true)
                    }
                    onRefresh()
                } else {
                    val pair = CompatUtil.findIndexOf(mAdapter.data, consumer.changeModel)
                    if (pair != null) {
                        val pairIndex = pair.index
                        mAdapter.onItemChanged(consumer.changeModel, pairIndex)
                    }
                }
            }
            KeyUtil.MUT_DELETE_FEED_REPLY -> {
                val pair = CompatUtil.findIndexOf(mAdapter.data, consumer.changeModel)
                if (pair != null) {
                    val pairIndex = pair.index
                    mAdapter.onItemRemoved(pairIndex)
                }
            }
            KeyUtil.MUT_DELETE_FEED -> activity?.finish()
        }
        initExtraComponents()
    }

    override fun onDestroyView() {
        composerWidget.onViewRecycled()
        super.onDestroyView()
    }

    override fun onChanged(value: FeedList?) {
        super.onChanged(value)
        if (value != null) {
            feedList = value
            initExtraComponents()
        } else {
            activity?.let {
                NotifyUtil.createAlerter(
                    it,
                    R.string.text_error_request,
                    R.string.layout_empty_response,
                    R.drawable.ic_warning_white_18dp,
                    R.color.colorStateOrange,
                )
            }
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<FeedReply>,
    ) {
        val feedList = feedList ?: return
        when (target.id) {
            R.id.series_image -> {
                val mediaBase = feedList.media ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, mediaBase.id)
                        putExtra(KeyUtil.arg_mediaType, mediaBase.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.widget_mention -> composerWidget.mentionUserFrom(data.value)
            R.id.widget_edit -> {
                composerWidget.setModel(data.value, KeyUtil.MUT_SAVE_FEED_REPLY)
                composerWidget.setText(data.value.reply)
            }
            R.id.widget_users -> {
                val likes = data.value.likes.orEmpty()
                if (likes.isNotEmpty()) {
                    mBottomSheet =
                        BottomSheetUsers
                            .Builder()
                            .setModel(likes)
                            .setTitle(R.string.title_bottom_sheet_likes)
                            .build()
                    showBottomSheet()
                } else {
                    activity?.let {
                        NotifyUtil.makeText(it, R.string.text_no_likes, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.user_avatar -> {
                data.value.user?.let { user ->
                    val host = activity ?: return
                    val intent =
                        Intent(host, ProfileActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyUtil.arg_id, user.id)
                        }
                    CompatUtil.startRevealAnim(host, target, intent)
                }
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<FeedReply>,
    ) = Unit
}
