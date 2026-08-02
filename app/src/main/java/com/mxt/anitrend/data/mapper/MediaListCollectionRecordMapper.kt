package com.mxt.anitrend.data.mapper

import com.google.gson.JsonParser
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.graphql.generated.MediaListCollectionData

/**
 * Maps the generated `MediaListCollectionData` GraphQL types to the immutable
 * [MediaListCollectionPageResult] consumed by the media list store pipeline.
 *
 * The collection is flattened across all returned lists, preserving the legacy
 * `lists -> entries` flattening that fed the media list store's collection-loaded
 * change. Generated Int ids are converted to domain Longs, generated enums are
 * exposed as their serialized `name` (matching the legacy String-backed entity
 * lane), and the `Json` scalar payloads (`customLists` array, `advancedScores`
 * map) are parsed into the domain shapes the mutation commands consume. `scoreRaw`
 * is intentionally null: the collection operation does not request it, matching the
 * legacy collection lane.
 */
fun MediaListCollectionData.MediaListCollection.toMediaListCollectionPageResult(): MediaListCollectionPageResult =
    MediaListCollectionPageResult(
        entries = lists.orEmpty()
            .flatMap { list -> list?.entries.orEmpty() }
            .filterNotNull()
            .map { it.toMediaListRecord() },
    )

fun MediaListCollectionData.MediaListCollectionListsEntries.toMediaListRecord(
    revision: Long = 0L,
    ownerUserId: Long? = null,
    ownerUserName: String? = null,
): MediaListRecord = MediaListRecord(
    id = id.toLong(),
    mediaId = mediaId.toLong(),
    status = status?.name,
    score = score ?: 0.0,
    scoreRaw = null,
    progress = progress ?: 0,
    progressVolumes = progressVolumes ?: 0,
    repeat = repeat ?: 0,
    priority = priority ?: 0,
    `private` = privateValue ?: false,
    hiddenFromStatusLists = hiddenFromStatusLists ?: false,
    customLists = customLists.toCustomListNames(),
    advancedScores = advancedScores.toAdvancedScores(),
    notes = notes,
    startedAt = startedAt?.toFuzzyDateRecord(),
    completedAt = completedAt?.toFuzzyDateRecord(),
    media = media?.toMediaSummaryRecord(),
    revision = revision,
    ownerUserId = ownerUserId,
    ownerUserName = ownerUserName,
)

private fun MediaListCollectionData.MediaListCollectionListsEntriesMedia.toMediaSummaryRecord(): MediaSummaryRecord = MediaSummaryRecord(
    id = id.toLong(),
    titleUserPreferred = title?.userPreferred,
    titleRomaji = title?.romaji,
    titleEnglish = title?.english,
    titleOriginal = title?.native,
    coverImage = coverImage?.extraLarge ?: coverImage?.large ?: coverImage?.medium,
    bannerImage = bannerImage,
    type = type?.name,
    format = format?.name,
    episodes = episodes ?: 0,
    chapters = chapters ?: 0,
    volumes = volumes ?: 0,
    status = status?.name,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
    startDate = startDate?.toFuzzyDateRecord(),
    nextAiringEpisode = nextAiringEpisode?.toAiringScheduleRecord(),
    averageScore = averageScore,
)

private fun MediaListCollectionData.MediaListCollectionListsEntriesStartedAt.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaListCollectionData.MediaListCollectionListsEntriesCompletedAt.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaListCollectionData.MediaListCollectionListsEntriesMediaStartDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaListCollectionData.MediaListCollectionListsEntriesMediaNextAiringEpisode.toAiringScheduleRecord(): AiringScheduleRecord = AiringScheduleRecord(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

/**
 * Parses the GraphQL `Json` scalar payload of `customLists(asArray: true)`, which the
 * generated code exposes as a raw JSON array string. Unknown or malformed payloads
 * degrade to an empty list, matching the legacy Gson lane for absent custom lists.
 */
private fun String?.toCustomListNames(): List<String> {
    val raw = this ?: return emptyList()
    if (raw.isBlank()) {
        return emptyList()
    }
    return runCatching {
        JsonParser.parseString(raw).asJsonArray.mapNotNull { element ->
            element.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
        }
    }.getOrDefault(emptyList())
}

/**
 * Parses the GraphQL `Json` scalar payload of `advancedScores` into the domain map.
 * Unknown or malformed payloads degrade to an empty map.
 */
private fun String?.toAdvancedScores(): Map<String, Double> {
    val raw = this ?: return emptyMap()
    if (raw.isBlank()) {
        return emptyMap()
    }
    return runCatching {
        val jsonObject: com.google.gson.JsonObject = JsonParser.parseString(raw).asJsonObject
        val scores = mutableMapOf<String, Double>()
        jsonObject.entrySet().forEach { (key, value) ->
            scores[key] = value.takeIf { it.isJsonPrimitive }?.asDouble ?: 0.0
        }
        scores
    }.getOrDefault(emptyMap())
}
