package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupMediaViewHolder
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.databinding.AdapterMediaHeaderBinding
import com.mxt.anitrend.databinding.AdapterSeriesBinding
import com.mxt.anitrend.databinding.AdapterStaffBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

private val characterItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is MediaBase && newItem is MediaBase -> oldItem.id == newItem.id && oldItem.contentType == newItem.contentType
        oldItem is StaffBase && newItem is StaffBase -> oldItem.id == newItem.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
}

class CharacterMediaAdapter(
    private val onMediaClick: (View, MediaBase) -> Unit,
    private val onMediaLongClick: (MediaBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(characterItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(
            AdapterEntityGroupBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
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

class CharacterActorsAdapter(
    private val onMediaClick: ItemClickListener<RecyclerItem>,
    private val onStaffClick: (View, StaffBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(characterItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupMediaViewHolder(
            AdapterMediaHeaderBinding.inflate(parent.context.getLayoutInflater(), parent, false),
            onMediaClick,
        )
    } else {
        CharacterStaffViewHolder(
            AdapterStaffBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class CharacterStaffViewHolder(
        private val binding: AdapterStaffBinding,
    ) : RecyclerViewHolder<RecyclerItem>(binding.root) {
        private var staff: StaffBase? = null

        init {
            itemView.findViewById<View>(R.id.container)?.setOnClickListener { view ->
                staff?.let { onStaffClick(view, it) }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            staff = model as? StaffBase
            val current = staff ?: return
            AspectImageView.setImage(binding.staffImg, current.image)
            binding.staffNameText.text = current.name?.fullName
            if (current.language.isNullOrBlank()) {
                binding.staffLanguageText.visibility = View.GONE
            } else {
                binding.staffLanguageText.visibility = View.VISIBLE
                binding.staffLanguageText.text = CompatUtil.capitalizeWords(current.language)
            }
            binding.favouriteIndicator.visibility = if (current.isFavourite) View.VISIBLE else View.GONE
        }

        override fun onViewRecycled() {
            Glide.with(itemView.context).clear(binding.staffImg)
            staff = null
        }

        override fun onClick(v: View) = staff?.let { onStaffClick(v, it) } ?: Unit

        override fun onLongClick(v: View): Boolean = false
    }
}
