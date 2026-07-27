package com.mxt.anitrend.view.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.viewmodel.UserSearchViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/20.
 */
class UserSearchFragment : FragmentBaseList<UserBase, PageContainer<UserBase>>() {
    private var searchQuery: String? = null

    private val mutationCoordinator by inject<WidgetMutationCoordinator>()

    private val userSearchViewModel: UserSearchViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): UserSearchFragment = UserSearchFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            searchQuery = args.getString(KeyUtil.arg_search)
        }
        mColumnSize = R.integer.single_list_x1
        isPager = true
        mAdapter = UserAdapter(ctx, mutationCoordinator)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userSearchViewModel.state.collect { state ->
                    when (state) {
                        is UserSearchViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is UserSearchViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is UserSearchViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val query = searchQuery ?: return
        userSearchViewModel.load(search = query, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: PageContainer<UserBase>) {
        if (content.hasPageInfo()) {
            setPageInfo(content.pageInfo)
        }
        if (!content.isEmpty) {
            onPostProcessed(content.pageData)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<UserBase>?) = Unit

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
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) = Unit
}
