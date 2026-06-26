package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.view.activity.detail.CharacterActivity

/**
 * Created by max on 2018/01/18.
 */
class MediaCharacterFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<CharacterEdge>>, MediaPresenter>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaCharacterFragment = MediaCharacterFragment().apply {
            arguments = args
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
        mAdapter = GroupCharacterAdapter(ctx)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, mediaId)
            putString(KeyUtil.arg_mediaType, mediaType)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            if (presenter.settings.displayAdultContent) {
                remove(KeyUtil.arg_isAdult)
            } else {
                putBoolean(KeyUtil.arg_isAdult, false)
            }
        }
        viewModel?.requestData(KeyUtil.MEDIA_CHARACTERS_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<EdgeContainer<CharacterEdge>>?) {
        val edgeContainer = content?.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo()) {
                    presenter.setPageInfo(edgeContainer.pageInfo)
                }
                if (!edgeContainer.isEmpty) {
                    onPostProcessed(GroupingUtil.groupCharactersByRole(edgeContainer.edges, mAdapter.data))
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
