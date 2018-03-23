package com.mxt.anitrend.model.entity.container.request;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import java.util.WeakHashMap;

public class QueryContainer implements Parcelable {

    private String query;
    private Map<String, Object> variables;

    public QueryContainer() {

    }

    public QueryContainer(Parcel in) {
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

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public QueryContainer setQuery(String query) {
        this.query = query;
        return this;
    }

    public QueryContainer setVariable(String key, Object value) {
        if(variables == null)
            variables = new WeakHashMap<>();
        variables.put(key, value);
        return this;
    }
}
