package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.databinding.BottomSheetSpoilerBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.KeyUtil

class BottomSheetSpoiler : BottomSheetBase<Unit>() {
    private var binding: BottomSheetSpoilerBinding? = null

    private val text by lazy(LazyThreadSafetyMode.NONE) {
        fromBundle(arguments)
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     *
     * @param savedInstanceState
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetSpoilerBinding.inflate(requireContext().getLayoutInflater())
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        createBottomSheetBehavior(requireNotNull(binding).root)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        binding?.bottomText?.richMarkDown(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)

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

        /**
         * Documented legacy channel: the spoiler body is rendered text, not identity.
         * It stays on arg_text (set by [Builder.setText]) until a spoiler-state model
         * is designed. Reads mirror the pre-refactor getter exactly (absent → null).
         */
        fun fromBundle(bundle: Bundle?): String? = resolveLegacyText(bundle?.getString(KeyUtil.arg_text))

        @VisibleForTesting
        internal fun resolveLegacyText(raw: String?): String? = raw
    }
}
