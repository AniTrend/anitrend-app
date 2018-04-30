package com.mxt.anitrend.model.entity.container.request;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.Map;

/**
 * Created by max on 2018/03/16.
 * Query & Variable builder for graph requests
 */
public class QueryContainerBuilder implements Parcelable {

    private QueryContainer queryContainer;

    public QueryContainerBuilder() {
        queryContainer = new QueryContainer();
    }

    protected QueryContainerBuilder(Parcel in) {
        queryContainer = in.readParcelable(QueryContainer.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(queryContainer, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QueryContainerBuilder> CREATOR = new Creator<QueryContainerBuilder>() {
        @Override
        public QueryContainerBuilder createFromParcel(Parcel in) {
            return new QueryContainerBuilder(in);
        }

        @Override
        public QueryContainerBuilder[] newArray(int size) {
            return new QueryContainerBuilder[size];
        }
    };

    public QueryContainerBuilder setQuery(String query) {
        this.queryContainer.setQuery(query);
        return this;
    }

    public QueryContainerBuilder putVariable(String key, Object value) {
        queryContainer.putVariable(key, value);
        return this;
    }

    public @Nullable Object getVariable(String key) {
        if(containsVariable(key))
            return queryContainer.variables.get(key);
        return null;
    }

    public boolean containsVariable(String key) {
        return queryContainer.containsVariable(key);
    }

    public QueryContainer build() {
        queryContainer.variables = Stream.of(queryContainer.variables)
                .filter(value -> value.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return queryContainer;
    }
}
