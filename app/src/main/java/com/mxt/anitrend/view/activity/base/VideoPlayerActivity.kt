package com.mxt.anitrend.view.activity.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityVideoPlayerBinding
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity
import timber.log.Timber

class VideoPlayerActivity : CommonActivity() {

    companion object {
        fun newIntent(context: Context, param: VideoPlayerScreenParam): Intent = Intent(context, VideoPlayerActivity::class.java).apply {
            putScreenParam(param)
            // Interim boundary: keep the legacy wire key alongside the typed param
            // until all pre-bridge callers and fixtures migrate.
            putExtra(KeyUtil.arg_model, param.url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        /**
         * Compatibility overload preserving the legacy URL-based callers. Bridges
         * into the typed parameter so navigation always uses [VideoPlayerScreenParam].
         */
        fun newIntent(context: Context, contentLink: String): Intent = newIntent(context, VideoPlayerScreenParam(url = contentLink))

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Pre-bridge callers still write the
         * legacy [KeyUtil.arg_model] extra, so that value is bridged here via
         * [resolve]. The bridge is a scalar conversion point inside the activity,
         * not a parcel path for any remote model.
         */
        fun fromIntent(intent: Intent): VideoPlayerScreenParam? = resolve(
            typed = intent.screenParam<VideoPlayerScreenParam>(),
            legacyUrl = intent.getStringExtra(KeyUtil.arg_model),
        )

        /**
         * Production parsing rule for the video player destination.
         *
         * A present typed parameter wins; it is accepted only when it carries a
         * non-empty url. Otherwise the legacy [KeyUtil.arg_model] extra is bridged
         * via [parseUrl]. Blank strings remain valid, preserving the pre-refactor
         * `isNullOrEmpty` contract.
         */
        @VisibleForTesting
        internal fun resolve(typed: VideoPlayerScreenParam?, legacyUrl: String?): VideoPlayerScreenParam? {
            typed?.let { param ->
                return if (param.url.isNotEmpty()) param else null
            }
            return parseUrl(legacyUrl)
        }

        @VisibleForTesting
        internal fun parseUrl(raw: String?): VideoPlayerScreenParam? = if (!raw.isNullOrEmpty()) VideoPlayerScreenParam(url = raw) else null
    }

    private var contentLink: String? = null
    private var player: ExoPlayer? = null
    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val args = fromIntent(intent)
        if (args != null) {
            contentLink = args.url
            startPlayer(args.url)
        } else {
            NotifyUtil.makeText(
                this,
                R.string.text_error_request,
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun startPlayer(link: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.videoPlayer.player = exoPlayer
            val mediaItem = MediaItem.fromUri(link)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Timber.e(error, "Video playback error")
                    NotifyUtil.makeText(
                        this@VideoPlayerActivity,
                        R.string.text_error_request,
                        R.drawable.ic_warning_white_18dp,
                        Toast.LENGTH_LONG,
                    ).show()
                    finish()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
