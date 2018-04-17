package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

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
    Call<GraphContainer<PageContainer<MediaBase>>> getMediaSearch(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StudioSearch")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<StudioBase>>> getStudioSearch(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StaffSearch")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<StaffBase>>> getStaffSearch(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("CharacterSearch")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<CharacterBase>>> getCharacterSearch(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UserSearch")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<UserBase>>> getUserSearch(@Body QueryContainerBuilder request);
}
