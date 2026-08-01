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
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity
import timber.log.Timber

class VideoPlayerActivity : CommonActivity() {

    /**
     * Navigation args for [VideoPlayerActivity]. Use [newIntent] to build and
     * [fromIntent] to read. Wire format key: [KeyUtil.arg_model].
     */
    data class Args(val contentLink: String)

    companion object {
        fun newIntent(context: Context, contentLink: String): Intent = Intent(context, VideoPlayerActivity::class.java).apply {
            putExtra(KeyUtil.arg_model, contentLink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        fun fromIntent(intent: Intent): Args? = parseArgs(intent.getStringExtra(KeyUtil.arg_model))

        @VisibleForTesting
        internal fun parseArgs(raw: String?): Args? = if (!raw.isNullOrEmpty()) Args(raw) else null
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
            contentLink = args.contentLink
            startPlayer(args.contentLink)
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
