package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.MediaBaseData
import com.mxt.anitrend.graphql.generated.MediaBaseVariables
import com.mxt.anitrend.graphql.generated.MediaCharactersData
import com.mxt.anitrend.graphql.generated.MediaCharactersVariables
import com.mxt.anitrend.graphql.generated.MediaEpisodesData
import com.mxt.anitrend.graphql.generated.MediaEpisodesVariables
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaOverviewVariables
import com.mxt.anitrend.graphql.generated.MediaRelationsData
import com.mxt.anitrend.graphql.generated.MediaRelationsVariables
import com.mxt.anitrend.graphql.generated.MediaSocialVariables
import com.mxt.anitrend.graphql.generated.MediaStaffData
import com.mxt.anitrend.graphql.generated.MediaStatsData
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
import retrofit2.Response
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
    suspend fun getMediaBase(
        @Body request: GraphQLOperationRequest<MediaBaseVariables>,
    ): Response<AniListContainer<MediaBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaBaseRecord(
        @Body request: GraphQLOperationRequest<MediaBaseVariables>,
    ): Response<GraphContainer<MediaBaseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaOverview(
        @Body request: GraphQLOperationRequest<MediaOverviewVariables>,
    ): Response<AniListContainer<Media>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaOverviewRecord(
        @Body request: GraphQLOperationRequest<MediaOverviewVariables>,
    ): Response<GraphContainer<MediaOverviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaRelations(
        @Body request: GraphQLOperationRequest<MediaRelationsVariables>,
    ): Response<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaRelationsRecord(
        @Body request: GraphQLOperationRequest<MediaRelationsVariables>,
    ): Response<GraphContainer<MediaRelationsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaStats(
        @Body request: GraphQLOperationRequest<MediaStatsVariables>,
    ): Response<AniListContainer<Media>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaStatsRecord(
        @Body request: GraphQLOperationRequest<MediaStatsVariables>,
    ): Response<GraphContainer<MediaStatsData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaEpisodes(
        @Body request: GraphQLOperationRequest<MediaEpisodesVariables>,
    ): Response<AniListContainer<ConnectionContainer<List<ExternalLink>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaEpisodesRecord(
        @Body request: GraphQLOperationRequest<MediaEpisodesVariables>,
    ): Response<GraphContainer<MediaEpisodesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaCharacters(
        @Body request: GraphQLOperationRequest<MediaCharactersVariables>,
    ): Response<AniListContainer<ConnectionContainer<EdgeContainer<CharacterEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaCharactersRecord(
        @Body request: GraphQLOperationRequest<MediaCharactersVariables>,
    ): Response<GraphContainer<MediaCharactersData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaStaff(
        @Body request: GraphQLOperationRequest<MediaStaffVariables>,
    ): Response<AniListContainer<ConnectionContainer<EdgeContainer<StaffEdge>>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaStaffRecord(
        @Body request: GraphQLOperationRequest<MediaStaffVariables>,
    ): Response<GraphContainer<MediaStaffData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaRecommendations(
        @Body request: GraphQLOperationRequest<RecommendationMediaVariables>,
    ): Response<GraphContainer<RecommendationMediaData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaSocial(
        @Body request: GraphQLOperationRequest<MediaSocialVariables>,
    ): Response<AniListContainer<PageContainer<FeedList>>>
}
