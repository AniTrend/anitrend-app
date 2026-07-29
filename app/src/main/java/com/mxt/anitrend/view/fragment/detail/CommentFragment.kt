package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.CommentListAdapter
import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseComment
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toUserBase
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.viewmodel.CommentViewModel
import com.mxt.anitrend.data.store.mutation.MutationResult
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CommentFragment : FragmentBaseComment() {
    private val commentViewModel: CommentViewModel by viewModel()
    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()

    private lateinit var feedAdapter: FeedListAdapter
    private lateinit var commentListAdapter: CommentListAdapter

    private sealed interface ComposerMode {
        data class Feed(val feedId: Long) : ComposerMode

        data class Reply(
            val feedId: Long,
            val replyId: Long? = null,
        ) : ComposerMode
    }

    private var composerMode: ComposerMode? = null

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): CommentFragment = CommentFragment().apply {
            arguments = params
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userActivityId = arguments?.getLong(KeyUtil.arg_id) ?: 0L
        mColumnSize = R.integer.single_list_x1
        setInflateMenu(R.menu.custom_menu)

        val ctx = requireContext()
        mAdapter = PlaceholderReplyAdapter(ctx)
        feedAdapter =
            FeedListAdapter(
                experimentalMarkdown = settings.experimentalMarkdown,
                currentUser = databaseHelper.currentUser,
                resolveFeed = { feedId -> currentState().feed?.takeIf { it.id == feedId } },
                onToggleLikeAction = { feedId ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        commentViewModel.toggleFeedLike(feedId)
                    }
                },
                onDeleteFeedAction = { feedId ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        commentViewModel.deleteFeed(feedId)
                    }
                },
                onOpenMedia = { target, _ -> openFeedMedia(target) },
                onOpenComments = {},
                onEditFeed = ::startFeedEdit,
                onShowLikes = { showFeedLikes() },
                onOpenProfile = { target, userId -> openProfile(target, userId) },
                onLongPressMedia = { target, _ -> onFeedMediaLongPressed(target) },
            )
        commentListAdapter =
            CommentListAdapter(
                experimentalMarkdown = settings.experimentalMarkdown,
                currentUser = databaseHelper.currentUser,
                onToggleLike = { replyId ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        commentViewModel.toggleReplyLike(replyId)
                    }
                },
                onDeleteReply = { replyId ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        commentViewModel.deleteReply(replyId)
                    }
                },
                onEditReply = ::startReplyEdit,
                onMentionReply = ::mentionReply,
                onShowLikes = ::showReplyLikes,
                onOpenProfile = { target, userId -> openProfile(target, userId) },
            )
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
        when (item.itemId) {
            R.id.action_share -> {
                val siteUrl = currentState().feed?.siteUrl
                if (!siteUrl.isNullOrBlank()) {
                    startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, siteUrl)
                                type = "text/plain"
                            },
                            getString(R.string.abc_shareactionprovider_share_with),
                        ),
                    )
                } else {
                    context?.let {
                        NotifyUtil.makeText(it, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        configureComposer()
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originRecycler.adapter = feedAdapter
        recyclerView.adapter = commentListAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentViewModel.state.collect { state ->
                    when {
                        state.errorMessage != null && state.feed == null -> showError(state.errorMessage)
                        state.isLoading && state.feed == null -> showLoading()
                        else -> renderState(state)
                    }
                }
            }
        }
    }

    override fun hasContentItems(): Boolean =
        (::feedAdapter.isInitialized && feedAdapter.itemCount > 0) ||
            (::commentListAdapter.isInitialized && commentListAdapter.itemCount > 0)

    override fun updateUI() {
        if (hasContentItems()) {
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

    override fun makeRequest() {
        commentViewModel.load(userActivityId)
    }

    override fun onBackPress(): Boolean {
        if (composerWidget.editBoxHasFocus(true)) {
            return true
        }
        return super.onBackPress()
    }

    override fun onDestroyView() {
        composerWidget.setListener(null)
        composerWidget.onViewRecycled()
        super.onDestroyView()
    }

    override fun onChanged(value: FeedList?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<FeedReply>,
    ) = Unit

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<FeedReply>,
    ) = Unit

    private fun configureComposer() {
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
                                BottomSheetGiphy.Builder()
                                    .setTitle(R.string.title_bottom_sheet_giphy)
                                    .build()
                                    .also { (it as? BottomSheetGiphy)?.onGiphySelected = { giphy -> composerWidget.insertGiphy(giphy) } }
                            showBottomSheet()
                        }
                        R.id.widget_flipper -> Unit
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
        composerWidget.setListener(
            object : ComposerWidget.Listener {
                override fun onSubmit(
                    text: String,
                    requestType: Int,
                    onResult: (Boolean) -> Unit,
                ) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result =
                            when (val mode = composerMode) {
                                is ComposerMode.Feed -> commentViewModel.editFeed(mode.feedId, text)
                                is ComposerMode.Reply -> commentViewModel.submitReply(mode.feedId, text, mode.replyId)
                                null -> MutationResult.Success
                            }
                        val success = result is MutationResult.Success
                        if (success) {
                            currentState().feed?.let(::setReplyComposer)
                        }
                        onResult(success)
                    }
                }
            },
        )
    }

    private fun renderState(state: CommentViewModel.CommentUiState) {
        val headerItems = listOfNotNull(state.feedItem)
        feedAdapter.submitList(headerItems)
        commentListAdapter.submitList(state.replies)

        if (state.feed != null && composerMode == null) {
            setReplyComposer(state.feed)
        }

        if (state.isDeleted) {
            activity?.finish()
            return
        }

        if (state.feed != null) {
            updateUI()
        } else if (!state.isLoading) {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    private fun setReplyComposer(feed: FeedList) {
        composerMode = ComposerMode.Reply(feedId = feed.id)
        composerWidget.setModel(feed, KeyUtil.MUT_SAVE_FEED_REPLY)
    }

    private fun startFeedEdit(feedId: Long) {
        val feed = currentState().feed?.takeIf { it.id == feedId } ?: return
        composerMode = ComposerMode.Feed(feedId)
        composerWidget.setModel(feed, KeyUtil.MUT_SAVE_TEXT_FEED)
        composerWidget.editor.text?.clear()
        composerWidget.setText(feed.text)
    }

    private fun startReplyEdit(replyId: Long) {
        val reply = currentState().replies.firstOrNull { it.id == replyId } ?: return
        composerMode = ComposerMode.Reply(feedId = userActivityId, replyId = replyId)
        composerWidget.setModel(reply.toFeedReply(), KeyUtil.MUT_SAVE_FEED_REPLY)
        composerWidget.editor.text?.clear()
        composerWidget.setText(reply.reply)
    }

    private fun mentionReply(replyId: Long) {
        val reply = currentState().replies.firstOrNull { it.id == replyId } ?: return
        composerWidget.mentionUserFrom(reply.toFeedReply())
    }

    private fun showReplyLikes(replyId: Long) {
        val reply = currentState().replies.firstOrNull { it.id == replyId } ?: return
        val likes = reply.likes.map { it.toUserBase() }
        if (likes.isNotEmpty()) {
            mBottomSheet =
                BottomSheetUsers.Builder()
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

    private fun showFeedLikes() {
        val likes = currentState().feed?.likes.orEmpty()
        if (likes.isNotEmpty()) {
            mBottomSheet =
                BottomSheetUsers.Builder()
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

    private fun openFeedMedia(target: View) {
        val media = currentState().feed?.media ?: return
        val host = activity ?: return
        val intent =
            Intent(host, MediaActivity::class.java).apply {
                putExtra(KeyUtil.arg_id, media.id)
                putExtra(KeyUtil.arg_mediaType, media.type)
            }
        CompatUtil.startRevealAnim(host, target, intent)
    }

    private fun openProfile(
        target: View,
        userId: Long,
    ) {
        val host = activity ?: return
        val intent =
            Intent(host, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(KeyUtil.arg_id, userId)
            }
        CompatUtil.startRevealAnim(host, target, intent)
    }

    private fun onFeedMediaLongPressed(target: View): Boolean {
        if (!settings.isAuthenticated) {
            context?.let {
                NotifyUtil.makeText(
                    it,
                    R.string.info_login_req,
                    R.drawable.ic_group_add_grey_600_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            return false
        }
        val media = currentState().feed?.media ?: return false
        val host = activity ?: return false
        mediaActionUtil = MediaActionUtil.Builder().setId(media.id).build(host)
        mediaActionUtil.startSeriesAction()
        return true
    }

    private fun currentState(): CommentViewModel.CommentUiState = commentViewModel.state.value

    private fun CommentReplyUiModel.toFeedReply(): FeedReply = FeedReply(
        id = id,
        text = reply,
        createdAt = createdAt,
        user = userId?.let { UserBase(name = userName).apply { this.id = it } },
        likes = likes.map { it.toUserBase() },
    )

    private class PlaceholderReplyAdapter(
        context: android.content.Context,
    ) : RecyclerViewAdapter<FeedReply>(context) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerViewHolder<FeedReply> =
            throw UnsupportedOperationException("Placeholder adapter should never create view holders")

        override fun getFilter(): Filter? = null
    }
}
