package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.model.entity.anilist.Character;
import com.mxt.anitrend.model.entity.base.CharacterBase;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Maxwell on 10/4/2016.
 * Model for characters
 */
public interface CharacterModel {

    @GET("character/{id}")
    Call<Character> getCharacter(@Path("id") long id);

    @GET("character/{id}/page")
    Call<Character> getCharacterPage(@Path("id") long id);

    @GET("character/search/{query}")
    Call<List<CharacterBase>> findCharacter(@Path("query") String query, @Query("page") int page);

    @FormUrlEncoded
    @POST("character/favourite")
    Call<ResponseBody> toggleFavourite(@Field("id") long id);
}
