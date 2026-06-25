package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupActorAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.StaffActivity

/**
 * Created by max on 2018/03/23.
 * Character actors with their respective media
 */
class CharacterActorsFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>, MediaPresenter>() {
    private var id: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): CharacterActorsFragment = CharacterActorsFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            id = args.getLong(KeyUtil.arg_id)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        val ctx = requireContext()
        mAdapter = GroupActorAdapter(ctx)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)

        (mAdapter as? GroupActorAdapter)?.setMediaClickListener(
            object : ItemClickListener<RecyclerItem> {
                override fun onItemClick(
                    target: View,
                    data: IntPair<RecyclerItem>,
                ) {
                    when (target.id) {
                        R.id.container -> {
                            val media = data.second as? MediaBase ?: return
                            val host = activity ?: return
                            val intent =
                                Intent(host, MediaActivity::class.java).apply {
                                    putExtra(KeyUtil.arg_id, media.id)
                                    putExtra(KeyUtil.arg_mediaType, media.type)
                                }
                            CompatUtil.startRevealAnim(host, target, intent)
                        }
                    }
                }

                override fun onItemLongClick(
                    target: View,
                    data: IntPair<RecyclerItem>,
                ) {
                    when (target.id) {
                        R.id.container -> {
                            if (presenter.settings.isAuthenticated) {
                                val media = data.second as? MediaBase ?: return
                                val host = activity ?: return
                                mediaActionUtil =
                                    MediaActionUtil
                                        .Builder()
                                        .setId(media.id)
                                        .build(host)
                                mediaActionUtil.startSeriesAction()
                            } else {
                                context?.let {
                                    NotifyUtil
                                        .makeText(
                                            it,
                                            R.string.info_login_req,
                                            R.drawable.ic_group_add_grey_600_18dp,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        }
                    }
                }
            },
        )
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, id)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        }
        viewModel?.requestData(KeyUtil.CHARACTER_ACTORS_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<EdgeContainer<MediaEdge>>?) {
        val edgeContainer = content?.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo()) {
                    presenter.setPageInfo(edgeContainer.pageInfo)
                }
                if (!edgeContainer.isEmpty) {
                    onPostProcessed(GroupingUtil.groupActorMediaEdge(edgeContainer.edges))
                } else {
                    onPostProcessed(emptyList())
                }
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
        data: IntPair<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                val staff = data.second as? StaffBase ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, StaffActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, staff.id)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IntPair<RecyclerItem>,
    ) = Unit
}
