package com.mxt.anitrend.base.custom.async

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.*
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.KeyUtil.*
import retrofit2.Call
import retrofit2.Callback

/**
 * Created by max on 2017/09/16.
 * Handles all service creation for Retrofit Endpoints on a background task,
 * which allows us to perform heavy operations such as token refreshing on demand
 */

@Suppress("UNCHECKED_CAST")
class RequestHandler<T>(
        private val param: Bundle,
        private var callback: Callback<T>?,
        @field:RequestType
        private val requestType: Int
) : AsyncTask<Context, Void, Call<T>>() {

    override fun doInBackground(vararg contexts: Context): Call<T>? {
        if (!isCancelled && callback != null) {
            val context = contexts[0]
            when (requestType) {
                GENRE_COLLECTION_REQ -> return WebFactory.createService(BaseModel::class.java, context).getGenres(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_TAG_REQ -> return WebFactory.createService(BaseModel::class.java, context).getTags(param.getParcelable(arg_graph_params)) as Call<T>


                EPISODE_FEED_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getRSS(param.getString(arg_search)) as Call<T>
                EPISODE_LATEST_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).latestFeed as Call<T>
                EPISODE_POPULAR_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).popularFeed as Call<T>


                GIPHY_SEARCH_REQ -> return WebFactory.createGiphyService(context).findGif(BuildConfig.GIPHY_KEY, param.getString(arg_search),
                        PAGING_LIMIT, param.getInt(arg_page_offset), "PG", "en") as Call<T>
                GIPHY_TRENDING_REQ -> return WebFactory.createGiphyService(context).getTrending(BuildConfig.GIPHY_KEY, PAGING_LIMIT, param.getInt(arg_page_offset), "PG") as Call<T>


                UPDATE_CHECKER_REQ -> return WebFactory.createRepositoryService().checkVersion(param.getString(arg_branch_name)) as Call<T>


                MEDIA_LIST_COLLECTION_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaListCollection(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_BROWSE_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaBrowse(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_LIST_BROWSE_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaListBrowse(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_LIST_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaList(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_WITH_LIST_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaWithList(param.getParcelable(arg_graph_params)) as Call<T>


                CHARACTER_ACTORS_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterActors(param.getParcelable(arg_graph_params)) as Call<T>
                CHARACTER_BASE_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterBase(param.getParcelable(arg_graph_params)) as Call<T>
                CHARACTER_MEDIA_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterMedia(param.getParcelable(arg_graph_params)) as Call<T>
                CHARACTER_OVERVIEW_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterOverview(param.getParcelable(arg_graph_params)) as Call<T>
                CHARACTER_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getCharacterSearch(param.getParcelable(arg_graph_params)) as Call<T>


                FEED_LIST_REPLY_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedListReply(param.getParcelable(arg_graph_params)) as Call<T>
                FEED_LIST_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedList(param.getParcelable(arg_graph_params)) as Call<T>
                FEED_MESSAGE_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedMessage(param.getParcelable(arg_graph_params)) as Call<T>


                MEDIA_BASE_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaBase(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_CHARACTERS_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaCharacters(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_EPISODES_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaEpisodes(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_OVERVIEW_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaOverview(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_RELATION_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaRelations(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_REVIEWS_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getReviewBrowse(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getMediaSearch(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_SOCIAL_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaSocial(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_STAFF_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaStaff(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_STATS_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaStats(param.getParcelable(arg_graph_params)) as Call<T>
                MEDIA_RECOMMENDATION_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaRecommendations(param.getParcelable(arg_graph_params)) as Call<T>
                /*case KeyUtils.MEDIA_TREND_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaTrends(param.getParcelable(arg_graph_params));*/

                RECOMMENDATIONS_REQ -> return WebFactory.createService(RecommendationModel::class.java, context).getRecommendationMediaList(param.getParcelable(arg_graph_params)) as Call<T>

                MUT_DELETE_FEED_REPLY -> return WebFactory.createService(FeedModel::class.java, context).deleteActivityReply(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_DELETE_FEED -> return WebFactory.createService(FeedModel::class.java, context).deleteActivity(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_DELETE_MEDIA_LIST -> return WebFactory.createService(BrowseModel::class.java, context).deleteMediaListEntry(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_DELETE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).deleteReview(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_RATE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).rateReview(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_SAVE_FEED_REPLY -> return WebFactory.createService(FeedModel::class.java, context).saveActivityReply(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_SAVE_MEDIA_LIST -> return WebFactory.createService(BrowseModel::class.java, context).saveMediaListEntry(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_SAVE_MESSAGE_FEED -> return WebFactory.createService(FeedModel::class.java, context).saveMessageActivity(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_SAVE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).saveReview(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_SAVE_TEXT_FEED -> return WebFactory.createService(FeedModel::class.java, context).saveTextActivity(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_TOGGLE_FAVOURITE -> return WebFactory.createService(BaseModel::class.java, context).toggleFavourite(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_TOGGLE_FOLLOW -> return WebFactory.createService(UserModel::class.java, context).toggleFollow(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_TOGGLE_LIKE -> return WebFactory.createService(BaseModel::class.java, context).toggleLike(param.getParcelable(arg_graph_params)) as Call<T>
                MUT_UPDATE_MEDIA_LISTS -> return WebFactory.createService(BrowseModel::class.java, context).updateMediaListEntries(param.getParcelable(arg_graph_params)) as Call<T>


                STAFF_BASE_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffBase(param.getParcelable(arg_graph_params)) as Call<T>
                STAFF_MEDIA_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffMedia(param.getParcelable(arg_graph_params)) as Call<T>
                STAFF_OVERVIEW_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffOverview(param.getParcelable(arg_graph_params)) as Call<T>
                STAFF_ROLES_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffRoles(param.getParcelable(arg_graph_params)) as Call<T>
                STAFF_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getStaffSearch(param.getParcelable(arg_graph_params)) as Call<T>


                STUDIO_BASE_REQ -> return WebFactory.createService(StudioModel::class.java, context).getStudioBase(param.getParcelable(arg_graph_params)) as Call<T>
                STUDIO_MEDIA_REQ -> return WebFactory.createService(StudioModel::class.java, context).getStudioMedia(param.getParcelable(arg_graph_params)) as Call<T>
                STUDIO_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getStudioSearch(param.getParcelable(arg_graph_params)) as Call<T>


                USER_ANIME_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getAnimeFavourites(param.getParcelable(arg_graph_params)) as Call<T>
                USER_BASE_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserBase(param.getParcelable(arg_graph_params)) as Call<T>
                USER_CHARACTER_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getCharacterFavourites(param.getParcelable(arg_graph_params)) as Call<T>
                USER_CURRENT_REQ -> return WebFactory.createService(UserModel::class.java, context).getCurrentUser(param.getParcelable(arg_graph_params)) as Call<T>
                USER_FAVOURITES_COUNT_REQ -> return WebFactory.createService(UserModel::class.java, context).getFavouritesCount(param.getParcelable(arg_graph_params)) as Call<T>
                USER_FOLLOWERS_REQ -> return WebFactory.createService(UserModel::class.java, context).getFollowers(param.getParcelable(arg_graph_params)) as Call<T>
                USER_FOLLOWING_REQ -> return WebFactory.createService(UserModel::class.java, context).getFollowing(param.getParcelable(arg_graph_params)) as Call<T>
                USER_MANGA_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getMangaFavourites(param.getParcelable(arg_graph_params)) as Call<T>
                USER_OVERVIEW_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserOverview(param.getParcelable(arg_graph_params)) as Call<T>
                USER_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getUserSearch(param.getParcelable(arg_graph_params)) as Call<T>
                USER_STAFF_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getStaffFavourites(param.getParcelable(arg_graph_params)) as Call<T>
                USER_STATS_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserStats(param.getParcelable(arg_graph_params)) as Call<T>
                USER_STUDIO_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getStudioFavourites(param.getParcelable(arg_graph_params)) as Call<T>
                USER_NOTIFICATION_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserNotifications(param.getParcelable(arg_graph_params)) as Call<T>
            }
        }
        return null
    }

    override fun onPostExecute(call: Call<T>?) {
        if (!isCancelled && call != null)
            callback?.also {
                call.enqueue(it)
            }
    }

    override fun onCancelled(call: Call<T>?) {
        call?.cancel()
        callback = null
        super.onCancelled(call)
    }
}
