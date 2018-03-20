package com.mxt.anitrend.base.interfaces.dao;

import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.base.Version;
import com.mxt.anitrend.model.entity.anilist.MediaList;

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
     * Get all users
     */
    List<UserBase> getAllUsers();

    /**
     * Gets all saved tags
     */
    List<MediaTag> getAllTags();

    /**
     * Gets all saved genres
     */
    List<Genre> getAllGenres();

    /**
     * Gets all saved series lists
     */
    List<MediaList> getAllSeries();

    /**
     * Saves current authenticated user
     */
    boolean saveCurrentUser(User user);

    /**
     * Get default authentication code
     */
    boolean saveAuthCode(AuthBase authBase);

    /**
     * Get web token
     */
    boolean saveWebToken(WebToken webToken);

    /**
     * Save the application version on github
     */
    boolean saveRemoteVersion(Version version);

    /**
     * Saves all the users
     */
    void saveUsers(List<UserBase> users);

    /**
     * Saves all saved mediaTags
     */
    void saveTags(List<MediaTag> mediaTags);

    /**
     * Saves all saved genres
     */
    void saveGenres(List<Genre> genres);

    /**
     * Saves all series lists
     */
    void saveSeries(List<MediaList> mediaLists);

    /**
     * Adds a user item
     */
    void addUser(UserBase user);

    /**
     * Removes following of a specific user
     */
    void removeUser(UserBase userBase);

    /**
     * Save current user favourites
     */
    void saveFavourite(Favourite favourite);

    /**
     * Get current user favourites
     */
    Favourite getFavourite(long userId);
}
