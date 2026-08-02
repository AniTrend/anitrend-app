package com.mxt.anitrend.fixture

import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.KeyUtil

/**
 * Builder helpers for creating test [MediaList] and [MediaBase] instances.
 *
 * All fixtures use sensible defaults so callers only need to override
 * the fields relevant to their test case.
 */
object MediaListFixtures {

    /**
     * Creates a [MediaList] with the given overrides.
     *
     * The returned instance has its [media][MediaList.media] field set
     * to a default anime [MediaBase] unless [media] is provided.
     */
    fun aMediaList(
        id: Long = 1,
        mediaId: Long = 100,
        status: String = KeyUtil.CURRENT,
        score: Float = 8f,
        scoreRaw: Int? = null,
        progress: Int = 5,
        progressVolumes: Int = 0,
        repeat: Int = 0,
        priority: Int = 0,
        notes: String? = null,
        isHidden: Boolean = false,
        isHiddenFromStatusLists: Boolean = false,
        advancedScores: Map<String, Float>? = null,
        startedAt: FuzzyDate? = null,
        completedAt: FuzzyDate? = null,
        media: MediaBase = anAnimeMediaBase(id = mediaId),
    ): MediaList = MediaList().apply {
        this.id = id
        this.mediaId = mediaId
        this.status = status
        this.score = score
        this.scoreRaw = scoreRaw
        this.progress = progress
        this.progressVolumes = progressVolumes
        this.repeat = repeat
        this.priority = priority
        this.notes = notes
        this.isHidden = isHidden
        this.isHiddenFromStatusLists = isHiddenFromStatusLists
        this.advancedScores = advancedScores
        this.startedAt = startedAt
        this.completedAt = completedAt
        this.media = media
    }

    /**
     * Creates an anime [MediaBase] with the given overrides.
     */
    fun anAnimeMediaBase(
        id: Long = 100,
        type: String = KeyUtil.ANIME,
        episodes: Int = 12,
        status: String = KeyUtil.FINISHED,
        chapters: Int = 0,
        volumes: Int = 0,
    ): MediaBase = MediaBase().apply {
        this.id = id
        this.type = type
        this.episodes = episodes
        this.chapters = chapters
        this.volumes = volumes
        this.status = status
    }

    /**
     * Creates a manga [MediaBase] with the given overrides.
     */
    fun aMangaMediaBase(
        id: Long = 101,
        type: String = KeyUtil.MANGA,
        chapters: Int = 100,
        volumes: Int = 10,
        status: String = KeyUtil.FINISHED,
    ): MediaBase = MediaBase().apply {
        this.id = id
        this.type = type
        this.episodes = 0
        this.chapters = chapters
        this.volumes = volumes
        this.status = status
    }

    /**
     * Creates a [MediaBase] with status [KeyUtil.NOT_YET_RELEASED].
     */
    fun aNotYetReleasedMediaBase(
        id: Long = 200,
        type: String = KeyUtil.ANIME,
        episodes: Int = 12,
    ): MediaBase = anAnimeMediaBase(
        id = id,
        type = type,
        episodes = episodes,
        status = KeyUtil.NOT_YET_RELEASED,
    )

    /**
     * Creates a [MediaBase] with status [KeyUtil.RELEASING].
     */
    fun anAiringMediaBase(
        id: Long = 300,
        type: String = KeyUtil.ANIME,
        episodes: Int = 12,
    ): MediaBase = anAnimeMediaBase(
        id = id,
        type = type,
        episodes = episodes,
        status = KeyUtil.RELEASING,
    )

    /**
     * Creates a [FuzzyDate] from year, month, day components.
     */
    fun aFuzzyDate(
        year: Int = 2024,
        month: Int = 6,
        day: Int = 15,
    ): FuzzyDate = FuzzyDate(day, month, year)

    /**
     * Creates a domain [FuzzyDateRecord] from year, month, day components.
     */
    fun aFuzzyDateRecord(
        year: Int = 2024,
        month: Int = 6,
        day: Int = 15,
    ): FuzzyDateRecord = FuzzyDateRecord(
        year = year,
        month = month,
        day = day,
    )
}
