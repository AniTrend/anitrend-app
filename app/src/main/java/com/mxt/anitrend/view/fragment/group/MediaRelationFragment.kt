package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
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
 * Created by max on 2018/01/05.
 * MediaRelationFragment
 */
class MediaRelationFragment :
    FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>, MediaPresenter>() {

    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaRelationFragment {
            return MediaRelationFragment().apply {
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
        mAdapter = GroupSeriesAdapter(ctx)
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
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_RELATION_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<EdgeContainer<MediaEdge>>?) {
        val edgeContainer = content?.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo())
                    presenter.setPageInfo(edgeContainer.pageInfo)
                if (!edgeContainer.isEmpty)
                    onPostProcessed(GroupingUtil.groupMediaByRelationType(edgeContainer.edges))
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
