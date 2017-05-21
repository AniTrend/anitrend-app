package com.mxt.anitrend.view.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.StudioSmall;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.Tag;
import com.mxt.anitrend.presenter.detail.SeriesPresenter;
import com.mxt.anitrend.utils.DateTimeConverter;
import com.mxt.anitrend.utils.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.detail.activity.StudioActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AnimeOverviewFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.anime_details_image) ImageView mModelImage;
    @BindView(R.id.rating_item) RatingBar mRating;


    /*Header Items*/
    @BindView(R.id.anime_details_main_title) TextView mAnimeMianTitle;
    @BindView(R.id.anime_details_air_status) TextView mAnimeAiringStatus;

    /*Small Ani Details*/
    @BindView(R.id.anime_details_main_title_eng) TextView mAnimeEngTitle;
    @BindView(R.id.anime_details_show_type) TextView mAnimeType;
    /*Calendar Stuff*/
    @BindView(R.id.anime_title_start_date) TextView mAnimeTitleStart;
    @BindView(R.id.anime_title_end_date) TextView mAnimeTitleEnd;
    @BindView(R.id.anime_details_start_date) TextView mAnimeStart;
    @BindView(R.id.anime_details_end_date) TextView mAnimeEnd;

    /*Other Ani Ifor*/
    @BindView(R.id.anime_details_season) TextView mAnimeSeason;
    @BindView(R.id.anime_details_genres) TextView mAnimeGenres;
    @BindView(R.id.anime_details_episode_count) TextView mAnimeTotalEps;
    @BindView(R.id.anime_details_episode_duration) TextView mAnimeEpDuration;
    @BindView(R.id.anime_details_hashtag) TextView mAnimeHashtag;
    @BindView(R.id.anime_details_origin) TextView mAnimeOrigin;
    @BindView(R.id.anime_details_main_studio) TextView mAnimeStudio;
    @BindView(R.id.anime_details_avg_score) TextView mAnimeAvgScore;
    @BindView(R.id.anime_main_studio_container) View mStudioContainer;

    /*Description Controls*/
    @BindView(R.id.anime_details_main_title_jap) TextView mAnimeJapTitle;
    @BindView(R.id.anime_details_decription) TextView mAnimeDesription;

    /*Tag & Next Airing Data*/
    @BindView(R.id.anime_details_next_ep) TextView mAnimeNextEp;
    @BindView(R.id.anime_details_tags) TextView mAnimeTags;

    private final static String ARG_KEY = "arg_data";
    private Series model;
    private SeriesPresenter mPresenter;
    private Unbinder unbinder;
    private StudioSmall studio;

    public AnimeOverviewFragment() {
        // Required empty public constructor
    }

    public static AnimeOverviewFragment newInstance(Series result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        AnimeOverviewFragment fragment = new AnimeOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            model = getArguments().getParcelable(ARG_KEY);
        }
        mPresenter = new SeriesPresenter(FilterTypes.SeriesTypes[FilterTypes.SeriesType.ANIME.ordinal()], getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_anime_overview, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        UpdateUI();
    }

    public void UpdateUI(){
        /*Populate all the views*/

        Glide.with(this)
             .load(model.getImage_url_lge())
             .diskCacheStrategy(DiskCacheStrategy.ALL)
             .crossFade()
             .centerCrop()
             .into(mModelImage);

        mRating.setRating((float) ((model.getAverage_score()*5)/100));

        if(model.getTags() != null && model.getTags().size() > 1) {
            StringBuilder named_tags = new StringBuilder();
            List<Tag> tags = model.getTags();
            for (int i = 0; i < tags.size(); i++) {
                if(i == 0)
                    named_tags.append(tags.get(i).getName());
                else {
                    named_tags.append(", ");
                    named_tags.append(tags.get(i).getName());
                }
            }
            mAnimeTags.setText(named_tags);
        }
        else
            mAnimeTags.setText("N/A");

        mAnimeNextEp.setText(DateTimeConverter.getNextEpDate(model.getNextAiring()));

        mAnimeMianTitle.setText(model.getTitle_romaji());
        mAnimeAiringStatus.setText(model.getAiring_status());

        mAnimeEngTitle.setText(model.getTitle_english());
        mAnimeType.setText(model.getType());

        mAnimeTitleStart.setText(DateTimeConverter.getStartTitle(model.getStart_date_fuzzy()));
        mAnimeTitleEnd.setText(DateTimeConverter.getEndTitle(model.getEnd_date_fuzzy()));
        mAnimeStart.setText(DateTimeConverter.convertDate(model.getStart_date_fuzzy()));
        mAnimeEnd.setText(DateTimeConverter.convertDate(model.getEnd_date_fuzzy()));

        mAnimeSeason.setText(mPresenter.getSeason(model.getSeason()));
        mAnimeGenres.setText(mPresenter.getGenres(model.getGenres()));
        if(model.getTotal_episodes() < 1)
            mAnimeTotalEps.setText(R.string.tba_placeholder);
        else
            mAnimeTotalEps.setText(getString(R.string.text_anime_episodes, model.getTotal_episodes()));

        if(model.getDuration() < 1)
            mAnimeEpDuration.setText(R.string.tba_placeholder);
        else
            mAnimeEpDuration.setText(getString(R.string.text_anime_length, model.getDuration()));

        if(model.getHashtag() != null) {
            mAnimeHashtag.setText(twittery(model.getHashtag()));
            mAnimeHashtag.setMovementMethod(LinkMovementMethod.getInstance());
        }
        else
            mAnimeHashtag.setText("N/A");

        mAnimeOrigin.setText(model.getSource());

        if(model.getStudio() != null) {
            Optional<StudioSmall> result = Stream.of(model.getStudio())
                    .filter(new Predicate<StudioSmall>() {
                        @Override
                        public boolean test(StudioSmall value) {
                            return value.getMain_studio() == 1;
                        }
                    }).findSingle();
            if(result.isPresent())
                studio = result.get();
            if(studio != null) {
                mAnimeStudio.setText(studio.getStudio_name());
                mAnimeStudio.setTextColor(ContextCompat.getColor(getContext(),R.color.colorStateBlue));
                mAnimeStudio.setOnClickListener(this);
                mStudioContainer.setOnClickListener(this);
            } else
                mAnimeStudio.setText("N/A");
        }
        else
            mAnimeStudio.setText("N/A");

        mAnimeAvgScore.setText(getString(R.string.text_anime_score, model.getAverage_score()));

        mAnimeJapTitle.setText(model.getTitle_japanese());
        mAnimeDesription.setText(model.getDescription());

        mModelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, model.getImage_url_lge());
                TransitionHelper.startSharedImageTransition(getActivity(), mModelImage, getString(R.string.transition_image_preview), intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.anime_main_studio_container:
                navigateToStudio();
                break;
            case R.id.anime_details_main_studio:
                navigateToStudio();
                break;
        }
    }

    private void navigateToStudio() {
        Intent starter = new Intent(getActivity(), StudioActivity.class);
        starter.putExtra(StudioActivity.STUDIO_PARAM, studio);
        startActivity(starter);
    }

    private Spanned twittery(String text) {
        return Html.fromHtml(String.format("<a href=\"https://twitter.com/search?q=%%23%s&src=typd\">%s</a>",text.replace("#",""), text));
    }
}
