package com.mxt.anitrend;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mxt.anitrend.api.call.SeriesModel;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.core.Token;
import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.service.AuthService;
import com.mxt.anitrend.api.structure.UserAnimeStats;
import com.mxt.anitrend.api.structure.UserLists;
import com.mxt.anitrend.api.structure.UserMangaStats;
import com.mxt.anitrend.custom.deserializer.AnimeStatsDeserializer;
import com.mxt.anitrend.custom.deserializer.FavouritesDeserializer;
import com.mxt.anitrend.custom.deserializer.MangaStatsDeserializer;
import com.mxt.anitrend.custom.deserializer.UserListsDeserializer;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by max on 2017/05/05.
 */
@RunWith(AndroidJUnit4.class)
public class ApiTest {

    private Context mContext;
    private Token mToken;

    public <S> S createTestService(Class<S> serviceClass) {

        if(mToken == null)
            mToken = AuthService.requestClientTokenSync();
        else if (mToken.getExpires() < (System.currentTimeMillis()/1000L))
            mToken = AuthService.requestClientTokenSync();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        //customized Gson builder to handle other converting mismatching object types
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Favourite.class, new FavouritesDeserializer())
                .registerTypeHierarchyAdapter(UserLists.class, new UserListsDeserializer())
                .registerTypeHierarchyAdapter(UserMangaStats.class, new MangaStatsDeserializer())
                .registerTypeHierarchyAdapter(UserAnimeStats.class, new AnimeStatsDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_LINK)
                .addConverterFactory(GsonConverterFactory.create(gson));

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original_ref = chain.request();
                Request request = original_ref.newBuilder()
                        .header(BuildConfig.HEADER_KEY, mToken.getHeaderValuePresets())
                        .method(original_ref.method(), original_ref.body())
                        .build();
                // W/System.err: java.net.ConnectException: Failed to connect to anilist.co
                return chain.proceed(request);
            }
        });
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    @Test
    public void getContext() {
        mContext = InstrumentationRegistry.getTargetContext();
        assertNotNull(mContext);
    }

    @Test
    public void getUser() throws IOException {
        Response<User> response = createTestService(UserModel.class).fetchUser("devtest").execute();
        assertNotNull(response);
        assertEquals(true, response.isSuccessful());
        assertNotNull(response.body());
        User mUser = response.body();
        assertNotNull(mUser.getDisplay_name());
        Log.d("getUser", mUser.getDisplay_name());
    }

    @Test
    public void getAnime() throws IOException {
        Response<List<Series>> response = createTestService(SeriesModel.class).fetchBrowsable("anime").execute();
        assertEquals(true, response.isSuccessful());
        assertNotNull(response.body());
        List<Series> series = response.body();
        assertTrue(series.size() > 0);
        Log.d("getAnime", String.valueOf(series.size()));
    }

    @Test
    public void getManga() throws IOException {
        Response<List<Series>> response = createTestService(SeriesModel.class).fetchBrowsable("manga").execute();
        assertEquals(true, response.isSuccessful());
        assertNotNull(response.body());
        List<Series> series = response.body();
        assertTrue(series.size() > 0);
        Log.d("getManga", String.valueOf(series.size()));
    }
}
