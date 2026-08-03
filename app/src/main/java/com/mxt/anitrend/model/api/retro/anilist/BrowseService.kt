package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
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
        @Body request: GraphQLRequest<MediaListCollectionVariables>,
    ): Call<GraphContainer<MediaListCollectionData>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaBrowse(
        @Body request: GraphQLRequest<MediaBrowseVariables>,
    ): Call<AniListContainer<PageContainer<MediaBase>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getReviewBrowse(
        @Body request: GraphQLRequest<ReviewBrowseVariables>,
    ): Call<AniListContainer<PageContainer<Review>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaListBrowse(
        @Body request: GraphQLRequest<MediaListBrowseVariables>,
    ): Call<AniListContainer<PageContainer<MediaList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaList(
        @Body request: GraphQLRequest<MediaListVariables>,
    ): Call<AniListContainer<MediaList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun getMediaWithList(
        @Body request: GraphQLRequest<MediaWithListVariables>,
    ): Call<AniListContainer<MediaBase>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun deleteMediaListEntry(
        @Body request: GraphQLRequest<DeleteMediaListEntryVariables>,
    ): Call<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun deleteReview(
        @Body request: GraphQLRequest<DeleteReviewVariables>,
    ): Call<AniListContainer<DeleteState>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveMediaListEntry(
        @Body request: GraphQLRequest<SaveMediaListEntryVariables>,
    ): Call<AniListContainer<MediaList>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun updateMediaListEntries(
        @Body request: GraphQLRequest<UpdateMediaListEntriesVariables>,
    ): Call<AniListContainer<List<MediaList>>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun rateReview(
        @Body request: GraphQLRequest<RateReviewVariables>,
    ): Call<AniListContainer<Review>>

    @POST("/")
    @Headers("Content-Type: application/json")
    fun saveReview(
        @Body request: GraphQLRequest<SaveReviewVariables>,
    ): Call<AniListContainer<Review>>
}
