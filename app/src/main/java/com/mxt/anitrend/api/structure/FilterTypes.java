package com.mxt.anitrend.api.structure;

/**
 * Created by Maxwell on 10/2/2016.
 * Do not rearrange order any properties
 */
public class FilterTypes {

    public enum ActionType {
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

    public enum SeriesType {
        ANIME, MANGA
    }

    public static final String[] SeriesTitles = {"romaji", "english", "japanese"};

    public enum SeasonTitle {
        WINTER,
        SPRING,
        SUMMER,
        FALL
    }

    public static final String[] GenreTypes = {
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

    public static final String[] SeasonTitles = {"winter","spring","summer","fall"};

    public enum ActivityType {
        PROGRESS,
        STATUS,
        PUBIC_STATUS,
        MESSAGE
    }

    public static final String[] ActivtyTypes = { "series", "text", "public-status", "message"};

    public enum ReviewType {
        LATEST,
        POPULAR,
        NEED_LOVE,
        CONTROVERSIAL
    }

    public static final String[] ReviewTypes = { "latest", "popular", "love", "controversial" };

    public enum SeriesTitle {
        ROMAJI, ENGLISH, JAPANESE
    }

    public static final String[] SeriesTypes = {"anime", "manga"};

    public enum ShowType{
        TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, MANGA, NOVEL, ONE_SHOT, DOUJIN, MANHUA, MANHWA
    }

    public static final String[] ShowTypes = {null, "Tv","Tv Short","Movie","Special","OVA","ONA","Manga","Novel","One Shot","Doujin","Manhua","Manhwa"};

    public enum AnimeStatusType{
        ALL_ITEMS, FINISHED_AIRING, CURRENTLY_AIRING, NOT_YET_AIRED, CANCELLED
    }

    public static final String[] AnimeStatusTypes = {null ,"finished airing","currently airing","not yet aired","cancelled"};

    public enum MangaStatusType{
        All_ITEMS ,FINISHED_PUBLISHING, PUBLISHING, NOT_YET_PUBLISHED, CANCELLED
    }

    public static final String[] MangaStatusTypes = {null, "finished publishing","publishing","not yet published","cancelled"};

    public enum AnimeSourceType{
        ORIGINAL, MANGA, LIGHT_NOVEL, VISUAL_NOVEL, VIDEO_GAME, OTHER
    }

    public static final String[] AnimeSourceTypes = {"Original","Manga","Light Novel","Visual Novel","Video Game","Other"};

    public enum SeriesSortType {
        TITLE ,SCORE, POPULARITY, START_DATE, END_DATE
    }

    public enum OrderType {
        ASC, DESC
    }

    public enum RankingType {
        POPULAR, RATED
    }

    public static final String[] Ranking_Type = { "popular", "rated" };

    public static final String[] SeriesSortTypes = { "title_romaji" ,"score", "popularity", "start_date" ,"end_date" };

    public static final String[] OrderTypes = { "asc", "desc" };

    public enum UserAnimeStatusKeys {
        WATCHING,
        PLAN_TO_WATCH,
        COMPLETED,
        ON_HOLD,
        DROPPED
    }

    public static final String[] UserAnimeStatus = {"watching","plan to watch","completed","on-hold","dropped"};

    public enum UserMangaStatusKeys {
        READING,
        PLAN_TO_READ,
        COMPLETED,
        ON_HOLD,
        DROPPED
    }

    public static final String[] UserMangaStatus = {"reading","plan to read","completed","on-hold","dropped"};

}
