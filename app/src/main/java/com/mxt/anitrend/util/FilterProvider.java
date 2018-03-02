package com.mxt.anitrend.util;

import android.support.annotation.Nullable;

import com.annimon.stream.Stream;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.anilist.Studio;
import com.mxt.anitrend.model.entity.anilist.UserActivity;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.general.FeedReview;
import com.mxt.anitrend.model.entity.general.SeriesList;
import com.mxt.anitrend.model.entity.general.SeriesList_;
import com.mxt.anitrend.model.entity.group.EntityGroup;

import java.util.List;

/**
 * Created by max on 2017/10/30.
 * Provides several filters
 */

@SuppressWarnings("unchecked")
public final class FilterProvider {

    /**
     * Filters out adult content if the current user is not the origin of the data
     * <br/>
     * @param presenter The parent presenter
     * @see UserBase
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<UserActivity> getUserActivityFilter(CommonPresenter presenter, List<UserActivity> model) {
        if(model != null)
            return Stream.of(model).filter(value ->
                    value.getSeries() == null || presenter.isCurrentUser(value.getUser_id()) || !value.getSeries().isAdult())
                    .toList();
        return null;
    }

    /**
     * Filters out adult content or private reviews from non authors
     * <br/>
     * @param current The current users Id
     * @see UserBase
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<Review> getReviewFilter(@Nullable final UserBase current, List<Review> model) {
        if(model != null)
                return Stream.of(model).filter(value -> {
                    if(current != null)
                        if(value.isReview_private() && value.getUser().getId() == current.getId())
                            return true;

                    if(value.getAnime() != null)
                        return !value.getAnime().isAdult();

                    return !value.getManga().isAdult();
                }).toList();
        return null;
    }

    /**
     * Filters out adult content or private reviews from non authors
     * <br/>
     * @param current The current users Id
     * @see UserBase
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static FeedReview getReviewFilter(@Nullable final UserBase current, FeedReview model) {
        if(model != null) {
            List<Review> anime = Stream.of(model.getAnime()).filter(value -> {
                if (current != null)
                    if (value.isReview_private() && value.getUser().getId() == current.getId())
                        return true;

                return !value.getAnime().isAdult();
            }).toList();

            List<Review> manga = Stream.of(model.getManga()).filter(value -> {
                if (current != null)
                    if (value.isReview_private() && value.getUser().getId() == current.getId())
                        return true;

                return !value.getManga().isAdult();
            }).toList();

            return new FeedReview(anime, manga);
        }
        return null;
    }

    /**
     * Filters out adult content from series
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<Series> getSeriesFilter(List<Series> model) {
        if(model != null)
            return Stream.of(model).filter(value -> !value.isAdult()).toList();
        return null;
    }

    /**
     * Filters out adult content from series
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<EntityGroup> getSeriesGroupFilter(List<EntityGroup> model) {
        if(model != null)
            return Stream.of(model).filter(value -> value.getContentType() != KeyUtils.RECYCLER_TYPE_HEADER && !((Series)value).isAdult()).toList();
        return null;
    }



    public static List<Series> getRecommendedFilter(List<Series> model, DatabaseHelper helper) {
        if(model != null)
            return Stream.of(model).filter(value -> helper.getBoxStore(SeriesList.class)
                            .query().equal(SeriesList_.series_id, value.getId())
                            .build().count() < 1).toList();
        return null;
    }

    /**
     * Filters out adult content from user lists
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<SeriesList> getListItemFilter(final boolean owner, List<SeriesList> model) {
        if(model != null)
            return Stream.of(model)
                    .filter(value -> {
                if (owner)
                    return true;
                else if (value.getAnime() != null)
                    return (!value.isPrivate() && !value.getAnime().isAdult());
                return (!value.isPrivate() && !value.getManga().isAdult());
            }).toList();
        return null;
    }

    /**
     * Performs filtering of adult content and sorts the list according to user prefs
     * <br/>
     * @param model Studio model
     * @param prefs Valid instance of ApiPrefs
     */
    public static Studio getStudioFilter(Studio model, ApplicationPref prefs, String languageTitle) {
        if(model != null && model.getAnime() != null)
            model.setAnime(Stream.of(model.getAnime()).filter(value -> !value.isAdult())
                    .sorted(ComparatorProvider.getSeriesStudioComparator(prefs, languageTitle))
                    .toList());
        return model;
    }

    /**
     * Performs filtering of adult content and sorts the list according to user prefs
     * <br/>
     * @param model Studio model
     */
    public static Studio getStudioFilter(Studio model) {
        if(model != null && model.getAnime() != null)
            model.setAnime(Stream.of(model.getAnime()).filter(value -> !value.isAdult())
                    .toList());
        return model;
    }
}
