package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.DeleteMediaListEntryData
import com.mxt.anitrend.graphql.generated.DeleteMediaListEntryVariables
import com.mxt.anitrend.graphql.generated.DeleteReviewData
import com.mxt.anitrend.graphql.generated.DeleteReviewVariables
import com.mxt.anitrend.graphql.generated.MediaBrowseData
import com.mxt.anitrend.graphql.generated.MediaBrowseVariables
import com.mxt.anitrend.graphql.generated.MediaListBrowseData
import com.mxt.anitrend.graphql.generated.MediaListBrowseVariables
import com.mxt.anitrend.graphql.generated.MediaListCollectionData
import com.mxt.anitrend.graphql.generated.MediaListCollectionVariables
import com.mxt.anitrend.graphql.generated.MediaListData
import com.mxt.anitrend.graphql.generated.MediaListVariables
import com.mxt.anitrend.graphql.generated.MediaWithListData
import com.mxt.anitrend.graphql.generated.MediaWithListVariables
import com.mxt.anitrend.graphql.generated.RateReviewData
import com.mxt.anitrend.graphql.generated.RateReviewVariables
import com.mxt.anitrend.graphql.generated.ReviewBrowseData
import com.mxt.anitrend.graphql.generated.ReviewBrowseVariables
import com.mxt.anitrend.graphql.generated.SaveMediaListEntryData
import com.mxt.anitrend.graphql.generated.SaveMediaListEntryVariables
import com.mxt.anitrend.graphql.generated.SaveReviewData
import com.mxt.anitrend.graphql.generated.SaveReviewVariables
import com.mxt.anitrend.graphql.generated.UpdateMediaListEntriesData
import com.mxt.anitrend.graphql.generated.UpdateMediaListEntriesVariables
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
    ): Response<GraphQLResponse<MediaListCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaBrowse(
        @Body request: GraphQLOperationRequest<MediaBrowseVariables>,
    ): Response<GraphQLResponse<MediaBrowseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getReviewBrowse(
        @Body request: GraphQLOperationRequest<ReviewBrowseVariables>,
    ): Response<GraphQLResponse<ReviewBrowseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaListBrowse(
        @Body request: GraphQLOperationRequest<MediaListBrowseVariables>,
    ): Response<GraphQLResponse<MediaListBrowseData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaList(
        @Body request: GraphQLOperationRequest<MediaListVariables>,
    ): Response<GraphQLResponse<MediaListData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getMediaWithList(
        @Body request: GraphQLOperationRequest<MediaWithListVariables>,
    ): Response<GraphQLResponse<MediaWithListData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteMediaListEntry(
        @Body request: GraphQLOperationRequest<DeleteMediaListEntryVariables>,
    ): Response<GraphQLResponse<DeleteMediaListEntryData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun deleteReview(
        @Body request: GraphQLOperationRequest<DeleteReviewVariables>,
    ): Response<GraphQLResponse<DeleteReviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveMediaListEntry(
        @Body request: GraphQLOperationRequest<SaveMediaListEntryVariables>,
    ): Response<GraphQLResponse<SaveMediaListEntryData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun updateMediaListEntries(
        @Body request: GraphQLOperationRequest<UpdateMediaListEntriesVariables>,
    ): Response<GraphQLResponse<UpdateMediaListEntriesData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun rateReview(
        @Body request: GraphQLOperationRequest<RateReviewVariables>,
    ): Response<GraphQLResponse<RateReviewData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun saveReview(
        @Body request: GraphQLOperationRequest<SaveReviewVariables>,
    ): Response<GraphQLResponse<SaveReviewData>>
}
