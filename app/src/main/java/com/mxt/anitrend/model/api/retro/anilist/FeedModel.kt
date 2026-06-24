package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import com.mxt.anitrend.graphql.generated.DeleteActivityReplyVariables
import com.mxt.anitrend.graphql.generated.DeleteActivityVariables
import com.mxt.anitrend.graphql.generated.FeedListReplyVariables
import com.mxt.anitrend.graphql.generated.FeedListVariables
import com.mxt.anitrend.graphql.generated.FeedMessageVariables
import com.mxt.anitrend.graphql.generated.SaveActivityReplyVariables
import com.mxt.anitrend.graphql.generated.SaveMessageActivityVariables
import com.mxt.anitrend.graphql.generated.SaveTextActivityVariables
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
    @Headers("Content-Type: application/json")
    fun getFeedList(
        @Body request: GraphQLRequest<FeedListVariables>,
    ): Call<AniListContainer<PageContainer<FeedList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getFeedListReply(
        @Body request: GraphQLRequest<FeedListReplyVariables>,
    ): Call<AniListContainer<FeedList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getFeedMessage(
        @Body request: GraphQLRequest<FeedMessageVariables>,
    ): Call<AniListContainer<PageContainer<FeedList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveTextActivity(
        @Body request: GraphQLRequest<SaveTextActivityVariables>,
    ): Call<AniListContainer<FeedList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveMessageActivity(
        @Body request: GraphQLRequest<SaveMessageActivityVariables>,
    ): Call<AniListContainer<FeedList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveActivityReply(
        @Body request: GraphQLRequest<SaveActivityReplyVariables>,
    ): Call<AniListContainer<FeedReply>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun deleteActivity(
        @Body request: GraphQLRequest<DeleteActivityVariables>,
    ): Call<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun deleteActivityReply(
        @Body request: GraphQLRequest<DeleteActivityReplyVariables>,
    ): Call<AniListContainer<DeleteState>>
}
