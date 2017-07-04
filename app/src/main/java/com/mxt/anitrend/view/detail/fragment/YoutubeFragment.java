package com.mxt.anitrend.view.detail.fragment;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;


public class YoutubeFragment extends YouTubePlayerSupportFragment {

    public final static String PARAM_URL = "trailer_url";
    public final static String PARAM_KEY = "module_key";

    private final String LINK_KEY = "STREAM_LINK";
    private String stream_link;
    private final String POSITION_KEY = "SEEK_POSITION";
    private int play_position;
    private final String API_LINK_KEY = "KEY_VALUE";
    private String api_key;
    private YouTubePlayer player_reference;
    private YouTubePlayer.OnInitializedListener initializedListener;

    public static YoutubeFragment newInstance(String param, String key) {
        YoutubeFragment fragment = new YoutubeFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_URL, param);
        args.putString(PARAM_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if(bundle != null) {
            stream_link = bundle.getString(LINK_KEY);
            api_key = bundle.getString(API_LINK_KEY);
            play_position = bundle.getInt(POSITION_KEY);
        }
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            initializedListener = new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {

                }

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                    if (!wasRestored && stream_link != null) {
                        player_reference = player;
                        player_reference.cueVideo(stream_link);
                    } else if (wasRestored) {
                        player.seekToMillis(play_position);
                    }
                }
            };
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if(player_reference != null)
            player_reference.release();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(player_reference != null)
            player_reference.release();
        super.onPause();
    }

    private void init() throws Exception {
        if(isVisible() && !isDetached() || !isRemoving()) {
            String reference = getArguments().getString(PARAM_URL);
            if (reference != null && stream_link == null && api_key == null) {
                stream_link = reference;
                api_key = getArguments().getString(PARAM_KEY);
                initialize(api_key, initializedListener);
            }
        }
    }

}
