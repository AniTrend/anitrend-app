package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentCharacterOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.CharacterOverviewViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharacterOverviewFragment : Fragment() {

    private var _binding: FragmentCharacterOverviewBinding? = null
    private val binding get() = _binding!!

    private var characterId: Long = 0
    private var model: MediaCharacter? = null

    private val characterOverviewViewModel: CharacterOverviewViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): CharacterOverviewFragment = CharacterOverviewFragment().apply {
            arguments = args
        }

        /**
         * Resolves the character identity from the fragment arguments.
         *
         * The typed [CharacterScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] extra is bridged with its exact raw value (0 or
         * negative ids pass through, mirroring the pre-refactor getter).
         */
        fun fromBundle(bundle: Bundle?): CharacterScreenParam? = resolve(
            typed = bundle?.screenParam<CharacterScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
        )

        @VisibleForTesting
        internal fun resolve(typed: CharacterScreenParam?, legacyId: Long): CharacterScreenParam? {
            typed?.let { param ->
                if (param.characterId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy value.
            }
            return CharacterScreenParam(characterId = legacyId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Resolve the destination through the typed character parameter, falling back
        // to the legacy wire key forwarded by the pager/activity for pre-bridge callers.
        fromBundle(arguments)?.let { args ->
            characterId = args.characterId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCharacterOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stateLayout.showLoading()
        binding.characterImg.setOnClickListener { onImageClick() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterOverviewViewModel.state.collect { state ->
                    when (state) {
                        is CharacterOverviewViewModel.UiState.Loading -> {
                            binding.stateLayout.showLoading()
                        }
                        is CharacterOverviewViewModel.UiState.Success -> {
                            model = state.character
                            bindCharacter(state.character)
                        }
                        is CharacterOverviewViewModel.UiState.Error -> {
                            binding.stateLayout.showError(
                                requireContext().getCompatDrawable(R.drawable.ic_warning_white_18dp, R.color.colorStateBlue),
                                state.message,
                                getString(R.string.try_again),
                            ) { loadCharacter() }
                        }
                    }
                }
            }
        }

        loadCharacter()
    }

    private fun loadCharacter() {
        characterOverviewViewModel.load(characterId)
    }

    private fun bindCharacter(character: MediaCharacter) {
        AspectImageView.setImage(binding.characterImg, character.image)
        binding.characterNameText.text = character.name?.fullName
        binding.characterNativeText.text = character.name?.original
        binding.characterAlternativeText.htmlText(character.name?.alternativeFormatted)
        binding.characterSummaryText.htmlText(character.description)
        binding.stateLayout.showContent()
    }

    private fun onImageClick() {
        CompatUtil.imagePreview(
            requireView(),
            model?.image?.large,
            R.string.image_preview_error_character_image,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
