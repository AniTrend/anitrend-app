package com.mxt.anitrend.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maxwell on 1/3/2017.
 */
@Deprecated
public class DataCentre {

    private final Database db;

    public DataCentre(Context mContext) {
        db = new Database(mContext);
    }

    public void addGenres(List<Genre> genres) {
        SQLiteDatabase writer = db.getReadableDatabase();
        writer.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Genre genre: genres) {
                values.put(StorageContract.GenreEntry.COLUMN_NAME_ID, genre.getId());
                values.put(StorageContract.GenreEntry.COLUMN_NAME_GENRE, genre.getGenre());
                writer.insertWithOnConflict(StorageContract.GenreEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            writer.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e("Data Center Insertion", ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        finally {
            writer.endTransaction();
        }
    }

    /**
     * Get saved genres in the database
     * <br/>
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
     * <br/><br/>
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     */
    public List<String> getGenres(String selection, String[] selectionArgs){
        String[] projection = new String[]{
                StorageContract.GenreEntry.COLUMN_NAME_ID,
                StorageContract.GenreEntry.COLUMN_NAME_GENRE};
        String sortOrder = StorageContract.GenreEntry.COLUMN_NAME_GENRE+" ASC";

        Cursor cursor = db.readData(StorageContract.GenreEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, //group by
                null, //having
                sortOrder);
        List<String> genres = new ArrayList<>();
        while (cursor.moveToNext()) {
            genres.add(cursor.getString(cursor.getColumnIndexOrThrow(StorageContract.GenreEntry.COLUMN_NAME_GENRE)));
        }
        cursor.close();
        return genres;
    }

    public void addAnime(List<Series> series) {
        ContentValues values = new ContentValues(series.size());
        for (Series anime: series) {
            values.put(StorageContract.AnimeEntry.COLUMN_NAME_ID, anime.getId());
            values.put(StorageContract.AnimeEntry.COLUMN_NAME_EN_Title, anime.getTitle_english());
            values.put(StorageContract.AnimeEntry.COLUMN_NAME_JP_Title, anime.getTitle_japanese());
            values.put(StorageContract.AnimeEntry.COLUMN_NAME_RM_Title, anime.getTitle_romaji());
        }
        //db.writeData(StorageContract.AnimeEntry.TABLE_NAME, values);
    }

    public void addManga(List<Series> series) {
        ContentValues values = new ContentValues(series.size());
        for (Series manga: series) {
            values.put(StorageContract.MangaEntry.COLUMN_NAME_ID, manga.getId());
            values.put(StorageContract.MangaEntry.COLUMN_NAME_EN_Title, manga.getTitle_english());
            values.put(StorageContract.MangaEntry.COLUMN_NAME_JP_Title, manga.getTitle_japanese());
            values.put(StorageContract.MangaEntry.COLUMN_NAME_RM_Title, manga.getTitle_romaji());
        }
       //db.writeData(StorageContract.MangaEntry.TABLE_NAME, values);
    }

    public void closeConnection(){
        db.close();
    }
}
