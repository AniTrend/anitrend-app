package com.mxt.anitrend.view.activity.base

import android.content.Intent
import android.os.Build
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
        card.setBackgroundColor(ContextCompat.getColor(this, R.color.black_transparent))
        card.setTitleColor(R.color.grey_200)
        card.setDescriptionColor(R.color.grey_300)
        return card
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isModernIcons = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        val paintIcon = if (isModernIcons) R.drawable.ic_format_paint_white_24dp else R.drawable.ic_format_paint_white_48dp
        val chartIcon = if (isModernIcons) R.drawable.ic_bubble_chart_white_24dp else R.drawable.ic_bubble_chart_white_48dp
        val searchIcon = if (isModernIcons) R.drawable.ic_search_white_24dp else R.drawable.ic_search_white_48dp

        ahoyPages = listOf(
            AhoyOnboarderCard(
                getString(R.string.app_name),
                "${getString(R.string.app_greeting)} ${getString(R.string.app_provider)}",
                R.mipmap.ic_launcher
            ),
            AhoyOnboarderCard(
                getString(R.string.app_intro_colors_title),
                getString(R.string.app_intro_colors_text),
                paintIcon
            ),
            AhoyOnboarderCard(
                getString(R.string.app_intro_content_title),
                getString(R.string.app_intro_content_text),
                chartIcon
            ),
            AhoyOnboarderCard(
                getString(R.string.app_intro_search_title),
                getString(R.string.app_intro_search_text),
                searchIcon
            )
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
        val target = findViewById<View>(com.codemybrainsout.onboarder.R.id.btn_skip)
        CompatUtil.startRevealAnim(this, target, Intent(this, MainActivity::class.java), true)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}
