package com.mxt.anitrend.data;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maxwell on 1/2/2017.
 * Database holder for caching results from the api which may not be likely to change
 */
@Deprecated
class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AniTrend.db";

    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(StorageContract.SQL_CREATE_GENRE);
        sqLiteDatabase.execSQL(StorageContract.SQL_CREATE_ANIME);
        sqLiteDatabase.execSQL(StorageContract.SQL_CREATE_MANGA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(StorageContract.SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    Cursor readData(String tableName ,String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(tableName, projection, selection, selectionArgs, groupBy, having, sortOrder);
    }
}
