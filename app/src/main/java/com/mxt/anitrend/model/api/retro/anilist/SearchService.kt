package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.CharacterSearchData
import com.mxt.anitrend.graphql.generated.CharacterSearchVariables
import com.mxt.anitrend.graphql.generated.MediaSearchData
import com.mxt.anitrend.graphql.generated.MediaSearchVariables
import com.mxt.anitrend.graphql.generated.StaffSearchData
import com.mxt.anitrend.graphql.generated.StaffSearchVariables
import com.mxt.anitrend.graphql.generated.StudioSearchData
import com.mxt.anitrend.graphql.generated.StudioSearchVariables
import com.mxt.anitrend.graphql.generated.UserSearchData
import com.mxt.anitrend.graphql.generated.UserSearchVariables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Search queries
 */

interface SearchService {

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaSearch(@Body request: GraphQLOperationRequest<MediaSearchVariables>): Response<GraphQLResponse<MediaSearchData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStudioSearch(@Body request: GraphQLOperationRequest<StudioSearchVariables>): Response<GraphQLResponse<StudioSearchData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffSearch(@Body request: GraphQLOperationRequest<StaffSearchVariables>): Response<GraphQLResponse<StaffSearchData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterSearch(@Body request: GraphQLOperationRequest<CharacterSearchVariables>): Response<GraphQLResponse<CharacterSearchData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserSearch(@Body request: GraphQLOperationRequest<UserSearchVariables>): Response<GraphQLResponse<UserSearchData>>
}
