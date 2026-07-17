package com.mxt.anitrend.view.fragment.list

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
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.util.selectedIndices
import com.mxt.anitrend.view.activity.detail.MediaActivity
import java.util.Locale

/**
 * Created by max on 2018/02/03.
 * Multi purpose media browse fragment
 */
open class MediaBrowseFragment : FragmentBaseList<MediaBase, PageContainer<MediaBase>, MediaPresenter>() {
    protected lateinit var requestArgs: Bundle
    private var mediaBrowseUtil: MediaBrowseUtil? = null

    companion object {
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
        requestArgs =
            Bundle(arguments ?: Bundle()).apply {
                if (!containsKey(KeyUtil.arg_page_limit)) {
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                }
                if (!containsKey(KeyUtil.arg_asHtml)) {
                    putBoolean(KeyUtil.arg_asHtml, false)
                }
            }
        mediaBrowseUtil = arguments?.parcelable(KeyUtil.arg_media_util)

        val browseUtil = mediaBrowseUtil ?: MediaBrowseUtil(true)
        mediaBrowseUtil = browseUtil

        isPager = true
        isFilterableEnabled = browseUtil.isFilterEnabled

        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, browseUtil.isCompactType)
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)

        mColumnSize =
            if (browseUtil.isCompactType) {
                R.integer.grid_giphy_x3
            } else {
                if (presenter.settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                    R.integer.single_list_x1
                } else {
                    R.integer.grid_list_x2
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        if (mediaBrowseUtil?.isBasicFilter == true) {
            menu.findItem(R.id.action_type).isVisible = false
            menu.findItem(R.id.action_year).isVisible = false
            menu.findItem(R.id.action_status).isVisible = false
            menu.findItem(R.id.action_genre).isVisible = false
            menu.findItem(R.id.action_tag).isVisible = false
        }
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
                ) { dialog, which ->
                    presenter.settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: presenter.settings.sortOrder,
                    )
                }
                return true
            }
            R.id.action_genre -> {
                val genres: List<Genre> = presenter.database.genreCollection
                if (CompatUtil.isEmpty(genres)) {
                    NotifyUtil
                        .makeText(
                            ctx,
                            R.string.app_splash_loading,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    val genresIndexMap = presenter.settings.selectedGenres.orEmpty()
                    val selectedGenres = genresIndexMap.keys.toTypedArray()
                    DialogUtil.createCheckList(
                        ctx,
                        R.string.app_filter_genres,
                        genres,
                        selectedGenres,
                        { _, _, _ -> },
                    ) { dialog, _ ->
                        val selectedIndices =
                            GenreTagUtil.createGenreSelectionMap(
                                genres,
                                dialog.selectedIndices,
                            )
                        presenter.settings.selectedGenres = selectedIndices
                    }
                }
                return true
            }
            R.id.action_tag -> {
                val tagList: List<MediaTag> = presenter.database.mediaTags
                if (CompatUtil.isEmpty(tagList)) {
                    NotifyUtil
                        .makeText(
                            ctx,
                            R.string.app_splash_loading,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    val tagsIndexMap = presenter.settings.selectedTags.orEmpty()
                    val selectedTags = tagsIndexMap.keys.toTypedArray()
                    DialogUtil.createCheckList(
                        ctx,
                        R.string.app_filter_tags,
                        tagList,
                        selectedTags,
                        { _, _, _ -> },
                    ) { dialog, _ ->
                        val selectedIndices =
                            GenreTagUtil.createTagSelectionMap(
                                tagList,
                                dialog.selectedIndices,
                            )
                        presenter.settings.selectedTags = selectedIndices
                    }
                }
                return true
            }
            R.id.action_type -> {
                val animeFormats =
                    arrayOf<String?>(
                        null,
                        KeyUtil.TV,
                        KeyUtil.TV_SHORT,
                        KeyUtil.MOVIE,
                        KeyUtil.SPECIAL,
                        KeyUtil.OVA,
                        KeyUtil.ONA,
                        KeyUtil.MUSIC,
                    )
                val mangaFormats =
                    arrayOf<String?>(
                        null,
                        KeyUtil.MANGA,
                        KeyUtil.NOVEL,
                        KeyUtil.ONE_SHOT,
                    )
                if (CompatUtil.equals(requestArgs.getString(KeyUtil.arg_mediaType), KeyUtil.ANIME)) {
                    DialogUtil.createSelection(
                        ctx,
                        R.string.app_filter_show_type,
                        CompatUtil.getIndexOf(animeFormats, presenter.settings.animeFormat),
                        CompatUtil.getStringList(ctx, R.array.anime_formats),
                    ) { dialog, _ ->
                        presenter.settings.animeFormat = animeFormats.getOrNull(dialog.selectedIndex)
                    }
                } else {
                    DialogUtil.createSelection(
                        ctx,
                        R.string.app_filter_show_type,
                        CompatUtil.getIndexOf(mangaFormats, presenter.settings.mangaFormat),
                        CompatUtil.getStringList(ctx, R.array.manga_formats),
                    ) { dialog, _ ->
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
                    yearRanges,
                ) { dialog, _ ->
                    presenter.settings.saveSeasonYear(yearRanges[dialog.selectedIndex])
                }
                return true
            }
            R.id.action_status -> {
                val mediaStatuses =
                    arrayOf<String?>(
                        null,
                        KeyUtil.FINISHED,
                        KeyUtil.RELEASING,
                        KeyUtil.NOT_YET_RELEASED,
                        KeyUtil.CANCELLED,
                    )
                DialogUtil.createSelection(
                    ctx,
                    R.string.anime,
                    CompatUtil.getIndexOf(mediaStatuses, presenter.settings.mediaStatus),
                    CompatUtil.getStringList(ctx, R.array.media_status),
                ) { dialog, _ ->
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
        val bundle = viewModel?.params ?: return
        val pref = presenter.settings

        bundle.apply {
            clear()
            putAll(requestArgs)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            applyAdultContentPreference(
                displayAdultContent = pref.displayAdultContent,
                configuredValue = requestArgs.takeIf { it.containsKey(KeyUtil.arg_isAdult) }?.getBoolean(KeyUtil.arg_isAdult),
            )
        }

        if (isFilterableEnabled) {
            if (mediaBrowseUtil?.isBasicFilter != true) {
                if (CompatUtil.equals(requestArgs.getString(KeyUtil.arg_mediaType), KeyUtil.MANGA)) {
                    bundle.putString(
                        KeyUtil.arg_startDateLike,
                        String.format(Locale.getDefault(), "%d%%", presenter.settings.seasonYear),
                    )
                    bundle.putString(KeyUtil.arg_format, pref.mangaFormat)
                } else {
                    bundle.putInt(KeyUtil.arg_seasonYear, presenter.settings.seasonYear)
                    bundle.putString(KeyUtil.arg_format, pref.animeFormat)
                }

                bundle.putString(KeyUtil.arg_status, pref.mediaStatus)
                bundle.putStringArrayList(
                    KeyUtil.arg_genres,
                    ArrayList(GenreTagUtil.getMappedValues(pref.selectedGenres).orEmpty()),
                )
                bundle.putStringArrayList(
                    KeyUtil.arg_tags,
                    ArrayList(GenreTagUtil.getMappedValues(pref.selectedTags).orEmpty()),
                )
            }
            bundle.putString(KeyUtil.arg_sort, pref.mediaSort + pref.sortOrder)
        }
        viewModel?.requestData(KeyUtil.MEDIA_BROWSE_REQ, ctx)
    }

    protected fun Bundle.applyAdultContentPreference(
        displayAdultContent: Boolean,
        configuredValue: Boolean? = null,
    ) {
        if (!displayAdultContent) {
            putBoolean(KeyUtil.arg_isAdult, false)
        } else if (configuredValue != null) {
            putBoolean(KeyUtil.arg_isAdult, configuredValue)
        } else {
            remove(KeyUtil.arg_isAdult)
        }
    }

    protected fun Bundle.putQueryValue(
        key: String,
        value: Any?,
    ) {
        when (value) {
            null -> remove(key)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Number -> putInt(key, value.toInt())
            else -> putString(key, value.toString())
        }
    }

    protected fun Bundle.putQueryStringList(
        key: String,
        value: Any?,
    ) {
        when (value) {
            is Iterable<*> -> putStringArrayList(key, ArrayList(value.mapNotNull { it?.toString() }))
            null -> remove(key)
            else -> putStringArrayList(key, arrayListOf(value.toString()))
        }
    }

    override fun onChanged(value: PageContainer<MediaBase>?) {
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
