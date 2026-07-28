package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import com.mxt.anitrend.graphql.generated.CharacterSearchVariables
import com.mxt.anitrend.graphql.generated.MediaSearchVariables
import com.mxt.anitrend.graphql.generated.StaffSearchVariables
import com.mxt.anitrend.graphql.generated.StudioSearchVariables
import com.mxt.anitrend.graphql.generated.UserSearchVariables
import com.mxt.anitrend.model.entity.base.*
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import retrofit2.Call
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
    fun getMediaSearch(@Body request: GraphQLRequest<MediaSearchVariables>): Call<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStudioSearch(@Body request: GraphQLRequest<StudioSearchVariables>): Call<AniListContainer<PageContainer<StudioBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getStaffSearch(@Body request: GraphQLRequest<StaffSearchVariables>): Call<AniListContainer<PageContainer<StaffBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getCharacterSearch(@Body request: GraphQLRequest<CharacterSearchVariables>): Call<AniListContainer<PageContainer<CharacterBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getUserSearch(@Body request: GraphQLRequest<UserSearchVariables>): Call<AniListContainer<PageContainer<UserBase>>>
}
