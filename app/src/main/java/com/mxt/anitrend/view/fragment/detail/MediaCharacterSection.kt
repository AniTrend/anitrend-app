package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.detail.projectGroupedCharacterRows
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.viewmodel.MediaCharacterViewModel
import kotlinx.coroutines.launch

/** View-only characters section used by the media destination. */
class MediaCharacterSection(
    context: Context,
    private val viewModel: MediaCharacterViewModel,
    private val mediaId: Long,
    private val mediaType: String?,
    private val isAdultContent: Boolean,
    private val onOpenCharacter: (View, CharacterBase) -> Unit,
) {
    private val listSection = DetailListSection(
        context = context,
        onLoadPage = ::load,
    )

    /** Inflates the character list section view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View = listSection.createView(inflater, container)

    /** Starts collecting character state for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is MediaCharacterViewModel.UiState.Loading -> listSection.renderLoading()
                        is MediaCharacterViewModel.UiState.Success -> render(state.content, state)
                        is MediaCharacterViewModel.UiState.Error -> listSection.renderError(state.message)
                    }
                }
            }
        }
    }

    /** Activates the section and loads characters when needed. */
    fun select() = listSection.select()

    /** Saves the section pagination state under [key]. */
    fun saveState(outState: android.os.Bundle, key: String) = listSection.saveState(outState, key)

    /** Restores the section pagination state from [savedState] under [key]. */
    fun restoreState(savedState: android.os.Bundle?, key: String) = listSection.restoreState(savedState, key)

    /** Releases the section view resources. */
    fun destroyView() = listSection.destroyView()

    private fun load(page: Int) {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        viewModel.load(
            mediaId = mediaId,
            type = type,
            page = page,
            isAdult = if (isAdultContent) null else false,
        )
    }

    private fun render(
        content: ConnectionContainer<EdgeContainer<CharacterEdge>>,
        emissionIdentity: Any,
    ) {
        val connection = content.connection
        val groupedItems: List<RecyclerItem> = GroupingUtil.groupCharactersByRole(connection.edges, null)
        val rows = projectGroupedCharacterRows(groupedItems, onOpenCharacter)
        listSection.renderPage(rows, connection.pageInfo, connection.isEmpty, emissionIdentity)
    }
}
