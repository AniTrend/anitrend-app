package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by max on 2017/11/01.
 * Authenticated user
 */
@Entity
public class AuthCode implements Parcelable {

    @Id
    private long id;
    private String code;
    private String refresh_code;

    public AuthCode() {

    }

    public AuthCode(String code, String refresh_code) {
        this.code = code;
        this.refresh_code = refresh_code;
    }


    protected AuthCode(Parcel in) {
        id = in.readLong();
        code = in.readString();
        refresh_code = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(code);
        dest.writeString(refresh_code);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AuthCode> CREATOR = new Creator<AuthCode>() {
        @Override
        public AuthCode createFromParcel(Parcel in) {
            return new AuthCode(in);
        }

        @Override
        public AuthCode[] newArray(int size) {
            return new AuthCode[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRefresh_code() {
        return refresh_code;
    }

    public void setRefresh_code(String refresh_code) {
        this.refresh_code = refresh_code;
    }
}
