package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.databinding.BottomSheetSpoilerBinding
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

class BottomSheetSpoiler : BottomSheetBase<Unit>() {
    private var binding: BottomSheetSpoilerBinding? = null

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
        binding = BottomSheetSpoilerBinding.inflate(CompatUtil.getLayoutInflater(requireContext()))
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
    }
}
