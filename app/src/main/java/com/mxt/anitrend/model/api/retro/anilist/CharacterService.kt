package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Character relation queries
 */

interface CharacterService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterBase(
        @Body request: GraphQLOperationRequest<CharacterBaseVariables>,
    ): Response<GraphContainer<CharacterBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterOverview(
        @Body request: GraphQLOperationRequest<CharacterOverviewVariables>,
    ): Response<GraphContainer<CharacterOverviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterMedia(
        @Body request: GraphQLOperationRequest<CharacterMediaVariables>,
    ): Response<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterActors(
        @Body request: GraphQLOperationRequest<CharacterActorsVariables>,
    ): Response<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>
}
