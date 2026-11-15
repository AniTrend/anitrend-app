package com.mxt.anitrend.adapter.spinner

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.share.ShareTypeOptionRow

/**
 * Spinner measures adapter rows before attaching them. A native text view supplies the initial
 * wrap-content size until the Compose row can be measured after attachment.
 */
private class ShareTypeOptionAdapterView(
    context: Context,
    viewTreeOwner: SavedStateRegistryOwner,
) : FrameLayout(context) {
    private val composeView =
        ComposeView(context).apply {
            id = View.generateViewId()
            setViewTreeLifecycleOwner(viewTreeOwner)
            // Spinner's popup list owns selection, matching the non-focusable XML row.
            isFocusable = false
            isFocusableInTouchMode = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            setViewTreeSavedStateRegistryOwner(viewTreeOwner)
            layoutParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
            )
        }
    private val rowPadding = context.resources.getDimensionPixelSize(R.dimen.mg_margin)
    private val iconSize = context.resources.getDimensionPixelSize(R.dimen.xl_margin)
    private val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.lg_margin)
    private val rowChromeWidth = rowPadding * 2 + iconSize + itemSpacing * 2
    private val textMeasurer =
        SingleLineTextView(context).apply {
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
        }
    private var rowLabel: String? = null
    private var rowIconRes: Int? = null
    private var contentDirty = false

    init {
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        addView(composeView)
    }

    fun bind(
        label: String,
        iconRes: Int,
    ) {
        if (rowLabel == label && rowIconRes == iconRes) return
        rowLabel = label
        rowIconRes = iconRes
        textMeasurer.text = label
        contentDirty = true
        renderContent()
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        if (!isAttachedToWindow) {
            val textWidthMeasureSpec =
                if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.UNSPECIFIED) {
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                } else {
                    View.MeasureSpec.makeMeasureSpec(
                        (View.MeasureSpec.getSize(widthMeasureSpec) - rowChromeWidth).coerceAtLeast(0),
                        View.MeasureSpec.AT_MOST,
                    )
                }
            textMeasurer.measure(
                textWidthMeasureSpec,
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            val desiredWidth = rowChromeWidth + textMeasurer.measuredWidth
            val desiredHeight = rowPadding * 2 + maxOf(iconSize, textMeasurer.measuredHeight)
            setMeasuredDimension(
                View.resolveSize(desiredWidth, widthMeasureSpec),
                View.resolveSize(desiredHeight, heightMeasureSpec),
            )
            return
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        renderContent()
    }

    private fun renderContent() {
        if (!isAttachedToWindow || !contentDirty) return
        val label = rowLabel ?: return
        val iconRes = rowIconRes ?: return
        composeView.setContent {
            AniTrendTheme {
                ShareTypeOptionRow(
                    label = label,
                    iconRes = iconRes,
                )
            }
        }
        contentDirty = false
        requestLayout()
    }
}

/**
 * Provides Compose-backed share-type Spinner rows.
 *
 * Both view methods are overridden, so ArrayAdapter's required layout resource is unused.
 */
class IconArrayAdapter(
    context: Context,
    private val viewTreeOwner: SavedStateRegistryOwner,
    objects: List<String>,
) : ArrayAdapter<String>(context, 0, objects) {
    private lateinit var indexIconMap: Map<Int, Int>

    /**
     * Set a map containing the index relative to the title containing a drawable int res
     * @param indexIconMap map of signature (position, R.drawable)
     */
    fun setIndexIconMap(indexIconMap: Map<Int, Int>) {
        this.indexIconMap = indexIconMap
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View = renderOptionView(position, convertView, parent)

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val popupRoot = parent.rootView
        if (popupRoot.findViewTreeLifecycleOwner() == null) {
            // Compose resolves the popup's window recomposer from its decor root.
            popupRoot.setViewTreeLifecycleOwner(viewTreeOwner)
        }
        return renderOptionView(position, convertView, parent)
    }

    private fun renderOptionView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val drawable =
            indexIconMap[position]
                ?: throw IllegalStateException("Missing icon for position $position")
        val label = getItem(position).orEmpty()
        val adapterView =
            (convertView as? ShareTypeOptionAdapterView)
                ?: ShareTypeOptionAdapterView(parent.context, viewTreeOwner)

        adapterView.bind(label, drawable)
        return adapterView
    }
}
