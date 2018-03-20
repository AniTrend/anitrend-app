package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * Staff queries
 */

public interface StaffModel {

    @POST("/")
    @GraphQuery("StaffBase")
    @Headers("Content-Type: application/json")
    Call<StaffBase> getStaffBase(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StaffOverview")
    @Headers("Content-Type: application/json")
    Call<StaffBase> getStaffOverview(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StaffMedia")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<String, MediaBase>>> getStaffMedia(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("StaffRoles")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<String, StaffBase>>> getStaffRoles(@Body GraphQueryContainer request);
}
