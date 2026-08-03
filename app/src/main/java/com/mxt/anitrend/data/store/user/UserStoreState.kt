package com.mxt.anitrend.data.store.user

import com.mxt.anitrend.domain.model.UserRecord

data class UserStoreState(
    val usersById: Map<Long, UserRecord> = emptyMap(),
)
