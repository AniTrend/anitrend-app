package com.mxt.anitrend.model.api.retro.anilist

import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import io.github.wax911.library.annotation.GraphQuery
import io.github.wax911.library.model.request.QueryContainerBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 */

interface BaseModel {

    @POST("/")
    @GraphQuery("GenreCollection")
    @Headers("Content-Type: application/json")
    fun getGenres(@Body request: QueryContainerBuilder?): Call<AniListContainer<List<String>>>

    @POST("/")
    @GraphQuery("MediaTagCollection")
    @Headers("Content-Type: application/json")
    fun getTags(@Body request: QueryContainerBuilder?): Call<AniListContainer<List<MediaTag>>>

    @POST("/")
    @GraphQuery("ToggleLike")
    @Headers("Content-Type: application/json")
    fun toggleLike(@Body request: QueryContainerBuilder?): Call<AniListContainer<List<UserBase>>>

    @POST("/")
    @GraphQuery("ToggleFavourite")
    @Headers("Content-Type: application/json")
    fun toggleFavourite(@Body request: QueryContainerBuilder?): Call<ResponseBody>
}
