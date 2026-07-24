package com.mxt.anitrend.base.custom.async

import android.content.Context
import android.os.Bundle
import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.AnimeFavourites
import com.mxt.anitrend.graphql.generated.CharacterActors
import com.mxt.anitrend.graphql.generated.CharacterBase
import com.mxt.anitrend.graphql.generated.CharacterFavourites
import com.mxt.anitrend.graphql.generated.CharacterMedia
import com.mxt.anitrend.graphql.generated.CharacterOverview
import com.mxt.anitrend.graphql.generated.CharacterSearch
import com.mxt.anitrend.graphql.generated.CharacterSort
import com.mxt.anitrend.graphql.generated.CurrentUser
import com.mxt.anitrend.graphql.generated.DeleteActivity
import com.mxt.anitrend.graphql.generated.DeleteActivityReply
import com.mxt.anitrend.graphql.generated.DeleteMediaListEntry
import com.mxt.anitrend.graphql.generated.DeleteReview
import com.mxt.anitrend.graphql.generated.FeedList
import com.mxt.anitrend.graphql.generated.FeedListReply
import com.mxt.anitrend.graphql.generated.FeedMessage
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.GenreCollection
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.MangaFavourites
import com.mxt.anitrend.graphql.generated.MediaBase
import com.mxt.anitrend.graphql.generated.MediaBrowse
import com.mxt.anitrend.graphql.generated.MediaCharacters
import com.mxt.anitrend.graphql.generated.MediaEpisodes
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaList
import com.mxt.anitrend.graphql.generated.MediaListBrowse
import com.mxt.anitrend.graphql.generated.MediaListCollection
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaOverview
import com.mxt.anitrend.graphql.generated.MediaRelations
import com.mxt.anitrend.graphql.generated.MediaSearch
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSocial
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStaff
import com.mxt.anitrend.graphql.generated.MediaStats
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaTagCollection
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.MediaWithList
import com.mxt.anitrend.graphql.generated.NotificationType
import com.mxt.anitrend.graphql.generated.RateReview
import com.mxt.anitrend.graphql.generated.RecommendationMedia
import com.mxt.anitrend.graphql.generated.RecommendationMediaList
import com.mxt.anitrend.graphql.generated.RecommendationSort
import com.mxt.anitrend.graphql.generated.ReviewBrowse
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.graphql.generated.SaveActivityReply
import com.mxt.anitrend.graphql.generated.SaveMediaListEntry
import com.mxt.anitrend.graphql.generated.SaveMessageActivity
import com.mxt.anitrend.graphql.generated.SaveReview
import com.mxt.anitrend.graphql.generated.SaveTextActivity
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.StaffBase
import com.mxt.anitrend.graphql.generated.StaffCharacters
import com.mxt.anitrend.graphql.generated.StaffFavourites
import com.mxt.anitrend.graphql.generated.StaffMedia
import com.mxt.anitrend.graphql.generated.StaffOverview
import com.mxt.anitrend.graphql.generated.StaffRoles
import com.mxt.anitrend.graphql.generated.StaffSearch
import com.mxt.anitrend.graphql.generated.StaffSort
import com.mxt.anitrend.graphql.generated.StudioBase
import com.mxt.anitrend.graphql.generated.StudioFavourites
import com.mxt.anitrend.graphql.generated.StudioMedia
import com.mxt.anitrend.graphql.generated.StudioSearch
import com.mxt.anitrend.graphql.generated.StudioSort
import com.mxt.anitrend.graphql.generated.ToggleFavourite
import com.mxt.anitrend.graphql.generated.ToggleFollow
import com.mxt.anitrend.graphql.generated.ToggleLike
import com.mxt.anitrend.graphql.generated.UpdateMediaListEntries
import com.mxt.anitrend.graphql.generated.UserBase
import com.mxt.anitrend.graphql.generated.UserFavouriteCount
import com.mxt.anitrend.graphql.generated.UserFollowers
import com.mxt.anitrend.graphql.generated.UserFollowing
import com.mxt.anitrend.graphql.generated.UserNotifications
import com.mxt.anitrend.graphql.generated.UserOverview
import com.mxt.anitrend.graphql.generated.UserSearch
import com.mxt.anitrend.graphql.generated.UserSort
import com.mxt.anitrend.graphql.generated.UserStats
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.*
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.util.KeyUtil
import retrofit2.Call
import retrofit2.Callback
import timber.log.Timber

/**
 * Created by max on 2017/09/16.
 * Handles all service creation for Retrofit Endpoints on a background task,
 * which allows us to perform heavy operations such as token refreshing on demand
 *
 * @deprecated Use repository classes (e.g. [com.mxt.anitrend.repository.UserRepository],
 * [com.mxt.anitrend.repository.MediaRepository], etc.) directly via Koin injection.
 * The repository layer handles GraphQL request building and network calls without
 * the overhead of this central dispatcher. See AGENTS.md for the ViewModel-first
 * architecture migration direction.
 */
@Deprecated(
    message = "Use repository classes directly via Koin injection. " +
        "See UserRepository, MediaRepository, BrowseRepository, etc.",
    level = DeprecationLevel.WARNING,
)
@Suppress("UNCHECKED_CAST")
class RequestHandler<T>(
    private val param: Bundle,
    private var callback: Callback<T>?,
    @field:KeyUtil.RequestType
    private val requestType: Int,
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
            when (requestType) {
                GENRE_COLLECTION_REQ -> return WebFactory.createService(BaseModel::class.java, context).getGenres(genreCollectionRequest()) as Call<T>
                MEDIA_TAG_REQ -> return WebFactory.createService(BaseModel::class.java, context).getTags(mediaTagCollectionRequest()) as Call<T>

                EPISODE_FEED_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getRSS(param.getString(arg_search)) as Call<T>
                EPISODE_LATEST_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).latestFeed as Call<T>
                EPISODE_POPULAR_REQ -> return WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).popularFeed as Call<T>

                GIPHY_SEARCH_REQ -> return WebFactory.createGiphyService(context).findGif(
                    BuildConfig.GIPHY_KEY,
                    param.getString(arg_search),
                    PAGING_LIMIT,
                    param.getInt(arg_page_offset),
                    "PG",
                    "en",
                ) as Call<T>
                GIPHY_TRENDING_REQ -> return WebFactory.createGiphyService(context).getTrending(BuildConfig.GIPHY_KEY, PAGING_LIMIT, param.getInt(arg_page_offset), "PG") as Call<T>

                UPDATE_CHECKER_REQ -> return WebFactory.createRepositoryService().checkVersion(param.getString(arg_branch_name)) as Call<T>

                MEDIA_LIST_COLLECTION_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaListCollection(mediaListCollectionRequest()) as Call<T>
                MEDIA_BROWSE_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaBrowse(mediaBrowseRequest()) as Call<T>
                MEDIA_LIST_BROWSE_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaListBrowse(mediaListBrowseRequest()) as Call<T>
                MEDIA_LIST_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaList(mediaListRequest()) as Call<T>
                MEDIA_WITH_LIST_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getMediaWithList(mediaWithListRequest()) as Call<T>

                CHARACTER_ACTORS_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterActors(characterActorsRequest()) as Call<T>
                CHARACTER_BASE_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterBase(characterBaseRequest()) as Call<T>
                CHARACTER_MEDIA_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterMedia(characterMediaRequest()) as Call<T>
                CHARACTER_OVERVIEW_REQ -> return WebFactory.createService(CharacterModel::class.java, context).getCharacterOverview(characterOverviewRequest()) as Call<T>
                CHARACTER_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getCharacterSearch(characterSearchRequest()) as Call<T>

                FEED_LIST_REPLY_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedListReply(feedListReplyRequest()) as Call<T>
                FEED_LIST_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedList(feedListRequest()) as Call<T>
                FEED_MESSAGE_REQ -> return WebFactory.createService(FeedModel::class.java, context).getFeedMessage(feedMessageRequest()) as Call<T>

                MEDIA_BASE_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaBase(mediaBaseRequest()) as Call<T>
                MEDIA_CHARACTERS_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaCharacters(mediaCharactersRequest()) as Call<T>
                MEDIA_EPISODES_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaEpisodes(mediaEpisodesRequest()) as Call<T>
                MEDIA_OVERVIEW_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaOverview(mediaOverviewRequest()) as Call<T>
                MEDIA_RELATION_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaRelations(mediaRelationsRequest()) as Call<T>
                MEDIA_REVIEWS_REQ -> return WebFactory.createService(BrowseModel::class.java, context).getReviewBrowse(reviewBrowseRequest()) as Call<T>
                MEDIA_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getMediaSearch(mediaSearchRequest()) as Call<T>
                MEDIA_SOCIAL_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaSocial(mediaSocialRequest()) as Call<T>
                MEDIA_STAFF_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaStaff(mediaStaffRequest()) as Call<T>
                MEDIA_STATS_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaStats(mediaStatsRequest()) as Call<T>
                MEDIA_RECOMMENDATION_REQ -> return WebFactory.createService(MediaModel::class.java, context).getMediaRecommendations(mediaRecommendationRequest()) as Call<T>
                RECOMMENDATIONS_REQ -> return WebFactory.createService(RecommendationModel::class.java, context).getRecommendationMediaList(recommendationMediaListRequest()) as Call<T>

                MUT_DELETE_FEED_REPLY -> return WebFactory.createService(FeedModel::class.java, context).deleteActivityReply(deleteActivityReplyRequest()) as Call<T>
                MUT_DELETE_FEED -> return WebFactory.createService(FeedModel::class.java, context).deleteActivity(deleteActivityRequest()) as Call<T>
                MUT_DELETE_MEDIA_LIST -> return WebFactory.createService(BrowseModel::class.java, context).deleteMediaListEntry(deleteMediaListEntryRequest()) as Call<T>
                MUT_DELETE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).deleteReview(deleteReviewRequest()) as Call<T>
                MUT_RATE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).rateReview(rateReviewRequest()) as Call<T>
                MUT_SAVE_FEED_REPLY -> return WebFactory.createService(FeedModel::class.java, context).saveActivityReply(saveActivityReplyRequest()) as Call<T>
                MUT_SAVE_MEDIA_LIST -> return WebFactory.createService(BrowseModel::class.java, context).saveMediaListEntry(saveMediaListEntryRequest()) as Call<T>
                MUT_SAVE_MESSAGE_FEED -> return WebFactory.createService(FeedModel::class.java, context).saveMessageActivity(saveMessageActivityRequest()) as Call<T>
                MUT_SAVE_REVIEW -> return WebFactory.createService(BrowseModel::class.java, context).saveReview(saveReviewRequest()) as Call<T>
                MUT_SAVE_TEXT_FEED -> return WebFactory.createService(FeedModel::class.java, context).saveTextActivity(saveTextActivityRequest()) as Call<T>
                MUT_TOGGLE_FAVOURITE -> return WebFactory.createService(BaseModel::class.java, context).toggleFavourite(toggleFavouriteRequest()) as Call<T>
                MUT_TOGGLE_FOLLOW -> return WebFactory.createService(UserModel::class.java, context).toggleFollow(toggleFollowRequest()) as Call<T>
                MUT_TOGGLE_LIKE -> return WebFactory.createService(BaseModel::class.java, context).toggleLike(toggleLikeRequest()) as Call<T>
                MUT_UPDATE_MEDIA_LISTS -> return WebFactory.createService(BrowseModel::class.java, context).updateMediaListEntries(updateMediaListEntriesRequest()) as Call<T>

                STAFF_BASE_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffBase(staffBaseRequest()) as Call<T>
                STAFF_MEDIA_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffMedia(staffMediaRequest()) as Call<T>
                STAFF_OVERVIEW_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffOverview(staffOverviewRequest()) as Call<T>
                STAFF_CHARACTERS_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffCharacters(staffCharactersRequest()) as Call<T>
                STAFF_ROLES_REQ -> return WebFactory.createService(StaffModel::class.java, context).getStaffRoles(staffRolesRequest()) as Call<T>
                STAFF_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getStaffSearch(staffSearchRequest()) as Call<T>

                STUDIO_BASE_REQ -> return WebFactory.createService(StudioModel::class.java, context).getStudioBase(studioBaseRequest()) as Call<T>
                STUDIO_MEDIA_REQ -> return WebFactory.createService(StudioModel::class.java, context).getStudioMedia(studioMediaRequest()) as Call<T>
                STUDIO_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getStudioSearch(studioSearchRequest()) as Call<T>

                // User requests
                USER_ANIME_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getAnimeFavourites(animeFavouritesRequest()) as Call<T>
                USER_BASE_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserBase(userBaseRequest()) as Call<T>
                USER_CHARACTER_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getCharacterFavourites(characterFavouritesRequest()) as Call<T>
                USER_CURRENT_REQ -> return WebFactory.createService(UserModel::class.java, context).getCurrentUser(currentUserRequest()) as Call<T>
                USER_FAVOURITES_COUNT_REQ -> return WebFactory.createService(UserModel::class.java, context).getFavouritesCount(userFavouriteCountRequest()) as Call<T>
                USER_FOLLOWERS_REQ -> return WebFactory.createService(UserModel::class.java, context).getFollowers(userFollowersRequest()) as Call<T>
                USER_FOLLOWING_REQ -> return WebFactory.createService(UserModel::class.java, context).getFollowing(userFollowingRequest()) as Call<T>
                USER_MANGA_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getMangaFavourites(mangaFavouritesRequest()) as Call<T>
                USER_OVERVIEW_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserOverview(userOverviewRequest()) as Call<T>
                USER_SEARCH_REQ -> return WebFactory.createService(SearchModel::class.java, context).getUserSearch(userSearchRequest()) as Call<T>
                USER_STAFF_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getStaffFavourites(staffFavouritesRequest()) as Call<T>
                USER_STATS_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserStats(userStatsRequest()) as Call<T>
                USER_STUDIO_FAVOURITES_REQ -> return WebFactory.createService(UserModel::class.java, context).getStudioFavourites(studioFavouritesRequest()) as Call<T>
                USER_NOTIFICATION_REQ -> return WebFactory.createService(UserModel::class.java, context).getUserNotifications(userNotificationsRequest()) as Call<T>
                else -> error("Unsupported request type: $requestType")
            }
        }
    }

    private fun genreCollectionRequest(): GraphQLRequest<EmptyGraphQLVariables> = GraphQLRequest(
        query = GenreCollection.document,
        operationName = GenreCollection.name,
    )

    private fun mediaTagCollectionRequest(): GraphQLRequest<EmptyGraphQLVariables> = GraphQLRequest(
        query = MediaTagCollection.document,
        operationName = MediaTagCollection.name,
    )

    private fun recommendationMediaListRequest() = RecommendationMediaList.request(
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun feedListRequest() = FeedList.request(
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        id = intValue(KeyUtil.arg_id),
        isFollowing = boolValue(KeyUtil.arg_isFollowing),
        userId = intValue(KeyUtil.arg_userId),
        type = enumValue<ActivityType>(KeyUtil.arg_type),
        isMixed = boolValue(KeyUtil.arg_isMixed),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun feedListReplyRequest() = FeedListReply.request(
        id = intValue(KeyUtil.arg_id),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun feedMessageRequest() = FeedMessage.request(
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        messengerId = intValue(KeyUtil.arg_messengerId),
        userId = intValue(KeyUtil.arg_userId),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun saveTextActivityRequest() = SaveTextActivity.request(
        id = intValue(KeyUtil.arg_id),
        text = stringValue(KeyUtil.arg_text),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun saveMessageActivityRequest() = SaveMessageActivity.request(
        id = intValue(KeyUtil.arg_id),
        message = stringValue(KeyUtil.arg_message),
        recipientId = intValue(KeyUtil.arg_recipientId),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun saveActivityReplyRequest() = SaveActivityReply.request(
        id = intValue(KeyUtil.arg_id),
        activityId = intValue(KeyUtil.arg_activityId),
        text = stringValue(KeyUtil.arg_text),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun deleteActivityRequest() = DeleteActivity.request(
        id = intValue(KeyUtil.arg_id),
    )

    private fun deleteActivityReplyRequest() = DeleteActivityReply.request(
        id = intValue(KeyUtil.arg_id),
    )

    private fun mediaListCollectionRequest() = MediaListCollection.request(
        userId = intValue(KeyUtil.arg_userId),
        userName = stringValue(KeyUtil.arg_userName),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        forceSingleCompletedList = boolValue(KeyUtil.arg_forceSingleCompletedList),
        sort = enumListValue<MediaListSort>(KeyUtil.arg_sort),
        statusIn = enumListValue<MediaListStatus>(KeyUtil.arg_statusIn),
        scoreFormat = enumValue<ScoreFormat>(KeyUtil.arg_scoreFormat) ?: ScoreFormat.POINT_100,
    )

    private fun mediaBrowseRequest() = MediaBrowse.request(
        id = intValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        seasonYear = intValue(KeyUtil.arg_seasonYear),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        format = enumValue<MediaFormat>(KeyUtil.arg_format),
        startDateLike = stringValue(KeyUtil.arg_startDateLike),
        endDateLike = stringValue(KeyUtil.arg_endDateLike),
        season = enumValue<MediaSeason>(KeyUtil.arg_season),
        genres = stringListValue(KeyUtil.arg_genres),
        genresExclude = stringListValue(KeyUtil.arg_genresExclude),
        isAdult = boolValue(KeyUtil.arg_isAdult),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort),
        onList = boolValue(KeyUtil.arg_onList),
        status = enumValue<MediaStatus>(KeyUtil.arg_status),
        tags = stringListValue(KeyUtil.arg_tags),
        tagsExclude = stringListValue(KeyUtil.arg_tagsExclude),
    )

    private fun reviewBrowseRequest() = ReviewBrowse.request(
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        mediaId = intValue(KeyUtil.arg_mediaId),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        sort = enumListValue<ReviewSort>(KeyUtil.arg_sort)
            ?: listOf(ReviewSort.CREATED_AT_DESC),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun mediaListBrowseRequest() = MediaListBrowse.request(
        id = intValue(KeyUtil.arg_id),
        userId = intValue(KeyUtil.arg_userId),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        status = enumValue<MediaListStatus>(KeyUtil.arg_listStatus),
        sort = enumListValue<MediaListSort>(KeyUtil.arg_sort),
        scoreFormat = enumValue<ScoreFormat>(KeyUtil.arg_scoreFormat) ?: ScoreFormat.POINT_100,
    )

    private fun mediaListRequest() = MediaList.request(
        id = intValue(KeyUtil.arg_id),
        mediaId = intValue(KeyUtil.arg_mediaId),
        userName = stringValue(KeyUtil.arg_userName),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        status = enumValue<MediaListStatus>(KeyUtil.arg_listStatus),
        sort = enumListValue<MediaListSort>(KeyUtil.arg_sort),
        scoreFormat = enumValue<ScoreFormat>(KeyUtil.arg_scoreFormat) ?: ScoreFormat.POINT_100,
    )

    private fun mediaWithListRequest() = MediaWithList.request(
        id = intValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        onList = boolValue(KeyUtil.arg_onList),
        scoreFormat = enumValue<ScoreFormat>(KeyUtil.arg_scoreFormat) ?: ScoreFormat.POINT_100,
    )

    private fun deleteMediaListEntryRequest() = DeleteMediaListEntry.request(
        id = intValue(KeyUtil.arg_id),
    )

    private fun deleteReviewRequest() = DeleteReview.request(
        id = intValue(KeyUtil.arg_id),
    )

    private fun saveMediaListEntryRequest() = SaveMediaListEntry.request(
        id = intValue(KeyUtil.arg_id),
        mediaId = intValue(KeyUtil.arg_mediaId),
        status = enumValue<MediaListStatus>(KeyUtil.arg_listStatus),
        scoreRaw = intValue(KeyUtil.arg_listScore_raw),
        score = doubleValue(KeyUtil.arg_listScore),
        progress = intValue(KeyUtil.arg_listProgress),
        progressVolumes = intValue(KeyUtil.arg_listProgressVolumes),
        repeat = intValue(KeyUtil.arg_listRepeat),
        priority = intValue(KeyUtil.arg_listPriority),
        private = boolValue(KeyUtil.arg_listPrivate) ?: false,
        hiddenFromStatusLists = boolValue(KeyUtil.arg_listHiddenFromStatusLists) ?: false,
        customLists = stringListValue(KeyUtil.arg_listCustom),
        advancedScores = doubleListValue(KeyUtil.arg_listAdvancedScore),
        notes = stringValue(KeyUtil.arg_listNotes),
        scoreFormat = enumValue<ScoreFormat>(KeyUtil.arg_scoreFormat) ?: ScoreFormat.POINT_100,
        startedAt = fuzzyDateInputValue(KeyUtil.arg_startedAt),
        completedAt = fuzzyDateInputValue(KeyUtil.arg_completedAt),
    )

    private fun updateMediaListEntriesRequest() = UpdateMediaListEntries.request(
        ids = intListValue(KeyUtil.arg_id),
        scoreFormat = enumValue<ScoreFormat>(KeyUtil.arg_scoreFormat) ?: ScoreFormat.POINT_100,
    )

    private fun rateReviewRequest() = RateReview.request(
        id = intValue(KeyUtil.arg_id),
        rating = enumValue<ReviewRating>(KeyUtil.arg_rating),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun saveReviewRequest() = SaveReview.request(
        id = intValue(KeyUtil.arg_id),
        mediaId = intValue(KeyUtil.arg_mediaId),
        body = stringValue(KeyUtil.arg_text),
        summary = stringValue(KeyUtil.arg_summary),
        score = intValue(KeyUtil.arg_score),
        private = boolValue(KeyUtil.arg_listPrivate),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun mediaSearchRequest() = MediaSearch.request(
        id = intValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        search = stringValue(KeyUtil.arg_search),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort) ?: listOf(MediaSort.SEARCH_MATCH),
    )

    private fun mediaBaseRequest() = MediaBase.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
    )

    private fun mediaOverviewRequest() = MediaOverview.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun mediaRelationsRequest() = MediaRelations.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
    )

    private fun mediaStatsRequest() = MediaStats.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
    )

    private fun mediaEpisodesRequest() = MediaEpisodes.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
    )

    private fun mediaCharactersRequest() = MediaCharacters.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<CharacterSort>(KeyUtil.arg_sort)
            ?: listOf(CharacterSort.ROLE, CharacterSort.RELEVANCE, CharacterSort.ID),
    )

    private fun mediaStaffRequest() = MediaStaff.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        sort = enumListValue<StaffSort>(KeyUtil.arg_sort)
            ?: listOf(StaffSort.RELEVANCE, StaffSort.ID),
        isAdult = boolValue(KeyUtil.arg_isAdult),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun mediaSocialRequest() = MediaSocial.request(
        mediaId = requiredIntValue(KeyUtil.arg_mediaId),
        isFollowing = boolValue(KeyUtil.arg_isFollowing) ?: true,
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun mediaRecommendationRequest() = RecommendationMedia.request(
        id = requiredIntValue(KeyUtil.arg_id),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
        isAdult = boolValue(KeyUtil.arg_isAdult),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<RecommendationSort>(KeyUtil.arg_sort)
            ?: listOf(RecommendationSort.RATING_DESC, RecommendationSort.ID),
    )

    private fun characterBaseRequest() = CharacterBase.request(
        id = requiredIntValue(KeyUtil.arg_id),
    )

    private fun characterOverviewRequest() = CharacterOverview.request(
        id = requiredIntValue(KeyUtil.arg_id),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun characterMediaRequest() = CharacterMedia.request(
        id = requiredIntValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort) ?: listOf(MediaSort.FORMAT),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
    )

    private fun characterActorsRequest() = CharacterActors.request(
        id = requiredIntValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<StaffSort>(KeyUtil.arg_sort)
            ?: listOf(StaffSort.LANGUAGE_DESC, StaffSort.ROLE_DESC),
    )

    private fun studioBaseRequest() = StudioBase.request(
        id = requiredIntValue(KeyUtil.arg_id),
    )

    private fun studioMediaRequest() = StudioMedia.request(
        id = requiredIntValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort) ?: listOf(MediaSort.POPULARITY),
    )

    private fun studioSearchRequest() = StudioSearch.request(
        id = intValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        search = stringValue(KeyUtil.arg_search),
        sort = enumListValue<StudioSort>(KeyUtil.arg_sort) ?: listOf(StudioSort.SEARCH_MATCH),
    )

    private fun staffSearchRequest() = StaffSearch.request(
        id = intValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        search = stringValue(KeyUtil.arg_search),
        sort = enumListValue<StaffSort>(KeyUtil.arg_sort) ?: listOf(StaffSort.SEARCH_MATCH),
    )

    private fun staffBaseRequest() = StaffBase.request(
        id = requiredIntValue(KeyUtil.arg_id),
    )

    private fun staffOverviewRequest() = StaffOverview.request(
        id = requiredIntValue(KeyUtil.arg_id),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun staffCharactersRequest() = StaffCharacters.request(
        id = requiredIntValue(KeyUtil.arg_id),
        onList = boolValue(KeyUtil.arg_onList),
        page = intValue(KeyUtil.arg_page),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort) ?: listOf(MediaSort.START_DATE_DESC),
    )

    private fun staffMediaRequest() = StaffMedia.request(
        id = requiredIntValue(KeyUtil.arg_id),
        onList = boolValue(KeyUtil.arg_onList),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort) ?: listOf(MediaSort.FORMAT),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
    )

    private fun staffRolesRequest() = StaffRoles.request(
        id = requiredIntValue(KeyUtil.arg_id),
        onList = boolValue(KeyUtil.arg_onList),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<MediaSort>(KeyUtil.arg_sort) ?: listOf(MediaSort.POPULARITY),
        type = enumValue<MediaType>(KeyUtil.arg_mediaType),
    )

    private fun characterSearchRequest() = CharacterSearch.request(
        id = intValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        search = stringValue(KeyUtil.arg_search),
        sort = enumListValue<CharacterSort>(KeyUtil.arg_sort) ?: listOf(CharacterSort.SEARCH_MATCH),
    )

    private fun userSearchRequest() = UserSearch.request(
        id = intValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        search = stringValue(KeyUtil.arg_search),
        sort = enumListValue<UserSort>(KeyUtil.arg_sort) ?: listOf(UserSort.SEARCH_MATCH),
    )

    // User request builders
    private fun userNotificationsRequest() = UserNotifications.request(
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        type = enumValue<NotificationType>(KeyUtil.arg_type),
        resetNotificationCount = boolValue(KeyUtil.arg_resetNotificationCount) ?: false,
    )

    private fun currentUserRequest() = CurrentUser.request(
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun userBaseRequest() = UserBase.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
    )

    private fun userOverviewRequest() = UserOverview.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        asHtml = boolValue(KeyUtil.arg_asHtml) ?: false,
    )

    private fun userStatsRequest() = UserStats.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
    )

    private fun userFollowersRequest() = UserFollowers.request(
        id = requiredIntValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<UserSort>(KeyUtil.arg_sort) ?: listOf(UserSort.USERNAME),
    )

    private fun userFollowingRequest() = UserFollowing.request(
        id = requiredIntValue(KeyUtil.arg_id),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
        sort = enumListValue<UserSort>(KeyUtil.arg_sort) ?: listOf(UserSort.USERNAME),
    )

    private fun userFavouriteCountRequest() = UserFavouriteCount.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun animeFavouritesRequest() = AnimeFavourites.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun mangaFavouritesRequest() = MangaFavourites.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun characterFavouritesRequest() = CharacterFavourites.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun staffFavouritesRequest() = StaffFavourites.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun studioFavouritesRequest() = StudioFavourites.request(
        id = intValue(KeyUtil.arg_id),
        userName = stringValue(KeyUtil.arg_userName),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun toggleFollowRequest() = ToggleFollow.request(
        userId = intValue(KeyUtil.arg_userId),
    )

    private fun toggleLikeRequest() = ToggleLike.request(
        id = intValue(KeyUtil.arg_id),
        type = enumValue<LikeableType>(KeyUtil.arg_type),
    )

    private fun toggleFavouriteRequest() = ToggleFavourite.request(
        animeId = intValue(KeyUtil.arg_animeId),
        mangaId = intValue(KeyUtil.arg_mangaId),
        characterId = intValue(KeyUtil.arg_characterId),
        staffId = intValue(KeyUtil.arg_staffId),
        studioId = intValue(KeyUtil.arg_studioId),
        page = intValue(KeyUtil.arg_page),
        perPage = intValue(KeyUtil.arg_page_limit),
    )

    private fun intValue(
        key: String,
    ): Int? = value(key)?.asInt()

    private fun requiredIntValue(
        key: String,
    ): Int = requireNotNull(intValue(key)) {
        "Missing required integer for '$key'"
    }

    private fun stringValue(
        key: String,
    ): String? = value(key)?.toString()

    private fun boolValue(
        key: String,
    ): Boolean? = when (val rawValue = value(key)) {
        is Boolean -> rawValue
        is String -> rawValue.toBooleanStrictOrNull()
        else -> null
    }

    private fun doubleValue(
        key: String,
    ): Double? = when (val rawValue = value(key)) {
        is Number -> rawValue.toDouble()
        is String -> rawValue.toDoubleOrNull()
        else -> null
    }

    @Suppress("DEPRECATION")
    private fun value(key: String): Any? = if (param.containsKey(key)) param.get(key) else null

    private fun Any?.asInt(): Int? = when (this) {
        is Number -> toInt()
        is String -> toIntOrNull()
        else -> null
    }

    private fun stringListValue(
        key: String,
    ): List<String?>? = when (val rawValue = value(key)) {
        is Iterable<*> -> rawValue.map { it?.toString() }
            .takeIf { it.isNotEmpty() }
        is Array<*> -> rawValue.map { it?.toString() }
            .takeIf { it.isNotEmpty() }
        else -> rawValue?.toString()?.let(::listOf)
    }

    private fun intListValue(
        key: String,
    ): List<Int?>? = when (val rawValue = value(key)) {
        is Iterable<*> -> rawValue.mapNotNull { it.asInt() }
            .takeIf { it.isNotEmpty() }
        is IntArray -> rawValue.toList().takeIf { it.isNotEmpty() }
        is LongArray -> rawValue.map { it.toInt() }.takeIf { it.isNotEmpty() }
        else -> rawValue.asInt()?.let(::listOf)
    }

    private fun doubleListValue(
        key: String,
    ): List<Double?>? = when (val rawValue = value(key)) {
        is Iterable<*> -> rawValue.mapNotNull { valueItem ->
            when (valueItem) {
                is Number -> valueItem.toDouble()
                is String -> valueItem.toDoubleOrNull()
                else -> null
            }
        }.takeIf { it.isNotEmpty() }
        is DoubleArray -> rawValue.toList().takeIf { it.isNotEmpty() }
        is FloatArray -> rawValue.map { it.toDouble() }.takeIf { it.isNotEmpty() }
        else -> doubleValue(key)?.let(::listOf)
    }

    private fun fuzzyDateInputValue(
        key: String,
    ): FuzzyDateInput? = when (val rawValue = value(key)) {
        is FuzzyDateInput -> rawValue
        is FuzzyDate -> rawValue.takeIf { it.isValidDate }?.let { date ->
            FuzzyDateInput(
                day = date.day,
                month = date.month,
                year = date.year,
            )
        }
        else -> null
    }

    private inline fun <reified T : Enum<T>> enumValue(
        key: String,
    ): T? = enumValue(value(key))

    private inline fun <reified T : Enum<T>> enumListValue(
        key: String,
    ): List<T?>? = when (val rawValue = value(key)) {
        is Iterable<*> -> rawValue.mapNotNull { enumValue<T>(it) }
            .takeIf { it.isNotEmpty() }
        else -> enumValue<T>(rawValue)?.let(::listOf)
    }

    private inline fun <reified T : Enum<T>> enumValue(rawValue: Any?): T? {
        val enumName = rawValue?.toString() ?: return null
        return runCatching { enumValueOf<T>(enumName) }
            .onFailure { Timber.tag("RequestHandler").w(it, "Unknown %s value: %s", T::class.java.simpleName, enumName) }
            .getOrNull()
    }
}
