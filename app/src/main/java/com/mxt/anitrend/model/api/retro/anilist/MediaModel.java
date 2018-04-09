package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * Series queries
 */

public interface MediaModel {

    @POST("/")
    @GraphQuery("MediaBase")
    @Headers("Content-Type: application/json")
    Call<MediaBase> getMediaBase(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaOverview")
    @Headers("Content-Type: application/json")
    Call<Media> getMediaOverview(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaRelations")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<MediaEdge>>> getMediaRelations(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaStats")
    @Headers("Content-Type: application/json")
    Call<Media> getMediaStats(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaEpisodes")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<List<ExternalLink>>> getMediaEpisodes(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaCharacters")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<CharacterEdge>>> getMediaCharacters(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaStaff")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<StaffEdge>>> getMediaStaff(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("MediaSocial")
    @Headers("Content-Type: application/json")
    Call<PageContainer<FeedList>> getMediaSocial(@Body QueryContainerBuilder request);
}
