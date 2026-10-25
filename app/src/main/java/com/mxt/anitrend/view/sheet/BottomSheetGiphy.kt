package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.databinding.BottomSheetGiphyBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.giphy.GiphyGrid
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.base.GiphyPreviewActivity
import com.mxt.anitrend.viewmodel.GiphyViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BottomSheetGiphy : BottomSheetBase<GiphyContainer>() {
    private var binding: BottomSheetGiphyBinding? = null
    private val items = mutableStateOf<List<Giphy>>(emptyList())
    var onGiphySelected: ((Giphy) -> Unit)? = null
    private val giphyViewModel: GiphyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dialog-only content has no fragment viewLifecycleOwner.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                giphyViewModel.state.collect { state ->
                    when (state) {
                        GiphyViewModel.UiState.Loading -> binding?.stateLayout?.showLoading()
                        is GiphyViewModel.UiState.Success -> {
                            items.value = state.container.data
                            updateUI()
                        }
                        is GiphyViewModel.UiState.Error -> showError(state.message)
                    }
                }
            }
        }
        giphyViewModel.loadIfNeeded()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val content = BottomSheetGiphyBinding.inflate(layoutInflater)
        binding = content
        dialog.setContentView(content.root)
        bindToolbarViews(content.root)
        createBottomSheetBehavior(content.root)
        searchView = content.customSheetToolbar.searchView
        content.giphyGridComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val nestedScrollInterop = rememberNestedScrollInteropConnection()
                AniTrendTheme {
                    GiphyGrid(
                        modifier = Modifier.nestedScroll(nestedScrollInterop),
                        items = items.value,
                        onSelect = { item ->
                            onGiphySelected?.invoke(item)
                            closeDialog()
                        },
                        onPreview = ::openPreview,
                        onLoadMore = giphyViewModel::loadMore,
                    )
                }
            }
        }
        setupSearchDelegate()
        return dialog
    }

    private fun setupSearchDelegate() {
        mSearchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                searchQuery = query
            }

            override fun onSearchSubmitted(query: String?) {
                searchQuery = query
                giphyViewModel.refresh(query)
            }

            override fun onSearchShown() {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                if (!TextUtils.isEmpty(searchQuery)) searchView?.setQuery(searchQuery, false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        toolbarSearch?.visibility = View.VISIBLE
    }

    private fun updateUI() {
        if (items.value.isEmpty()) {
            showEmpty(getString(R.string.layout_empty_response))
            return
        }
        binding?.stateLayout?.showContent()
        val settings = KoinExt.get(Settings::class.java)
        if (settings.shouldShowTipFor(KeyUtil.KEY_GIPHY_TIP)) {
            activity?.let {
                NotifyUtil.createAlerter(
                    it,
                    R.string.title_new_feature,
                    R.string.text_giphy_feature,
                    R.drawable.ic_gif_white_24dp,
                    R.color.colorStateBlue,
                    KeyUtil.DURATION_LONG,
                )
            }
            settings.disableTipFor(KeyUtil.KEY_GIPHY_TIP)
        }
    }

    override fun showError(error: String) {
        super.showError(error)
        binding?.stateLayout?.showError(
            context?.getCompatDrawable(R.drawable.ic_emoji_cry),
            error,
            getString(R.string.try_again),
        ) { giphyViewModel.refresh(searchQuery) }
    }

    override fun showEmpty(message: String) {
        super.showEmpty(message)
        binding?.stateLayout?.showError(
            context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
            message,
            getString(R.string.try_again),
        ) { giphyViewModel.refresh(searchQuery) }
    }

    private fun openPreview(item: Giphy) {
        activity?.let { host ->
            // Keep the preview destination's missing-URL error path.
            val sample = item.images[KeyUtil.GIPHY_LARGE_DOWN_SAMPLE]
            host.startActivity(GiphyPreviewActivity.newIntent(host, sample?.url ?: ""))
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
