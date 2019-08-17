package com.mxt.anitrend.model.api.retro.anilist

import io.github.wax911.library.annotation.GraphQuery
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.model.request.QueryContainerBuilder

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 */

interface BrowseModel {

    @POST("/")
    @GraphQuery("MediaListCollection")
    @Headers("Content-Type: application/json")
    fun getMediaListCollection(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<MediaListCollection>>>

    @POST("/")
    @GraphQuery("MediaBrowse")
    @Headers("Content-Type: application/json")
    fun getMediaBrowse(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @GraphQuery("ReviewBrowse")
    @Headers("Content-Type: application/json")
    fun getReviewBrowse(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<Review>>>

    @POST("/")
    @GraphQuery("MediaListBrowse")
    @Headers("Content-Type: application/json")
    fun getMediaListBrowse(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<MediaList>>>

    @POST("/")
    @GraphQuery("MediaList")
    @Headers("Content-Type: application/json")
    fun getMediaList(@Body request: QueryContainerBuilder?): Call<AniListContainer<MediaList>>

    @POST("/")
    @GraphQuery("MediaWithList")
    @Headers("Content-Type: application/json")
    fun getMediaWithList(@Body request: QueryContainerBuilder?): Call<AniListContainer<MediaBase>>

    @POST("/")
    @GraphQuery("DeleteMediaListEntry")
    @Headers("Content-Type: application/json")
    fun deleteMediaListEntry(@Body request: QueryContainerBuilder?): Call<AniListContainer<DeleteState>>

    @POST("/")
    @GraphQuery("DeleteReview")
    @Headers("Content-Type: application/json")
    fun deleteReview(@Body request: QueryContainerBuilder?): Call<AniListContainer<DeleteState>>

    @POST("/")
    @GraphQuery("SaveMediaListEntry")
    @Headers("Content-Type: application/json")
    fun saveMediaListEntry(@Body request: QueryContainerBuilder?): Call<AniListContainer<MediaList>>

    @POST("/")
    @GraphQuery("UpdateMediaListEntries")
    @Headers("Content-Type: application/json")
    fun updateMediaListEntries(@Body request: QueryContainerBuilder?): Call<AniListContainer<List<MediaList>>>

    @POST("/")
    @GraphQuery("RateReview")
    @Headers("Content-Type: application/json")
    fun rateReview(@Body request: QueryContainerBuilder?): Call<AniListContainer<Review>>

    @POST("/")
    @GraphQuery("SaveReview")
    @Headers("Content-Type: application/json")
    fun saveReview(@Body request: QueryContainerBuilder?): Call<AniListContainer<Review>>
}
