package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLTransportCodec
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioMediaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class StudioGraphQLResponseDecodeTest {

    private val codec = KotlinxGraphQLTransportCodec()

    @Test
    fun `decodes studio base data`() {
        val response = codec.decodeResponse<GraphQLResponse<StudioBaseData>>(
            """{"data":{"Studio":{"id":1,"name":"Kyoto Animation","isAnimationStudio":true,"isFavourite":false,"siteUrl":"https://anilist.co/studio/1"}}}""",
            graphQLResponseType(StudioBaseData::class.java),
        )

        val studio = (response.data as GraphQLData.Present<*>).value as StudioBaseData
        assertEquals(1, studio.studio?.id)
        assertEquals("Kyoto Animation", studio.studio?.name)
        assertEquals(true, studio.studio?.isAnimationStudio)
        assertEquals(false, studio.studio?.isFavourite)
        assertEquals("https://anilist.co/studio/1", studio.studio?.siteUrl)
    }

    @Test
    fun `decodes studio media data`() {
        val response = codec.decodeResponse<GraphQLResponse<StudioMediaData>>(
            """{"data":{"Studio":{"media":{"pageInfo":{"total":1,"perPage":20,"currentPage":1,"lastPage":1,"hasNextPage":false},"nodes":[{"id":10,"title":{"romaji":"Violet Evergarden","english":"Violet Evergarden","native":"ヴァイオレット・エヴァーガーデン","userPreferred":"Violet Evergarden"},"coverImage":{"extraLarge":"extra.jpg","large":"large.jpg","medium":"medium.jpg","color":"#fff"},"bannerImage":"banner.jpg","type":"ANIME","format":"TV","season":"WINTER","status":"FINISHED","siteUrl":"https://anilist.co/anime/10","meanScore":84,"averageScore":85,"startDate":{"year":2018,"month":1,"day":11},"endDate":{"year":2018,"month":4,"day":5},"episodes":13,"chapters":null,"volumes":null,"isAdult":false,"isFavourite":true,"nextAiringEpisode":{"id":100,"mediaId":10,"airingAt":123456789,"timeUntilAiring":3600,"episode":14},"mediaListEntry":{"id":200,"status":"COMPLETED"},"updatedAt":123}]}}}}""",
            graphQLResponseType(StudioMediaData::class.java),
        )

        val media = (response.data as GraphQLData.Present<*>).value as StudioMediaData
        val studioMedia = media.studio?.media
        assertEquals(1, studioMedia?.nodes?.size)
        assertEquals(10, studioMedia?.nodes?.single()?.id)
        assertEquals("Violet Evergarden", studioMedia?.nodes?.single()?.title?.userPreferred)
        assertEquals(false, studioMedia?.pageInfo?.hasNextPage)
    }

    @Test
    fun `decodes studio errors`() {
        val response = codec.decodeResponse<GraphQLResponse<StudioBaseData>>(
            """{"errors":[{"message":"Boom"}]}""",
            graphQLResponseType(StudioBaseData::class.java),
        )

        assertTrue(response.data is GraphQLData.Absent)
        assertEquals("Boom", response.errors?.firstOrNull()?.message)
    }

    private fun graphQLResponseType(typeArgument: Type): ParameterizedType = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(typeArgument)

        override fun getRawType(): Type = GraphQLResponse::class.java

        override fun getOwnerType(): Type? = null
    }
}
