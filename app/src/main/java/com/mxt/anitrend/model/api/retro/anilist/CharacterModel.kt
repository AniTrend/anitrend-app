package com.mxt.anitrend.model.api.retro.anilist

import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import io.github.wax911.library.annotation.GraphQuery
import io.github.wax911.library.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by max on 2018/03/20.
 * Character relation queries
 */

interface CharacterModel {

    @POST("/")
    @GraphQuery("CharacterBase")
    @Headers("Content-Type: application/json")
    fun getCharacterBase(@Body request: QueryContainerBuilder?): Call<AniListContainer<CharacterBase>>

    @POST("/")
    @GraphQuery("CharacterOverview")
    @Headers("Content-Type: application/json")
    fun getCharacterOverview(@Body request: QueryContainerBuilder?): Call<AniListContainer<MediaCharacter>>

    @POST("/")
    @GraphQuery("CharacterMedia")
    @Headers("Content-Type: application/json")
    fun getCharacterMedia(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>

    @POST("/")
    @GraphQuery("CharacterActors")
    @Headers("Content-Type: application/json")
    fun getCharacterActors(@Body request: QueryContainerBuilder?): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>
}
