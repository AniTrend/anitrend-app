package com.mxt.anitrend.view.fragment.youtube

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.AdapterFeedSlideBinding
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.markdown.RegexUtil
import timber.log.Timber

class YouTubeEmbedFragment : Fragment() {
    private var mediaTrailer: MediaTrailer? = null

    private var binding: AdapterFeedSlideBinding? = null

    companion object {
        @JvmStatic
        fun newInstance(model: MediaTrailer): YouTubeEmbedFragment {
            val args =
                Bundle().apply {
                    putParcelable(KeyUtil.arg_media_trailer, model)
                }
            return YouTubeEmbedFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaTrailer = arguments?.parcelable(KeyUtil.arg_media_trailer)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val viewBinding = AdapterFeedSlideBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()
        makeRequest()
    }

    private fun updateUI() {
        val trailer = mediaTrailer ?: return
        val youtubeLink = RegexUtil.buildYoutube(trailer.id.orEmpty())
        val thumbnailUrl = RegexUtil.getYoutubeThumb(youtubeLink)
        activity?.let { host ->
            binding?.let { viewBinding ->
                Glide
                    .with(host)
                    .load(thumbnailUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .apply(RequestOptions.centerCropTransform())
                    .into(viewBinding.feedStatusImage)
            }
        }
    }

    private fun makeRequest() {
        val trailer = mediaTrailer ?: return
        binding?.feedStatusImage?.setOnClickListener {
            try {
                val youtubeLink = RegexUtil.buildYoutube(trailer.id.orEmpty())
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(youtubeLink)
                    }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
                context?.let {
                    NotifyUtil
                        .makeText(it, R.string.init_youtube_missing, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
