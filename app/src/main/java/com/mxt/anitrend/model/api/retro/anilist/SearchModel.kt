package com.mxt.anitrend.model.api.retro.anilist

import io.github.wax911.library.annotation.GraphQuery
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.model.request.QueryContainerBuilder

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Search queries
 */

interface SearchModel {

    @POST("/")
    @GraphQuery("MediaSearch")
    @Headers("Content-Type: application/json")
    fun getMediaSearch(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @GraphQuery("StudioSearch")
    @Headers("Content-Type: application/json")
    fun getStudioSearch(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<StudioBase>>>

    @POST("/")
    @GraphQuery("StaffSearch")
    @Headers("Content-Type: application/json")
    fun getStaffSearch(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<StaffBase>>>

    @POST("/")
    @GraphQuery("CharacterSearch")
    @Headers("Content-Type: application/json")
    fun getCharacterSearch(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<CharacterBase>>>

    @POST("/")
    @GraphQuery("UserSearch")
    @Headers("Content-Type: application/json")
    fun getUserSearch(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<UserBase>>>
}
