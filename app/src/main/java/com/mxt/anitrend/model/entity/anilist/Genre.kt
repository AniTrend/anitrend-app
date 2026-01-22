package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Created by Maxwell on 10/24/2016.
 * API Genres
 */
@Parcelize
@Entity
class Genre @JvmOverloads constructor(
    @Index
    var genre: String? = null
) : Parcelable {

    @IgnoredOnParcel
    @Id
    var id: Long = 0

    @IgnoredOnParcel
    var isSelected: Boolean = false

    override fun toString(): String = genre.orEmpty()
}
