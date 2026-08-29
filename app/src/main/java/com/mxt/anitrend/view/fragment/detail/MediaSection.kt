package com.mxt.anitrend.view.fragment.detail

import com.mxt.anitrend.R

/** Local sections for the unified media detail destination. */
enum class MediaSection(val titleRes: Int) {
    OVERVIEW(R.string.anime_page_title_overview),
    RELATIONS(R.string.title_relation),
    RECOMMENDATIONS(R.string.title_recommendations),
    STATS(R.string.title_series_stats),
    CHARACTERS(R.string.anime_page_title_characters),
    STAFF(R.string.anime_page_title_staff),
    FEED(R.string.title_social),
    REVIEWS(R.string.drawer_title_reviews),
    ;

    companion object {
        fun visibleSections(isAuthenticated: Boolean): List<MediaSection> = if (isAuthenticated) entries else entries.filterNot { it == FEED || it == REVIEWS }

        fun fromOrdinal(ordinal: Int, isAuthenticated: Boolean): MediaSection {
            val visible = visibleSections(isAuthenticated)
            return visible.getOrNull(ordinal) ?: OVERVIEW
        }
    }
}
