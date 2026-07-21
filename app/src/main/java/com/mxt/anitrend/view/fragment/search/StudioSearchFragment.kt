package com.mxt.anitrend.view.fragment.search

import android.os.Bundle
import android.view.View
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.StudioActivity

/**
 * Created by max on 2017/12/20.
 * studio search fragment
 */
class StudioSearchFragment : FragmentBaseList<StudioBase, PageContainer<StudioBase>, BasePresenter>() {
    private var searchQuery: String? = null

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StudioSearchFragment = StudioSearchFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            searchQuery = args.getString(KeyUtil.arg_search)
        }
        mColumnSize = R.integer.grid_list_x2
        isPager = true
        mAdapter = StudioAdapter(ctx)
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putString(KeyUtil.arg_search, searchQuery)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            putString(KeyUtil.arg_sort, KeyUtil.SEARCH_MATCH)
        }
        viewModel?.requestData(KeyUtil.STUDIO_SEARCH_REQ, ctx)
    }

    override fun onChanged(content: PageContainer<StudioBase>?) {
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
