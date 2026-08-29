package com.mxt.anitrend.view.fragment.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.LinkAdapter
import com.mxt.anitrend.adapter.recycler.detail.RankAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.FragmentSeriesStatsBinding
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.model.entity.anilist.meta.ScoreDistribution
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution
import com.mxt.anitrend.util.ChartUtil
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.media.MediaUtil
import com.mxt.anitrend.viewmodel.MediaStatsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * View-only media statistics section used by the media destination.
 *
 * The callback-heavy constructor and grouped chart helpers intentionally keep
 * the destination's existing statistics actions in one section controller.
 */
@Suppress("LongParameterList", "TooManyFunctions")
class MediaStatsSection(
    private val mediaStatsViewModel: MediaStatsViewModel,
    private val mediaId: Long,
    @KeyUtil.MediaType private val mediaType: String?,
    private val isAdultContent: Boolean,
    private val onOpenRank: (MediaRank, Media) -> Unit,
    private val onOpenExternalLink: (String) -> Unit,
    private val onCopyExternalLink: (String) -> Unit,
) {
    private var _binding: FragmentSeriesStatsBinding? = null
    private val binding: FragmentSeriesStatsBinding
        get() = checkNotNull(_binding)

    private var model: Media? = null
    private var clipboardManager: ClipboardManager? = null

    private var rankAdapter: RankAdapter? = null
    private var linkAdapter: LinkAdapter? = null
    private var selected = false

    /** Inflates and initializes the media statistics view. */
    fun inflate(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): View {
        _binding = FragmentSeriesStatsBinding.inflate(inflater, container, false)
        clipboardManager = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        binding.linksRecycler.apply {
            layoutManager =
                StaggeredGridLayoutManager(
                    resources.getInteger(R.integer.grid_list_x2),
                    StaggeredGridLayoutManager.VERTICAL,
                )
            setHasFixedSize(true)
        }
        binding.rankingRecycler.apply {
            layoutManager =
                StaggeredGridLayoutManager(
                    resources.getInteger(R.integer.grid_list_x2),
                    StaggeredGridLayoutManager.VERTICAL,
                )
            setHasFixedSize(true)
        }

        binding.stateLayout.showLoading()
        return binding.root
    }

    /** Starts collecting statistics state for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaStatsViewModel.state.collect { state ->
                    when (state) {
                        is MediaStatsViewModel.UiState.Loading -> {
                            binding.stateLayout.showLoading()
                        }
                        is MediaStatsViewModel.UiState.Success -> {
                            model = state.media
                            updateUI()
                        }
                        is MediaStatsViewModel.UiState.Error -> {
                            binding.stateLayout.showError(
                                binding.root.context.getCompatDrawable(R.drawable.ic_emoji_sweat),
                                state.message,
                                binding.root.context.getString(R.string.try_again),
                            ) { loadStats() }
                        }
                    }
                }
            }
        }
    }

    /** Loads the statistics the first time this section is selected. */
    fun select() {
        if (selected) return
        selected = true
        loadStats()
    }

    private fun loadStats() {
        binding.stateLayout.showLoading()
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        mediaStatsViewModel.load(mediaId = mediaId, type = type, isAdult = if (isAdultContent) null else false)
    }

    /** Releases the statistics binding and cached rendering state. */
    fun clear() {
        _binding = null
        model = null
        clipboardManager = null
        rankAdapter = null
        linkAdapter = null
        selected = false
    }

    private fun updateUI() {
        val ctx = binding.root.context
        val model = model ?: return
        binding.linksRecycler.visibility =
            if (!CompatUtil.isEmpty(model.externalLinks)) View.VISIBLE else View.GONE
        binding.rankingRecycler.visibility =
            if (!CompatUtil.isEmpty(model.rankings)) View.VISIBLE else View.GONE
        binding.scoreDistributionCard.visibility =
            if (!CompatUtil.isEmpty(model.stats?.scoreDistribution)) View.VISIBLE else View.GONE
        binding.statusDistributionCard.visibility =
            if (!CompatUtil.isEmpty(model.stats?.statusDistribution)) View.VISIBLE else View.GONE

        if (rankAdapter == null) {
            rankAdapter =
                RankAdapter(ctx).apply {
                    onItemsInserted(model.rankings ?: emptyList())
                    setClickListener(
                        object : ItemClickListener<MediaRank> {
                            override fun onItemClick(
                                target: View,
                                data: IndexedValue<MediaRank>,
                            ) {
                                onOpenRank(data.value, model)
                            }

                            override fun onItemLongClick(
                                target: View,
                                data: IndexedValue<MediaRank>,
                            ) = Unit
                        },
                    )
                }
        }

        if (linkAdapter == null) {
            linkAdapter =
                LinkAdapter(ctx).apply {
                    onItemsInserted(model.externalLinks ?: emptyList())
                    setClickListener(
                        object : ItemClickListener<ExternalLink> {
                            override fun onItemClick(
                                target: View,
                                data: IndexedValue<ExternalLink>,
                            ) {
                                data.value.url?.let(onOpenExternalLink)
                            }

                            override fun onItemLongClick(
                                target: View,
                                data: IndexedValue<ExternalLink>,
                            ) {
                                val url = data.value.url ?: return
                                clipboardManager?.setPrimaryClip(
                                    ClipData.newPlainText("", url),
                                )
                                onCopyExternalLink(url)
                            }
                        },
                    )
                }
        }

        binding.stateLayout.showContent()
        binding.linksRecycler.adapter = linkAdapter
        binding.rankingRecycler.adapter = rankAdapter
        showStatusDistribution()
        showScoreDistribution()
    }

    private fun showScoreDistribution() {
        val ctx = binding.root.context
        val scoreDistribution = model?.stats?.scoreDistribution ?: return

        val barEntries = getMediaScoreDistribution(scoreDistribution)
        val barDataSet =
            BarDataSet(barEntries, binding.root.context.getString(R.string.title_score_distribution)).apply {
                valueTextColor = ctx.getCompatColorAttr(R.attr.titleColor)
                setColors(
                    Color.parseColor("#c26fc1ea"),
                    Color.parseColor("#c248c76d"),
                    Color.parseColor("#c2f7464a"),
                    Color.parseColor("#c29256f3"),
                    Color.parseColor("#c2fba640"),
                    Color.parseColor("#c26fc1ea"),
                    Color.parseColor("#c248c76d"),
                    Color.parseColor("#c2f7464a"),
                    Color.parseColor("#c29256f3"),
                    Color.parseColor("#c2fba640"),
                )
            }

        configureScoreDistribution(scoreDistribution)

        val barData =
            BarData(barDataSet).apply {
                barWidth = 0.6f
            }

        binding.seriesScoreDist.setData(barData)
        binding.seriesScoreDist.disableScroll()
        binding.seriesScoreDist.setFitBars(true)
        binding.seriesScoreDist.setPinchZoom(false)
        binding.seriesScoreDist.setDoubleTapToZoomEnabled(false)
        binding.seriesScoreDist.invalidate()
    }

    private fun showStatusDistribution() {
        val ctx = binding.root.context
        val statusDistribution = model?.stats?.statusDistribution ?: return

        configureSeriesStats()

        val pieEntries: List<PieEntry> = getMediaStats(statusDistribution)
        val pieDataSet =
            PieDataSet(pieEntries, binding.root.context.getString(R.string.title_series_stats)).apply {
                sliceSpace = 3f
                setColors(
                    Color.parseColor("#c26fc1ea"),
                    Color.parseColor("#c248c76d"),
                    Color.parseColor("#c2f7464a"),
                    Color.parseColor("#c29256f3"),
                    Color.parseColor("#c2fba640"),
                )
            }

        val pieData =
            PieData(pieDataSet).apply {
                setValueTextColor(ctx.getCompatColorAttr(R.attr.titleColor))
                setValueTextSize(9f)
                setValueFormatter(PercentFormatter())
            }

        binding.seriesStats.legend.textColor = ctx.getCompatColorAttr(R.attr.titleColor)
        binding.seriesStats.setHoleColor(ctx.getCompatColorAttr(R.attr.color))
        binding.seriesStats.setData(pieData)
        binding.seriesStats.invalidate()
    }

    private fun configureScoreDistribution(scoreDistributions: List<ScoreDistribution>) {
        val ctx = binding.root.context
        binding.seriesScoreDist.description.isEnabled = false
        binding.seriesScoreDist.setDrawGridBackground(false)
        binding.seriesScoreDist.setDrawBarShadow(false)
        binding.seriesScoreDist.setHighlightFullBarEnabled(true)

        ChartUtil
            .StepXAxisFormatter()
            .setDataModel(scoreDistributions.map { it.score })
            .setChartBase(binding.seriesScoreDist)
            .build(ctx)

        ChartUtil
            .StepYAxisFormatter()
            .setChartBase(binding.seriesScoreDist)
            .build(ctx)
    }

    private fun configureSeriesStats() {
        binding.seriesStats.setUsePercentValues(true)
        binding.seriesStats.description.isEnabled = false
        binding.seriesStats.setExtraOffsets(0f, 0f, 50f, 0f)
        binding.seriesStats.setDrawHoleEnabled(true)
        binding.seriesStats.holeRadius = 58f
        binding.seriesStats.transparentCircleRadius = 61f

        binding.seriesStats.rotationAngle = 0f
        binding.seriesStats.isRotationEnabled = false
        binding.seriesStats.isHighlightPerTapEnabled = true

        val legend = binding.seriesStats.legend
        legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.xEntrySpace = 0f
        legend.yEntrySpace = 0f
        legend.yOffset = 0f

        binding.seriesStats.setDrawEntryLabels(false)
    }

    // Extracted from MediaPresenter

    private fun getMediaStats(statusDistribution: List<StatusDistribution>): List<PieEntry> {
        val highestStatus = statusDistribution.maxOfOrNull { it.amount } ?: 0
        if (highestStatus > 0) {
            return statusDistribution
                .map { status ->
                    PieEntry(
                        (status.amount * 100f) / highestStatus,
                        String.format(
                            Locale.getDefault(),
                            "%s: %s",
                            CompatUtil.capitalizeWords(status.status),
                            MediaUtil.getFormattedCount(status.amount),
                        ),
                    )
                }.sortedBy { it.label }
        }
        return emptyList()
    }

    private fun getMediaScoreDistribution(scoreDistribution: List<ScoreDistribution>): List<BarEntry> = scoreDistribution.mapIndexed { index, score ->
        BarEntry(index.toFloat(), score.amount.toFloat())
    }
}
