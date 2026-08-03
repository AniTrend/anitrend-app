package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListCollection
import com.mxt.anitrend.graphql.generated.MediaListCollectionData
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseMediaListCollectionRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(BrowseService::class.java)
    private val queryKey = MediaListQueryKey(
        userId = 42L,
        userName = null,
        mediaType = MediaType.ANIME,
        statuses = setOf(MediaListStatus.CURRENT),
        sort = MediaListSort.PROGRESS_DESC,
    )

    @Test
    fun `getMediaListCollection success maps flattened entries preserving order and commits to store`() = runTest {
        val mediaListStore = InMemoryMediaListStore()
        val repository = BrowseRepository(
            browseService = service,
            ioDispatcher = testDispatcher,
            mediaListStore = mediaListStore,
        )
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = collectionData(
                        listOf(
                            list(entries = listOf(entry(id = 1, mediaId = 100), entry(id = 2, mediaId = 200))),
                            list(entries = listOf(entry(id = 3, mediaId = 300))),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
            queryKey = queryKey,
            readToken = 7L,
        )

        assertTrue(result.isSuccess)
        val page: MediaListCollectionPageResult = result.getOrThrow()
        assertEquals(listOf(1L, 2L, 3L), page.entries.map { it.id })
        assertEquals(100L, page.entries.first().mediaId)
        assertEquals("CURRENT", page.entries.first().status)
        assertEquals(8.0, page.entries.first().score, 0.0)
        assertEquals(5, page.entries.first().progress)
        assertEquals("ANIME", page.entries.first().media?.type)
        assertNull(page.pageInfo)

        val committed = mediaListStore.observeQuery(queryKey).first()
        assertEquals(listOf(1L, 2L, 3L), committed.entries.map { it.id })
        assertEquals(7L, committed.entries.first().revision)
        assertEquals(42L, committed.entries.first().ownerUserId)
        assertEquals("https://cover-xl", committed.entries.first().media?.coverImage)
    }

    @Test
    fun `getMediaListCollection parses custom lists and advanced scores JSON scalars`() = runTest {
        val repository = BrowseRepository(browseService = service, ioDispatcher = testDispatcher)
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = collectionData(
                        listOf(
                            list(
                                entries = listOf(
                                    entry(
                                        id = 1,
                                        mediaId = 100,
                                        customLists = buildJsonArray {
                                            add("Favourites")
                                            add("Rewatch")
                                        },
                                        advancedScores = buildJsonObject {
                                            put("Story", 9.5)
                                            put("Art", 8.0)
                                        },
                                        notes = "note",
                                    ),
                                ),
                            ),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )

        assertTrue(result.isSuccess)
        val page: MediaListCollectionPageResult = result.getOrThrow()
        val record = page.entries.single()
        assertEquals(listOf("Favourites", "Rewatch"), record.customLists)
        assertEquals(mapOf("Story" to 9.5, "Art" to 8.0), record.advancedScores)
        assertEquals("note", record.notes)
        assertNull(record.scoreRaw)
    }

    @Test
    fun `getMediaListCollection degrades null wrong-shape and non-string non-numeric JSON scalars`() = runTest {
        val repository = BrowseRepository(browseService = service, ioDispatcher = testDispatcher)
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = collectionData(
                        listOf(
                            list(
                                entries = listOf(
                                    entry(
                                        id = 1,
                                        mediaId = 100,
                                        customLists = null,
                                        advancedScores = null,
                                    ),
                                    entry(
                                        id = 2,
                                        mediaId = 200,
                                        customLists = JsonPrimitive("not-an-array"),
                                        advancedScores = buildJsonArray { add(1) },
                                    ),
                                    entry(
                                        id = 3,
                                        mediaId = 300,
                                        customLists = buildJsonArray {
                                            add("Favourites")
                                            add(5)
                                            add(JsonNull)
                                            add("Rewatch")
                                        },
                                        advancedScores = buildJsonObject {
                                            put("Story", JsonPrimitive("nope"))
                                            put("Art", 8.0)
                                            put("Null", JsonNull)
                                        },
                                    ),
                                ),
                            ),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )

        assertTrue(result.isSuccess)
        val records = result.getOrThrow().entries
        assertEquals(emptyList<String>(), records[0].customLists)
        assertEquals(emptyMap<String, Double>(), records[0].advancedScores)
        assertEquals(emptyList<String>(), records[1].customLists)
        assertEquals(emptyMap<String, Double>(), records[1].advancedScores)
        assertEquals(listOf("Favourites", "Rewatch"), records[2].customLists)
        assertEquals(mapOf("Story" to 0.0, "Art" to 8.0, "Null" to 0.0), records[2].advancedScores)
    }

    @Test
    fun `getMediaListCollection skips null lists and null entries while preserving order`() = runTest {
        val repository = BrowseRepository(browseService = service, ioDispatcher = testDispatcher)
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = collectionData(
                        listOf(
                            null,
                            list(entries = listOf(entry(id = 1, mediaId = 100), null, entry(id = 2, mediaId = 200))),
                            list(entries = null),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )

        assertTrue(result.isSuccess)
        assertEquals(listOf(1L, 2L), result.getOrThrow().entries.map { it.id })
    }

    @Test
    fun `getMediaListCollection GraphQL error returns failed Result with message`() = runTest {
        val repository = BrowseRepository(browseService = service, ioDispatcher = testDispatcher)
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaListCollectionData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media list collection failed")),
                ),
            ),
        )

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )

        assertTrue(result.isFailure)
        assertEquals("Media list collection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaListCollection empty body returns failed Result`() = runTest {
        val repository = BrowseRepository(browseService = service, ioDispatcher = testDispatcher)
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaListCollection null root collection returns failed Result`() = runTest {
        val repository = BrowseRepository(browseService = service, ioDispatcher = testDispatcher)
        val call = collectionCall()
        `when`(service.getMediaListCollection(collectionRequest())).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaListCollectionData>(
                    data = MediaListCollectionData(mediaListCollection = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaListCollection(
            userId = 42L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectionCall(): Call<GraphContainer<MediaListCollectionData>> = mock(Call::class.java) as Call<GraphContainer<MediaListCollectionData>>

    private fun collectionRequest() = MediaListCollection.request(
        userId = 42,
        userName = null,
        type = MediaType.ANIME,
        forceSingleCompletedList = true,
        sort = listOf(MediaListSort.PROGRESS_DESC),
        statusIn = listOf(MediaListStatus.CURRENT),
        scoreFormat = ScoreFormat.POINT_100,
    )

    private fun collectionData(
        lists: List<MediaListCollectionData.MediaListCollectionLists?>,
    ): MediaListCollectionData = MediaListCollectionData(
        mediaListCollection = MediaListCollectionData.MediaListCollection(lists = lists),
    )

    private fun list(
        entries: List<MediaListCollectionData.MediaListCollectionListsEntries?>?,
    ): MediaListCollectionData.MediaListCollectionLists = MediaListCollectionData.MediaListCollectionLists(
        entries = entries,
        isCustomList = false,
        isSplitCompletedList = false,
        name = null,
        status = MediaListStatus.CURRENT,
    )

    private fun entry(
        id: Int,
        mediaId: Int,
        status: MediaListStatus = MediaListStatus.CURRENT,
        score: Double? = 8.0,
        progress: Int? = 5,
        progressVolumes: Int? = 0,
        repeat: Int? = 0,
        priority: Int? = 0,
        privateValue: Boolean? = false,
        hiddenFromStatusLists: Boolean? = false,
        customLists: JsonElement? = null,
        advancedScores: JsonElement? = null,
        notes: String? = null,
    ): MediaListCollectionData.MediaListCollectionListsEntries = MediaListCollectionData.MediaListCollectionListsEntries(
        advancedScores = advancedScores,
        completedAt = null,
        customLists = customLists,
        hiddenFromStatusLists = hiddenFromStatusLists,
        id = id,
        media = media(id = mediaId),
        mediaId = mediaId,
        notes = notes,
        priority = priority,
        privateValue = privateValue,
        progress = progress,
        progressVolumes = progressVolumes,
        repeat = repeat,
        score = score,
        startedAt = null,
        status = status,
        updatedAt = null,
    )

    private fun media(
        id: Int,
    ): MediaListCollectionData.MediaListCollectionListsEntriesMedia = MediaListCollectionData.MediaListCollectionListsEntriesMedia(
        averageScore = 80,
        bannerImage = null,
        chapters = 0,
        coverImage = MediaListCollectionData.MediaListCollectionListsEntriesMediaCoverImage(
            color = null,
            extraLarge = "https://cover-xl",
            large = "https://cover-lg",
            medium = "https://cover-md",
        ),
        endDate = null,
        episodes = 12,
        format = MediaFormat.TV,
        id = id,
        isAdult = false,
        isFavourite = false,
        meanScore = null,
        mediaListEntry = null,
        nextAiringEpisode = null,
        season = null,
        siteUrl = "https://anilist.co/anime/$id",
        startDate = null,
        status = MediaStatus.RELEASING,
        title = MediaListCollectionData.MediaListCollectionListsEntriesMediaTitle(
            english = "English $id",
            native = "Native $id",
            romaji = "Romaji $id",
            userPreferred = "Preferred $id",
        ),
        type = MediaType.ANIME,
        updatedAt = null,
        volumes = 0,
    )
}
