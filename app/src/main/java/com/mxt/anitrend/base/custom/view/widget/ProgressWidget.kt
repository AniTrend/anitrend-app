package com.mxt.anitrend.base.custom.view.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetProgressBinding
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.extension.getLayoutInflater
import java.util.Locale

open class ProgressWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener,
    TextWatcher {
    private var progressMaximum: Int = 0
    var progressCurrent: Int = 0
        private set

    protected lateinit var binding: WidgetProgressBinding
    protected var isNotDirectInput: Boolean = false
    protected var deltaFactor: Float = 0f

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

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetProgressBinding.inflate(context.getLayoutInflater(), this, true)
        binding.progressCurrent.setTextColor(context.getCompatColorAttr(R.attr.colorOnSurface))
        binding.progressMaximum.visibility = GONE
        binding.progressDecrement.setOnClickListener(this)
        binding.progressIncrement.setOnClickListener(this)
        setDefaultDeltaFactor()
    }

    /**
     * Sets the default delta value for manipulating scores or progress
     */
    protected open fun setDefaultDeltaFactor() {
        deltaFactor = 1f
    }

    fun setProgressMaximum(progressMaximum: Int) {
        this.progressMaximum = progressMaximum
        binding.progressMaximum.visibility = VISIBLE
        binding.progressMaximum.text = String.format(Locale.getDefault(), "/ %d", progressMaximum)
    }

    fun setProgressCurrent(progressCurrent: Int) {
        this.progressCurrent = progressCurrent
        binding.progressCurrent.setText(progressCurrent.toString())
    }

    fun getProgressMaximum(): Int = progressMaximum

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        binding.progressCurrent.removeTextChangedListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.progressCurrent.addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        onViewRecycled()
        super.onDetachedFromWindow()
    }

    private fun boundCheck(delta: Int): Boolean {
        if (progressMaximum < 1) {
            return delta > -1
        }
        return delta > -1 && delta <= progressMaximum
    }

    private fun progressChange(delta: Int) {
        if (boundCheck(delta)) {
            progressCurrent = delta
            binding.progressCurrent.setText(progressCurrent.toString())
            binding.progressCurrent.setSelection(binding.progressCurrent.text?.length ?: 0)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.progress_increment -> {
                isNotDirectInput = true
                progressChange(progressCurrent + deltaFactor.toInt())
                isNotDirectInput = false
            }
            R.id.progress_decrement -> {
                isNotDirectInput = true
                progressChange(progressCurrent - deltaFactor.toInt())
                isNotDirectInput = false
            }
        }
    }

    override fun beforeTextChanged(
        charSequence: CharSequence,
        start: Int,
        count: Int,
        after: Int,
    ) = Unit

    override fun onTextChanged(
        charSequence: CharSequence,
        start: Int,
        before: Int,
        count: Int,
    ) = Unit

    override fun afterTextChanged(editable: Editable) {
        if (isNotDirectInput) {
            return
        }
        val currentChange = editable.toString()
        val temporaryValue = if (!TextUtils.isEmpty(currentChange)) currentChange.toInt() else 0
        if (boundCheck(temporaryValue)) {
            progressCurrent = temporaryValue
        } else {
            binding.progressCurrent.post { progressChange(progressCurrent) }
        }
    }
}
