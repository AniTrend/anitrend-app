package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import butterknife.ButterKnife
import com.afollestad.materialdialogs.DialogAction
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.GenreAdapter
import com.mxt.anitrend.adapter.recycler.detail.TagAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.FragmentSeriesOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity
import com.mxt.anitrend.view.activity.detail.StudioActivity
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment

/**
 * Created by max on 2017/12/31.
 */

class MediaOverviewFragment : FragmentBase<Media, MediaPresenter, Media>() {

    private var binding: FragmentSeriesOverviewBinding? = null
    private var model: Media? = null

    private var genreAdapter: GenreAdapter? = null
    private var tagAdapter: TagAdapter? = null

    private var mediaId: Long = 0
    @KeyUtil.MediaType
    private var mediaType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mediaId = arguments?.getLong(KeyUtil.arg_id) ?: 0
            mediaType = arguments?.getString(KeyUtil.arg_mediaType)
        }
        isMenuDisabled = true
        mColumnSize = R.integer.grid_list_x2
        setPresenter(MediaPresenter(context))
        setViewModel(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSeriesOverviewBinding.inflate(inflater, container, false).apply {
            unbinder = ButterKnife.bind(this, root)
            stateLayout.showLoading()

            genreRecycler.layoutManager = StaggeredGridLayoutManager(resources.getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL)
            genreRecycler.isNestedScrollingEnabled = false
            genreRecycler.setHasFixedSize(true)

            tagsRecycler.layoutManager = StaggeredGridLayoutManager(resources.getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL)
            tagsRecycler.isNestedScrollingEnabled = false
            tagsRecycler.setHasFixedSize(true)

            listOf(
                    R.id.series_image,
                    R.id.anime_main_studio_container,
                    R.id.show_spoiler_tags
            ).map {
                root.findViewById<View>(it)
            }.forEach { it?.setOnClickListener(this@MediaOverviewFragment) }
        }

        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        makeRequest()
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    override fun updateUI() {
        if (activity != null && model?.trailer != null && CompatUtil.equals(model?.trailer?.site, "youtube")) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.youtube_view, YouTubeEmbedFragment.newInstance(model?.trailer))
                    .commit()
        } else
            binding?.youtubeView?.visibility = View.GONE
        binding?.presenter = presenter
        binding?.model = model


        if (model?.tags != null && model?.tagsNoSpoilers != null) {
            if (model?.tagsNoSpoilers?.size == model?.tags?.size)
                binding?.showSpoilerTags?.visibility = View.GONE
            else
                binding?.showSpoilerTags?.visibility = View.VISIBLE
        }

        if (genreAdapter == null) {
            genreAdapter = GenreAdapter(context)
            genreAdapter?.onItemsInserted(presenter.buildGenres(model))
            genreAdapter?.setClickListener(object : ItemClickListener<Genre> {
                override fun onItemClick(target: View, data: IntPair<Genre>) {
                    when (target.id) {
                        R.id.container -> {
                            val args = Bundle()
                            val intent = Intent(activity, MediaBrowseActivity::class.java)
                            args.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(true)
                                    .putVariable(KeyUtil.arg_type, mediaType)
                                    .putVariable(KeyUtil.arg_genres, data.second.genre))
                            args.putString(KeyUtil.arg_activity_tag, data.second.genre)
                            args.putParcelable(KeyUtil.arg_media_util, MediaBrowseUtil()
                                    .setCompactType(true)
                                    .setBasicFilter(true)
                                    .setFilterEnabled(true))
                            intent.putExtras(args)
                            startActivity(intent)
                        }
                    }
                }

                override fun onItemLongClick(target: View, data: IntPair<Genre>) {

                }
            })
        }
        binding?.genreRecycler?.adapter = genreAdapter
        model?.tagsNoSpoilers?.also {
            if (tagAdapter == null) {
                tagAdapter = TagAdapter(context)
                tagAdapter?.onItemsInserted(it)
                tagAdapter?.setClickListener(object : ItemClickListener<MediaTag> {
                    override fun onItemClick(target: View, data: IntPair<MediaTag>) {
                        when (target.id) {
                            R.id.container -> DialogUtil.createTagMessage(activity, data.second.name, data.second.description, data.second.isMediaSpoiler,
                                    R.string.More, R.string.Close) { _, which ->
                                if (which == DialogAction.POSITIVE) {
                                    val args = Bundle()
                                    val intent = Intent(activity, MediaBrowseActivity::class.java)
                                    args.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(true)
                                            .putVariable(KeyUtil.arg_type, mediaType)
                                            .putVariable(KeyUtil.arg_tags, data.second.name))
                                    args.putString(KeyUtil.arg_activity_tag, data.second.name)
                                    args.putParcelable(KeyUtil.arg_media_util, MediaBrowseUtil()
                                            .setCompactType(true)
                                            .setBasicFilter(true)
                                            .setFilterEnabled(true))
                                    intent.putExtras(args)
                                    startActivity(intent)
                                }
                            }
                        }
                    }

                    override fun onItemLongClick(target: View, data: IntPair<MediaTag>) {

                    }
                })
            }
            binding?.tagsRecycler?.adapter = tagAdapter
        }

        binding?.stateLayout?.showContent()
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    override fun makeRequest() {
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, mediaId)
                .putVariable(KeyUtil.arg_type, mediaType)
        getViewModel().params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        getViewModel().requestData(KeyUtil.MEDIA_OVERVIEW_REQ, context!!)
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    override fun onChanged(model: Media?) {
        if (model != null) {
            this.model = model
            updateUI()
        } else
            binding?.stateLayout?.showError(context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
                    getString(R.string.layout_empty_response), getString(R.string.try_again)) { _ ->
                binding?.stateLayout?.showLoading()
                makeRequest()
            }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        val intent: Intent
        when (v.id) {
            R.id.series_image -> CompatUtil.imagePreview(v, model?.coverImage?.extraLarge, R.string.image_preview_error_series_cover)
            R.id.anime_main_studio_container -> {
                val studioBase = presenter.getMainStudioObject(model)
                if (studioBase != null) {
                    intent = Intent(activity, StudioActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, studioBase.id)
                    startActivity(intent)
                }
            }
            R.id.show_spoiler_tags -> {
                model?.tags?.also {
                    tagAdapter?.onItemRangeChanged(it)
                    tagAdapter?.notifyDataSetChanged()
                    v.visibility = View.GONE
                }
            }
            else -> super.onClick(v)
        }
    }

    companion object {

        fun newInstance(args: Bundle): MediaOverviewFragment {
            val fragment = MediaOverviewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
