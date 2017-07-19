package com.mxt.anitrend.api.service;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.api.call.TokenModel;
import com.mxt.anitrend.api.core.Token;
import com.mxt.anitrend.util.KeyUtils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.mxt.anitrend.util.KeyUtils.GrantTypes;

/**
 * Created by Maxwell on 10/7/2016.
 * Handles authentication service
 */

public final class AuthService {

    private final static Retrofit mBuilder = new Retrofit.Builder()
                                                .baseUrl(BuildConfig.API_LINK)
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();

    /**
     * Normal Access Token
     * Asynchronously
     */
    public static void requestClientTokenAsync(Callback<Token> tokenCallback) {
        Call<Token> getAccessToken =
                mBuilder.create((TokenModel.class))
                        .getAccessToken(GrantTypes[KeyUtils.AUTHENTICATION_TYPE], BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET);
        getAccessToken.enqueue(tokenCallback);
    }

    /**
     * Normal Access Token
     * Synchronously
     */
    public static Token requestClientTokenSync() {
        try {
            Call<Token> getAccessToken =
                    mBuilder.create((TokenModel.class))
                            .getAccessToken(GrantTypes[KeyUtils.AUTHENTICATION_TYPE],
                                    BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET);

            Response<Token> response = getAccessToken.execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * New login requests only
     */
    public static Token requestCodeTokenSync(String code) {
        try {
            Call<Token> getAuthRequest =
                    mBuilder.create((TokenModel.class))
                            .getAuthRequest(GrantTypes[KeyUtils.AUTHENTICATION_CODE],
                                    BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, BuildConfig.REDIRECT_URI, code);

            Response<Token> response = getAuthRequest.execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void requestRefreshTokenAsync(Callback<Token> callback, String refresh) {
        Call<Token> getAuthRequest =
                mBuilder.create((TokenModel.class))
                        .getAccessToken(GrantTypes[KeyUtils.REFRESH_TYPE],
                                BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, refresh);

        getAuthRequest.enqueue(callback);
    }

    /**
     * Requests token on demand when the service generator needs to assign headers
     */
    public static Token requestRefreshTokenSync(String refresh) {
        try {
            Call<Token> getAuthRequest =
                    mBuilder.create((TokenModel.class))
                            .getAccessToken(GrantTypes[KeyUtils.REFRESH_TYPE],
                                    BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, refresh);

            Response<Token> response= getAuthRequest.execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
