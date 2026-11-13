package com.mxt.anitrend.base.custom.view.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.medialist.MediaListNumericControl
import java.util.Locale

open class ProgressWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView {
    private var progressMaximum: Int = 0
    var progressCurrent: Int = 0
        private set

    protected var currentText: TextFieldValue by mutableStateOf(TextFieldValue())
    protected var maximumLabel: String? by mutableStateOf(null)
    protected var keyboardType: KeyboardType by mutableStateOf(KeyboardType.Number)
    protected var deltaFactor: Float = 0f
    private lateinit var composeView: ComposeView

    init {
        onInit()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    override fun onInit() {
        setDefaultDeltaFactor()
        composeView =
            ComposeView(context).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                setContent {
                    AniTrendTheme {
                        MediaListNumericControl(
                            value = currentText,
                            maximumLabel = maximumLabel,
                            keyboardType = keyboardType,
                            onValueChange = ::handleValueChange,
                            onDecrement = ::onDecrement,
                            onIncrement = ::onIncrement,
                        )
                    }
                }
            }
        addView(
            composeView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
    }

    override fun onViewRecycled() {
        composeView.disposeComposition()
    }

    protected open fun setDefaultDeltaFactor() {
        deltaFactor = 1f
    }

    fun setProgressMaximum(progressMaximum: Int) {
        this.progressMaximum = progressMaximum
        maximumLabel = String.format(Locale.getDefault(), "/ %d", progressMaximum)
    }

    fun setProgressCurrent(progressCurrent: Int) {
        this.progressCurrent = progressCurrent
        setCurrentText(progressCurrent.toString())
    }

    protected fun setCurrentText(value: String) {
        currentText = TextFieldValue(value, selection = TextRange(value.length))
    }

    fun getProgressMaximum(): Int = progressMaximum

    protected open fun handleValueChange(currentChange: TextFieldValue) {
        val text = currentChange.text
        if (!isValidInput(text)) return
        val temporaryValue = if (text.isNotEmpty()) text.toInt() else 0
        if (boundCheck(temporaryValue)) {
            progressCurrent = temporaryValue
            currentText = currentChange
        } else if (boundCheck(progressCurrent)) {
            setCurrentText(progressCurrent.toString())
        } else {
            currentText = currentChange
        }
    }

    protected open fun isValidInput(value: String): Boolean = value.all(Char::isDigit)

    protected open fun onIncrement() {
        progressChange(progressCurrent + deltaFactor.toInt())
    }

    protected open fun onDecrement() {
        progressChange(progressCurrent - deltaFactor.toInt())
    }

    private fun boundCheck(delta: Int): Boolean = if (progressMaximum < 1) {
        delta > -1
    } else {
        delta > -1 && delta <= progressMaximum
    }

    private fun progressChange(delta: Int) {
        if (boundCheck(delta)) {
            progressCurrent = delta
            setCurrentText(progressCurrent.toString())
        }
    }
}
