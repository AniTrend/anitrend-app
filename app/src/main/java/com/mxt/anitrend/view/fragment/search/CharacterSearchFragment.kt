package com.mxt.anitrend.view.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.view.activity.detail.CharacterActivity
import com.mxt.anitrend.viewmodel.CharacterSearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/20.
 */
class CharacterSearchFragment : FragmentBaseList<RecyclerItem, PageContainer<CharacterBase>, BasePresenter>() {
    private var searchQuery: String? = null

    private val characterSearchViewModel: CharacterSearchViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): CharacterSearchFragment = CharacterSearchFragment().apply {
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
        mAdapter = GroupCharacterAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterSearchViewModel.state.collect { state ->
                    when (state) {
                        is CharacterSearchViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is CharacterSearchViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is CharacterSearchViewModel.UiState.Error -> {
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
        characterSearchViewModel.load(search = query, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: PageContainer<CharacterBase>) {
        if (content.hasPageInfo()) {
            setPageInfo(content.pageInfo)
        }
        if (!content.isEmpty) {
            onPostProcessed(GroupingUtil.wrapInGroup(content.pageData))
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<CharacterBase>?) = Unit

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
