package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioBaseVariables
import com.mxt.anitrend.graphql.generated.StudioMediaData
import com.mxt.anitrend.graphql.generated.StudioMediaVariables
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Studio queries
 */

interface StudioModel {
    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStudioBase(
        @Body request: GraphQLRequest<StudioBaseVariables>,
    ): Call<GraphContainer<StudioBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStudioMedia(
        @Body request: GraphQLRequest<StudioMediaVariables>,
    ): Call<GraphContainer<StudioMediaData>>
}
