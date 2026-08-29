package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaLatestAdapter
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.MediaLatestViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

open class MediaLatestList : MediaBrowseFragment() {

    private val settings: Settings by inject()

    private val mediaLatestViewModel: MediaLatestViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaLatestList {
            val args = Bundle(params)
            return MediaLatestList().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFilterableEnabled = false
        mAdapter = MediaLatestAdapter(requireContext())
        mColumnSize = R.integer.single_list_x1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaLatestViewModel.state.collect { state ->
                    when (state) {
                        is MediaLatestViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaLatestViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaLatestViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        val type = NavigationArgs.resolveMediaType(requestArgs.getString(KeyUtil.arg_mediaType))
        val sort = NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_sort), requestArgs.getString(KeyUtil.arg_sort))
        val isAdult: Boolean? =
            if (!settings.displayAdultContent) {
                false
            } else {
                NavigationArgs.optionalBoolean(requestArgs.containsKey(KeyUtil.arg_isAdult), requestArgs.getBoolean(KeyUtil.arg_isAdult))
            }
        mediaLatestViewModel.load(
            type = type,
            page = mScrollListener.currentPage,
            pageLimit = NavigationArgs.intWithDefault(requestArgs.containsKey(KeyUtil.arg_page_limit), requestArgs.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
            sort = sort,
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
}
