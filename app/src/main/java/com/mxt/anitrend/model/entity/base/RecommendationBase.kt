package com.mxt.anitrend.model.entity.base

import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

open class RecommendationBase : RecyclerItem() {
    var id: Long = 0
    var mediaRecommendation: MediaBase? = null
    var rating: Int = 0
    var user: UserBase? = null

    @KeyUtil.RecommendationRating
    var userRating: String? = null
}
