package com.mxt.anitrend.model.entity.base

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class NotificationHistory() {

    @Id(assignable = true)
    var id: Long = 0
    var read: Boolean = false

    constructor(id: Long) : this() {
        this.id = id
    }
}