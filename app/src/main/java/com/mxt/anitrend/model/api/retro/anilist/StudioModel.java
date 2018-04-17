package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * Studio queries
 */

public interface StudioModel {

    @POST("/")
    @GraphQuery("StudioBase")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<StudioBase>> getStudioBase(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("StudioMedia")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<PageContainer<MediaBase>>>> getStudioMedia(@Body QueryContainerBuilder request);
}
