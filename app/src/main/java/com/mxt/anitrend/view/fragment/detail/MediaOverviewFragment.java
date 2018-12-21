package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.IntPair;
import com.google.android.youtube.player.YouTubeIntents;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.GenreAdapter;
import com.mxt.anitrend.adapter.recycler.detail.TagAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.FragmentSeriesOverviewBinding;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaBrowseUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity;
import com.mxt.anitrend.view.activity.detail.StudioActivity;
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment;
import com.mxt.anitrend.view.fragment.youtube.YoutubePlayerFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/12/31.
 */

public class MediaOverviewFragment extends FragmentBase<Media, MediaPresenter, Media> {

    private FragmentSeriesOverviewBinding binding;
    private YoutubePlayerFragment youtubePlayerFragment;
    private Media model;

    private GenreAdapter genreAdapter;
    private TagAdapter tagAdapter;

    private long mediaId;
    private @KeyUtil.MediaType String mediaType;

    public static MediaOverviewFragment newInstance(Bundle args) {
        MediaOverviewFragment fragment = new MediaOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mediaId = getArguments().getLong(KeyUtil.arg_id);
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
        }
        isMenuDisabled = true; mColumnSize = R.integer.grid_list_x2;
                setPresenter(new MediaPresenter(getContext()));
        setViewModel(true);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeriesOverviewBinding.inflate(inflater, container, false);
        unbinder = ButterKnife.bind(this, binding.getRoot());
        binding.stateLayout.showLoading();

        binding.genreRecycler.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL));
        binding.genreRecycler.setNestedScrollingEnabled(false);
        binding.genreRecycler.setHasFixedSize(true);

        binding.tagsRecycler.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL));
        binding.tagsRecycler.setNestedScrollingEnabled(false);
        binding.tagsRecycler.setHasFixedSize(true);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        makeRequest();
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(getActivity() != null && model.getTrailer() != null && CompatUtil.equals(model.getTrailer().getSite(), "youtube")) {
            if(YouTubeIntents.canResolvePlayVideoIntent(getActivity())) {
                if (youtubePlayerFragment == null)
                    youtubePlayerFragment = YoutubePlayerFragment.newInstance(model.getTrailer());
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.youtube_view, youtubePlayerFragment)
                        .commit();
            } else {
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.youtube_view, YouTubeEmbedFragment.newInstance(model.getTrailer()))
                        .commit();
            }
        } else
            binding.youtubeView.setVisibility(View.GONE);
        binding.setPresenter(getPresenter());
        binding.setModel(model);

        if(genreAdapter == null) {
            genreAdapter = new GenreAdapter(getContext());
            genreAdapter.onItemsInserted(getPresenter().buildGenres(model));
            genreAdapter.setClickListener(new ItemClickListener<Genre>() {
                @Override
                public void onItemClick(View target, IntPair<Genre> data) {
                    switch (target.getId()) {
                        case R.id.container:
                            Bundle args = new Bundle();
                            Intent intent = new Intent(getActivity(), MediaBrowseActivity.class);
                            args.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(true)
                                    .putVariable(KeyUtil.arg_type, mediaType)
                                    .putVariable(KeyUtil.arg_genres, data.getSecond().getGenre()));
                            args.putString(KeyUtil.arg_activity_tag, data.getSecond().getGenre());
                            args.putParcelable(KeyUtil.arg_media_util, new MediaBrowseUtil()
                                    .setCompactType(true)
                                    .setBasicFilter(true)
                                    .setFilterEnabled(true));
                            intent.putExtras(args);
                            startActivity(intent);
                            break;
                    }
                }

                @Override
                public void onItemLongClick(View target, IntPair<Genre> data) {

                }
            });
        }
        binding.genreRecycler.setAdapter(genreAdapter);

        if(tagAdapter == null) {
            tagAdapter = new TagAdapter(getContext());
            tagAdapter.onItemsInserted(model.getTags());
            tagAdapter.setClickListener(new ItemClickListener<MediaTag>() {
                @Override
                public void onItemClick(View target, IntPair<MediaTag> data) {
                    switch (target.getId()) {
                        case R.id.container:
                            DialogUtil.createMessage(getActivity(), data.getSecond().getName(), data.getSecond().getDescription(),
                                    R.string.More, R.string.Close, (dialog, which) -> {
                                        switch (which) {
                                            case POSITIVE:
                                                Bundle args = new Bundle();
                                                Intent intent = new Intent(getActivity(), MediaBrowseActivity.class);
                                                args.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(true)
                                                        .putVariable(KeyUtil.arg_type, mediaType)
                                                        .putVariable(KeyUtil.arg_tags, data.getSecond().getName()));
                                                args.putString(KeyUtil.arg_activity_tag, data.getSecond().getName());
                                                args.putParcelable(KeyUtil.arg_media_util, new MediaBrowseUtil()
                                                        .setCompactType(true)
                                                        .setBasicFilter(true)
                                                        .setFilterEnabled(true));
                                                intent.putExtras(args);
                                                startActivity(intent);
                                                break;
                                        }
                                    });
                            break;
                    }
                }

                @Override
                public void onItemLongClick(View target, IntPair<MediaTag> data) {

                }
            });
        }
        binding.tagsRecycler.setAdapter(tagAdapter);

        binding.stateLayout.showContent();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, mediaId)
                .putVariable(KeyUtil.arg_type, mediaType);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_OVERVIEW_REQ, getContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable Media model) {
        if(model != null) {
            this.model = model;
            updateUI();
        } else
            binding.stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_emoji_sweat),
                    getString(R.string.layout_empty_response), getString(R.string.try_again), view -> { binding.stateLayout.showLoading(); makeRequest(); });
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override @OnClick({R.id.series_image, R.id.anime_main_studio_container})
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.series_image:
                intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra(KeyUtil.arg_model, model.getCoverImage().getLarge());
                CompatUtil.startSharedImageTransition(getActivity(), v, intent, R.string.transition_image_preview);
            break;
            case R.id.anime_main_studio_container:
                StudioBase studioBase = getPresenter().getMainStudioObject(model);
                if(studioBase != null) {
                    intent = new Intent(getActivity(), StudioActivity.class);
                    intent.putExtra(KeyUtil.arg_id, studioBase.getId());
                    startActivity(intent);
                }
                break;
            default:
                super.onClick(v);
            break;
        }
    }
}
