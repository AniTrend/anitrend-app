package com.mxt.anitrend.model.api.retro.anilist

import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.RecommendationEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.RecommendationBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.annotation.GraphQuery
import io.github.wax911.library.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Series queries
 */

interface MediaModel {

    @POST("/")
    @GraphQuery("MediaBase")
    @Headers("Content-Type: application/json")
    fun getMediaBase(@Body request: QueryContainerBuilder?): Call<AniListContainer<MediaBase>>

    @POST("/")
    @GraphQuery("MediaOverview")
    @Headers("Content-Type: application/json")
    fun getMediaOverview(@Body request: QueryContainerBuilder?): Call<AniListContainer<Media>>

    @POST("/")
    @GraphQuery("MediaRelations")
    @Headers("Content-Type: application/json")
    fun getMediaRelations(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>

    @POST("/")
    @GraphQuery("MediaStats")
    @Headers("Content-Type: application/json")
    fun getMediaStats(@Body request: QueryContainerBuilder?): Call<AniListContainer<Media>>

    @POST("/")
    @GraphQuery("MediaEpisodes")
    @Headers("Content-Type: application/json")
    fun getMediaEpisodes(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<List<ExternalLink>>>>

    @POST("/")
    @GraphQuery("MediaCharacters")
    @Headers("Content-Type: application/json")
    fun getMediaCharacters(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<EdgeContainer<CharacterEdge>>>>

    @POST("/")
    @GraphQuery("MediaStaff")
    @Headers("Content-Type: application/json")
    fun getMediaStaff(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<EdgeContainer<StaffEdge>>>>


    @POST("/")
    @GraphQuery("RecommendationMedia")
    @Headers("Content-Type: application/json")
    fun getMediaRecommendations(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<PageContainer<RecommendationBase>>>>

    @POST("/")
    @GraphQuery("MediaSocial")
    @Headers("Content-Type: application/json")
    fun getMediaSocial(@Body request: QueryContainerBuilder?): Call<AniListContainer<PageContainer<FeedList>>>
}
