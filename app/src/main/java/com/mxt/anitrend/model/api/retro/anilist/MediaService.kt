package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.MediaBaseData
import com.mxt.anitrend.graphql.generated.MediaBaseVariables
import com.mxt.anitrend.graphql.generated.MediaCharactersData
import com.mxt.anitrend.graphql.generated.MediaCharactersVariables
import com.mxt.anitrend.graphql.generated.MediaEpisodesData
import com.mxt.anitrend.graphql.generated.MediaEpisodesVariables
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaOverviewVariables
import com.mxt.anitrend.graphql.generated.MediaRelationsData
import com.mxt.anitrend.graphql.generated.MediaRelationsVariables
import com.mxt.anitrend.graphql.generated.MediaSocialData
import com.mxt.anitrend.graphql.generated.MediaSocialVariables
import com.mxt.anitrend.graphql.generated.MediaStaffData
import com.mxt.anitrend.graphql.generated.MediaStaffVariables
import com.mxt.anitrend.graphql.generated.MediaStatsData
import com.mxt.anitrend.graphql.generated.MediaStatsVariables
import com.mxt.anitrend.graphql.generated.RecommendationMediaData
import com.mxt.anitrend.graphql.generated.RecommendationMediaVariables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Series queries
 */

interface MediaService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaBaseRecord(
        @Body request: GraphQLOperationRequest<MediaBaseVariables>,
    ): Response<GraphQLResponse<MediaBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaOverviewRecord(
        @Body request: GraphQLOperationRequest<MediaOverviewVariables>,
    ): Response<GraphQLResponse<MediaOverviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaRelationsRecord(
        @Body request: GraphQLOperationRequest<MediaRelationsVariables>,
    ): Response<GraphQLResponse<MediaRelationsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaStatsRecord(
        @Body request: GraphQLOperationRequest<MediaStatsVariables>,
    ): Response<GraphQLResponse<MediaStatsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaEpisodesRecord(
        @Body request: GraphQLOperationRequest<MediaEpisodesVariables>,
    ): Response<GraphQLResponse<MediaEpisodesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaCharactersRecord(
        @Body request: GraphQLOperationRequest<MediaCharactersVariables>,
    ): Response<GraphQLResponse<MediaCharactersData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaStaffRecord(
        @Body request: GraphQLOperationRequest<MediaStaffVariables>,
    ): Response<GraphQLResponse<MediaStaffData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaRecommendations(
        @Body request: GraphQLOperationRequest<RecommendationMediaVariables>,
    ): Response<GraphQLResponse<RecommendationMediaData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaSocialRecord(
        @Body request: GraphQLOperationRequest<MediaSocialVariables>,
    ): Response<GraphQLResponse<MediaSocialData>>
}
