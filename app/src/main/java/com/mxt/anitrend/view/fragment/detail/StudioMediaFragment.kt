package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity

/**
 * Created by max on 2018/03/25.
 * StudioMediaFragment
 */
class StudioMediaFragment : FragmentBaseList<MediaBase, ConnectionContainer<PageContainer<MediaBase>>, MediaPresenter>() {
    private var id: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StudioMediaFragment = StudioMediaFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            id = args.getLong(KeyUtil.arg_id)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        isFilterableEnabled = true
        mAdapter = MediaAdapter(ctx, true)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_genre).isVisible = false
        menu.findItem(R.id.action_tag).isVisible = false
        menu.findItem(R.id.action_type).isVisible = false
        menu.findItem(R.id.action_year).isVisible = false
        menu.findItem(R.id.action_status).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                val mediaSortTypes = KeyUtil.MediaSortType
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(mediaSortTypes, presenter.settings.mediaSort),
                    CompatUtil.capitalizeWords(mediaSortTypes),
                ) { dialog, _ ->
                    presenter.settings.mediaSort =
                        mediaSortTypes.getOrNull(dialog.selectedIndex)
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
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val pref: Settings = presenter.settings
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, id)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            putString(KeyUtil.arg_sort, pref.mediaSort + pref.sortOrder)
        }
        viewModel?.requestData(KeyUtil.STUDIO_MEDIA_REQ, ctx)
    }

    override fun onChanged(content: ConnectionContainer<PageContainer<MediaBase>>?) {
        val pageContainer = content?.connection
        if (pageContainer != null) {
            if (!pageContainer.isEmpty) {
                if (pageContainer.hasPageInfo()) {
                    presenter.setPageInfo(pageContainer.pageInfo)
                }
                if (!pageContainer.isEmpty) {
                    onPostProcessed(pageContainer.pageData)
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
        data: IndexedValue<MediaBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, data.value.id)
                        putExtra(KeyUtil.arg_mediaType, data.value.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.value.id)
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
