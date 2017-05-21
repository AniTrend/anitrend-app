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
            new AhoyOnboarderCard("AniTrend", "Anime Trend Features Overview", R.drawable.ic_launcher_web),
            new AhoyOnboarderCard("Color States", "Colors Representing Anime/Manga Status!", R.drawable.intro_colorstates),
            new AhoyOnboarderCard("Content", "Multiple Anime & Manga Listings!", R.drawable.intro_anime_manga),
            new AhoyOnboarderCard("Searching", "Anime & Manga Search Functionality!", R.drawable.intro_search),
            new AhoyOnboarderCard("Videos", "Using Builtin Youtube Video Player!", R.drawable.intro_youtube),
            new AhoyOnboarderCard("Web View", "Builtin Web Browser For Viewing Links!", R.drawable.into_webview)
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

        setFinishButtonTitle("Get Started");
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
