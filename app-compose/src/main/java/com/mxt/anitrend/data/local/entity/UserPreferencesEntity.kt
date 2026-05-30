package com.mxt.anitrend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val selectedTheme: String = "light",
    val showAdultContent: Boolean = false,
    val currentUserId: Long? = null,
    val lastNotificationCount: Int = 0
)
