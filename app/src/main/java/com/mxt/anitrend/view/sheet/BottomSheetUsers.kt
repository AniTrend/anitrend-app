package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
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
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.parcelableArrayList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.user.UserList
import com.mxt.anitrend.ui.user.UserRowUiState
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.viewmodel.BottomSheetUsersViewModel
import com.mxt.anitrend.widget.ProgressLayout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Bottom sheet displaying a supplied list of users. */
class BottomSheetUsers : BottomSheetBase<List<UserBase>>() {

    private var onUserClick: ((UserScreenParam) -> Unit)? = null
    private var userRows by mutableStateOf<List<UserRowUiState>>(emptyList(), referentialEqualityPolicy())

    private var stateLayout: ProgressLayout? = null
    private var userCount: Int = 0

    private val databaseHelper: DatabaseHelper by inject()
    private val userStore: UserStore by inject()
    private val toggleUserFollowInteractor: ToggleUserFollowInteractor by inject()
    private val usersViewModel: BottomSheetUsersViewModel by viewModel()

    /** Factory and argument helpers for the users sheet. */
    companion object {
        /** Creates a users sheet with the supplied arguments. */
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetUsers = BottomSheetUsers().apply {
            arguments = bundle
        }

        /**
         * Identity-preserving read of the users sheet payload.
         *
         * The bundle carries [UserSheetModel] entries (parcel-safe by construction,
         * see [UserBase.toUserSheetModel]) and is converted back to in-memory
         * [UserBase] items with their real ids, avatars, and initial follow state
         * restored. Reading [UserBase] directly from the bundle would zero every
         * `@IgnoredOnParcel` id and break follow dispatch and store rebinding.
         */
        @VisibleForTesting
        internal fun resolveUsers(bundle: Bundle?): List<UserBase>? = bundle?.parcelableArrayList<UserSheetModel>(KeyUtil.arg_list_model)
            ?.map(UserSheetModel::toUserBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val users = resolveUsers(arguments).orEmpty()
        userCount = users.size
        usersViewModel.setSourceUsers(users)
        userRows = usersViewModel.filteredRows.value
        observeUserStore()
        observeUserRows()
    }

    /** Launches a follow mutation; the committed [UserStore] record updates row state. */
    private fun toggleFollow(userId: Long) {
        lifecycleScope.launch {
            val result = toggleUserFollowInteractor(ToggleUserFollowCommand(userId = userId))
            if (result is MutationResult.Failure) {
                context?.let {
                    NotifyUtil
                        .makeText(
                            it,
                            result.message,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    private fun observeUserStore() {
        lifecycleScope.launch {
            userStore.state.collect { state ->
                state.usersById.values.forEach(usersViewModel::rebindUserIfPresent)
            }
        }
    }

    private fun observeUserRows() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.filteredRows.collect { rows ->
                    userRows = rows
                    updateEmptyState(rows)
                }
            }
        }
    }

    private fun updateEmptyState(rows: List<UserRowUiState>) {
        if (rows.isEmpty()) {
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val sheetBinding = BottomSheetListBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        bindToolbarViews(sheetBinding.root)
        searchView = sheetBinding.customSheetToolbar.searchView
        stateLayout = sheetBinding.stateLayout
        sheetBinding.userListComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                val nestedScrollInterop = rememberNestedScrollInteropConnection()
                AniTrendTheme {
                    UserList(
                        modifier = Modifier.nestedScroll(nestedScrollInterop),
                        items = userRows,
                        listState = rememberLazyGridState(),
                        currentUser = databaseHelper.currentUser,
                        onUserClick = { userId ->
                            onUserClick?.invoke(UserScreenParam(userId))
                        },
                        onToggleFollow = ::toggleFollow,
                    )
                }
            }
        }
        createBottomSheetBehavior(sheetBinding.root)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        toolbarTitle?.text = getString(mTitle, userCount)
        toolbarSearch?.visibility = View.VISIBLE
        mSearchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                usersViewModel.setQuery(query)
            }

            override fun onSearchSubmitted(query: String?) = Unit

            override fun onSearchShown() {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }

            override fun onSearchClosed() {
                usersViewModel.setQuery("")
            }
        }
        updateEmptyState(userRows)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onUserClick = null
    }

    /** Builds a users sheet and configures its navigation callback. */
    class Builder : BottomSheetBuilder() {
        private var onUserClick: ((UserScreenParam) -> Unit)? = null

        override fun build(): BottomSheetBase<*> = newInstance(bundle).also { instance ->
            instance.onUserClick = onUserClick
        }

        /** Sets the callback invoked when a user is selected. */
        fun setOnUserClick(listener: (UserScreenParam) -> Unit): Builder {
            onUserClick = listener
            return this
        }

        /**
         * Writes the user list through the parcel-safe [UserSheetModel] boundary.
         * [UserBase] itself cannot be parceled without losing its `@IgnoredOnParcel`
         * id (and avatar), so the identity is captured here while the entities are
         * still alive in memory and restored on read by [resolveUsers].
         */
        fun setModel(model: List<UserBase>): Builder {
            bundle.putParcelableArrayList(
                KeyUtil.arg_list_model,
                ArrayList(model.map(UserBase::toUserSheetModel)),
            )
            return this
        }
    }
}
