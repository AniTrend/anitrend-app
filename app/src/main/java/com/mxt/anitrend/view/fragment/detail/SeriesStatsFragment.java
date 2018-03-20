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
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.LinkAdapter;
import com.mxt.anitrend.adapter.recycler.detail.RankAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.databinding.FragmentSeriesStatsBinding;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.MediaRank;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.ChartUtil;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.ParamBuilderUtil;
import com.mxt.anitrend.view.activity.detail.BrowseActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/28.
 */

public class SeriesStatsFragment extends FragmentBase<Media, SeriesPresenter, Media> implements PublisherListener<Media> {

    private FragmentSeriesStatsBinding binding;
    private Media model;

    private RankAdapter rankAdapter;
    private LinkAdapter linkAdapter;

    public static SeriesStatsFragment newInstance(Bundle args) {
        SeriesStatsFragment fragment = new SeriesStatsFragment();
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
                    Intent intent = new Intent(getActivity(), BrowseActivity.class);
                    Bundle args = ParamBuilderUtil.Builder()
                            .setSeries_type(model.getSeries_type())
                            .setSeason(data.getSeason())
                            .setYear(data.getYear())
                            .setSeries_show_type(data.getFormat())
                            .build();
                    args.putString(KeyUtils.arg_activity_tag, data.getTypeHtmlPlainTitle());
                    intent.putExtras(args);
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View target, MediaRank data) {

                }
            });
        }
        binding.rankingRecycler.setAdapter(rankAdapter);

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
        binding.linksRecycler.setAdapter(linkAdapter);  binding.stateLayout.showContent();
        showRingStats(); showScoreDistribution(); showAiringScoreCorrelation();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        updateUI();
    }

    private void showScoreDistribution() {
        if(model.getScore_distribution() != null && !model.getScore_distribution().isEmpty()) {
            List<Map.Entry<Integer, Float>> scoreDist = Stream.of(model.getScore_distribution().entrySet())
                    .sorted((o1, o2) -> o1.getKey() > o2.getKey() ? 1 : -1).toList();

            configureScoreDistribution(scoreDist);

            BarDataSet barDataSet = new BarDataSet(getPresenter().getSeriesScoreDistribution(scoreDist), getString(R.string.title_score_distribution));
            barDataSet.setColor(CompatUtil.getColorFromAttr(getContext(), R.attr.colorAccent), 253);
            barDataSet.setValueTextColor(CompatUtil.getColorFromAttr(getContext(), R.attr.titleColor));
            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.9f);

            binding.seriesScoreDist.setData(barData);
            binding.seriesScoreDist.setFitBars(true);
            binding.seriesScoreDist.invalidate();
        }
    }

    private void showAiringScoreCorrelation() {
        if(model.getAiring_stats() != null && !model.getAiring_stats().isEmpty()) {
            List<Map.Entry<String, Map<String, Float>>> scoreCorrelation = Stream.of(model.getAiring_stats().entrySet()).toList();

            configureAiringScoreCorrelation(model.getAiring_stats());

            List<List<Entry>> correlation = getPresenter().getSeriesAiringCorrelation(scoreCorrelation);

            List<ILineDataSet> dataSets = new ArrayList<>();

            LineDataSet scoreDataSet = new LineDataSet(correlation.get(0), getString(R.string.title_score));
            scoreDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            scoreDataSet.setHighLightColor(CompatUtil.getColor(getContext(), R.color.colorStateGreen));
            scoreDataSet.setCircleColor(CompatUtil.getColor(getContext(), R.color.colorStateGreen));
            scoreDataSet.setValueTextColor(CompatUtil.getColorFromAttr(getContext(), R.attr.titleColor));
            dataSets.add(scoreDataSet);

            LineDataSet watchingDataSet = new LineDataSet(correlation.get(1), getString(R.string.title_watching));
            watchingDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            watchingDataSet.setHighLightColor(CompatUtil.getColor(getContext(), R.color.colorStateBlue));
            watchingDataSet.setCircleColor(CompatUtil.getColor(getContext(), R.color.colorStateBlue));
            watchingDataSet.setValueTextColor(CompatUtil.getColorFromAttr(getContext(), R.attr.titleColor));
            dataSets.add(watchingDataSet);

            LineData data = new LineData(dataSets);
            binding.seriesAiringCorrelation.setData(data);
            binding.seriesAiringCorrelation.invalidate();
        }
    }

    private void showRingStats() {
        if(model.getList_stats() != null && !model.getList_stats().isEmpty()) {
            configureSeriesStats();

            List<Map.Entry<String, Float>> seriesStats = Stream.of(model.getList_stats())
                    .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                    .toList();

            List<PieEntry> pieEntries = getPresenter().getSeriesStats(seriesStats);
            PieDataSet pieDataSet = new PieDataSet(pieEntries, getString(R.string.title_series_stats));
            pieDataSet.setSliceSpace(3f);
            pieDataSet.setColors(Color.parseColor("#6fc1ea"), Color.parseColor("#48c76d"),
                    Color.parseColor("#f7464a"), Color.parseColor("#46bfbd"), Color.parseColor("#fba640"));

            PieData pieData = new PieData(pieDataSet);
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
            makeRequest();
        }
    }

    private void configureScoreDistribution(List<Map.Entry<Integer, Float>> scoreDistribution) {
        binding.seriesScoreDist.getDescription().setEnabled(false);
        binding.seriesScoreDist.setDrawGridBackground(false);
        binding.seriesScoreDist.setDrawBarShadow(false);

        List<Integer> mapKeys = Stream.of(scoreDistribution)
                .map(Map.Entry::getKey)
                .toList();

        new ChartUtil.StepXAxisFormatter<Integer>()
                .setDataModel(mapKeys)
                .setChartBase(binding.seriesScoreDist)
                .build();

        new ChartUtil.StepYAxisFormatter()
                .setChartBase(binding.seriesScoreDist)
                .build();
    }

    private void configureAiringScoreCorrelation(Map<String, Map<String, Float>> scoreCorrelation) {
        binding.seriesAiringCorrelation.getDescription().setEnabled(false);
        binding.seriesAiringCorrelation.setDrawGridBackground(false);
        List<String> mapKeys = Stream.of(scoreCorrelation)
                .map(Map.Entry::getKey)
                .toList();

        new ChartUtil.StepXAxisFormatter<String>()
                .setDataModel(mapKeys)
                .setChartBase(binding.seriesAiringCorrelation)
                .build();

        /*new ChartUtil.StepYAxisFormatter()
                .setChartBase(binding.seriesAiringCorrelation)
                .build();*/
    }

    private void configureSeriesStats() {
        binding.seriesStats.setUsePercentValues(true);
        binding.seriesStats.getDescription().setEnabled(false);
        binding.seriesStats.setExtraOffsets(0, 0, 50, 0);
        binding.seriesStats.setDrawHoleEnabled(true);
        binding.seriesStats.setHoleRadius(58f);
        binding.seriesStats.setTransparentCircleRadius(61f);

        binding.seriesStats.setRotationAngle(0);
        // enable rotation of the chart by touch
        binding.seriesStats.setRotationEnabled(true);
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
