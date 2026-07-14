package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivityVideoPlayerBinding
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber

class VideoPlayerActivity : ActivityBase<Void, BasePresenter>() {

    private var contentLink: String? = null
    private var player: ExoPlayer? = null
    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(KeyUtil.arg_model)) {
            contentLink = intent.getStringExtra(KeyUtil.arg_model)
            onActivityReady()
        } else {
            NotifyUtil.makeText(
                this,
                R.string.text_error_request,
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_LONG,
            ).show()
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

    override fun onActivityReady() {
        val link = contentLink ?: return
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

    override fun updateUI() {
        // Player starts automatically when prepared
    }

    override fun makeRequest() {}

    override fun onChanged(model: Void?) {}
}
