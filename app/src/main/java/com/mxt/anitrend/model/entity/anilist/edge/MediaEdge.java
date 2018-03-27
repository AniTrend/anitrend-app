package com.mxt.anitrend.model.entity.anilist.edge;

import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.attribute.Edge;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

/**
 * Created by max on 2018/03/25.
 * MediaEdge
 */

public class MediaEdge extends Edge<MediaBase> {

    private @KeyUtil.CharacterRole String characterRole;
    private @KeyUtil.MediaRelation String relationType;
    private List<StaffBase> voiceActors;
    private String staffRole;

    /**
     * The characters role in the media
     */
    public @KeyUtil.CharacterRole String getCharacterRole() {
        return characterRole;
    }

    /**
     * The voice actors of the character
     */
    public List<StaffBase> getVoiceActors() {
        return voiceActors;
    }

    /**
     * The type of relation to the parent model
     */
    public @KeyUtil.MediaRelation String getRelationType() {
        return relationType;
    }

    /**
     * The role of the staff member in the production of the media
     */
    public String getStaffRole() {
        return staffRole;
    }
}
