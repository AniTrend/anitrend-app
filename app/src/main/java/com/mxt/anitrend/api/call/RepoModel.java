package com.mxt.anitrend.api.call;

import com.mxt.anitrend.util.AppVersionTracking;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by max on 2017/04/16.
 */
public interface RepoModel {

    String DOWNLOAD_LINK = "https://github.com/wax911/AniTrend/raw/master/builds/app-release.apk";

    @GET("/wax911/AniTrend/{branch}/builds/meta.json")
    Call<AppVersionTracking> checkVersion(@Path("branch") String branch);

    @GET("/wax911/AniTrend/master/builds/meta.json")
    Call<AppVersionTracking> checkVersion();

}
