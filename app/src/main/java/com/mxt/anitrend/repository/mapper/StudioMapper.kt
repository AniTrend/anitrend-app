package com.mxt.anitrend.repository.mapper

import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioMediaData
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import com.mxt.anitrend.model.entity.base.StudioBase as StudioEntity

fun StudioBaseData.toStudioEntity(): StudioEntity =
    studio?.toStudioEntity() ?: throw IllegalStateException("Empty response body")

fun StudioBaseData.Studio.toStudioEntity(): StudioEntity =
    StudioEntity().also { entity ->
        entity.id = id.toLong()
        entity.name = name
        entity.isFavourite = isFavourite
        entity.siteUrl = siteUrl
    }

fun StudioMediaData.toStudioMediaConnection(): ConnectionContainer<PageContainer<MediaEntity>> {
    val media = studio?.media ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<PageContainer<MediaEntity>>().also { connection ->
        connection.connection = media.toStudioMediaPage()
    }
}

fun StudioMediaData.StudioMedia.toStudioMediaPage(): PageContainer<MediaEntity> =
    PageContainer<MediaEntity>().also { page ->
        page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toMediaEntity() }
        pageInfo?.toPageInfo()?.let { pageInfo ->
            page.pageInfo = pageInfo
        }
    }

fun StudioMediaData.StudioMediaNodes.toMediaEntity(): MediaEntity =
    MediaEntity().also { entity ->
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

private fun StudioMediaData.StudioMediaNodesTitle.toMediaTitle(): MediaTitle =
    MediaTitle(
        romajiRaw = romaji,
        englishRaw = english,
        originalRaw = native,
        userPreferredRaw = userPreferred,
    )

private fun StudioMediaData.StudioMediaNodesCoverImage.toImageBase(): ImageBase =
    ImageBase(
        extraLarge = extraLarge,
        large = large,
        medium = medium,
    )

private fun StudioMediaData.StudioMediaNodesStartDate.toFuzzyDate(): FuzzyDate =
    FuzzyDate(
        day = day ?: 0,
        month = month ?: 0,
        year = year ?: 0,
    )

private fun StudioMediaData.StudioMediaNodesEndDate.toFuzzyDate(): FuzzyDate =
    FuzzyDate(
        day = day ?: 0,
        month = month ?: 0,
        year = year ?: 0,
    )

private fun StudioMediaData.StudioMediaNodesNextAiringEpisode.toAiringSchedule(): AiringSchedule =
    AiringSchedule(
        airingAt = airingAt.toLong(),
        timeUntilAiring = timeUntilAiring.toLong(),
        episode = episode,
    )

private fun StudioMediaData.StudioMediaNodesMediaListEntry.toMediaList(): MediaList =
    MediaList().also { mediaList ->
        mediaList.id = id.toLong()
        mediaList.status = status?.name
    }

private fun StudioMediaData.StudioMediaPageInfo.toPageInfo(): PageInfo =
    PageInfo(
        total = total ?: 0,
        perPage = perPage ?: 0,
        currentPage = currentPage ?: 0,
    ).also { pageInfo ->
        pageInfo.setHasNextPage(hasNextPage ?: false)
    }
