package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.View
import com.mxt.anitrend.R
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.util.Locale

class ScoreWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressWidget(context, attrs, defStyleAttr) {

    private var scoreMaximum: Float = 0f
    var scoreCurrent: Float = 0f
        private set

    @KeyUtil.ScoreFormat
    private var scoreFormat: String? = null

    fun setScoreFormat(@KeyUtil.ScoreFormat scoreFormat: String) {
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

    /**
     * Sets the default delta value for manipulating scores or progress
     */
    override fun setDefaultDeltaFactor() {
        when (scoreFormat) {
            KeyUtil.POINT_10_DECIMAL -> {
                val separator = DecimalFormatSymbols.getInstance().decimalSeparator
                binding.progressCurrent.keyListener = DigitsKeyListener.getInstance("0123456789$separator")
                deltaFactor = 0.1f
            }
            else -> deltaFactor = 1f
        }
    }

    private fun setScoreMaximum() {
        binding.progressMaximum.visibility = VISIBLE
        if (CompatUtil.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL))
            binding.progressMaximum.text = String.format(Locale.getDefault(), "/ %.1f", scoreMaximum)
        else
            binding.progressMaximum.text = String.format(Locale.getDefault(), "/ %d", scoreMaximum.toInt())
    }

    fun setScoreCurrent(scoreCurrent: Float) {
        this.scoreCurrent = scoreCurrent
        if (CompatUtil.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL))
            binding.progressCurrent.setText(String.format(Locale.getDefault(), "%.1f", scoreCurrent))
        else
            binding.progressCurrent.setText(String.format(Locale.getDefault(), "%d", scoreCurrent.toInt()))
    }

    private fun boundCheck(delta: Float): Boolean {
        if (scoreMaximum < 1f)
            return delta > -0.1f
        return delta > -0.1f && delta <= scoreMaximum
    }

    private fun scoreChange(delta: Float) {
        if (boundCheck(delta)) {
            scoreCurrent = delta
            if (CompatUtil.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL))
                binding.progressCurrent.setText(String.format(Locale.getDefault(), "%.1f", scoreCurrent))
            else
                binding.progressCurrent.setText(String.format(Locale.getDefault(), "%d", scoreCurrent.toInt()))
            binding.progressCurrent.setSelection(binding.progressCurrent.text.length)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.progress_increment -> {
                isNotDirectInput = true
                scoreChange(getRoundedScore(scoreCurrent + deltaFactor))
                isNotDirectInput = false
            }
            R.id.progress_decrement -> {
                isNotDirectInput = true
                scoreChange(getRoundedScore(scoreCurrent - deltaFactor))
                isNotDirectInput = false
            }
        }
    }

    private fun getRoundedScore(score: Float): Float {
        val formatSymbols = DecimalFormatSymbols(Locale.US)
        return DecimalFormat("#.#", formatSymbols).format(score).toFloat()
    }

    override fun afterTextChanged(editable: Editable) {
        if (isNotDirectInput)
            return
        val currentChange = editable.toString()
        var temporaryValue = 0f
        try {
            temporaryValue = if (!TextUtils.isEmpty(currentChange))
                DecimalFormat("#.#").parse(currentChange)?.toFloat() ?: 0f
            else
                0f
        } catch (e: ParseException) {
            Timber.e(e)
        }
        if (scoreFormat == KeyUtil.POINT_10_DECIMAL && !boundCheck(temporaryValue)) {
            temporaryValue /= 10
            scoreCurrent = temporaryValue
            binding.progressCurrent.post { scoreChange(scoreCurrent) }
        }
        if (boundCheck(temporaryValue))
            scoreCurrent = temporaryValue
        else
            binding.progressCurrent.post { scoreChange(scoreCurrent) }
    }
}
