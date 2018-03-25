package com.mxt.anitrend.model.entity.anilist.edge;

import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.attribute.Edge;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;

/**
 * Created by max on 2018/03/25.
 * CharacterEdge
 */

public class CharacterEdge extends Edge<CharacterBase> {

    private @KeyUtils.CharacterRole String role;
    private List<StaffBase> voiceActors;

    /**
     * The characters role in the media
     */
    public @KeyUtils.CharacterRole String getRole() {
        return role;
    }

    /**
     * The voice actors of the character
     */
    public List<StaffBase> getVoiceActors() {
        return voiceActors;
    }
}
