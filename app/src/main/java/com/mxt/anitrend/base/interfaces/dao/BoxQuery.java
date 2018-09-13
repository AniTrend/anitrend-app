package com.mxt.anitrend.base.interfaces.dao;

import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthBase;
import com.mxt.anitrend.model.entity.base.VersionBase;

import java.util.List;

import io.objectbox.Box;

public interface BoxQuery {

    /**
     * Gets the object box from a requested class type.
     *
     * <br/>
     * @param classType Type of class which must not be a list instance
     * @return Box of type class requested
     */
    <S> Box<S> getBoxStore(Class<S> classType);

    /**
     * Used when the application is logging out a user preferably
     */
    void invalidateBoxStores();

    /**
     * Gets current authenticated user
     */
    User getCurrentUser();

    /**
     * Get default authentication code
     */
    AuthBase getAuthCode();

    /**
     * Get web token
     */
    WebToken getWebToken();

    /**
     * Get the application version on github
     */
    VersionBase getRemoteVersion();

    /**
     * Gets all saved tags
     */
    List<MediaTag> getMediaTags();

    /**
     * Gets all saved genres
     */
    List<Genre> getGenreCollection();

    /**
     * Saves current authenticated user
     */
    void saveCurrentUser(User user);

    /**
     * Get default authentication code
     */
    void saveAuthCode(AuthBase authBase);

    /**
     * Get web token
     */
    void saveWebToken(WebToken webToken);

    /**
     * Save the application version on github
     */
    void saveRemoteVersion(VersionBase versionBase);

    /**
     * Saves all saved mediaTags
     */
    void saveMediaTags(List<MediaTag> mediaTags);

    /**
     * Saves all saved genres
     */
    void saveGenreCollection(List<Genre> genres);
}
