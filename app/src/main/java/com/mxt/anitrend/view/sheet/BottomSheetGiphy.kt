package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.GiphyAdapter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.sheet.BottomSheetGiphyList
import com.mxt.anitrend.base.custom.view.search.MaterialSearchView
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.databinding.BottomSheetListBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.base.GiphyModel
import com.mxt.anitrend.model.entity.giphy.Gif
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.base.GiphyPreviewActivity
import com.mxt.anitrend.viewmodel.GiphyViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class BottomSheetGiphy : BottomSheetGiphyList() {
    private var binding: BottomSheetListBinding? = null
    private var searchView: MaterialSearchView? = null

    private lateinit var giphyViewModel: GiphyViewModel

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetGiphy = BottomSheetGiphy().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        presenter = BasePresenter(ctx)
        mAdapter = GiphyAdapter(ctx)
        mColumnSize = resources.getInteger(R.integer.grid_giphy_x3)
        isPager = true

        // Direct ViewModel replaces the legacy viewModel?.requestData(...) path.
        giphyViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    GiphyViewModel(
                        giphyService = WebFactory.createGiphyService(ctx),
                    ) as T
            },
        )[GiphyViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeGiphyViewModel()
    }

    private fun observeGiphyViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                giphyViewModel.state.collect { state ->
                    when (state) {
                        is GiphyViewModel.UiState.Loading -> {
                            stateLayout?.showLoading()
                        }
                        is GiphyViewModel.UiState.Success -> {
                            super@BottomSheetGiphy.onChanged(state.container)
                        }
                        is GiphyViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetListBinding.inflate(layoutInflater)
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        bindListViews(requireNotNull(binding).root)
        createBottomSheetBehavior(requireNotNull(binding).root)
        searchView = binding?.customSheetToolbar?.searchView
        mLayoutManager = StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL)
        return dialog
    }

    override fun updateUI() {
        toolbarTitle?.text = getString(mTitle)
        toolbarSearch?.visibility = View.VISIBLE
        mSearchDelegate = object : ISearchDelegate {
            override fun onQueryChanged(query: String?) {
                searchQuery = query
            }

            override fun onSearchSubmitted(query: String?) {
                searchQuery = query
                stateLayout?.showLoading()
                onRefresh()
            }

            override fun onSearchShown() {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                if (!TextUtils.isEmpty(searchQuery)) {
                    searchView?.setQuery(searchQuery, false)
                }
            }
        }
        injectAdapter()
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

    override fun makeRequest() {
        val hasQuery = !searchQuery.isNullOrEmpty()
        val offset = presenter.currentOffset
        if (hasQuery) {
            giphyViewModel.search(searchQuery!!, offset)
        } else {
            giphyViewModel.loadTrending(offset)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<Giphy>,
    ) {
        EventBus.getDefault().post(data)
        closeDialog()
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<Giphy>,
    ) {
        activity?.let { host ->
            val index = KeyUtil.GIPHY_LARGE_DOWN_SAMPLE
            val giphySample: Gif? = data.value.images[index]
            // Empty-string sentinel: when giphySample?.url is null, the
            // empty string triggers fromIntent → null → error toast in
            // GiphyPreviewActivity, preserving pre-existing behaviour.
            val intent = GiphyPreviewActivity.newIntent(host, giphySample?.url ?: "")
            host.startActivity(intent)
        }
    }

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)
    }
}
