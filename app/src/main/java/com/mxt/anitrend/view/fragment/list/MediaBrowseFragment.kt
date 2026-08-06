package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GenreTagUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.util.selectedIndices
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaBrowseViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2018/02/03.
 * Multi purpose media browse fragment
 */
open class MediaBrowseFragment : FragmentBaseList<MediaBase, PageContainer<MediaBase>>() {
    protected lateinit var requestArgs: Bundle
    private var mediaBrowseUtil: MediaBrowseUtil? = null

    private val settings: Settings by inject()

    private val mediaBrowseViewModel: MediaBrowseViewModel by viewModel()

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
            }
        mediaBrowseUtil = arguments?.parcelable(KeyUtil.arg_media_util)

        val browseUtil = mediaBrowseUtil ?: MediaBrowseUtil(true)
        mediaBrowseUtil = browseUtil

        isPager = true
        isFilterableEnabled = browseUtil.isFilterEnabled

        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, browseUtil.isCompactType)

        mColumnSize =
            if (browseUtil.isCompactType) {
                R.integer.grid_giphy_x3
            } else {
                if (settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                    R.integer.single_list_x1
                } else {
                    R.integer.grid_list_x2
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaBrowseViewModel.state.collect { state ->
                    when (state) {
                        is MediaBrowseViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaBrowseViewModel.UiState.Success -> {
                            handleSuccess(state.content, state.replaceExisting)
                        }
                        is MediaBrowseViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
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
                ) { dialog, which ->
                    settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: settings.sortOrder,
                    )
                }
                return true
            }
            R.id.action_genre -> {
                val genres: List<Genre> = mediaBrowseViewModel.genreCollection
                if (CompatUtil.isEmpty(genres)) {
                    NotifyUtil
                        .makeText(
                            ctx,
                            R.string.app_splash_loading,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    val genresIndexMap = settings.selectedGenres.orEmpty()
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
                        settings.selectedGenres = selectedIndices
                    }
                }
                return true
            }
            R.id.action_tag -> {
                val tagList: List<MediaTag> = mediaBrowseViewModel.mediaTags
                if (CompatUtil.isEmpty(tagList)) {
                    NotifyUtil
                        .makeText(
                            ctx,
                            R.string.app_splash_loading,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    val tagsIndexMap = settings.selectedTags.orEmpty()
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
                        settings.selectedTags = selectedIndices
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
                        CompatUtil.getIndexOf(animeFormats, settings.animeFormat),
                        CompatUtil.getStringList(ctx, R.array.anime_formats),
                    ) { dialog, _ ->
                        settings.animeFormat = animeFormats.getOrNull(dialog.selectedIndex)
                    }
                } else {
                    DialogUtil.createSelection(
                        ctx,
                        R.string.app_filter_show_type,
                        CompatUtil.getIndexOf(mangaFormats, settings.mangaFormat),
                        CompatUtil.getStringList(ctx, R.array.manga_formats),
                    ) { dialog, _ ->
                        settings.mangaFormat = mangaFormats.getOrNull(dialog.selectedIndex)
                    }
                }
                return true
            }
            R.id.action_year -> {
                val yearRanges = DateUtil.getYearRanges(1950, 1)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_year,
                    CompatUtil.getIndexOf(yearRanges, settings.seasonYear),
                    yearRanges,
                ) { dialog, _ ->
                    settings.saveSeasonYear(yearRanges[dialog.selectedIndex])
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
                    CompatUtil.getIndexOf(mediaStatuses, settings.mediaStatus),
                    CompatUtil.getStringList(ctx, R.array.media_status),
                ) { dialog, _ ->
                    settings.mediaStatus = mediaStatuses.getOrNull(dialog.selectedIndex)
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
        // All filter reads go through NavigationArgs so the exact containsKey /
        // null-versus-empty semantics are captured in production helpers and are
        // JVM-testable. The browse destination has no stable identity of its own;
        // every value stays on the legacy bundle channel until a Phase 2+ browse
        // configuration model is introduced.
        val type = NavigationArgs.resolveMediaType(requestArgs.getString(KeyUtil.arg_mediaType))
        val isAdult: Boolean? =
            if (!settings.displayAdultContent) {
                false
            } else {
                NavigationArgs.optionalBoolean(requestArgs.containsKey(KeyUtil.arg_isAdult), requestArgs.getBoolean(KeyUtil.arg_isAdult))
            }

        // Bundle values are explicit caller intent. Settings only provide fallback defaults
        // when filtering is enabled and the caller did not provide that query value.
        val season =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_season), requestArgs.getString(KeyUtil.arg_season))
        var format =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_format), requestArgs.getString(KeyUtil.arg_format))
        var seasonYear =
            NavigationArgs.optionalInt(requestArgs.containsKey(KeyUtil.arg_seasonYear), requestArgs.getInt(KeyUtil.arg_seasonYear))
        var startDateLike =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_startDateLike), requestArgs.getString(KeyUtil.arg_startDateLike))
        var status =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_status), requestArgs.getString(KeyUtil.arg_status))
        var genres: List<String>? =
            NavigationArgs.optionalStringList(requestArgs.containsKey(KeyUtil.arg_genres), requestArgs.getStringArrayList(KeyUtil.arg_genres))
        var tags: List<String>? =
            NavigationArgs.optionalStringList(requestArgs.containsKey(KeyUtil.arg_tags), requestArgs.getStringArrayList(KeyUtil.arg_tags))
        var sort =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_sort), requestArgs.getString(KeyUtil.arg_sort))

        if (isFilterableEnabled) {
            if (mediaBrowseUtil?.isBasicFilter != true) {
                if (CompatUtil.equals(requestArgs.getString(KeyUtil.arg_mediaType), KeyUtil.MANGA)) {
                    if (!requestArgs.containsKey(KeyUtil.arg_startDateLike)) {
                        startDateLike = String.format(Locale.getDefault(), "%d%%", settings.seasonYear)
                    }
                    if (!requestArgs.containsKey(KeyUtil.arg_format)) {
                        format = settings.mangaFormat
                    }
                } else {
                    if (!requestArgs.containsKey(KeyUtil.arg_seasonYear)) {
                        seasonYear = settings.seasonYear
                    }
                    if (!requestArgs.containsKey(KeyUtil.arg_format)) {
                        format = settings.animeFormat
                    }
                }
                if (!requestArgs.containsKey(KeyUtil.arg_status)) {
                    status = settings.mediaStatus
                }
                if (!requestArgs.containsKey(KeyUtil.arg_genres)) {
                    genres = ArrayList(GenreTagUtil.getMappedValues(settings.selectedGenres).orEmpty())
                }
                if (!requestArgs.containsKey(KeyUtil.arg_tags)) {
                    tags = ArrayList(GenreTagUtil.getMappedValues(settings.selectedTags).orEmpty())
                }
            }
            if (!requestArgs.containsKey(KeyUtil.arg_sort)) {
                sort = settings.mediaSort + settings.sortOrder
            }
        }

        mediaBrowseViewModel.load(
            type = type,
            page = mScrollListener.currentPage,
            pageLimit = NavigationArgs.intWithDefault(requestArgs.containsKey(KeyUtil.arg_page_limit), requestArgs.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
            season = season,
            sort = sort,
            isAdult = isAdult,
            format = format,
            seasonYear = seasonYear,
            startDateLike = startDateLike,
            status = status,
            genres = genres,
            tags = tags,
        )
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<MediaBase>?) = Unit

    private fun handleSuccess(
        value: PageContainer<MediaBase>,
        replaceExisting: Boolean,
    ) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            if (replaceExisting) {
                mAdapter.onItemsInserted(value.pageData)
                updateUI()
            } else {
                onPostProcessed(value.pageData)
            }
        } else {
            if (replaceExisting) {
                mAdapter.onItemsInserted(emptyList())
                updateUI()
            } else {
                onPostProcessed(emptyList())
            }
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
                val intent = MediaActivity.newIntent(host, data.value.id, data.value.type)
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
                if (settings.isAuthenticated) {
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
