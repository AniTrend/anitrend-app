package com.mxt.anitrend.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity

/**
 * Created by max on 2018/03/25.
 * MediaFavouriteFragment
 */
class MediaFavouriteFragment : FragmentBaseList<MediaBase, ConnectionContainer<Favourite>, BasePresenter>() {
    private var userId: Long = 0

    @KeyUtil.MediaType
    private var mediaType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            @KeyUtil.MediaType mediaType: String,
        ): MediaFavouriteFragment {
            val args =
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                }
            return MediaFavouriteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, true)
        setPresenter(BasePresenter(ctx))
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, userId)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        }
        val requestMode =
            if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                KeyUtil.USER_ANIME_FAVOURITES_REQ
            } else {
                KeyUtil.USER_MANGA_FAVOURITES_REQ
            }
        viewModel?.requestData(requestMode, ctx)
    }

    override fun onChanged(content: ConnectionContainer<Favourite>?) {
        if (content != null) {
            if (!content.isEmpty) {
                val pageContainer =
                    if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                        content.connection.anime
                    } else {
                        content.connection.manga
                    }
                if (pageContainer != null) {
                    if (pageContainer.hasPageInfo()) {
                        presenter.setPageInfo(pageContainer.pageInfo)
                    }
                    onPostProcessed(pageContainer.pageData)
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
        data: IntPair<MediaBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, data.second.id)
                        putExtra(KeyUtil.arg_mediaType, data.second.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IntPair<MediaBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.second.id)
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
}
