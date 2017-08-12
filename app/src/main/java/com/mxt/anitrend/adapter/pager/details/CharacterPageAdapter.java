package com.mxt.anitrend.adapter.pager.details;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Character;
import com.mxt.anitrend.api.model.CharacterSmall;
import com.mxt.anitrend.view.detail.fragment.CharacterActorFragment;
import com.mxt.anitrend.view.detail.fragment.CharacterAnimeFragment;
import com.mxt.anitrend.view.detail.fragment.CharacterMangaFragment;
import com.mxt.anitrend.view.detail.fragment.CharacterOverviewFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

/**
 * Created by max on 2017/08/12.
 */

public class CharacterPageAdapter extends DefaultStatePagerAdapter {

    private Character model;
    private CharacterSmall actors;

    public CharacterPageAdapter(FragmentManager fragmentManager, Character model, CharacterSmall actors, Context context) {
        super(fragmentManager, context);
        this.model = model;
        this.actors = actors;
        mTitles = context.getResources().getStringArray(R.array.staff_page_titles);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CharacterOverviewFragment.newInstance(model);
            case 1:
                return CharacterAnimeFragment.newInstance(model);
            case 2:
                return CharacterMangaFragment.newInstance(model);
            case 3:
                return CharacterActorFragment.newInstance(actors);
        }
        return null;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        int count = super.getCount();
        return actors.getActor() != null?count:count - 1;
    }
}