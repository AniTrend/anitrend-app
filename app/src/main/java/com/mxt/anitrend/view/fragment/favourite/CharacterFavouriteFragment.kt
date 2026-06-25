package com.mxt.anitrend.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.view.activity.detail.CharacterActivity

/**
 * Created by max on 2018/03/25.
 * CharacterFavouriteFragment
 */
class CharacterFavouriteFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<Favourite>, BasePresenter>() {
    private var userId: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): CharacterFavouriteFragment {
            val args = Bundle(params)
            return CharacterFavouriteFragment().apply {
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
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = GroupCharacterAdapter(ctx)
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, userId)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        }
        viewModel?.requestData(KeyUtil.USER_CHARACTER_FAVOURITES_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<Favourite>?) {
        if (content != null) {
            if (!content.isEmpty) {
                val pageContainer = content.connection.characters
                if (pageContainer != null) {
                    if (pageContainer.hasPageInfo()) {
                        presenter.setPageInfo(pageContainer.pageInfo)
                    }
                    onPostProcessed(GroupingUtil.wrapInGroup(pageContainer.pageData))
                } else {
                    onPostProcessed(emptyList())
                }
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
        data: IntPair<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                val character = data.second as? CharacterBase ?: return
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
        data: IntPair<RecyclerItem>,
    ) = Unit
}
