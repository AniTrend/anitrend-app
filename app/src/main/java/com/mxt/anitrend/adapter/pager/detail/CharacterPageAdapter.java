package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.detail.CharacterOverviewFragment;
import com.mxt.anitrend.view.fragment.group.CharacterActorsFragment;
import com.mxt.anitrend.view.fragment.group.MediaFormatFragment;

/**
 * Created by max on 2017/12/01.
 */

public class CharacterPageAdapter  extends BaseStatePageAdapter {

    public CharacterPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.character_page_titles);
    }

    @Override
    public int getCount() {
        return super.getCount() - 1;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CharacterOverviewFragment.newInstance(getParams());
            case 1:
                return MediaFormatFragment.newInstance(getParams(), KeyUtil.ANIME, KeyUtil.CHARACTER_MEDIA_REQ);
            case 2:
                return MediaFormatFragment.newInstance(getParams(), KeyUtil.MANGA, KeyUtil.CHARACTER_MEDIA_REQ);
            case 3:
                return CharacterActorsFragment.newInstance(getParams());
        }
        return null;
    }
}