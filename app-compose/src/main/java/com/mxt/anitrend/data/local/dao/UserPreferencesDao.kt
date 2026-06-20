package com.mxt.anitrend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mxt.anitrend.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun observe(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preferences: UserPreferencesEntity)
}
