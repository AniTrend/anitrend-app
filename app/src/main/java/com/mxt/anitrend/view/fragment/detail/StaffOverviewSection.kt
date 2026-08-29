package com.mxt.anitrend.view.fragment.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentStaffOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.util.CompatUtil

/**
 * Renders the Staff overview without owning a Fragment or a data source.
 */
class StaffOverviewSection(
    private val onRetry: () -> Unit,
) {

    private var binding: FragmentStaffOverviewBinding? = null
    private var model: StaffBase? = null

    /** Inflates and initializes the staff overview view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentStaffOverviewBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.staffImg.setOnClickListener {
            CompatUtil.imagePreview(
                sectionBinding.root,
                model?.image?.large,
                R.string.image_preview_error_staff_image,
            )
        }
        return sectionBinding.root
    }

    /** Shows the loading state for the overview. */
    fun renderLoading() {
        binding?.stateLayout?.showLoading()
    }

    /** Renders [staff] in the overview. */
    fun render(staff: StaffBase) {
        val current = binding ?: return
        model = staff
        AspectImageView.setImage(current.staffImg, staff.image)
        current.staffNameText.text = staff.name?.fullName
        current.staffLanguageText.text = CompatUtil.capitalizeWords(staff.language)
        current.staffSummaryText.htmlText(staff.description)
        current.stateLayout.showContent()
    }

    /** Shows [message] and exposes the retry action. */
    fun renderError(message: String) {
        binding?.stateLayout?.showError(
            binding?.root?.context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
            message,
            binding?.root?.context?.getString(R.string.try_again),
        ) { onRetry() }
    }

    /** Releases the current view binding and rendered model. */
    fun destroyView() {
        binding = null
        model = null
    }
}
