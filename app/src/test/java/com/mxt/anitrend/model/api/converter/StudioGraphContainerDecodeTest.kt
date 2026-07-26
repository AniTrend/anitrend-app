package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLJson
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioMediaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class StudioGraphContainerDecodeTest {

    private val json = KotlinxGraphQLJson()

    @Test
    fun `decodes studio base data`() {
        val container = json.decode<GraphContainer<StudioBaseData>>(
            """{"data":{"Studio":{"id":1,"name":"Kyoto Animation","isAnimationStudio":true,"isFavourite":false,"siteUrl":"https://anilist.co/studio/1"}}}""",
            graphContainerType(StudioBaseData::class.java),
        )

        val studio = container.data?.studio
        assertEquals(1, studio?.id)
        assertEquals("Kyoto Animation", studio?.name)
        assertEquals(true, studio?.isAnimationStudio)
        assertEquals(false, studio?.isFavourite)
        assertEquals("https://anilist.co/studio/1", studio?.siteUrl)
    }

    @Test
    fun `decodes studio media data`() {
        val container = json.decode<GraphContainer<StudioMediaData>>(
            """{"data":{"Studio":{"media":{"pageInfo":{"total":1,"perPage":20,"currentPage":1,"lastPage":1,"hasNextPage":false},"nodes":[{"id":10,"title":{"romaji":"Violet Evergarden","english":"Violet Evergarden","native":"ヴァイオレット・エヴァーガーデン","userPreferred":"Violet Evergarden"},"coverImage":{"extraLarge":"extra.jpg","large":"large.jpg","medium":"medium.jpg","color":"#fff"},"bannerImage":"banner.jpg","type":"ANIME","format":"TV","season":"WINTER","status":"FINISHED","siteUrl":"https://anilist.co/anime/10","meanScore":84,"averageScore":85,"startDate":{"year":2018,"month":1,"day":11},"endDate":{"year":2018,"month":4,"day":5},"episodes":13,"chapters":null,"volumes":null,"isAdult":false,"isFavourite":true,"nextAiringEpisode":{"id":100,"mediaId":10,"airingAt":123456789,"timeUntilAiring":3600,"episode":14},"mediaListEntry":{"id":200,"status":"COMPLETED"},"updatedAt":123}]}}}}""",
            graphContainerType(StudioMediaData::class.java),
        )

        val media = container.data?.studio?.media
        assertEquals(1, media?.nodes?.size)
        assertEquals(10, media?.nodes?.single()?.id)
        assertEquals("Violet Evergarden", media?.nodes?.single()?.title?.userPreferred)
        assertEquals(false, media?.pageInfo?.hasNextPage)
    }

    @Test
    fun `decodes studio errors`() {
        val container = json.decode<GraphContainer<StudioBaseData>>(
            """{"errors":[{"message":"Boom"}]}""",
            graphContainerType(StudioBaseData::class.java),
        )

        assertNull(container.data)
        assertEquals("Boom", container.errors?.firstOrNull()?.message)
    }

    private fun graphContainerType(typeArgument: Type): ParameterizedType = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(typeArgument)

        override fun getRawType(): Type = GraphContainer::class.java

        override fun getOwnerType(): Type? = null
    }
}
