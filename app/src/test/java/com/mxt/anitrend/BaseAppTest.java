package com.mxt.anitrend;

import com.mxt.anitrend.api.call.EpisodeModel;
import com.mxt.anitrend.api.call.Hub;
import com.mxt.anitrend.api.hub.Playlist;
import com.mxt.anitrend.api.hub.Result;
import com.mxt.anitrend.api.hub.Video;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.Rss;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class BaseAppTest {

    @Test
    public void crunchyFeedTest() throws IOException {
        EpisodeModel aniTrend = ServiceGenerator.createCrunchyService(true);

        Call<Rss> rssFeed = aniTrend.getLatestFeed();

        assertNotNull(rssFeed);
        Response<Rss> result = rssFeed.execute();
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void rssFeedTest() throws IOException {
        Hub aniTrend = ServiceGenerator.createHubService();

        Call<Result<Video>> rssFeed = aniTrend
                .getRssFeed("-addedDate", 10, 1,
                null, null,  Hub.filter, "json");

        assertNotNull(rssFeed);
        Response<Result<Video>> result = rssFeed.execute();
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void videoListTest() throws IOException {
        Hub aniTrend = ServiceGenerator.createHubService();

        Call<Result<Video>> videos = aniTrend
                .getVideos("-addedDate", 10, 1,
                        null, null,  Hub.filter);

        assertNotNull(videos);
        Response<Result<Video>> result = videos.execute();
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void playlistTest() throws IOException {
        Hub aniTrend = ServiceGenerator.createHubService();

        Call<Result<Playlist>> playlist = aniTrend
                .getPlaylist("-addedDate", 10, 1,
                        "users.addedBy", null, "{\"videos\":{\"$exists\":true}}");

        assertNotNull(playlist);
        Response<Result<Playlist>> result = playlist.execute();
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void videoTest() throws IOException {
        Hub aniTrend = ServiceGenerator.createHubService();

        Call<Result<Video>> videos = aniTrend
                .getVideos("-addedDate", 10, 1,
                        null, null,  Hub.filter);

        assertNotNull(videos);
        Response<Result<Video>> result = videos.execute();
        assertNotNull(result);
        assertTrue(result.isSuccessful());

        Result<Video> videoResult = result.body();
        assertTrue(videoResult.getCount() > 0);

        Call<Video> videoCall = aniTrend.getEpisode(videoResult.getResults().get(0).get_id());

        assertNotNull(videoCall);
        Response<Video> video = videoCall.execute();
        assertNotNull(video);
        assertTrue(video.isSuccessful());
    }
}