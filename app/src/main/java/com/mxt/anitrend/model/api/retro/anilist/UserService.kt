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
import retrofit2.Call
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
    fun getUserNotifications(
        @Body request: GraphQLOperationRequest<UserNotificationsVariables>,
    ): Call<GraphContainer<UserNotificationsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCurrentUser(
        @Body request: GraphQLOperationRequest<CurrentUserVariables>,
    ): Call<AniListContainer<User>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getUserBase(
        @Body request: GraphQLOperationRequest<UserBaseVariables>,
    ): Call<AniListContainer<UserBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getUserOverview(
        @Body request: GraphQLOperationRequest<UserOverviewVariables>,
    ): Call<AniListContainer<User>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getUserStats(
        @Body request: GraphQLOperationRequest<UserStatsVariables>,
    ): Call<GraphContainer<UserStatsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getFollowers(
        @Body request: GraphQLOperationRequest<UserFollowersVariables>,
    ): Call<AniListContainer<PageContainer<UserBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getFollowing(
        @Body request: GraphQLOperationRequest<UserFollowingVariables>,
    ): Call<AniListContainer<PageContainer<UserBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getFavouritesCount(
        @Body request: GraphQLOperationRequest<UserFavouriteCountVariables>,
    ): Call<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getAnimeFavourites(
        @Body request: GraphQLOperationRequest<AnimeFavouritesVariables>,
    ): Call<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMangaFavourites(
        @Body request: GraphQLOperationRequest<MangaFavouritesVariables>,
    ): Call<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCharacterFavourites(
        @Body request: GraphQLOperationRequest<CharacterFavouritesVariables>,
    ): Call<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStaffFavourites(
        @Body request: GraphQLOperationRequest<StaffFavouritesVariables>,
    ): Call<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStudioFavourites(
        @Body request: GraphQLOperationRequest<StudioFavouritesVariables>,
    ): Call<AniListContainer<ConnectionContainer<Favourite>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun toggleFollow(
        @Body request: GraphQLOperationRequest<ToggleFollowVariables>,
    ): Call<AniListContainer<UserBase>>

    /** Sends a typed UpdateUser mutation request to AniList. */
    @POST("/")
    @Headers("Content-Type: application/json")
    fun updateUser(
        @Body request: GraphQLOperationRequest<UpdateUserVariables>,
    ): Call<GraphContainer<UpdateUserData>>
}
