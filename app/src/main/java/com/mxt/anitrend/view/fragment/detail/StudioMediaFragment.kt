package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.StudioMediaViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudioMediaFragment : FragmentBaseList<MediaBase, ConnectionContainer<PageContainer<MediaBase>>>() {
    private var id: Long = 0

    private val settings: Settings by inject()

    private val mediaViewModel: StudioMediaViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StudioMediaFragment = StudioMediaFragment().apply {
            arguments = args
        }

        /**
         * Resolves the studio identity from the fragment arguments.
         *
         * The typed [StudioScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] extra is bridged with its exact raw value (0 or
         * negative ids pass through, mirroring the pre-refactor getter).
         */
        fun fromBundle(bundle: Bundle?): Long? = resolve(
            typed = bundle?.screenParam<StudioScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
        )

        @VisibleForTesting
        internal fun resolve(typed: StudioScreenParam?, legacyId: Long): Long? {
            typed?.let { param ->
                if (param.studioId > 0) return param.studioId
                // Typed param present but invalid: fall through to the legacy raw value.
            }
            return legacyId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        fromBundle(arguments)?.let { args ->
            id = args
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        isFilterableEnabled = true
        mAdapter = MediaAdapter(ctx, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeMediaViewModel()
    }

    private fun observeMediaViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Collect on STARTED so refreshes coming back from background also update.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaViewModel.state.collect { state ->
                    when (state) {
                        is StudioMediaViewModel.UiState.Loading -> {
                            showLoading()
                        }
                        is StudioMediaViewModel.UiState.Success -> {
                            val container = state.container
                            val pageContainer = container.connection
                            if (pageContainer != null && !pageContainer.isEmpty) {
                                if (pageContainer.hasPageInfo()) {
                                    setPageInfo(pageContainer.pageInfo)
                                }
                                onPostProcessed(pageContainer.pageData)
                            } else {
                                onPostProcessed(emptyList())
                            }
                        }
                        is StudioMediaViewModel.UiState.Error -> {
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
                val mediaSortTypes = KeyUtil.MediaSortType
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(mediaSortTypes, settings.mediaSort),
                    CompatUtil.capitalizeWords(mediaSortTypes),
                ) { dialog, _ ->
                    settings.mediaSort =
                        mediaSortTypes.getOrNull(dialog.selectedIndex)
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

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val pref = settings
        mediaViewModel.load(
            studioId = id,
            page = mScrollListener.currentPage,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = pref.mediaSort + pref.sortOrder,
        )
    }

    /**
     * No-op: the direct ViewModel collection in [observeMediaViewModel] replaces the
     * legacy observer path.
     */
    override fun onChanged(value: ConnectionContainer<PageContainer<MediaBase>>?) = Unit

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
