package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.meta.FormatStats
import com.mxt.anitrend.model.entity.anilist.meta.GenreStats
import com.mxt.anitrend.model.entity.anilist.meta.MediaTagStats
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution
import com.mxt.anitrend.model.entity.anilist.meta.YearStats
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 11/15/2016.
 * UserStats for user
 * @see User
 */
@Deprecated("Deprecated in Java")
@Parcelize
class UserStats
@JvmOverloads
constructor(
    var watchedTime: Int = 0,
    var chaptersRead: Int = 0,
    var animeStatusDistribution: List<StatusDistribution>? = null,
    var mangaStatusDistribution: List<StatusDistribution>? = null,
    var favouredGenres: List<GenreStats>? = null,
    var favouredTags: List<MediaTagStats>? = null,
    var favouredYears: List<YearStats>? = null,
    var favouredFormats: List<FormatStats>? = null,
) : Parcelable
