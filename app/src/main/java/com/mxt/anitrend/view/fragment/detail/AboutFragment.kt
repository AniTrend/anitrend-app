package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.base.ChangelogActivity

/**
 * Created by max on 2018/03/04.
 * Application about screen
 */

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = createM3View(inflater, container)

    // ── Material 3 redesigned layout ──────────────────────────────────

    private fun createM3View(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_about_m3, container, false)

        // Set version text
        val versionText = view.findViewById<TextView>(R.id.about_version_text)
        versionText?.text = getString(
            R.string.text_about_application_version,
            BuildConfig.versionName,
        )

        val versionView = view.findViewById<TextView>(R.id.about_version)
        versionView?.text = getString(
            R.string.text_about_application_version,
            BuildConfig.versionName,
        )

        // Wire click listeners (preserve exact same actions as legacy)
        configureExternalLinkRow(view, R.id.about_rate_play_store, R.string.text_about_rate_play_store) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mxt.anitrend")),
            )
        }

        configureExternalLinkRow(view, R.id.about_follow_twitter, R.string.text_about_follow_twitter) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/anitrend_app")),
            )
        }

        configureExternalLinkRow(view, R.id.about_github, R.string.text_about_github) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AniTrend")),
            )
        }

        configureExternalLinkRow(view, R.id.about_website, R.string.text_about_website) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://anitrend.co")),
            )
        }

        view.findViewById<View>(R.id.about_whats_new)?.apply {
            setOnClickListener {
                val intent = Intent(requireContext(), ChangelogActivity::class.java)
                startActivity(intent)
            }
        }

        configureExternalLinkRow(view, R.id.about_faq, R.string.text_about_frequently_asked_questions) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://anitrend.gitbook.io/project/faq"),
                ),
            )
        }

        configureExternalLinkRow(view, R.id.about_terms, R.string.text_about_terms_and_conditions) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://github.com/AniTrend/anitrend-app/blob/develop/TERMS_OF_SERVICE.md",
                    ),
                ),
            )
        }

        configureExternalLinkRow(view, R.id.about_code_of_conduct, R.string.text_about_code_of_conduct) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://github.com/AniTrend/anitrend-app/blob/develop/CODE_OF_CONDUCT.md",
                    ),
                ),
            )
        }

        return view
    }

    /**
     * Sets a click listener on an external link row and marks it as opening in a browser
     * for accessibility services.
     */
    private fun configureExternalLinkRow(
        view: View,
        rowId: Int,
        labelRes: Int,
        action: () -> Unit,
    ) {
        view.findViewById<View>(rowId)?.apply {
            setOnClickListener { action() }
            contentDescription = getString(
                R.string.about_external_link_content_description,
                getString(labelRes),
            )
        }
    }

    companion object {
        fun newInstance(): AboutFragment = AboutFragment()
    }
}
