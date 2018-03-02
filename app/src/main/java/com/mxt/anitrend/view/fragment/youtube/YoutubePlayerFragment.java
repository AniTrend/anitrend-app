package com.mxt.anitrend.view.fragment.youtube;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/12/29.
 * Youtube support player fragment
 */

public class YoutubePlayerFragment extends YouTubePlayerSupportFragment implements YouTubePlayer.OnInitializedListener {

    private String streamLink;

    private YouTubePlayer youTubePlayer;

    public static YoutubePlayerFragment newInstance(Bundle args) {
        YoutubePlayerFragment fragment = new YoutubePlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if(getArguments() != null)
            streamLink = getArguments().getString(KeyUtils.arg_model);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isAlive() && !TextUtils.isEmpty(streamLink))
            initialize(BuildConfig.API_KEY, this);
    }

    @Override
    public void onDestroyView() {
        if(youTubePlayer != null)
            youTubePlayer.release();
        super.onDestroyView();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            this.youTubePlayer = youTubePlayer;
            if (!wasRestored)
                this.youTubePlayer.cueVideo(streamLink);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    /**
     * Check to see if fragment is still alive
     * @return true if the fragment is still valid otherwise false
     */
    private boolean isAlive() {
        return isVisible() || !isDetached() || !isRemoving();
    }
}
