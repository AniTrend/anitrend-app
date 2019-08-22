package com.mxt.anitrend.view.activity.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ahoyPages = new ArrayList<>(CompatUtil.INSTANCE.constructListFrom(
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_name),
                            getString(R.string.app_greeting) + " " + getString(R.string.app_provider),
                            R.mipmap.ic_launcher)),
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_colors_title),
                            getString(R.string.app_intro_colors_text),
                            R.drawable.ic_format_paint_white_24dp)),
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_content_title),
                            getString(R.string.app_intro_content_text),
                            R.drawable.ic_bubble_chart_white_24dp)),
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_search_title),
                            getString(R.string.app_intro_search_text),
                            R.drawable.ic_search_white_24dp))
            ));
        } else {
            ahoyPages = new ArrayList<>(CompatUtil.INSTANCE.constructListFrom(
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_name),
                            getString(R.string.app_greeting) + " " + getString(R.string.app_provider),
                            R.mipmap.ic_launcher)),
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_colors_title),
                            getString(R.string.app_intro_colors_text),
                            R.drawable.ic_format_paint_white_48dp)),
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_content_title),
                            getString(R.string.app_intro_content_text),
                            R.drawable.ic_bubble_chart_white_48dp)),
                    applyStyle(new AhoyOnboarderCard(getString(R.string.app_intro_search_title),
                            getString(R.string.app_intro_search_text),
                            R.drawable.ic_search_white_48dp))
            ));
        }

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
        CompatUtil.INSTANCE.startRevealAnim(this, target, new Intent(WelcomeActivity.this, MainActivity.class), true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            if (hasFocus)
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
