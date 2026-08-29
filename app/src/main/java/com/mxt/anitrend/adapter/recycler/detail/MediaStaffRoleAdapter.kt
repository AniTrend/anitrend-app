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
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.databinding.AdapterStaffBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

private val mediaStaffItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is StaffBase && newItem is StaffBase -> oldItem.id == newItem.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
}

@Suppress("UNCHECKED_CAST")
class MediaStaffRoleAdapter(
    private val onStaffClick: (View, StaffBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(mediaStaffItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(
            AdapterEntityGroupBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    } else {
        StaffViewHolder(AdapterStaffBinding.inflate(parent.context.getLayoutInflater(), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class StaffViewHolder(
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
