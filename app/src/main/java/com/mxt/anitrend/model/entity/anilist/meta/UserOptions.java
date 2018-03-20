package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2018/03/20.
 */

public class UserOptions implements Parcelable {

    private boolean displayAdultContent;
    private @KeyUtils.UserLanguageTitle String titleLanguage;

    protected UserOptions(Parcel in) {
        displayAdultContent = in.readByte() != 0;
        titleLanguage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (displayAdultContent ? 1 : 0));
        dest.writeString(titleLanguage);
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

    public boolean isDisplayAdultContent() {
        return displayAdultContent;
    }

    public @KeyUtils.UserLanguageTitle String getTitleLanguage() {
        return titleLanguage;
    }
}
