package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.viewmodel.AiringListViewModel
import com.mxt.anitrend.viewmodel.MediaListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/11/03.
 */
class AiringListFragment : MediaListFragment() {

    private val settings: Settings by inject()
    private val databaseHelper by lazy { DatabaseHelper() }

    private val airingListViewModel: AiringListViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(): AiringListFragment = AiringListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper.currentUser?.let { userBase ->
            userId = userBase.id
            userName = userBase.name
        }
        mediaType = KeyUtil.ANIME
        (mAdapter as? MediaListAdapter)?.setCurrentUser(userName)
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
                            handleSuccess(state.content)
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
        val currentUser = databaseHelper.currentUser ?: return
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

    override fun updateUI() {
        injectAdapter()
    }

    /** StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<MediaListCollection>?) = Unit

    private fun handleSuccess(value: PageContainer<MediaListCollection>) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            val mediaListCollection = value.pageData.firstOrNull()
            if (mediaListCollection != null) {
                val mediaList =
                    mediaListCollection.entries
                        .orEmpty()
                        .filter { entry ->
                            CompatUtil.equals(entry.media.status, KeyUtil.RELEASING)
                        }

                val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
                if (MediaListUtil.isTitleSort(mediaListSort)) {
                    val sorted = MediaListViewModel.sortMediaListByTitle(mediaList, settings.sortOrder)
                    onPostProcessed(sorted)
                } else {
                    onPostProcessed(mediaList)
                }
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
}
