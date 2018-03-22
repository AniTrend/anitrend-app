package com.mxt.anitrend.data;

import android.content.Context;
import android.support.annotation.Nullable;

import com.mxt.anitrend.App;
import com.mxt.anitrend.base.interfaces.dao.BoxQuery;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthBase;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.base.Version;
import com.mxt.anitrend.model.entity.anilist.MediaList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.BoxStore;

/**
 * Created by max on 2017/11/02.
 * Database helper class
 */

public class DatabaseHelper implements BoxQuery {

    private BoxStore boxStore;
    private Context context;

    // frequently used instance variables
    private User user;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    /**
     * Gets the object box from a requested class type.
     *
     * <br/>
     * @param classType Type of class which must not be a list instance
     * @return Box of type class requested
     */
    public <S> Box<S> getBoxStore(Class<S> classType) {
        if(boxStore == null)
            boxStore = ((App)context.getApplicationContext()).getBoxStore();
        return boxStore.boxFor(classType);
    }

    /**
     * When logging out of the application
     */
    public void invalidateBoxStores() {
        getBoxStore(WebToken.class).removeAll();
        getBoxStore(AuthBase.class).removeAll();
        getBoxStore(User.class).removeAll();
        getBoxStore(UserBase.class).removeAll();
        getBoxStore(Version.class).removeAll();
        getBoxStore(MediaList.class).removeAll();
        getBoxStore(NotificationBase.class).removeAll();
        getBoxStore(Favourite.class).removeAll();
    }

    /**
     * Gets current authenticated user
     */
    @Override
    public User getCurrentUser() {
        if(user == null)
            user = getBoxStore(User.class)
                    .query().build()
                    .findFirst();
        return user;
    }

    /**
     * Get default authentication code
     */
    @Override
    public AuthBase getAuthCode() {
        return getBoxStore(AuthBase.class)
                .query().build()
                .findFirst();
    }

    /**
     * Get web token
     */
    @Override
    public @Nullable WebToken getWebToken() {
        return getBoxStore(WebToken.class)
                .query().build().findFirst();
    }

    /**
     * Get the application version on github
     */
    @Override
    public Version getRemoteVersion() {
        return getBoxStore(Version.class)
                .query().build().findFirst();
    }

    /**
     * Gets all saved tags
     */
    @Override
    public List<MediaTag> getMediaTags() {
        return getBoxStore(MediaTag.class)
                .query().build()
                .findLazy();
    }

    /**
     * Gets all saved genres
     */
    @Override
    public List<Genre> getGenreCollection() {
        return getBoxStore(Genre.class).query()
                .notEqual(Genre_.genre, "Hentai")
                .build().findLazy();
    }

    /**
     * Gets all saved series lists
     */
    @Override
    public List<MediaList> getMediaLists() {
        return getBoxStore(MediaList.class)
                .query().build()
                .findLazy();
    }

    /**
     * Saves current authenticated user
     *
     * @param user
     */
    @Override
    public void saveCurrentUser(User user) {
        this.user = user;
        getBoxStore(User.class).put(user);
    }

    /**
     * Get default authentication code
     *
     * @param authBase
     */
    @Override
    public void saveAuthCode(AuthBase authBase) {
        getBoxStore(AuthBase.class).removeAll();
        getBoxStore(AuthBase.class).put(authBase);
    }

    /**
     * Get web token
     *
     * @param webToken
     */
    @Override
    public void saveWebToken(WebToken webToken) {
        getBoxStore(WebToken.class).removeAll();
        Box<WebToken> tokenBox = getBoxStore(WebToken.class);
        tokenBox.put(webToken);
    }

    /**
     * Save the application version on github
     *
     * @param version
     */
    @Override
    public void saveRemoteVersion(Version version) {
        Box<Version> versionBox = getBoxStore(Version.class);
        if(versionBox.count() > 0)
            versionBox.removeAll();
        version.setLast_checked(System.currentTimeMillis());
        versionBox.put(version);
    }

    /**
     * Saves all saved mediaTags
     *
     * @param mediaTags
     */
    @Override
    public void saveMediaTags(List<MediaTag> mediaTags) {
        Box<MediaTag> tagBox = getBoxStore(MediaTag.class);
        if(tagBox.count() < mediaTags.size())
            tagBox.put(mediaTags);
    }

    /**
     * Saves all saved genres
     *
     * @param genres
     */
    @Override
    public void saveGenreCollection(List<Genre> genres) {
        Box<Genre> genreBox = getBoxStore(Genre.class);
        if(genreBox.count() < genres.size())
            genreBox.put(genres);
    }

    /**
     * Saves all series lists
     *
     * @param mediaLists
     */
    @Override
    public void saveMediaLists(List<MediaList> mediaLists) {
        Box<MediaList> seriesListBox = getBoxStore(MediaList.class);
        if(seriesListBox.count() < mediaLists.size())
            seriesListBox.put(mediaLists);
    }


    /**
     * Removes following of a specific user
     *
     * @param userBase
     */
    @Override
    public void removeUser(UserBase userBase) {
        getBoxStore(UserBase.class).remove(userBase);
    }

    public void saveNotifications(NotificationBase... notification) {
        getBoxStore(NotificationBase.class).put(notification);
    }

    /**
     * Saves all series lists
     *
     * @param seriesMap
     */
    public void saveSeries(Map<String, List<MediaList>> seriesMap) {
        List<MediaList> mediaLists = new ArrayList<>();
        for (List<MediaList> list: seriesMap.values())
            mediaLists.addAll(list);
        saveMediaLists(mediaLists);
    }
}