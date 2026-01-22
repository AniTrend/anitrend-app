package com.mxt.anitrend.view.fragment.list

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.collection.GenreTagUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import io.github.wax911.library.model.request.QueryContainerBuilder
import java.util.Locale
import java.util.WeakHashMap

/**
 * Created by max on 2018/02/03.
 * Multi purpose media browse fragment
 */
open class MediaBrowseFragment :
    FragmentBaseList<MediaBase, PageContainer<MediaBase>, MediaPresenter>() {

    protected lateinit var queryContainer: QueryContainerBuilder
    private var mediaBrowseUtil: MediaBrowseUtil? = null

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle, queryContainer: QueryContainerBuilder): MediaBrowseFragment {
            val args = Bundle(params).apply {
                putParcelable(KeyUtil.arg_graph_params, queryContainer)
            }
            return MediaBrowseFragment().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(params: Bundle): MediaBrowseFragment {
            val args = Bundle(params)
            return MediaBrowseFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            queryContainer = args.parcelable(KeyUtil.arg_graph_params)
                ?: GraphUtil.getDefaultQuery(true)
            mediaBrowseUtil = args.parcelable(KeyUtil.arg_media_util)
        } ?: run {
            queryContainer = GraphUtil.getDefaultQuery(true)
        }

        val browseUtil = mediaBrowseUtil ?: MediaBrowseUtil(true)
        mediaBrowseUtil = browseUtil

        isPager = true
        isFilterableEnabled = browseUtil.isFilterEnabled

        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, browseUtil.isCompactType)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)

        mColumnSize = if (browseUtil.isCompactType) {
            R.integer.grid_giphy_x3
        } else {
            if (presenter.settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                R.integer.single_list_x1
            } else {
                R.integer.grid_list_x2
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (mediaBrowseUtil?.isBasicFilter == true) {
            menu.findItem(R.id.action_type).isVisible = false
            menu.findItem(R.id.action_year).isVisible = false
            menu.findItem(R.id.action_status).isVisible = false
            menu.findItem(R.id.action_genre).isVisible = false
            menu.findItem(R.id.action_tag).isVisible = false
        }
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
            R.id.action_genre -> {
                val genres: List<Genre> = presenter.database.genreCollection
                if (CompatUtil.isEmpty(genres)) {
                    NotifyUtil.makeText(
                        ctx,
                        R.string.app_splash_loading,
                        R.drawable.ic_warning_white_18dp,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val genresIndexMap = presenter.settings.selectedGenres.orEmpty()
                    val selectedGenres = genresIndexMap.keys.toTypedArray()
                    DialogUtil.createCheckList(
                        ctx,
                        R.string.app_filter_genres,
                        genres,
                        selectedGenres,
                        { _, _, _ -> false }
                    ) { dialog, which ->
                        when (which) {
                            DialogAction.POSITIVE -> {
                                val selectedIndices = GenreTagUtil.createGenreSelectionMap(
                                    genres,
                                    dialog.selectedIndices
                                )
                                presenter.settings.selectedGenres = selectedIndices
                            }
                            DialogAction.NEGATIVE -> {
                                presenter.settings.selectedGenres = WeakHashMap()
                            }
                            else -> Unit
                        }
                    }
                }
                return true
            }
            R.id.action_tag -> {
                val tagList: List<MediaTag> = presenter.database.mediaTags
                if (CompatUtil.isEmpty(tagList)) {
                    NotifyUtil.makeText(
                        ctx,
                        R.string.app_splash_loading,
                        R.drawable.ic_warning_white_18dp,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val tagsIndexMap = presenter.settings.selectedTags.orEmpty()
                    val selectedTags = tagsIndexMap.keys.toTypedArray()
                    DialogUtil.createCheckList(
                        ctx,
                        R.string.app_filter_tags,
                        tagList,
                        selectedTags,
                        { _, _, _ -> false }
                    ) { dialog, which ->
                        when (which) {
                            DialogAction.POSITIVE -> {
                                val selectedIndices = GenreTagUtil.createTagSelectionMap(
                                    tagList,
                                    dialog.selectedIndices
                                )
                                presenter.settings.selectedTags = selectedIndices
                            }
                            DialogAction.NEGATIVE -> {
                                presenter.settings.selectedTags = WeakHashMap()
                            }
                            else -> Unit
                        }
                    }
                }
                return true
            }
            R.id.action_type -> {
                val animeFormats = arrayOf<String?>(
                    null,
                    KeyUtil.TV,
                    KeyUtil.TV_SHORT,
                    KeyUtil.MOVIE,
                    KeyUtil.SPECIAL,
                    KeyUtil.OVA,
                    KeyUtil.ONA,
                    KeyUtil.MUSIC
                )
                val mangaFormats = arrayOf<String?>(
                    null,
                    KeyUtil.MANGA,
                    KeyUtil.NOVEL,
                    KeyUtil.ONE_SHOT
                )
                if (CompatUtil.equals(queryContainer.getVariable(KeyUtil.arg_mediaType), KeyUtil.ANIME)) {
                    DialogUtil.createSelection(
                        ctx,
                        R.string.app_filter_show_type,
                        CompatUtil.getIndexOf(animeFormats, presenter.settings.animeFormat),
                        CompatUtil.getStringList(ctx, R.array.anime_formats)
                    ) { dialog, which ->
                        if (which == DialogAction.POSITIVE)
                            presenter.settings.animeFormat = animeFormats.getOrNull(dialog.selectedIndex)
                    }
                } else {
                    DialogUtil.createSelection(
                        ctx,
                        R.string.app_filter_show_type,
                        CompatUtil.getIndexOf(mangaFormats, presenter.settings.mangaFormat),
                        CompatUtil.getStringList(ctx, R.array.manga_formats)
                    ) { dialog, which ->
                        if (which == DialogAction.POSITIVE)
                            presenter.settings.mangaFormat = mangaFormats.getOrNull(dialog.selectedIndex)
                    }
                }
                return true
            }
            R.id.action_year -> {
                val yearRanges = DateUtil.getYearRanges(1950, 1)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_year,
                    CompatUtil.getIndexOf(yearRanges, presenter.settings.seasonYear),
                    yearRanges
                ) { dialog, which ->
                    if (which == DialogAction.POSITIVE)
                        presenter.settings.saveSeasonYear(yearRanges[dialog.selectedIndex])
                }
                return true
            }
            R.id.action_status -> {
                val mediaStatuses = arrayOf<String?>(
                    null,
                    KeyUtil.FINISHED,
                    KeyUtil.RELEASING,
                    KeyUtil.NOT_YET_RELEASED,
                    KeyUtil.CANCELLED
                )
                DialogUtil.createSelection(
                    ctx,
                    R.string.anime,
                    CompatUtil.getIndexOf(mediaStatuses, presenter.settings.mediaStatus),
                    CompatUtil.getStringList(ctx, R.array.media_status)
                ) { dialog, which ->
                    if (which == DialogAction.POSITIVE)
                        presenter.settings.mediaStatus = mediaStatuses.getOrNull(dialog.selectedIndex)
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
        val bundle = viewModel?.params
        val pref = presenter.settings
        queryContainer.putVariable(KeyUtil.arg_page, presenter.currentPage)

        if (isFilterableEnabled) {
            if (mediaBrowseUtil?.isBasicFilter != true) {
                if (CompatUtil.equals(queryContainer.getVariable(KeyUtil.arg_mediaType), KeyUtil.MANGA)) {
                    queryContainer.putVariable(
                        KeyUtil.arg_startDateLike,
                        String.format(Locale.getDefault(), "%d%%", presenter.settings.seasonYear)
                    ).putVariable(KeyUtil.arg_format, pref.mangaFormat)
                } else {
                    queryContainer.putVariable(KeyUtil.arg_seasonYear, presenter.settings.seasonYear)
                        .putVariable(KeyUtil.arg_format, pref.animeFormat)
                }

                queryContainer.putVariable(KeyUtil.arg_status, pref.mediaStatus)
                    .putVariable(KeyUtil.arg_genres, GenreTagUtil.getMappedValues(pref.selectedGenres))
                    .putVariable(KeyUtil.arg_tags, GenreTagUtil.getMappedValues(pref.selectedTags))
            }
            queryContainer.putVariable(KeyUtil.arg_sort, pref.mediaSort + pref.sortOrder)
        }
        bundle?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_BROWSE_REQ, ctx)
    }

    override fun onChanged(content: PageContainer<MediaBase>?) {
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

    override fun onItemClick(target: View, data: IntPair<MediaBase>) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent = Intent(host, MediaActivity::class.java).apply {
                    putExtra(KeyUtil.arg_id, data.second.id)
                    putExtra(KeyUtil.arg_mediaType, data.second.type)
                }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(target: View, data: IntPair<MediaBase>) {
        when (target.id) {
            R.id.container -> {
                if (presenter.settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(data.second.id).build(host)
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
