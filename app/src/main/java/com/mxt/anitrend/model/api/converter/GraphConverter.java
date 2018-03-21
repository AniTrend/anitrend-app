package com.mxt.anitrend.model.api.converter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.base.custom.annotation.processor.GraphProcessor;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.container.attribute.GraphError;
import com.mxt.anitrend.model.entity.container.body.DataContainer;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;
import com.mxt.anitrend.util.KeyUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by max on 2017/10/22.
 * Body factory default implementation
 */

public final class GraphConverter extends Converter.Factory {

    private GraphProcessor graphProcessor;

    public static GraphConverter create(Context context) {
        return new GraphConverter(context);
    }

    private GraphConverter(Context context) {
        graphProcessor = new GraphProcessor(context);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new GraphResponseConverter<>(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
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
            GraphContainer<DataContainer<T>> container;
            T targetResult = null;
            try {
                container = WebFactory.gson.fromJson(responseBody.string(), new TypeToken<GraphContainer<DataContainer<T>>>(){}.getType());
                if(container != null) {
                    if(container.isSuccess()) {
                        DataContainer<T> dataContainer = container.getData();
                        if (dataContainer.getResult() != null && !dataContainer.isEmpty()) {
                            String response = WebFactory.gson.toJson(dataContainer.getResult());
                            targetResult = WebFactory.gson.fromJson(response, type);
                        }
                    } else {
                        List<GraphError> graphErrors = container.getErrors();
                        for (GraphError error: graphErrors)
                            Log.e(this.toString(), error.toString());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                responseBody.close();
            }
            return targetResult;
        }
    }

    /**
     * GraphQL request body converter and injector, uses method annotation for a given retrofit call
     */
    private class GraphRequestConverter implements Converter<GraphQueryContainer, RequestBody> {
        private Annotation[] methodAnnotations;

        GraphRequestConverter(Annotation[] methodAnnotations) {
            this.methodAnnotations = methodAnnotations;
        }

        @Override
        public RequestBody convert(@NonNull GraphQueryContainer container) {
            container.setQuery(graphProcessor.getQuery(methodAnnotations))
                    .setVariable(KeyUtils.arg_per_page, KeyUtils.PAGING_LIMIT);
            String queryJson = WebFactory.gson.toJson(container);
            return RequestBody.create(MediaType.parse("application/graphql"), queryJson);
        }
    }
}