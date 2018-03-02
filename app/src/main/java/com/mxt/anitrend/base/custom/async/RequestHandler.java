package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.CharacterModel;
import com.mxt.anitrend.model.api.retro.anilist.ReviewModel;
import com.mxt.anitrend.model.api.retro.anilist.SeriesModel;
import com.mxt.anitrend.model.api.retro.anilist.StaffModel;
import com.mxt.anitrend.model.api.retro.anilist.StudioModel;
import com.mxt.anitrend.model.api.retro.anilist.UserListModel;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.KeyUtils;

import retrofit2.Call;
import retrofit2.Callback;

import static com.mxt.anitrend.util.KeyUtils.*;

/**
 * Created by max on 2017/09/16.
 * Handles all service creation for Retrofit Endpoints
 */

@SuppressWarnings("unchecked")
public class RequestHandler<T> extends AsyncTask<Context,Void,Call<T>> {

    private Bundle param;
    private Callback<T> callback;
    private @RequestMode int requestType;

    public RequestHandler(Bundle param, Callback<T> callback, int requestType) {
        this.param = param;
        this.callback = callback;
        this.requestType = requestType;
    }

    @Override
    protected Call<T> doInBackground(Context... contexts) {
        if(!isCancelled() && callback != null) {
            Context context = contexts[0];
            ApplicationPref preferences;
            switch (requestType) {
                // user models
                case ACTION_FOLLOW_TOGGLE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).toggleFollow(param.getLong(arg_id));
                case ACTIVITY_CREATE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityStatus(param.getString(arg_text));
                case ACTIVITY_DELETE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityDelete(param.getInt(arg_id));
                case ACTIVITY_EDIT_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityEdit(param.getInt(arg_id), param.getString(arg_text), param.getLong(arg_user_id));
                case ACTIVITY_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).toggleFavourite(param.getInt(arg_id));
                case ACTIVITY_REPLY_DELETE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityDeleteReply(param.getInt(arg_id));
                case ACTIVITY_REPLY_EDIT_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityEditReply(param.getInt(arg_activity_id), param.getInt(arg_id), param.getString(arg_text));
                case ACTIVITY_REPLY_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).toggleReplyFavourite(param.getInt(arg_id));
                case ACTIVITY_REPLY_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityReply(param.getInt(arg_id), param.getString(arg_text));
                case DIRECT_MESSAGE_EDIT_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityMessageEdit(param.getInt(arg_id), param.getLong(arg_recipient_id), param.getString(arg_text), param.getLong(arg_user_id));
                case DIRECT_MESSAGE_SEND_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).activityMessage(param.getString(arg_text), param.getLong(arg_recipient_id));
                case CURRENT_USER_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getCurrentUser();

                case USER_ACCOUNT_REQ:
                    if (param.containsKey(arg_user_id))
                        return (Call<T>) WebFactory.createService(UserModel.class, context).getUser(param.getLong(arg_user_id));
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getUser(param.getString(arg_user_name));

                case USER_FAVOURITES_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getFavourites(param.getLong(arg_user_id));
                case USER_FOLLOWERS_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getFollowers(param.getLong(arg_user_id));
                case USER_FOLLOWING_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getFollowing(param.getLong(arg_user_id));
                case USER_NOTIFICATION_COUNT:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getNotificationCount();
                case USER_NOTIFICATION_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getNotifications();
                case USER_ACTIVITY_BY_ID_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getUserActivity(param.getInt(arg_id));
                case USER_ACTIVITY_REPLIES_BY_ID_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getActivityReplies(param.getInt(arg_id));
                case BROWSE_AIRING_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getAiring(AIRING_LIMIT);
                case USER_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).searchUser(param.getString(arg_search_query), param.getInt(arg_page));
                case AUTH_USER_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getCurrentUser();
                case USER_ACTIVITY_REQ:
                    if(!param.containsKey(arg_user_name))
                        return (Call<T>) WebFactory.createService(UserModel.class, context).getCurrentUserActivity(param.getInt(arg_page), param.getString(arg_request_type));
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getUserActivity(param.getString(arg_user_name), param.getInt(arg_page), param.getString(arg_request_type));



                case ANIME_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).toggleFavourite(SeriesTypes[ANIME], param.getLong(arg_id));
                case GENRE_LIST_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getGenres();
                case TAG_LIST_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getTags();
                case BROWSE_FILTER_REQ:
                    preferences = new ApplicationPref(context);
                    boolean airingData = false;
                    String seriesType = param.getString(arg_series_type);
                    if (!TextUtils.isEmpty(seriesType))
                        airingData = seriesType.equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]);
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getFilteredBrowse(
                            param.getString(arg_series_type), param.getInt(arg_series_year) == 0? null : param.getInt(arg_series_year), param.getString(arg_series_season),
                            param.getString(arg_series_show_type), param.getString(arg_series_status), param.getString(arg_series_genres),
                            null, param.getString(arg_series_tag), param.getString(arg_series_tag_exclude),
                            param.getString(arg_series_sort_by) == null? preferences.getSort_by(): param.getString(arg_series_sort_by),
                            airingData, false, param.getInt(arg_page));

                case BROWSE_ANIME_REQ:
                    preferences = new ApplicationPref(context);
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getFilteredBrowse(
                            SeriesTypes[ANIME], preferences.getYear(), param.getString(arg_season_title),
                            preferences.getAnimeMediaType(), preferences.getAnimeStatus(), preferences.getGenres(), preferences.getExcluded(),
                            param.getString(arg_series_tag), param.getString(arg_series_tag_exclude), preferences.getSort_by(),
                            true, false, param.getInt(arg_page));

                case BROWSE_MANGA_REQ:
                    preferences = new ApplicationPref(context);
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getFilteredBrowse(
                            SeriesTypes[MANGA], preferences.getYear(), null, preferences.getMangaMediaType(),
                            preferences.getMangaStatus(), preferences.getGenres(), preferences.getExcluded(),
                            param.getString(arg_series_tag), param.getString(arg_series_tag_exclude),
                            preferences.getSort_by(), false, false, param.getInt(arg_page));

                case BROWSE_MANGA_LATEST_REQ:
                    preferences = new ApplicationPref(context);
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getFilteredBrowse(
                            SeriesTypes[MANGA], null, null, null,
                            null, null, preferences.getExcluded(),
                            param.getString(arg_series_tag), param.getString(arg_series_tag_exclude),
                            "id-desc", false, false, param.getInt(arg_page));

                case BROWSE_ANIME_LATEST_REQ:
                    preferences = new ApplicationPref(context);
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getFilteredBrowse(
                            SeriesTypes[ANIME], null, null, null,
                            null, null, preferences.getExcluded(),
                            param.getString(arg_series_tag), param.getString(arg_series_tag_exclude),
                            "id-desc", true, false, param.getInt(arg_page));

                case BROWSE_ANIME_TRENDING_REQ:
                    preferences = new ApplicationPref(context);
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getFilteredBrowse(
                            SeriesTypes[ANIME], null, null, preferences.getAnimeMediaType(),
                            AnimeStatusTypes[CURRENTLY_AIRING], preferences.getGenres(), preferences.getExcluded(),
                            param.getString(arg_series_tag), param.getString(arg_series_tag_exclude),
                            "popularity-desc", true, false, param.getInt(arg_page));

                case ANIME_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).findSeries(SeriesTypes[ANIME], param.getString(arg_search_query), param.getInt(arg_page));
                case MANGA_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).findSeries(SeriesTypes[MANGA], param.getString(arg_search_query), param.getInt(arg_page));
                case MANGA_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).toggleFavourite(SeriesTypes[MANGA], param.getLong(arg_id));
                case SERIES_PAGE_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getSeriesPage(param.getString(arg_series_type), param.getLong(arg_id));
                case SERIES_CHARACTER_PAGE_REQ:
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getSeriesCharacters(param.getString(arg_series_type), param.getLong(arg_id));
                case SERIES_ACTIVITY_REQ:
                    if(param.getInt(KeyUtils.arg_series_type) == KeyUtils.ANIME)
                        return (Call<T>) WebFactory.createService(SeriesModel.class, context).getAnimeProgressList(param.getLong(KeyUtils.arg_id), param.getInt(KeyUtils.arg_page));
                    return (Call<T>) WebFactory.createService(SeriesModel.class, context).getMangaProgressList(param.getLong(KeyUtils.arg_id), param.getInt(KeyUtils.arg_page));



                case ANIME_LIST_ITEM_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).getAnimeListStatus(param.getLong(arg_id));
                case ANIME_LIST_ADD_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).addAnimeItem(param.getLong(arg_id),
                            param.getString(arg_list_status), param.getString(arg_list_score), param.getInt(arg_list_score_raw),
                            param.getInt(arg_list_watched), param.getInt(arg_list_re_watched), param.getString(arg_list_notes),
                            param.getString(arg_list_advanced_rating),param.getInt(arg_list_custom_list), param.getInt(arg_list_hidden));
                case ANIME_LIST_DELETE_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).deleteAnime(param.getLong(arg_id));
                case ANIME_LIST_EDIT_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).editAnimeItem(param.getLong(arg_id),
                            param.getString(arg_list_status), param.getString(arg_list_score), param.getInt(arg_list_score_raw),
                            param.getInt(arg_list_watched), param.getInt(arg_list_re_watched), param.getString(arg_list_notes),
                            param.getString(arg_list_advanced_rating),param.getInt(arg_list_custom_list), param.getInt(arg_list_hidden));

                case MANGA_LIST_ITEM_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).getMangaListStatus(param.getLong(arg_id));
                case MANGA_LIST_ADD_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).addMangaItem(param.getLong(arg_id),
                            param.getString(arg_list_status), param.getString(arg_list_score), param.getInt(arg_list_score_raw),
                            param.getInt(arg_list_volumes), param.getInt(arg_list_read), param.getInt(arg_list_re_read),
                            param.getString(arg_list_notes), param.getString(arg_list_advanced_rating),
                            param.getInt(arg_list_custom_list), param.getInt(arg_list_hidden));
                case MANGA_LIST_DELETE_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).deleteManga(param.getLong(arg_id));
                case MANGA_LIST_EDIT_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).editMangaItem(param.getLong(arg_id),
                            param.getString(arg_list_status), param.getString(arg_list_score), param.getInt(arg_list_score_raw),
                            param.getInt(arg_list_volumes), param.getInt(arg_list_read), param.getInt(arg_list_re_read),
                            param.getString(arg_list_notes), param.getString(arg_list_advanced_rating),
                            param.getInt(arg_list_custom_list), param.getInt(arg_list_hidden));

                case USER_ANIME_LIST_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).getAnimeList(param.getString(arg_user_name));
                case USER_MANGA_LIST_REQ:
                    return (Call<T>) WebFactory.createService(UserListModel.class, context).getMangaList(param.getString(arg_user_name));



                case CHARACTER_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).toggleFavourite(param.getLong(arg_id));
                case CHARACTER_INFO_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).getCharacterPage(param.getLong(arg_id));
                case CHARACTER_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).findCharacter(param.getString(arg_search_query), param.getInt(arg_page));



                case STAFF_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).toggleFavourite(param.getLong(arg_id));
                case STAFF_INFO_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).getStaffPage(param.getLong(arg_id));
                case STAFF_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).findStaff(param.getString(arg_search_query), param.getInt(arg_page));



                case REVIEW_ANIME_RATE_REQ:
                    return (Call<T>) WebFactory.createService(ReviewModel.class, context).rateAnimeReview(param.getInt(arg_id), param.getInt(arg_rating));
                case REVIEW_MANGA_RATE_REQ:
                    return (Call<T>) WebFactory.createService(ReviewModel.class, context).rateMangaReview(param.getInt(arg_id), param.getInt(arg_rating));
                case ANIME_REVIEWS_REQ:
                    return (Call<T>) WebFactory.createService(ReviewModel.class, context).getAnimeReviews(param.getLong(arg_id));
                case MANGA_REVIEWS_REQ:
                    return (Call<T>) WebFactory.createService(ReviewModel.class, context).getMangaReviews(param.getLong(arg_id));
                case SERIES_REVIEWS_REQ:
                    return (Call<T>) WebFactory.createService(ReviewModel.class, context).getSeriesReviews(param.getString(arg_review_type), param.getInt(arg_page));



                case STUDIO_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(StudioModel.class, context).findStudio(param.getString(arg_search_query), param.getInt(arg_page));
                case STUDIO_INFO_REQ:
                    return (Call<T>) WebFactory.createService(StudioModel.class, context).getStudioPage(param.getLong(arg_id));
                case STUDIO_FAVOURITE_REQ:
                    return (Call<T>) WebFactory.createService(StudioModel.class, context).toggleFavourite(param.getLong(arg_id));



                case EPISODE_FEED_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getRSS(param.getString(arg_search_query));
                case EPISODE_LATEST_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getLatestFeed();
                case EPISODE_POPULAR_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getPopularFeed();



                case GIPHY_SEARCH_REQ:
                    return (Call<T>) WebFactory.createGiphyService(context).findGif(BuildConfig.GIPHY_KEY, param.getString(arg_search_query),
                            PAGING_LIMIT, param.getInt(arg_page_offset), "PG", "en");
                case GIPHY_TRENDING_REQ:
                    return (Call<T>) WebFactory.createGiphyService(context).getTrending(BuildConfig.GIPHY_KEY, PAGING_LIMIT, param.getInt(arg_page_offset), "PG");



                case UPDATE_CHECKER_REQ:
                    if(param.containsKey(arg_branch_name))
                        return (Call<T>) WebFactory.createRepositoryService().checkVersion(param.getString(arg_branch_name));
                    return (Call<T>) WebFactory.createRepositoryService().checkVersion();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Call<T> call) {
        if(!isCancelled() && call != null && callback != null)
            call.enqueue(callback);
    }

    @Override
    protected void onCancelled(Call<T> call) {
        if(call != null)
            call.cancel();
        callback = null;
        super.onCancelled(call);
    }
}
