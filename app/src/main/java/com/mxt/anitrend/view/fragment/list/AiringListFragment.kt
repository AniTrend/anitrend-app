package com.mxt.anitrend.view.fragment.list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.viewmodel.AiringListViewModel
import com.mxt.anitrend.viewmodel.MediaListDisplayState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/03.
 */
open class AiringListFragment : MediaListFragment() {

    private val settings: Settings by inject()
    private val userRepository: UserRepository by inject()

    private val airingListViewModel: AiringListViewModel by viewModel()

    override val display: StateFlow<MediaListDisplayState?>
        get() = airingListViewModel.display

    override fun onSearchQueryChanged(searchQuery: String?) {
        airingListViewModel.onSearchQueryChanged(searchQuery)
    }

    companion object {
        @JvmStatic
        fun newInstance(): AiringListFragment = AiringListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository.cachedCurrentUser?.let { userBase ->
            userId = userBase.id
            userName = userBase.name
        }
        mediaType = KeyUtil.ANIME
        statusIn = KeyUtil.CURRENT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                airingListViewModel.state.collect { state ->
                    when (state) {
                        is AiringListViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is AiringListViewModel.UiState.Success -> {
                            handleSuccess(state)
                        }
                        is AiringListViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        val currentUser = userRepository.cachedCurrentUser ?: return
        if (userId == 0L) {
            userId = currentUser.id
        }
        val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
        val sortString =
            if (!MediaListUtil.isTitleSort(mediaListSort)) {
                mediaListSort + settings.sortOrder
            } else {
                KeyUtil.MEDIA_ID + settings.sortOrder
            }
        val scoreFormat: ScoreFormat? =
            runCatching { ScoreFormat.valueOf(currentUser.mediaListOptions.scoreFormat) }.getOrNull()
        airingListViewModel.load(
            type = MediaType.ANIME,
            userId = userId.toInt(),
            sort = sortString,
            statusIn = statusIn,
            scoreFormat = scoreFormat,
        )
    }

    /** StateFlow collector above handles the response. */
    override fun onChanged(value: MediaListCollectionPageResult?) = Unit

    /**
     * Resolve against the Airing ViewModel's current state and reuse the shared
     * mutation dispatch, including its viewport-anchor restoration.
     */
    override fun incrementMediaProgress(item: MediaListItemUiModel) {
        val state = airingListViewModel.state.value as? AiringListViewModel.UiState.Success ?: return
        val entry = state.entries.firstOrNull { record ->
            record.id == item.id || record.mediaId == item.mediaId
        } ?: return
        dispatchIncrement(entry)
    }

    private fun handleSuccess(state: AiringListViewModel.UiState.Success) {
        // Rows and entries arrive through the inherited display collector as one
        // snapshot; this handler only owns page info and the surface decision.
        state.pageInfo?.let(pageState::setPageInfo)
        if (state.renderedItems.isNotEmpty()) {
            updateUI()
        } else {
            showEmpty(getString(com.mxt.anitrend.R.string.layout_empty_response))
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (key != null && GraphUtil.isKeyFilter(key)) {
            swipeRefreshLayout.setRefreshing(true)
            onRefresh()
            return
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }
}
