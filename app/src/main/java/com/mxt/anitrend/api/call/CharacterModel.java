package com.mxt.anitrend.api.call;

import com.mxt.anitrend.api.model.Character;
import com.mxt.anitrend.base.custom.Payload;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Maxwell on 10/4/2016.
 * Model for characters
 */
public interface CharacterModel {

    @GET("character/{id}")
    Call<Character> fetchCharacter(@Path("id") int id);

    @GET("character/{id}/page")
    Call<Character> fetchCharacterPage(@Path("id") int id);

    @GET("character/search/{query}")
    Call<List<Character>> findChatacter(@Path("query") String query);

    @POST("character/favourite")
    Call<ResponseBody> toggleFavourite(@Body Payload.ActionIdBased payload);

}
