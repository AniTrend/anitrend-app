package com.mxt.anitrend.model.api.retro;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.extension.KoinExt;
import com.mxt.anitrend.model.api.converter.AniGraphConverter;
import com.mxt.anitrend.model.api.interceptor.AuthInterceptor;
import com.mxt.anitrend.model.api.interceptor.CacheInterceptor;
import com.mxt.anitrend.model.api.interceptor.NetworkCacheInterceptor;
import com.mxt.anitrend.model.api.retro.anilist.AuthModel;
import com.mxt.anitrend.model.api.retro.base.GiphyModel;
import com.mxt.anitrend.model.api.retro.base.RepositoryModel;
import com.mxt.anitrend.model.api.retro.crunchy.EpisodeModel;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.graphql.AniGraphErrorUtilKt;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import timber.log.Timber;

/**
 * Created by max on 2017/10/14.
 * Retrofit service factory
 */

public class WebFactory {

    public final static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient().create();

    public final static String API_AUTH_LINK =
            String.format("%sauthorize?grant_type=%s&client_id=%s&redirect_uri=%s&response_type=%s",
                    BuildConfig.API_AUTH_LINK, KeyUtil.AUTHENTICATION_CODE,
                    BuildConfig.CLIENT_ID, BuildConfig.REDIRECT_URI,
                    BuildConfig.RESPONSE_TYPE);

    private static Retrofit mRetrofit, mGiphy;

    /**
     * Creates a standard HttpBuilder with most common likely used configuration and optionally
     * will include http logging based off a given log level.
     * @see HttpLoggingInterceptor#setLevel(HttpLoggingInterceptor.Level)
     * <br/>
     *
     * @param interceptor Optional interceptor of your own implementation
     * @param logLevel Mandatory log level that the logging http interceptor should use
     */
    private static OkHttpClient.Builder createHttpClient(@Nullable Interceptor interceptor, @NonNull HttpLoggingInterceptor.Level logLevel) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .readTimeout(35, TimeUnit.SECONDS)
                .connectTimeout(35, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor()
                    .setLevel(logLevel);
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);
        }

        if(interceptor != null)
            okHttpClientBuilder.addInterceptor(interceptor);
        return okHttpClientBuilder;
    }

    /**
     * Generates retrofit service classes in a background thread
     * and handles creation of API tokens or renewal of them
     * <br/>
     *
     * @param serviceClass The interface class to use such as
     *
     * @param context A valid application, fragment or activity context but must be application context
     */
    public static <S> S createService(@NonNull Class<S> serviceClass, Context context) {
        WebTokenRequest.getToken(context);
        if(mRetrofit == null) {
            OkHttpClient.Builder httpClient = createHttpClient(
                    KoinExt.get(AuthInterceptor.class),
                    HttpLoggingInterceptor.Level.HEADERS
            );

            mRetrofit = new Retrofit.Builder().client(httpClient.build())
                    .addConverterFactory(AniGraphConverter.Companion.create(context))
                    .baseUrl(BuildConfig.API_LINK)
                    .build();
        }
        return mRetrofit.create(serviceClass);
    }

    public static EpisodeModel createCrunchyService(boolean feeds, Context context) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(feeds?BuildConfig.FEEDS_LINK:BuildConfig.CRUNCHY_LINK)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
                .client(createHttpClient(new CacheInterceptor(context, true), HttpLoggingInterceptor.Level.HEADERS)
                        .addNetworkInterceptor(new NetworkCacheInterceptor(context, true))
                        .cache(CompatUtil.INSTANCE.cacheProvider(context)).build())
                .build();
        return retrofit.create(EpisodeModel.class);
    }

    public static GiphyModel createGiphyService(Context context) {
        if(mGiphy == null) {
            mGiphy = new Retrofit.Builder().baseUrl(BuildConfig.GIPHY_LINK)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(createHttpClient(new CacheInterceptor(context, true), HttpLoggingInterceptor.Level.HEADERS)
                            .addNetworkInterceptor(new NetworkCacheInterceptor(context, true))
                            .cache(CompatUtil.INSTANCE.cacheProvider(context)).build())
                    .build();
        }
        return mGiphy.create(GiphyModel.class);
    }

    public static RepositoryModel createRepositoryService() {
        return new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
                .client(createHttpClient(null, HttpLoggingInterceptor.Level.HEADERS).build())
                .baseUrl(BuildConfig.APP_REPO).build().create(RepositoryModel.class);
    }

    /**
     * Gets a new access token using the authentication code code provided from a callback
     */
    public static @Nullable WebToken requestCodeTokenSync(String code) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(createHttpClient(null, HttpLoggingInterceptor.Level.HEADERS)
                    .build()).addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(BuildConfig.API_AUTH_LINK)
                    .build();

            Call<WebToken> refreshTokenCall = retrofit.create(AuthModel.class).getAuthRequest(KeyUtil.AUTHENTICATION_CODE,
                                BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, BuildConfig.REDIRECT_URI, code);

            Response<WebToken> response = refreshTokenCall.execute();
            if(!response.isSuccessful())
                Timber.tag("requestCodeTokenSync").w(AniGraphErrorUtilKt.apiError(response));
            return response.body();
        } catch (Exception ex) {
            Timber.tag("requestCodeTokenSync").e(ex);
            ex.printStackTrace();
            return null;
        }
    }

    public static void invalidate() {
        mRetrofit = null;
        mGiphy = null;
    }
}
