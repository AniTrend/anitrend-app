package com.mxt.anitrend.view.activity.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityWelcomeBinding
import com.mxt.anitrend.databinding.ItemOnboardingPageBinding
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class WelcomeActivity : CommonActivity() {

    private val onboardingViewModel by viewModel<OnboardingViewModel>()
    private lateinit var binding: ActivityWelcomeBinding

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

        val adapter = OnboardingAdapter(onboardingViewModel.pages)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageIndicator(position)
                if (position == onboardingViewModel.pages.size - 1) {
                    binding.actionButton.setText(R.string.get_started)
                } else {
                    binding.actionButton.setText(R.string.action_onboarding_continue)
                }
            }
        })
        updatePageIndicator(binding.viewPager.currentItem)

        binding.actionButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingViewModel.pages.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                onFinishButtonPressed()
            }
        }
    }

    private fun updatePageIndicator(position: Int) {
        binding.indicatorContainer.children.forEachIndexed { index, view ->
            view.isSelected = index == position
        }
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

    private class OnboardingAdapter(
        private val pages: List<OnboardingViewModel.OnboardingPage>,
    ) : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemOnboardingPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val page = pages[position]
            holder.binding.title.setText(page.titleRes)
            holder.binding.description.setText(page.descriptionRes)
            holder.binding.heroContainer.removeAllViews()
            LayoutInflater.from(holder.binding.root.context)
                .inflate(page.heroLayoutRes, holder.binding.heroContainer, true)
        }

        override fun getItemCount() = pages.size
    }
}
