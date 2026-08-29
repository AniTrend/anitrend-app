package com.mxt.anitrend.view.fragment.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentCharacterOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.util.CompatUtil

/**
 * Renders the Character overview section without owning a Fragment or a data
 * source. The parent destination owns the ViewModel and lifecycle collection.
 */
class CharacterOverviewSection(
    private val onRetry: () -> Unit,
) {

    private var binding: FragmentCharacterOverviewBinding? = null
    private var model: MediaCharacter? = null

    /** Inflates and initializes the character overview view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentCharacterOverviewBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.characterImg.setOnClickListener {
            CompatUtil.imagePreview(
                sectionBinding.root,
                model?.image?.large,
                R.string.image_preview_error_character_image,
            )
        }
        return sectionBinding.root
    }

    /** Shows the loading state for the overview. */
    fun renderLoading() {
        binding?.stateLayout?.showLoading()
    }

    /** Renders [character] in the overview. */
    fun render(character: MediaCharacter) {
        val current = binding ?: return
        model = character
        AspectImageView.setImage(current.characterImg, character.image)
        current.characterNameText.text = character.name?.fullName
        current.characterNativeText.text = character.name?.original
        current.characterAlternativeText.htmlText(character.name?.alternativeFormatted)
        current.characterSummaryText.htmlText(character.description)
        current.stateLayout.showContent()
    }

    /** Shows [message] and exposes the retry action. */
    fun renderError(message: String) {
        binding?.stateLayout?.showError(
            binding?.root?.context?.getCompatDrawable(R.drawable.ic_warning_white_18dp),
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
