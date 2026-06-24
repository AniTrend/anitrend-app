package com.mxt.anitrend.adapter.recycler.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.databinding.AdapterStaffBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/01/30.
 * Media staff roles which includes the staff or actor character involvement
 */
class GroupStaffRoleAdapter(
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
        StaffViewHolder(
            AdapterStaffBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    }

    @KeyUtil.RecyclerViewType
    override fun getItemViewType(position: Int): Int = data[position].contentType

    override fun getFilter(): Filter? = null

    inner class StaffViewHolder(
        private val binding: AdapterStaffBinding,
    ) : RecyclerViewHolder<RecyclerItem>(binding.root) {
        init {
            bindClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            val staff = model as? StaffBase ?: return
            AspectImageView.setImage(binding.staffImg, staff.image)
            binding.staffNameText.text = staff.name?.fullName
            val language = staff.language
            if (language.isNullOrBlank()) {
                binding.staffLanguageText.visibility = View.GONE
            } else {
                binding.staffLanguageText.visibility = View.VISIBLE
                binding.staffLanguageText.text = CompatUtil.capitalizeWords(language)
            }
            binding.favouriteIndicator.visibility =
                if (staff.isFavourite) View.VISIBLE else View.GONE
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.staffImg)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
