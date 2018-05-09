package com.mxt.anitrend.model.api.converter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mxt.anitrend.base.custom.annotation.processor.GraphProcessor;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.container.attribute.GraphError;
import com.mxt.anitrend.model.entity.container.body.DataContainer;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by max on 2017/10/22.
 * Body for GraphQL requests and responses
 */

public final class GraphQLConverter extends Converter.Factory {

    private GraphProcessor graphProcessor;

    private final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setLenient()
            .create();

    public static GraphQLConverter create(Context context) {
        return new GraphQLConverter(context);
    }

    private GraphQLConverter(Context context) {
        this.graphProcessor = GraphProcessor.getInstance(context);
    }

    @Override
    public Converter<ResponseBody, GraphContainer<?>> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new GraphResponseConverter<>(type);
    }

    @Override
    public Converter<QueryContainerBuilder, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new GraphRequestConverter(methodAnnotations);
    }

    /**
     * GraphQL response body converter to unwrap nested object results,
     * resulting in a smaller generic tree for requests
     */
    private class GraphResponseConverter<T> implements Converter<ResponseBody, T> {
        private Type type;

        GraphResponseConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(@NonNull ResponseBody responseBody) {
            T targetResult = null;
            String jsonResponse = null;
            try {
                jsonResponse = responseBody.string();
                GraphContainer<T> container = gson.fromJson(jsonResponse, type);
                if(!container.isEmpty() && !container.getData().isEmpty()) {
                    DataContainer<T> dataContainer = container.getData();
                    targetResult = dataContainer.getResult();
                } else
                    for (GraphError error: container.getErrors())
                        Log.e(this.toString(), error.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("GraphQLConverter", jsonResponse);
            } finally {
                responseBody.close();
            }
            return targetResult;
        }
    }

    /**
     * GraphQL request body converter and injector, uses method annotation for a given retrofit call
     */
    private class GraphRequestConverter implements Converter<QueryContainerBuilder, RequestBody> {
        private Annotation[] methodAnnotations;

        GraphRequestConverter(Annotation[] methodAnnotations) {
            this.methodAnnotations = methodAnnotations;
        }

        @Override
        public RequestBody convert(@NonNull QueryContainerBuilder containerBuilder) {
            QueryContainer queryContainer = containerBuilder
                    .setQuery(graphProcessor.getQuery(methodAnnotations))
                    .build();
            String queryJson = gson.toJson(queryContainer);
            Log.d("GraphRequestConverter", queryJson);
            return RequestBody.create(MediaType.parse("application/graphql"), queryJson);
        }
    }
}