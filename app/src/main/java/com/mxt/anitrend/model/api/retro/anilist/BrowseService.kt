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
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 */

interface BrowseService {
    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaListCollection(
        @Body request: GraphQLOperationRequest<MediaListCollectionVariables>,
    ): Call<GraphContainer<MediaListCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaBrowse(
        @Body request: GraphQLOperationRequest<MediaBrowseVariables>,
    ): Call<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getReviewBrowse(
        @Body request: GraphQLOperationRequest<ReviewBrowseVariables>,
    ): Call<AniListContainer<PageContainer<Review>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaListBrowse(
        @Body request: GraphQLOperationRequest<MediaListBrowseVariables>,
    ): Call<AniListContainer<PageContainer<MediaList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaList(
        @Body request: GraphQLOperationRequest<MediaListVariables>,
    ): Call<AniListContainer<MediaList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaWithList(
        @Body request: GraphQLOperationRequest<MediaWithListVariables>,
    ): Call<AniListContainer<MediaBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun deleteMediaListEntry(
        @Body request: GraphQLOperationRequest<DeleteMediaListEntryVariables>,
    ): Call<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun deleteReview(
        @Body request: GraphQLOperationRequest<DeleteReviewVariables>,
    ): Call<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveMediaListEntry(
        @Body request: GraphQLOperationRequest<SaveMediaListEntryVariables>,
    ): Call<AniListContainer<MediaList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun updateMediaListEntries(
        @Body request: GraphQLOperationRequest<UpdateMediaListEntriesVariables>,
    ): Call<AniListContainer<List<MediaList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun rateReview(
        @Body request: GraphQLOperationRequest<RateReviewVariables>,
    ): Call<AniListContainer<Review>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveReview(
        @Body request: GraphQLOperationRequest<SaveReviewVariables>,
    ): Call<AniListContainer<Review>>
}
