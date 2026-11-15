package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.character.CharacterRow
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil

private val mediaCharacterItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is CharacterBase && newItem is CharacterBase -> oldItem.id == newItem.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
}

/** Displays characters associated with a media item, including role headers. */
@Suppress("UNCHECKED_CAST")
class MediaCharacterAdapter(
    private val onCharacterClick: (View, CharacterBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(mediaCharacterItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(parent)
    } else {
        val rowMargin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
        val composeView =
            ComposeView(parent.context).apply {
                id = R.id.container
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        setMargins(rowMargin, rowMargin, rowMargin, rowMargin)
                    }
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            }
        CharacterViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class CharacterViewHolder(
        private val composeView: ComposeView,
    ) : RecyclerViewHolder<RecyclerItem>(composeView) {
        private val character = mutableStateOf<CharacterBase?>(null, neverEqualPolicy())

        init {
            composeView.setContent {
                AniTrendTheme {
                    character.value?.let { current ->
                        CharacterRow(
                            characterName = current.name?.fullName.orEmpty(),
                            imageUrl = current.image?.let { it.extraLarge ?: it.large },
                            isFavourite = current.isFavourite,
                            onClick = { onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            character.value = model as? CharacterBase
        }

        override fun onViewRecycled() {
            character.value = null
        }

        override fun onClick(v: View) {
            character.value?.let { onCharacterClick(v, it) }
        }

        override fun onLongClick(v: View): Boolean = false
    }
}
