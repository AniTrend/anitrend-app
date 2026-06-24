package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.AdapterStaffBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.util.CompatUtil

/**
 * Created by max on 2017/12/20.
 * StaffAdapter
 */
class StaffAdapter(
    context: Context,
) : RecyclerViewAdapter<StaffBase>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<StaffBase> = StaffViewHolder(
        AdapterStaffBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    inner class StaffViewHolder(
        private val binding: AdapterStaffBinding,
    ) : RecyclerViewHolder<StaffBase>(binding.root) {
        init {
            bindClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: StaffBase) {
            AspectImageView.setImage(binding.staffImg, model.image)
            binding.staffNameText.text = model.name?.fullName
            val language = model.language
            if (language.isNullOrBlank()) {
                binding.staffLanguageText.visibility = View.GONE
            } else {
                binding.staffLanguageText.visibility = View.VISIBLE
                binding.staffLanguageText.text = CompatUtil.capitalizeWords(language)
            }
            binding.favouriteIndicator.visibility =
                if (model.isFavourite) View.VISIBLE else View.GONE
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
