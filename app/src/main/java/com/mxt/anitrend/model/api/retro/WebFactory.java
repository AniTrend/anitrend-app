package com.mxt.anitrend.model.api.retro;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.base.custom.annotation.processor.GraphProcessor;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.model.api.converter.GraphQLConverter;
import com.mxt.anitrend.model.api.interceptor.AuthInterceptor;
import com.mxt.anitrend.model.api.interceptor.CacheInterceptor;
import com.mxt.anitrend.model.api.interceptor.NetworkCacheInterceptor;
import com.mxt.anitrend.model.api.retro.anilist.AuthModel;
import com.mxt.anitrend.model.api.retro.base.GiphyModel;
import com.mxt.anitrend.model.api.retro.base.RepositoryModel;
import com.mxt.anitrend.model.api.retro.crunchy.EpisodeModel;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by max on 2017/10/14.
 * Retrofit service factory
 */

public class WebFactory {

    public final static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient().create();

    private final static OkHttpClient.Builder baseClient = new OkHttpClient.Builder()
            .readTimeout(35, TimeUnit.SECONDS)
            .connectTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true);

    private final static Retrofit.Builder crunchyBuilder = new Retrofit.Builder()
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict());

    private final static Retrofit.Builder giphyBuilder = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BuildConfig.GIPHY_LINK);

    public final static String API_AUTH_LINK =
            String.format("%sauthorize?grant_type=%s&client_id=%s&redirect_uri=%s&response_type=%s",
                    BuildConfig.API_AUTH_LINK, KeyUtil.AUTHENTICATION_CODE,
                    BuildConfig.CLIENT_ID, BuildConfig.REDIRECT_URI,
                    BuildConfig.RESPONSE_TYPE);

    private static Retrofit mRetrofit, mCrunchy, mGiphy, mAuth;

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
        GraphProcessor.getInstance(context);
        if(mRetrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .readTimeout(35, TimeUnit.SECONDS)
                    .connectTimeout(35, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor(context))
                    .retryOnConnectionFailure(true);

            if(BuildConfig.DEBUG) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(httpLoggingInterceptor);
            }

            mRetrofit = new Retrofit.Builder().client(httpClient.build())
                    .addConverterFactory(GraphQLConverter.create(context))
                    .baseUrl(BuildConfig.API_LINK)
                    .build();
        }
        return mRetrofit.create(serviceClass);
    }

    public static EpisodeModel createCrunchyService(boolean feeds, Context context) {
        if(mCrunchy == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .readTimeout(45, TimeUnit.SECONDS)
                    .connectTimeout(45, TimeUnit.SECONDS)
                    .addInterceptor(new CacheInterceptor(context, true))
                    .addNetworkInterceptor(new NetworkCacheInterceptor(context, true))
                    .cache(CompatUtil.cacheProvider(context))
                    .retryOnConnectionFailure(true);

            if(BuildConfig.DEBUG) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BASIC);
                httpClient.addInterceptor(httpLoggingInterceptor);
            }
            crunchyBuilder.client(httpClient.build());
        }
        mCrunchy = crunchyBuilder.baseUrl(feeds?BuildConfig.FEEDS_LINK:BuildConfig.CRUNCHY_LINK).build();
        return mCrunchy.create(EpisodeModel.class);
    }

    public static GiphyModel createGiphyService(Context context) {
        if(mGiphy == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .readTimeout(35, TimeUnit.SECONDS)
                    .connectTimeout(35, TimeUnit.SECONDS)
                    .addInterceptor(new CacheInterceptor(context, true))
                    .addNetworkInterceptor(new NetworkCacheInterceptor(context, true))
                    .cache(CompatUtil.cacheProvider(context))
                    .retryOnConnectionFailure(true);

            if(BuildConfig.DEBUG) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BASIC);
                httpClient.addInterceptor(httpLoggingInterceptor);
            }
            mGiphy = giphyBuilder.client(httpClient.build()).build();
        }
        return mGiphy.create(GiphyModel.class);
    }

    public static RepositoryModel createRepositoryService() {
        return new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
                .client(baseClient.build()).baseUrl(BuildConfig.APP_REPO)
                .build().create(RepositoryModel.class);
    }

    /**
     * Gets a new access token using the authentication code code provided from a callback
     */
    public static @Nullable WebToken requestCodeTokenSync(String code) {
        try {
            if(mAuth == null)
                mAuth = new Retrofit.Builder().client(baseClient.build())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .baseUrl(BuildConfig.API_AUTH_LINK)
                        .build();
            Call<WebToken> refreshTokenCall = mAuth.create(AuthModel.class).getAuthRequest(KeyUtil.AUTHENTICATION_CODE,
                                BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, BuildConfig.REDIRECT_URI, code);

            Response<WebToken> response = refreshTokenCall.execute();
            if(!response.isSuccessful())
                Log.e("requestCodeTokenSync", ErrorUtil.getError(response));
            return response.body();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void invalidate() {
        mRetrofit = null;
        mCrunchy = null;
        mGiphy = null;
    }
}
