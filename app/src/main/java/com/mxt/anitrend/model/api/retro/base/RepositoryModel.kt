package com.mxt.anitrend.model.api.retro.base

import com.mxt.anitrend.model.entity.base.VersionBase

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by max on 2017/04/16.
 * Base request model
 */
interface RepositoryModel {

    @GET("/AniTrend/anitrend-app/raw/{branch}/app/.meta/version.json")
    fun checkVersion(
            @Path("branch"
            ) branch: String?): Call<VersionBase>

    companion object {
        const val DOWNLOAD_LINK = "https://github.com/AniTrend/anitrend-app/releases/download/%s/app%s-release.apk"
    }
}
