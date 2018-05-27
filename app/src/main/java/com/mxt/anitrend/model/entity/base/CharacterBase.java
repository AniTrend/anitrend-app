package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class CharacterBase extends RecyclerItem implements Parcelable {

    private long id;
    private TitleBase name;
    private ImageBase image;
    private boolean isFavourite;
    private String siteUrl;

    protected CharacterBase(Parcel in) {
        id = in.readLong();
        name = in.readParcelable(TitleBase.class.getClassLoader());
        image = in.readParcelable(ImageBase.class.getClassLoader());
        isFavourite = in.readByte() != 0;
        siteUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(name, flags);
        dest.writeParcelable(image, flags);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeString(siteUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CharacterBase> CREATOR = new Creator<CharacterBase>() {
        @Override
        public CharacterBase createFromParcel(Parcel in) {
            return new CharacterBase(in);
        }

        @Override
        public CharacterBase[] newArray(int size) {
            return new CharacterBase[size];
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

    public String getSiteUrl() {
        return siteUrl;
    }

    public void toggleFavourite() {
        isFavourite = !isFavourite;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CharacterBase)
            return ((CharacterBase)obj).getId() == id;
        return super.equals(obj);
    }
}
