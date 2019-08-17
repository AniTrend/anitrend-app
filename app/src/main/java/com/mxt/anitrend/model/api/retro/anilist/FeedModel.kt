package com.mxt.anitrend.model.api.retro.anilist

import io.github.wax911.library.annotation.GraphQuery
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.model.request.QueryContainerBuilder

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Feed model queries
 */

interface FeedModel {

    @POST("/")
    @GraphQuery("FeedList")
    @Headers("Content-Type: application/json")
    fun getFeedList(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<FeedList>>>

    @POST("/")
    @GraphQuery("FeedListReply")
    @Headers("Content-Type: application/json")
    fun getFeedListReply(@Body request: QueryContainerBuilder?): Call<AniListContainer<FeedList>>

    @POST("/")
    @GraphQuery("FeedMessage")
    @Headers("Content-Type: application/json")
    fun getFeedMessage(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<FeedList>>>

    @POST("/")
    @GraphQuery("SaveTextActivity")
    @Headers("Content-Type: application/json")
    fun saveTextActivity(@Body request: QueryContainerBuilder?): Call<AniListContainer<FeedList>>

    @POST("/")
    @GraphQuery("SaveMessageActivity")
    @Headers("Content-Type: application/json")
    fun saveMessageActivity(@Body request: QueryContainerBuilder?): Call<AniListContainer<FeedList>>

    @POST("/")
    @GraphQuery("SaveActivityReply")
    @Headers("Content-Type: application/json")
    fun saveActivityReply(@Body request: QueryContainerBuilder?): Call<AniListContainer<FeedReply>>

    @POST("/")
    @GraphQuery("DeleteActivity")
    @Headers("Content-Type: application/json")
    fun deleteActivity(@Body request: QueryContainerBuilder?): Call<AniListContainer<DeleteState>>

    @POST("/")
    @GraphQuery("DeleteActivityReply")
    @Headers("Content-Type: application/json")
    fun deleteActivityReply(@Body request: QueryContainerBuilder?): Call<AniListContainer<DeleteState>>
}
