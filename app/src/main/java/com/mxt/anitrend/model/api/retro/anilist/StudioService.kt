package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioBaseVariables
import com.mxt.anitrend.graphql.generated.StudioMediaData
import com.mxt.anitrend.graphql.generated.StudioMediaVariables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Studio queries
 */

interface StudioService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStudioBase(
        @Body request: GraphQLOperationRequest<StudioBaseVariables>,
    ): Response<GraphContainer<StudioBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStudioMedia(
        @Body request: GraphQLOperationRequest<StudioMediaVariables>,
    ): Response<GraphContainer<StudioMediaData>>
}
