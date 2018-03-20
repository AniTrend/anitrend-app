package com.mxt.anitrend.model.api.retro.base;

import com.mxt.anitrend.model.entity.base.Version;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by max on 2017/04/16.
 * Base request model
 */
public interface RepositoryModel {

    String DOWNLOAD_LINK = "https://github.com/wax911/anitrend-resources/raw/master/builds/app-release.apk";

    @GET("/wax911/anitrend-resources/{branch}/builds/meta.json")
    Call<Version> checkVersion(@Path("branch") String branch);

    @GET("/wax911/anitrend-resources/master/builds/meta.json")
    Call<Version> checkVersion();

}
