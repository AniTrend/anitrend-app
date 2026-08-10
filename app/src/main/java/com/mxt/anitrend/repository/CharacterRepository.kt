package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.data.mapper.toCharacterRecord
import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.graphql.generated.CharacterActors
import com.mxt.anitrend.graphql.generated.CharacterActorsData
import com.mxt.anitrend.graphql.generated.CharacterBase
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterMedia
import com.mxt.anitrend.graphql.generated.CharacterMediaData
import com.mxt.anitrend.graphql.generated.CharacterOverview
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffSort
import com.mxt.anitrend.model.api.retro.anilist.CharacterService
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.mapper.toMediaCharacterEntity
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity

class CharacterRepository(
    private val characterService: CharacterService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    suspend fun getCharacterBase(id: Long): Result<CharacterRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterBase.request(id = id.toInt())
            val response = characterService.getCharacterBase(request)
            if (response.isSuccessful) {
                handleCharacterBase(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterBase(body: GraphQLResponse<CharacterBaseData>): CharacterRecord {
        val data = handleGraphQLResponse(body)
        return data.character?.toCharacterRecord() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getCharacterOverview(id: Long, asHtml: Boolean = false): Result<MediaCharacter> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterOverview.request(id = id.toInt(), asHtml = asHtml)
            val response = characterService.getCharacterOverview(request)
            if (response.isSuccessful) {
                handleCharacterOverview(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterOverview(body: GraphQLResponse<CharacterOverviewData>): MediaCharacter {
        val data = handleGraphQLResponse(body)
        return data.toMediaCharacterEntity()
    }

    suspend fun getCharacterMedia(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        type: MediaType? = null,
    ): Result<ConnectionContainer<PageContainer<MediaEntity>>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterMedia.request(id = id.toInt(), page = page, perPage = perPage, sort = sort, type = type)
            val response = characterService.getCharacterMedia(request)
            if (response.isSuccessful) {
                handleCharacterMedia(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterMedia(body: GraphQLResponse<CharacterMediaData>): ConnectionContainer<PageContainer<MediaEntity>> {
        val data = handleGraphQLResponse(body)
        return data.toCharacterMediaConnection()
    }

    suspend fun getCharacterActors(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<StaffSort>? = null,
    ): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterActors.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = characterService.getCharacterActors(request)
            if (response.isSuccessful) {
                handleCharacterActors(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterActors(body: GraphQLResponse<CharacterActorsData>): ConnectionContainer<EdgeContainer<MediaEdge>> {
        val data = handleGraphQLResponse(body)
        return data.toCharacterActorsConnection()
    }
}

// Generated response mapping to the legacy entity surface (Phase 2).
//
// Maps operation data at the repository boundary into the entity containers the
// repository has always exposed, preserving page-info, edge, and null handling
// of the previous AniListContainer decode path.

internal fun CharacterMediaData.toCharacterMediaConnection(): ConnectionContainer<PageContainer<MediaEntity>> {
    val media = character?.media ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<PageContainer<MediaEntity>>().also { connection ->
        connection.connection = media.toCharacterMediaPage()
    }
}

internal fun CharacterMediaData.CharacterMedia.toCharacterMediaPage(): PageContainer<MediaEntity> = PageContainer<MediaEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toMediaEntity() }
    pageInfo?.let { page.pageInfo = it.toPageInfo() }
}

internal fun CharacterMediaData.CharacterMediaNodes.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.id = id.toLong()
    entity.title = title?.toMediaTitle()
    entity.coverImage = coverImage?.toImageBase()
    entity.bannerImage = bannerImage
    entity.type = type?.name
    entity.format = format?.name
    entity.season = season?.name
    entity.status = status?.name
    entity.siteUrl = siteUrl
    entity.meanScore = meanScore ?: 0
    entity.averageScore = averageScore ?: 0
    entity.startDate = startDate?.toFuzzyDate()
    entity.endDate = endDate?.toFuzzyDate()
    entity.episodes = episodes ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaList()
}

internal fun CharacterActorsData.toCharacterActorsConnection(): ConnectionContainer<EdgeContainer<MediaEdge>> {
    val media = character?.media ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<EdgeContainer<MediaEdge>>().also { connection ->
        connection.connection = EdgeContainer<MediaEdge>().also { edgeContainer ->
            edgeContainer.edges = media.edges.orEmpty().mapNotNull { edge -> edge?.toMediaEdge() }
            media.pageInfo?.let { edgeContainer.pageInfo = it.toPageInfo() }
        }
    }
}

internal fun CharacterActorsData.CharacterMediaEdges.toMediaEdge(): MediaEdge = MediaEdge().also { edge ->
    edge.characterRole = characterRole?.name
    edge.voiceActors = voiceActors?.mapNotNull { voiceActor -> voiceActor?.toStaffBase() }
    node?.let { edge.node = it.toMediaEntity() }
}

internal fun CharacterActorsData.CharacterMediaEdgesNode.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.id = id.toLong()
    entity.title = title?.toMediaTitle()
    entity.coverImage = coverImage?.toImageBase()
    entity.bannerImage = bannerImage
    entity.type = type?.name
    entity.format = format?.name
    entity.season = season?.name
    entity.status = status?.name
    entity.siteUrl = siteUrl
    entity.meanScore = meanScore ?: 0
    entity.averageScore = averageScore ?: 0
    entity.startDate = startDate?.toFuzzyDate()
    entity.endDate = endDate?.toFuzzyDate()
    entity.episodes = episodes ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaList()
}

private fun CharacterActorsData.CharacterMediaEdgesVoiceActors.toStaffBase(): StaffBase = StaffBase().also { staff ->
    staff.id = id.toLong()
    staff.name = name?.toTitleBase()
    staff.image = image?.toImageBase()
    staff.isFavourite = isFavourite
    staff.language = language?.name
    staff.siteUrl = siteUrl
}

private fun CharacterMediaData.CharacterMediaNodesTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun CharacterActorsData.CharacterMediaEdgesNodeTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun CharacterActorsData.CharacterMediaEdgesVoiceActorsName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { value -> value },
)

private fun CharacterMediaData.CharacterMediaNodesCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun CharacterActorsData.CharacterMediaEdgesNodeCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun CharacterActorsData.CharacterMediaEdgesVoiceActorsImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

private fun CharacterMediaData.CharacterMediaNodesStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun CharacterMediaData.CharacterMediaNodesEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun CharacterActorsData.CharacterMediaEdgesNodeStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun CharacterActorsData.CharacterMediaEdgesNodeEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun CharacterMediaData.CharacterMediaNodesNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun CharacterActorsData.CharacterMediaEdgesNodeNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun CharacterMediaData.CharacterMediaNodesMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun CharacterActorsData.CharacterMediaEdgesNodeMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun CharacterMediaData.CharacterMediaPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun CharacterActorsData.CharacterMediaPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}
