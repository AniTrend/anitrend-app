package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.afollestad.materialdialogs.DialogAction
import com.mxt.anitrend.R
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil

/**
 * Created by max on 2017/11/04.
 * Suggestions adapter
 */
class SuggestionListFragment : MediaBrowseFragment() {

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): SuggestionListFragment {
            val args = Bundle(params).apply {
                putParcelable(
                    KeyUtil.arg_graph_params,
                    GraphUtil.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                        .putVariable(KeyUtil.arg_onList, false)
                )
            }
            return SuggestionListFragment().apply {
                arguments = args
            }
        }
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val pref: Settings = presenter.settings
        val bundle = viewModel?.params ?: Bundle.EMPTY
        queryContainer.putVariable(KeyUtil.arg_tagsInclude, presenter.getTopFavouriteTags(6))
            .putVariable(KeyUtil.arg_genresInclude, presenter.getTopFavouriteGenres(4))
            .putVariable(KeyUtil.arg_sort, pref.mediaSort + pref.sortOrder)
            .putVariable(KeyUtil.arg_page, presenter.currentPage)
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_BROWSE_REQ, ctx)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
                    CompatUtil.capitalizeWords(KeyUtil.MediaSortType)
                ) { dialog, which ->
                    if (which == DialogAction.POSITIVE)
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
                    CompatUtil.getStringList(ctx, R.array.order_by_types)
                ) { dialog, which ->
                    if (which == DialogAction.POSITIVE)
                        presenter.settings.saveSortOrder(
                            sortOrders.getOrNull(dialog.selectedIndex) ?: presenter.settings.sortOrder
                        )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
