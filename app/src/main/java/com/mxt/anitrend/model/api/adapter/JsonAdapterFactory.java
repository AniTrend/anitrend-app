package com.mxt.anitrend.model.api.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mxt.anitrend.model.api.retro.WebFactory;

import java.lang.reflect.Type;

/**
 * Created by max on 2018/01/24.
 * Checks for data types miss match and returns null
 */

public class JsonAdapterFactory<T> implements JsonDeserializer<T> {

    public static final class Factory {
        public static JsonAdapterFactory create() {
            return new JsonAdapterFactory<>();
        }
    }

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonArray() && typeOfT.getClass().isArray())
            return WebFactory.gson.fromJson(json, typeOfT);
        if(json.isJsonPrimitive() && typeOfT.getClass().isPrimitive())
            return WebFactory.gson.fromJson(json, typeOfT);

        try {
            return WebFactory.gson.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
