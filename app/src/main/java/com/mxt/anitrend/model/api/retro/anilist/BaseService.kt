package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.GenreCollectionData
import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import com.mxt.anitrend.graphql.generated.ToggleFavouriteVariables
import com.mxt.anitrend.graphql.generated.ToggleLikeVariables
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 */

interface BaseService {
    @POST("/")
    @Headers("Content-Type: application/json")
    fun getGenres(
        @Body request: GraphQLRequest<EmptyGraphQLVariables>,
    ): Call<GraphContainer<GenreCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getTags(
        @Body request: GraphQLRequest<EmptyGraphQLVariables>,
    ): Call<GraphContainer<MediaTagCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun toggleLike(
        @Body request: GraphQLRequest<ToggleLikeVariables>,
    ): Call<AniListContainer<List<UserBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun toggleFavourite(
        @Body request: GraphQLRequest<ToggleFavouriteVariables>,
    ): Call<ResponseBody>
}
