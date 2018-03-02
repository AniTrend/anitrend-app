package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.model.entity.anilist.Studio;
import com.mxt.anitrend.model.entity.base.StudioBase;

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
 */
public interface StudioModel {

    /**
     * Basic
     * @return  a studio model.
     * */
    @GET("studio/{id}")
    Call<Studio> getStudio(@Path("id") long id);

    /**
     * Page Item which returns a studio model with the following:
     * <p>
     * small anime models
     *
     * @return a studio model
     */
    @GET("studio/{id}/page")
    Call<Studio> getStudioPage(@Path("id") long id);

    /**
     * Search for a type of studio
     *
     * @param query search for what?
     * @return studio models
     */
    @GET("studio/search/{query}")
    Call<List<StudioBase>> findStudio(@Path("query") String query, @Query("page") long page);

    @FormUrlEncoded
    @POST("studio/favourite")
    Call<ResponseBody> toggleFavourite(@Field("id") long id);
}
