package com.mxt.anitrend.view.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.StaffActivity
import com.mxt.anitrend.viewmodel.StaffSearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/20.
 */
class StaffSearchFragment : FragmentBaseList<StaffBase, PageContainer<StaffBase>, BasePresenter>() {
    private var searchQuery: String? = null

    private val staffSearchViewModel: StaffSearchViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StaffSearchFragment = StaffSearchFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            searchQuery = args.getString(KeyUtil.arg_search)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = StaffAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffSearchViewModel.state.collect { state ->
                    when (state) {
                        is StaffSearchViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is StaffSearchViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is StaffSearchViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val query = searchQuery ?: return
        staffSearchViewModel.load(search = query, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: PageContainer<StaffBase>) {
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
    override fun onChanged(value: PageContainer<StaffBase>?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<StaffBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent =
                    Intent(host, StaffActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, data.value.id)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<StaffBase>,
    ) = Unit
}
