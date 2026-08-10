package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.AnimeFavouritesData
import com.mxt.anitrend.graphql.generated.AnimeFavouritesVariables
import com.mxt.anitrend.graphql.generated.CharacterFavouritesData
import com.mxt.anitrend.graphql.generated.CharacterFavouritesVariables
import com.mxt.anitrend.graphql.generated.CurrentUserData
import com.mxt.anitrend.graphql.generated.CurrentUserVariables
import com.mxt.anitrend.graphql.generated.MangaFavouritesData
import com.mxt.anitrend.graphql.generated.MangaFavouritesVariables
import com.mxt.anitrend.graphql.generated.StaffFavouritesData
import com.mxt.anitrend.graphql.generated.StaffFavouritesVariables
import com.mxt.anitrend.graphql.generated.StudioFavouritesData
import com.mxt.anitrend.graphql.generated.StudioFavouritesVariables
import com.mxt.anitrend.graphql.generated.ToggleFollowData
import com.mxt.anitrend.graphql.generated.ToggleFollowVariables
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UpdateUserVariables
import com.mxt.anitrend.graphql.generated.UserBaseData
import com.mxt.anitrend.graphql.generated.UserBaseVariables
import com.mxt.anitrend.graphql.generated.UserFavouriteCountData
import com.mxt.anitrend.graphql.generated.UserFavouriteCountVariables
import com.mxt.anitrend.graphql.generated.UserFollowersData
import com.mxt.anitrend.graphql.generated.UserFollowersVariables
import com.mxt.anitrend.graphql.generated.UserFollowingData
import com.mxt.anitrend.graphql.generated.UserFollowingVariables
import com.mxt.anitrend.graphql.generated.UserNotificationsData
import com.mxt.anitrend.graphql.generated.UserNotificationsVariables
import com.mxt.anitrend.graphql.generated.UserOverviewData
import com.mxt.anitrend.graphql.generated.UserOverviewVariables
import com.mxt.anitrend.graphql.generated.UserStatsData
import com.mxt.anitrend.graphql.generated.UserStatsVariables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * user models
 */

interface UserService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserNotifications(
        @Body request: GraphQLOperationRequest<UserNotificationsVariables>,
    ): Response<GraphQLResponse<UserNotificationsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCurrentUser(
        @Body request: GraphQLOperationRequest<CurrentUserVariables>,
    ): Response<GraphQLResponse<CurrentUserData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserBase(
        @Body request: GraphQLOperationRequest<UserBaseVariables>,
    ): Response<GraphQLResponse<UserBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserOverview(
        @Body request: GraphQLOperationRequest<UserOverviewVariables>,
    ): Response<GraphQLResponse<UserOverviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserStats(
        @Body request: GraphQLOperationRequest<UserStatsVariables>,
    ): Response<GraphQLResponse<UserStatsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFollowers(
        @Body request: GraphQLOperationRequest<UserFollowersVariables>,
    ): Response<GraphQLResponse<UserFollowersData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFollowing(
        @Body request: GraphQLOperationRequest<UserFollowingVariables>,
    ): Response<GraphQLResponse<UserFollowingData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFavouritesCount(
        @Body request: GraphQLOperationRequest<UserFavouriteCountVariables>,
    ): Response<GraphQLResponse<UserFavouriteCountData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getAnimeFavourites(
        @Body request: GraphQLOperationRequest<AnimeFavouritesVariables>,
    ): Response<GraphQLResponse<AnimeFavouritesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMangaFavourites(
        @Body request: GraphQLOperationRequest<MangaFavouritesVariables>,
    ): Response<GraphQLResponse<MangaFavouritesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterFavourites(
        @Body request: GraphQLOperationRequest<CharacterFavouritesVariables>,
    ): Response<GraphQLResponse<CharacterFavouritesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffFavourites(
        @Body request: GraphQLOperationRequest<StaffFavouritesVariables>,
    ): Response<GraphQLResponse<StaffFavouritesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStudioFavourites(
        @Body request: GraphQLOperationRequest<StudioFavouritesVariables>,
    ): Response<GraphQLResponse<StudioFavouritesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun toggleFollow(
        @Body request: GraphQLOperationRequest<ToggleFollowVariables>,
    ): Response<GraphQLResponse<ToggleFollowData>>

    /** Sends a typed UpdateUser mutation request to AniList. */
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun updateUser(
        @Body request: GraphQLOperationRequest<UpdateUserVariables>,
    ): Response<GraphQLResponse<UpdateUserData>>
}
