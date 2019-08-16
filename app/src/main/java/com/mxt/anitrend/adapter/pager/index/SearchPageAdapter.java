package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.view.fragment.search.CharacterSearchFragment;
import com.mxt.anitrend.view.fragment.search.MediaSearchFragment;
import com.mxt.anitrend.view.fragment.search.StaffSearchFragment;
import com.mxt.anitrend.view.fragment.search.StudioSearchFragment;
import com.mxt.anitrend.view.fragment.search.UserSearchFragment;

/**
 * Created by max on 2017/12/19.
 */

public class SearchPageAdapter extends BaseStatePageAdapter {

    public SearchPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(new Settings(context).isAuthenticated()? R.array.search_titles_auth : R.array.search_titles);
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
                return MediaSearchFragment.newInstance(getParams(), KeyUtil.ANIME);
            case 1:
                return MediaSearchFragment.newInstance(getParams(), KeyUtil.MANGA);
            case 2:
                return StudioSearchFragment.newInstance(getParams());
            case 3:
                return StaffSearchFragment.newInstance(getParams());
            case 4:
                return CharacterSearchFragment.newInstance(getParams());
            case 5:
                return UserSearchFragment.newInstance(getParams());
        }
        return null;
    }
}
