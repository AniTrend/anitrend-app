package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.utils.MarkDown;

/**
 * Created by Maxwell on 1/9/2017.
 */

public class Comment implements Parcelable {

    private int id;
    private int parent_id;
    private int lft;
    private int rgt;
    private int depth;
    private int user_id;
    private int thread_id;
    private String comment;
    private String created_at;
    private String updated_at;

    protected Comment(Parcel in) {
        id = in.readInt();
        parent_id = in.readInt();
        lft = in.readInt();
        rgt = in.readInt();
        depth = in.readInt();
        user_id = in.readInt();
        thread_id = in.readInt();
        comment = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getParent_id() {
        return parent_id;
    }

    public int getLft() {
        return lft;
    }

    public int getRgt() {
        return rgt;
    }

    public int getDepth() {
        return depth;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getThread_id() {
        return thread_id;
    }

    public String getComment() {
        return MarkDown.convert(comment).toString();
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(parent_id);
        parcel.writeInt(lft);
        parcel.writeInt(rgt);
        parcel.writeInt(depth);
        parcel.writeInt(user_id);
        parcel.writeInt(thread_id);
        parcel.writeString(comment);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
    }
}
