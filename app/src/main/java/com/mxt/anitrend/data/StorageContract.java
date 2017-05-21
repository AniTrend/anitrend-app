package com.mxt.anitrend.data;

import android.provider.BaseColumns;

/**
 * Created by Maxwell on 1/2/2017.
 * Database Contract
 */
@Deprecated
final class StorageContract {

    static final String SQL_CREATE_GENRE =
            "CREATE TABLE IF NOT EXISTS " + GenreEntry.TABLE_NAME + " (" +
                    GenreEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY NOT NULL UNIQUE," +
                    GenreEntry.COLUMN_NAME_GENRE + " TEXT);";

    static final String SQL_CREATE_ANIME =
            "CREATE TABLE IF NOT EXISTS " + AnimeEntry.TABLE_NAME + " (" +
                    AnimeEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY NOT NULL UNIQUE," +
                    AnimeEntry.COLUMN_NAME_EN_Title + " TEXT," +
                    AnimeEntry.COLUMN_NAME_JP_Title + " TEXT," +
                    AnimeEntry.COLUMN_NAME_RM_Title + " TEXT);";

    static final String SQL_CREATE_MANGA =
            "CREATE TABLE IF NOT EXISTS " + MangaEntry.TABLE_NAME + " (" +
                    MangaEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY NOT NULL UNIQUE," +
                    MangaEntry.COLUMN_NAME_EN_Title + " TEXT," +
                    MangaEntry.COLUMN_NAME_JP_Title + " TEXT," +
                    MangaEntry.COLUMN_NAME_RM_Title + " TEXT);";


    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " +
                    GenreEntry.TABLE_NAME + "," +
                    GenreEntry.TABLE_NAME + "," +
                    GenreEntry.TABLE_NAME + ";";

    private StorageContract() {
        //non instantiatable class
    }

    static class GenreEntry implements BaseColumns {
        static final String TABLE_NAME = "genre";
        static final String COLUMN_NAME_ID = "id";
        static final String COLUMN_NAME_GENRE = "genre";
    }

    static class AnimeEntry implements BaseColumns {
        static final String TABLE_NAME = "anime";
        static final String COLUMN_NAME_ID = "id";
        static final String COLUMN_NAME_EN_Title = "en_title";
        static final String COLUMN_NAME_JP_Title = "jp_title";
        static final String COLUMN_NAME_RM_Title = "rm_title";
    }

    static class MangaEntry implements BaseColumns {
        static final String TABLE_NAME = "manga";
        static final String COLUMN_NAME_ID = "id";
        static final String COLUMN_NAME_EN_Title = "en_title";
        static final String COLUMN_NAME_JP_Title = "jp_title";
        static final String COLUMN_NAME_RM_Title = "rm_title";
    }
}
