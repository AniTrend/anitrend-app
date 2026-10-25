package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.user.UserStore
import com.mxt.anitrend.databinding.BottomSheetListBinding
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.UserListScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.user.UserList
import com.mxt.anitrend.ui.user.UserRowUiState
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.UserListViewModel
import com.mxt.anitrend.widget.ProgressLayout
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private const val USER_LIST_VISIBLE_THRESHOLD = 9

/** Bottom sheet displaying a paged list of users. */
class BottomSheetListUsers : BottomSheetBase<PageContainer<UserBase>>() {
    private var stateLayout: ProgressLayout? = null

    @VisibleForTesting
    internal val userListState = LazyGridState()

    @VisibleForTesting
    internal val userItems = mutableStateOf(emptyList<UserRowUiState>())
    private var isLoading: Boolean = false
    private var hasMore: Boolean = true
    private var lastAppliedPageState: UserListViewModel.UiState.Success? = null
    private var requestedPage: Int = 1

    private var count: Int = 0
    private var userId: Long = 0
    private var onUserClick: ((UserScreenParam) -> Unit)? = null

    @KeyUtil.RequestType
    private var requestType: Int = 0

    private val databaseHelper: DatabaseHelper by inject()
    private val userStore: UserStore by inject()
    private val toggleUserFollowInteractor: ToggleUserFollowInteractor by inject()

    private val userListViewModel: UserListViewModel by viewModel()

    private val stateLayoutOnClick =
        View.OnClickListener {
            stateLayout?.showLoading()
            onRefresh()
        }

    /** Factory and argument helpers for the user list sheet. */
    companion object {
        /** Creates a user list sheet with the supplied arguments. */
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetListUsers = BottomSheetListUsers().apply {
            arguments = bundle
        }

        /**
         * Resolves the user-list identity from the sheet arguments.
         *
         * The typed [UserListScreenParam] takes precedence only when valid (positive
         * user id); otherwise the legacy [KeyUtil.arg_userId] / [KeyUtil.arg_request_type]
         * extras are bridged with their exact raw values (absent resolves to 0,
         * explicit zero or negative ids pass through, mirroring the pre-refactor
         * getter). The model count stays on the legacy channel (toolbar presentation).
         */
        fun fromBundle(bundle: Bundle?): UserListScreenParam? = resolve(
            typed = bundle?.screenParam<UserListScreenParam>(),
            legacyUserId = bundle?.getLong(KeyUtil.arg_userId) ?: 0L,
            legacyRequestType = bundle?.getInt(KeyUtil.arg_request_type) ?: 0,
        )

        @VisibleForTesting
        internal fun resolve(typed: UserListScreenParam?, legacyUserId: Long, legacyRequestType: Int): UserListScreenParam? {
            typed?.let { param ->
                if (param.userId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy values.
            }
            return UserListScreenParam(userId = legacyUserId, requestType = legacyRequestType)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromBundle(arguments)?.let { args ->
            userId = args.userId
            requestType = args.requestType
        }
        count = arguments?.getInt(KeyUtil.arg_model) ?: 0
        observeUserStore()
        observeViewModelState()
    }

    /** Dispatches the follow mutation; UserStore applies committed changes to row projections. */
    private fun toggleFollow(userId: Long) {
        lifecycleScope.launch {
            val result = toggleUserFollowInteractor(ToggleUserFollowCommand(userId = userId))
            reportFollowFailure(result) { message ->
                context?.let {
                    NotifyUtil
                        .makeText(
                            it,
                            message,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    /** Tracks committed follow changes for users currently represented by the list. */
    private fun observeUserStore() {
        lifecycleScope.launch {
            userStore.state.collect { state ->
                state.usersById.values.forEach(::rebindUserIfPresent)
            }
        }
    }

    private fun rebindUserIfPresent(record: UserRecord) {
        val rows = userItems.value
        val index = rows.indexOfFirst { it.model.id == record.id }
        if (index < 0) return
        val current = rows[index]
        if (!current.model.rebindFollowState(record)) return
        userItems.value =
            rows.mapIndexed { position, item ->
                if (position == index) item.copy(isFollowing = record.isFollowing) else item
            }
    }

    /**
     * Renders the ViewModel's terminal states into the dialog's content.
     *
     * This dialog attaches its content in [onCreateDialog] via `dialog.setContentView`
     * and never creates a fragment view, so [onViewCreated] (and with it
     * `viewLifecycleOwner`) never runs. Collection therefore starts on the fragment
     * lifecycle from [onCreate]: [repeatOnLifecycle] begins collecting exactly when the
     * dialog is started and cancels when it is dismissed, so a terminal state is
     * rendered while the dialog is visible, a dismissed dialog never renders a late
     * response, and no collector outlives the fragment.
     */
    private fun observeViewModelState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userListViewModel.state.collect { state ->
                    when (state) {
                        is UserListViewModel.UiState.Loading -> {
                            isLoading = true
                            stateLayout?.showLoading()
                        }
                        is UserListViewModel.UiState.Success -> {
                            // Re-entering STARTED re-delivers the latest Success;
                            // applying it again would duplicate the accumulated
                            // page and break the keyed lazy grid.
                            if (lastAppliedPageState === state) return@collect
                            lastAppliedPageState = state
                            onChanged(state.container)
                        }
                        is UserListViewModel.UiState.Error -> {
                            isLoading = false
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val binding = BottomSheetListBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        bindToolbarViews(binding.root)
        stateLayout = binding.stateLayout
        binding.userListComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val nestedScrollInterop = rememberNestedScrollInteropConnection()
                LaunchedEffect(userListState) {
                    snapshotFlow {
                        val layoutInfo = userListState.layoutInfo
                        layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { lastVisibleIndex ->
                            layoutInfo.totalItemsCount to lastVisibleIndex
                        }
                    }.distinctUntilChanged().collect { visibleRange ->
                        if (
                            visibleRange != null &&
                            visibleRange.first - visibleRange.second - 1 <= USER_LIST_VISIBLE_THRESHOLD
                        ) {
                            onLoadMore()
                        }
                    }
                }
                AniTrendTheme {
                    UserList(
                        modifier = Modifier.nestedScroll(nestedScrollInterop),
                        items = userItems.value,
                        listState = userListState,
                        currentUser = databaseHelper.currentUser,
                        onUserClick = { userId -> onUserClick?.invoke(UserScreenParam(userId)) },
                        onToggleFollow = ::toggleFollow,
                    )
                }
            }
        }
        searchView = binding.customSheetToolbar.searchView
        createBottomSheetBehavior(binding.root)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        toolbarTitle?.text = getString(mTitle, count)
        mSearchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                searchQuery = query
            }

            override fun onSearchSubmitted(query: String?) {
                searchQuery = query
                stateLayout?.showLoading()
                onRefresh()
            }

            override fun onSearchShown() {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                if (!TextUtils.isEmpty(searchQuery)) {
                    searchView?.setQuery(searchQuery, false)
                }
            }
        }
        searchView?.visibility = View.GONE
        stateLayout?.showLoading()
        if (userItems.value.isEmpty()) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        if (userItems.value.isEmpty()) {
            showEmpty(getString(R.string.layout_empty_response))
        } else {
            stateLayout?.showContent()
        }
    }

    private fun onRefresh() {
        presenter.onRefreshPage()
        hasMore = true
        isLoading = true
        makeRequest()
    }

    private fun onLoadMore() {
        if (isLoading || !hasMore) return
        presenter.currentPage++
        isLoading = true
        makeRequest()
    }

    /** Loads the current user list page according to the configured request type. */
    fun makeRequest() {
        isLoading = true
        val page = presenter.currentPage
        requestedPage = page
        val perPage = KeyUtil.PAGING_LIMIT
        when (requestType) {
            KeyUtil.USER_FOLLOWERS_REQ -> userListViewModel.loadFollowers(userId, page, perPage)
            KeyUtil.USER_FOLLOWING_REQ -> userListViewModel.loadFollowing(userId, page, perPage)
            else -> {
                Timber.w("Unknown requestType: %s in BottomSheetListUsers", requestType)
                showError(getString(R.string.text_unknown_error))
            }
        }
    }

    private fun UserBase.toUserRowUiState(): UserRowUiState = UserRowUiState(
        model = this,
        name = name,
        avatar = avatar,
        isFollowing = isFollowing,
    )

    private fun onPostProcessed(content: List<UserBase>?) {
        isLoading = false
        val mapped = content.orEmpty().map { it.toUserRowUiState() }
        if (requestedPage <= 1) {
            // Refresh/retry re-requests page 1; rows must be replaced even by an
            // empty result, or stale rows (and later duplicate keys) survive the
            // refresh and the empty surface never shows.
            userItems.value = mapped
        } else if (mapped.isNotEmpty()) {
            val existing = userItems.value.mapTo(mutableSetOf()) { it.model.id }
            userItems.value = userItems.value + mapped.filter { it.model.id !in existing }
        }
        if (mapped.isNotEmpty()) {
            updateUI()
        } else {
            hasMore = false
            if (userItems.value.isEmpty()) {
                showEmpty(getString(R.string.layout_empty_response))
            }
        }
    }

    private fun onChanged(content: PageContainer<UserBase>?) {
        if (content?.hasPageInfo() == true) {
            presenter.setPageInfo(content.pageInfo)
            hasMore = content.pageInfo.hasNextPage()
        } else {
            hasMore = content != null
        }
        onPostProcessed(content?.pageData)
    }

    override fun showError(error: String) {
        super.showError(error)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_cry)
        stateLayout?.showError(drawable, error, getString(R.string.button_try_again), stateLayoutOnClick)
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        val drawable = context?.getCompatDrawable(R.drawable.ic_emoji_sweat)
        stateLayout?.showError(drawable, message, getString(R.string.button_try_again), stateLayoutOnClick)
    }

    /** Builds a user list sheet and configures its navigation callback. */
    class Builder : BottomSheetBuilder() {
        private var onUserClick: ((UserScreenParam) -> Unit)? = null

        override fun build(): BottomSheetBase<*> {
            // Typed identity write at the production entry point, derived from the
            // legacy setters; the legacy keys are retained for pre-migration readers.
            bundle.putParcelable(
                screenParamKey<UserListScreenParam>(),
                UserListScreenParam(
                    userId = bundle.getLong(KeyUtil.arg_userId, 0L),
                    requestType = bundle.getInt(KeyUtil.arg_request_type, 0),
                ),
            )
            return newInstance(bundle).also { instance ->
                instance.onUserClick = onUserClick
            }
        }

        /** Sets the callback invoked when a user is selected. */
        fun setOnUserClick(listener: (UserScreenParam) -> Unit): Builder {
            onUserClick = listener
            return this
        }

        /** Sets the user id whose followers or following are loaded. */
        fun setUserId(userId: Long): Builder {
            bundle.putLong(KeyUtil.arg_userId, userId)
            return this
        }

        /** Sets the count displayed in the sheet title. */
        fun setModelCount(count: Int): Builder {
            bundle.putInt(KeyUtil.arg_model, count)
            return this
        }

        /** Sets whether followers or following should be requested. */
        fun setRequestType(
            @KeyUtil.RequestType requestType: Int,
        ): Builder {
            bundle.putInt(KeyUtil.arg_request_type, requestType)
            return this
        }
    }
}

/**
 * Converges a legacy list item's follow state to the authoritative committed [UserRecord].
 * Returns true when the item's follow state actually changed (successful mutation state
 * convergence), false when the record is absent (a failed mutation commits nothing, so the
 * displayed state must stay unchanged), belongs to another user, or already matches (no
 * spurious re-render).
 */
internal fun UserBase.rebindFollowState(record: UserRecord?): Boolean {
    if (record == null || record.id != id || isFollowing == record.isFollowing) return false
    isFollowing = record.isFollowing
    return true
}

/**
 * Surfaces a failed follow mutation through the sheet's notification convention.
 *
 * A successful mutation commits to [com.mxt.anitrend.data.store.user.UserStore] and
 * converges the displayed rows via [BottomSheetListUsers.observeUserStore], so only
 * failures need explicit reporting here: a failed mutation commits nothing and would
 * otherwise stay silent until the widget's bounded loading fallback expires. Success
 * results are intentionally not surfaced.
 */
@VisibleForTesting
internal fun reportFollowFailure(
    result: MutationResult,
    notify: (String) -> Unit,
) {
    if (result is MutationResult.Failure) {
        notify(result.message)
    }
}
