package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.MediaListCollection;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
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
    @GraphQuery("MediaListCollection")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<MediaListCollection>>> getMediaListCollection(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaBrowse")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<MediaBase>>> getMediaBrowse(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("ReviewBrowse")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<Review>>> getReviewBrowse(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaListBrowse")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<MediaList>>> getMediaListBrowse(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaList")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<MediaList>> getMediaList(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaWithList")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<MediaBase>> getMediaWithList(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("DeleteMediaListEntry")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<DeleteState>> deleteMediaListEntry(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("DeleteReview")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<DeleteState>> deleteReview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveMediaListEntry")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<MediaList>> saveMediaListEntry(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("UpdateMediaListEntries")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<List<MediaList>>> updateMediaListEntries(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("RateReview")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<Review>> rateReview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveReview")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<Review>> saveReview(@Body QueryContainerBuilder request);
}
