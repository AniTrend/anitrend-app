package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 */

public interface BrowseModel {

    @POST("/")
    @GraphQuery("MediaBrowse")
    @Headers("Content-Type: application/json")
    Call<PageContainer<MediaBase>> getMediaBrowse(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("ReviewBrowse")
    @Headers("Content-Type: application/json")
    Call<PageContainer<Review>> getReviewBrowse(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaListBrowse")
    @Headers("Content-Type: application/json")
    Call<PageContainer<MediaList>> getMediaListBrowse(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaList")
    @Headers("Content-Type: application/json")
    Call<MediaList> getMediaList(@Body GraphQueryContainer request);
}
