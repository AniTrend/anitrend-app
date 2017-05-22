package com.mxt.anitrend.api.service;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.api.call.EpisodeModel;
import com.mxt.anitrend.api.call.Hub;
import com.mxt.anitrend.api.call.RepoModel;
import com.mxt.anitrend.api.core.Cache;
import com.mxt.anitrend.api.core.Token;
import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.api.structure.UserAnimeStats;
import com.mxt.anitrend.api.structure.UserLists;
import com.mxt.anitrend.api.structure.UserMangaStats;
import com.mxt.anitrend.async.TokenReference;
import com.mxt.anitrend.custom.deserializer.AnimeStatsDeserializer;
import com.mxt.anitrend.custom.deserializer.FavouritesDeserializer;
import com.mxt.anitrend.custom.deserializer.MangaStatsDeserializer;
import com.mxt.anitrend.custom.deserializer.UserListsDeserializer;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static com.mxt.anitrend.api.core.GrantTypes.AUTHENTICATION_CODE;

/**
 * Created by Maxwell on 10/2/2016.
 */
public class ServiceGenerator {

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static OkHttpClient.Builder baseClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS);

    // Customized Gson builder to handle other converting mismatching object types
    // Which may cause the application to crash, thus custom type checking is handled here
    private static Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Favourite.class, new FavouritesDeserializer())
            .registerTypeHierarchyAdapter(UserLists.class, new UserListsDeserializer())
            .registerTypeHierarchyAdapter(UserMangaStats.class, new MangaStatsDeserializer())
            .registerTypeHierarchyAdapter(UserAnimeStats.class, new AnimeStatsDeserializer())
            .enableComplexMapKeySerialization()
            .create();

    private static Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_LINK)
                    .addConverterFactory(GsonConverterFactory.create(gson));

    private static Retrofit app_repo = new Retrofit.Builder()
            .baseUrl(BuildConfig.APP_REPO)
            .addConverterFactory(GsonConverterFactory.create())
            .client(baseClient.build()).build();

    private static Retrofit.Builder ep_ret = new Retrofit.Builder()
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(baseClient.build());

    final static String[] API_TOKEN_TYPE = {"client_credentials", "authorization_code", "refresh_token"};

    public final static String API_AUTH_LINK = String.format("https://anilist.co/api/auth/authorize?grant_type=%s&client_id=%s&redirect_uri=%s&response_type=%s",
            API_TOKEN_TYPE[AUTHENTICATION_CODE.ordinal()],
            BuildConfig.CLIENT_ID,
            BuildConfig.REDIRECT_URI,
            BuildConfig.RESPONSE_TYPE);

    private static Retrofit ani_ret;

    /**
     * Method returns a Call<S> to perform retrofit operations on
     * Should be called in an Async Task/Thread to avoid network calls on main thread exceptions
     * <br/>
     * @param serviceClass Any interface with @Call<T> return types
     * @param mContext Usual context object
     * <br/>
     * @return Retrofit Class from the serviceClass param
     */
    public static <S> S createService(Class<S> serviceClass, Context mContext) {
        try {
            Token mToken = TokenReference.getInstance();
            if (mToken == null) {
                mToken = new TokenReference(mContext).reInitInstance();
            }
            long currentTime = System.currentTimeMillis() / 1000;
            long expires = mToken.getExpires();

            if (currentTime > expires)
                mToken = new TokenReference(mContext).reInitInstance();
            else if (expires > currentTime && ani_ret != null) {
                return ani_ret.create(serviceClass);
            }

            AuthInterceptor interceptor = new AuthInterceptor(mToken);
            if (httpClient.interceptors().size() > 0) {
                httpClient.interceptors().clear();
                httpClient.addInterceptor(interceptor);
            } else {
                httpClient.addInterceptor(interceptor);
            }
            ani_ret = builder.client(httpClient.build()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ani_ret.create(serviceClass);
    }

    public static EpisodeModel createCrunchyService(boolean feeds) {
        return ep_ret.baseUrl(feeds?BuildConfig.FEEDS_LINK:BuildConfig.CRUNCHY_LINK)
                .build().create(EpisodeModel.class);
    }

    public static RepoModel createRepoService() {
        return app_repo.create(RepoModel.class);
    }

    public static Hub createHubService() {
        return ep_ret.baseUrl(BuildConfig.HUB_BASE_LINK)
                .build().create(Hub.class);
    }

    /**
     * Prepares the application for new authentication type
     */
    public static void authStateChange(Context mContext) {
        new Cache(mContext).invalidateCachedToken();
        TokenReference.invalidateInstance();
        httpClient.interceptors().clear();
        ani_ret = null;
    }
}
