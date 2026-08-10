package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.StaffBaseData
import com.mxt.anitrend.graphql.generated.StaffBaseVariables
import com.mxt.anitrend.graphql.generated.StaffCharactersVariables
import com.mxt.anitrend.graphql.generated.StaffMediaVariables
import com.mxt.anitrend.graphql.generated.StaffOverviewVariables
import com.mxt.anitrend.graphql.generated.StaffRolesVariables
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
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
 * Staff queries
 */

interface StaffService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffBase(
        @Body request: GraphQLOperationRequest<StaffBaseVariables>,
    ): Response<GraphContainer<StaffBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffOverview(
        @Body request: GraphQLOperationRequest<StaffOverviewVariables>,
    ): Response<AniListContainer<StaffBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffCharacters(
        @Body request: GraphQLOperationRequest<StaffCharactersVariables>,
    ): Response<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffMedia(
        @Body request: GraphQLOperationRequest<StaffMediaVariables>,
    ): Response<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffRoles(
        @Body request: GraphQLOperationRequest<StaffRolesVariables>,
    ): Response<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>
}
