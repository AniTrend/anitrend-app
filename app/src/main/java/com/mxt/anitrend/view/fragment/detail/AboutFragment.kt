package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.DialogUtil

/**
 * Created by max on 2018/03/04.
 * Application about screen
 */

class AboutFragment : FragmentBase<Void, BasePresenter, Void>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPresenter(BasePresenter(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val context = requireContext()
        return ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            isFillViewport = true
            addView(
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    setPadding(dp(24), dp(32), dp(24), dp(32))

                    // App icon
                    addView(
                        ImageView(context).apply {
                            setImageResource(R.mipmap.ic_launcher)
                            layoutParams = LinearLayout.LayoutParams(dp(96), dp(96)).apply {
                                gravity = Gravity.CENTER_HORIZONTAL
                                bottomMargin = dp(24)
                            }
                        },
                    )

                    // Section 1 - General Information
                    addView(headerView(context, R.string.text_about_general_information))
                    addView(descriptionView(context, R.string.app_description))
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_system_update_grey_600_24dp,
                            getString(R.string.text_about_application_version, BuildConfig.versionName),
                        ),
                    )
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_cloud_download_white_24dp,
                            "Rate on Play Store",
                        ) {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mxt.anitrend")),
                            )
                        },
                    )
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_share_white_24dp,
                            "Follow on Twitter",
                        ) {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/anitrend_app")),
                            )
                        },
                    )

                    // Section 2 - Additional Information
                    addView(headerView(context, R.string.text_about_additional_information))
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_code_grey_600_24dp,
                            "GitHub",
                        ) {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AniTrend")),
                            )
                        },
                    )
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_link_white_24dp,
                            "Website",
                        ) {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://anitrend.co")),
                            )
                        },
                    )
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_fiber_new_white_24dp,
                            getString(R.string.text_what_is_new),
                        ) {
                            DialogUtil.createChangeLog(requireContext())
                        },
                    )
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_help_grey_600_24dp,
                            getString(R.string.text_about_frequently_asked_questions),
                        ) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://anitrend.gitbook.io/project/faq"),
                                ),
                            )
                        },
                    )

                    // Section 3 - Legal Information
                    addView(headerView(context, R.string.text_about_legal_information))
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_privacy_grey_600_24dp,
                            getString(R.string.text_about_terms_and_conditions),
                        ) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        "https://github.com/AniTrend/anitrend-app/blob/develop/TERMS_OF_SERVICE.md",
                                    ),
                                ),
                            )
                        },
                    )
                    addView(
                        itemView(
                            context,
                            R.drawable.ic_privacy_grey_600_24dp,
                            getString(R.string.text_about_code_of_conduct),
                        ) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        "https://github.com/AniTrend/anitrend-app/blob/develop/CODE_OF_CONDUCT.md",
                                    ),
                                ),
                            )
                        },
                    )
                },
            )
        }
    }

    /**
     * Creates a section header TextView with top padding.
     */
    private fun headerView(context: Context, @StringRes text: Int): TextView {
        val colorOnSurface = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorOnSurface,
            "AboutFragment",
        )
        return TextView(context).apply {
            setText(text)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6)
            setTextColor(colorOnSurface)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(28)
                bottomMargin = dp(8)
            }
        }
    }

    /**
     * Creates a description TextView below the header.
     */
    private fun descriptionView(context: Context, @StringRes text: Int): TextView {
        val colorOnSurfaceVariant = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            "AboutFragment",
        )
        return TextView(context).apply {
            setText(text)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1)
            setTextColor(colorOnSurfaceVariant)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(12)
            }
        }
    }

    /**
     * Creates a clickable row with an icon and title.
     * When [onClick] is supplied the row gets a ripple foreground and click listener.
     */
    private fun itemView(
        context: Context,
        @DrawableRes icon: Int,
        title: String,
        onClick: (() -> Unit)? = null,
    ): LinearLayout {
        val colorOnSurface = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorOnSurface,
            "AboutFragment",
        )
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            minimumHeight = dp(48)

            if (onClick != null) {
                isClickable = true
                isFocusable = true
                foreground = resolveRippleDrawable(context)
                setOnClickListener { onClick() }
            }

            addView(
                ImageView(context).apply {
                    setImageResource(icon)
                    layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                        marginEnd = dp(16)
                    }
                },
            )

            addView(
                TextView(context).apply {
                    text = title
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1)
                    setTextColor(colorOnSurface)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f,
                    )
                },
            )
        }
    }

    /**
     * Resolves `?selectableItemBackground` from the current theme.
     */
    private fun resolveRippleDrawable(context: Context): android.graphics.drawable.Drawable? {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue,
            true,
        )
        return ContextCompat.getDrawable(context, outValue.resourceId)
    }

    /**
     * Converts density-independent pixels to raw pixels.
     */
    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics,
    ).toInt()

    override fun updateUI() {
    }

    override fun makeRequest() {
    }

    override fun onChanged(model: Void?) {
    }

    companion object {
        fun newInstance(): AboutFragment = AboutFragment()
    }
}
