package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
class MediaTag @JvmOverloads constructor(
    var name: String? = null,
    var description: String? = null,
    var category: String? = null,
    var rank: Int = 0,
    var isGeneralSpoiler: Boolean = false,
    var isMediaSpoiler: Boolean = false,
    var isAdult: Boolean = false,
    var isSelected: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    @Id(assignable = true)
    var id: Long = 0

    override fun toString(): String = name.orEmpty()
}
