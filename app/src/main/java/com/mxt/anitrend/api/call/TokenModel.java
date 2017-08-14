package com.mxt.anitrend.api.call;


import com.mxt.anitrend.api.core.Token;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Maxwell on 10/7/2016.
 *
 */

public interface TokenModel {

    /**
     * If the resource owner accepts the client, they will be redirected to the client’s redirected uri.
     * A code parameter will be included in the redirect uri, this is not the access token,
     * but instead the authorization code which will be exchanged for the access token in the next step.
     *
     * @param code Authorization code from previous request
     */
    @POST("auth/access_token")
    Call<Token> getAuthRequest(@Query("grant_type") String grant_type,
                                   @Query("client_id") String client_id,
                                   @Query("client_secret") String client_secret,
                                   @Query("redirect_uri") String redirect_uri,
                                   @Query("code") String code);

    @POST("auth/access_token")
    Call<Token> getAccessToken(@Query("grant_type") String grant_type,
                               @Query("client_id") String client_id,
                               @Query("client_secret") String client_secret,
                               @Query("refresh_token") String refresh_token);

    @POST("auth/access_token")
    Call<Token> getAccessToken(@Query("grant_type") String grant_type,
                               @Query("client_id") String client_id,
                               @Query("client_secret") String client_secret);

    @FormUrlEncoded
    @POST("client/revoke")
    Call<ResponseBody> revokeAccessToken(@Field("id") String client_id);

}
