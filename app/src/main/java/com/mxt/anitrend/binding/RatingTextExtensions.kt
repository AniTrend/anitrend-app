package com.mxt.anitrend.binding

import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.domain.model.MediaListItemRenderModel
import com.mxt.anitrend.domain.model.MediaSearchItemRenderModel
import com.mxt.anitrend.domain.model.RecommendationItemRenderModel
import com.mxt.anitrend.model.entity.base.MediaBase

/**
 * Sets the rating, list status and favourite state from the legacy media entity.
 */
fun RatingTextView.setAverageRating(mediaBase: MediaBase) {
    setRating(mediaBase)
    setListStatus(mediaBase)
    setFavourState(mediaBase.isFavourite)
}

/**
 * Sets the rating, list status and favourite state from a media list render model.
 */
fun RatingTextView.setAverageRating(renderModel: MediaListItemRenderModel) {
    setListStatus()
    setRating(renderModel.score)
    setFavourState(renderModel.mediaIsFavourite)
}

/**
 * Sets the rating, list status and favourite state from a recommendation render model.
 */
fun RatingTextView.setAverageRating(renderModel: RecommendationItemRenderModel) {
    setListStatus()
    setRating(renderModel.averageScore)
    setFavourState(renderModel.isFavourite)
}

/**
 * Sets the rating, list status and favourite state from a media search render model.
 */
fun RatingTextView.setAverageRating(renderModel: MediaSearchItemRenderModel) {
    setListStatus()
    setRating(renderModel.averageScore)
    setFavourState(renderModel.isFavourite)
}
