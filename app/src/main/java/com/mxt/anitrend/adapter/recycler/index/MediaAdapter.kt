package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.databinding.ViewDataBinding
import butterknife.OnClick
import butterknife.OnLongClick
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.*
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.KeyUtil.RecyclerViewType

/**
 * Created by max on 2017/10/25.
 * Media adapter
 */
class MediaAdapter(context: Context?, private val isCompatType: Boolean) :
    RecyclerViewAdapter<MediaBase?>(context) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        @RecyclerViewType viewType: Int
    ): RecyclerViewHolder<MediaBase?> {
        if (isCompatType)
            return MediaViewHolder(AdapterSeriesBinding.inflate(parent.context.getLayoutInflater(), parent, false))

        if (viewType == KeyUtil.RECYCLER_TYPE_ANIME)
            return createAnimeViewHolder(parent)

        return createMangaViewHolder(parent)
    }

    private fun createAnimeViewHolder(parent: ViewGroup) : AnimeViewHolder {
        val adapter = when (presenter.settings.mediaListStyle) {
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X1,
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X2 -> {
                AdapterAnimeCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
            else -> {
                AdapterAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        }
        return AnimeViewHolder(adapter)
    }

    private fun createMangaViewHolder(parent: ViewGroup) : MangaViewHolder {
        val adapter = when (presenter.settings.mediaListStyle) {
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X1,
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X2 -> {
                AdapterMangaCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
            else -> {
                AdapterMangaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        }
        return MangaViewHolder(adapter)
    }

    @RecyclerViewType
    override fun getItemViewType(position: Int): Int {
        return if (data[position]!!.type == KeyUtil.ANIME)
            KeyUtil.RECYCLER_TYPE_ANIME
        else
            KeyUtil.RECYCLER_TYPE_MANGA
    }

    override fun getFilter(): Filter? {
        return null
    }

    inner class AnimeViewHolder
    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     * @see ButterKnife
     */ internal constructor(private val binding: ViewDataBinding) :
        RecyclerViewHolder<MediaBase?>(binding.root) {

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br></br>
         *
         * @param model Is the model at the current adapter position
         * @see Media
         */
        override fun onBindViewHolder(model: MediaBase?) {
            when (binding) {
                is AdapterAnimeBinding -> {
                    binding.model = model
                    binding.seriesTitle.setTitle(model)
                }
                is AdapterAnimeCompactBinding -> {
                    binding.model = model
                    binding.seriesTitle.setTitle(model)

                    if (presenter.settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X2) {
                        binding.customRatingWidget.visibility = View.GONE
                    }
                }
                else -> {}
            }
            binding.executePendingBindings()
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
         * <br></br>
         *
         * @see Glide
         */
        override fun onViewRecycled() {
            if (binding is AdapterAnimeBinding)
                Glide.with(context).clear(binding.seriesImage)

            binding.unbind()
        }

        /**
         * Handle any onclick events from our views
         * <br></br>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @OnClick(R.id.container)
        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        @OnLongClick(R.id.container)
        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }
    }

    inner class MangaViewHolder
    /**
     * Default constructor which includes binding with butter knife
     *
     * @param view
     */ internal constructor(private val binding: ViewDataBinding) :
        RecyclerViewHolder<MediaBase?>(binding.root) {

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br></br>
         *
         * @param model Is the model at the current adapter position
         * @see Media
         */
        override fun onBindViewHolder(model: MediaBase?) {
            when (binding) {
                is AdapterMangaBinding -> {
                    binding.model = model
                    binding.seriesTitle.setTitle(model)
                }
                is AdapterMangaCompactBinding -> {
                    binding.model = model
                    binding.seriesTitle.setTitle(model)

                    if (presenter.settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X2) {
                        binding.customRatingWidget.visibility = View.GONE
                    }
                }
                else -> {}
            }
            binding.executePendingBindings()
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
         * <br></br>
         *
         * @see Glide
         */
        override fun onViewRecycled() {
            if (binding is AdapterMangaBinding)
                Glide.with(context).clear(binding.seriesImage)

            binding.unbind()
        }

        @OnClick(R.id.container)
        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        @OnLongClick(R.id.container)
        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }

    }

    inner class MediaViewHolder
    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     */ (private val binding: AdapterSeriesBinding) :
        RecyclerViewHolder<MediaBase?>(binding.root) {

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br></br>
         *
         * @param model Is the model at the current adapter position
         */
        override fun onBindViewHolder(model: MediaBase?) {
            binding.model = model
            binding.seriesTitle.setTitle(model)
            binding.executePendingBindings()
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
         * <br></br>
         *
         * @see Glide
         */
        override fun onViewRecycled() {
            Glide.with(context).clear(binding.seriesImage)
            binding.unbind()
        }

        @OnClick(R.id.container)
        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        @OnLongClick(R.id.container)
        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }

    }

}