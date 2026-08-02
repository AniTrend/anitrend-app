package com.mxt.anitrend.data.store.user

import com.mxt.anitrend.domain.model.UserRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserStore {
    val state: StateFlow<UserStoreState>

    suspend fun apply(change: UserStoreChange)

    suspend fun clear()

    fun observeUser(userId: Long): Flow<UserRecord?>
}
