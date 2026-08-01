package com.mxt.anitrend.view.activity.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.ui.commit
import com.mxt.anitrend.ui.model.FragmentItem
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.fragment.detail.StudioMediaFragment
import com.mxt.anitrend.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class StudioActivity : CommonActivity() {

    data class Args(val id: Long)

    companion object {
        fun newIntent(context: Context, id: Long): Intent = Intent(context, StudioActivity::class.java).apply {
            putExtra(KeyUtil.arg_id, id)
        }

        fun fromIntent(intent: Intent): Args? {
            if (!intent.hasExtra(KeyUtil.arg_id)) return null
            val id = intent.getLongExtra(KeyUtil.arg_id, -1)
            return if (id > 0) Args(id) else null
        }
    }

    private lateinit var binding: ActivityFrameGenericBinding

    private var model: StudioBase? = null
    private var studioId: Long = 0
    private var favouriteWidget: FavouriteToolbarWidget? = null
    private val studioViewModel: StudioViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/studio/{id}) so arg_id is injected
        // into the intent before fromIntent reads it. Previously handled by
        // ActivityBase.onCreate → IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        studioId = args.id

        observeViewModel()
        addStudioMediaFragment(intent.extras ?: Bundle.EMPTY)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                studioViewModel.state.collect { state ->
                    when (state) {
                        is StudioViewModel.UiState.Loading -> { /* content loads below */ }
                        is StudioViewModel.UiState.Success -> {
                            model = state.studio
                            updateUI()
                        }
                        is StudioViewModel.UiState.Error -> {
                            NotifyUtil.makeText(
                                this@StudioActivity,
                                state.message,
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
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
            }
            favouriteWidget?.setListener(object : FavouriteToolbarWidget.Listener {
                override fun onToggleFavourite(
                    animeId: Int?,
                    mangaId: Int?,
                    characterId: Int?,
                    staffId: Int?,
                    studioId: Int?,
                    onResult: (Result<Unit>) -> Unit,
                ) {
                    lifecycleScope.launch {
                        onResult(studioViewModel.toggleFavourite(animeId, mangaId, characterId, staffId, studioId))
                    }
                }
            })
        }
        return true
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
            favouriteWidget?.setModel(current)
            supportActionBar?.title = current.name
        }
    }

    override fun onDestroy() {
        favouriteWidget?.setListener(null)
        super.onDestroy()
    }
}
