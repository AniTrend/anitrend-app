package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.BaseModel;
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel;
import com.mxt.anitrend.model.api.retro.anilist.CharacterModel;
import com.mxt.anitrend.model.api.retro.anilist.FeedModel;
import com.mxt.anitrend.model.api.retro.anilist.MediaModel;
import com.mxt.anitrend.model.api.retro.anilist.SearchModel;
import com.mxt.anitrend.model.api.retro.anilist.StaffModel;
import com.mxt.anitrend.model.api.retro.anilist.StudioModel;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.util.KeyUtil;

import retrofit2.Call;
import retrofit2.Callback;

import static com.mxt.anitrend.util.KeyUtil.*;

/**
 * Created by max on 2017/09/16.
 * Handles all service creation for Retrofit Endpoints on a background task,
 * which allows us to perform heavy operations such as token refreshing on demand
 */

@SuppressWarnings("unchecked")
public class RequestHandler<T> extends AsyncTask<Context,Void,Call<T>> {

    private Bundle param;
    private Callback<T> callback;
    private @RequestType
    int requestType;

    public RequestHandler(Bundle param, Callback<T> callback, int requestType) {
        this.param = param;
        this.callback = callback;
        this.requestType = requestType;
    }

    @Override
    protected Call<T> doInBackground(Context... contexts) {
        if(!isCancelled() && callback != null) {
            Context context = contexts[0];
            switch (requestType) {
                case GENRE_COLLECTION_REQ:
                    return (Call<T>) WebFactory.createService(BaseModel.class, context).getGenres(param.getParcelable(arg_graph_params));
                case MEDIA_TAG_REQ:
                    return (Call<T>) WebFactory.createService(BaseModel.class, context).getTags(param.getParcelable(arg_graph_params));


                case EPISODE_FEED_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getRSS(param.getString(arg_search));
                case EPISODE_LATEST_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getLatestFeed();
                case EPISODE_POPULAR_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getPopularFeed();


                case GIPHY_SEARCH_REQ:
                    return (Call<T>) WebFactory.createGiphyService(context).findGif(BuildConfig.GIPHY_KEY, param.getString(arg_search),
                            PAGING_LIMIT, param.getInt(arg_page_offset), "PG", "en");
                case GIPHY_TRENDING_REQ:
                    return (Call<T>) WebFactory.createGiphyService(context).getTrending(BuildConfig.GIPHY_KEY, PAGING_LIMIT, param.getInt(arg_page_offset), "PG");


                case UPDATE_CHECKER_REQ:
                    if (param.containsKey(arg_branch_name))
                        return (Call<T>) WebFactory.createRepositoryService().checkVersion(param.getString(arg_branch_name));
                    return (Call<T>) WebFactory.createRepositoryService().checkVersion();


                case KeyUtil.MEDIA_BROWSE_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaBrowse(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_LIST_BROWSE_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaListBrowse(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_LIST_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaList(param.getParcelable(arg_graph_params));


                case KeyUtil.CHARACTER_ACTORS_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).getCharacterActors(param.getParcelable(arg_graph_params));
                case KeyUtil.CHARACTER_BASE_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).getCharacterBase(param.getParcelable(arg_graph_params));
                case KeyUtil.CHARACTER_MEDIA_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).getCharacterMedia(param.getParcelable(arg_graph_params));
                case KeyUtil.CHARACTER_OVERVIEW_REQ:
                    return (Call<T>) WebFactory.createService(CharacterModel.class, context).getCharacterOverview(param.getParcelable(arg_graph_params));
                case KeyUtil.CHARACTER_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SearchModel.class, context).getCharacterSearch(param.getParcelable(arg_graph_params));


                case KeyUtil.FEED_LIST_REPLY_REQ:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).getFeedListReply(param.getParcelable(arg_graph_params));
                case KeyUtil.FEED_LIST_REQ:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).getFeedList(param.getParcelable(arg_graph_params));
                case KeyUtil.FEED_MESSAGE_REQ:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).getFeedMessage(param.getParcelable(arg_graph_params));


                case KeyUtil.MEDIA_BASE_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaBase(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_CHARACTERS_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaCharacters(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_EPISODES_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaEpisodes(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_OVERVIEW_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaOverview(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_RELATION_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaRelations(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_REVIEWS_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getReviewBrowse(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SearchModel.class, context).getMediaSearch(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_SOCIAL_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaSocial(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_STAFF_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaStaff(param.getParcelable(arg_graph_params));
                case KeyUtil.MEDIA_STATS_REQ:
                    return (Call<T>) WebFactory.createService(MediaModel.class, context).getMediaStats(param.getParcelable(arg_graph_params));
                /*case KeyUtils.MEDIA_TRENDS_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaTrends(param.getParcelable(arg_graph_params));*/


                case KeyUtil.MUT_DELETE_FEED_REPLY:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).deleteActivityReply(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_DELETE_FEED:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).deleteActivity(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_DELETE_MEDIA_LIST:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).deleteMediaListEntry(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_DELETE_REVIEW:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).deleteReview(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_RATE_REVIEW:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).rateReview(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_SAVE_FEED_REPLY:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).saveActivityReply(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_SAVE_MEDIA_LIST:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).saveMediaListEntry(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_SAVE_MESSAGE_FEED:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).saveMessageActivity(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_SAVE_REVIEW:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).saveReview(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_SAVE_TEXT_FEED:
                    return (Call<T>) WebFactory.createService(FeedModel.class, context).saveTextActivity(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_TOGGLE_FAVOURITE:
                    return (Call<T>) WebFactory.createService(BaseModel.class, context).toggleFavourite(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_TOGGLE_FOLLOW:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).toggleFollow(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_TOGGLE_LIKE:
                    return (Call<T>) WebFactory.createService(BaseModel.class, context).toggleLike(param.getParcelable(arg_graph_params));
                case KeyUtil.MUT_UPDATE_MEDIA_LISTS:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).updateMediaListEntries(param.getParcelable(arg_graph_params));


                case KeyUtil.STAFF_BASE_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).getStaffBase(param.getParcelable(arg_graph_params));
                case KeyUtil.STAFF_MEDIA_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).getStaffMedia(param.getParcelable(arg_graph_params));
                case KeyUtil.STAFF_OVERVIEW_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).getStaffOverview(param.getParcelable(arg_graph_params));
                case KeyUtil.STAFF_ROLES_REQ:
                    return (Call<T>) WebFactory.createService(StaffModel.class, context).getStaffRoles(param.getParcelable(arg_graph_params));
                case KeyUtil.STAFF_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SearchModel.class, context).getStaffSearch(param.getParcelable(arg_graph_params));


                case KeyUtil.STUDIO_BASE_REQ:
                    return (Call<T>) WebFactory.createService(StudioModel.class, context).getStudioBase(param.getParcelable(arg_graph_params));
                case KeyUtil.STUDIO_MEDIA_REQ:
                    return (Call<T>) WebFactory.createService(StudioModel.class, context).getStudioMedia(param.getParcelable(arg_graph_params));
                case KeyUtil.STUDIO_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SearchModel.class, context).getStudioSearch(param.getParcelable(arg_graph_params));


                case KeyUtil.USER_ANIME_FAVOURITES_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getAnimeFavourites(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_BASE_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getUserBase(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_CHARACTER_FAVOURITES_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getCharacterFavourites(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_CURRENT_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getCurrentUser(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_FAVOURITES_COUNT_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getFavouritesCount(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_FOLLOWERS_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getFollowers(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_FOLLOWING_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getFollowing(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_MANGA_FAVOURITES_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getMangaFavourites(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_OVERVIEW_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getUserOverview(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_SEARCH_REQ:
                    return (Call<T>) WebFactory.createService(SearchModel.class, context).getUserSearch(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_STAFF_FAVOURITES_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getStaffFavourites(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_STATS_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getUserStats(param.getParcelable(arg_graph_params));
                case KeyUtil.USER_STUDIO_FAVOURITES_REQ:
                    return (Call<T>) WebFactory.createService(UserModel.class, context).getStudioFavourites(param.getParcelable(arg_graph_params));
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
