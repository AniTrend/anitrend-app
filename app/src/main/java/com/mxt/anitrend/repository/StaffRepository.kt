package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.data.mapper.toStaffRecord
import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffBase
import com.mxt.anitrend.graphql.generated.StaffBaseData
import com.mxt.anitrend.graphql.generated.StaffCharacters
import com.mxt.anitrend.graphql.generated.StaffCharactersData
import com.mxt.anitrend.graphql.generated.StaffMedia
import com.mxt.anitrend.graphql.generated.StaffMediaData
import com.mxt.anitrend.graphql.generated.StaffOverview
import com.mxt.anitrend.graphql.generated.StaffOverviewData
import com.mxt.anitrend.graphql.generated.StaffRoles
import com.mxt.anitrend.graphql.generated.StaffRolesData
import com.mxt.anitrend.model.api.retro.anilist.StaffService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import com.mxt.anitrend.model.entity.base.StaffBase as StaffEntity

class StaffRepository(
    private val staffService: StaffService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    suspend fun getStaffBase(id: Long): Result<StaffRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffBase.request(id = id.toInt())
            val response = staffService.getStaffBase(request)
            if (response.isSuccessful) {
                handleStaffBase(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStaffBase(body: GraphQLResponse<StaffBaseData>): StaffRecord {
        val data = handleGraphQLResponse(body)
        return data.staff?.toStaffRecord() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getStaffOverview(id: Long, asHtml: Boolean = false): Result<StaffEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffOverview.request(id = id.toInt(), asHtml = asHtml)
            val response = staffService.getStaffOverview(request)
            if (response.isSuccessful) {
                handleStaffOverview(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStaffOverview(body: GraphQLResponse<StaffOverviewData>): StaffEntity {
        val data = handleGraphQLResponse(body)
        return data.toStaffEntity()
    }

    suspend fun getStaffCharacters(
        id: Long,
        onList: Boolean? = null,
        page: Int? = null,
        sort: List<MediaSort>? = null,
    ): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffCharacters.request(id = id.toInt(), onList = onList, page = page, sort = sort)
            val response = staffService.getStaffCharacters(request)
            if (response.isSuccessful) {
                handleStaffCharacters(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStaffCharacters(body: GraphQLResponse<StaffCharactersData>): ConnectionContainer<EdgeContainer<MediaEdge>> {
        val data = handleGraphQLResponse(body)
        return data.toStaffCharactersConnection()
    }

    suspend fun getStaffMedia(
        id: Long,
        onList: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        type: MediaType? = null,
    ): Result<ConnectionContainer<PageContainer<MediaEntity>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffMedia.request(id = id.toInt(), onList = onList, page = page, perPage = perPage, sort = sort, type = type)
            val response = staffService.getStaffMedia(request)
            if (response.isSuccessful) {
                handleStaffMedia(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStaffMedia(body: GraphQLResponse<StaffMediaData>): ConnectionContainer<PageContainer<MediaEntity>> {
        val data = handleGraphQLResponse(body)
        return data.toStaffMediaConnection()
    }

    suspend fun getStaffRoles(
        id: Long,
        onList: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        type: MediaType? = null,
    ): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffRoles.request(id = id.toInt(), onList = onList, page = page, perPage = perPage, sort = sort, type = type)
            val response = staffService.getStaffRoles(request)
            if (response.isSuccessful) {
                handleStaffRoles(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStaffRoles(body: GraphQLResponse<StaffRolesData>): ConnectionContainer<EdgeContainer<MediaEdge>> {
        val data = handleGraphQLResponse(body)
        return data.toStaffRolesConnection()
    }
}

// Generated response mapping to the legacy entity surface (Phase 2).
//
// Maps operation data at the repository boundary into the entity containers the
// repository has always exposed, preserving page-info, edge, and null handling
// of the previous AniListContainer decode path.

internal fun StaffOverviewData.toStaffEntity(): StaffEntity = staff?.toStaffEntity() ?: throw IllegalStateException("Empty response body")

internal fun StaffOverviewData.Staff.toStaffEntity(): StaffEntity = StaffEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.toImageBase()
    entity.isFavourite = isFavourite
    entity.description = description
    entity.language = language?.name
    entity.siteUrl = siteUrl
}

internal fun StaffCharactersData.toStaffCharactersConnection(): ConnectionContainer<EdgeContainer<MediaEdge>> {
    val media = staff?.characterMedia ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<EdgeContainer<MediaEdge>>().also { connection ->
        connection.connection = EdgeContainer<MediaEdge>().also { edgeContainer ->
            edgeContainer.edges = media.edges.orEmpty().mapNotNull { edge -> edge?.toMediaEdge() }
            media.pageInfo?.let { edgeContainer.pageInfo = it.toPageInfo() }
        }
    }
}

internal fun StaffCharactersData.StaffCharacterMediaEdges.toMediaEdge(): MediaEdge = MediaEdge().also { edge ->
    edge.characters = characters?.mapNotNull { character -> character?.toCharacterBase() }
    node?.let { edge.node = it.toMediaEntity() }
}

internal fun StaffCharactersData.StaffCharacterMediaEdgesCharacters.toCharacterBase(): CharacterBase = CharacterBase().also { character ->
    character.id = id.toLong()
    character.name = name?.toTitleBase()
    character.image = image?.toImageBase()
    character.isFavourite = isFavourite
    character.siteUrl = siteUrl
}

internal fun StaffCharactersData.StaffCharacterMediaEdgesNode.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
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

internal fun StaffMediaData.toStaffMediaConnection(): ConnectionContainer<PageContainer<MediaEntity>> {
    val media = staff?.staffMedia ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<PageContainer<MediaEntity>>().also { connection ->
        connection.connection = media.toStaffMediaPage()
    }
}

internal fun StaffMediaData.StaffStaffMedia.toStaffMediaPage(): PageContainer<MediaEntity> = PageContainer<MediaEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toMediaEntity() }
    pageInfo?.let { page.pageInfo = it.toPageInfo() }
}

internal fun StaffMediaData.StaffStaffMediaNodes.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
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

internal fun StaffRolesData.toStaffRolesConnection(): ConnectionContainer<EdgeContainer<MediaEdge>> {
    val media = staff?.staffMedia ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<EdgeContainer<MediaEdge>>().also { connection ->
        connection.connection = EdgeContainer<MediaEdge>().also { edgeContainer ->
            edgeContainer.edges = media.edges.orEmpty().mapNotNull { edge -> edge?.toMediaEdge() }
            media.pageInfo?.let { edgeContainer.pageInfo = it.toPageInfo() }
        }
    }
}

internal fun StaffRolesData.StaffStaffMediaEdges.toMediaEdge(): MediaEdge = MediaEdge().also { edge ->
    edge.staffRole = staffRole
    node?.let { edge.node = it.toMediaEntity() }
}

internal fun StaffRolesData.StaffStaffMediaEdgesNode.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
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

private fun StaffOverviewData.StaffName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { value -> value },
)

private fun StaffCharactersData.StaffCharacterMediaEdgesCharactersName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { value -> value },
)

private fun StaffCharactersData.StaffCharacterMediaEdgesNodeTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun StaffMediaData.StaffStaffMediaNodesTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun StaffRolesData.StaffStaffMediaEdgesNodeTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun StaffOverviewData.StaffImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

private fun StaffCharactersData.StaffCharacterMediaEdgesCharactersImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

private fun StaffCharactersData.StaffCharacterMediaEdgesNodeCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun StaffMediaData.StaffStaffMediaNodesCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun StaffRolesData.StaffStaffMediaEdgesNodeCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun StaffCharactersData.StaffCharacterMediaEdgesNodeStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun StaffCharactersData.StaffCharacterMediaEdgesNodeEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun StaffMediaData.StaffStaffMediaNodesStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun StaffMediaData.StaffStaffMediaNodesEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun StaffRolesData.StaffStaffMediaEdgesNodeStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun StaffRolesData.StaffStaffMediaEdgesNodeEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun StaffCharactersData.StaffCharacterMediaEdgesNodeNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun StaffMediaData.StaffStaffMediaNodesNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun StaffRolesData.StaffStaffMediaEdgesNodeNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun StaffCharactersData.StaffCharacterMediaEdgesNodeMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun StaffMediaData.StaffStaffMediaNodesMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun StaffRolesData.StaffStaffMediaEdgesNodeMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun StaffCharactersData.StaffCharacterMediaPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun StaffMediaData.StaffStaffMediaPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun StaffRolesData.StaffStaffMediaPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}
