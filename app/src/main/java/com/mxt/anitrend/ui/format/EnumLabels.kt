/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.format

import androidx.annotation.StringRes
import com.mxt.anitrend.R

/*
 * Shared localized labels for categorical wire identifiers. The `when` keys are
 * the exact enum values from app/src/main/graphql/schema.graphql (and the
 * KeyUtil constants that mirror them), not generated GraphQL types, so domain
 * APIs stay free of codegen classes.
 *
 * Every function maps a nullable raw wire string to a string resource. Unknown
 * or null metadata maps to R.string.label_unknown; callers that do not require
 * a value should omit the row instead of rendering it.
 */

/** `MediaType`: anime or manga. */
@StringRes
internal fun mediaTypeLabelRes(raw: String?): Int = when (raw) {
    "ANIME" -> R.string.label_media_type_anime
    "MANGA" -> R.string.label_media_type_manga
    else -> R.string.label_unknown
}

/** `MediaFormat`: release format; shared label across anime and manga formats. */
@StringRes
internal fun mediaFormatLabelRes(raw: String?): Int = when (raw) {
    "TV" -> R.string.anime_format_tv
    "TV_SHORT" -> R.string.anime_format_tv_short
    "MOVIE" -> R.string.anime_format_movie
    "SPECIAL" -> R.string.anime_format_special
    "OVA" -> R.string.anime_format_ova
    "ONA" -> R.string.anime_format_ona
    "MUSIC" -> R.string.anime_format_music
    "MANGA" -> R.string.manga_format_manga
    "NOVEL" -> R.string.manga_format_novel
    "ONE_SHOT" -> R.string.manga_format_oneshot
    else -> R.string.label_unknown
}

/** `MediaStatus` (schema version 2): current release status. */
@StringRes
internal fun mediaStatusLabelRes(raw: String?): Int = when (raw) {
    "FINISHED" -> R.string.media_status_finished
    "RELEASING" -> R.string.media_status_releasing
    "NOT_YET_RELEASED" -> R.string.media_status_not_yet_released
    "CANCELLED" -> R.string.media_status_canceled
    "HIATUS" -> R.string.label_media_status_hiatus
    else -> R.string.label_unknown
}

/** `MediaSeason`: premiere season. */
@StringRes
internal fun mediaSeasonLabelRes(raw: String?): Int = when (raw) {
    "WINTER" -> R.string.seasons_title_winter
    "SPRING" -> R.string.seasons_title_spring
    "SUMMER" -> R.string.seasons_title_summer
    "FALL" -> R.string.seasons_title_fall
    else -> R.string.label_unknown
}

/** `MediaSource`: source material the media was adapted from. */
@StringRes
internal fun mediaSourceLabelRes(raw: String?): Int = when (raw) {
    "ANIME" -> R.string.label_media_source_anime
    "COMIC" -> R.string.label_media_source_comic
    "DOUJINSHI" -> R.string.label_media_source_doujinshi
    "GAME" -> R.string.label_media_source_game
    "LIGHT_NOVEL" -> R.string.label_media_source_light_novel
    "LIVE_ACTION" -> R.string.label_media_source_live_action
    "MANGA" -> R.string.label_media_source_manga
    "MULTIMEDIA_PROJECT" -> R.string.label_media_source_multimedia_project
    "NOVEL" -> R.string.label_media_source_novel
    "ORIGINAL" -> R.string.label_media_source_original
    "OTHER" -> R.string.label_media_source_other
    "PICTURE_BOOK" -> R.string.label_media_source_picture_book
    "VIDEO_GAME" -> R.string.label_media_source_video_game
    "VISUAL_NOVEL" -> R.string.label_media_source_visual_novel
    "WEB_NOVEL" -> R.string.label_media_source_web_novel
    else -> R.string.label_unknown
}

/** `MediaListStatus`: user list entry status. */
@StringRes
internal fun mediaListStatusLabelRes(raw: String?): Int = when (raw) {
    "CURRENT" -> R.string.media_list_status_current
    "COMPLETED" -> R.string.media_list_status_completed
    "PLANNING" -> R.string.media_list_status_planning
    "DROPPED" -> R.string.media_list_status_dropped
    "PAUSED" -> R.string.media_list_status_paused
    "REPEATING" -> R.string.media_list_status_repeating
    else -> R.string.label_unknown
}

/** `CharacterRole`: role the character plays in the media. */
@StringRes
internal fun characterRoleLabelRes(raw: String?): Int = when (raw) {
    "MAIN" -> R.string.label_character_role_main
    "SUPPORTING" -> R.string.label_character_role_supporting
    "BACKGROUND" -> R.string.label_character_role_background
    else -> R.string.label_unknown
}

/** `StaffLanguage`: primary language of the voice actor. */
@StringRes
internal fun staffLanguageLabelRes(raw: String?): Int = when (raw) {
    "JAPANESE" -> R.string.label_staff_language_japanese
    "ENGLISH" -> R.string.label_staff_language_english
    "KOREAN" -> R.string.label_staff_language_korean
    "ITALIAN" -> R.string.label_staff_language_italian
    "HEBREW" -> R.string.label_staff_language_hebrew
    "HUNGARIAN" -> R.string.label_staff_language_hungarian
    "GERMAN" -> R.string.label_staff_language_german
    "SPANISH" -> R.string.label_staff_language_spanish
    "PORTUGUESE" -> R.string.label_staff_language_portuguese
    "FRENCH" -> R.string.label_staff_language_french
    else -> R.string.label_unknown
}

/** `MediaRelation`: relation of the media to its parent. */
@StringRes
internal fun mediaRelationLabelRes(raw: String?): Int = when (raw) {
    "ADAPTATION" -> R.string.label_media_relation_adaptation
    "ALTERNATIVE" -> R.string.label_media_relation_alternative
    "CHARACTER" -> R.string.label_media_relation_character
    "COMPILATION" -> R.string.label_media_relation_compilation
    "CONTAINS" -> R.string.label_media_relation_contains
    "OTHER" -> R.string.label_media_relation_other
    "PARENT" -> R.string.label_media_relation_parent
    "PREQUEL" -> R.string.label_media_relation_prequel
    "SEQUEL" -> R.string.label_media_relation_sequel
    "SIDE_STORY" -> R.string.label_media_relation_side_story
    "SOURCE" -> R.string.label_media_relation_source
    "SPIN_OFF" -> R.string.label_media_relation_spin_off
    "SUMMARY" -> R.string.label_media_relation_summary
    else -> R.string.label_unknown
}

/**
 * `MediaSort` dimension name for the app's exposed sort keys. Direction is a
 * separate concern rendered from its own resources
 * (`order_by_type_ascending` / `order_by_type_descending`).
 */
@StringRes
internal fun mediaSortLabelRes(raw: String?): Int = when (raw) {
    "ID" -> R.string.label_media_sort_id
    "TITLE_ROMAJI" -> R.string.label_media_sort_title_romaji
    "TITLE_ENGLISH" -> R.string.label_media_sort_title_english
    "TITLE_NATIVE" -> R.string.label_media_sort_title_native
    "TYPE" -> R.string.label_media_sort_type
    "FORMAT" -> R.string.label_media_sort_format
    "START_DATE" -> R.string.label_media_sort_start_date
    "END_DATE" -> R.string.label_media_sort_end_date
    "SCORE" -> R.string.label_media_sort_score
    "POPULARITY" -> R.string.label_media_sort_popularity
    "TRENDING" -> R.string.label_media_sort_trending
    "EPISODES" -> R.string.label_media_sort_episodes
    "DURATION" -> R.string.label_media_sort_duration
    "STATUS" -> R.string.label_media_sort_status
    "CHAPTERS" -> R.string.label_media_sort_chapters
    "VOLUMES" -> R.string.label_media_sort_volumes
    else -> R.string.label_unknown
}
