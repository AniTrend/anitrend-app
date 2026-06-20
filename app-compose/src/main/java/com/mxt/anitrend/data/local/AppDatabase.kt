package com.mxt.anitrend.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mxt.anitrend.data.local.dao.UserPreferencesDao
import com.mxt.anitrend.data.local.entity.UserPreferencesEntity

@Database(
    entities = [UserPreferencesEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_preferences ADD COLUMN lastNotificationCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun create(context: android.content.Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "anitrend-compose.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
