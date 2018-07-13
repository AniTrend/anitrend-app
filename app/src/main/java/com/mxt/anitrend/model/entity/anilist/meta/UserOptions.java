package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2018/03/20.
 */

public class UserOptions implements Parcelable {

    private @KeyUtil.UserLanguageTitle String titleLanguage;
    private boolean displayAdultContent;
    private boolean airingNotifications;
    private @KeyUtil.ProfileColor String profileColor;

    protected UserOptions(Parcel in) {
        titleLanguage = in.readString();
        displayAdultContent = in.readByte() != 0;
        airingNotifications = in.readByte() != 0;
        profileColor = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(titleLanguage);
        dest.writeByte((byte) (displayAdultContent ? 1 : 0));
        dest.writeByte((byte) (airingNotifications ? 1 : 0));
        dest.writeString(profileColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserOptions> CREATOR = new Creator<UserOptions>() {
        @Override
        public UserOptions createFromParcel(Parcel in) {
            return new UserOptions(in);
        }

        @Override
        public UserOptions[] newArray(int size) {
            return new UserOptions[size];
        }
    };

    public @KeyUtil.UserLanguageTitle String getTitleLanguage() {
        return titleLanguage;
    }

    public boolean isDisplayAdultContent() {
        return displayAdultContent;
    }

    public boolean isAiringNotifications() {
        return airingNotifications;
    }

    public @KeyUtil.ProfileColor String getProfileColor() {
        return profileColor;
    }
}
