package com.mxt.anitrend.model.api.retro.anilist

import io.github.wax911.library.annotation.GraphQuery
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.model.request.QueryContainerBuilder

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Staff queries
 */

interface StaffModel {

    @POST("/")
    @GraphQuery("StaffBase")
    @Headers("Content-Type: application/json")
    fun getStaffBase(@Body request: QueryContainerBuilder?): Call<AniListContainer<StaffBase>>

    @POST("/")
    @GraphQuery("StaffOverview")
    @Headers("Content-Type: application/json")
    fun getStaffOverview(@Body request: QueryContainerBuilder?): Call<AniListContainer<StaffBase>>

    @POST("/")
    @GraphQuery("StaffMedia")
    @Headers("Content-Type: application/json")
    fun getStaffMedia(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>

    @POST("/")
    @GraphQuery("StaffRoles")
    @Headers("Content-Type: application/json")
    fun getStaffRoles(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>
}
