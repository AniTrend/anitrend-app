package com.mxt.anitrend.model.entity.base;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

@Entity
public class NotificationHistory {

    @Id(assignable = true)
    private long id;
    @Index
    private long lastReadId;

    public NotificationHistory() {

    }

    public long getId() {
        return id;
    }

    public long getLastReadId() {
        return lastReadId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLastReadId(long lastReadId) {
        this.lastReadId = lastReadId;
    }

    public boolean isNew(long id) {
        return lastReadId >= id;
    }
}
