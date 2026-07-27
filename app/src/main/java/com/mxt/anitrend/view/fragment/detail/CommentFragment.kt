package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.CommentAdapter
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseComment
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.extension.hideKeyboard
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.repository.BaseMutation
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.FeedMutation
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.CommentActivity
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Created by max on 2017/11/16.
 * Comment fragment
 */
class CommentFragment : FragmentBaseComment() {
    private lateinit var feedAdapter: FeedAdapter

    private val mutationCoordinator by inject<WidgetMutationCoordinator>()

    private val baseRepository: BaseRepository by inject()

    private val feedRepository: FeedRepository by inject()

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
        setInflateMenu(R.menu.custom_menu)
        mAdapter = CommentAdapter(ctx, mutationCoordinator)
        feedAdapter = FeedAdapter(ctx, mutationCoordinator)
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
                                    .also { (it as? BottomSheetGiphy)?.onGiphySelected = { giphy -> composerWidget.insertGiphy(giphy) } }

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
        composerWidget.setListener(object : ComposerWidget.Listener {
            override fun onSubmit(
                text: String,
                @KeyUtil.RequestType requestType: Int,
                onResult: (Boolean) -> Unit,
            ) {
                lifecycleScope.launch {
                    val success = when (requestType) {
                        KeyUtil.MUT_SAVE_FEED_REPLY -> {
                            feedRepository.saveActivityReply(
                                id = null,
                                activityId = feedList?.id ?: 0,
                                text = text,
                                asHtml = false,
                            ).onSuccess { reply ->
                                appendReply(reply)
                            }.isSuccess
                        }
                        KeyUtil.MUT_SAVE_TEXT_FEED -> {
                            feedRepository.saveTextActivity(
                                id = feedList?.id,
                                text = text,
                                asHtml = false,
                            ).isSuccess
                        }
                        else -> false
                    }
                    onResult(success)
                }
            }
        })
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    feedRepository.mutationEvents.collect(::handleFeedMutation)
                }
                launch {
                    baseRepository.mutationEvents.collect(::handleBaseMutation)
                }
            }
        }
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

        lifecycleScope.launch {
            feedRepository
                .getFeedListReply(id = userActivityId, asHtml = false)
                .onSuccess(::onChanged)
                .onFailure { throwable ->
                    Timber.e(throwable)
                    showError(throwable.message ?: getString(R.string.text_error_request))
                }
        }
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

        feedAdapter.onItemsInserted(listOf(feedList))
        if (feedAdapter.clickListener == null) {
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

    override fun onDestroyView() {
        composerWidget.setListener(null)
        composerWidget.onViewRecycled()
        super.onDestroyView()
    }

    override fun onChanged(value: FeedList?) {
        super.onChanged(value)
        if (value != null) {
            feedList = value
            initExtraComponents()
            publishUpdatedFeedResult()
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

    private fun handleFeedMutation(event: FeedMutation) {
        when (event) {
            is FeedMutation.FeedSaved -> {
                val currentFeed = feedList ?: return
                if (currentFeed.id == event.feed.id) {
                    event.feed.replies = currentFeed.replies
                    feedList = event.feed
                    initExtraComponents()
                    publishUpdatedFeedResult()
                }
            }
            is FeedMutation.ReplyDeleted -> {
                val currentFeed = feedList ?: return
                val replies = currentFeed.replies.orEmpty()
                if (replies.any { it.id == event.id }) {
                    val updatedReplies = replies.filterNot { it.id == event.id }
                    currentFeed.replies = updatedReplies
                    currentFeed.replyCount = updatedReplies.size
                    mAdapter.onItemsInserted(updatedReplies)
                    initExtraComponents()
                    publishUpdatedFeedResult()
                }
            }
            else -> Unit
        }
    }

    private fun handleBaseMutation(event: BaseMutation) {
        when (event) {
            is BaseMutation.LikeToggled -> {
                when (event.targetType) {
                    LikeableType.ACTIVITY -> {
                        val currentFeed = feedList ?: return
                        if (currentFeed.id == event.targetId) {
                            currentFeed.likes = event.users
                            initExtraComponents()
                            publishUpdatedFeedResult()
                        }
                    }
                    LikeableType.ACTIVITY_REPLY -> {
                        val currentFeed = feedList ?: return
                        val replies = currentFeed.replies.orEmpty().toMutableList()
                        val index = replies.indexOfFirst { it.id == event.targetId }
                        if (index >= 0) {
                            replies[index].likes = event.users
                            currentFeed.replies = replies
                            mAdapter.onItemsInserted(replies)
                        }
                    }
                    else -> Unit
                }
            }
            else -> Unit
        }
    }

    private fun appendReply(reply: FeedReply) {
        val currentFeed = feedList ?: return
        val replies = currentFeed.replies.orEmpty().toMutableList()
        val index = replies.indexOfFirst { it.id == reply.id }
        if (index >= 0) {
            replies[index] = reply
        } else {
            replies.add(reply)
        }
        currentFeed.replies = replies
        currentFeed.replyCount = replies.size
        mAdapter.onItemsInserted(replies)
        initExtraComponents()
        publishUpdatedFeedResult()
    }

    private fun publishUpdatedFeedResult() {
        val currentFeed = feedList ?: return
        (activity as? CommentActivity)?.updateResult(currentFeed)
    }
}
