package com.mxt.anitrend.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.view.activity.detail.CharacterActivity
import com.mxt.anitrend.viewmodel.CharacterFavouritesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/25.
 * CharacterFavouriteFragment
 */
class CharacterFavouriteFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<Favourite>>() {
    private var userId: Long = 0

    private val characterFavouritesViewModel: CharacterFavouritesViewModel by viewModel()

    companion object {
        // Documented legacy channel: the favourites host activity writes only the
        // legacy wire extras (arg_id), so the read stays on the transitional channel
        // until the host navigates with typed parameters.

        @JvmStatic
        fun newInstance(params: Bundle): CharacterFavouriteFragment {
            val args = Bundle(params)
            return CharacterFavouriteFragment().apply {
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
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = GroupCharacterAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterFavouritesViewModel.state.collect { state ->
                    when (state) {
                        is CharacterFavouritesViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is CharacterFavouritesViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is CharacterFavouritesViewModel.UiState.Error -> {
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
        characterFavouritesViewModel.load(userId = userId, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: ConnectionContainer<Favourite>) {
        if (!content.isEmpty) {
            val pageContainer = content.connection.characters
            if (pageContainer != null) {
                if (pageContainer.hasPageInfo()) {
                    setPageInfo(pageContainer.pageInfo)
                }
                onPostProcessed(GroupingUtil.wrapInGroup(pageContainer.pageData))
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
        data: IndexedValue<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                val character = data.value as? CharacterBase ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, CharacterActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, character.id)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<RecyclerItem>,
    ) = Unit
}
