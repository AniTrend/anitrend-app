package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mxt.anitrend.R
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.adapter.pager.detail.AnimePageAdapter
import com.mxt.anitrend.adapter.pager.detail.MangaPageAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.databinding.ActivitySeriesBinding
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.TapTargetUtil
import com.mxt.anitrend.util.TutorialUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import io.github.wax911.library.model.request.QueryContainerBuilder
import java.util.Locale

/**
 * Created by max on 2017/12/01.
 * Media activity
 */
class MediaActivity : ActivityBase<MediaBase, MediaPresenter>(), View.OnClickListener {

    private lateinit var binding: ActivitySeriesBinding

    @KeyUtil.MediaType
    private var mediaType: String? = null

    private var favouriteWidget: FavouriteToolbarWidget? = null
    private var malMenuItem: MenuItem? = null
    private var manageMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPresenter(MediaPresenter(applicationContext))
        setSupportActionBar(binding.toolbar.toolbar)
        disableToolbarTitle()
        binding.seriesBanner.setOnClickListener(this)
        setViewModel(true)
        if (intent.hasExtra(KeyUtil.arg_id))
            id = intent.getLongExtra(KeyUtil.arg_id, -1)
        if (intent.hasExtra(KeyUtil.arg_mediaType))
            mediaType = intent.getStringExtra(KeyUtil.arg_mediaType)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mActionBar?.setHomeAsUpIndicator(getCompatDrawable(R.drawable.ic_arrow_back_white_24dp))
        onActivityReady()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = presenter.settings.isAuthenticated
        menuInflater.inflate(R.menu.media_base_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth

        val model = getModel()
        malMenuItem = menu.findItem(R.id.action_mal)
        malMenuItem?.isVisible = model?.idMal?.let { it > 0 } ?: false

        manageMenuItem = menu.findItem(R.id.action_manage)
        manageMenuItem?.isVisible = isAuth
        setMenuItemIcons()

        if (isAuth) {
            val favouriteMenuItem = menu.findItem(R.id.action_favourite)
            favouriteWidget = favouriteMenuItem.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                favouriteMenuItem.isVisible = false
            } else {
                setFavouriteWidgetMenuItemIcon()
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val model = getModel()
        if (model != null) {
            when (item.itemId) {
                R.id.action_manage -> {
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(model.id).build(this)
                    mediaActionUtil?.startSeriesAction()
                }
                R.id.action_share -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(
                            Intent.EXTRA_TEXT,
                            String.format(
                                Locale.getDefault(),
                                "%s - %s",
                            model.title?.userPreferred ?: "",
                                model.siteUrl
                            )
                        )
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                }
                R.id.action_mal -> {
                    mediaType?.let { type ->
                        val url = String.format(
                            Locale.getDefault(),
                            "https://myanimelist.net/%s/%d",
                            type.lowercase(Locale.getDefault()),
                            model.idMal
                        )
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(Intent.createChooser(intent, getString(R.string.abc_shareactionprovider_share_with)))
                    }
                }
            }
        } else {
            NotifyUtil.makeText(
                applicationContext,
                R.string.text_activity_loading,
                Toast.LENGTH_SHORT
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        val type = mediaType
        if (type != null) {
            val baseStatePageAdapter: BaseStatePageAdapter =
                if (!CompatUtil.equals(type, KeyUtil.ANIME))
                    MangaPageAdapter(this, applicationContext)
                else
                    AnimePageAdapter(this, applicationContext)
            baseStatePageAdapter.params = intent.extras ?: Bundle.EMPTY
            binding.pageContainer.pageContainer.adapter = baseStatePageAdapter
            binding.pageContainer.pageContainer.offscreenPageLimit = offScreenLimit
            TabLayoutMediator(binding.smartTab.smartTab, binding.pageContainer.pageContainer) { tab, position ->
                tab.text = baseStatePageAdapter.getPageTitle(position)
            }.attach()
        } else {
            NotifyUtil.createAlerter(
                this,
                R.string.text_error_request,
                R.string.text_unknown_error,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (getModel() == null)
            makeRequest()
        else
            updateUI()
    }

    override fun updateUI() {
        val model = getModel()
        if (model != null) {
            WideImageView.setImage(binding.seriesBanner, model.bannerImage)
            setFavouriteWidgetMenuItemIcon()
            setMenuItemIcons()
            if (presenter.settings.isAuthenticated) {
                val favouritesPrompt = TutorialUtil().setContext(this)
                    .setFocalColour(R.color.colorGrey600)
                    .setTapTarget(KeyUtil.KEY_DETAIL_TIP)
                    .setSettings(presenter.settings)
                    .createTapTarget(
                        R.string.tip_series_options_title,
                        R.string.tip_series_options_message,
                        R.id.action_manage
                    )
                TapTargetUtil.showMultiplePrompts(favouritesPrompt)
            }
        }
    }

    override fun makeRequest() {
        val queryContainer: QueryContainerBuilder = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_mediaType, mediaType)
            .putVariable(KeyUtil.arg_id, id)

        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_BASE_REQ, applicationContext)
    }

    override fun onChanged(model: MediaBase?) {
        super.onChanged(model)
        updateUI()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.series_banner -> {
                val model = getModel()
                if (model != null) {
                    CompatUtil.imagePreview(
                        view,
                        model.bannerImage,
                        R.string.image_preview_error_series_banner
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        favouriteWidget?.onViewRecycled()
        super.onDestroy()
    }

    private fun setMenuItemIcons() {
        val model = getModel()
        if (model != null) {
            if (model.mediaListEntry != null && manageMenuItem != null)
                manageMenuItem?.icon = getCompatDrawable(R.drawable.ic_mode_edit_white_24dp)
            malMenuItem?.isVisible = model.idMal > 0
        }
    }

    private fun setFavouriteWidgetMenuItemIcon() {
        val model = getModel()
        if (model != null)
            favouriteWidget?.setModel(model)
    }
}
