package com.mxt.anitrend.data.auth

import com.mxt.anitrend.data.local.dao.UserPreferencesDao
import com.mxt.anitrend.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val userPreferencesDao: UserPreferencesDao,
) {
    fun observeAuthState(): Flow<Boolean> =
        userPreferencesDao.observe().map { preferences ->
            preferences?.currentUserId != null
        }

    suspend fun markLoggedIn(userId: Long = 1L) {
        val preferences = userPreferencesDao.observe().firstOrNull() ?: UserPreferencesEntity()
        userPreferencesDao.upsert(preferences.copy(currentUserId = userId))
    }

    suspend fun markLoggedOut() {
        val preferences = userPreferencesDao.observe().firstOrNull() ?: UserPreferencesEntity()
        userPreferencesDao.upsert(preferences.copy(currentUserId = null))
    }
}
