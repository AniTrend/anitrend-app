package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.binding.basicText
import com.mxt.anitrend.databinding.BottomSheetMessageBinding
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/11/03.
 * Displays messages with two buttons
 */
class BottomSheetMessage :
    BottomSheetBase<Unit>(),
    View.OnClickListener {
    private var binding: BottomSheetMessageBinding? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetMessage = BottomSheetMessage().apply {
            arguments = bundle
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetMessageBinding.inflate(layoutInflater)
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        createBottomSheetBehavior(requireNotNull(binding).root)
        requireNotNull(binding).bottomPositive.setOnClickListener(this)
        requireNotNull(binding).bottomNegative.setOnClickListener(this)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        binding?.bottomText?.basicText(getString(mText))
        if (mPositive != 0) {
            binding?.bottomPositive?.setText(mPositive)
        } else {
            binding?.bottomPositive?.visibility = View.GONE
        }

        if (mNegative != 0) {
            binding?.bottomNegative?.setText(mNegative)
        } else {
            binding?.bottomNegative?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.bottom_positive -> {
                bottomSheetChoice?.onPositiveButton()
                closeDialog()
            }
            R.id.bottom_negative -> {
                bottomSheetChoice?.onNegativeButton()
                closeDialog()
            }
        }
    }

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)

        fun setText(
            @StringRes text: Int,
        ): Builder {
            bundle.putInt(KeyUtil.arg_text, text)
            return this
        }
    }
}
