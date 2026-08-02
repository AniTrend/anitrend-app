package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.MediaBaseVariables
import com.mxt.anitrend.graphql.generated.MediaCharactersVariables
import com.mxt.anitrend.graphql.generated.MediaEpisodesVariables
import com.mxt.anitrend.graphql.generated.MediaOverviewVariables
import com.mxt.anitrend.graphql.generated.MediaRelationsVariables
import com.mxt.anitrend.graphql.generated.MediaSocialVariables
import com.mxt.anitrend.graphql.generated.MediaStaffVariables
import com.mxt.anitrend.graphql.generated.MediaStatsVariables
import com.mxt.anitrend.graphql.generated.RecommendationMediaData
import com.mxt.anitrend.graphql.generated.RecommendationMediaVariables
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Series queries
 */

interface MediaService {
    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaBase(
        @Body request: GraphQLRequest<MediaBaseVariables>,
    ): Call<AniListContainer<MediaBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaOverview(
        @Body request: GraphQLRequest<MediaOverviewVariables>,
    ): Call<AniListContainer<Media>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaRelations(
        @Body request: GraphQLRequest<MediaRelationsVariables>,
    ): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaStats(
        @Body request: GraphQLRequest<MediaStatsVariables>,
    ): Call<AniListContainer<Media>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaEpisodes(
        @Body request: GraphQLRequest<MediaEpisodesVariables>,
    ): Call<AniListContainer<ConnectionContainer<List<ExternalLink>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaCharacters(
        @Body request: GraphQLRequest<MediaCharactersVariables>,
    ): Call<AniListContainer<ConnectionContainer<EdgeContainer<CharacterEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaStaff(
        @Body request: GraphQLRequest<MediaStaffVariables>,
    ): Call<AniListContainer<ConnectionContainer<EdgeContainer<StaffEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaRecommendations(
        @Body request: GraphQLRequest<RecommendationMediaVariables>,
    ): Call<GraphContainer<RecommendationMediaData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaSocial(
        @Body request: GraphQLRequest<MediaSocialVariables>,
    ): Call<AniListContainer<PageContainer<FeedList>>>
}
