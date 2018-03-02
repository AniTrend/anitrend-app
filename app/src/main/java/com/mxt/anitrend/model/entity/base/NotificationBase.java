package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.DateUtil;

/**
 * Created by max on 2018/02/24.
 * Notification base meta data class
 */
@Entity
public class NotificationBase extends EntityGroup implements Parcelable {

    @Id(assignable = true)
    private long id;
    private long user_id;
    private int invoker_id;
    private int object_id;
    private int object_type;
    private String meta_data;
    private String created_at;
    private boolean read;

    public NotificationBase() {
        // empty constructor required by object box
    }

    protected NotificationBase(Parcel in) {
        id = in.readLong();
        user_id = in.readLong();
        invoker_id = in.readInt();
        object_id = in.readInt();
        object_type = in.readInt();
        meta_data = in.readString();
        created_at = in.readString();
        read = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(user_id);
        dest.writeInt(invoker_id);
        dest.writeInt(object_id);
        dest.writeInt(object_type);
        dest.writeString(meta_data);
        dest.writeString(created_at);
        dest.writeByte((byte) (read ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NotificationBase> CREATOR = new Creator<NotificationBase>() {
        @Override
        public NotificationBase createFromParcel(Parcel in) {
            return new NotificationBase(in);
        }

        @Override
        public NotificationBase[] newArray(int size) {
            return new NotificationBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public long getUser_id() {
        return user_id;
    }

    public int getInvoker_id() {
        return invoker_id;
    }

    public int getObject_id() {
        return object_id;
    }

    public int getObject_type() {
        return object_type;
    }

    public String getMeta_data() {
        return meta_data;
    }

    public String getCreated_at() {
        //JSP time zone
        return DateUtil.getPrettyDateCustom(created_at);
    }

    public boolean isRead() {
        return read;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public void setInvoker_id(int invoker_id) {
        this.invoker_id = invoker_id;
    }

    public void setObject_id(int object_id) {
        this.object_id = object_id;
    }

    public void setObject_type(int object_type) {
        this.object_type = object_type;
    }

    public void setMeta_data(String meta_data) {
        this.meta_data = meta_data;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
