package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.GenreCollectionData
import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import com.mxt.anitrend.graphql.generated.ToggleFavouriteData
import com.mxt.anitrend.graphql.generated.ToggleFavouriteVariables
import com.mxt.anitrend.graphql.generated.ToggleLikeData
import com.mxt.anitrend.graphql.generated.ToggleLikeVariables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 */

interface BaseService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getGenres(
        @Body request: GraphQLRequest<EmptyGraphQLVariables>,
    ): Response<GraphQLResponse<GenreCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getTags(
        @Body request: GraphQLRequest<EmptyGraphQLVariables>,
    ): Response<GraphQLResponse<MediaTagCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun toggleLike(
        @Body request: GraphQLOperationRequest<ToggleLikeVariables>,
    ): Response<GraphQLResponse<ToggleLikeData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun toggleFavourite(
        @Body request: GraphQLOperationRequest<ToggleFavouriteVariables>,
    ): Response<GraphQLResponse<ToggleFavouriteData>>
}
