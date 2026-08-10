package com.mxt.anitrend.repository.mapper

import com.mxt.anitrend.graphql.generated.MediaBaseData
import com.mxt.anitrend.graphql.generated.MediaCharactersData
import com.mxt.anitrend.graphql.generated.MediaEpisodesData
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaRelationsData
import com.mxt.anitrend.graphql.generated.MediaSocialData
import com.mxt.anitrend.graphql.generated.MediaStaffData
import com.mxt.anitrend.graphql.generated.MediaStatsData
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaStats
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer
import com.mxt.anitrend.model.entity.anilist.meta.ScoreDistribution
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.model.entity.base.MediaBase as MediaBaseEntity
import com.mxt.anitrend.model.entity.anilist.Media as MediaEntity

/**
 * Maps the generated media GraphQL data types back to the legacy mutable entity
 * lane at the repository boundary.
 *
 * Covers the exact field set requested by the media detail queries
 * (`MediaBase.graphql`, `MediaOverview.graphql`, `MediaStats.graphql`,
 * `MediaRelations.graphql`, `MediaEpisodes.graphql`, `MediaCharacters.graphql`,
 * `MediaStaff.graphql`, `MediaSocial.graphql`) and their shared fragments.
 * Generated Int ids and timestamps are widened to entity Longs, generated enums
 * are exposed as their serialized `name` (matching the legacy String-backed
 * entity fields), and nullable optional blocks are preserved as null. Null list
 * elements are dropped via `mapNotNull`, following the established node-list
 * mapping convention. Null roots (absent `media`/`page`) throw
 * `IllegalStateException("Empty response body")`, matching the legacy
 * `handleGraphResponse` semantics.
 */

fun MediaBaseData.toMediaBaseEntity(): MediaBaseEntity = media?.toMediaBaseEntity() ?: throw IllegalStateException("Empty response body")

fun MediaBaseData.Media.toMediaBaseEntity(): MediaBaseEntity = MediaBaseEntity().also { entity ->
    entity.id = id.toLong()
    entity.idMal = idMal?.toLong() ?: 0
    entity.title = title?.toMediaTitle()
    entity.bannerImage = bannerImage
    entity.type = type?.name
    entity.isFavourite = isFavourite
    entity.mediaListEntry = mediaListEntry?.toMediaListEntity()
    entity.siteUrl = siteUrl
}

fun MediaOverviewData.Media.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
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
    entity.duration = duration ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaListEntity()
    entity.description = description
    entity.genres = genres?.mapNotNull { value -> value }
    entity.tags = tags?.mapNotNull { tag -> tag?.toMediaTagEntity() }
    entity.trailer = trailer?.toMediaTrailer()
    entity.hashTag = hashtag
    entity.source = source?.name
    entity.studios = studios?.nodes?.let { nodes ->
        ConnectionContainer<List<StudioBase>>().also { connection ->
            connection.connection = nodes.mapNotNull { node -> node?.toStudioEntity() }
        }
    }
}

fun MediaStatsData.Media.toMediaStatsEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.type = type?.name
    entity.externalLinks = externalLinks?.mapNotNull { link -> link?.toExternalLink() }
    entity.stats = stats?.toMediaStats()
    entity.rankings = rankings?.mapNotNull { ranking -> ranking?.toMediaRank() }
}

fun MediaRelationsData.toMediaRelationsConnection(): ConnectionContainer<EdgeContainer<MediaEdge>> {
    val relations = media?.relations
        ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<EdgeContainer<MediaEdge>>().also { connection ->
        connection.connection = EdgeContainer<MediaEdge>().also { edgeContainer ->
            edgeContainer.edges = relations.edges.orEmpty().mapNotNull { edge -> edge?.toMediaEdge() }
            relations.pageInfo?.toPageInfo()?.let { pageInfo -> edgeContainer.pageInfo = pageInfo }
        }
    }
}

fun MediaEpisodesData.toMediaEpisodesConnection(): ConnectionContainer<List<ExternalLink>> {
    val media = media ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<List<ExternalLink>>().also { connection ->
        connection.connection = media.externalLinks.orEmpty().mapNotNull { link -> link?.toExternalLink() }
    }
}

fun MediaCharactersData.toMediaCharactersConnection(): ConnectionContainer<EdgeContainer<CharacterEdge>> {
    val characters = media?.characters
        ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<EdgeContainer<CharacterEdge>>().also { connection ->
        connection.connection = EdgeContainer<CharacterEdge>().also { edgeContainer ->
            edgeContainer.edges = characters.edges.orEmpty().mapNotNull { edge -> edge?.toCharacterEdge() }
            characters.pageInfo?.toPageInfo()?.let { pageInfo -> edgeContainer.pageInfo = pageInfo }
        }
    }
}

fun MediaStaffData.toMediaStaffConnection(): ConnectionContainer<EdgeContainer<StaffEdge>> {
    val staff = media?.staff
        ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<EdgeContainer<StaffEdge>>().also { connection ->
        connection.connection = EdgeContainer<StaffEdge>().also { edgeContainer ->
            edgeContainer.edges = staff.edges.orEmpty().mapNotNull { edge -> edge?.toStaffEdge() }
            staff.pageInfo?.toPageInfo()?.let { pageInfo -> edgeContainer.pageInfo = pageInfo }
        }
    }
}

fun MediaSocialData.toFeedPage(): PageContainer<FeedList> {
    val page = page ?: throw IllegalStateException("Empty response body")
    return PageContainer<FeedList>().also { container ->
        container.pageData = page.activities.orEmpty().mapNotNull { activity ->
            (activity as? MediaSocialData.PageActivities.ListActivity)?.toFeedList()
        }
        page.pageInfo?.toPageInfo()?.let { pageInfo -> container.pageInfo = pageInfo }
    }
}

private fun MediaBaseData.MediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = null,
    englishRaw = null,
    originalRaw = null,
    userPreferredRaw = userPreferred,
)

private fun MediaOverviewData.MediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaSocialData.ListActivityPageActivitiesMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaOverviewData.MediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaSocialData.ListActivityPageActivitiesMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaOverviewData.MediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaOverviewData.MediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaSocialData.ListActivityPageActivitiesMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaSocialData.ListActivityPageActivitiesMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaOverviewData.MediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaSocialData.ListActivityPageActivitiesMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaBaseData.MediaMediaListEntry.toMediaListEntity(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun MediaOverviewData.MediaMediaListEntry.toMediaListEntity(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun MediaSocialData.ListActivityPageActivitiesMediaMediaListEntry.toMediaListEntity(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry.toMediaListEntity(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun MediaOverviewData.MediaTags.toMediaTagEntity(): MediaTag = MediaTag(
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isAdult = isAdult ?: false,
).also { mediaTag ->
    mediaTag.id = id.toLong()
}

private fun MediaOverviewData.MediaTrailer.toMediaTrailer(): MediaTrailer = MediaTrailer(
    id = id,
    site = site,
)

private fun MediaOverviewData.MediaStudiosNodes.toStudioEntity(): StudioBase = StudioBase().also { studio ->
    studio.id = id.toLong()
    studio.name = name
    studio.siteUrl = siteUrl
    studio.isFavourite = isFavourite
}

private fun MediaStatsData.MediaExternalLinks.toExternalLink(): ExternalLink = ExternalLink(
    url = url,
    site = site,
).also { link ->
    link.id = id
}

private fun MediaEpisodesData.MediaExternalLinks.toExternalLink(): ExternalLink = ExternalLink(
    url = url,
    site = site,
).also { link ->
    link.id = id
}

private fun MediaStatsData.MediaStats.toMediaStats(): MediaStats = MediaStats(
    scoreDistribution = scoreDistribution?.mapNotNull { distribution ->
        distribution?.let { ScoreDistribution(score = it.score ?: 0, amount = it.amount ?: 0) }
    },
    statusDistribution = statusDistribution?.mapNotNull { distribution ->
        distribution?.let { StatusDistribution(status = it.status?.name, amount = it.amount ?: 0) }
    },
)

private fun MediaStatsData.MediaRankings.toMediaRank(): MediaRank = MediaRank(
    id = id,
    rank = rank,
    type = type.name,
    format = format.name,
    year = year ?: 0,
    season = season?.name,
    isAllTime = allTime ?: false,
    context = context,
)

private fun MediaRelationsData.MediaRelationsEdges.toMediaEdge(): MediaEdge? {
    val mediaNode = node ?: return null
    return MediaEdge().also { edge ->
        edge.relationType = relationType?.name
        edge.node = mediaNode.toMediaBaseEntity()
    }
}

private fun MediaSocialData.ListActivityPageActivitiesMedia.toMediaBaseEntity(): MediaBaseEntity = MediaBaseEntity().also { entity ->
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
    entity.mediaListEntry = mediaListEntry?.toMediaListEntity()
}

private fun MediaRelationsData.MediaRelationsEdgesNode.toMediaBaseEntity(): MediaBaseEntity = MediaBaseEntity().also { entity ->
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
    entity.mediaListEntry = mediaListEntry?.toMediaListEntity()
}

private fun MediaCharactersData.MediaCharactersEdges.toCharacterEdge(): CharacterEdge? {
    val characterNode = node ?: return null
    return CharacterEdge().also { edge ->
        edge.role = role?.name
        edge.node = characterNode.toCharacterBaseEntity()
    }
}

private fun MediaCharactersData.MediaCharactersEdgesNode.toCharacterBaseEntity(): CharacterBase = CharacterBase().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.toImageBase()
    entity.isFavourite = isFavourite
    entity.siteUrl = siteUrl
}

private fun MediaCharactersData.MediaCharactersEdgesNodeName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { value -> value },
)

private fun MediaCharactersData.MediaCharactersEdgesNodeImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

private fun MediaStaffData.MediaStaffEdges.toStaffEdge(): StaffEdge? {
    val staffNode = node ?: return null
    return StaffEdge().also { edge ->
        edge.role = role
        edge.node = staffNode.toStaffBaseEntity()
    }
}

private fun MediaStaffData.MediaStaffEdgesNode.toStaffBaseEntity(): StaffBase = StaffBase().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.toImageBase()
    entity.isFavourite = isFavourite
    entity.language = language?.name
    entity.siteUrl = siteUrl
}

private fun MediaStaffData.MediaStaffEdgesNodeName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { value -> value },
)

private fun MediaStaffData.MediaStaffEdgesNodeImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

private fun MediaSocialData.PageActivities.ListActivity.toFeedList(): FeedList = FeedList(
    id = id.toLong(),
    replyCount = replyCount,
    type = type?.name,
    status = status,
    text = progress,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    media = media?.toMediaBaseEntity(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
    siteUrl = siteUrl,
).also { feed ->
    feed.replies = replies?.mapNotNull { reply -> reply?.toFeedReply() }
}

private fun MediaSocialData.ListActivityPageActivitiesReplies.toFeedReply(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

private fun MediaSocialData.ListActivityPageActivitiesUser.toUserBase(): UserBase = toUserBase(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

private fun MediaSocialData.ListActivityPageActivitiesLikes.toUserBase(): UserBase = toUserBase(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

private fun MediaSocialData.ListActivityPageActivitiesRepliesUser.toUserBase(): UserBase = toUserBase(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

private fun MediaSocialData.ListActivityPageActivitiesRepliesLikes.toUserBase(): UserBase = toUserBase(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

private fun toUserBase(
    id: Int,
    name: String,
    bannerImage: String?,
    isFollowing: Boolean?,
    avatarLarge: String?,
    avatarMedium: String?,
): UserBase = UserBase(
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing ?: false,
).also { user ->
    user.id = id.toLong()
    user.avatar = ImageBase(
        extraLarge = null,
        large = avatarLarge,
        medium = avatarMedium,
    )
}

private fun MediaRelationsData.MediaRelationsPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun MediaCharactersData.MediaCharactersPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun MediaStaffData.MediaStaffPageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun MediaSocialData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}
