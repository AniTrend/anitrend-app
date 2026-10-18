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
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterSeriesBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.character.CharacterMediaHeaderRow
import com.mxt.anitrend.ui.character.CharacterMediaHeaderUiState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.staff.StaffRow
import com.mxt.anitrend.ui.staff.StaffRowUiState
import com.mxt.anitrend.util.KeyUtil

private val characterItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is MediaBase && newItem is MediaBase -> oldItem.id == newItem.id && oldItem.contentType == newItem.contentType
        oldItem is StaffBase && newItem is StaffBase -> oldItem.id == newItem.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
}

/** Displays media entries grouped by character role and forwards media actions. */
class CharacterMediaAdapter(
    private val onMediaClick: (View, MediaBase) -> Unit,
    private val onMediaLongClick: (MediaBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(characterItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(parent)
    } else {
        CharacterMediaViewHolder(
            AdapterSeriesBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class CharacterMediaViewHolder(
        private val binding: AdapterSeriesBinding,
    ) : RecyclerViewHolder<RecyclerItem>(binding.root) {
        init {
            itemView.findViewById<View>(R.id.container)?.setOnClickListener { view ->
                boundMedia()?.let { onMediaClick(view, it) }
            }
            itemView.findViewById<View>(R.id.container)?.setOnLongClickListener {
                boundMedia()?.let(onMediaLongClick)
                true
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            val media = model as? MediaBase ?: return
            AspectImageView.setImage(binding.seriesImage, media.coverImage)
            SeriesStatusWidget.setStatus(binding.seriesStatus, media)
            SeriesYearTypeTextView.htmlText(binding.seriesYearType, media)
            binding.customRatingWidget.setAverageRating(media)
            binding.seriesTitle.setTitle(media)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.seriesImage)
        }

        override fun onClick(v: View) = boundMedia()?.let { onMediaClick(v, it) } ?: Unit

        override fun onLongClick(v: View): Boolean {
            boundMedia()?.let(onMediaLongClick)
            return true
        }

        private fun boundMedia(): MediaBase? = bindingAdapterPosition
            .takeIf { it != RecyclerView.NO_POSITION }
            ?.let(::getItem)
            as? MediaBase
    }
}

/** Displays a character's media and staff entries and forwards item actions. */
class CharacterActorsAdapter(
    private val onMediaClick: ItemClickListener<RecyclerItem>,
    private val onStaffClick: (View, StaffBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(characterItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        CharacterMediaHeaderViewHolder(parent)
    } else {
        CharacterStaffViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class CharacterMediaHeaderViewHolder(
        parent: ViewGroup,
    ) : RecyclerViewHolder<RecyclerItem>(
        ComposeView(parent.context).apply {
            id = R.id.container
            val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(margin, margin, margin, margin) }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        },
    ) {
        private val composeView = itemView as ComposeView
        private var media: MediaBase? = null
        private val rowState = mutableStateOf<CharacterMediaHeaderUiState?>(null)

        init {
            composeView.setContent {
                AniTrendTheme {
                    rowState.value?.let { current ->
                        CharacterMediaHeaderRow(
                            state = current,
                            onClick = { this@CharacterMediaHeaderViewHolder.onClick(composeView) },
                            onLongClick = { this@CharacterMediaHeaderViewHolder.onLongClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            val current = model as? MediaBase
            media = current
            rowState.value = current?.let { CharacterMediaHeaderUiState.from(it) }
        }

        override fun onViewRecycled() {
            media = null
            rowState.value = null
        }

        override fun onClick(v: View) {
            val pair = isValidIndexPair()
            val model = media
            if (model != null && isClickable(model) && pair.value) {
                onMediaClick.onItemClick(v, IndexedValue(pair.index, model))
            }
        }

        override fun onLongClick(v: View): Boolean {
            val pair = isValidIndexPair()
            val model = media
            if (model != null && isLongClickable(model) && pair.value) {
                onMediaClick.onItemLongClick(v, IndexedValue(pair.index, model))
                return true
            }
            return false
        }
    }

    private inner class CharacterStaffViewHolder(
        parent: ViewGroup,
    ) : RecyclerViewHolder<RecyclerItem>(
        ComposeView(parent.context).apply {
            id = R.id.container
            val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(margin, margin, margin, margin) }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        },
    ) {
        private val composeView = itemView as ComposeView
        private val staffState = mutableStateOf<StaffRowUiState?>(null, neverEqualPolicy())
        private var staff: StaffBase? = null

        init {
            composeView.setContent {
                AniTrendTheme {
                    staffState.value?.let { current ->
                        StaffRow(
                            state = current,
                            onClick = { this@CharacterStaffViewHolder.onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            staff = model as? StaffBase
            staffState.value = staff?.let { StaffRowUiState.from(it) }
        }

        override fun onViewRecycled() {
            staffState.value = null
            staff = null
        }

        override fun onClick(v: View) {
            staff?.let { onStaffClick(v, it) }
        }

        override fun onLongClick(v: View): Boolean = false
    }
}
