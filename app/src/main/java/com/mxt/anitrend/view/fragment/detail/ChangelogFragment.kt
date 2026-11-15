package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.changelog.ChangelogMarkdown
import com.mxt.anitrend.ui.changelog.ChangelogScreen
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import timber.log.Timber
import java.io.IOException

/**
 * Fragment that hosts the Compose changelog while retaining Navigation 2 ownership.
 */
class ChangelogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val markdown =
            try {
                requireContext().assets
                    .open("changelog.md")
                    .bufferedReader()
                    .use { it.readText() }
            } catch (e: IOException) {
                Timber.e(e)
                null
            }
        val versionName = String.format("v%s", BuildConfig.versionName)

        return ComposeView(requireContext()).apply {
            id = R.id.changelog_compose_view
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme {
                    ChangelogScreen(
                        versionName = versionName,
                        onDismiss = { findNavController().navigateUp() },
                    ) {
                        ChangelogMarkdown(markdown)
                    }
                }
            }
        }
    }
}
