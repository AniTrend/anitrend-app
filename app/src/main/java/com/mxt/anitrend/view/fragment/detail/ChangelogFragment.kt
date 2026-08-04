package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.binding.richMarkDown
import timber.log.Timber
import java.io.IOException

/**
 * Fragment that renders the changelog content as a full-screen destination.
 */
class ChangelogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_changelog_m3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set version text
        val versionView = view.findViewById<MaterialTextView>(R.id.changelog_version)
        versionView?.text = String.format("v%s", BuildConfig.versionName)

        // Load and render changelog markdown from assets
        try {
            val changelog = requireContext().assets
                .open("changelog.md")
                .bufferedReader()
                .use { it.readText() }
            val richMarkdownTextView =
                view.findViewById<RichMarkdownTextView>(R.id.changelog_information)
            richMarkdownTextView?.richMarkDown(changelog)
        } catch (e: IOException) {
            Timber.e(e)
        }

        // Wire dismiss button -- finish the host activity
        view.findViewById<MaterialButton>(R.id.changelog_dismiss)?.setOnClickListener {
            requireActivity().finish()
        }
    }
}
