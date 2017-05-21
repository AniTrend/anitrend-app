package com.mxt.anitrend.adapter.pager.details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.model.Character;

import java.util.Locale;

/**
 * Created by max on 2017/04/09.
 */
public class CharacterPageAdapter extends FragmentStatePagerAdapter {

    private Character character;

    public CharacterPageAdapter(FragmentManager fm, Character model) {
        super(fm);
        this.character = model;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position) {
            case 0:
                return "overview".toUpperCase(locale);
            case 1:
                return "anime".toUpperCase(locale);
            case 2:
                return "manga".toUpperCase(locale);
        }
        return null;
    }
}