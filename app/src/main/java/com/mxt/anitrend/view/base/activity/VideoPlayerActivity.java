package com.mxt.anitrend.view.base.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.mxt.anitrend.R;

public class VideoPlayerActivity extends AppCompatActivity implements EasyVideoCallback {

    public static final String URL_VIDEO_LINK = "video_link_url";
    private final String KEY_LINK;
    private String mLink;
    @BindView(R.id.player) EasyVideoPlayer player;

    public VideoPlayerActivity() {
        this.KEY_LINK = "key_link";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        this.player.setCallback(this);
        if (getIntent().hasExtra(URL_VIDEO_LINK)) {
            this.mLink = getIntent().getStringExtra(URL_VIDEO_LINK);
        }
    }

    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (this.mLink == null) {
            Toast.makeText(this, R.string.text_error_request, 0).show();
        } else {
            this.player.setSource(Uri.parse(this.mLink));
        }
    }

    public void onPause() {
        super.onPause();
        this.player.pause();
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("key_link", this.mLink);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            this.mLink = savedInstanceState.getString("key_link");
        }
    }

    public void onStarted(EasyVideoPlayer player) {
    }

    public void onPaused(EasyVideoPlayer player) {
    }

    public void onPreparing(EasyVideoPlayer player) {
    }

    public void onPrepared(EasyVideoPlayer player) {
    }

    public void onBuffering(int percent) {
    }

    public void onError(EasyVideoPlayer player, Exception e) {
        e.printStackTrace();
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }

    public void onCompletion(EasyVideoPlayer player) {
    }

    public void onRetry(EasyVideoPlayer player, Uri source) {
    }

    public void onSubmit(EasyVideoPlayer player, Uri source) {
    }
}
