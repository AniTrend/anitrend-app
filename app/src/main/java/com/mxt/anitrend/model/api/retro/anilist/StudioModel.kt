package com.mxt.anitrend.model.api.retro.anilist

import io.github.wax911.library.annotation.GraphQuery
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer

import io.github.wax911.library.model.request.QueryContainerBuilder

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Studio queries
 */

interface StudioModel {

    @POST("/")
    @GraphQuery("StudioBase")
    @Headers("Content-Type: application/json")
    fun getStudioBase(@Body request: QueryContainerBuilder?): Call<AniListContainer<StudioBase>>

    @POST("/")
    @GraphQuery("StudioMedia")
    @Headers("Content-Type: application/json")
    fun getStudioMedia(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>
}
