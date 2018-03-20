package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class StaffBase extends EntityGroup implements Parcelable {

    private long id;
    private TitleBase name;
    private ImageBase image;
    private boolean isFavourite;
    private String description;
    private String language;

    protected StaffBase(Parcel in) {
        id = in.readLong();
        name = in.readParcelable(TitleBase.class.getClassLoader());
        image = in.readParcelable(ImageBase.class.getClassLoader());
        isFavourite = in.readByte() != 0;
        description = in.readString();
        language = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(name, flags);
        dest.writeParcelable(image, flags);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeString(description);
        dest.writeString(language);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StaffBase> CREATOR = new Creator<StaffBase>() {
        @Override
        public StaffBase createFromParcel(Parcel in) {
            return new StaffBase(in);
        }

        @Override
        public StaffBase[] newArray(int size) {
            return new StaffBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public TitleBase getName() {
        return name;
    }

    public ImageBase getImage() {
        return image;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StaffBase)
            return ((StaffBase) obj).getId() == id;
        return super.equals(obj);
    }
}
