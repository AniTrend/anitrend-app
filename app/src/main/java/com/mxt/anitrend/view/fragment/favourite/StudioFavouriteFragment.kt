package com.mxt.anitrend.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.activity.detail.StudioActivity

/**
 * Created by max on 2018/03/25.
 * StudioFavouriteFragment
 */
class StudioFavouriteFragment :
    FragmentBaseList<StudioBase, ConnectionContainer<Favourite>, BasePresenter>() {

    private var userId: Long = 0

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
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
            .putVariable(KeyUtil.arg_id, userId)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.USER_STUDIO_FAVOURITES_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<Favourite>?) {
        if (content != null) {
            if (!content.isEmpty) {
                val pageContainer = content.connection.studios
                if (pageContainer != null) {
                    if (pageContainer.hasPageInfo())
                        presenter.setPageInfo(pageContainer.pageInfo)
                    onPostProcessed(pageContainer.pageData)
                } else {
                    onPostProcessed(emptyList())
                }
            } else
                onPostProcessed(emptyList())
        } else
            onPostProcessed(emptyList())
        if (mAdapter.itemCount < 1)
            onPostProcessed(null)
    }

    override fun onItemClick(target: View, data: IntPair<StudioBase>) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent = Intent(host, StudioActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, data.second.id)
                }
                startActivity(intent)
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<StudioBase>) = Unit
}
