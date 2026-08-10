package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.DeleteMediaListEntryVariables
import com.mxt.anitrend.graphql.generated.DeleteReviewVariables
import com.mxt.anitrend.graphql.generated.MediaBrowseVariables
import com.mxt.anitrend.graphql.generated.MediaListBrowseVariables
import com.mxt.anitrend.graphql.generated.MediaListCollectionData
import com.mxt.anitrend.graphql.generated.MediaListCollectionVariables
import com.mxt.anitrend.graphql.generated.MediaListVariables
import com.mxt.anitrend.graphql.generated.MediaWithListVariables
import com.mxt.anitrend.graphql.generated.RateReviewVariables
import com.mxt.anitrend.graphql.generated.ReviewBrowseVariables
import com.mxt.anitrend.graphql.generated.SaveMediaListEntryVariables
import com.mxt.anitrend.graphql.generated.SaveReviewVariables
import com.mxt.anitrend.graphql.generated.UpdateMediaListEntriesVariables
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 */

interface BrowseService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaListCollection(
        @Body request: GraphQLOperationRequest<MediaListCollectionVariables>,
    ): Response<GraphContainer<MediaListCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaBrowse(
        @Body request: GraphQLOperationRequest<MediaBrowseVariables>,
    ): Response<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getReviewBrowse(
        @Body request: GraphQLOperationRequest<ReviewBrowseVariables>,
    ): Response<AniListContainer<PageContainer<Review>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaListBrowse(
        @Body request: GraphQLOperationRequest<MediaListBrowseVariables>,
    ): Response<AniListContainer<PageContainer<MediaList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaList(
        @Body request: GraphQLOperationRequest<MediaListVariables>,
    ): Response<AniListContainer<MediaList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaWithList(
        @Body request: GraphQLOperationRequest<MediaWithListVariables>,
    ): Response<AniListContainer<MediaBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteMediaListEntry(
        @Body request: GraphQLOperationRequest<DeleteMediaListEntryVariables>,
    ): Response<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteReview(
        @Body request: GraphQLOperationRequest<DeleteReviewVariables>,
    ): Response<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveMediaListEntry(
        @Body request: GraphQLOperationRequest<SaveMediaListEntryVariables>,
    ): Response<AniListContainer<MediaList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun updateMediaListEntries(
        @Body request: GraphQLOperationRequest<UpdateMediaListEntriesVariables>,
    ): Response<AniListContainer<List<MediaList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun rateReview(
        @Body request: GraphQLOperationRequest<RateReviewVariables>,
    ): Response<AniListContainer<Review>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveReview(
        @Body request: GraphQLOperationRequest<SaveReviewVariables>,
    ): Response<AniListContainer<Review>>
}
