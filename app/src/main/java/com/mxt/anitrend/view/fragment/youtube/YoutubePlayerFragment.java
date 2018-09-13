package com.mxt.anitrend.view.fragment.youtube;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/12/29.
 * Youtube support player fragment
 */

public class YoutubePlayerFragment extends YouTubePlayerSupportFragment implements YouTubePlayer.OnInitializedListener {

    private MediaTrailer mediaTrailer;

    private YouTubePlayer youTubePlayer;

    public static YoutubePlayerFragment newInstance(MediaTrailer mediaTrailer) {
        Bundle args = new Bundle();
        args.putParcelable(KeyUtil.arg_media_trailer, mediaTrailer);
        YoutubePlayerFragment fragment = new YoutubePlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if(getArguments() != null)
            mediaTrailer = getArguments().getParcelable(KeyUtil.arg_media_trailer);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAlive())
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
        if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            this.youTubePlayer = youTubePlayer;
            if (!wasRestored)
                this.youTubePlayer.cueVideo(mediaTrailer.getId());
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
        return isVisible() || !isDetached() || !isRemoving() && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }
}
