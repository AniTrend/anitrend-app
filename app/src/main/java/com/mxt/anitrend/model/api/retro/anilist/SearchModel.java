package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * Search queries
 */

public interface SearchModel {

    @POST("/")
    @GraphQuery("MediaSearch")
    @Headers("Content-Type: application/json")
    Call<PageContainer<MediaBase>> getMediaSearch(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StudioSearch")
    @Headers("Content-Type: application/json")
    Call<PageContainer<StudioBase>> getStudioSearch(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StaffSearch")
    @Headers("Content-Type: application/json")
    Call<PageContainer<StaffBase>> getStaffSearch(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("CharacterSearch")
    @Headers("Content-Type: application/json")
    Call<PageContainer<CharacterBase>> getCharacterSearch(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("UserSearch")
    @Headers("Content-Type: application/json")
    Call<PageContainer<UserBase>> getUserSearch(@Body GraphQueryContainer request);
}
