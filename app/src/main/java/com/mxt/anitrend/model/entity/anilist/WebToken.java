package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by max on 2017/10/14.
 *
 * POST: auth/access_token
 * Url Parms:
 * grant_type    : "client_credentials"
 * client_id     :  Client id
 * client_secret :  Client secret
 * You can now access the majority of the resource server’s GET end points by including this access token as a “access_token” header or url parameter.
 * For security this access token will expire in 1 hour, to receive a new one simply repeat this step.
 */

@Entity
public class WebToken implements Parcelable, Cloneable {

    @Id
    private long id;
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;

    public WebToken() {

    }

    public WebToken(String access_token, String token_type, long expires_in, long expires, String refresh_token) {
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.refresh_token = refresh_token;
    }

    protected WebToken(Parcel in) {
        id = in.readLong();
        access_token = in.readString();
        token_type = in.readString();
        expires_in = in.readLong();
        refresh_token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(access_token);
        dest.writeString(token_type);
        dest.writeLong(expires_in);
        dest.writeString(refresh_token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WebToken> CREATOR = new Creator<WebToken>() {
        @Override
        public WebToken createFromParcel(Parcel in) {
            return new WebToken(in);
        }

        @Override
        public WebToken[] newArray(int size) {
            return new WebToken[size];
        }
    };

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public long getExpires() {
        return (System.currentTimeMillis() - 8000L) + expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getHeader() {
        return String.format("%s %s",token_type,access_token);
    }

    @Override
    public String toString() {
        return "{" +
                "id: " +id +
                " access_token: " +access_token +
                " token_type: " +token_type +
                " expires_in: " +expires_in +
                " refresh_token: " +refresh_token +
                "}";
    }

    @Override
    public WebToken clone() throws CloneNotSupportedException {
        super.clone();
        return this;
    }
}
