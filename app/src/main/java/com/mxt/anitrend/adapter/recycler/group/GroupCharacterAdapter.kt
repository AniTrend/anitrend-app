package com.mxt.anitrend.adapter.recycler.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.character.CharacterRow
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/12/20.
 */
class GroupCharacterAdapter(
    context: Context,
) : RecyclerViewAdapter<RecyclerItem>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        @KeyUtil.RecyclerViewType viewType: Int,
    ): RecyclerViewHolder<RecyclerItem> = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
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

    override fun onViewAttachedToWindow(holder: RecyclerViewHolder<RecyclerItem>) {
        super.onViewAttachedToWindow(holder)
        val layoutParams =
            holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                ?: return
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION &&
            getItemViewType(position) == KeyUtil.RECYCLER_TYPE_HEADER
        ) {
            layoutParams.isFullSpan = true
        }
    }

    @KeyUtil.RecyclerViewType
    override fun getItemViewType(position: Int): Int = data[position].contentType

    override fun getFilter(): Filter? = null

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
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
