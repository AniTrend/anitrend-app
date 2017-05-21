package com.mxt.anitrend.api.call;

import com.mxt.anitrend.api.model.Staff;
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
public interface StaffModel {

    @GET("staff/{id}")
    Call<Staff> fetchStaff(@Path("id") int id);

    @GET("staff/{id}/page")
    Call<Staff> fetchStaffPage(@Path("id") int id);

    @GET("staff/search/{query}")
    Call<List<Staff>> findStaff(@Path("query") String query);

    @POST("staff/favourite")
    Call<ResponseBody> toggleFavourite(@Body Payload.ActionIdBased payload);

}
