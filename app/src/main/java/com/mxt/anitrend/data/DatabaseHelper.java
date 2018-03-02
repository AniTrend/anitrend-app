package com.mxt.anitrend.data;

import android.content.Context;
import android.support.annotation.Nullable;

import com.mxt.anitrend.App;
import com.mxt.anitrend.base.interfaces.dao.BoxQuery;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.Genre_;
import com.mxt.anitrend.model.entity.anilist.Tag;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthCode;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.base.UserBase_;
import com.mxt.anitrend.model.entity.base.Version;
import com.mxt.anitrend.model.entity.general.SeriesList;

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
        getBoxStore(AuthCode.class).removeAll();
        getBoxStore(User.class).removeAll();
        getBoxStore(UserBase.class).removeAll();
        getBoxStore(Version.class).removeAll();
        getBoxStore(SeriesList.class).removeAll();
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
    public AuthCode getAuthCode() {
        return getBoxStore(AuthCode.class)
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
     * Gets all saved users except the current user
     */
    @Override
    public List<UserBase> getAllUsers() {
        if(getCurrentUser() != null)
            return getBoxStore(UserBase.class).query().
                    notEqual(UserBase_.id, getCurrentUser().getId())
                    .build().findLazy();
        return getBoxStore(UserBase.class)
                .query().build().findLazy();
    }

    /**
     * Gets all saved tags
     */
    @Override
    public List<Tag> getAllTags() {
        return getBoxStore(Tag.class)
                .query().build()
                .findLazy();
    }

    /**
     * Gets all saved genres
     */
    @Override
    public List<Genre> getAllGenres() {
        return getBoxStore(Genre.class).query()
                .notEqual(Genre_.genre, "Hentai")
                .build().findLazy();
    }

    /**
     * Gets all saved series lists
     */
    @Override
    public List<SeriesList> getAllSeries() {
        return getBoxStore(SeriesList.class)
                .query().build()
                .findLazy();
    }

    /**
     * Saves current authenticated user
     *
     * @param user
     */
    @Override
    public boolean saveCurrentUser(User user) {
        this.user = user;
        if(user.getLists() != null && user.getLists().size() > 0)
            saveSeries(user.getLists());
        return getBoxStore(User.class).put(user) > -1;
    }

    /**
     * Get default authentication code
     *
     * @param authCode
     */
    @Override
    public boolean saveAuthCode(AuthCode authCode) {
        getBoxStore(AuthCode.class).removeAll();
        return getBoxStore(AuthCode.class).put(authCode) > -1;
    }

    /**
     * Get web token
     *
     * @param webToken
     */
    @Override
    public boolean saveWebToken(WebToken webToken) {
        getBoxStore(WebToken.class).removeAll();
        Box<WebToken> tokenBox = getBoxStore(WebToken.class);
        return tokenBox.put(webToken) > -1;
    }

    /**
     * Save the application version on github
     *
     * @param version
     */
    @Override
    public boolean saveRemoteVersion(Version version) {
        Box<Version> versionBox = getBoxStore(Version.class);
        if(versionBox.count() > 0)
            versionBox.removeAll();
        version.setLast_checked(System.currentTimeMillis());
        versionBox.put(version);
        return false;
    }

    /**
     * Saves all users
     *
     * @param users
     */
    @Override
    public void saveUsers(List<UserBase> users) {
        Box<UserBase> userSmallBox = getBoxStore(UserBase.class);
        if(userSmallBox.count() < users.size())
            userSmallBox.put(users);
    }

    /**
     * Saves all saved tags
     *
     * @param tags
     */
    @Override
    public void saveTags(List<Tag> tags) {
        Box<Tag> tagBox = getBoxStore(Tag.class);
        if(tagBox.count() < tags.size())
            tagBox.put(tags);
    }

    /**
     * Saves all saved genres
     *
     * @param genres
     */
    @Override
    public void saveGenres(List<Genre> genres) {
        Box<Genre> genreBox = getBoxStore(Genre.class);
        if(genreBox.count() < genres.size())
            genreBox.put(genres);
    }

    /**
     * Saves all series lists
     *
     * @param seriesLists
     */
    @Override
    public void saveSeries(List<SeriesList> seriesLists) {
        Box<SeriesList> seriesListBox = getBoxStore(SeriesList.class);
        if(seriesListBox.count() < seriesLists.size())
            seriesListBox.put(seriesLists);
    }

    /**
     * Adds a user item
     *
     * @param user
     */
    @Override
    public void addUser(UserBase user) {
        getBoxStore(UserBase.class).put(user);
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

    /**
     * Save current user favourites
     *
     * @param favourite
     */
    @Override
    public void saveFavourite(Favourite favourite) {
        getBoxStore(Favourite.class).put(favourite);
    }

    /**
     * Get current user favourites
     */
    @Override
    public Favourite getFavourite(long userId) {
        return getBoxStore(Favourite.class).get(userId);
    }

    public void saveNotifications(NotificationBase... notification) {
        getBoxStore(NotificationBase.class).put(notification);
    }

    /**
     * Saves all series lists
     *
     * @param seriesMap
     */
    public void saveSeries(Map<String, List<SeriesList>> seriesMap) {
        List<SeriesList> seriesLists = new ArrayList<>();
        for (List<SeriesList> list: seriesMap.values())
            seriesLists.addAll(list);
        saveSeries(seriesLists);
    }
}