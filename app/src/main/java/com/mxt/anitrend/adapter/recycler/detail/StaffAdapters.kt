package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
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
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterSeriesBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.staff.StaffCharacterRoleRow
import com.mxt.anitrend.util.KeyUtil

private data class CharacterRoleRowState(
    val characterName: String,
    val mediaTitle: String,
    val imageUrl: String?,
    val isFavourite: Boolean,
)

private fun CharacterStaffBase.roleImageUrl(): String? = character.image?.let { it.extraLarge ?: it.large }

private fun CharacterStaffBase.toRoleRowState(): CharacterRoleRowState = CharacterRoleRowState(
    characterName = character.name?.fullName.orEmpty(),
    mediaTitle = media.title?.userPreferred.orEmpty(),
    imageUrl = roleImageUrl(),
    isFavourite = character.isFavourite,
)

private fun CharacterStaffBase.hasSameRoleContent(other: CharacterStaffBase): Boolean = character.name?.fullName == other.character.name?.fullName &&
    media.title?.userPreferred == other.media.title?.userPreferred &&
    roleImageUrl() == other.roleImageUrl() &&
    character.isFavourite == other.character.isFavourite

private val staffItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is MediaBase && newItem is MediaBase -> oldItem.id == newItem.id && oldItem.contentType == newItem.contentType
        oldItem is CharacterStaffBase && newItem is CharacterStaffBase -> oldItem.character.id == newItem.character.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is CharacterStaffBase && newItem is CharacterStaffBase -> oldItem.hasSameRoleContent(newItem)
        else -> oldItem == newItem
    }
}

/** Displays media associated with a staff member, including group headers. */
class StaffMediaAdapter(
    private val onMediaClick: (View, MediaBase) -> Unit,
    private val onMediaLongClick: (MediaBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(staffItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(parent)
    } else {
        MediaViewHolder(AdapterSeriesBinding.inflate(parent.context.getLayoutInflater(), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class MediaViewHolder(
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

/** Displays character roles associated with a staff member. */
class StaffCharacterRolesAdapter(
    private val onCharacterClick: (View, CharacterStaffBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(staffItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(parent)
    } else {
        val rowMargin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
        CharacterViewHolder(
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
            },
        )
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
        private val characterRoleState = mutableStateOf<CharacterRoleRowState?>(null)

        init {
            composeView.setContent {
                AniTrendTheme {
                    characterRoleState.value?.let { item ->
                        StaffCharacterRoleRow(
                            characterName = item.characterName,
                            mediaTitle = item.mediaTitle,
                            imageUrl = item.imageUrl,
                            isFavourite = item.isFavourite,
                            onClick = { this@CharacterViewHolder.onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            characterRoleState.value = (model as? CharacterStaffBase)?.toRoleRowState()
        }

        override fun onViewRecycled() {
            characterRoleState.value = null
        }

        override fun onClick(v: View) {
            boundItem()?.let { onCharacterClick(v, it) }
        }

        private fun boundItem(): CharacterStaffBase? = bindingAdapterPosition
            .takeIf { it != RecyclerView.NO_POSITION }
            ?.let(::getItem)
            as? CharacterStaffBase

        override fun onLongClick(v: View): Boolean = false
    }
}
