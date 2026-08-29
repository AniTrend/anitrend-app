package com.mxt.anitrend.view.fragment.detail

import com.mxt.anitrend.R

/** Local sections for the unified media detail destination. */
enum class MediaSection(
    /** String resource used for the section title. */
    val titleRes: Int,
) {
    OVERVIEW(R.string.anime_page_title_overview),
    RELATIONS(R.string.title_relation),
    RECOMMENDATIONS(R.string.title_recommendations),
    STATS(R.string.title_series_stats),
    CHARACTERS(R.string.anime_page_title_characters),
    STAFF(R.string.anime_page_title_staff),
    FEED(R.string.title_social),
    REVIEWS(R.string.drawer_title_reviews),
    ;

    /** Helpers for filtering and restoring media detail sections. */
    companion object {
        /** Returns sections available to the current authentication state. */
        fun visibleSections(isAuthenticated: Boolean): List<MediaSection> = if (isAuthenticated) entries else entries.filterNot { it == FEED || it == REVIEWS }

        /** Resolves a visible section ordinal, defaulting to [OVERVIEW]. */
        fun fromOrdinal(ordinal: Int, isAuthenticated: Boolean): MediaSection {
            val visible = visibleSections(isAuthenticated)
            return visible.getOrNull(ordinal) ?: OVERVIEW
        }
    }
}
