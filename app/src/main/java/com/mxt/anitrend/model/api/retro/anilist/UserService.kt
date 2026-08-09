package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.AnimeFavouritesVariables
import com.mxt.anitrend.graphql.generated.UserStatsData
import com.mxt.anitrend.graphql.generated.CharacterFavouritesVariables
import com.mxt.anitrend.graphql.generated.CurrentUserVariables
import com.mxt.anitrend.graphql.generated.MangaFavouritesVariables
import com.mxt.anitrend.graphql.generated.StaffFavouritesVariables
import com.mxt.anitrend.graphql.generated.StudioFavouritesVariables
import com.mxt.anitrend.graphql.generated.ToggleFollowVariables
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UpdateUserVariables
import com.mxt.anitrend.graphql.generated.UserBaseVariables
import com.mxt.anitrend.graphql.generated.UserFavouriteCountVariables
import com.mxt.anitrend.graphql.generated.UserFollowersVariables
import com.mxt.anitrend.graphql.generated.UserFollowingVariables
import com.mxt.anitrend.graphql.generated.UserNotificationsData
import com.mxt.anitrend.graphql.generated.UserNotificationsVariables
import com.mxt.anitrend.graphql.generated.UserOverviewVariables
import com.mxt.anitrend.graphql.generated.UserStatsVariables
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
    ): Response<GraphContainer<UserNotificationsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCurrentUser(
        @Body request: GraphQLOperationRequest<CurrentUserVariables>,
    ): Response<AniListContainer<User>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserBase(
        @Body request: GraphQLOperationRequest<UserBaseVariables>,
    ): Response<AniListContainer<UserBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserOverview(
        @Body request: GraphQLOperationRequest<UserOverviewVariables>,
    ): Response<AniListContainer<User>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserStats(
        @Body request: GraphQLOperationRequest<UserStatsVariables>,
    ): Response<GraphContainer<UserStatsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFollowers(
        @Body request: GraphQLOperationRequest<UserFollowersVariables>,
    ): Response<AniListContainer<PageContainer<UserBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFollowing(
        @Body request: GraphQLOperationRequest<UserFollowingVariables>,
    ): Response<AniListContainer<PageContainer<UserBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFavouritesCount(
        @Body request: GraphQLOperationRequest<UserFavouriteCountVariables>,
    ): Response<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getAnimeFavourites(
        @Body request: GraphQLOperationRequest<AnimeFavouritesVariables>,
    ): Response<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMangaFavourites(
        @Body request: GraphQLOperationRequest<MangaFavouritesVariables>,
    ): Response<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterFavourites(
        @Body request: GraphQLOperationRequest<CharacterFavouritesVariables>,
    ): Response<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffFavourites(
        @Body request: GraphQLOperationRequest<StaffFavouritesVariables>,
    ): Response<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStudioFavourites(
        @Body request: GraphQLOperationRequest<StudioFavouritesVariables>,
    ): Response<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun toggleFollow(
        @Body request: GraphQLOperationRequest<ToggleFollowVariables>,
    ): Response<AniListContainer<UserBase>>

    /** Sends a typed UpdateUser mutation request to AniList. */
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun updateUser(
        @Body request: GraphQLOperationRequest<UpdateUserVariables>,
    ): Response<GraphContainer<UpdateUserData>>
}
