package com.mxt.anitrend.model.entity.container.request;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import java.util.WeakHashMap;

public class GraphQueryContainer implements Parcelable {

    private String query;
    private Map<String, Object> variables;

    public GraphQueryContainer() {

    }

    public GraphQueryContainer(Parcel in) {
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

    public static final Creator<GraphQueryContainer> CREATOR = new Creator<GraphQueryContainer>() {
        @Override
        public GraphQueryContainer createFromParcel(Parcel in) {
            return new GraphQueryContainer(in);
        }

        @Override
        public GraphQueryContainer[] newArray(int size) {
            return new GraphQueryContainer[size];
        }
    };

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public GraphQueryContainer setVariable(String key, Object value) {
        if(variables == null)
            variables = new WeakHashMap<>();
        variables.put(key, value);
        return this;
    }
}
