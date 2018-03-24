package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import java.util.List;

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
    Call<PageContainer<MediaBase>> getMediaBrowse(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("ReviewBrowse")
    @Headers("Content-Type: application/json")
    Call<PageContainer<Review>> getReviewBrowse(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaListBrowse")
    @Headers("Content-Type: application/json")
    Call<PageContainer<MediaList>> getMediaListBrowse(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaList")
    @Headers("Content-Type: application/json")
    Call<MediaList> getMediaList(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("DeleteMediaListEntry")
    @Headers("Content-Type: application/json")
    Call<Boolean> deleteMediaListEntry(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("DeleteReview")
    @Headers("Content-Type: application/json")
    Call<Boolean> deleteReview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveMediaListEntry")
    @Headers("Content-Type: application/json")
    Call<MediaList> saveMediaListEntry(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UpdateMediaListEntries")
    @Headers("Content-Type: application/json")
    Call<List<MediaList>> updateMediaListEntries(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("RateReview")
    @Headers("Content-Type: application/json")
    Call<Review> rateReview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveReview")
    @Headers("Content-Type: application/json")
    Call<Review> saveReview(@Body QueryContainerBuilder request);
}
