package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.viewmodel.StudioMediaViewModel
import com.mxt.anitrend.viewmodel.StudioViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Unified Studio destination.
 *
 * The former Activity and its hosted media section are one logical screen, so
 * this Fragment owns the studio identity, metadata, media list,
 * toolbar actions, and pagination state without a child FragmentManager.
 */
@Suppress("TooManyFunctions") // Lifecycle, navigation, and studio list responsibilities stay centralized.
class StudioFragment : FragmentBaseList<MediaBase, ConnectionContainer<PageContainer<MediaBase>>>() {

    private var studioId: Long = 0
    private var model: com.mxt.anitrend.domain.model.StudioRecord? = null
    private var favouriteWidget: FavouriteToolbarWidget? = null

    private val settings: Settings by inject()
    private val studioViewModel: StudioViewModel by viewModel()
    private val mediaViewModel: StudioMediaViewModel by viewModel()

    /** Argument helpers for the studio destination. */
    companion object {
        /** Reads the studio identity from typed or legacy arguments. */
        fun fromBundle(bundle: Bundle?): StudioScreenParam? = resolve(
            typed = bundle?.screenParam<StudioScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
        )

        @VisibleForTesting
        internal fun resolve(typed: StudioScreenParam?, legacyId: Long): StudioScreenParam? {
            typed?.let { param ->
                if (param.studioId > 0) return param
            }
            return legacyId.takeIf { it > 0 }?.let(::StudioScreenParam)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studioId = fromBundle(arguments)?.studioId ?: 0L
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        isFilterableEnabled = true
        setInflateMenu(R.menu.custom_menu)
        mAdapter = MediaAdapter(requireContext(), true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStudio()
        observeMedia()
    }

    private fun observeStudio() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    studioViewModel.state.collect { state ->
                        when (state) {
                            is StudioViewModel.UiState.Loading -> Unit
                            is StudioViewModel.UiState.Success -> {
                                model = state.studio
                                (activity as? AppCompatActivity)?.supportActionBar?.title = state.studio.name
                            }
                            is StudioViewModel.UiState.Error -> {
                                showError(state.message)
                            }
                        }
                    }
                }
                launch {
                    combine(
                        studioViewModel.favouriteFlag,
                        studioViewModel.favouriteLoading,
                    ) { flag, loading ->
                        FavouriteWidgetRenderState.fromFlag(
                            flag = flag,
                            fallbackIsFavourite = model?.isFavourite ?: false,
                            isLoading = loading,
                        )
                    }.collect { renderState ->
                        favouriteWidget?.render(renderState)
                    }
                }
            }
        }
    }

    private fun observeMedia() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaViewModel.state.collect { state ->
                    when (state) {
                        is StudioMediaViewModel.UiState.Loading -> showLoading()
                        is StudioMediaViewModel.UiState.Success -> {
                            if (!state.container.isEmpty) {
                                val pageContainer = state.container.connection
                                if (pageContainer.hasPageInfo()) {
                                    setPageInfo(pageContainer.pageInfo)
                                }
                                onPostProcessed(pageContainer.pageData)
                            } else {
                                onPostProcessed(emptyList())
                            }
                        }
                        is StudioMediaViewModel.UiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_genre)?.isVisible = false
        menu.findItem(R.id.action_tag)?.isVisible = false
        menu.findItem(R.id.action_type)?.isVisible = false
        menu.findItem(R.id.action_year)?.isVisible = false
        menu.findItem(R.id.action_status)?.isVisible = false

        val isAuth = studioViewModel.isAuthenticated()
        menu.findItem(R.id.action_favourite)?.isVisible = isAuth
        if (isAuth) {
            favouriteWidget = menu.findItem(R.id.action_favourite)?.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                menu.findItem(R.id.action_favourite)?.isVisible = false
            } else {
                favouriteWidget?.setOnToggleAction {
                    studioViewModel.toggleFavouriteStudio(studioId)
                }
                renderFavouriteWidget()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                val mediaSortTypes = KeyUtil.MediaSortType
                DialogUtil.createSelection(
                    context,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(mediaSortTypes, settings.mediaSort),
                    CompatUtil.capitalizeWords(mediaSortTypes),
                ) { dialog, _ ->
                    settings.mediaSort = mediaSortTypes.getOrNull(dialog.selectedIndex)
                }
                return true
            }
            R.id.action_order -> {
                val sortOrders = arrayOf(KeyUtil.ASC, KeyUtil.DESC)
                DialogUtil.createSelection(
                    context,
                    R.string.app_filter_order,
                    CompatUtil.getIndexOf(sortOrders, settings.sortOrder),
                    CompatUtil.getStringList(context, R.array.order_by_types),
                ) { dialog, _ ->
                    settings.saveSortOrder(sortOrders.getOrNull(dialog.selectedIndex) ?: settings.sortOrder)
                }
                return true
            }
            R.id.action_share -> {
                val current = model
                if (current != null) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(
                            Intent.EXTRA_TEXT,
                            String.format(Locale.getDefault(), "%s - %s", current.name, current.siteUrl),
                        )
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                } else {
                    NotifyUtil.makeText(
                        context,
                        R.string.text_activity_loading,
                        R.drawable.ic_warning_white_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun renderFavouriteWidget() {
        favouriteWidget?.render(
            FavouriteWidgetRenderState.fromFlag(
                flag = studioViewModel.favouriteFlag.value,
                fallbackIsFavourite = model?.isFavourite ?: false,
                isLoading = studioViewModel.favouriteLoading.value,
            ),
        )
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        if (studioId <= 0) {
            showError(getString(R.string.text_error_request))
            return
        }
        mediaViewModel.load(
            studioId = studioId,
            page = mScrollListener.currentPage,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = settings.mediaSort + settings.sortOrder,
        )
    }

    override fun onChanged(value: ConnectionContainer<PageContainer<MediaBase>>?) = Unit

    override fun onItemClick(target: View, data: IndexedValue<MediaBase>) {
        if (target.id == R.id.container) {
            navigateToMedia(MediaScreenParam(data.value.id, data.value.type))
        }
    }

    override fun onItemLongClick(target: View, data: IndexedValue<MediaBase>) {
        if (target.id == R.id.container) {
            if (settings.isAuthenticated) {
                val host = activity ?: return
                mediaActionUtil = MediaActionUtil.Builder().setId(data.value.id).build(host)
                mediaActionUtil.startSeriesAction()
            } else {
                context?.let {
                    NotifyUtil.makeText(
                        it,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (studioId > 0) {
            studioViewModel.load(studioId)
        }
    }

    override fun onDestroyView() {
        favouriteWidget?.setOnToggleAction(null)
        favouriteWidget = null
        super.onDestroyView()
    }
}
