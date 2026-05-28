package com.mxt.anitrend.model.entity.anilist

import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.PageContainer

/**
 * Created by Maxwell on 11/12/2016.
 */
class Favourite {
    var anime: PageContainer<MediaBase>? = null
    var manga: PageContainer<MediaBase>? = null
    var characters: PageContainer<CharacterBase>? = null
    var staff: PageContainer<StaffBase>? = null
    var studios: PageContainer<StudioBase>? = null
}
