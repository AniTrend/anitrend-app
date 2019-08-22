package com.mxt.anitrend.base.interfaces.dao

import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.model.entity.base.AuthBase
import com.mxt.anitrend.model.entity.base.VersionBase

import io.objectbox.Box

interface BoxQuery {

    /**
     * Gets current authenticated user
     */
    var currentUser: User?

    /**
     * Get default authentication code
     */
    var authCode: AuthBase?

    /**
     * Get web token
     */
    var webToken: WebToken?

    /**
     * Get the application version on github
     */
    var remoteVersion: VersionBase?

    /**
     * Gets all saved tags
     */
    var mediaTags: List<MediaTag>

    /**
     * Gets all saved genres
     */
    var genreCollection: List<Genre>

    /**
     * Gets the object box from a requested class type.
     *
     * <br></br>
     * @param classType Type of class which must not be a list instance
     * @return Box of type class requested
     */
    fun <S> getBoxStore(classType: Class<S>): Box<S>

    /**
     * Used when the application is logging out a user preferably
     */
    fun invalidateBoxStores()
}
