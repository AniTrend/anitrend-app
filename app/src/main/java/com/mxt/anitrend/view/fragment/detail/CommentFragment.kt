package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toUserBase
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.databinding.FragmentCommentBinding
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.toFeedReplyRecord
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.CommentReplyRowCallbacks
import com.mxt.anitrend.ui.feed.CommentThreadLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.viewmodel.CommentViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CommentFragment :
    FragmentBase<FeedReply, FeedList>(),
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private val commentViewModel: CommentViewModel by viewModel()
    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val commentListState = LazyListState()
    private val commentUiState = mutableStateOf<CommentViewModel.CommentUiState?>(null)
    private var userActivityId: Long = 0L

    private sealed interface ComposerMode {
        data class Feed(val feedId: Long) : ComposerMode

        data class Reply(
            val feedId: Long,
            val replyId: Long? = null,
        ) : ComposerMode
    }

    private var composerMode: ComposerMode? = null

    private val feedCallbacks =
        FeedRowCallbacks(
            onToggleLike = { feedId -> viewLifecycleOwner.lifecycleScope.launch { commentViewModel.toggleFeedLike(feedId) } },
            onDelete = ::deleteCurrentFeed,
            onOpenMedia = ::openFeedMedia,
            onOpenComments = {},
            onEdit = { item -> startFeedEdit(item.id) },
            onShowLikes = { item -> showFeedLikes(item.id) },
            onOpenProfile = { _, id -> openProfile(id) },
            onLongPressMedia = { _, item -> onFeedMediaLongPressed(item.id) },
        )

    private val replyCallbacks =
        CommentReplyRowCallbacks(
            onToggleLike = { replyId -> viewLifecycleOwner.lifecycleScope.launch { commentViewModel.toggleReplyLike(replyId) } },
            onDelete = ::deleteCurrentReply,
            onEdit = ::startReplyEdit,
            onMention = ::mentionReply,
            onShowLikes = ::showReplyLikes,
            onOpenProfile = { _, id -> openProfile(id) },
        )

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): CommentFragment = CommentFragment().apply {
            arguments = params
        }

        /** Legacy compatibility read for callers that still provide arg_id. */
        fun fromBundle(bundle: Bundle?): Long = bundle?.getLong(KeyUtil.arg_id) ?: 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userActivityId = arguments?.screenParam<CommentScreenParam>()?.feedId ?: fromBundle(arguments)
        setInflateMenu(R.menu.custom_menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        binding.refreshLayout.setOnRefreshAndLoadListener(this)
        binding.refreshLayout.setPermitLoad(false)
        binding.refreshLayout.scrollBoundary = { direction ->
            when (direction) {
                -1 -> commentListState.canScrollBackward
                1 -> commentListState.canScrollForward
                else -> false
            }
        }
        binding.commentList.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.commentList.setContent {
            AniTrendTheme {
                val state = commentUiState.value
                CommentThreadLazyList(
                    feed = state?.feedItem,
                    replies = state?.replies.orEmpty(),
                    currentUserId = state?.currentUserId,
                    listState = commentListState,
                    feedCallbacks = feedCallbacks,
                    replyCallbacks = replyCallbacks,
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentViewModel.state.collect(::renderState)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_favourite)?.isVisible = false
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
                return true
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        configureComposer()
        if (currentState().feed == null) {
            showLoading()
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun updateUI() {
        val current = _binding ?: return
        if (currentState().feedItem != null) {
            finishRefreshIndicators(current)
            current.stateLayout.showContent()
        } else if (!currentState().isLoading) {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    override fun makeRequest() {
        commentViewModel.load(userActivityId, databaseHelper.currentUser?.id)
    }

    override fun onRefresh() {
        binding.refreshLayout.setLoading(false)
        makeRequest()
    }

    override fun onLoad() = Unit

    override fun showError(error: String) {
        super.showError(error)
        val current = _binding ?: return
        finishRefreshIndicators(current)
        current.stateLayout.showError(
            context?.getCompatDrawable(R.drawable.ic_emoji_cry),
            error,
            getString(R.string.try_again),
        ) { onRefresh() }
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        val current = _binding ?: return
        finishRefreshIndicators(current)
        current.stateLayout.showError(
            context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
            message,
            getString(R.string.try_again),
        ) { onRefresh() }
    }

    override fun onDestroyView() {
        _binding?.apply {
            refreshLayout.setOnRefreshAndLoadListener(null)
            refreshLayout.scrollBoundary = null
            composerWidget.setListener(null)
            composerWidget.onViewRecycled()
        }
        commentUiState.value = null
        _binding = null
        super.onDestroyView()
    }

    override fun onChanged(value: FeedList?) = Unit

    private fun configureComposer() {
        val composerWidget = binding.composerWidget
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
                                BottomSheetGiphy().apply {
                                    arguments = Bundle().apply {
                                        putInt(KeyUtil.arg_title, R.string.title_bottom_sheet_giphy)
                                    }
                                }.also { it.onGiphySelected = { giphy -> composerWidget.insertGiphy(giphy) } }
                            showBottomSheet()
                        }
                        R.id.widget_flipper -> Unit
                        else -> context?.let {
                            DialogUtil.createDialogAttachMedia(target.id, composerWidget.editor, it)
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
                        if (success) currentState().feed?.let(::setReplyComposer)
                        onResult(success)
                    }
                }
            },
        )
    }

    private fun renderState(state: CommentViewModel.CommentUiState) {
        commentUiState.value = state
        if (state.feed != null && composerMode == null) setReplyComposer(state.feed)
        if (state.isDeleted) {
            runCatching { findNavController().navigateUp() }.getOrElse { activity?.finish() }
            return
        }
        when {
            state.errorMessage != null && state.feed == null -> showError(state.errorMessage)
            state.isLoading && state.feed == null -> showLoading()
            state.feed != null -> updateUI()
            !state.isLoading -> showEmpty(getString(R.string.layout_empty_response))
        }
    }

    private fun showLoading() {
        _binding?.stateLayout?.showLoading()
    }

    private fun setReplyComposer(feed: FeedRecord) {
        composerMode = ComposerMode.Reply(feedId = feed.id)
        _binding?.composerWidget?.setModel(feed, KeyUtil.MUT_SAVE_FEED_REPLY)
    }

    private fun startFeedEdit(feedId: Long) {
        val feedItem = resolveCurrentFeedItem(feedId)?.takeIf { it.canEdit } ?: return
        val feed = currentState().feed?.takeIf { it.id == feedItem.id } ?: return
        composerMode = ComposerMode.Feed(feedId)
        _binding?.composerWidget?.apply {
            setModel(feed, KeyUtil.MUT_SAVE_TEXT_FEED)
            editor.text?.clear()
            setText(feed.text)
        }
    }

    private fun startReplyEdit(replyId: Long) {
        val state = currentState()
        val reply = state.replies.firstOrNull { it.id == replyId } ?: return
        if (reply.userId == null || reply.userId != state.currentUserId) return
        composerMode = ComposerMode.Reply(feedId = userActivityId, replyId = replyId)
        _binding?.composerWidget?.apply {
            setModel(reply.toFeedReplyRecord(activityId = userActivityId), KeyUtil.MUT_SAVE_FEED_REPLY)
            editor.text?.clear()
            setText(reply.reply)
        }
    }

    private fun mentionReply(replyId: Long) {
        val reply = currentState().replies.firstOrNull { it.id == replyId } ?: return
        _binding?.composerWidget?.mentionUserFrom(reply.toFeedReplyRecord())
    }

    private fun deleteCurrentReply(replyId: Long) {
        val state = currentState()
        val reply = state.replies.firstOrNull { it.id == replyId } ?: return
        if (reply.userId == null || reply.userId != state.currentUserId || reply.isDeletePending) return
        viewLifecycleOwner.lifecycleScope.launch { commentViewModel.deleteReply(replyId) }
    }

    private fun showReplyLikes(replyId: Long) {
        val reply = currentState().replies.firstOrNull { it.id == replyId } ?: return
        val likes = reply.likes.map { it.toUserBase() }
        if (likes.isNotEmpty()) {
            mBottomSheet = BottomSheetUsers.Builder()
                .setModel(likes)
                .setOnUserClick(::navigateToProfile)
                .setTitle(R.string.title_bottom_sheet_likes)
                .build()
            showBottomSheet()
        } else {
            activity?.let { NotifyUtil.makeText(it, R.string.text_no_likes, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun showFeedLikes(feedId: Long) {
        val likes = resolveCurrentFeedItem(feedId)?.likes.orEmpty().map { it.toUserBase() }
        if (likes.isNotEmpty()) {
            mBottomSheet = BottomSheetUsers.Builder()
                .setModel(likes)
                .setOnUserClick(::navigateToProfile)
                .setTitle(R.string.title_bottom_sheet_likes)
                .build()
            showBottomSheet()
        } else {
            activity?.let { NotifyUtil.makeText(it, R.string.text_no_likes, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun openFeedMedia(
        target: View,
        feedItem: FeedItemUiModel,
    ) {
        val current = resolveCurrentFeedItem(feedItem.id) ?: return
        val mediaId = current.mediaId ?: return
        navigateToMedia(MediaScreenParam(mediaId, current.mediaType))
    }

    private fun openProfile(userId: Long) {
        navigateToProfile(UserScreenParam(userId))
    }

    private fun deleteCurrentFeed(feedId: Long) {
        val feedItem = resolveCurrentFeedItem(feedId) ?: return
        if (feedItem.canDelete && !feedItem.isDeletePending) {
            viewLifecycleOwner.lifecycleScope.launch { commentViewModel.deleteFeed(feedId) }
        }
    }

    private fun onFeedMediaLongPressed(feedId: Long): Boolean {
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
        val mediaId = resolveCurrentFeedItem(feedId)?.mediaId ?: return false
        val host = activity ?: return false
        mediaActionUtil = MediaActionUtil.Builder().setId(mediaId).build(host)
        mediaActionUtil.startSeriesAction()
        return true
    }

    private fun currentState(): CommentViewModel.CommentUiState = commentViewModel.state.value

    private fun resolveCurrentFeedItem(feedId: Long): FeedItemUiModel? = currentState().feedItem?.takeIf { it.id == feedId }

    private fun finishRefreshIndicators(current: FragmentCommentBinding) {
        if (current.refreshLayout.isRefreshing()) current.refreshLayout.setRefreshing(false)
        if (current.refreshLayout.isLoading()) current.refreshLayout.setLoading(false)
    }
}
