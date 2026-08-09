package com.mxt.anitrend.model.api.retro.anilist

import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import com.mxt.anitrend.graphql.generated.RecommendationMediaListVariables
import com.mxt.anitrend.model.entity.anilist.Recommendation
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RecommendationService {
    @POST("/")
    @Headers("Content-Type: application/json")
    suspend fun getRecommendationMediaList(
        @Body request: GraphQLOperationRequest<RecommendationMediaListVariables>,
    ): Response<AniListContainer<PageContainer<Recommendation>>>
}
