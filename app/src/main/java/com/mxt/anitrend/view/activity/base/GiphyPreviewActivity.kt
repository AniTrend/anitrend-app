package com.mxt.anitrend.view.activity.base

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityGiphyPreviewBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings

class GiphyPreviewActivity :
    AppCompatActivity(),
    RequestListener<Drawable> {

    data class Args(val modelUrl: String)

    companion object {
        fun newIntent(context: Context, modelUrl: String): Intent = Intent(context, GiphyPreviewActivity::class.java).apply {
            putExtra(KeyUtil.arg_model, modelUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        fun fromIntent(intent: Intent): Args? = parseArgs(intent.getStringExtra(KeyUtil.arg_model))

        @VisibleForTesting
        internal fun parseArgs(raw: String?): Args? = if (!raw.isNullOrEmpty()) Args(raw) else null
    }

    private lateinit var binding: ActivityGiphyPreviewBinding

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        // Preserve configured theme (previously handled by ActivityBase.configureActivity).
        val settings = KoinExt.get(Settings::class.java)
        val themeRes = when (settings.theme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
        super.onCreate(savedInstanceState)

        binding = ActivityGiphyPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.previewClose.setOnClickListener { finish() }

        val args = fromIntent(intent)
        if (args != null) {
            Glide
                .with(this)
                .load(args.modelUrl)
                .listener(this)
                .into(binding.previewImage)
        } else {
            NotifyUtil.makeText(
                this,
                R.string.layout_empty_response,
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_SHORT,
            ).show()
            showStatus(R.string.layout_empty_response)
        }

        binding.previewCredits.setImageResource(
            if (!CompatUtil.isLightTheme(settings)) {
                R.drawable.powered_by_giphy_light
            } else {
                R.drawable.powered_by_giphy_dark
            },
        )
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Drawable>,
        isFirstResource: Boolean,
    ): Boolean {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            showStatus(R.string.text_unknown_error)
        }
        return false
    }

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>,
        dataSource: DataSource,
        isFirstResource: Boolean,
    ): Boolean {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            binding.previewLoadingContainer.isVisible = false
            binding.previewStatusCard.isVisible = false
        }
        return false
    }

    private fun showStatus(messageRes: Int) {
        binding.previewLoadingContainer.isVisible = false
        binding.previewStatusMessage.setText(messageRes)
        binding.previewStatusCard.isVisible = true
    }
}
