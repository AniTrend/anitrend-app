package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.GenreAdapter;
import com.mxt.anitrend.adapter.recycler.detail.TagAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.databinding.FragmentSeriesOverviewBinding;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.Tag;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.ParamBuilderUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;
import com.mxt.anitrend.view.activity.detail.BrowseActivity;
import com.mxt.anitrend.view.activity.detail.StudioActivity;
import com.mxt.anitrend.view.fragment.youtube.YoutubePlayerFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/12/31.
 */

public class SeriesOverviewFragment extends FragmentBase<Media, SeriesPresenter, Media> implements PublisherListener<Media> {

    private FragmentSeriesOverviewBinding binding;
    private YoutubePlayerFragment youtubePlayerFragment;
    private Media model;

    private GenreAdapter genreAdapter;
    private TagAdapter tagAdapter;

    public static SeriesOverviewFragment newInstance(Bundle args) {
        SeriesOverviewFragment fragment = new SeriesOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMenuDisabled = true; mColumnSize = R.integer.grid_list_x2;
        setPresenter(new SeriesPresenter(getContext()));
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

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(!TextUtils.isEmpty(model.getYoutube_id())) {
            if (youtubePlayerFragment == null) {
                youtubePlayerFragment = YoutubePlayerFragment.newInstance(getViewModel().getParams());
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.youtube_view, youtubePlayerFragment)
                        .commit();
            }
        } else
            binding.youtubeView.setVisibility(View.GONE);
        binding.setPresenter(getPresenter());
        binding.setModel(model);

        if(genreAdapter == null) {
            genreAdapter = new GenreAdapter(getPresenter().buildGenres(model), getContext());
            genreAdapter.setClickListener(new ItemClickListener<Genre>() {
                @Override
                public void onItemClick(View target, Genre data) {
                    switch (target.getId()) {
                        case R.id.container:
                            Intent intent = new Intent(getActivity(), BrowseActivity.class);
                            Bundle args = ParamBuilderUtil.Builder()
                                    .setSeries_type(model.getSeries_type())
                                    .addGenre(data.getGenre())
                                    .build();
                            args.putString(KeyUtils.arg_activity_tag, data.getGenre());
                            intent.putExtras(args);
                            startActivity(intent);
                            break;
                    }
                }

                @Override
                public void onItemLongClick(View target, Genre data) {

                }
            });
        }
        binding.genreRecycler.setAdapter(genreAdapter);

        if(tagAdapter == null) {
            tagAdapter = new TagAdapter(model.getTags(), getContext());
            tagAdapter.setClickListener(new ItemClickListener<Tag>() {
                @Override
                public void onItemClick(View target, Tag data) {
                    switch (target.getId()) {
                        case R.id.container:
                            DialogUtil.createMessage(getActivity(), data.getName(), data.getDescription(),
                                    R.string.More, R.string.Close, (dialog, which) -> {
                                        switch (which) {
                                            case POSITIVE:
                                                Intent intent = new Intent(getActivity(), BrowseActivity.class);
                                                Bundle args = ParamBuilderUtil.Builder()
                                                        .setSeries_type(KeyUtils.SeriesTypes[KeyUtils.ANIME])
                                                        .addTag(data.getName())
                                                        .build();
                                                args.putString(KeyUtils.arg_activity_tag, data.getName());
                                                intent.putExtras(args);
                                                startActivity(intent);
                                                break;
                                        }
                                    });
                            break;
                    }
                }

                @Override
                public void onItemLongClick(View target, Tag data) {

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

    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable Media model) {

    }

    /**
     * Responds to published events, be sure to add subscribe annotation
     *
     * @param param passed event
     * @see Subscribe
     */
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Media param) {
        if(model == null) {
            model = param;
            getViewModel().getParams().putString(KeyUtils.arg_model, model.getYoutube_id());
            updateUI();
        }
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
                intent.putExtra(KeyUtils.arg_model, model.getImage_url_lge());
                CompatUtil.startSharedImageTransition(getActivity(), v, intent, R.string.transition_image_preview);
            break;
            case R.id.anime_main_studio_container:
                Optional<StudioBase> result = Stream.of(model.getStudio())
                        .filter(studio -> studio.getMain_studio() == 1)
                        .findFirst();
                if(result.isPresent()) {
                    intent = new Intent(getActivity(), StudioActivity.class);
                    intent.putExtra(KeyUtils.arg_id, result.get().getId());
                    startActivity(intent);
                }
                break;
            default:
                super.onClick(v);
            break;
        }
    }
}
