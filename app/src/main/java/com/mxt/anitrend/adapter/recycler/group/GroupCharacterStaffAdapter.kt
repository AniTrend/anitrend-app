package com.mxt.anitrend.adapter.recycler.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.AdapterCharacterStaffBinding
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by LuK1337 on 2021/05/05.
 */
class GroupCharacterStaffAdapter(
    context: Context,
) : RecyclerViewAdapter<RecyclerItem>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        @KeyUtil.RecyclerViewType viewType: Int,
    ): RecyclerViewHolder<RecyclerItem> = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(
            AdapterEntityGroupBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    } else {
        CharacterViewHolder(
            AdapterCharacterStaffBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
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

    inner class CharacterViewHolder(
        private val binding: AdapterCharacterStaffBinding,
    ) : RecyclerViewHolder<RecyclerItem>(binding.root) {
        init {
            bindClickListeners(R.id.container)
        }

        override fun onBindViewHolder(recyclerItem: RecyclerItem) {
            val model = recyclerItem as? CharacterStaffBase ?: return
            AspectImageView.setImage(binding.characterImg, model.character.image)
            binding.characterName.text = model.character.name?.fullName
            binding.mediaTitle.text = model.media.title?.userPreferred
            binding.favouriteIndicator.visibility =
                if (model.character.isFavourite) View.VISIBLE else View.GONE
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.characterImg)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
