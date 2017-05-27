package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.util.ComparatorProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Maxwell on 1/12/2017.
 */

public class KeyValueTemp implements Parcelable {

    public float[] values;
    public String[] keys;

    public KeyValueTemp(int size) {
        values = new float[size];
        keys = new String[size];
    }

    public KeyValueTemp setKeyVals(HashMap<String, Integer> collection) {
        int index = 0;
        // Sorting the list based on values
        List<Map.Entry<String, Integer>> list = new LinkedList<>(collection.entrySet());
        // Sorting the list based on values
        Collections.sort(list, ComparatorProvider.getGenreKeyComparator());

        for (HashMap.Entry<String, Integer> kv: list) {
            keys[index] = kv.getKey();
            values[index] = ((float)kv.getValue())/100f;
            index++;
        }
        return this;
    }

    protected KeyValueTemp(Parcel in) {
        values = in.createFloatArray();
        keys = in.createStringArray();
    }

    public static final Creator<KeyValueTemp> CREATOR = new Creator<KeyValueTemp>() {
        @Override
        public KeyValueTemp createFromParcel(Parcel in) {
            return new KeyValueTemp(in);
        }

        @Override
        public KeyValueTemp[] newArray(int size) {
            return new KeyValueTemp[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloatArray(values);
        parcel.writeStringArray(keys);
    }
}
