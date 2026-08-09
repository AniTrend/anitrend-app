package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
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
    ): Response<AniListContainer<PageContainer<FeedList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFeedListReply(
        @Body request: GraphQLOperationRequest<FeedListReplyVariables>,
    ): Response<AniListContainer<FeedList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getFeedMessage(
        @Body request: GraphQLOperationRequest<FeedMessageVariables>,
    ): Response<AniListContainer<PageContainer<FeedList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveTextActivity(
        @Body request: GraphQLOperationRequest<SaveTextActivityVariables>,
    ): Response<AniListContainer<FeedList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveMessageActivity(
        @Body request: GraphQLOperationRequest<SaveMessageActivityVariables>,
    ): Response<AniListContainer<FeedList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveActivityReply(
        @Body request: GraphQLOperationRequest<SaveActivityReplyVariables>,
    ): Response<AniListContainer<FeedReply>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteActivity(
        @Body request: GraphQLOperationRequest<DeleteActivityVariables>,
    ): Response<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteActivityReply(
        @Body request: GraphQLOperationRequest<DeleteActivityReplyVariables>,
    ): Response<AniListContainer<DeleteState>>
}
