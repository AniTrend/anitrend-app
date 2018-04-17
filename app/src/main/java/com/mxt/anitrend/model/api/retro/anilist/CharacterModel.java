package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
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
 * Character relation queries
 */

public interface CharacterModel {

    @POST("/")
    @GraphQuery("CharacterBase")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<CharacterBase>>getCharacterBase(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("CharacterOverview")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<Character>> getCharacterOverview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("CharacterMedia")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<PageContainer<MediaBase>>>> getCharacterMedia(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("CharacterActors")
    @Headers("Content-Type: application/json")
    Call<GraphContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>> getCharacterActors(@Body QueryContainerBuilder request);
}
