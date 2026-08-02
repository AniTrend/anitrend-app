package com.mxt.anitrend.data.store.user

import com.mxt.anitrend.domain.model.UserRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryUserStore : UserStore {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(UserStoreState())

    override val state: StateFlow<UserStoreState> = mutableState.asStateFlow()

    override suspend fun apply(change: UserStoreChange) {
        mutex.withLock {
            mutableState.value = when (change) {
                is UserStoreChange.UserUpserted -> reduceUserUpserted(change.user)
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            mutableState.value = UserStoreState()
        }
    }

    override fun observeUser(userId: Long): Flow<UserRecord?> = state.map { it.usersById[userId] }.distinctUntilChanged()

    private fun reduceUserUpserted(user: UserRecord): UserStoreState {
        val currentState = mutableState.value
        val currentRevision = currentState.usersById[user.id]?.revision ?: Long.MIN_VALUE
        if (user.revision < currentRevision) {
            return currentState
        }
        return currentState.copy(
            usersById = currentState.usersById.toMutableMap().apply {
                put(user.id, user)
            },
        )
    }
}
