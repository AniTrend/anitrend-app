package com.mxt.anitrend.view.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.mxt.anitrend.R;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.view.activity.index.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/11/09.
 */

public class WelcomeActivity extends AhoyOnboarderActivity {

    private List<AhoyOnboarderCard> ahoyPages;

    private AhoyOnboarderCard applyStyle(AhoyOnboarderCard ahoyOnboarderCard) {
        ahoyOnboarderCard.setBackgroundColor(R.color.black_transparent);
        ahoyOnboarderCard.setTitleColor(R.color.grey_200);
        ahoyOnboarderCard.setDescriptionColor(R.color.grey_300);
        return ahoyOnboarderCard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ahoyPages = new ArrayList<>(CompatUtil.getListFromArray(
                applyStyle(new AhoyOnboarderCard(getString(R.string.app_name), getString(R.string.app_greeting), R.drawable.ic_launcher_round_web)),
                applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_colors_title), getString(R.string.app_intro_colors_text), R.drawable.intro_colorstates)),
                applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_content_title), getString(R.string.app_intro_content_text), R.drawable.intro_anime_manga)),
                applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_search_title), getString(R.string.app_intro_search_text), R.drawable.intro_search)),
                applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_videos_title), getString(R.string.app_intro_videos_text), R.drawable.intro_youtube))));

        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.finish_button_style));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setFinishButtonTitle(R.string.get_started);
        showNavigationControls(true);
        setGradientBackground();
        setOnboardPages(ahoyPages);
    }

    @Override
    public void onFinishButtonPressed() {
        View target = findViewById(com.codemybrainsout.onboarder.R.id.btn_skip);
        CompatUtil.startRevealAnim(this, target, new Intent(WelcomeActivity.this, MainActivity.class), true);
    }
}
