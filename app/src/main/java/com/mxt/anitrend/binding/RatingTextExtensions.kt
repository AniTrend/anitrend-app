package com.mxt.anitrend.binding

import androidx.databinding.BindingAdapter
import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.base.MediaBase


@BindingAdapter("rating")
fun RatingTextView.setAverageRating(mediaBase: MediaBase) {
    setRating(mediaBase)
    setListStatus(mediaBase)
    setFavourState(mediaBase.isFavourite)
}

@BindingAdapter("rating")
fun RatingTextView.setAverageRating(mediaList: MediaList) {
    setListStatus()
    setRating(mediaList)
    setFavourState(mediaList.media.isFavourite)
}