package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.AdapterCharacterBinding
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

private val mediaCharacterItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is CharacterBase && newItem is CharacterBase -> oldItem.id == newItem.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
}

@Suppress("UNCHECKED_CAST")
class MediaCharacterAdapter(
    private val onCharacterClick: (View, CharacterBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(mediaCharacterItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(
            AdapterEntityGroupBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    } else {
        CharacterViewHolder(AdapterCharacterBinding.inflate(parent.context.getLayoutInflater(), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class CharacterViewHolder(
        private val binding: AdapterCharacterBinding,
    ) : RecyclerViewHolder<RecyclerItem>(binding.root) {
        private var character: CharacterBase? = null

        init {
            itemView.findViewById<View>(R.id.container)?.setOnClickListener { view ->
                character?.let { onCharacterClick(view, it) }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            character = model as? CharacterBase
            val current = character ?: return
            AspectImageView.setImage(binding.characterImg, current.image)
            binding.characterNameText.text = current.name?.fullName
            binding.favouriteIndicator.visibility = if (current.isFavourite) View.VISIBLE else View.GONE
        }

        override fun onViewRecycled() {
            Glide.with(itemView.context).clear(binding.characterImg)
            character = null
        }

        override fun onClick(v: View) = character?.let { onCharacterClick(v, it) } ?: Unit

        override fun onLongClick(v: View): Boolean = false
    }
}
