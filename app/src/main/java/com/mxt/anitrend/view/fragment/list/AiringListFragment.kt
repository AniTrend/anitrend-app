package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.content.SharedPreferences
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.viewmodel.AiringListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/03.
 */
class AiringListFragment : MediaListFragment() {

    private val settings: Settings by inject()
    private val userRepository: UserRepository by inject()

    private val airingListViewModel: AiringListViewModel by viewModel()

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
    override fun onChanged(value: com.mxt.anitrend.model.entity.container.body.PageContainer<com.mxt.anitrend.model.entity.anilist.MediaListCollection>?) = Unit

    private fun handleSuccess(state: AiringListViewModel.UiState.Success) {
        state.pageInfo?.let(::setPageInfo)
        submitStateList(state.items, state.renderedItems)
        if ((stateListAdapter?.itemCount ?: 0) > 0) {
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
