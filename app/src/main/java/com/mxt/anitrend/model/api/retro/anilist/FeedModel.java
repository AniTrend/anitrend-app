package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * Feed model queries
 */

public interface FeedModel {

    @POST("/")
    @GraphQuery("FeedList")
    @Headers("Content-Type: application/json")
    Call<PageContainer<FeedList>> getFeedList(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("FeedListReply")
    @Headers("Content-Type: application/json")
    Call<PageContainer<FeedList>> getFeedListReply(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("FeedMessage")
    @Headers("Content-Type: application/json")
    Call<PageContainer<FeedList>> getFeedMessage(@Body GraphQueryContainer request);
}
