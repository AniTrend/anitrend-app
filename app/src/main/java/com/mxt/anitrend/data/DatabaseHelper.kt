package com.mxt.anitrend.data

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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Created by max on 2017/11/02.
 * Database helper class
 */

class DatabaseHelper : BoxQuery, KoinComponent {

    private val boxStore by inject<BoxStore>()

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
    override var currentUser: User? = null
        get() {
            return getBoxStore(User::class.java).query()
                    .build().findFirst()
        }
        set(value) {
            field = value
            if (value != null)
                getBoxStore(User::class.java).put(value)
        }
    /**
     * Get default authentication code
     */
    override var authCode: AuthBase? = null
        get() = getBoxStore(AuthBase::class.java)
                .query()
                .build()
                .findFirst()
        set(value) {
            field = value
            if (value != null) {
                getBoxStore(AuthBase::class.java).removeAll()
                getBoxStore(AuthBase::class.java).put(value)
            }
        }

    /**
     * Get web token
     */
    override var webToken: WebToken? = null
        get() = getBoxStore(WebToken::class.java)
                .query()
                .build()
                .findFirst()
        set(value) {
            field = value
            if (value != null) {
                getBoxStore(WebToken::class.java).removeAll()
                getBoxStore(WebToken::class.java).put(value)
            }
        }
    /**
     * Get the application version on github
     */
    override var remoteVersion: VersionBase? = null
        get() = getBoxStore(VersionBase::class.java)
                .query().build().findFirst()
        set(value) {
            field = value
            if (value != null) {
                val versionBox = getBoxStore(VersionBase::class.java)
                if (versionBox.count() != 0L)
                    versionBox.removeAll()
                value.lastChecked = System.currentTimeMillis()
                versionBox.put(value)
            }
        }
    /**
     * Gets all saved tags
     */
    override var mediaTags: List<MediaTag> = Collections.emptyList()
        get() = getBoxStore(MediaTag::class.java)
                .query()
                .build()
                .findLazy()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                val tagBox = getBoxStore(MediaTag::class.java)
                if (tagBox.count() < value.size)
                    tagBox.put(value)
            }
        }
    /**
     * Gets all saved genres
     */
    override var genreCollection: List<Genre> = Collections.emptyList()
        get() = getBoxStore(Genre::class.java)
                .query()
                .build()
                .findLazy()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                val genreBox = getBoxStore(Genre::class.java)
                if (genreBox.count() < value.size)
                    genreBox.put(value)
            }
        }
}