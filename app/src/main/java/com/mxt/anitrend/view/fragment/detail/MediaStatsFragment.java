package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.LinkAdapter;
import com.mxt.anitrend.adapter.recycler.detail.RankAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.FragmentSeriesStatsBinding;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.MediaRank;
import com.mxt.anitrend.model.entity.anilist.meta.ScoreDistribution;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.ChartUtil;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/28.
 */

public class MediaStatsFragment extends FragmentBase<Media, MediaPresenter, Media> {

    private FragmentSeriesStatsBinding binding;
    private Media model;

    private RankAdapter rankAdapter;
    private LinkAdapter linkAdapter;

    private long mediaId;
    private @KeyUtil.MediaType String mediaType;

    public static MediaStatsFragment newInstance(Bundle args) {
        MediaStatsFragment fragment = new MediaStatsFragment();
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
        binding = FragmentSeriesStatsBinding.inflate(inflater, container, false);
        unbinder = ButterKnife.bind(this, binding.getRoot());
        binding.stateLayout.showLoading();
        binding.linksRecycler.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL));
        binding.linksRecycler.setHasFixedSize(true);
        binding.rankingRecycler.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL));
        binding.rankingRecycler.setHasFixedSize(true);
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
        binding.setModel(model);
        if(rankAdapter == null) {
            rankAdapter = new RankAdapter(model.getRankings(), getContext());
            rankAdapter.setClickListener(new ItemClickListener<MediaRank>() {
                @Override
                public void onItemClick(View target, MediaRank data) {
                    Intent intent = new Intent(getActivity(), MediaBrowseActivity.class);
                    Bundle args = new Bundle();
                    args.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(true)
                            .putVariable(KeyUtil.arg_type, mediaType)
                            .putVariable(KeyUtil.arg_season, data.getSeason())
                            .putVariable(KeyUtil.arg_seasonYear, data.getYear())
                            .putVariable(KeyUtil.arg_format, data.getFormat()));
                    args.putString(KeyUtil.arg_activity_tag, data.getTypeHtmlPlainTitle());
                    args.putBoolean(KeyUtil.arg_media_compact, true);
                    intent.putExtras(args);
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View target, MediaRank data) {

                }
            });
        }

        if(linkAdapter == null) {
            linkAdapter = new LinkAdapter(model.getExternalLinks(), getContext());
            linkAdapter.setClickListener(new ItemClickListener<ExternalLink>() {
                @Override
                public void onItemClick(View target, ExternalLink data) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(data.getUrl()));
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View target, ExternalLink data) {

                }
            });
        }

        binding.linksRecycler.setAdapter(linkAdapter);

        binding.rankingRecycler.setAdapter(rankAdapter);

        binding.stateLayout.showContent();
        showStatusDistribution(); showScoreDistribution();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, mediaId)
                .putVariable(KeyUtil.arg_type, mediaType);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_STATS_REQ, getContext());
    }

    private void showScoreDistribution() {
        if(model.getStats() != null && model.getStats().getScoreDistribution() != null) {

            configureScoreDistribution(model.getStats().getScoreDistribution());

            BarDataSet barDataSet = new BarDataSet(getPresenter().getMediaScoreDistribution(model.getStats().getScoreDistribution()), getString(R.string.title_score_distribution));
            if (getContext() != null)
                barDataSet.setColor(CompatUtil.getColorFromAttr(getContext(), R.attr.colorAccent), 253);
            barDataSet.setValueTextColor(CompatUtil.getColorFromAttr(getContext(), R.attr.titleColor));
            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.9f);

            binding.seriesScoreDist.setData(barData);
            binding.seriesScoreDist.setFitBars(true);
            binding.seriesScoreDist.invalidate();
        }
    }

    private void showStatusDistribution() {
        if(model.getStats() != null && model.getStats().getStatusDistribution() != null) {

            configureSeriesStats();

            List<PieEntry> pieEntries = getPresenter().getMediaStats(model.getStats().getStatusDistribution());
            PieDataSet pieDataSet = new PieDataSet(pieEntries, getString(R.string.title_series_stats));
            pieDataSet.setSliceSpace(3f);
            pieDataSet.setColors(Color.parseColor("#6fc1ea"), Color.parseColor("#48c76d"),
                    Color.parseColor("#f7464a"), Color.parseColor("#46bfbd"), Color.parseColor("#fba640"));

            PieData pieData = new PieData(pieDataSet);
            if (getContext() != null)
                pieData.setValueTextColor(CompatUtil.getColorFromAttr(getContext(), R.attr.titleColor));
            pieData.setValueTextSize(10f);
            pieData.setValueFormatter(new PercentFormatter());
            binding.seriesStats.setData(pieData);
            binding.seriesStats.highlightValues(null);
            binding.seriesStats.invalidate();
        }
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
            binding.stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_warning_white_18dp, R.color.colorStateOrange),
                    getString(R.string.layout_empty_response), getString(R.string.try_again), view -> { binding.stateLayout.showLoading(); makeRequest(); });
    }

    private void configureScoreDistribution(List<ScoreDistribution> scoreDistribution) {
        binding.seriesScoreDist.getDescription().setEnabled(false);
        binding.seriesScoreDist.setDrawGridBackground(false);
        binding.seriesScoreDist.setDrawBarShadow(false);

        List<Integer> mapKeys = Stream.of(scoreDistribution)
                .map(ScoreDistribution::getAmount)
                .toList();

        new ChartUtil.StepXAxisFormatter<Integer>()
                .setDataModel(mapKeys)
                .setChartBase(binding.seriesScoreDist)
                .build();
    }

    private void configureSeriesStats() {
        binding.seriesStats.setUsePercentValues(true);
        binding.seriesStats.getDescription().setEnabled(false);
        binding.seriesStats.setExtraOffsets(0, 0, 50, 0);
        binding.seriesStats.setDrawHoleEnabled(true);
        binding.seriesStats.setHoleRadius(58f);
        binding.seriesStats.setTransparentCircleRadius(61f);

        binding.seriesStats.setRotationAngle(0);
        binding.seriesStats.setRotationEnabled(false);
        binding.seriesStats.setHighlightPerTapEnabled(true);

        Legend l = binding.seriesStats.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        // binding.seriesStats.setEntryLabelColor(CompatUtil.getColorFromAttr(getContext(), R.attr.subtitleColor));
        binding.seriesStats.setDrawEntryLabels(false);
    }
}
