package com.mxt.anitrend.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.StaffActivity
import com.mxt.anitrend.viewmodel.StaffFavouritesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/25.
 * StaffFavouriteFragment
 */
class StaffFavouriteFragment : FragmentBaseList<StaffBase, ConnectionContainer<Favourite>>() {
    private var userId: Long = 0

    private val staffFavouritesViewModel: StaffFavouritesViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): StaffFavouriteFragment {
            val args = Bundle(params)
            return StaffFavouriteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            userId = args.getLong(KeyUtil.arg_id)
        }
        mAdapter = StaffAdapter(ctx)
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffFavouritesViewModel.state.collect { state ->
                    when (state) {
                        is StaffFavouritesViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is StaffFavouritesViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is StaffFavouritesViewModel.UiState.Error -> {
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
        staffFavouritesViewModel.load(userId = userId, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: ConnectionContainer<Favourite>) {
        if (!content.isEmpty) {
            val pageContainer = content.connection.staff
            if (pageContainer != null) {
                if (pageContainer.hasPageInfo()) {
                    setPageInfo(pageContainer.pageInfo)
                }
                onPostProcessed(pageContainer.pageData)
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

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: ConnectionContainer<Favourite>?) = Unit

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
