package com.mxt.anitrend.presenter.activity;

import android.content.Context;

import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by max on 2017/11/14.
 */

public class ProfilePresenter extends CommonPresenter {

    public ProfilePresenter(Context context) {
        super(context);
    }

    public String getAnimeTime(int animeTime) {
        float item_time = animeTime / 60;
        if(item_time > 60) {
            item_time /= 24;
            if(item_time > 365)
                return getContext().getString(R.string.anime_time_years, item_time/365);
            return getContext().getString(R.string.anime_time_days, item_time);
        }
        return getContext().getString(R.string.anime_time_hours, item_time);
    }

    public String getMangaChaptersCount(long manga_chap) {
        if(manga_chap > 1000)
            return String.format(Locale.getDefault(), "%.1f K", (float)manga_chap/1000);
        return String.format(Locale.getDefault(), "%d", manga_chap);
    }

    public String getMapCount(HashMap<String, Integer> map) {
        int totalCount = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet())
            totalCount += entry.getValue();

        if(totalCount > 1000)
            return String.format(Locale.getDefault(), "%d K", totalCount/1000);
        return String.format(Locale.getDefault(),"%d", totalCount);
    }
}
