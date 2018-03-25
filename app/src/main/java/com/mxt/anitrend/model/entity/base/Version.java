package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.BuildConfig;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by max on 2017/10/22.
 * Version model from github
 */
@Entity
public class Version implements Parcelable {

    @Id(assignable = true)
    private long code;
    private long lastChecked;
    private boolean migration;
    private String releaseNotes;
    private String version;
    private String appId;

    public Version() {

    }

    public Version(int code, String version) {
        this.code = code;
        this.version = version;
    }

    protected Version(Parcel in) {
        code = in.readLong();
        lastChecked = in.readLong();
        migration = in.readByte() != 0;
        releaseNotes = in.readString();
        version = in.readString();
        appId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(code);
        dest.writeLong(lastChecked);
        dest.writeByte((byte) (migration ? 1 : 0));
        dest.writeString(releaseNotes);
        dest.writeString(version);
        dest.writeString(appId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        @Override
        public Version createFromParcel(Parcel in) {
            return new Version(in);
        }

        @Override
        public Version[] newArray(int size) {
            return new Version[size];
        }
    };

    public long getCode() {
        return code;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public boolean isMigration() {
        return migration;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public String getVersion() {
        return version;
    }

    public String getAppId() {
        return appId;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }
}