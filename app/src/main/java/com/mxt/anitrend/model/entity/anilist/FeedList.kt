package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Created by Maxwell on 11/12/2016.
 */
@Parcelize
class FeedList @JvmOverloads constructor(
    var id: Long = 0,
    var replyCount: Int = 0,
    @param:KeyUtil.FeedType var type: String? = null,
    var status: String? = null,
    @SerializedName(value = "text", alternate = ["message", "progress"])
    var text: String? = null,
    var createdAt: Long = 0,
    var user: UserBase? = null,
    var media: MediaBase? = null,
    var messenger: UserBase? = null,
    var recipient: UserBase? = null,
    var likes: List<UserBase>? = null,
    var siteUrl: String? = null
) : Parcelable {

    @IgnoredOnParcel
    var replies: List<FeedReply>? = null

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is FeedReply -> other.id == id
            is FeedList -> other.id == id
            else -> super.equals(other)
        }
    }
}
