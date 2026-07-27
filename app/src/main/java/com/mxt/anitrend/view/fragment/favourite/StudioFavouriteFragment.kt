package com.mxt.anitrend.view.fragment.favourite

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.StudioActivity
import com.mxt.anitrend.viewmodel.StudioFavouritesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/25.
 * StudioFavouriteFragment
 */
class StudioFavouriteFragment : FragmentBaseList<StudioBase, ConnectionContainer<Favourite>>() {
    private var userId: Long = 0

    private val studioFavouritesViewModel: StudioFavouritesViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): StudioFavouriteFragment {
            val args = Bundle(params)
            return StudioFavouriteFragment().apply {
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
        mColumnSize = R.integer.grid_list_x2
        isPager = true
        mAdapter = StudioAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                studioFavouritesViewModel.state.collect { state ->
                    when (state) {
                        is StudioFavouritesViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is StudioFavouritesViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is StudioFavouritesViewModel.UiState.Error -> {
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
        studioFavouritesViewModel.load(userId = userId, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: ConnectionContainer<Favourite>) {
        if (!content.isEmpty) {
            val pageContainer = content.connection.studios
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
        data: IndexedValue<StudioBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent = StudioActivity.newIntent(host, data.value.id)
                startActivity(intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<StudioBase>,
    ) = Unit
}
