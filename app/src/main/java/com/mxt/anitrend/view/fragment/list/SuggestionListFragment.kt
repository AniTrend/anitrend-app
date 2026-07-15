package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.mxt.anitrend.R
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.selectedIndex

/**
 * Created by max on 2017/11/04.
 * Suggestions adapter
 */
class SuggestionListFragment : MediaBrowseFragment() {
    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): SuggestionListFragment {
            val args =
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putBoolean(KeyUtil.arg_onList, false)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                }
            return SuggestionListFragment().apply {
                arguments = args
            }
        }
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val pref: Settings = presenter.settings
        val bundle = viewModel?.params ?: return
        bundle.putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        bundle.putBoolean(KeyUtil.arg_onList, false)
        bundle.putStringArrayList(KeyUtil.arg_tags, ArrayList(presenter.getTopFavouriteTags(6).orEmpty()))
        bundle.putStringArrayList(KeyUtil.arg_genres, ArrayList(presenter.getTopFavouriteGenres(4).orEmpty()))
        bundle.putString(KeyUtil.arg_sort, pref.mediaSort + pref.sortOrder)
        bundle.putInt(KeyUtil.arg_page, presenter.currentPage)
        bundle.putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
        bundle.applyAdultContentPreference(displayAdultContent = pref.displayAdultContent)
        viewModel?.requestData(KeyUtil.MEDIA_BROWSE_REQ, ctx)
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
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(KeyUtil.MediaSortType, presenter.settings.mediaSort),
                    CompatUtil.capitalizeWords(KeyUtil.MediaSortType),
                ) { dialog, _ ->
                    presenter.settings.mediaSort = KeyUtil.MediaSortType[dialog.selectedIndex]
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
}
