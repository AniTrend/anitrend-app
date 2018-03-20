package com.mxt.anitrend.presenter.base;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.annimon.stream.Stream;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.crunchy.MediaContent;
import com.mxt.anitrend.model.entity.crunchy.Thumbnail;
import com.mxt.anitrend.model.entity.anilist.UserStats;
import com.mxt.anitrend.service.TagGenreService;
import com.mxt.anitrend.util.ComparatorProvider;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by max on 2017/09/16.
 * General presenter for most objects
 */

public class BasePresenter extends CommonPresenter {

    private StringBuilder favouriteGenres;

    public BasePresenter(Context context) {
        super(context);
    }

    public final void checkGenresAndTags(FragmentActivity fragmentActivity) {
        Intent intent = new Intent(fragmentActivity, TagGenreService.class);
        fragmentActivity.startService(intent);
    }

    public String getThumbnail(List<Thumbnail> thumbnails) {
        if(thumbnails == null || thumbnails.size() < 1)
            return null;
        return thumbnails.get(0).getUrl();
    }

    public String getDuration(MediaContent mediaContent) {
        if(mediaContent.getDuration() != null) {
            long timeSpan = Integer.valueOf(mediaContent.getDuration());
            long minutes = TimeUnit.SECONDS.toMinutes(timeSpan);
            long seconds = timeSpan - TimeUnit.MINUTES.toSeconds(minutes);
            return (String.format(Locale.getDefault(), seconds < 10 ? "%d:0%d":"%d:%d", minutes, seconds));
        }
        return "00:00";
    }

    public String getFavouriteGenres() {
        if(favouriteGenres == null) {
            favouriteGenres = new StringBuilder();
            UserStats userStats;
            if (getDatabase().getCurrentUser() != null && (userStats = getDatabase().getCurrentUser().getStats()) != null) {
                if (userStats.getFavourite_genres() != null && !userStats.getFavourite_genres().isEmpty()) {
                    List<Map.Entry<String, Integer>> mStats = Stream.of(userStats.getFavourite_genres())
                            .sorted(ComparatorProvider.getGenresComparator())
                            .limit(3).toList();
                    for (Map.Entry<String, Integer> entry : mStats) {
                        if (favouriteGenres.length() < 1) favouriteGenres.append(entry.getKey());
                        else favouriteGenres.append(",").append(entry.getKey());
                    }
                }
            }
        }
        return favouriteGenres.toString();
    }

    public @Nullable Favourite getFavourites() {
        if(getApplicationPref().isAuthenticated()) {
            UserBase current = getDatabase().getCurrentUser();
            if(current != null)
                return getDatabase().getFavourite(current.getId());
        }
        return null;
    }
}
