package com.mxt.anitrend.data.media

data class MediaCharacter(
    val id: Long,
    val name: String,
    val imageMedium: String?,
    val imageLarge: String?,
    val role: String?,
    val isFavourite: Boolean,
    val siteUrl: String?,
)

data class MediaStaffMember(
    val id: Long,
    val name: String,
    val imageMedium: String?,
    val imageLarge: String?,
    val role: String?,
    val language: String?,
    val isFavourite: Boolean,
    val siteUrl: String?,
)

data class MediaRelation(
    val id: Long,
    val title: String,
    val type: String?,
    val format: String?,
    val status: String?,
    val coverMedium: String?,
    val meanScore: Int?,
    val episodes: Int?,
    val chapters: Int?,
    val relationType: String?,
)

data class ScoreDistribution(
    val score: Int?,
    val amount: Int?,
)

data class StatusDistribution(
    val status: String?,
    val amount: Int?,
)

data class Ranking(
    val id: Int?,
    val rank: Int?,
    val type: String?,
    val format: String?,
    val year: Int?,
    val season: String?,
    val allTime: Boolean?,
    val context: String?,
)

data class MediaSocialItem(
    val id: Int,
    val userId: Int,
    val userName: String,
    val userAvatar: String?,
    val status: String?,
    val progress: String?,
    val createdAt: Int,
    val mediaTitle: String?,
    val mediaCover: String?,
    val mediaType: String?,
)

data class RecommendationItem(
    val id: Int,
    val title: String?,
    val type: String?,
    val format: String?,
    val coverMedium: String?,
    val meanScore: Int?,
)
