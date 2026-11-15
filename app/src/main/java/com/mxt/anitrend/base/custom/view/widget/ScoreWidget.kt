package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.util.Locale
class ScoreWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ProgressWidget(context, attrs, defStyleAttr) {
    private var scoreMaximum: Float = 0f
    var scoreCurrent: Float = 0f
        private set

    @KeyUtil.ScoreFormat
    private var scoreFormat: String? = null

    fun setScoreFormat(
        @KeyUtil.ScoreFormat scoreFormat: String,
    ) {
        this.scoreFormat = scoreFormat
        when (scoreFormat) {
            KeyUtil.POINT_10_DECIMAL -> scoreMaximum = 10.0f
            KeyUtil.POINT_100 -> scoreMaximum = 100f
            KeyUtil.POINT_10 -> scoreMaximum = 10f
            KeyUtil.POINT_5 -> scoreMaximum = 5f
            KeyUtil.POINT_3 -> scoreMaximum = 3f
        }
        setDefaultDeltaFactor()
        setScoreMaximum()
    }

    override fun setDefaultDeltaFactor() {
        when (scoreFormat) {
            KeyUtil.POINT_10_DECIMAL -> {
                keyboardType = KeyboardType.Decimal
                deltaFactor = 0.1f
            }
            else -> deltaFactor = 1f
        }
    }

    private fun setScoreMaximum() {
        maximumLabel =
            if (CompatUtil.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL)) {
                String.format(Locale.getDefault(), "/ %.1f", scoreMaximum)
            } else {
                String.format(Locale.getDefault(), "/ %d", scoreMaximum.toInt())
            }
    }

    fun setScoreCurrent(scoreCurrent: Float) {
        this.scoreCurrent = scoreCurrent
        setCurrentText(formatScore(scoreCurrent))
    }

    private fun boundCheck(delta: Float): Boolean = if (scoreMaximum < 1f) {
        delta > -0.1f
    } else {
        delta > -0.1f && delta <= scoreMaximum
    }

    private fun scoreChange(delta: Float) {
        if (boundCheck(delta)) {
            scoreCurrent = delta
            setCurrentText(formatScore(scoreCurrent))
        }
    }

    override fun onIncrement() {
        scoreChange(getRoundedScore(scoreCurrent + deltaFactor))
    }

    override fun onDecrement() {
        scoreChange(getRoundedScore(scoreCurrent - deltaFactor))
    }

    override fun isValidInput(value: String): Boolean {
        val separator = DecimalFormatSymbols.getInstance().decimalSeparator
        return value.all { character ->
            character.isDigit() || (keyboardType == KeyboardType.Decimal && character == separator)
        }
    }

    override fun handleValueChange(currentChange: TextFieldValue) {
        val text = currentChange.text
        if (!isValidInput(text)) return

        var temporaryValue = 0f
        try {
            temporaryValue =
                if (text.isNotEmpty()) {
                    DecimalFormat("#.#").parse(text)?.toFloat() ?: 0f
                } else {
                    0f
                }
        } catch (e: ParseException) {
            Timber.e(e)
        }

        val shouldScaleDecimalInput =
            CompatUtil.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL) && !boundCheck(temporaryValue)
        if (shouldScaleDecimalInput) {
            temporaryValue /= 10
            scoreCurrent = temporaryValue
            if (boundCheck(temporaryValue)) {
                setCurrentText(formatScore(scoreCurrent))
            } else {
                currentText = currentChange
            }
        }
        if (boundCheck(temporaryValue)) {
            scoreCurrent = temporaryValue
            if (!shouldScaleDecimalInput) currentText = currentChange
        } else if (!shouldScaleDecimalInput && boundCheck(scoreCurrent)) {
            setCurrentText(formatScore(scoreCurrent))
        } else if (!shouldScaleDecimalInput) {
            currentText = currentChange
        }
    }

    private fun formatScore(score: Float): String = if (CompatUtil.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL)) {
        String.format(Locale.getDefault(), "%.1f", score)
    } else {
        String.format(Locale.getDefault(), "%d", score.toInt())
    }

    private fun getRoundedScore(score: Float): Float {
        val formatSymbols = DecimalFormatSymbols(Locale.US)
        return DecimalFormat("#.#", formatSymbols).format(score).toFloat()
    }
}
