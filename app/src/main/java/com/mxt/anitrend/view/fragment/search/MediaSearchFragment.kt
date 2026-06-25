package com.mxt.anitrend.view.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity

/**
 * Created by max on 2017/12/20.
 * series searching fragment
 */
class MediaSearchFragment : FragmentBaseList<MediaBase, PageContainer<MediaBase>, BasePresenter>() {
    private var searchQuery: String? = null

    @KeyUtil.MediaType
    private var mediaType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(
            bundle: Bundle,
            @KeyUtil.MediaType mediaType: String,
        ): MediaSearchFragment {
            val args =
                Bundle(bundle).apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                }
            return MediaSearchFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            searchQuery = args.getString(KeyUtil.arg_search)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, true)
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putString(KeyUtil.arg_search, searchQuery)
            putString(KeyUtil.arg_mediaType, mediaType)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            putString(KeyUtil.arg_sort, KeyUtil.SEARCH_MATCH)
            if (presenter.settings.displayAdultContent) {
                remove(KeyUtil.arg_isAdult)
            } else {
                putBoolean(KeyUtil.arg_isAdult, false)
            }
        }
        viewModel?.requestData(KeyUtil.MEDIA_SEARCH_REQ, ctx)
    }

    override fun onChanged(content: PageContainer<MediaBase>?) {
        if (content != null) {
            if (content.hasPageInfo()) {
                presenter.setPageInfo(content.pageInfo)
            }
            if (!content.isEmpty) {
                onPostProcessed(content.pageData)
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
