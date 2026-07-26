package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.CharacterActorsVariables
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterBaseVariables
import com.mxt.anitrend.graphql.generated.CharacterMediaVariables
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.graphql.generated.CharacterOverviewVariables
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Character relation queries
 */

interface CharacterModel {
    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCharacterBase(
        @Body request: GraphQLRequest<CharacterBaseVariables>,
    ): Call<GraphContainer<CharacterBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCharacterOverview(
        @Body request: GraphQLRequest<CharacterOverviewVariables>,
    ): Call<GraphContainer<CharacterOverviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCharacterMedia(
        @Body request: GraphQLRequest<CharacterMediaVariables>,
    ): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCharacterActors(
        @Body request: GraphQLRequest<CharacterActorsVariables>,
    ): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>
}
