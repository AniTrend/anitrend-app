package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.viewmodel.SuggestionListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/04.
 * Suggestions adapter
 */
class SuggestionListFragment : MediaBrowseFragment() {

    private val settings: Settings by inject()

    private val suggestionListViewModel: SuggestionListViewModel by viewModel()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                suggestionListViewModel.state.collect { state ->
                    when (state) {
                        is SuggestionListViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is SuggestionListViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is SuggestionListViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        val sort = settings.mediaSort + settings.sortOrder
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        suggestionListViewModel.load(
            sort = sort,
            page = mScrollListener.currentPage,
            tags = getTopFavouriteTags(6)?.let { ArrayList(it) },
            genres = getTopFavouriteGenres(4)?.let { ArrayList(it) },
            isAdult = isAdult,
        )
    }

    private fun handleSuccess(content: PageContainer<MediaBase>) {
        if (content.hasPageInfo()) {
            setPageInfo(content.pageInfo)
        }
        if (!content.isEmpty) {
            onPostProcessed(content.pageData)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
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
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(KeyUtil.MediaSortType, settings.mediaSort),
                    CompatUtil.capitalizeWords(KeyUtil.MediaSortType),
                ) { dialog, _ ->
                    settings.mediaSort = KeyUtil.MediaSortType[dialog.selectedIndex]
                }
                return true
            }
            R.id.action_order -> {
                val sortOrders = arrayOf(KeyUtil.ASC, KeyUtil.DESC)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_order,
                    CompatUtil.getIndexOf(sortOrders, settings.sortOrder),
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                ) { dialog, _ ->
                    settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: settings.sortOrder,
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // ---- Helpers copied from BasePresenter ----

    private fun getTopFavouriteGenres(limit: Int): List<String>? {
        val userStats = suggestionListViewModel.currentUser?.statistics ?: return null
        val genres = userStats.anime.genres ?: return null
        if (genres.isEmpty()) return null
        return genres
            .sortedByDescending { it.count }
            .mapNotNull { it.genre }
            .take(limit)
    }

    private fun getTopFavouriteTags(limit: Int): List<String>? {
        val userStats = suggestionListViewModel.currentUser?.statistics ?: return null
        val tags = userStats.anime.tags ?: return null
        if (tags.isEmpty()) return null
        return tags.sortedByDescending { it.count }
            .filter { it.tag != null }
            .mapNotNull { it.tag?.name }
            .take(limit)
    }
}
