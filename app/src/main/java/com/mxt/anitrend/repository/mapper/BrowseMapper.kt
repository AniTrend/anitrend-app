package com.mxt.anitrend.repository.mapper

import com.mxt.anitrend.graphql.generated.DeleteMediaListEntryData
import com.mxt.anitrend.graphql.generated.DeleteReviewData
import com.mxt.anitrend.graphql.generated.MediaBrowseData
import com.mxt.anitrend.graphql.generated.MediaListBrowseData
import com.mxt.anitrend.graphql.generated.MediaListData
import com.mxt.anitrend.graphql.generated.MediaWithListData
import com.mxt.anitrend.graphql.generated.RateReviewData
import com.mxt.anitrend.graphql.generated.ReviewBrowseData
import com.mxt.anitrend.graphql.generated.SaveMediaListEntryData
import com.mxt.anitrend.graphql.generated.SaveReviewData
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import com.mxt.anitrend.model.entity.anilist.MediaList as MediaEntityList
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState

/**
 * Maps the generated browse GraphQL data types back to the legacy mutable entity
 * lane at the repository boundary.
 *
 * Covers the exact field set requested by the browse queries and mutations
 * (`MediaBrowse.graphql`, `ReviewBrowse.graphql`, `MediaListBrowse.graphql`,
 * `MediaList.graphql`, `MediaWithList.graphql`, `DeleteMediaListEntry.graphql`,
 * `DeleteReview.graphql`, `SaveMediaListEntry.graphql`, `RateReview.graphql`,
 * `SaveReview.graphql`). Generated Int ids and timestamps are widened to entity
 * Longs, generated enums are exposed as their serialized `name` (matching the
 * legacy String-backed entity fields), and nullable optional blocks are preserved
 * as null. Null list elements are dropped via `filterNotNull`, following the
 * established node-list mapping convention. Null roots (absent `page`, `media`,
 * `mediaList`, or mutation result) throw `IllegalStateException("Empty response
 * body")`, matching the legacy `handleGraphResponse` semantics.
 */

fun MediaBrowseData.toMediaBrowsePage(): PageContainer<MediaBase> {
    val page = page ?: throw IllegalStateException("Empty response body")
    return buildPageContainer(
        items = page.media.orEmpty().filterNotNull().map { it.toMediaBase() },
        pageInfo = page.pageInfo?.toPageInfo(),
    )
}

fun ReviewBrowseData.toReviewBrowsePage(): PageContainer<Review> {
    val page = page ?: throw IllegalStateException("Empty response body")
    return buildPageContainer(
        items = page.reviews.orEmpty().filterNotNull().map { it.toReview() },
        pageInfo = page.pageInfo?.toPageInfo(),
    )
}

fun MediaListBrowseData.toMediaListBrowsePage(): PageContainer<MediaEntityList> {
    val page = page ?: throw IllegalStateException("Empty response body")
    return buildPageContainer(
        items = page.mediaList.orEmpty().filterNotNull().map { it.toMediaList() },
        pageInfo = page.pageInfo?.toPageInfo(),
    )
}

fun MediaListData.toMediaListEntity(): MediaEntityList = mediaList?.toMediaList() ?: throw IllegalStateException("Empty response body")

fun MediaWithListData.toMediaBaseEntity(): MediaBase = media?.toMediaBase() ?: throw IllegalStateException("Empty response body")

fun DeleteMediaListEntryData.toDeleteState(): DeleteState = deleteMediaListEntry?.toDeleteState() ?: throw IllegalStateException("Empty response body")

fun DeleteReviewData.toDeleteState(): DeleteState = deleteReview?.toDeleteState() ?: throw IllegalStateException("Empty response body")

fun SaveMediaListEntryData.toMediaListEntity(): MediaEntityList = saveMediaListEntry?.toMediaList() ?: throw IllegalStateException("Empty response body")

fun RateReviewData.toReview(): Review = rateReview?.toReview() ?: throw IllegalStateException("Empty response body")

fun SaveReviewData.toReview(): Review = saveReview?.toReview() ?: throw IllegalStateException("Empty response body")

private fun <T> buildPageContainer(
    items: List<T>,
    pageInfo: PageInfo?,
): PageContainer<T> = PageContainer<T>().apply {
    pageData = items
    if (pageInfo != null) {
        this.pageInfo = pageInfo
    }
}

// MediaBrowse data mapping

private fun MediaBrowseData.PageMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun MediaBrowseData.PageMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaBrowseData.PageMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaBrowseData.PageMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaBrowseData.PageMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaBrowseData.PageMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaBrowseData.PageMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun MediaBrowseData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).apply {
    setHasNextPage(hasNextPage ?: false)
}

// ReviewBrowse data mapping

private fun ReviewBrowseData.PageReviews.toReview(): Review = Review().apply {
    id = this@toReview.id.toLong()
    summary = this@toReview.summary
    mediaType = this@toReview.mediaType?.name
    body = this@toReview.body
    rating = this@toReview.rating ?: 0
    ratingAmount = this@toReview.ratingAmount ?: 0
    userRating = this@toReview.userRating?.name
    score = this@toReview.score ?: 0
    isPrivate = this@toReview.privateValue ?: false
    createdAt = this@toReview.createdAt.toLong()
    user = this@toReview.user?.toUserBase() ?: UserBase()
    media = this@toReview.media?.toMediaBase() ?: MediaBase()
}

private fun ReviewBrowseData.PageReviewsMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun ReviewBrowseData.PageReviewsMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun ReviewBrowseData.PageReviewsMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun ReviewBrowseData.PageReviewsMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun ReviewBrowseData.PageReviewsMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun ReviewBrowseData.PageReviewsMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun ReviewBrowseData.PageReviewsMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun ReviewBrowseData.PageReviewsUser.toUserBase(): UserBase = UserBase(
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing ?: false,
).apply {
    id = this@toUserBase.id.toLong()
    avatar = this@toUserBase.avatar?.toImageBase()
}

private fun ReviewBrowseData.PageReviewsUserAvatar.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

private fun ReviewBrowseData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).apply {
    setHasNextPage(hasNextPage ?: false)
}

// MediaListBrowse data mapping

private fun MediaListBrowseData.PageMediaList.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    mediaId = this@toMediaList.mediaId.toLong()
    status = this@toMediaList.status?.name
    score = this@toMediaList.score?.toFloat() ?: 0f
    progress = this@toMediaList.progress ?: 0
    progressVolumes = this@toMediaList.progressVolumes ?: 0
    repeat = this@toMediaList.repeat ?: 0
    priority = this@toMediaList.priority ?: 0
    notes = this@toMediaList.notes
    isHidden = this@toMediaList.privateValue ?: false
    isHiddenFromStatusLists = this@toMediaList.hiddenFromStatusLists ?: false
    advancedScores = this@toMediaList.advancedScores.toAdvancedScores()
    customLists = this@toMediaList.customLists.toCustomLists()
    startedAt = this@toMediaList.startedAt?.toFuzzyDate()
    completedAt = this@toMediaList.completedAt?.toFuzzyDate()
    updatedAt = this@toMediaList.updatedAt?.toLong() ?: 0L
    media = this@toMediaList.media?.toMediaBase() ?: MediaBase()
}

private fun MediaListBrowseData.PageMediaListMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun MediaListBrowseData.PageMediaListMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaListBrowseData.PageMediaListMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaListBrowseData.PageMediaListMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListBrowseData.PageMediaListMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListBrowseData.PageMediaListMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaListBrowseData.PageMediaListMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun MediaListBrowseData.PageMediaListStartedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListBrowseData.PageMediaListCompletedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListBrowseData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).apply {
    setHasNextPage(hasNextPage ?: false)
}

// MediaList data mapping

private fun MediaListData.MediaList.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    mediaId = this@toMediaList.mediaId.toLong()
    status = this@toMediaList.status?.name
    score = this@toMediaList.score?.toFloat() ?: 0f
    progress = this@toMediaList.progress ?: 0
    progressVolumes = this@toMediaList.progressVolumes ?: 0
    repeat = this@toMediaList.repeat ?: 0
    priority = this@toMediaList.priority ?: 0
    notes = this@toMediaList.notes
    isHidden = this@toMediaList.privateValue ?: false
    isHiddenFromStatusLists = this@toMediaList.hiddenFromStatusLists ?: false
    advancedScores = this@toMediaList.advancedScores.toAdvancedScores()
    customLists = this@toMediaList.customLists.toCustomLists()
    startedAt = this@toMediaList.startedAt?.toFuzzyDate()
    completedAt = this@toMediaList.completedAt?.toFuzzyDate()
    updatedAt = this@toMediaList.updatedAt?.toLong() ?: 0L
    media = this@toMediaList.media?.toMediaBase() ?: MediaBase()
}

private fun MediaListData.MediaListMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun MediaListData.MediaListMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaListData.MediaListMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaListData.MediaListMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListData.MediaListMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListData.MediaListMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaListData.MediaListMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun MediaListData.MediaListStartedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaListData.MediaListCompletedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

// MediaWithList data mapping

private fun MediaWithListData.Media.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun MediaWithListData.MediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MediaWithListData.MediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaWithListData.MediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaWithListData.MediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaWithListData.MediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaWithListData.MediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    mediaId = this@toMediaList.mediaId.toLong()
    status = this@toMediaList.status?.name
    score = this@toMediaList.score?.toFloat() ?: 0f
    progress = this@toMediaList.progress ?: 0
    progressVolumes = this@toMediaList.progressVolumes ?: 0
    repeat = this@toMediaList.repeat ?: 0
    priority = this@toMediaList.priority ?: 0
    notes = this@toMediaList.notes
    isHidden = this@toMediaList.privateValue ?: false
    isHiddenFromStatusLists = this@toMediaList.hiddenFromStatusLists ?: false
    customLists = this@toMediaList.customLists.toCustomLists()
    startedAt = this@toMediaList.startedAt?.toFuzzyDate()
    completedAt = this@toMediaList.completedAt?.toFuzzyDate()
    // The query does not request a nested media object, so the entry keeps the
    // default empty media like the legacy Gson lane, letting consumers fall
    // back to the separately returned media payload.
}

private fun MediaWithListData.MediaMediaListEntryStartedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MediaWithListData.MediaMediaListEntryCompletedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

// Delete mutations data mapping

private fun DeleteMediaListEntryData.DeleteMediaListEntry.toDeleteState(): DeleteState = DeleteState(deleted ?: false)

private fun DeleteReviewData.DeleteReview.toDeleteState(): DeleteState = DeleteState(deleted ?: false)

// SaveMediaListEntry data mapping

private fun SaveMediaListEntryData.SaveMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    mediaId = this@toMediaList.mediaId.toLong()
    status = this@toMediaList.status?.name
    score = this@toMediaList.score?.toFloat() ?: 0f
    progress = this@toMediaList.progress ?: 0
    progressVolumes = this@toMediaList.progressVolumes ?: 0
    repeat = this@toMediaList.repeat ?: 0
    priority = this@toMediaList.priority ?: 0
    notes = this@toMediaList.notes
    isHidden = this@toMediaList.privateValue ?: false
    isHiddenFromStatusLists = this@toMediaList.hiddenFromStatusLists ?: false
    advancedScores = this@toMediaList.advancedScores.toAdvancedScores()
    customLists = this@toMediaList.customLists.toCustomLists()
    startedAt = this@toMediaList.startedAt?.toFuzzyDate()
    completedAt = this@toMediaList.completedAt?.toFuzzyDate()
    updatedAt = this@toMediaList.updatedAt?.toLong() ?: 0L
    media = this@toMediaList.media?.toMediaBase() ?: MediaBase()
}

private fun SaveMediaListEntryData.SaveMediaListEntryMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun SaveMediaListEntryData.SaveMediaListEntryMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun SaveMediaListEntryData.SaveMediaListEntryMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun SaveMediaListEntryData.SaveMediaListEntryMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun SaveMediaListEntryData.SaveMediaListEntryMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun SaveMediaListEntryData.SaveMediaListEntryMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun SaveMediaListEntryData.SaveMediaListEntryMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun SaveMediaListEntryData.SaveMediaListEntryStartedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun SaveMediaListEntryData.SaveMediaListEntryCompletedAt.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

// RateReview data mapping

private fun RateReviewData.RateReview.toReview(): Review = Review().apply {
    id = this@toReview.id.toLong()
    summary = this@toReview.summary
    mediaType = this@toReview.mediaType?.name
    body = this@toReview.body
    rating = this@toReview.rating ?: 0
    ratingAmount = this@toReview.ratingAmount ?: 0
    userRating = this@toReview.userRating?.name
    score = this@toReview.score ?: 0
    isPrivate = this@toReview.privateValue ?: false
    createdAt = this@toReview.createdAt.toLong()
    user = this@toReview.user?.toUserBase() ?: UserBase()
    media = this@toReview.media?.toMediaBase() ?: MediaBase()
}

private fun RateReviewData.RateReviewMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun RateReviewData.RateReviewMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun RateReviewData.RateReviewMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun RateReviewData.RateReviewMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun RateReviewData.RateReviewMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun RateReviewData.RateReviewMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun RateReviewData.RateReviewMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun RateReviewData.RateReviewUser.toUserBase(): UserBase = UserBase(
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing ?: false,
).apply {
    id = this@toUserBase.id.toLong()
    avatar = this@toUserBase.avatar?.toImageBase()
}

private fun RateReviewData.RateReviewUserAvatar.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

// SaveReview data mapping

private fun SaveReviewData.SaveReview.toReview(): Review = Review().apply {
    id = this@toReview.id.toLong()
    summary = this@toReview.summary
    mediaType = this@toReview.mediaType?.name
    body = this@toReview.body
    rating = this@toReview.rating ?: 0
    ratingAmount = this@toReview.ratingAmount ?: 0
    userRating = this@toReview.userRating?.name
    score = this@toReview.score ?: 0
    isPrivate = this@toReview.privateValue ?: false
    createdAt = this@toReview.createdAt.toLong()
    user = this@toReview.user?.toUserBase() ?: UserBase()
    media = this@toReview.media?.toMediaBase() ?: MediaBase()
}

private fun SaveReviewData.SaveReviewMedia.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id.toLong()
    title = this@toMediaBase.title?.toMediaTitle()
    coverImage = this@toMediaBase.coverImage?.toImageBase()
    bannerImage = this@toMediaBase.bannerImage
    type = this@toMediaBase.type?.name
    format = this@toMediaBase.format?.name
    season = this@toMediaBase.season?.name
    status = this@toMediaBase.status?.name
    siteUrl = this@toMediaBase.siteUrl
    meanScore = this@toMediaBase.meanScore ?: 0
    averageScore = this@toMediaBase.averageScore ?: 0
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    endDate = this@toMediaBase.endDate?.toFuzzyDate()
    episodes = this@toMediaBase.episodes ?: 0
    chapters = this@toMediaBase.chapters ?: 0
    volumes = this@toMediaBase.volumes ?: 0
    isAdult = this@toMediaBase.isAdult ?: false
    isFavourite = this@toMediaBase.isFavourite
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
    mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
}

private fun SaveReviewData.SaveReviewMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun SaveReviewData.SaveReviewMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun SaveReviewData.SaveReviewMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun SaveReviewData.SaveReviewMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun SaveReviewData.SaveReviewMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun SaveReviewData.SaveReviewMediaMediaListEntry.toMediaList(): MediaEntityList = MediaEntityList().apply {
    id = this@toMediaList.id.toLong()
    status = this@toMediaList.status?.name
}

private fun SaveReviewData.SaveReviewUser.toUserBase(): UserBase = UserBase(
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing ?: false,
).apply {
    id = this@toUserBase.id.toLong()
    avatar = this@toUserBase.avatar?.toImageBase()
}

private fun SaveReviewData.SaveReviewUserAvatar.toImageBase(): ImageBase = ImageBase(
    extraLarge = null,
    large = large,
    medium = medium,
)

// Json scalar helpers shared by the media list lanes

private fun JsonElement?.toCustomLists(): List<CustomList>? {
    if (this !is JsonArray) return null
    return mapNotNull { element ->
        (element as? JsonPrimitive)
            ?.takeIf { it.isString }
            ?.content
            ?.let { name -> CustomList(name = name, isEnabled = true) }
    }
}

private fun JsonElement?.toAdvancedScores(): Map<String, Float>? {
    if (this !is JsonObject) return null
    val scores = mutableMapOf<String, Float>()
    for ((key, value) in this) {
        scores[key] = (value as? JsonPrimitive)
            ?.takeIf { !it.isString }
            ?.content
            ?.toFloatOrNull() ?: 0f
    }
    return scores
}
