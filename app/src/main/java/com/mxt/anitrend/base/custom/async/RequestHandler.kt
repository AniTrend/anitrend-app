package com.mxt.anitrend.base.custom.async

import android.content.Context
import android.os.Bundle
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.*
import com.mxt.anitrend.util.KeyUtil
import io.github.wax911.library.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.Callback
import timber.log.Timber

/**
 * Created by max on 2017/09/16.
 * Handles all service creation for Retrofit Endpoints on a background task,
 * which allows us to perform heavy operations such as token refreshing on demand
 */
@Suppress("UNCHECKED_CAST")
class RequestHandler<T>(
    private val param: Bundle,
    private var callback: Callback<T>?,
    @field:KeyUtil.RequestType
    private val requestType: Int
) {

    private var call: Call<T>? = null

    fun execute(context: Context) {
        val callback = callback ?: return
        call = runCatching { createCall(context) }
            .onFailure { error ->
                Timber.tag("RequestHandler").e(error, "Unable to create call for requestType=%d", requestType)
            }
            .getOrNull()
        call?.enqueue(callback)
    }

    fun cancel() {
        call?.cancel()
        callback = null
    }

    private fun createCall(context: Context): Call<T> {
        with(KeyUtil) {
            val graphParams = param.parcelable<QueryContainerBuilder>(arg_graph_params)
            when (requestType) {
                GENRE_COLLECTION_REQ -> return WebFactory.createService(BaseModel::class.java, context).getGenres(graphParams) as Call<T>
                MEDIA_TAG_REQ -> return WebFactory.createService(BaseModel::class.java, context).getTags(graphParams) as Call<T>


                EPISODE_FEED_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getRSS(param.getString(arg_search)) as Call<T>
                EPISODE_LATEST_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).latestFeed as Call<T>
                EPISODE_POPULAR_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).popularFeed as Call<T>


                GIPHY_SEARCH_REQ -> return WebFactory.createGiphyService(context).findGif(BuildConfig.GIPHY_KEY, param.getString(arg_search),
                        PAGING_LIMIT, param.getInt(arg_page_offset), "PG", "en") as Call<T>
                GIPHY_TRENDING_REQ -> return WebFactory.createGiphyService(context).getTrending(BuildConfig.GIPHY_KEY, PAGING_LIMIT, param.getInt(arg_page_offset), "PG") as Call<T>


                UPDATE_CHECKER_REQ -> return WebFactory.createRepositoryService().checkVersion(param.getString(arg_branch_name)) as Call<T>


                MEDIA_LIST_COLLECTION_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaListCollection(graphParams) as Call<T>
                MEDIA_BROWSE_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaBrowse(graphParams) as Call<T>
                MEDIA_LIST_BROWSE_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaListBrowse(graphParams) as Call<T>
                MEDIA_LIST_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaList(graphParams) as Call<T>
                MEDIA_WITH_LIST_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaWithList(graphParams) as Call<T>


                CHARACTER_ACTORS_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterActors(graphParams) as Call<T>
                CHARACTER_BASE_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterBase(graphParams) as Call<T>
                CHARACTER_MEDIA_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterMedia(graphParams) as Call<T>
                CHARACTER_OVERVIEW_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterOverview(graphParams) as Call<T>
                CHARACTER_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getCharacterSearch(graphParams) as Call<T>


                FEED_LIST_REPLY_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedListReply(graphParams) as Call<T>
                FEED_LIST_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedList(graphParams) as Call<T>
                FEED_MESSAGE_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedMessage(graphParams) as Call<T>


                MEDIA_BASE_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaBase(graphParams) as Call<T>
                MEDIA_CHARACTERS_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaCharacters(graphParams) as Call<T>
                MEDIA_EPISODES_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaEpisodes(graphParams) as Call<T>
                MEDIA_OVERVIEW_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaOverview(graphParams) as Call<T>
                MEDIA_RELATION_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaRelations(graphParams) as Call<T>
                MEDIA_REVIEWS_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getReviewBrowse(graphParams) as Call<T>
                MEDIA_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getMediaSearch(graphParams) as Call<T>
                MEDIA_SOCIAL_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaSocial(graphParams) as Call<T>
                MEDIA_STAFF_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaStaff(graphParams) as Call<T>
                MEDIA_STATS_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaStats(graphParams) as Call<T>
                MEDIA_RECOMMENDATION_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaRecommendations(graphParams) as Call<T>
                /*case KeyUtils.MEDIA_TREND_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaTrends(param.getParcelable(arg_graph_params));*/

                RECOMMENDATIONS_REQ -> return WebFactory.createService(RecommendationModel::class.java, context).getRecommendationMediaList(graphParams) as Call<T>

                MUT_DELETE_FEED_REPLY -> return WebFactory.createService(FeedModel::class.java, context).deleteActivityReply(graphParams) as Call<T>
                MUT_DELETE_FEED -> return WebFactory.createService(FeedModel::class.java, context).deleteActivity(graphParams) as Call<T>
                MUT_DELETE_MEDIA_LIST -> return WebFactory.createService(BrowseModel::class.java, context).deleteMediaListEntry(graphParams) as Call<T>
                MUT_DELETE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).deleteReview(graphParams) as Call<T>
                MUT_RATE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).rateReview(graphParams) as Call<T>
                MUT_SAVE_FEED_REPLY -> return WebFactory.createService(FeedModel::class.java, context).saveActivityReply(graphParams) as Call<T>
                MUT_SAVE_MEDIA_LIST -> return WebFactory.createService(BrowseModel::class.java, context).saveMediaListEntry(graphParams) as Call<T>
                MUT_SAVE_MESSAGE_FEED -> return WebFactory.createService(FeedModel::class.java, context).saveMessageActivity(graphParams) as Call<T>
                MUT_SAVE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).saveReview(graphParams) as Call<T>
                MUT_SAVE_TEXT_FEED -> return WebFactory.createService(FeedModel::class.java, context).saveTextActivity(graphParams) as Call<T>
                MUT_TOGGLE_FAVOURITE -> return WebFactory.createService(BaseModel::class.java, context).toggleFavourite(graphParams) as Call<T>
                MUT_TOGGLE_FOLLOW -> return WebFactory.createService(UserModel::class.java, context).toggleFollow(graphParams) as Call<T>
                MUT_TOGGLE_LIKE -> return WebFactory.createService(BaseModel::class.java, context).toggleLike(graphParams) as Call<T>
                MUT_UPDATE_MEDIA_LISTS -> return WebFactory.createService(BrowseModel::class.java, context).updateMediaListEntries(graphParams) as Call<T>


                STAFF_BASE_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffBase(graphParams) as Call<T>
                STAFF_MEDIA_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffMedia(graphParams) as Call<T>
                STAFF_OVERVIEW_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffOverview(graphParams) as Call<T>
                STAFF_CHARACTERS_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffCharacters(graphParams) as Call<T>
                STAFF_ROLES_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffRoles(graphParams) as Call<T>
                STAFF_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getStaffSearch(graphParams) as Call<T>


                STUDIO_BASE_REQ -> return WebFactory.createService(StudioModel::class.java, context).getStudioBase(graphParams) as Call<T>
                STUDIO_MEDIA_REQ -> return WebFactory.createService(StudioModel::class.java, context).getStudioMedia(graphParams) as Call<T>
                STUDIO_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getStudioSearch(graphParams) as Call<T>


                USER_ANIME_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getAnimeFavourites(graphParams) as Call<T>
                USER_BASE_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserBase(graphParams) as Call<T>
                USER_CHARACTER_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getCharacterFavourites(graphParams) as Call<T>
                USER_CURRENT_REQ -> return WebFactory.createService(UserModel::class.java, context).getCurrentUser(graphParams) as Call<T>
                USER_FAVOURITES_COUNT_REQ -> return WebFactory.createService(UserModel::class.java, context).getFavouritesCount(graphParams) as Call<T>
                USER_FOLLOWERS_REQ -> return WebFactory.createService(UserModel::class.java, context).getFollowers(graphParams) as Call<T>
                USER_FOLLOWING_REQ -> return WebFactory.createService(UserModel::class.java, context).getFollowing(graphParams) as Call<T>
                USER_MANGA_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getMangaFavourites(graphParams) as Call<T>
                USER_OVERVIEW_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserOverview(graphParams) as Call<T>
                USER_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getUserSearch(graphParams) as Call<T>
                USER_STAFF_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getStaffFavourites(graphParams) as Call<T>
                USER_STATS_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserStats(graphParams) as Call<T>
                USER_STUDIO_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getStudioFavourites(graphParams) as Call<T>
                USER_NOTIFICATION_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserNotifications(graphParams) as Call<T>
                else -> error("Unsupported request type: $requestType")
            }
        }
    }
}
