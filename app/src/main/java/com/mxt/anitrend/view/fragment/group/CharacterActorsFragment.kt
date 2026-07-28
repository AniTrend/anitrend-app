package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupActorAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.StaffActivity
import com.mxt.anitrend.viewmodel.CharacterActorsViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/23.
 * Character actors with their respective media
 */
class CharacterActorsFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>>() {
    private var id: Long = 0

    private val settings: Settings by inject()

    private val characterActorsViewModel: CharacterActorsViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): CharacterActorsFragment = CharacterActorsFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            id = args.getLong(KeyUtil.arg_id)
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        val ctx = requireContext()
        mAdapter = GroupActorAdapter(ctx)

        (mAdapter as? GroupActorAdapter)?.setMediaClickListener(
            object : ItemClickListener<RecyclerItem> {
                override fun onItemClick(
                    target: View,
                    data: IndexedValue<RecyclerItem>,
                ) {
                    when (target.id) {
                        R.id.container -> {
                            val media = data.value as? MediaBase ?: return
                            val host = activity ?: return
                            val intent =
                                Intent(host, MediaActivity::class.java).apply {
                                    putExtra(KeyUtil.arg_id, media.id)
                                    putExtra(KeyUtil.arg_mediaType, media.type)
                                }
                            CompatUtil.startRevealAnim(host, target, intent)
                        }
                    }
                }

                override fun onItemLongClick(
                    target: View,
                    data: IndexedValue<RecyclerItem>,
                ) {
                    when (target.id) {
                        R.id.container -> {
                            if (settings.isAuthenticated) {
                                val media = data.value as? MediaBase ?: return
                                val host = activity ?: return
                                mediaActionUtil =
                                    MediaActionUtil
                                        .Builder()
                                        .setId(media.id)
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
            },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterActorsViewModel.state.collect { state ->
                    when (state) {
                        is CharacterActorsViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is CharacterActorsViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is CharacterActorsViewModel.UiState.Error -> {
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
        characterActorsViewModel.load(id = id, page = mScrollListener.currentPage)
    }

    private fun handleSuccess(content: ConnectionContainer<EdgeContainer<MediaEdge>>) {
        val edgeContainer = content.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo()) {
                    setPageInfo(edgeContainer.pageInfo)
                }
                if (!edgeContainer.isEmpty) {
                    onPostProcessed(GroupingUtil.groupActorMediaEdge(edgeContainer.edges))
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
                val staff = data.value as? StaffBase ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, StaffActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, staff.id)
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
