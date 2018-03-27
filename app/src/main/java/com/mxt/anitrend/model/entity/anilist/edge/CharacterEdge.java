package com.mxt.anitrend.model.entity.anilist.edge;

import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.attribute.Edge;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

/**
 * Created by max on 2018/03/25.
 * CharacterEdge
 */

public class CharacterEdge extends Edge<CharacterBase> {

    private @KeyUtil.CharacterRole String role;
    private List<StaffBase> voiceActors;
    private List<MediaBase> media;

    /**
     * The characters role in the media
     */
    public @KeyUtil.CharacterRole String getRole() {
        return role;
    }

    /**
     * The voice actors of the character
     */
    public List<StaffBase> getVoiceActors() {
        return voiceActors;
    }

    /**
     * The media the character is in
     */
    public List<MediaBase> getMedia() {
        return media;
    }
}
