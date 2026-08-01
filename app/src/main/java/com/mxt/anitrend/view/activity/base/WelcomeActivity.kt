package com.mxt.anitrend.view.activity.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard
import com.mxt.anitrend.R
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.view.activity.index.MainActivity

/**
 * Created by max on 2017/11/09.
 */
class WelcomeActivity : AhoyOnboarderActivity() {
    private lateinit var ahoyPages: List<AhoyOnboarderCard>

    private fun applyStyle(card: AhoyOnboarderCard): AhoyOnboarderCard {
        card.setBackgroundColor(R.color.black_transparent)
        card.setTitleColor(R.color.grey_200)
        card.setDescriptionColor(R.color.grey_300)
        return card
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paintIcon = R.drawable.ic_format_paint_white_24dp
        val chartIcon = R.drawable.ic_bubble_chart_white_24dp
        val searchIcon = R.drawable.ic_search_white_24dp

        ahoyPages =
            listOf(
                AhoyOnboarderCard(
                    getString(R.string.app_name),
                    "${getString(R.string.app_greeting)} ${getString(R.string.app_provider)}",
                    R.mipmap.ic_launcher,
                ),
                AhoyOnboarderCard(
                    getString(R.string.app_intro_colors_title),
                    getString(R.string.app_intro_colors_text),
                    paintIcon,
                ),
                AhoyOnboarderCard(
                    getString(R.string.app_intro_content_title),
                    getString(R.string.app_intro_content_text),
                    chartIcon,
                ),
                AhoyOnboarderCard(
                    getString(R.string.app_intro_search_title),
                    getString(R.string.app_intro_search_text),
                    searchIcon,
                ),
            ).map(::applyStyle)

        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.finish_button_style))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setFinishButtonTitle(R.string.get_started)
        showNavigationControls(true)
        setGradientBackground()
        setOnboardPages(ahoyPages)
    }

    override fun onFinishButtonPressed() {
        val target = findViewById<View>(R.id.btn_skip)
        CompatUtil.startRevealAnim(this, target, Intent(this, MainActivity::class.java), true)
    }

    @Suppress("DEPRECATION")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}
