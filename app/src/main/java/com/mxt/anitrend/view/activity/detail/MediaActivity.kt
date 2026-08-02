package com.mxt.anitrend.view.activity.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.AnimePageAdapter
import com.mxt.anitrend.adapter.pager.detail.MangaPageAdapter
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.databinding.ActivitySeriesBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.TapTargetUtil
import com.mxt.anitrend.util.TutorialUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.viewmodel.MediaViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2017/12/01.
 * Media activity
 */
class MediaActivity :
    CommonActivity(),
    View.OnClickListener {

    companion object {
        fun newIntent(context: Context, param: MediaScreenParam): Intent = Intent(context, MediaActivity::class.java).apply {
            putScreenParam(param)
            // Interim boundary: the pager and its fragments still consume the legacy
            // wire keys from the activity extras, so keep them alongside the typed param.
            putExtra(KeyUtil.arg_id, param.mediaId)
            putExtra(KeyUtil.arg_mediaType, param.mediaType)
        }

        /**
         * Compatibility overload preserving the legacy id/type-based callers. Bridges into
         * the typed parameter so navigation always uses [MediaScreenParam].
         */
        fun newIntent(context: Context, mediaId: Long, mediaType: String?): Intent =
            newIntent(context, MediaScreenParam(mediaId = mediaId, mediaType = mediaType))

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Deep links (injected by
         * [IntentBundleUtil.checkIntentData]) still write the legacy [KeyUtil.arg_id] and
         * [KeyUtil.arg_mediaType] extras, so those values are bridged here into
         * [MediaScreenParam]. The bridge is a scalar conversion point inside the activity,
         * not a parcel path for the media entity.
         */
        fun fromIntent(intent: Intent): MediaScreenParam? {
            intent.screenParam<MediaScreenParam>()?.let { param ->
                return if (param.mediaId > 0) param else null
            }
            val id = intent.getLongExtra(KeyUtil.arg_id, -1)
            return if (id > 0) {
                MediaScreenParam(mediaId = id, mediaType = intent.getStringExtra(KeyUtil.arg_mediaType))
            } else {
                null
            }
        }
    }

    private lateinit var binding: ActivitySeriesBinding

    @KeyUtil.MediaType
    private var mediaType: String? = null

    private var model: MediaBase? = null
    private var mediaId: Long = 0

    private var favouriteWidget: FavouriteToolbarWidget? = null
    private var malMenuItem: MenuItem? = null
    private var manageMenuItem: MenuItem? = null
    private var mediaActionUtil: MediaActionUtil? = null

    private val mediaViewModel: MediaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/anime/{id}) so arg_id is injected
        // into the intent before we read it. Previously handled by
        // ActivityBase.onCreate -> IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivitySeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(
            getCompatDrawable(R.drawable.ic_arrow_back_white_24dp),
        )
        binding.seriesBanner.setOnClickListener(this)

        // Resolve the destination through the typed parameter, falling back to the
        // legacy wire keys for deep links and pre-bridge callers.
        val args = fromIntent(intent)
        if (args == null) {
            NotifyUtil.makeText(
                this,
                R.string.text_error_request,
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_SHORT,
            ).show()
            finish()
            return
        }
        mediaId = args.mediaId
        mediaType = args.mediaType

        observeViewModel()
        setUpPager()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaViewModel.state.collect { state ->
                    when (state) {
                        is MediaViewModel.UiState.Loading -> { /* content loads below */ }
                        is MediaViewModel.UiState.Success -> {
                            model = state.media
                            updateUI()
                        }
                        is MediaViewModel.UiState.Error -> {
                            NotifyUtil.makeText(
                                this@MediaActivity,
                                state.message,
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the canonical favourite store through the ViewModel and
                // re-render after every committed mutation (or in-flight loading change).
                combine(
                    mediaViewModel.favouriteFlag,
                    mediaViewModel.favouriteLoading,
                ) { flag, loading ->
                    FavouriteWidgetRenderState.fromFlag(
                        flag = flag,
                        fallbackIsFavourite = model?.isFavourite ?: false,
                        isLoading = loading,
                    )
                }.collect { renderState ->
                    favouriteWidget?.render(renderState)
                }
            }
        }
    }

    private fun setUpPager() {
        val type = mediaType
        if (type != null) {
            val baseStatePageAdapter: BaseStatePageAdapter =
                if (!CompatUtil.equals(type, KeyUtil.ANIME)) {
                    MangaPageAdapter(this, applicationContext)
                } else {
                    AnimePageAdapter(this, applicationContext)
                }
            baseStatePageAdapter.params = intent.extras ?: Bundle.EMPTY
            binding.pageContainer.pageContainer.adapter = baseStatePageAdapter
            binding.pageContainer.pageContainer.offscreenPageLimit = 3
            TabLayoutMediator(
                binding.smartTab.smartTab,
                binding.pageContainer.pageContainer,
            ) { tab, position ->
                tab.text = baseStatePageAdapter.getPageTitle(position)
            }.attach()
        } else {
            NotifyUtil.createAlerter(
                this,
                R.string.text_error_request,
                R.string.text_unknown_error,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed,
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = settings.isAuthenticated
        menuInflater.inflate(R.menu.media_base_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth

        val current = model
        malMenuItem = menu.findItem(R.id.action_mal)
        malMenuItem?.isVisible = current?.idMal?.let { it > 0 } ?: false

        manageMenuItem = menu.findItem(R.id.action_manage)
        manageMenuItem?.isVisible = isAuth
        setMenuItemIcons()

        if (isAuth) {
            val favouriteMenuItem = menu.findItem(R.id.action_favourite)
            favouriteWidget = favouriteMenuItem.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                favouriteMenuItem.isVisible = false
            } else {
                favouriteWidget?.setOnToggleAction {
                    mediaViewModel.toggleFavouriteMedia(mediaId, mediaType)
                }
                // The widget is created after the observeViewModel collectors start, so
                // render once with the current values and let the collector re-render on
                // any subsequent store or loading change.
                renderFavouriteWidget()
            }
        }
        return true
    }

    private fun renderFavouriteWidget() {
        favouriteWidget?.render(
            FavouriteWidgetRenderState.fromFlag(
                flag = mediaViewModel.favouriteFlag.value,
                fallbackIsFavourite = model?.isFavourite ?: false,
                isLoading = mediaViewModel.favouriteLoading.value,
            ),
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        val current = model
        if (current != null) {
            when (item.itemId) {
                R.id.action_manage -> {
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(current.id)
                            .build(this)
                    mediaActionUtil?.startSeriesAction()
                    return true
                }
                R.id.action_share -> {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                String.format(
                                    Locale.getDefault(),
                                    "%s - %s",
                                    current.title?.userPreferred ?: "",
                                    current.siteUrl,
                                ),
                            )
                            type = "text/plain"
                        }
                    startActivity(
                        Intent.createChooser(
                            intent,
                            getString(R.string.abc_shareactionprovider_share_with),
                        ),
                    )
                    return true
                }
                R.id.action_mal -> {
                    mediaType?.let { type ->
                        val url =
                            String.format(
                                Locale.getDefault(),
                                "https://myanimelist.net/%s/%d",
                                type.lowercase(Locale.getDefault()),
                                current.idMal,
                            )
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(
                            Intent.createChooser(
                                intent,
                                getString(R.string.abc_shareactionprovider_share_with),
                            ),
                        )
                    }
                    return true
                }
            }
        } else {
            NotifyUtil
                .makeText(
                    applicationContext,
                    R.string.text_activity_loading,
                    Toast.LENGTH_SHORT,
                ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mediaViewModel.load(mediaId, mediaType, settings.displayAdultContent)
    }

    private fun updateUI() {
        model?.let { current ->
            WideImageView.setImage(binding.seriesBanner, current.bannerImage)
            setMenuItemIcons()
            if (settings.isAuthenticated) {
                val favouritesPrompt =
                    TutorialUtil()
                        .setContext(this)
                        .setFocalColour(R.color.colorGrey600)
                        .setTapTarget(KeyUtil.KEY_DETAIL_TIP)
                        .setSettings(settings)
                        .createTapTarget(
                            R.string.tip_series_options_title,
                            R.string.tip_series_options_message,
                            R.id.action_manage,
                        )
                TapTargetUtil.showMultiplePrompts(favouritesPrompt)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.series_banner -> {
                model?.let { current ->
                    CompatUtil.imagePreview(
                        view,
                        current.bannerImage,
                        R.string.image_preview_error_series_banner,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        favouriteWidget?.setOnToggleAction(null)
        favouriteWidget?.onViewRecycled()
        mediaActionUtil?.onDestroy()
        super.onDestroy()
    }

    private fun setMenuItemIcons() {
        model?.let { current ->
            if (current.mediaListEntry != null && manageMenuItem != null) {
                manageMenuItem?.icon = getCompatDrawable(R.drawable.ic_mode_edit_white_24dp)
            }
            malMenuItem?.isVisible = current.idMal > 0
        }
    }
}
