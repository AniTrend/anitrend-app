package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
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
import retrofit2.Call
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
    fun getStaffBase(
        @Body request: GraphQLRequest<StaffBaseVariables>,
    ): Call<AniListContainer<StaffBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStaffOverview(
        @Body request: GraphQLRequest<StaffOverviewVariables>,
    ): Call<AniListContainer<StaffBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStaffCharacters(
        @Body request: GraphQLRequest<StaffCharactersVariables>,
    ): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStaffMedia(
        @Body request: GraphQLRequest<StaffMediaVariables>,
    ): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStaffRoles(
        @Body request: GraphQLRequest<StaffRolesVariables>,
    ): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>
}
