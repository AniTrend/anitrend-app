package com.mxt.anitrend.view.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.activity.detail.StaffActivity

/**
 * Created by max on 2017/12/20.
 */
class StaffSearchFragment :
    FragmentBaseList<StaffBase, PageContainer<StaffBase>, BasePresenter>() {

    private var searchQuery: String? = null

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StaffSearchFragment {
            return StaffSearchFragment().apply {
                arguments = args
            }
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
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
            .putVariable(KeyUtil.arg_search, searchQuery)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)
            .putVariable(KeyUtil.arg_sort, KeyUtil.SEARCH_MATCH)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.STAFF_SEARCH_REQ, ctx)
    }

    override fun onChanged(content: PageContainer<StaffBase>?) {
        if (content != null) {
            if (content.hasPageInfo())
                presenter.setPageInfo(content.pageInfo)
            if (!content.isEmpty)
                onPostProcessed(content.pageData)
            else
                onPostProcessed(emptyList())
        } else
            onPostProcessed(emptyList())
        if (mAdapter.itemCount < 1)
            onPostProcessed(null)
    }

    override fun onItemClick(target: View, data: IntPair<StaffBase>) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent = Intent(host, StaffActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, data.second.id)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<StaffBase>) = Unit
}
