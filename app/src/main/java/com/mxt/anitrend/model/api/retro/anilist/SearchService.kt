package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.CharacterSearchVariables
import com.mxt.anitrend.graphql.generated.MediaSearchVariables
import com.mxt.anitrend.graphql.generated.StaffSearchVariables
import com.mxt.anitrend.graphql.generated.StudioSearchVariables
import com.mxt.anitrend.graphql.generated.UserSearchVariables
import com.mxt.anitrend.model.entity.base.*
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
    suspend fun getMediaSearch(@Body request: GraphQLOperationRequest<MediaSearchVariables>): Response<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStudioSearch(@Body request: GraphQLOperationRequest<StudioSearchVariables>): Response<AniListContainer<PageContainer<StudioBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getStaffSearch(@Body request: GraphQLOperationRequest<StaffSearchVariables>): Response<AniListContainer<PageContainer<StaffBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getCharacterSearch(@Body request: GraphQLOperationRequest<CharacterSearchVariables>): Response<AniListContainer<PageContainer<CharacterBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getUserSearch(@Body request: GraphQLOperationRequest<UserSearchVariables>): Response<AniListContainer<PageContainer<UserBase>>>
}
