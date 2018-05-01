package com.mxt.anitrend.model.api.retro.base;

import com.mxt.anitrend.model.entity.base.VersionBase;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by max on 2017/04/16.
 * Base request model
 */
public interface RepositoryModel {

    String DOWNLOAD_LINK = "https://github.com/AniTrend/anitrend-resources/raw/%s/builds/app-release.apk";

    @GET("/AniTrend/anitrend-resources/{branch}/builds/meta.json")
    Call<VersionBase> checkVersion(@Path("branch") String branch);
}
