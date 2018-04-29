package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.Notification;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.UserStats;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * user models
 */

public interface UserModel {

    @POST("/")
    @GraphQuery("UserNotifications")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<Notification>>> getUserNotifications(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("CurrentUser")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<User>> getCurrentUser(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserBase")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<UserBase>> getUserBase(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserOverview")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<User>> getUserOverview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserStats")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<UserStats>>> getUserStats(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserFollowers")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<UserBase>>> getFollowers(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserFollowing")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<UserBase>>> getFollowing(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserFavouriteCount")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<Favourite>>> getFavouritesCount(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("AnimeFavourites")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<Favourite>>> getAnimeFavourites(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MangaFavourites")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<Favourite>>> getMangaFavourites(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("CharacterFavourites")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<Favourite>>> getCharacterFavourites(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StaffFavourites")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<Favourite>>> getStaffFavourites(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StudioFavourites")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<Favourite>>> getStudioFavourites(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("ToggleFollow")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<UserBase>> toggleFollow(@Body QueryContainerBuilder request);
}
