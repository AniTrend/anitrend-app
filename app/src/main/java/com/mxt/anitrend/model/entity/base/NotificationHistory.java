package com.mxt.anitrend.model.entity.base;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class NotificationHistory {

    @Id(assignable = true)
    private long id;
    private boolean read;

    public NotificationHistory() {

    }

    public NotificationHistory(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean isRead() {
        return read;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
