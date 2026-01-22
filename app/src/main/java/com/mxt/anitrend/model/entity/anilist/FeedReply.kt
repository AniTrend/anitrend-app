package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import com.mxt.anitrend.model.entity.base.UserBase
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/03/13.
 */
@Parcelize
class FeedReply @JvmOverloads constructor(
    var id: Long = 0,
    var text: String? = null,
    var createdAt: Long = 0,
    var user: UserBase? = null,
    var likes: List<UserBase>? = null
) : Parcelable {

    val reply: String?
        get() = text

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is FeedReply -> other.id == id
            is FeedList -> other.id == id
            else -> super.equals(other)
        }
    }
}
