package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the review reader bottom sheet.
 *
 * Carries only stable identity so the destination never receives a complete
 * [com.mxt.anitrend.domain.model.ReviewRecord] or the legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.Review]. The reader renders its static
 * content from the immutable record supplied by the hosting screen; this param
 * records the review identity and the nested user/media identities so the sheet
 * can resolve related navigation (e.g. opening the review author's profile).
 *
 * @property reviewId Stable review id.
 * @property mediaId Stable media id the review belongs to, when known.
 * @property mediaType Presentation-independent media type string, when known.
 * @property userId Stable id of the review author, when known.
 */
@Parcelize
data class ReviewScreenParam(
    val reviewId: Long,
    val mediaId: Long? = null,
    val mediaType: String? = null,
    val userId: Long? = null,
) : ScreenParam
