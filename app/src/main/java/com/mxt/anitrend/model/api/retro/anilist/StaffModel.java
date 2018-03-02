package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.model.entity.anilist.Character;
import com.mxt.anitrend.model.entity.anilist.Staff;
import com.mxt.anitrend.model.entity.base.StaffBase;

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
public interface StaffModel {

    @GET("staff/{id}")
    Call<Character> getStaff(@Path("id") long id);

    @GET("staff/{id}/page")
    Call<Staff> getStaffPage(@Path("id") long id);

    @GET("staff/search/{query}")
    Call<List<StaffBase>> findStaff(@Path("query") String query, @Query("page") int page);

    @FormUrlEncoded
    @POST("staff/favourite")
    Call<ResponseBody> toggleFavourite(@Field("id") long id);

}
