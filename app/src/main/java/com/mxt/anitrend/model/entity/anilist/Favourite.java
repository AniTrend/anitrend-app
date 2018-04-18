package com.mxt.anitrend.model.entity.anilist;

import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;

/**
 * Created by Maxwell on 11/12/2016.
 */

public class Favourite {

    private PageContainer<MediaBase> anime;
    private PageContainer<MediaBase> manga;
    private PageContainer<CharacterBase> characters;
    private PageContainer<StaffBase> staff;
    private PageContainer<StudioBase> studios;

    public PageContainer<MediaBase> getAnime() {
        return anime;
    }

    public PageContainer<MediaBase> getManga() {
        return manga;
    }

    public PageContainer<CharacterBase> getCharacters() {
        return characters;
    }

    public PageContainer<StaffBase> getStaff() {
        return staff;
    }

    public PageContainer<StudioBase> getStudios() {
        return studios;
    }
}
