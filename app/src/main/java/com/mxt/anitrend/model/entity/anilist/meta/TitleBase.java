package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by max on 2018/03/20.
 * TitleBase for staff and characters
 */

public class TitleBase implements Parcelable {

    private String first;
    private String last;
    @SerializedName("native")
    private String original;
    private List<String> alternative;

    protected TitleBase(Parcel in) {
        first = in.readString();
        last = in.readString();
        original = in.readString();
        alternative = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(first);
        dest.writeString(last);
        dest.writeString(original);
        dest.writeStringList(alternative);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TitleBase> CREATOR = new Creator<TitleBase>() {
        @Override
        public TitleBase createFromParcel(Parcel in) {
            return new TitleBase(in);
        }

        @Override
        public TitleBase[] newArray(int size) {
            return new TitleBase[size];
        }
    };

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public String getOriginal() {
        return original;
    }

    public List<String> getAlternative() {
        return alternative;
    }

    public String getAlternativeFormatted() {
        StringBuilder builder = new StringBuilder();
        for (String alternate: alternative) {
            if(TextUtils.isEmpty(builder))
                builder.append(alternate);
            else
                builder.append(", ").append(alternate);
        }
        return builder.toString();
    }

    public String getFullName() {
        String fullName = first;
        if(!TextUtils.isEmpty(last))
            fullName += " " + last;
        return fullName;
    }
}
