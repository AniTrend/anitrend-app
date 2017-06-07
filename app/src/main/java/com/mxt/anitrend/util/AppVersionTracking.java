package com.mxt.anitrend.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.BuildConfig;

/**
 * Created by Maxwell on 9/24/2016.
 */
public class AppVersionTracking implements Parcelable {

    private int code;
    private String version;
    private String app_id;

    protected AppVersionTracking(Parcel in) {
        code = in.readInt();
        version = in.readString();
        app_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(version);
        dest.writeString(app_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppVersionTracking> CREATOR = new Creator<AppVersionTracking>() {
        @Override
        public AppVersionTracking createFromParcel(Parcel in) {
            return new AppVersionTracking(in);
        }

        @Override
        public AppVersionTracking[] newArray(int size) {
            return new AppVersionTracking[size];
        }
    };

    public int getCode() {
        return code;
    }

    public String getVersion() {
        return version;
    }

    public String getApp_id() {
        return app_id;
    }

    public boolean checkAgainstCurrent() {
        return code > BuildConfig.VERSION_CODE;
    }

    public AppVersionTracking(int code, String version) {
        this.code = code;
        this.version = version;
    }

}
