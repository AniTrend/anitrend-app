package com.mxt.anitrend.view.fragment.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaRelationViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/01/05.
 * MediaRelationFragment
 */
class MediaRelationFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    private val settings: Settings by inject()

    private val mediaRelationViewModel: MediaRelationViewModel by viewModel()

    companion object {
        /**
         * Resolves the media identity from the fragment arguments.
         *
         * The typed [MediaScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] / [KeyUtil.arg_mediaType] extras are bridged with
         * their exact raw values (0 or negative ids pass through, mirroring the
         * pre-refactor getter). A typed param present but invalid falls back to the
         * legacy raw values.
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(typed: MediaScreenParam?, legacyId: Long, legacyType: String?): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy values.
            }
            return MediaScreenParam(mediaId = legacyId, mediaType = legacyType)
        }

        @JvmStatic
        fun newInstance(args: Bundle): MediaRelationFragment = MediaRelationFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        fromBundle(arguments)?.let { args ->
            mediaId = args.mediaId
            mediaType = args.mediaType
        }
        mColumnSize = R.integer.grid_giphy_x3
        mAdapter = GroupSeriesAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaRelationViewModel.state.collect { state ->
                    when (state) {
                        is MediaRelationViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaRelationViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaRelationViewModel.UiState.Error -> {
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
        mediaRelationViewModel.load(mediaId = mediaId, type = type, isAdult = isAdult)
    }

    private fun handleSuccess(content: ConnectionContainer<EdgeContainer<MediaEdge>>) {
        val edgeContainer = content.connection
        if (edgeContainer != null) {
            if (!edgeContainer.isEmpty) {
                if (edgeContainer.hasPageInfo()) {
                    setPageInfo(edgeContainer.pageInfo)
                }
                if (!edgeContainer.isEmpty) {
                    onPostProcessed(GroupingUtil.groupMediaByRelationType(edgeContainer.edges))
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
                val media = data.value as? MediaBase ?: return
                val host = activity ?: return
                val intent = MediaActivity.newIntent(host, media.id, media.type)
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
}
