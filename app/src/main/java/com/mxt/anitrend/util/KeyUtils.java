package com.mxt.anitrend.util;

import android.support.annotation.IntDef;

/**
 * Created by Maxwell on 10/2/2016.
 * Do not rearrange order any properties
 */
public interface KeyUtils {

    // Unaffiliated values
    String[] ShowTypes = {null, "Tv","Tv Short","Movie","Special","OVA","ONA","Manga","Novel","One Shot","Doujin","Manhua","Manhwa"};

    // TODO: 2017/07/19  Will be removed after the DB is fixed
    String[] GenreTypes = {
            "Action",
            "Adventure",
            "Comedy",
            "Drama",
            "Ecchi",
            "Fantasy",
            "Hentai",
            "Horror",
            "Mahou Shoujo",
            "Mecha",
            "Music",
            "Mystery",
            "Psychological",
            "Romance",
            "Sci-Fi",
            "Slice of Life",
            "Sports",
            "Supernatural",
            "Thriller"
    };

    enum ActionType {
        ACTION_FOLLOW_TOGGLE,
        ACTIVITY_CREATE,
        ACTIVITY_EDIT,
        ACTIVITY_DELETE,
        ACTIVITY_REPLY,
        ACTIVITY_FAVOURITE,
        ACTIVITY_REPLY_FAVOURITE,
        ACTIVITY_REPLY_EDIT,
        ACTIVITY_REPLY_DELETE,
        DIRECT_MESSAGE_SEND,
        DIRECT_MESSAGE_EDIT,
        ANIME_FAVOURITE,
        MANGA_FAVOURITE,
        CHARACTER_FAVOURITE,
        STAFF_FAVOURITE,
        STUDIO_FAVOURITE,
        REVIEW_ANIME_RATE,
        REVIEW_MANGA_RATE,
        ANIME_LIST_ADD,
        ANIME_LIST_EDIT,
        ANIME_LIST_DELETE,
        MANGA_LIST_ADD,
        MANGA_LIST_EDIT,
        MANGA_LIST_DELETE
    }

    // Hub request types
    int FEED_TYPE = 0, RSS_TYPE = 1, PLAYLIST_TYPE = 2, VIDEO_TYPE = 3;

    String[] HubTypes = {"client_credentials", "authorization_code", "refresh_token"};
    @IntDef({FEED_TYPE, RSS_TYPE, PLAYLIST_TYPE, VIDEO_TYPE})
    @interface HubType {}

    // Token grant types
    int AUTHENTICATION_TYPE = 0, AUTHENTICATION_CODE = 1, REFRESH_TYPE = 2;

    String[] GrantTypes = {"client_credentials", "authorization_code", "refresh_token"};
    @IntDef({AUTHENTICATION_TYPE, AUTHENTICATION_CODE, REFRESH_TYPE})
    @interface GrantType {}


    // Series Types
    int ANIME = 0, MANGA = 1;

    String[] SeriesTypes = {"anime", "manga"};
    @IntDef({ANIME, MANGA})
    @interface SeriesType {}

    // Season Types
    int WINTER = 0, SPRING = 1, SUMMER = 2, FALL = 3;

    String[] SeasonTitles = {"winter","spring","summer","fall"};
    @IntDef({WINTER, SPRING, SUMMER, FALL})
    @interface SeasonTitle {}


    // Activity types
    int PROGRESS = 0, STATUS = 1, PUBIC_STATUS = 2, MESSAGE = 3;

    String[] ActivityTypes = { "series", "text", "public-status-replies", "message"};
    @IntDef({PROGRESS,STATUS,PUBIC_STATUS,MESSAGE})
    @interface ActivityType {}

    // Review Types
    int LATEST = 0, POPULAR = 1, NEED_LOVE = 2, CONTROVERSIAL = 3;

    String[] ReviewTypes = { "latest", "popular", "love", "controversial" };
    @IntDef({LATEST,POPULAR,NEED_LOVE,CONTROVERSIAL})
    @interface ReviewType {}

    // Share series status types
    int ALL_ITEMS = 0, CANCELLED = 4;

    // Anime status types
    int FINISHED_AIRING = 1, CURRENTLY_AIRING = 2, NOT_YET_AIRED = 3;

    String[] AnimeStatusTypes = {null ,"finished airing","currently airing","not yet aired","cancelled"};
    @IntDef({ALL_ITEMS, FINISHED_AIRING, CURRENTLY_AIRING, NOT_YET_AIRED, CANCELLED})
    @interface AnimeStatusType {}


    // Manga status types
    int FINISHED_PUBLISHING = 1, PUBLISHING = 2, NOT_YET_PUBLISHED = 3;

    String[] MangaStatusTypes = {null, "finished publishing","publishing","not yet published","cancelled"};
    @IntDef({ALL_ITEMS ,FINISHED_PUBLISHING, PUBLISHING, NOT_YET_PUBLISHED, CANCELLED})
    @interface MangaStatusType {}


    // Season sorting types
    int TITLE = 0 ,SCORE = 1, POPULARITY = 2, START_DATE = 3, END_DATE = 4;

    String[] SeriesSortTypes = { "title_romaji" ,"score", "popularity", "start_date" ,"end_date" };
    @IntDef({TITLE ,SCORE, POPULARITY, START_DATE, END_DATE})
    @interface SeriesSortType {}


    // Order types
    int ASC = 0, DESC = 1;

    String[] OrderTypes = { "asc", "desc" };
    @IntDef({ASC, DESC})
    @interface OrderType {}


    // Shared int defs
    int COMPLETED = 2, ON_HOLD = 3, DROPPED = 4;

    // Anime Status
    int WATCHING = 0, PLAN_TO_WATCH = 1;

    String[] UserAnimeStatus = {"watching","plan to watch","completed","on-hold","dropped"};
    @IntDef({WATCHING, PLAN_TO_WATCH, COMPLETED, ON_HOLD, DROPPED})
    @interface UserAnimeStatusKey {}


    // Manga Status
    int READING = 0, PLAN_TO_READ = 1;

    String[] UserMangaStatus = {"reading","plan to read","completed","on-hold","dropped"};
    @IntDef({READING, PLAN_TO_READ, COMPLETED, ON_HOLD, DROPPED})
    @interface UserMangaStatusKey {}
}
