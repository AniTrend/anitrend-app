package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by max on 2018/03/22.
 * MediaListTypeOptions for media types
 */

public class MediaListTypeOptions implements Parcelable {

    private List<String> sectionOrder;
    private boolean splitCompletedSectionByFormat;
    private List<String> customLists;
    private List<String> advancedScoring;
    private boolean advancedScoringEnabled;

    protected MediaListTypeOptions(Parcel in) {
        sectionOrder = in.createStringArrayList();
        splitCompletedSectionByFormat = in.readByte() != 0;
        customLists = in.createStringArrayList();
        advancedScoring = in.createStringArrayList();
        advancedScoringEnabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(sectionOrder);
        dest.writeByte((byte) (splitCompletedSectionByFormat ? 1 : 0));
        dest.writeStringList(customLists);
        dest.writeStringList(advancedScoring);
        dest.writeByte((byte) (advancedScoringEnabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaListTypeOptions> CREATOR = new Creator<MediaListTypeOptions>() {
        @Override
        public MediaListTypeOptions createFromParcel(Parcel in) {
            return new MediaListTypeOptions(in);
        }

        @Override
        public MediaListTypeOptions[] newArray(int size) {
            return new MediaListTypeOptions[size];
        }
    };

    public List<String> getSectionOrder() {
        return sectionOrder;
    }

    public boolean isSplitCompletedSectionByFormat() {
        return splitCompletedSectionByFormat;
    }

    public List<String> getCustomLists() {
        return customLists;
    }

    public List<String> getAdvancedScoring() {
        return advancedScoring;
    }

    public boolean isAdvancedScoringEnabled() {
        return advancedScoringEnabled;
    }
}
