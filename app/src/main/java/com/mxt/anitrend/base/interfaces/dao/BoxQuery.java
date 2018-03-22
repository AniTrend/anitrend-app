package com.mxt.anitrend.base.interfaces.dao;

import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.base.Version;

import java.util.List;

public interface BoxQuery {

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
    Version getRemoteVersion();

    /**
     * Gets all saved tags
     */
    List<MediaTag> getMediaTags();

    /**
     * Gets all saved genres
     */
    List<Genre> getGenreCollection();

    /**
     * Gets all saved series lists
     */
    List<MediaList> getMediaLists();

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
    void saveRemoteVersion(Version version);

    /**
     * Saves all saved mediaTags
     */
    void saveMediaTags(List<MediaTag> mediaTags);

    /**
     * Saves all saved genres
     */
    void saveGenreCollection(List<Genre> genres);

    /**
     * Saves all series lists
     */
    void saveMediaLists(List<MediaList> mediaLists);

    /**
     * Removes following of a specific user
     */
    void removeUser(UserBase userBase);
}
