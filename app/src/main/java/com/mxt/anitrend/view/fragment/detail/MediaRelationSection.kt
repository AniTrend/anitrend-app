package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.adapter.recycler.detail.CharacterMediaAdapter
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.viewmodel.MediaRelationViewModel
import kotlinx.coroutines.launch

/** View-only relations section used by the media destination. */
class MediaRelationSection(
    context: Context,
    private val viewModel: MediaRelationViewModel,
    private val mediaId: Long,
    private val mediaType: String?,
    private val isAdultContent: Boolean,
    private val onOpenMedia: (View, MediaBase) -> Unit,
    private val onLongPressMedia: (MediaBase) -> Unit,
) {
    private val adapter = CharacterMediaAdapter(
        onMediaClick = onOpenMedia,
        onMediaLongClick = onLongPressMedia,
    )
    private val listSection = DetailListSection(
        context = context,
        adapter = adapter,
        onLoadPage = ::load,
    )

    fun createView(inflater: LayoutInflater, container: ViewGroup?): View = listSection.createView(inflater, container)

    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is MediaRelationViewModel.UiState.Loading -> listSection.renderLoading()
                        is MediaRelationViewModel.UiState.Success -> render(state.content)
                        is MediaRelationViewModel.UiState.Error -> listSection.renderError(state.message)
                    }
                }
            }
        }
    }

    fun select() = listSection.select()

    fun saveState(outState: android.os.Bundle, key: String) = listSection.saveState(outState, key)

    fun restoreState(savedState: android.os.Bundle?, key: String) = listSection.restoreState(savedState, key)

    fun destroyView() = listSection.destroyView()

    private fun load(page: Int) {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        viewModel.load(
            mediaId = mediaId,
            type = type,
            isAdult = if (isAdultContent) null else false,
        )
    }

    private fun render(content: ConnectionContainer<EdgeContainer<MediaEdge>>) {
        val connection = content.connection
        val items: List<RecyclerItem> = GroupingUtil.groupMediaByRelationType(connection.edges)
        listSection.render(items, connection.pageInfo, connection.isEmpty)
    }
}
