package com.mxt.anitrend.viewmodel.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mxt.anitrend.R;
import com.mxt.anitrend.utils.ApplicationPrefs;
import com.mxt.anitrend.utils.PatternMatcher;

/**
 * Created by max on 2017/03/26.
 * AppCompatActivity extender to make activity changes on start
 */
public abstract class DefaultActivity extends AppCompatActivity {

    protected String mIntentData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(new ApplicationPrefs(this).isLightTheme() ? R.style.DarkTheme : R.style.DarkTheme_DarkSide);
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if(data != null)
            mIntentData = PatternMatcher.findIntentKey(data.getPath());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Optionally allowed to override
     */
    protected void startInit() {

    }

    protected abstract void updateUI();
}
