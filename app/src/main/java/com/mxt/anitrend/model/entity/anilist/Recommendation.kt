package com.mxt.anitrend.model.entity.anilist

import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.RecommendationBase

class Recommendation : RecommendationBase() {
    var media: MediaBase? = null
}
