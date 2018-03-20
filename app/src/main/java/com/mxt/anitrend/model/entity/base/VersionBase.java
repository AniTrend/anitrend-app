package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.BuildConfig;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by max on 2017/10/22.
 * VersionBase model from github
 */
@Entity
public class VersionBase implements Parcelable {

    @Id(assignable = true)
    private long code;
    private long last_checked;
    private boolean db_migration;
    private String version;
    private String app_id;

    public VersionBase() {

    }

    public VersionBase(int code, String version) {
        this.code = code;
        this.version = version;
    }

    protected VersionBase(Parcel in) {
        code = in.readInt();
        version = in.readString();
        app_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(code);
        dest.writeString(version);
        dest.writeString(app_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VersionBase> CREATOR = new Creator<VersionBase>() {
        @Override
        public VersionBase createFromParcel(Parcel in) {
            return new VersionBase(in);
        }

        @Override
        public VersionBase[] newArray(int size) {
            return new VersionBase[size];
        }
    };

    public long getCode() {
        return code;
    }

    public String getVersion() {
        return version;
    }

    public String getApp_id() {
        return app_id;
    }

    public boolean isNewerVersion() {
        return code > BuildConfig.VERSION_CODE;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public long getLast_checked() {
        return last_checked;
    }

    public void setLast_checked(long last_checked) {
        this.last_checked = last_checked;
    }

    public boolean isDb_migration() {
        return db_migration;
    }

    public void setDb_migration(boolean db_migration) {
        this.db_migration = db_migration;
    }
}
