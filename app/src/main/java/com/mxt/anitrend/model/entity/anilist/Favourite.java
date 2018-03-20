package com.mxt.anitrend.model.entity.anilist;

import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;

/**
 * Created by Maxwell on 11/12/2016.
 */

public class Favourite {

    private long id;
    private PageContainer<Media> anime;
    private PageContainer<Media> manga;
    private PageContainer<CharacterBase> character;
    private PageContainer<StaffBase> staff;
    private PageContainer<StudioBase> studio;

    public Favourite() {

    }

    public long getId() {
        return id;
    }

    public PageContainer<Media> getAnime() {
        return anime;
    }

    public PageContainer<Media> getManga() {
        return manga;
    }

    public PageContainer<CharacterBase> getCharacter() {
        return character;
    }

    public PageContainer<StaffBase> getStaff() {
        return staff;
    }

    public PageContainer<StudioBase> getStudio() {
        return studio;
    }
}
