package com.mxt.anitrend.view.activity.detail

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
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.CharacterPageAdapter
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.databinding.ActivityCharacterBinding
import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.viewmodel.CharacterViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Created by max on 2017/12/14.
 * character activity
 */
class CharacterActivity : CommonActivity() {

    private lateinit var binding: ActivityCharacterBinding

    private var model: CharacterRecord? = null
    private var characterId: Long = 0
    private var favouriteWidget: FavouriteToolbarWidget? = null
    private val characterViewModel: CharacterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process deep links (e.g. anilist.co/character/{id}) so arg_id is injected
        // into the intent before we read it. Previously handled by
        // ActivityBase.onCreate -> IntentBundleUtil.checkIntentData.
        IntentBundleUtil(intent).checkIntentData(this)

        binding = ActivityCharacterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.characterErrorRetry.setOnClickListener {
            characterViewModel.load(characterId)
        }

        if (intent.hasExtra(KeyUtil.arg_id)) {
            characterId = intent.getLongExtra(KeyUtil.arg_id, -1)
        }

        observeViewModel()
        setUpPager()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterViewModel.state.collect { state ->
                    when (state) {
                        is CharacterViewModel.UiState.Loading -> showLoadingState()
                        is CharacterViewModel.UiState.Success -> {
                            model = state.character
                            showContentState()
                            updateUI()
                        }
                        is CharacterViewModel.UiState.Error -> {
                            showErrorState(state.message)
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
                    characterViewModel.favouriteFlag,
                    characterViewModel.favouriteLoading,
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
        val pageAdapter =
            CharacterPageAdapter(this, applicationContext).apply {
                params = intent.extras ?: Bundle.EMPTY
            }
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = 3
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = pageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val isAuth = KoinExt.get(Settings::class.java).isAuthenticated
        menuInflater.inflate(R.menu.custom_menu, menu)
        menu.findItem(R.id.action_favourite).isVisible = isAuth
        if (isAuth) {
            val favouriteMenuItem = menu.findItem(R.id.action_favourite)
            favouriteWidget = favouriteMenuItem.actionView as? FavouriteToolbarWidget
            if (favouriteWidget == null) {
                favouriteMenuItem.isVisible = false
            } else {
                favouriteWidget?.setOnToggleAction {
                    characterViewModel.toggleFavouriteCharacter(characterId)
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
                flag = characterViewModel.favouriteFlag.value,
                fallbackIsFavourite = model?.isFavourite ?: false,
                isLoading = characterViewModel.favouriteLoading.value,
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
                                    current.name ?: "",
                                    current.siteUrl ?: "",
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
        characterViewModel.load(characterId)
    }

    private fun updateUI() {
        model?.let { current ->
            binding.characterDisplayName.text = current.name
            binding.characterIdentityTier.visibility = VISIBLE
        }
    }

    private fun showLoadingState() {
        binding.characterStateOverlay.visibility = VISIBLE
        binding.characterLoadingState.visibility = VISIBLE
        binding.characterErrorState.visibility = GONE
        binding.characterStateOverlay.contentDescription =
            getString(R.string.character_loading_content_description)
    }

    private fun showContentState() {
        binding.characterStateOverlay.visibility = GONE
    }

    private fun showErrorState(message: String) {
        binding.characterStateOverlay.visibility = VISIBLE
        binding.characterLoadingState.visibility = GONE
        binding.characterErrorState.visibility = VISIBLE
        binding.characterErrorText.text = message
        binding.characterStateOverlay.contentDescription = message
    }

    override fun onDestroy() {
        favouriteWidget?.setOnToggleAction(null)
        super.onDestroy()
    }
}
