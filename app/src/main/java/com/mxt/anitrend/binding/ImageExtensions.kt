package com.mxt.anitrend.binding

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AvatarImageView
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase


@BindingAdapter("avatarUrl")
fun AvatarImageView.setImage(url: String?) {
    Glide.with(context).load(url).apply(RequestOptions.centerCropTransform())
            .apply(RequestOptions.placeholderOf(R.drawable.avatar_placeholder))
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}

@BindingAdapter("avatarUrl")
fun AvatarImageView.setImage(imageBase: ImageBase?) {
    setImage(imageBase?.large)
}