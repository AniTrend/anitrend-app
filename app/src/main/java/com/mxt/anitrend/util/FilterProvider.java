package com.mxt.anitrend.util;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.annimon.stream.function.Predicate;
import com.mxt.anitrend.api.model.Review;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.Studio;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.api.structure.ReviewType;
import com.mxt.anitrend.base.custom.async.FilterBackgroundTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by max on 2017/05/13.
 * Provides several filters
 */
@SuppressWarnings("unchecked")
public class FilterProvider {

    /**
     * Filters out adult content if the current user is not the origin of the data
     * <br/>
     * @param current The current users Id
     * @see com.mxt.anitrend.api.model.UserSmall
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<UserActivity> getUserActivityFilter(final int current, List<UserActivity> model) {
        if(model != null)
            try {
                return new FilterBackgroundTask<>(model).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<UserActivity>() {
                    @Override
                    public boolean test(UserActivity value) {
                        return value.getUser_id() == current || !value.getSeries().isAdult();
                    }
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return model;
    }

    /**
     * Filters out adult content or private reviews from non authors
     * <br/>
     * @param current The current users Id
     * @see com.mxt.anitrend.api.model.UserSmall
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<Review> getReviewFilter(@Nullable final UserSmall current, List<Review> model) {
        if(model != null)
            try {
                return new FilterBackgroundTask<>(model).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<Review>() {
                    @Override
                    public boolean test(Review value) {
                        if(current != null)
                            if(value.isReview_private() && value.getUser().getId() == current.getId())
                                return true;

                        if(value.getAnime() != null)
                            return !value.getAnime().isAdult();

                        return !value.getManga().isAdult();
                    }
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return model;
    }

    /**
     * Filters out adult content or private reviews from non authors
     * <br/>
     * @param current The current users Id
     * @see com.mxt.anitrend.api.model.UserSmall
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static ReviewType getReviewFilter(@Nullable final UserSmall current, ReviewType model) {
        if(model != null)
            try {
                List<Review> anime = new FilterBackgroundTask<>(model.getAnime()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<Review>() {
                    @Override
                    public boolean test(Review value) {
                        if(current != null)
                            if(value.isReview_private() && value.getUser().getId() == current.getId())
                                return true;

                            return !value.getAnime().isAdult();
                    }
                }).get();

                List<Review> manga = new FilterBackgroundTask<>(model.getManga()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<Review>() {
                    @Override
                    public boolean test(Review value) {
                        if(current != null)
                            if(value.isReview_private() && value.getUser().getId() == current.getId())
                                return true;

                        return !value.getManga().isAdult();
                    }
                }).get();

                return new ReviewType(anime, manga);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return model;
    }

    /**
     * Filters out adult content from series
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<Series> getSeriesFilter(List<Series> model) {
        if(model != null)
            try {
                return new FilterBackgroundTask<>(model).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<Series>() {
                    @Override
                    public boolean test(Series value) {
                        return !value.isAdult();
                    }
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return model;
    }

    /**
     * Filters out adult content from user lists
     * <br/>
     * @param model the response body from a request or any object matching the signature of this method
     */
    public static List<ListItem> getListItemFilter(final boolean owner, List<ListItem> model) {
        if(model != null)
            try {
                return new FilterBackgroundTask<>(model).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<ListItem>() {
                    @Override
                    public boolean test (ListItem value){
                        if (owner)
                            return true;
                        if (value.getAnime() != null)
                            return (!value.isPrivate() && !value.getAnime().isAdult());
                        return (!value.isPrivate() && !value.getManga().isAdult());
                    }}).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return model;
    }

    /**
     * Performs filtering of adult content and sorts the list according to user prefs
     * <br/>
     * @param model Studio model
     * @param prefs Valid instance of ApiPrefs
     * @see com.mxt.anitrend.presenter.CommonPresenter to understand where an ApiPrefs can be obtained
     */
    public static Studio getStudioFilter(Studio model, ApiPreferences prefs) {
        if(model != null && model.getAnime() != null)
            try {
                final List<Series> temp = new FilterBackgroundTask<>(model.getAnime(), ComparatorProvider.getSeriesStudioComparator(prefs))
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new Predicate<Series>() {
                    @Override
                    public boolean test(Series value) {
                        return !value.isAdult();
                    }
                }).get();
                model.setAnime(temp);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return model;
    }
}
