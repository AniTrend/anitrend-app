package com.mxt.anitrend.data.media

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.MediaCharactersQuery
import com.mxt.anitrend.data.graphql.MediaOverviewQuery
import com.mxt.anitrend.data.graphql.MediaRecommendationsQuery
import com.mxt.anitrend.data.graphql.MediaRelationsQuery
import com.mxt.anitrend.data.graphql.MediaSocialQuery
import com.mxt.anitrend.data.graphql.MediaStaffQuery
import com.mxt.anitrend.data.graphql.MediaStatsQuery
import com.mxt.anitrend.ui.detail.MediaDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface MediaRepository {
    fun observeMedia(id: Int): Flow<MediaDetail?>
    fun observeCharacters(id: Int): Flow<List<MediaCharacter>>
    fun observeStaff(id: Int): Flow<List<MediaStaffMember>>
    fun observeRelations(id: Int): Flow<List<MediaRelation>>
    fun observeStats(id: Int): Flow<Pair<List<ScoreDistribution>, List<Ranking>>>
    fun observeSocial(id: Int): Flow<List<MediaSocialItem>>
    fun observeRecommendations(id: Int): Flow<List<RecommendationItem>>
}

class ApolloMediaRepository(
    private val apolloClient: ApolloClient,
) : MediaRepository {

    override fun observeMedia(id: Int): Flow<MediaDetail?> = flow {
        val response = apolloClient.query(MediaOverviewQuery(id = id)).execute()
        val media = response.data?.Media
        emit(media?.toMediaDetail())
    }

    private fun MediaOverviewQuery.Media.toMediaDetail(): MediaDetail {
        return MediaDetail(
            id = id.toLong(),
            title = title?.userPreferred ?: title?.romaji ?: "Unknown",
            romajiTitle = title?.romaji,
            englishTitle = title?.english,
            type = type?.name,
            format = format?.name,
            status = status?.name,
            description = description,
            genres = genres?.filterNotNull().orEmpty(),
            meanScore = meanScore,
            averageScore = averageScore,
            popularity = popularity,
            favourites = favourites,
            episodes = episodes,
            chapters = chapters,
            volumes = volumes,
            duration = duration,
            season = season?.name,
            seasonYear = seasonYear,
            isAdult = isAdult ?: false,
            isFavourite = isFavourite ?: false,
            siteUrl = siteUrl,
            hashtag = hashtag,
            source = source?.name,
            bannerImage = bannerImage,
            coverImageLarge = coverImage?.large,
            coverImageExtraLarge = coverImage?.extraLarge,
            trailerId = trailer?.id,
            trailerSite = trailer?.site,
            trailerThumbnail = trailer?.thumbnail,
            studios = studios?.nodes?.filterNotNull()?.map {
                MediaDetail.Studio(it.id, it.name ?: "", it.siteUrl)
            }.orEmpty(),
            tags = tags?.filterNotNull()?.map {
                MediaDetail.Tag(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    rank = it.rank,
                    isMediaSpoiler = it.isMediaSpoiler ?: false,
                )
            }.orEmpty(),
            startDate = formatFuzzyDate(
                startDate?.year, startDate?.month, startDate?.day,
            ),
            endDate = formatFuzzyDate(
                endDate?.year, endDate?.month, endDate?.day,
            ),
            nextAiringEpisode = nextAiringEpisode?.let {
                MediaDetail.AiringEpisode(
                    airingAt = it.airingAt,
                    timeUntilAiring = it.timeUntilAiring,
                    episode = it.episode,
                )
            },
            mediaListEntry = mediaListEntry?.let {
                MediaDetail.MediaListEntry(
                    status = it.status?.name,
                    score = it.score,
                    progress = it.progress,
                    progressVolumes = it.progressVolumes,
                )
            },
        )
    }

    private fun formatFuzzyDate(year: Int?, month: Int?, day: Int?): String? {
        val parts = listOfNotNull(
            day?.toString(),
            month?.toString()?.let { m -> "%02d".format(m.toInt()) },
            year?.toString(),
        )
        return parts.takeIf { it.isNotEmpty() }?.joinToString("/")
    }

    override fun observeCharacters(id: Int): Flow<List<MediaCharacter>> = flow {
        val response = apolloClient.query(MediaCharactersQuery(id = id)).execute()
        val characters = response.data?.Media?.characters?.edges?.mapNotNull { edge ->
            val node = edge?.node ?: return@mapNotNull null
            MediaCharacter(
                id = node.id.toLong(),
                name = node.name?.full ?: buildString {
                    node.name?.first?.let { append(it) }
                    if (node.name?.first != null && node.name?.last != null) append(" ")
                    node.name?.last?.let { append(it) }
                }.ifEmpty { "Unknown" },
                imageMedium = node.image?.medium,
                imageLarge = node.image?.large,
                role = edge.role?.name,
                isFavourite = node.isFavourite ?: false,
                siteUrl = node.siteUrl,
            )
        }.orEmpty()
        emit(characters)
    }

    override fun observeStaff(id: Int): Flow<List<MediaStaffMember>> = flow {
        val response = apolloClient.query(MediaStaffQuery(id = id)).execute()
        val staff = response.data?.Media?.staff?.edges?.mapNotNull { edge ->
            val node = edge?.node ?: return@mapNotNull null
            MediaStaffMember(
                id = node.id.toLong(),
                name = node.name?.full ?: buildString {
                    node.name?.first?.let { append(it) }
                    if (node.name?.first != null && node.name?.last != null) append(" ")
                    node.name?.last?.let { append(it) }
                }.ifEmpty { "Unknown" },
                imageMedium = node.image?.medium,
                imageLarge = node.image?.large,
                role = edge.role,
                language = node.language?.name,
                isFavourite = node.isFavourite ?: false,
                siteUrl = node.siteUrl,
            )
        }.orEmpty()
        emit(staff)
    }

    override fun observeRelations(id: Int): Flow<List<MediaRelation>> = flow {
        val response = apolloClient.query(MediaRelationsQuery(id = id)).execute()
        val relations = response.data?.Media?.relations?.edges?.mapNotNull { edge ->
            val node = edge?.node ?: return@mapNotNull null
            MediaRelation(
                id = node.id.toLong(),
                title = node.title?.userPreferred ?: "Unknown",
                type = node.type?.name,
                format = node.format?.name,
                status = node.status?.name,
                coverMedium = node.coverImage?.medium,
                meanScore = node.meanScore,
                episodes = node.episodes,
                chapters = node.chapters,
                relationType = edge.relationType?.name,
            )
        }.orEmpty()
        emit(relations)
    }

    override fun observeStats(id: Int): Flow<Pair<List<ScoreDistribution>, List<Ranking>>> = flow {
        val response = apolloClient.query(MediaStatsQuery(id = id)).execute()
        val scoreDistribution = response.data?.Media?.stats?.scoreDistribution
            ?.filterNotNull()
            ?.map {
                ScoreDistribution(
                    score = it.score,
                    amount = it.amount,
                )
            }.orEmpty()
        val rankings = response.data?.Media?.rankings
            ?.filterNotNull()
            ?.map {
                Ranking(
                    id = it.id,
                    rank = it.rank,
                    type = it.type.rawValue,
                    format = it.format.rawValue,
                    year = it.year,
                    season = it.season?.rawValue,
                    allTime = it.allTime,
                    context = it.context,
                )
            }.orEmpty()
        emit(Pair(scoreDistribution, rankings))
    }

    override fun observeSocial(id: Int): Flow<List<MediaSocialItem>> = flow {
        val response = apolloClient.query(MediaSocialQuery(mediaId = id)).execute()
        val items = response.data?.Page?.activities?.mapNotNull { activity ->
            val listActivity = activity?.onListActivity ?: return@mapNotNull null
            MediaSocialItem(
                id = listActivity.id,
                userId = listActivity.user?.id ?: 0,
                userName = listActivity.user?.name ?: "Unknown",
                userAvatar = listActivity.user?.avatar?.medium,
                status = listActivity.status,
                progress = listActivity.progress,
                createdAt = listActivity.createdAt,
                mediaTitle = listActivity.media?.title?.userPreferred,
                mediaCover = listActivity.media?.coverImage?.medium,
                mediaType = listActivity.media?.type?.rawValue,
            )
        }.orEmpty()
        emit(items)
    }

    override fun observeRecommendations(id: Int): Flow<List<RecommendationItem>> = flow {
        val response = apolloClient.query(MediaRecommendationsQuery(id = id)).execute()
        val items = response.data?.Media?.recommendations?.nodes
            ?.filterNotNull()
            ?.mapNotNull { node ->
                val rec = node.mediaRecommendation ?: return@mapNotNull null
                RecommendationItem(
                    id = rec.id,
                    title = rec.title?.userPreferred,
                    type = rec.type?.rawValue,
                    format = rec.format?.rawValue,
                    coverMedium = rec.coverImage?.medium,
                    meanScore = rec.meanScore,
                )
            }.orEmpty()
        emit(items)
    }
}
