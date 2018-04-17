package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

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
    Call<GraphContainer<StaffBase>> getStaffBase(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StaffOverview")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<StaffBase>> getStaffOverview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StaffMedia")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<PageContainer<MediaBase>>>> getStaffMedia(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StaffRoles")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>> getStaffRoles(@Body QueryContainerBuilder request);
}
