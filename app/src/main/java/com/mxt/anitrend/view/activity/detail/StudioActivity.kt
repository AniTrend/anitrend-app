package com.mxt.anitrend.view.activity.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.databinding.ActivityStudioBinding
import com.mxt.anitrend.domain.model.StudioRecord
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.ui.commit
import com.mxt.anitrend.ui.model.FragmentItem
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.fragment.detail.StudioMediaFragment
import com.mxt.anitrend.viewmodel.StudioViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class StudioActivity : CommonActivity() {

    companion object {
        fun newIntent(context: Context, param: StudioScreenParam): Intent = Intent(context, StudioActivity::class.java).apply {
            putScreenParam(param)
            // Interim boundary: keep the legacy wire key until the StudioMediaFragment
            // destination reads the typed parameter directly.
            putExtra(KeyUtil.arg_id, param.studioId)
        }

        /**
         * Compatibility overload preserving the legacy id-based callers. Bridges into the
         * typed parameter so navigation always uses [StudioScreenParam].
         */
        fun newIntent(context: Context, id: Long): Intent = newIntent(context, StudioScreenParam(studioId = id))

        /**
         * Resolves the typed parameter from the intent.
         *
         * The typed parameter is read first. Deep links (injected by
         * [IntentBundleUtil.checkIntentData]) still write the legacy [KeyUtil.arg_id]
         * extra, so that value is bridged here into [StudioScreenParam]. The bridge is a
         * single scalar conversion point inside the activity, not a parcel path for the
         * studio entity.
         */
        fun fromIntent(intent: Intent): StudioScreenParam? {
            intent.screenParam<StudioScreenParam>()?.let { param ->
                return if (param.studioId > 0) param else null
            }
            val id = intent.getLongExtra(KeyUtil.arg_id, -1)
            return if (id > 0) StudioScreenParam(studioId = id) else null
        }
    }

    private lateinit var binding: ActivityStudioBinding

    private var model: StudioRecord? = null
    private var studioId: Long = 0
    private var favouriteWidget: FavouriteToolbarWidget? = null
    private val studioViewModel: StudioViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/studio/{id}) so arg_id is injected
        // into the intent before fromIntent reads it. Previously handled by
        // ActivityBase.onCreate → IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.studioErrorRetry.setOnClickListener {
            studioViewModel.load(studioId)
        }

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
        studioId = args.studioId

        observeViewModel()
        addStudioMediaFragment(intent.extras ?: Bundle.EMPTY)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    studioViewModel.state.collect { state ->
                        when (state) {
                            is StudioViewModel.UiState.Loading -> showLoadingState()
                            is StudioViewModel.UiState.Success -> {
                                model = state.studio
                                showContentState()
                                updateUI()
                            }
                            is StudioViewModel.UiState.Error -> {
                                showErrorState(state.message)
                            }
                        }
                    }
                }
                launch {
                    // Observe the canonical favourite store through the ViewModel and
                    // re-render after every committed mutation (or in-flight loading change).
                    combine(
                        studioViewModel.favouriteFlag,
                        studioViewModel.favouriteLoading,
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
    }

    private fun addStudioMediaFragment(args: Bundle) {
        FragmentItem(fragment = StudioMediaFragment::class.java, parameter = args)
            .commit(contentFrame = R.id.content_frame, context = this@StudioActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = studioViewModel.isAuthenticated()
        menuInflater.inflate(R.menu.custom_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth
        if (isAuth) {
            val favouriteMenuItem = menu.findItem(R.id.action_favourite)
            favouriteWidget = favouriteMenuItem.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                favouriteMenuItem.isVisible = false
            } else {
                favouriteWidget?.setOnToggleAction {
                    studioViewModel.toggleFavouriteStudio(studioId)
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
                flag = studioViewModel.favouriteFlag.value,
                fallbackIsFavourite = model?.isFavourite ?: false,
                isLoading = studioViewModel.favouriteLoading.value,
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
                R.id.action_share -> {
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                String.format(
                                    Locale.getDefault(),
                                    "%s - %s",
                                    current.name,
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
        studioViewModel.load(studioId)
    }

    private fun updateUI() {
        model?.let { current ->
            supportActionBar?.title = current.name
        }
    }

    private fun showLoadingState() {
        binding.studioStateOverlay.visibility = VISIBLE
        binding.studioLoadingState.visibility = VISIBLE
        binding.studioErrorState.visibility = GONE
        binding.studioStateOverlay.contentDescription =
            getString(R.string.studio_loading_content_description)
    }

    private fun showContentState() {
        binding.studioStateOverlay.visibility = GONE
    }

    private fun showErrorState(message: String) {
        binding.studioStateOverlay.visibility = VISIBLE
        binding.studioLoadingState.visibility = GONE
        binding.studioErrorState.visibility = VISIBLE
        binding.studioErrorText.text = message
        binding.studioStateOverlay.contentDescription = message
    }

    override fun onDestroy() {
        favouriteWidget?.setOnToggleAction(null)
        super.onDestroy()
    }
}
