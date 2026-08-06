package com.mxt.anitrend.view.fragment.search

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.StudioActivity
import com.mxt.anitrend.viewmodel.StudioSearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/20.
 * studio search fragment
 */
class StudioSearchFragment : FragmentBaseList<StudioBase, PageContainer<StudioBase>>() {
    private var searchQuery: String? = null

    private val studioSearchViewModel: StudioSearchViewModel by viewModel()

    companion object {
        /**
         * Documented legacy channel: the search query is caller state, not identity.
         * It stays on arg_search until a search-state model is designed. Reads mirror
         * the pre-refactor getter exactly (absent resolves to null).
         */
        fun fromBundle(bundle: Bundle?): String? = resolveLegacyQuery(bundle?.getString(KeyUtil.arg_search))

        @VisibleForTesting
        internal fun resolveLegacyQuery(raw: String?): String? = raw

        @JvmStatic
        fun newInstance(args: Bundle): StudioSearchFragment = StudioSearchFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        searchQuery = fromBundle(arguments)
        mColumnSize = R.integer.grid_list_x2
        isPager = true
        mAdapter = StudioAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                studioSearchViewModel.state.collect { state ->
                    when (state) {
                        is StudioSearchViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is StudioSearchViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is StudioSearchViewModel.UiState.Error -> {
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
        studioSearchViewModel.load(search = query, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: PageContainer<StudioBase>) {
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
    override fun onChanged(value: PageContainer<StudioBase>?) = Unit

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
