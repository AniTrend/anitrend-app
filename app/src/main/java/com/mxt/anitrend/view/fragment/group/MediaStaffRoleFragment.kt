package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.serializable
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity

/**
 * Created by max on 2018/01/30.
 * MediaStaffRoleFragment
 */
class MediaStaffRoleFragment :
    FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>, MediaPresenter>() {

    private var id: Long = 0
    private var onList: Boolean? = null

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaStaffRoleFragment {
            return MediaStaffRoleFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            id = args.getLong(KeyUtil.arg_id)
            onList = args.serializable(KeyUtil.arg_onList)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = GroupSeriesAdapter(ctx)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
            .putVariable(KeyUtil.arg_id, id)
            .putVariable(KeyUtil.arg_onList, onList)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.STAFF_ROLES_REQ, ctx)
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun onChanged(content: ConnectionContainer<EdgeContainer<MediaEdge>>?) {
        val edgeContainer = content?.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo())
                    presenter.setPageInfo(edgeContainer.pageInfo)
                if (!edgeContainer.isEmpty)
                    onPostProcessed(GroupingUtil.groupMediaByStaffRole(edgeContainer.edges, mAdapter.data))
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
                val media = data.second as? MediaBase ?: return
                val host = activity ?: return
                val intent = Intent(host, MediaActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, media.id)
                    putExtra(KeyUtil.arg_mediaType, media.type)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<RecyclerItem>) {
        when (target.id) {
            R.id.container -> {
                if (presenter.settings.isAuthenticated) {
                    val media = data.second as? MediaBase ?: return
                    val host = activity ?: return
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(media.id).build(host)
                    mediaActionUtil.startSeriesAction()
                } else {
                    context?.let {
                        NotifyUtil.makeText(
                            it,
                            R.string.info_login_req,
                            R.drawable.ic_group_add_grey_600_18dp,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
