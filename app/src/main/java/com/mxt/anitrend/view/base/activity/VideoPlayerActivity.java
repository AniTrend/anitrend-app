package com.mxt.anitrend.view.base.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.mxt.anitrend.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.jiecao.jcvideoplayer_lib.JCUserActionStandard;
import fm.jiecao.jcvideoplayer_lib.JCUtils;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private final String KEY_LINK = "key_link";
    public static final String URL_VIDEO_LINK = "video_link_url";

    private String mLink;

    @BindView(R.id.video_player)
    JCVideoPlayerStandard player;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        if (getIntent().hasExtra(URL_VIDEO_LINK))
            mLink = getIntent().getStringExtra(URL_VIDEO_LINK);
        configurePlayer();
    }

    private void configurePlayer() {
        player.setUp(mLink, JCVideoPlayer.SCREEN_WINDOW_FULLSCREEN);
        player.backButton.setOnClickListener(this);
        player.tinyBackImageView.setVisibility(View.INVISIBLE);
        player.fullscreenButton.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_shrink);
        player.fullscreenButton.setOnClickListener(this);
        player.setSystemTimeAndBattery();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (this.mLink == null)
            Toast.makeText(this, R.string.text_error_request, Toast.LENGTH_LONG).show();
        else
            player.startButton.performClick();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress())
            return;
        super.onBackPressed();
    }

    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_LINK, this.mLink);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            this.mLink = savedInstanceState.getString(KEY_LINK);
    }

    private void destoryActivity() {
        onBackPressed();
        if(!isDestroyed() || !isFinishing())
            finish();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                destoryActivity();
                break;
            case fm.jiecao.jcvideoplayer_lib.R.id.fullscreen:
                destoryActivity();
                break;
        }
    }
}
