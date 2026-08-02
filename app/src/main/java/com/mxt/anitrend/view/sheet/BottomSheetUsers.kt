package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.store.user.UserStore
import com.mxt.anitrend.databinding.BottomSheetListBinding
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.parcelableArrayList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.widget.ProgressLayout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BottomSheetUsers :
    BottomSheetBase<List<UserBase>>(),
    ItemClickListener<UserBase>,
    Observer<List<UserBase>?> {

    private var binding: BottomSheetListBinding? = null

    private lateinit var mAdapter: RecyclerViewAdapter<UserBase>
    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private var stateLayout: ProgressLayout? = null
    private var recyclerView: StatefulRecyclerView? = null
    private var mColumnSize: Int = 0

    private val databaseHelper: DatabaseHelper by inject()
    private val userStore: UserStore by inject()
    private val toggleUserFollowInteractor: ToggleUserFollowInteractor by inject()

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetUsers = BottomSheetUsers().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        mColumnSize = resources.getInteger(R.integer.single_list_x1)
        mAdapter = UserAdapter(
            context = ctx,
            currentUser = databaseHelper.currentUser,
            onToggleFollowAction = ::toggleFollow,
        )
        val baseList = arguments?.parcelableArrayList<UserBase>(KeyUtil.arg_list_model)
        if (!baseList.isNullOrEmpty()) {
            mAdapter.onItemsInserted(baseList)
        }
        observeUserStore()
    }

    /**
     * Fire-and-forget delivery from the render-only [FollowStateWidget]. The legacy adapter
     * still passes a result callback slot, which is intentionally ignored: the mutation result
     * is applied by observing [UserStore] below.
     */
    private fun toggleFollow(
        userId: Long,
        @Suppress("UNUSED_PARAMETER") onResult: (Result<UserBase>) -> Unit,
    ) {
        lifecycleScope.launch {
            toggleUserFollowInteractor(ToggleUserFollowCommand(userId = userId))
        }
    }

    /**
     * Smallest behavior-preserving bridge between the canonical [UserStore] and the legacy
     * adapter: committed follow changes for users in the list are written back onto the
     * existing item so the widget re-renders the authoritative state.
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
        if (current.isFollowing == record.isFollowing) return
        current.isFollowing = record.isFollowing
        mAdapter.onItemChanged(current, position)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetListBinding.inflate(layoutInflater)
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        searchView = binding?.customSheetToolbar?.searchView
        stateLayout = binding?.stateLayout
        recyclerView = binding?.recyclerView
        createBottomSheetBehavior(requireNotNull(binding).root)
        mLayoutManager = StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        toolbarTitle?.text = getString(mTitle, mAdapter.itemCount)
        toolbarSearch?.visibility = View.VISIBLE
        mSearchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                if (!TextUtils.isEmpty(query) && mAdapter.filter != null) {
                    mAdapter.filter.filter(query)
                }
            }

            override fun onSearchSubmitted(query: String?) = Unit

            override fun onSearchShown() {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }

            override fun onSearchClosed() {
                mAdapter.filter?.filter("")
            }
        }
        injectAdapter()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onChanged(value: List<UserBase>?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent =
                    Intent(host, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_id, data.value.id)
                    }
                host.startActivity(intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) = Unit

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)

        fun setModel(model: List<UserBase>): Builder {
            bundle.putParcelableArrayList(KeyUtil.arg_list_model, ArrayList(model))
            return this
        }
    }
}
