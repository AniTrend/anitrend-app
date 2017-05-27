package com.mxt.anitrend.view.detail.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.Tag;
import com.mxt.anitrend.presenter.detail.SeriesPresenter;
import com.mxt.anitrend.util.DateTimeConverter;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MangaOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MangaOverviewFragment extends Fragment {


    @BindView(R.id.manga_details_image) ImageView mModelImage;
    @BindView(R.id.rating_item) RatingBar mRating;

    /*Header Items*/
    @BindView(R.id.manga_details_main_title) TextView mMangaMianTitle;
    @BindView(R.id.manga_details_publish_status) TextView mMangaPublishingStatus;

    /*Small Ani Details*/
    @BindView(R.id.manga_details_main_title_eng) TextView mMangaEngTitle;
    @BindView(R.id.manga_details_show_type) TextView mMangaType;
    /*Calendar Stuff*/
    @BindView(R.id.manga_title_start_date) TextView mMangaTitleStart;
    @BindView(R.id.manga_title_end_date) TextView mMangaTitleEnd;
    @BindView(R.id.manga_details_start_date) TextView mMangaStart;
    @BindView(R.id.manga_details_end_date) TextView mMangaEnd;

    /*Other Ani Ifor*/
    @BindView(R.id.manga_details_genres) TextView mMangaGenres;
    @BindView(R.id.manga_details_chapters_count) TextView mMangaTotalChap;
    @BindView(R.id.manga_details_volume_count) TextView mMangaTotalVol;
    @BindView(R.id.manga_details_hashtag) TextView mMangaHashtag;
    @BindView(R.id.manga_details_avg_score) TextView mMangaScore;

    /*Description Controls*/
    @BindView(R.id.manga_details_main_title_jap) TextView mMangaJapTitle;
    @BindView(R.id.manga_details_decription) TextView mMangaDesription;

    /*Tag & Next Airing Data*/
    @BindView(R.id.manga_details_tags) TextView mMangaTags;

    private Series model;
    private SeriesPresenter mPresenter;
    private Unbinder unbinder;
    private final static String ARG_KEY = "arg_data";

    public static MangaOverviewFragment newInstance(Series result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        MangaOverviewFragment fragment = new MangaOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            model = getArguments().getParcelable(ARG_KEY);
        }
        mPresenter = new SeriesPresenter(FilterTypes.SeriesTypes[FilterTypes.SeriesType.MANGA.ordinal()], getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_manga_overview, container, false);
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

        if(model.getTags().size() > 1) {
            String named_tags = "";
            List<Tag> tags = model.getTags();
            for (int i = 0; i < tags.size(); i++) {
                if(i == 0)
                    named_tags += tags.get(i).getName();
                else
                    named_tags += ", "+tags.get(i).getName();
            }
            mMangaTags.setText(named_tags);
        }
        else
            mMangaTags.setText("N/A");

        mMangaMianTitle.setText(model.getTitle_romaji());
        mMangaPublishingStatus.setText(model.getPublishing_status());

        mMangaEngTitle.setText(model.getTitle_english());
        mMangaType.setText(model.getType());

        mMangaTitleStart.setText(DateTimeConverter.getStartTitle(model.getStart_date_fuzzy()));
        mMangaTitleEnd.setText(DateTimeConverter.getEndTitle(model.getEnd_date_fuzzy()));
        mMangaStart.setText(DateTimeConverter.convertDate(model.getStart_date_fuzzy()));
        mMangaEnd.setText(DateTimeConverter.convertDate(model.getEnd_date_fuzzy()));

        mMangaGenres.setText(mPresenter.getGenres(model.getGenres()));
        if(model.getTotal_chapters() < 1)
            mMangaTotalChap.setText(R.string.tba_placeholder);
        else
            mMangaTotalChap.setText(String.format(Locale.getDefault(), "%d Chapters", model.getTotal_chapters()));

        if(model.getTotal_volumes() < 1)
            mMangaTotalVol.setText(R.string.tba_placeholder);
        else
            mMangaTotalVol.setText(String.format(Locale.getDefault(), "%d Volumes", model.getTotal_volumes()));

        mMangaHashtag.setText(model.getHashtag());
        mMangaScore.setText(String.format(Locale.getDefault(), "%.1f %% Avg", model.getAverage_score()));

        mMangaJapTitle.setText(model.getTitle_japanese());
        mMangaDesription.setText(model.getDescription());

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
}
