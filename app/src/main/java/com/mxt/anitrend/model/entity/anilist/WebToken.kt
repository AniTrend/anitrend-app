package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/10/14.
 *
 * POST: auth/access_token
 * Url Parms:
 * grant_type    : "client_credentials"
 * client_id     :  Client id
 * client_secret :  Client secret
 * You can now access the majority of the resource server’s GET end points by including this access token as a “access_token” header or url parameter.
 * For security this access token will expire in 1 hour, to receive a new one simply repeat this step.
 */
@Parcelize
@Entity
class WebToken @JvmOverloads constructor(
    var access_token: String? = null,
    var token_type: String? = null,
    var expires_in: Long = 0,
    var expires: Long = 0,
    var refresh_token: String? = null
) : Parcelable, Cloneable {

    @IgnoredOnParcel
    @Id
    var id: Long = 0

    @IgnoredOnParcel
    val header: String
        get() = String.format("%s %s", token_type.orEmpty(), access_token.orEmpty())

    fun calculateExpires() {
        expires = (System.currentTimeMillis() - 8000L) + expires_in
    }

    override fun toString(): String {
        return "{" +
            "id: " + id +
            " access_token: " + access_token +
            " token_type: " + token_type +
            " expires_in: " + expires_in +
            " refresh_token: " + refresh_token +
            "}"
    }

    public override fun clone(): WebToken {
        super.clone()
        return this
    }
}
