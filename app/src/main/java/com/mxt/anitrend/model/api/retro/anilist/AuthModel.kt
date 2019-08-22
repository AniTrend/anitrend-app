package com.mxt.anitrend.model.api.retro.anilist

import com.mxt.anitrend.model.entity.anilist.WebToken

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by max on 2017/10/14.
 * Authentication endpoints
 */

interface AuthModel {

    /**
     * If the resource owner accepts the client, they will be redirected to the client’s redirected uri.
     * A code parameter will be included in the redirect uri, this is not the access token,
     * but instead the authorization code which will be exchanged for the access token in the next step.
     *
     * @param code Authorization code from previous request
     */
    @FormUrlEncoded
    @POST("token")
    fun getAuthRequest(@Field("grant_type") grant_type: String,
                       @Field("client_id") client_id: String,
                       @Field("client_secret") client_secret: String,
                       @Field("redirect_uri") redirect_uri: String,
                       @Field("code") code: String): Call<WebToken>
}
