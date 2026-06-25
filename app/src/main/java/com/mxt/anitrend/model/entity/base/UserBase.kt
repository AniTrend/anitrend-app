package com.mxt.anitrend.model.entity.base

import android.os.Parcelable
import com.mxt.anitrend.data.converter.ImageBaseConverter
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 10/4/2016.
 */
@Parcelize
@Entity
open class UserBase
@JvmOverloads
constructor(
    @Index
    var name: String? = null,
    var bannerImage: String? = null,
    var isFollowing: Boolean = false,
) : Parcelable {
    @IgnoredOnParcel
    @Id(assignable = true)
    var id: Long = 0

    @IgnoredOnParcel
    @field:Convert(converter = ImageBaseConverter::class, dbType = String::class)
    var avatar: ImageBase? = null

    fun toggleFollow() {
        isFollowing = !isFollowing
    }

    override fun equals(other: Any?): Boolean {
        if (other is UserBase) {
            return other.id == id
        }
        return super.equals(other)
    }
}
