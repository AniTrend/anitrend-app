package com.mxt.anitrend.data

import android.content.Context

import com.mxt.anitrend.App
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.model.entity.base.AuthBase
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.base.VersionBase

import io.objectbox.Box
import io.objectbox.BoxStore

/**
 * Created by max on 2017/11/02.
 * Database helper class
 */

class DatabaseHelper(context: Context) : BoxQuery {

    private val boxStore: BoxStore by lazy {
        (context.applicationContext as App).boxStore
    }

    // Frequently used instance variables
    private var user: User? = null

    /**
     * Gets the object box from a requested class type.
     *
     * <br></br>
     * @param classType Type of class which must not be a list instance
     * @return Box of type class requested
     */
    override fun <S> getBoxStore(classType: Class<S>): Box<S> =
            boxStore.boxFor(classType)

    /**
     * Used when the application is logging out a user preferably
     */
    override fun invalidateBoxStores() {
        getBoxStore(WebToken::class.java).removeAll()
        getBoxStore(AuthBase::class.java).removeAll()
        getBoxStore(User::class.java).removeAll()
        getBoxStore(UserBase::class.java).removeAll()
        getBoxStore(VersionBase::class.java).removeAll()
        getBoxStore(NotificationHistory::class.java).removeAll()
    }

    /**
     * Gets current authenticated user
     */
    override fun getCurrentUser(): User? {
        if (user == null)
            user = getBoxStore(User::class.java).query()
                    .build().findFirst()
        return user
    }

    /**
     * Get default authentication code
     */
    override fun getAuthCode(): AuthBase? {
        return getBoxStore(AuthBase::class.java)
                .query()
                .build()
                .findFirst()
    }

    /**
     * Get web token
     */
    override fun getWebToken(): WebToken? {
        return getBoxStore(WebToken::class.java)
                .query()
                .build()
                .findFirst()
    }

    /**
     * Get the application version on github
     */
    override fun getRemoteVersion(): VersionBase? {
        return getBoxStore(VersionBase::class.java)
                .query()
                .build()
                .findFirst()
    }

    /**
     * Gets all saved tags
     */
    override fun getMediaTags(): List<MediaTag> {
        return getBoxStore(MediaTag::class.java)
                .query()
                .build()
                .findLazy()
    }

    /**
     * Gets all saved genres
     */
    override fun getGenreCollection(): List<Genre> {
        return getBoxStore(Genre::class.java)
                .query()
                .build()
                .findLazy()
    }

    /**
     * Saves current authenticated user
     *
     * @param user
     */
    override fun saveCurrentUser(user: User) {
        this.user = user
        getBoxStore(User::class.java).put(user)
    }

    /**
     * Get default authentication code
     *
     * @param authBase
     */
    override fun saveAuthCode(authBase: AuthBase) {
        getBoxStore(AuthBase::class.java).removeAll()
        getBoxStore(AuthBase::class.java).put(authBase)
    }

    /**
     * Get web token
     *
     * @param webToken
     */
    override fun saveWebToken(webToken: WebToken) {
        getBoxStore(WebToken::class.java).removeAll()
        val tokenBox = getBoxStore(WebToken::class.java)
        tokenBox.put(webToken)
    }

    /**
     * Save the application version on github
     *
     * @param versionBase
     */
    override fun saveRemoteVersion(versionBase: VersionBase) {
        val versionBox = getBoxStore(VersionBase::class.java)
        if (versionBox.count() != 0L)
            versionBox.removeAll()
        versionBase.lastChecked = System.currentTimeMillis()
        versionBox.put(versionBase)
    }

    /**
     * Saves all saved mediaTags
     *
     * @param mediaTags
     */
    override fun saveMediaTags(mediaTags: List<MediaTag>) {
        val tagBox = getBoxStore(MediaTag::class.java)
        if (tagBox.count() < mediaTags.size)
            tagBox.put(mediaTags)
    }

    /**
     * Saves all saved genres
     *
     * @param genres
     */
    override fun saveGenreCollection(genres: List<Genre>) {
        val genreBox = getBoxStore(Genre::class.java)
        if (genreBox.count() < genres.size)
            genreBox.put(genres)
    }
}