package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterStaffAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.serializable
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.view.activity.detail.CharacterActivity
import com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by LuK1337 on 2021/05/05.
 * MediaAnimeRoleFragment
 */
class MediaAnimeRoleFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>>() {
    private var id: Long = 0
    private var onList: Boolean? = null

    private val mediaAnimeRoleViewModel: MediaAnimeRoleViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaAnimeRoleFragment = MediaAnimeRoleFragment().apply {
            arguments = params
        }

        /**
         * Documented legacy channel: the hosting pager adapters (character/staff)
         * write only legacy wire extras, so the owner-id read stays on the
         * transitional channel. Reads mirror the pre-refactor getter exactly
         * (absent resolves to 0).
         */
        fun fromBundle(bundle: Bundle?): Long = bundle?.getLong(KeyUtil.arg_id) ?: 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = fromBundle(arguments)
        onList = arguments?.serializable(KeyUtil.arg_onList)
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        val ctx = requireContext()
        mAdapter = GroupCharacterStaffAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaAnimeRoleViewModel.state.collect { state ->
                    when (state) {
                        is MediaAnimeRoleViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaAnimeRoleViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaAnimeRoleViewModel.UiState.Error -> {
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
        mediaAnimeRoleViewModel.load(
            id = id,
            onList = onList,
            page = mScrollListener.currentPage,
        )
    }

    private fun handleSuccess(content: ConnectionContainer<EdgeContainer<MediaEdge>>) {
        val edgeContainer = content.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo()) {
                    setPageInfo(edgeContainer.pageInfo)
                }
                if (!edgeContainer.isEmpty) {
                    onPostProcessed(GroupingUtil.groupCharactersByYear(edgeContainer.edges, mAdapter.data))
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
    override fun onChanged(value: ConnectionContainer<EdgeContainer<MediaEdge>>?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                val model = data.value as? CharacterStaffBase ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, CharacterActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, model.character.id)
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
