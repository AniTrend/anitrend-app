package com.mxt.anitrend.model.entity.anilist;

import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.RecommendationBase;

public class Recommendation extends RecommendationBase {

    private MediaBase media;

    public MediaBase getMedia() {
        return media;
    }
}
