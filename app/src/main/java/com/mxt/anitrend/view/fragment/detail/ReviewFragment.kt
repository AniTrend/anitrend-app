package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.sheet.BottomReviewReader

/**
 * Created by max on 2017/12/28.
 * Reviews for a given series
 */
class ReviewFragment : FragmentBaseList<Review, PageContainer<Review>, BasePresenter>() {

    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): ReviewFragment {
            return ReviewFragment().apply {
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
        mAdapter = ReviewAdapter(ctx, true)
        mColumnSize = R.integer.single_list_x1
        isPager = true
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        if (mediaId == 0L)
            return
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
            .putVariable(KeyUtil.arg_mediaId, mediaId)
            .putVariable(KeyUtil.arg_mediaType, mediaType)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_REVIEWS_REQ, ctx)
    }

    override fun onChanged(content: PageContainer<Review>?) {
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

    override fun onItemClick(target: View, data: IntPair<Review>) {
        when (target.id) {
            R.id.series_image -> {
                val mediaBase: MediaBase = data.second.media
                val host = activity ?: return
                val intent = Intent(host, MediaActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, mediaBase.id)
                    putExtra(KeyUtil.arg_mediaType, mediaBase.type)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.user_avatar -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    val intent = Intent(host, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_id, data.second.user.id)
                    }
                    CompatUtil.startRevealAnim(host, target, intent)
                } else {
                    context?.let {
                        NotifyUtil.makeText(
                            it,
                            R.string.info_login_req,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            R.id.review_read_more -> {
                mBottomSheet = BottomReviewReader.Builder()
                    .setReview(data.second)
                    .setTitle(R.string.drawer_title_reviews)
                    .build()
                showBottomSheet()
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<Review>) {
        when (target.id) {
            R.id.series_image -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(data.second.media.id).build(host)
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
