package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.UserStats;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

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
    @GraphQuery("CurrentUser")
    @Headers("Content-Type: application/json")
    Call<User> getCurrentUser(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserBase")
    @Headers("Content-Type: application/json")
    Call<UserBase> getUserBase(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserOverview")
    @Headers("Content-Type: application/json")
    Call<User> getUserOverview(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserStats")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<UserStats>> getUserStats(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserFollowers")
    @Headers("Content-Type: application/json")
    Call<PageContainer<UserBase>> getFollowers(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserFollowing")
    @Headers("Content-Type: application/json")
    Call<PageContainer<UserBase>> getFollowing(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserFavouriteCount")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<Favourite>> getFavouritesCount(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("AnimeFavourites")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<Favourite>> getAnimeFavourites(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MangaFavourites")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<Favourite>> getMangaFavourites(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("CharacterFavourites")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<Favourite>> getCharacterFavourites(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StaffFavourites")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<Favourite>> getStaffFavourites(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StudioFavourites")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<Favourite>> getStudioFavourites(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("ToggleFollow")
    @Headers("Content-Type: application/json")
    Call<UserBase> toggleFollow(@Body GraphQueryContainer request);
}
