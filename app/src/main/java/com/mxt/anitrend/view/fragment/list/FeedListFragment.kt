package com.mxt.anitrend.view.fragment.list

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.TapTargetUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.CommentActivity
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.viewmodel.FeedListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/07.
 * Home page feed base
 */
open class FeedListFragment : FragmentBaseList<FeedList, PageContainer<FeedList>, BasePresenter>() {

    private val settings: Settings by inject()

    private val mutationCoordinator by inject<WidgetMutationCoordinator>()

    private val feedListViewModel: FeedListViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): FeedListFragment {
            val args = Bundle(params)
            return FeedListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        isPager = true
        isFeed = true
        mColumnSize = R.integer.single_list_x1
        mAdapter = FeedAdapter(ctx, mutationCoordinator)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feedListViewModel.state.collect { state ->
                    when (state) {
                        is FeedListViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is FeedListViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is FeedListViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_post -> {
                mBottomSheet =
                    BottomSheetComposer
                        .Builder()
                        .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.menu_title_new_activity_post)
                        .build()
                showBottomSheet()
                return true
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        injectAdapter()
        if (!TapTargetUtil.isActive(KeyUtil.KEY_POST_TYPE_TIP) && isFeed) {
            if (settings.shouldShowTipFor(KeyUtil.KEY_POST_TYPE_TIP)) {
                val host = activity ?: return
                TapTargetUtil
                    .buildDefault(host, R.string.tip_status_post_title, R.string.tip_status_post_text, R.id.action_post)
                    .setPromptStateChangeListener { _, state ->
                        if (state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED ||
                            state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                        ) {
                            settings.disableTipFor(KeyUtil.KEY_POST_TYPE_TIP)
                        }
                        if (state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED) {
                            TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, true)
                        }
                    }.show()
                TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, false)
            }
        }
    }

    override fun makeRequest() {
        val args = arguments ?: return
        feedListViewModel.load(
            page = mScrollListener.currentPage,
            pageLimit = args.getInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT),
            isFollowing = if (args.containsKey(KeyUtil.arg_isFollowing)) args.getBoolean(KeyUtil.arg_isFollowing) else null,
            type = args.getString(KeyUtil.arg_type)?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() },
            isMixed = if (args.containsKey(KeyUtil.arg_isMixed)) args.getBoolean(KeyUtil.arg_isMixed) else null,
        )
    }

    protected fun Bundle.applyBaseFeedRequestArguments(source: Bundle?) {
        putInt(KeyUtil.arg_page_limit, source?.getInt(KeyUtil.arg_page_limit) ?: KeyUtil.PAGING_LIMIT)

        if (source?.containsKey(KeyUtil.arg_type) == true) {
            putString(KeyUtil.arg_type, source.getString(KeyUtil.arg_type))
        }
        if (source?.containsKey(KeyUtil.arg_isFollowing) == true) {
            putBoolean(KeyUtil.arg_isFollowing, source.getBoolean(KeyUtil.arg_isFollowing))
        }
        if (source?.containsKey(KeyUtil.arg_isMixed) == true) {
            putBoolean(KeyUtil.arg_isMixed, source.getBoolean(KeyUtil.arg_isMixed))
        }
        if (source?.containsKey(KeyUtil.arg_asHtml) == true) {
            putBoolean(KeyUtil.arg_asHtml, source.getBoolean(KeyUtil.arg_asHtml))
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<FeedList>?) = Unit

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

    override fun onItemClick(
        target: View,
        data: IndexedValue<FeedList>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                val series = data.value.media ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, series.id)
                        putExtra(KeyUtil.arg_mediaType, series.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.widget_comment -> {
                val host = activity ?: return
                val intent =
                    Intent(host, CommentActivity::class.java).apply {
                        putExtra(KeyUtil.arg_model, data.value)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.widget_edit -> {
                mBottomSheet =
                    BottomSheetComposer
                        .Builder()
                        .setUserActivity(data.value)
                        .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.edit_status_title)
                        .build()
                showBottomSheet()
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
                val user = data.value.user
                if (user != null) {
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
        data: IndexedValue<FeedList>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                if (settings.isAuthenticated) {
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

    override fun onPrepareActionMode(
        mode: ActionMode,
        menu: Menu,
    ): Boolean {
        menu.findItem(R.id.action_bookmark).isVisible = true
        return true
    }

    override fun onActionItemClicked(
        mode: ActionMode,
        item: MenuItem,
    ): Boolean {
        val selected = actionMode?.selectedItems.orEmpty()
        when (item.itemId) {
            R.id.action_bookmark -> Unit
            R.id.action_delete -> Unit
        }
        return true
    }
}
