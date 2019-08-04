package com.mxt.anitrend.view.activity.base;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jzvd.JZDataSource;
import cn.jzvd.Jzvd;

public class VideoPlayerActivity extends ActivityBase<Void, BasePresenter> implements View.OnClickListener {

    private String contentLink;

    @BindView(R.id.video_player)
    Jzvd player;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        if (getIntent().hasExtra(KeyUtil.arg_model)) {
            contentLink = getIntent().getStringExtra(KeyUtil.arg_model);
            onActivityReady();
        } else {
            NotifyUtil.makeText(this, R.string.text_error_request, R.drawable.ic_warning_white_18dp, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            setImmersive(true);
        setTransparentStatusBar();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        try {
            player.cancelProgressTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(Jzvd.backPress()) {
            NotifyUtil.makeText(this, R.string.text_confirm_exit, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        JZDataSource dataSource = new JZDataSource(contentLink);
        player.setUp(dataSource, Jzvd.SCREEN_FULLSCREEN);
        // player.backButton.setOnClickListener(this);
        // player.tinyBackImageView.setVisibility(View.INVISIBLE);
        player.fullscreenButton.setImageResource(R.drawable.jz_shrink);
        player.fullscreenButton.setOnClickListener(this);
        // player.clarity.setVisibility(View.GONE);
        //player.setSystemTimeAndBattery();
        updateUI();
    }

    @Override
    protected void updateUI() {
        player.startButton.performClick();
    }

    @Override
    protected void makeRequest() {

    }

    public void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
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
                onBackPressed();
                break;
            case R.id.fullscreen:
                onBackPressed();
                break;
        }
    }
}
