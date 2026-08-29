package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
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
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.UserListViewModel
import com.mxt.anitrend.widget.ProgressLayout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BottomSheetListUsers :
    BottomSheetBase<PageContainer<UserBase>>(),
    ItemClickListener<UserBase>,
    androidx.lifecycle.Observer<PageContainer<UserBase>?>,
    RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private var stateLayout: ProgressLayout? = null
    private var recyclerView: StatefulRecyclerView? = null

    private lateinit var mAdapter: RecyclerViewAdapter<UserBase>
    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private var mColumnSize: Int = 0
    private var isPager: Boolean = false
    private var isLimit: Boolean = false

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

    companion object {
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
        val ctx = requireContext()
        fromBundle(arguments)?.let { args ->
            userId = args.userId
            requestType = args.requestType
        }
        count = arguments?.getInt(KeyUtil.arg_model) ?: 0
        mAdapter = UserAdapter(
            context = ctx,
            currentUser = databaseHelper.currentUser,
            onToggleFollowAction = ::toggleFollow,
        )
        isPager = true
        mColumnSize = resources.getInteger(R.integer.single_list_x1)
        observeUserStore()
        observeViewModelState()
    }

    /**
     * Fire-and-forget delivery from the render-only [FollowStateWidget]. The legacy adapter
     * still passes a result callback slot, which is intentionally ignored: the committed
     * result is applied by observing [UserStore] below. Request failures are reported
     * explicitly because a failed mutation commits nothing and would otherwise stay silent
     * until the widget's bounded loading fallback expires.
     */
    private fun toggleFollow(
        userId: Long,
        @Suppress("UNUSED_PARAMETER") onResult: (Result<UserBase>) -> Unit,
    ) {
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

    /**
     * Smallest behavior-preserving bridge between the canonical [UserStore] and the legacy
     * adapter: committed follow changes for users in the list are written back onto the
     * existing item so the widget re-renders the authoritative state. A failed mutation
     * commits nothing, so the displayed state stays unchanged.
     */
    private fun observeUserStore() {
        lifecycleScope.launch {
            userStore.state.collect { state ->
                state.usersById.values.forEach(::rebindUserIfPresent)
            }
        }
    }

    private fun rebindUserIfPresent(record: UserRecord) {
        val position = mAdapter.data.indexOfFirst { it.id == record.id }
        if (position < 0) return
        val current = mAdapter.data[position]
        if (current.rebindFollowState(record)) {
            mAdapter.onItemChanged(current, position)
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
                            stateLayout?.showLoading()
                        }
                        is UserListViewModel.UiState.Success -> {
                            onChanged(state.container)
                        }
                        is UserListViewModel.UiState.Error -> {
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
        recyclerView = binding.recyclerView
        searchView = binding.customSheetToolbar.searchView
        createBottomSheetBehavior(binding.root)
        mLayoutManager = StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL)
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
        if (mAdapter.itemCount < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    private fun addScrollLoadTrigger() {
        if (isPager) {
            val recycler = recyclerView ?: return
            if (!recycler.hasOnScrollListener()) {
                presenter.initListener(mLayoutManager, this)
                recycler.addOnScrollListener(presenter)
            }
        }
    }

    private fun removeScrollLoadTrigger() {
        if (isPager) {
            recyclerView?.clearOnScrollListeners()
        }
    }

    override fun onPause() {
        removeScrollLoadTrigger()
        super.onPause()
    }

    override fun onResume() {
        addScrollLoadTrigger()
        super.onResume()
    }

    private fun injectAdapter() {
        mAdapter.setClickListener(this)
        val recycler = recyclerView ?: return
        if (recycler.adapter == null) {
            recycler.setHasFixedSize(true)
            recycler.isNestedScrollingEnabled = true
            recycler.layoutManager = mLayoutManager
            recycler.adapter = mAdapter
        }
        if (mAdapter.itemCount < 1) {
            val drawable =
                context?.getCompatDrawable(
                    R.drawable.ic_new_releases_white_24dp,
                    R.color.colorStateBlue,
                ) ?: return
            stateLayout?.showEmpty(drawable, getString(R.string.layout_empty_response))
        } else {
            stateLayout?.showContent()
        }
    }

    private fun updateUI() {
        injectAdapter()
    }

    private fun setLimitReached() {
        if (presenter.currentPage != 0) {
            isLimit = true
        }
    }

    override fun onRefresh() {
        if (isPager) {
            presenter.onRefreshPage()
        }
        makeRequest()
    }

    override fun onLoad() = Unit

    override fun onLoadMore() {
        makeRequest()
    }

    fun makeRequest() {
        val page = presenter.currentPage
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

    private fun onPostProcessed(content: List<UserBase>?) {
        val items = content ?: emptyList()
        if (!CompatUtil.isEmpty(items)) {
            if (isPager) {
                if (mAdapter.itemCount < 1) {
                    mAdapter.onItemsInserted(items)
                } else {
                    mAdapter.onItemRangeInserted(items)
                }
            } else {
                mAdapter.onItemsInserted(items)
            }
            updateUI()
        } else {
            if (isPager) {
                setLimitReached()
            }
            if (mAdapter.itemCount < 1) {
                showEmpty(getString(R.string.layout_empty_response))
            }
        }
    }

    override fun onChanged(content: PageContainer<UserBase>?) {
        if (content != null) {
            if (content.hasPageInfo()) {
                presenter.setPageInfo(content.pageInfo)
            }
            if (!content.isEmpty) {
                onPostProcessed(content.pageData)
            } else {
                onPostProcessed(emptyList())
            }
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
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

    override fun onItemClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                onUserClick?.invoke(UserScreenParam(data.value.id))
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) = Unit

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

        fun setOnUserClick(listener: (UserScreenParam) -> Unit): Builder {
            onUserClick = listener
            return this
        }

        fun setUserId(userId: Long): Builder {
            bundle.putLong(KeyUtil.arg_userId, userId)
            return this
        }

        fun setModelCount(count: Int): Builder {
            bundle.putInt(KeyUtil.arg_model, count)
            return this
        }

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
