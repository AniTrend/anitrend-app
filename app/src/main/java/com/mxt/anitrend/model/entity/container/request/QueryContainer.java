package com.mxt.anitrend.model.entity.container.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.api.converter.GraphQLConverter;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Actual query and variable container
 * Use case can be found here:
 * @see GraphQLConverter#requestBodyConverter
 */
public class QueryContainer implements Parcelable {

    protected String query;
    protected Map<String, Object> variables;

    QueryContainer() {
        variables = new WeakHashMap<>();
    }

    private QueryContainer(Parcel in) {
        query = in.readString();
        variables = in.readHashMap(WeakHashMap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(query);
        dest.writeMap(variables);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QueryContainer> CREATOR = new Creator<QueryContainer>() {
        @Override
        public QueryContainer createFromParcel(Parcel in) {
            return new QueryContainer(in);
        }

        @Override
        public QueryContainer[] newArray(int size) {
            return new QueryContainer[size];
        }
    };

    protected void setQuery(String query) {
        this.query = query;
    }

    void putVariable(String key, Object value) {
        variables.put(key, value);
    }

    boolean containsVariable(String key) {
        return variables != null && variables.containsKey(key);
    }
}