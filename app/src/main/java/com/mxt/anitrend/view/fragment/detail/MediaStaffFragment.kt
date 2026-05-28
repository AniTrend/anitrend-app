package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupStaffRoleAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.activity.detail.StaffActivity

/**
 * Created by max on 2018/01/18.
 */
class MediaStaffFragment :
    FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<StaffEdge>>, MediaPresenter>() {

    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaStaffFragment {
            return MediaStaffFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            mediaId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = GroupStaffRoleAdapter(ctx)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
            .putVariable(KeyUtil.arg_id, mediaId)
            .putVariable(KeyUtil.arg_type, mediaType)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)

        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_STAFF_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<EdgeContainer<StaffEdge>>?) {
        val edgeContainer = content?.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo())
                    presenter.setPageInfo(edgeContainer.pageInfo)
                if (!edgeContainer.isEmpty)
                    onPostProcessed(GroupingUtil.groupStaffByRole(edgeContainer.edges, mAdapter.data))
                else
                    onPostProcessed(emptyList())
            }
        } else
            onPostProcessed(emptyList())
        if (mAdapter.itemCount < 1)
            onPostProcessed(null)
    }

    override fun onItemClick(target: View, data: IntPair<RecyclerItem>) {
        when (target.id) {
            R.id.container -> {
                val staff = data.second as? StaffBase ?: return
                val host = activity ?: return
                val intent = Intent(host, StaffActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, staff.id)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<RecyclerItem>) = Unit
}
