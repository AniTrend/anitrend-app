package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.FeedReply;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

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
    Call<GraphContainer<PageContainer<FeedList>>> getFeedList(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("FeedListReply")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<FeedList>> getFeedListReply(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("FeedMessage")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<PageContainer<FeedList>>> getFeedMessage(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveTextActivity")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<FeedList>> saveTextActivity(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveMessageActivity")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<FeedList>> saveMessageActivity(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("SaveActivityReply")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<FeedReply>> saveActivityReply(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("DeleteActivity")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<Boolean>> deleteActivity(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("DeleteActivityReply")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<Boolean>> deleteActivityReply(@Body QueryContainerBuilder request);
}
