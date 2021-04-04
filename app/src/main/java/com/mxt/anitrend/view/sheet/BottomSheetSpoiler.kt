package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.util.KeyUtil

class BottomSheetSpoiler : BottomSheetBase<Unit>() {

    private lateinit var richMarkdownTextView: RichMarkdownTextView

    private val text by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getString(KeyUtil.arg_text)
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     *
     * @param savedInstanceState
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val contentView = View.inflate(context, R.layout.bottom_sheet_spoiler, null)
        dialog.setContentView(contentView)
        unbinder = ButterKnife.bind(this, dialog)
        richMarkdownTextView = contentView.findViewById(R.id.bottom_text)
        createBottomSheetBehavior(contentView)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        richMarkdownTextView.richMarkDown(text)
    }

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> {
            return newInstance(bundle)
        }

        fun setText(text: String?): BottomSheetBuilder {
            bundle.putString(KeyUtil.arg_text, text)
            return this
        }
    }

    companion object {
        fun newInstance(bundle: Bundle): BottomSheetSpoiler {
            val fragment = BottomSheetSpoiler()
            fragment.arguments = bundle
            return fragment
        }
    }
}