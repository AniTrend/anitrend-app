package com.mxt.anitrend.model.entity.anilist.edge

import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.attribute.Edge
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/03/25.
 * MediaEdge
 */
class MediaEdge : Edge<MediaBase>() {
    /**
     * The list of characters in the media
     */
    var characters: List<CharacterBase>? = null

    /**
     * The characters role in the media
     */
    @KeyUtil.CharacterRole
    var characterRole: String? = null

    /**
     * The type of relation to the parent model
     */
    @KeyUtil.MediaRelation
    var relationType: String? = null

    /**
     * The voice actors of the character
     */
    var voiceActors: List<StaffBase>? = null

    /**
     * The role of the staff member in the production of the media
     */
    var staffRole: String? = null
}
