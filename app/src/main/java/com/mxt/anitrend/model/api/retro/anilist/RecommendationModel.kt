package com.mxt.anitrend.model.api.retro.anilist

import com.mxt.anitrend.model.entity.anilist.Recommendation
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.annotation.GraphQuery
import io.github.wax911.library.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RecommendationModel {


    @POST("/")
    @GraphQuery("RecommendationMediaList")
    @Headers("Content-Type: application/json")
    fun getRecommendationMediaList(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<Recommendation>>>
}