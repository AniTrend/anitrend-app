package com.mxt.anitrend.api.call;



import com.mxt.anitrend.api.model.Studio;
import com.mxt.anitrend.api.model.StudioSmall;
import com.mxt.anitrend.custom.Payload;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Maxwell on 10/4/2016.
 */
public interface StudioModel {

    /**
     * Basic
     * @return  a studio model.
     * */
    @GET("studio/{id}")
    Call<Studio> fetchStudio(@Path("id") int id);

    /**
     * Page Item which returns a studio model with the following:
     * <p>
     * small anime models
     *
     * @return a studio model
     * */
    @GET("studio/{id}/page")
    Call<Studio> fetchStudioPage(@Path("id") int id);

    /**
     * Search for a type of studio
     *
     * @param query search for what?
     * @return studio models
     */
    @GET("studio/search/{query}")
    Call<List<StudioSmall>> findStudio(@Path("query") String query);

    @POST("studio/favourite")
    Call<ResponseBody> toggleFavourite(@Body Payload.ActionIdBased actionIdBased);
}
