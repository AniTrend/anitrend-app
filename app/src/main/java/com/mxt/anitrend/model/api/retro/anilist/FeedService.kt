package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.DeleteActivityReplyData
import com.mxt.anitrend.graphql.generated.DeleteActivityReplyVariables
import com.mxt.anitrend.graphql.generated.DeleteActivityData
import com.mxt.anitrend.graphql.generated.DeleteActivityVariables
import com.mxt.anitrend.graphql.generated.FeedListData
import com.mxt.anitrend.graphql.generated.FeedListReplyData
import com.mxt.anitrend.graphql.generated.FeedListReplyVariables
import com.mxt.anitrend.graphql.generated.FeedListVariables
import com.mxt.anitrend.graphql.generated.FeedMessageData
import com.mxt.anitrend.graphql.generated.FeedMessageVariables
import com.mxt.anitrend.graphql.generated.SaveActivityReplyData
import com.mxt.anitrend.graphql.generated.SaveActivityReplyVariables
import com.mxt.anitrend.graphql.generated.SaveMessageActivityData
import com.mxt.anitrend.graphql.generated.SaveMessageActivityVariables
import com.mxt.anitrend.graphql.generated.SaveTextActivityData
import com.mxt.anitrend.graphql.generated.SaveTextActivityVariables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Feed model queries
 */

interface FeedService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFeedList(
        @Body request: GraphQLOperationRequest<FeedListVariables>,
    ): Response<GraphQLResponse<FeedListData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFeedListReply(
        @Body request: GraphQLOperationRequest<FeedListReplyVariables>,
    ): Response<GraphQLResponse<FeedListReplyData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFeedMessage(
        @Body request: GraphQLOperationRequest<FeedMessageVariables>,
    ): Response<GraphQLResponse<FeedMessageData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveTextActivity(
        @Body request: GraphQLOperationRequest<SaveTextActivityVariables>,
    ): Response<GraphQLResponse<SaveTextActivityData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveMessageActivity(
        @Body request: GraphQLOperationRequest<SaveMessageActivityVariables>,
    ): Response<GraphQLResponse<SaveMessageActivityData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveActivityReply(
        @Body request: GraphQLOperationRequest<SaveActivityReplyVariables>,
    ): Response<GraphQLResponse<SaveActivityReplyData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteActivity(
        @Body request: GraphQLOperationRequest<DeleteActivityVariables>,
    ): Response<GraphQLResponse<DeleteActivityData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteActivityReply(
        @Body request: GraphQLOperationRequest<DeleteActivityReplyVariables>,
    ): Response<GraphQLResponse<DeleteActivityReplyData>>
}
