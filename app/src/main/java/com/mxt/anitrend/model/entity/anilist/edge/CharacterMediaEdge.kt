package com.mxt.anitrend.model.entity.anilist.edge

import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.attribute.Edge
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by LuK1337 on 2021/05/05.
 * CharacterMediaEdge
 */
class CharacterMediaEdge : Edge<CharacterBase>() {
    /**
     * The characters role in the media
     */
    @KeyUtil.CharacterRole
    var role: String? = null

    /**
     * The voice actors of the character
     */
    var voiceActors: List<StaffBase>? = null

    /**
     * The media the character is in
     */
    var media: List<MediaBase>? = null
}
