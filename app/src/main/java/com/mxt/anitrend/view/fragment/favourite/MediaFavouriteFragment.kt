package com.mxt.anitrend.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaFavouritesViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/25.
 * MediaFavouriteFragment
 */
class MediaFavouriteFragment : FragmentBaseList<MediaBase, ConnectionContainer<Favourite>, BasePresenter>() {
    private var userId: Long = 0

    @KeyUtil.MediaType
    private var mediaType: String? = null

    private val settings: Settings by inject()

    private val mediaFavouritesViewModel: MediaFavouritesViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            @KeyUtil.MediaType mediaType: String,
        ): MediaFavouriteFragment {
            val args =
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                }
            return MediaFavouriteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, true)
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaFavouritesViewModel.state.collect { state ->
                    when (state) {
                        is MediaFavouritesViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaFavouritesViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaFavouritesViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        val type = mediaType ?: return
        mediaFavouritesViewModel.load(
            userId = userId,
            page = mScrollListener.currentPage,
            mediaType = type,
        )
    }

    private fun handleSuccess(content: ConnectionContainer<Favourite>) {
        if (!content.isEmpty) {
            val pageContainer =
                if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                    content.connection.anime
                } else {
                    content.connection.manga
                }
            if (pageContainer != null) {
                if (pageContainer.hasPageInfo()) {
                    setPageInfo(pageContainer.pageInfo)
                }
                onPostProcessed(pageContainer.pageData)
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

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: ConnectionContainer<Favourite>?) = Unit

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
