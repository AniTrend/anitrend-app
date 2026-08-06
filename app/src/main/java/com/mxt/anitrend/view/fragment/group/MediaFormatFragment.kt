package com.mxt.anitrend.view.fragment.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.serializable
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaFormatViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/01/27.
 * Shared fragment between media for staff and character
 */
class MediaFormatFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<PageContainer<MediaBase>>>() {
    private var id: Long = 0

    @KeyUtil.RequestType
    private var requestType: Int = 0

    private val settings: Settings by inject()

    private val mediaFormatViewModel: MediaFormatViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            @KeyUtil.MediaType mediaType: String,
            @KeyUtil.RequestType requestType: Int,
        ): MediaFormatFragment {
            val args =
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                    putInt(KeyUtil.arg_request_type, requestType)
                }
            return MediaFormatFragment().apply {
                arguments = args
            }
        }

        /**
         * Resolves the owner identity from the fragment arguments.
         *
         * The format tab is shared by the character and staff detail pagers. The
         * character pager forwards CharacterActivity's intent extras (typed
         * [CharacterScreenParam] + legacy arg_id); the staff pager forwards
         * StaffActivity's intent extras (typed [StaffScreenParam] + legacy arg_id)
         * when present, or fresh legacy bundles otherwise. The typed identity of the
         * hosting family wins when present and valid; otherwise the exact raw legacy
         * id is used (0 or negative values pass through, mirroring the pre-refactor
         * getter). requestType / mediaType / onList stay on the legacy pager channels.
         */
        fun fromBundle(bundle: Bundle?): Long? = resolve(
            character = bundle?.screenParam<CharacterScreenParam>(),
            staff = bundle?.screenParam<StaffScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
        )

        @VisibleForTesting
        internal fun resolve(character: CharacterScreenParam?, staff: StaffScreenParam?, legacyId: Long): Long? {
            character?.let { param ->
                if (param.characterId > 0) return param.characterId
                // Typed param present but invalid: fall through to the next source.
            }
            staff?.let { param ->
                if (param.staffId > 0) return param.staffId
                // Typed param present but invalid: fall through to the exact raw legacy value.
            }
            return legacyId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            requestType = args.getInt(KeyUtil.arg_request_type)
        }
        fromBundle(arguments)?.let { args ->
            id = args
        }
        mColumnSize = R.integer.grid_giphy_x3
        isPager = true
        mAdapter = GroupSeriesAdapter(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaFormatViewModel.state.collect { state ->
                    when (state) {
                        is MediaFormatViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaFormatViewModel.UiState.Success -> {
                            handleSuccess(state)
                        }
                        is MediaFormatViewModel.UiState.Error -> {
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
        val args = arguments ?: return
        val mediaType = args.getString(KeyUtil.arg_mediaType)
        val onList = args.serializable<Boolean>(KeyUtil.arg_onList)
        mediaFormatViewModel.load(
            id = id,
            onList = onList,
            mediaType = mediaType,
            page = mScrollListener.currentPage,
            requestType = requestType,
        )
    }

    private fun handleSuccess(state: MediaFormatViewModel.UiState.Success) {
        if (state.pageInfo != null) {
            mScrollListener.setPageInfo(state.pageInfo)
        }
        if (state.isEmpty) {
            if (mAdapter.itemCount < 1) {
                showEmpty(getString(R.string.layout_empty_response))
            } else {
                setLimitReached()
            }
        } else if (state.newItems.isNotEmpty()) {
            if (isPager && !swipeRefreshLayout.isRefreshing()) {
                if (mAdapter.itemCount < 1) {
                    mAdapter.onItemsInserted(state.newItems)
                } else {
                    mAdapter.onItemRangeInserted(state.newItems)
                }
            } else {
                mAdapter.onItemsInserted(state.newItems)
            }
            updateUI()
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: ConnectionContainer<PageContainer<MediaBase>>?) = Unit

    override fun onRefresh() {
        isLimit = false
        mScrollListener.onRefreshPage()
        makeRequest()
    }

    override fun onLoadMore() {
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }

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
