package com.mxt.anitrend.view.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity

/**
 * Created by max on 2017/12/20.
 */
class UserSearchFragment :
    FragmentBaseList<UserBase, PageContainer<UserBase>, BasePresenter>() {

    private var searchQuery: String? = null

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): UserSearchFragment {
            return UserSearchFragment().apply {
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
        mColumnSize = R.integer.single_list_x1
        isPager = true
        mAdapter = UserAdapter(ctx)
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
            .putVariable(KeyUtil.arg_search, searchQuery)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)
            .putVariable(KeyUtil.arg_sort, KeyUtil.SEARCH_MATCH)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.USER_SEARCH_REQ, ctx)
    }

    override fun onChanged(content: PageContainer<UserBase>?) {
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

    override fun onItemClick(target: View, data: IntPair<UserBase>) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent = Intent(host, ProfileActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(KeyUtil.arg_id, data.second.id)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<UserBase>) = Unit
}
