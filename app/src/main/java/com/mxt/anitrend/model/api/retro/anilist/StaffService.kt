package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.StaffBaseData
import com.mxt.anitrend.graphql.generated.StaffBaseVariables
import com.mxt.anitrend.graphql.generated.StaffCharactersData
import com.mxt.anitrend.graphql.generated.StaffCharactersVariables
import com.mxt.anitrend.graphql.generated.StaffMediaData
import com.mxt.anitrend.graphql.generated.StaffMediaVariables
import com.mxt.anitrend.graphql.generated.StaffOverviewData
import com.mxt.anitrend.graphql.generated.StaffOverviewVariables
import com.mxt.anitrend.graphql.generated.StaffRolesData
import com.mxt.anitrend.graphql.generated.StaffRolesVariables
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
    ): Response<GraphQLResponse<StaffBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffOverview(
        @Body request: GraphQLOperationRequest<StaffOverviewVariables>,
    ): Response<GraphQLResponse<StaffOverviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffCharacters(
        @Body request: GraphQLOperationRequest<StaffCharactersVariables>,
    ): Response<GraphQLResponse<StaffCharactersData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffMedia(
        @Body request: GraphQLOperationRequest<StaffMediaVariables>,
    ): Response<GraphQLResponse<StaffMediaData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffRoles(
        @Body request: GraphQLOperationRequest<StaffRolesVariables>,
    ): Response<GraphQLResponse<StaffRolesData>>
}
