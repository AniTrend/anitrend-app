package com.mxt.anitrend.view.activity.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityWelcomeBinding
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.onboarding.OnboardingPager
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Hosts the first-run onboarding flow and routes users into the main activity. */
class WelcomeActivity : CommonActivity() {

    private val onboardingViewModel by viewModel<OnboardingViewModel>()
    private lateinit var binding: ActivityWelcomeBinding
    private val advanceRequests = mutableIntStateOf(0)
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val pagerParams = binding.viewPager.layoutParams as ConstraintLayout.LayoutParams
            pagerParams.topMargin =
                resources.getDimensionPixelSize(R.dimen.activity_vertical_margin) + systemBars.top
            binding.viewPager.layoutParams = pagerParams

            val buttonParams = binding.actionButton.layoutParams as ConstraintLayout.LayoutParams
            buttonParams.bottomMargin =
                resources.getDimensionPixelSize(R.dimen.activity_vertical_margin) + systemBars.bottom
            binding.actionButton.layoutParams = buttonParams
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        binding.viewPager.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme {
                    OnboardingPager(
                        pages = onboardingViewModel.pages,
                        advanceRequests = advanceRequests.intValue,
                        onPageChanged = { position ->
                            currentPage = position
                            updatePageIndicator(position)
                        },
                    )
                }
            }
        }

        binding.actionButton.setOnClickListener {
            if (currentPage < onboardingViewModel.pages.lastIndex) {
                advanceRequests.intValue++
            } else {
                onFinishButtonPressed()
            }
        }
    }

    private fun updatePageIndicator(position: Int) {
        binding.indicatorContainer.children.forEachIndexed { index, view ->
            view.isSelected = index == position
        }
        binding.actionButton.setText(
            if (position == onboardingViewModel.pages.lastIndex) R.string.get_started else R.string.action_onboarding_continue,
        )
    }

    private fun onFinishButtonPressed() {
        onboardingViewModel.onPostFreshInstall()
        CompatUtil.startRevealAnim(this, binding.actionButton, Intent(this, MainActivity::class.java), true)
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
