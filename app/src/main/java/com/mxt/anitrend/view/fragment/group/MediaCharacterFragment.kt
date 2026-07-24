package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.view.activity.detail.CharacterActivity
import com.mxt.anitrend.viewmodel.MediaCharacterViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/01/18.
 */
class MediaCharacterFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<CharacterEdge>>, MediaPresenter>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    private val settings: Settings by inject()

    private val mediaCharacterViewModel: MediaCharacterViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaCharacterFragment = MediaCharacterFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            mediaId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = GroupCharacterAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaCharacterViewModel.state.collect { state ->
                    when (state) {
                        is MediaCharacterViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaCharacterViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaCharacterViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        mediaCharacterViewModel.load(
            mediaId = mediaId,
            type = type,
            page = mScrollListener.currentPage,
            isAdult = isAdult,
        )
    }

    private fun handleSuccess(content: ConnectionContainer<EdgeContainer<CharacterEdge>>) {
        val edgeContainer = content.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo()) {
                    setPageInfo(edgeContainer.pageInfo)
                }
                if (!edgeContainer.isEmpty) {
                    onPostProcessed(GroupingUtil.groupCharactersByRole(edgeContainer.edges, mAdapter.data))
                } else {
                    onPostProcessed(emptyList())
                }
            }
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: ConnectionContainer<EdgeContainer<CharacterEdge>>?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                val character = data.value as? CharacterBase ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, CharacterActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, character.id)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<RecyclerItem>,
    ) = Unit
}
