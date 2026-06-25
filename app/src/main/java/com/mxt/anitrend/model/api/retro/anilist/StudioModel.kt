package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import com.mxt.anitrend.graphql.generated.StudioBaseVariables
import com.mxt.anitrend.graphql.generated.StudioMediaVariables
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
    ): Call<AniListContainer<StudioBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStudioMedia(
        @Body request: GraphQLRequest<StudioMediaVariables>,
    ): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>
}
