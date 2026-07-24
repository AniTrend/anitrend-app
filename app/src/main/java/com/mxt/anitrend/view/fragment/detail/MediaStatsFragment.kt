package com.mxt.anitrend.view.fragment.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.util.media.MediaUtil
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity
import com.mxt.anitrend.viewmodel.MediaStatsViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2017/12/28.
 */
class MediaStatsFragment : Fragment() {
    private var _binding: FragmentSeriesStatsBinding? = null
    private val binding get() = _binding!!

    private var model: Media? = null
    private var clipboardManager: ClipboardManager? = null

    private var rankAdapter: RankAdapter? = null
    private var linkAdapter: LinkAdapter? = null

    private var mediaId: Long = 0

    @KeyUtil.MediaType
    private var mediaType: String? = null

    private val settings: Settings by inject()

    private val mediaStatsViewModel: MediaStatsViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaStatsFragment = MediaStatsFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            mediaId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSeriesStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                                requireContext().getCompatDrawable(R.drawable.ic_emoji_sweat),
                                state.message,
                                getString(R.string.try_again),
                            ) { loadStats() }
                        }
                    }
                }
            }
        }

        loadStats()
    }

    private fun loadStats() {
        binding.stateLayout.showLoading()
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        mediaStatsViewModel.load(mediaId = mediaId, type = type, isAdult = isAdult)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI() {
        val ctx = context ?: return
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
                                val host = activity ?: return
                                val intent = Intent(host, MediaBrowseActivity::class.java)
                                val args = Bundle()
                                args.putString(KeyUtil.arg_mediaType, mediaType)
                                args.putString(KeyUtil.arg_format, data.value.format)
                                args.putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                                if (!settings.displayAdultContent) {
                                    args.putBoolean(KeyUtil.arg_isAdult, false)
                                }

                                if (MediaUtil.isAnimeType(model)) {
                                    args.putString(KeyUtil.arg_season, data.value.season)
                                }

                                if (!data.value.isAllTime) {
                                    if (MediaUtil.isAnimeType(model)) {
                                        args.putInt(KeyUtil.arg_seasonYear, data.value.year)
                                    } else {
                                        args.putString(
                                            KeyUtil.arg_startDateLike,
                                            String.format(Locale.getDefault(), "%d%%", data.value.year),
                                        )
                                    }
                                }

                                when (data.value.type) {
                                    KeyUtil.RATED ->
                                        args.putString(KeyUtil.arg_sort, KeyUtil.SCORE + KeyUtil.DESC)
                                    KeyUtil.POPULAR ->
                                        args.putString(KeyUtil.arg_sort, KeyUtil.POPULARITY + KeyUtil.DESC)
                                }
                                args.putParcelable(
                                    KeyUtil.arg_media_util,
                                    MediaBrowseUtil().setCompactType(true).setFilterEnabled(false),
                                )
                                args.putString(KeyUtil.arg_activity_tag, data.value.typeHtmlPlainTitle)
                                intent.putExtras(args)
                                startActivity(intent)
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
                                val intent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        setData(Uri.parse(data.value.url))
                                    }
                                startActivity(intent)
                            }

                            override fun onItemLongClick(
                                target: View,
                                data: IndexedValue<ExternalLink>,
                            ) {
                                clipboardManager?.setPrimaryClip(
                                    ClipData.newPlainText("", data.value.url),
                                )
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    context?.let {
                                        NotifyUtil
                                            .makeText(
                                                it,
                                                R.string.text_url_copied_to_clipboard,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                }
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
        val ctx = context ?: return
        val scoreDistribution = model?.stats?.scoreDistribution ?: return

        val barEntries = getMediaScoreDistribution(scoreDistribution)
        val barDataSet =
            BarDataSet(barEntries, getString(R.string.title_score_distribution)).apply {
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
        val ctx = context ?: return
        val statusDistribution = model?.stats?.statusDistribution ?: return

        configureSeriesStats()

        val pieEntries: List<PieEntry> = getMediaStats(statusDistribution)
        val pieDataSet =
            PieDataSet(pieEntries, getString(R.string.title_series_stats)).apply {
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
        val ctx = context ?: return
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
