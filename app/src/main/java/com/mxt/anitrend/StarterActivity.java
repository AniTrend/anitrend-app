package com.mxt.anitrend;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

import top.wefor.circularanim.CircularAnim;

public class StarterActivity extends AhoyOnboarderActivity {

    private final AhoyOnboarderCard[] onboarderCards = {
            new AhoyOnboarderCard(getString(R.string.app_name), getString(R.string.app_greeting), R.drawable.ic_launcher_web),
            new AhoyOnboarderCard(getString(R.string.app_intro_colors_title), getString(R.string.app_intro_colors_text), R.drawable.intro_colorstates),
            new AhoyOnboarderCard(getString(R.string.app_intro_content_title), getString(R.string.app_intro_content_text), R.drawable.intro_anime_manga),
            new AhoyOnboarderCard(getString(R.string.app_intro_search_title), getString(R.string.app_intro_search_text), R.drawable.intro_search),
            new AhoyOnboarderCard(getString(R.string.app_intro_videos_title), getString(R.string.app_intro_videos_text), R.drawable.intro_youtube)
    };
    private final String KEY_SHORT = "saved_shortcut";
    private int shortcut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Intent intent = getIntent();
            shortcut = intent.getIntExtra(MainActivity.REDIRECT, 0);
        } else {
            shortcut = savedInstanceState.getInt(KEY_SHORT);
        }

        List<AhoyOnboarderCard> tempCards = new ArrayList<>();

        for (AhoyOnboarderCard page : onboarderCards) {
            page.setBackgroundColor(R.color.black_transparent);
            page.setTitleColor(R.color.grey_200);
            page.setDescriptionColor(R.color.grey_300);
            tempCards.add(page);
        }

        setFinishButtonTitle(R.string.get_started);
        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.starter_finish_button));
        showNavigationControls(true);
        setGradientBackground();
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        setFont(face);
        setOnboardPages(tempCards);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SHORT, shortcut);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFinishButtonPressed() {
        CircularAnim.fullActivity(StarterActivity.this, findViewById(com.codemybrainsout.onboarder.R.id.btn_skip)).colorOrImageRes(R.color.colorDarkKnight).go(new CircularAnim.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                Intent intent = new Intent(StarterActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.REDIRECT, shortcut);
                startActivity(intent);
                finish();
            }
        });
    }
}
