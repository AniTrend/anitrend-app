package com.mxt.anitrend.data.store.user

import com.mxt.anitrend.domain.model.UserRecord

sealed interface UserStoreChange {
    data class UserUpserted(
        val user: UserRecord,
    ) : UserStoreChange
}
