package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.sheet.BottomReviewReader

/**
 * Created by max on 2017/10/30.
 * Media review browse
 */
class BrowseReviewFragment : FragmentBaseList<Review, PageContainer<Review>, BasePresenter>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(
            @KeyUtil.MediaType mediaType: String,
        ): BrowseReviewFragment {
            val args =
                Bundle().apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                }
            return BrowseReviewFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        isPager = true
        mColumnSize = R.integer.single_list_x1
        isFilterableEnabled = true
        mAdapter = ReviewAdapter(ctx)
        setPresenter(BasePresenter(ctx))
        setViewModel(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_genre).isVisible = false
        menu.findItem(R.id.action_tag).isVisible = false
        menu.findItem(R.id.action_type).isVisible = false
        menu.findItem(R.id.action_year).isVisible = false
        menu.findItem(R.id.action_status).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                val reviewSortTypes = KeyUtil.ReviewSortType
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(reviewSortTypes, presenter.settings.reviewSort),
                    CompatUtil.capitalizeWords(reviewSortTypes),
                ) { dialog, _ ->
                    presenter.settings.reviewSort =
                        reviewSortTypes.getOrNull(dialog.selectedIndex)
                }
                return true
            }
            R.id.action_order -> {
                val sortOrders = arrayOf(KeyUtil.ASC, KeyUtil.DESC)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_order,
                    CompatUtil.getIndexOf(sortOrders, presenter.settings.sortOrder),
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                ) { dialog, _ ->
                    presenter.settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: presenter.settings.sortOrder,
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val pref: Settings = presenter.settings
        viewModel?.params?.apply {
            putString(KeyUtil.arg_mediaType, mediaType)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            putString(KeyUtil.arg_sort, pref.reviewSort + pref.sortOrder)
            putBoolean(KeyUtil.arg_asHtml, false)
        }
        viewModel?.requestData(KeyUtil.MEDIA_REVIEWS_REQ, ctx)
    }

    override fun onChanged(value: PageContainer<Review>?) {
        if (value != null) {
            if (value.hasPageInfo()) {
                presenter.setPageInfo(value.pageInfo)
            }
            if (!value.isEmpty) {
                onPostProcessed(value.pageData)
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
        data: IndexedValue<Review>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                val mediaBase: MediaBase = data.value.media
                val host = activity ?: return
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, mediaBase.id)
                        putExtra(KeyUtil.arg_mediaType, mediaBase.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
            R.id.review_read_more -> {
                mBottomSheet =
                    BottomReviewReader
                        .Builder()
                        .setReview(data.value)
                        .setTitle(R.string.drawer_title_reviews)
                        .build()
                showBottomSheet()
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<Review>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.value.media.id)
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
