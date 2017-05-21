package com.mxt.anitrend.custom;

import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.api.structure.UserLists;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maxwell on 2016-12-21.
 * Store instances of the users anime or manga lists
 * into one collection, making it easier to perform operations on the data set
 */
public class UserListsCache {

    private List<ListItem> anime_list, manga_list;

    public UserListsCache() {

    }

    public List<ListItem> getMangaLists() {
        return manga_list;
    }

    /**
     * Save the current manga list of items into your lists
     */
    public void setMangaLists(UserLists userLists) {
        manga_list = new ArrayList<>();
        if(userLists != null) {
            if (userLists.getReading() != null)
                manga_list.addAll(userLists.getReading());

            if (userLists.getPlan_to_read() != null)
                manga_list.addAll(userLists.getPlan_to_read());

            if (userLists.getCompleted() != null)
                manga_list.addAll(userLists.getCompleted());

            if (userLists.getOn_hold() != null)
                manga_list.addAll(userLists.getOn_hold());

            if (userLists.getDropped() != null)
                manga_list.addAll(userLists.getDropped());
        }
    }

    public List<ListItem> getAnimeLists() {
        return anime_list;
    }

    /**
     * Save the current anime list of items into your lists
     */
    public void setAnimeLists(UserLists userLists) {
        anime_list = new ArrayList<>();
        if(userLists != null) {
            if (userLists.getWatching() != null)
                anime_list.addAll(userLists.getWatching());

            if (userLists.getPlan_to_watch() != null)
                anime_list.addAll(userLists.getPlan_to_watch());

            if (userLists.getCompleted() != null)
                anime_list.addAll(userLists.getCompleted());

            if (userLists.getOn_hold() != null)
                anime_list.addAll(userLists.getOn_hold());

            if (userLists.getDropped() != null)
                anime_list.addAll(userLists.getDropped());
        }
    }
}
