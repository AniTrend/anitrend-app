package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;

/**
 * Created by LuK1337 on 2021/05/05.
 */
public class CharacterStaffBase extends RecyclerItem implements Parcelable {

    private CharacterBase character;
    private MediaBase media;

    public CharacterStaffBase(CharacterBase character, MediaBase media) {
        this.character = character;
        this.media = media;
    }

    protected CharacterStaffBase(Parcel in) {
        character = in.readParcelable(CharacterBase.class.getClassLoader());
        media = in.readParcelable(MediaBase.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(character, flags);
        dest.writeParcelable(media, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CharacterStaffBase> CREATOR = new Creator<CharacterStaffBase>() {
        @Override
        public CharacterStaffBase createFromParcel(Parcel in) {
            return new CharacterStaffBase(in);
        }

        @Override
        public CharacterStaffBase[] newArray(int size) {
            return new CharacterStaffBase[size];
        }
    };

    public CharacterBase getCharacter() { return character; }

    public MediaBase getMedia() { return media; }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CharacterStaffBase)
            return ((CharacterStaffBase)obj).character.getId() == character.getId();
        return super.equals(obj);
    }
}
