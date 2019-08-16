package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import androidx.databinding.BindingAdapter
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase

/**
 * Created by max on 2017/10/29.
 * Circle image view
 */

class AvatarImageView : AppCompatImageView, CustomView {

    constructor(context: Context) : super(context) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {

    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        Glide.with(context).clear(this)
    }
}
